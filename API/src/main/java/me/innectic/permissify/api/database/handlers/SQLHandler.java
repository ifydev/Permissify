/*
*
* This file is part of Permissify, licensed under the MIT License (MIT).
* Copyright (c) Innectic
* Copyright (c) contributors
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
 */
package me.innectic.permissify.api.database.handlers;

import me.innectic.permissify.api.PermissifyAPI;
import me.innectic.permissify.api.database.ConnectionError;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.database.ConnectionInformation;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.permission.GroupInheritance;
import me.innectic.permissify.api.profile.PermissifyProfile;
import me.innectic.permissify.api.util.Tristate;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 6/8/2017
 */
public class SQLHandler extends DatabaseHandler {

    private boolean isUsingSQLite = false;
    private final String baseConnectionUrl;

    public SQLHandler(ConnectionInformation connectionInformation) {
        super(connectionInformation);

        String type;
        String databaseURL;

        if (connectionInformation.getMeta().containsKey("sqlite")) {
            type = "sqlite";
            Map sqliteData = (Map) connectionInformation.getMeta().get("sqlite");
            databaseURL = (String) sqliteData.get("file");
            isUsingSQLite = true;
        } else {
            type = "mysql";
            databaseURL = "//" + connectionInformation.getUrl() + ":" + connectionInformation.getPort();
        }
        baseConnectionUrl = "jdbc:" + type + ":" + databaseURL;
    }

    /**
     * Get a connection to the mysql server.
     *
     * @return an optional connection, filled if successful.
     */
    private Optional<Connection> getConnection() {
        try {
            if (isUsingSQLite) return Optional.ofNullable(DriverManager.getConnection(baseConnectionUrl));
            String connectionURL = baseConnectionUrl + "/" + connectionInformation.getDatabase();
            return Optional.ofNullable(DriverManager.getConnection(connectionURL, connectionInformation.getUsername(), connectionInformation.getPassword()));
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void initialize() {
        // Make sure that the cache is empty
        this.cachedPermissions = new HashMap<>();

        try {
            Connection connection;
            if (isUsingSQLite) connection = DriverManager.getConnection(baseConnectionUrl);
            else connection = DriverManager.getConnection(baseConnectionUrl, connectionInformation.getUsername(), connectionInformation.getPassword());
            if (connection == null) {
                System.out.println("Could not connect to database during initialization.");
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
                return;
            }

            String database = connectionInformation.getDatabase();

            if (!isUsingSQLite) {
                PreparedStatement statement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + database);
                statement.execute();
                statement.close();
                database += ".";
            } else database = "";

            PreparedStatement groupMembersStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database +
                    "groupMembers (uuid VARCHAR(767) NOT NULL, `group` VARCHAR(700) NOT NULL, `primary` TINYINT NOT NULL, ladderPosition INTEGER NOT NULL)");
            groupMembersStatement.execute();
            groupMembersStatement.close();

            PreparedStatement groupPermissionsStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database +"groupPermissions (groupName VARCHAR(767) NOT NULL, permission VARCHAR(767) NOT NULL)");
            groupPermissionsStatement.execute();
            groupPermissionsStatement.close();
            try (PreparedStatement inheritance = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database +
                    "groupInheritance (childGroup VARCHAR(100) NOT NULL, parentGroup VARCHAR(100) NOT NULL, PRIMARY KEY (childGroup,parentGroup))")) {
                inheritance.execute();
            }

            PreparedStatement groupsStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database +
                    "groups (name VARCHAR(100) NOT NULL UNIQUE, displayName VARCHAR(100) NOT NULL, prefix VARCHAR(100) NOT NULL, suffix VARCHAR(100) NOT NULL, chatcolor VARCHAR(4) NOT NULL, defaultGroup TINYINT NOT NULL, ladder VARCHAR(767))");
            groupsStatement.execute();
            groupsStatement.close();

            PreparedStatement playerPermissionsStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database + "playerPermissions (uuid VARCHAR(767) NOT NULL, permission VARCHAR(767) NOT NULL, granted TINYINT NOT NULL)");
            playerPermissionsStatement.execute();
            playerPermissionsStatement.close();

            PreparedStatement superAdminStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database + "superAdmin (uuid VARCHAR(767) NOT NULL)");
            superAdminStatement.execute();
            superAdminStatement.close();

            PreparedStatement ladderStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database + "ladders (name VARCHAR(767))");
            ladderStatement.execute();
            ladderStatement.close();

            PreparedStatement ladderLevelStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database + "ladderLevels (ladder VARCHAR(767), name VARCHAR(767), power INTEGER)");
            ladderLevelStatement.execute();
            ladderLevelStatement.close();

            if (!hasFormattingTable(connection, database)) {
                PreparedStatement formattingStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + database + "formatting (`format` VARCHAR(400) NOT NULL, formatter VARCHAR(200) NOT NULL)");
                formattingStatement.execute();
                formattingStatement.close();

                PreparedStatement defaultChatFormat = connection.prepareStatement("INSERT INTO " + database + "formatting (`format`, formatter) VALUES (?,?)");
                defaultChatFormat.setString(1, "{group} {username}: {message}");
                defaultChatFormat.setString(2, "chat");
                defaultChatFormat.execute();
                defaultChatFormat.close();

                PreparedStatement defaultWhisperFormat = connection.prepareStatement("INSERT INTO " + database + "formatting (`format`, formatter) VALUES (?,?)");
                defaultWhisperFormat.setString(1, "{senderGroup} {username} > {receiverGroup} {to}: {message}");
                defaultWhisperFormat.setString(2, "whisper");
                defaultWhisperFormat.execute();
                defaultWhisperFormat.close();
            }

            connection.close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
    }

    @Override
    public void reload(List<UUID> onlinePlayers) {
        cachedGroups = new HashMap<>();
        cachedPermissions = new HashMap<>();
        superAdmins = new ArrayList<>();
        defaultGroup = Optional.empty();

        loadSuperAdmins();
        loadGroups();

        onlinePlayers.forEach(this::getPermissions);
    }

    @Override
    protected void loadGroups() {
        Optional<Connection> connection = getConnection();
        if (connection.isEmpty()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }

        try {
            PreparedStatement groupStatement = connection.get().prepareStatement("SELECT * from groups");
            ResultSet groupResults = groupStatement.executeQuery();
            while (groupResults.next()) {
                String groupName = groupResults.getString("name");
                String displayName = groupResults.getString("displayName");
                PermissionGroup group = new PermissionGroup(groupName,
                        displayName,
                        groupResults.getString("chatcolor"),
                        groupResults.getString("prefix"),
                        groupResults.getString("suffix"));

                PreparedStatement groupPermissionsStatement = connection.get().prepareStatement("SELECT * FROM groupPermissions WHERE groupName=?");
                groupPermissionsStatement.setString(1, group.getName());
                ResultSet groupPermissionsResult = groupPermissionsStatement.executeQuery();

                while (groupPermissionsResult.next()) {
                    group.addPermission(groupPermissionsResult.getString("permission"));
                }
                groupPermissionsResult.close();
                groupPermissionsStatement.close();

                PreparedStatement groupMembersStatement = connection.get().prepareStatement("SELECT uuid,`primary` FROM groupMembers WHERE `group`=?");
                groupMembersStatement.setString(1, group.getName());

                ResultSet groupMembersResults = groupMembersStatement.executeQuery();
                while (groupMembersResults.next()) {
                    group.addPlayer(UUID.fromString(groupMembersResults.getString("uuid")), groupMembersResults.getBoolean("primary"));
                }

                groupMembersResults.close();
                groupMembersStatement.close();
                try (PreparedStatement inheritance = connection.get().prepareStatement("SELECT parentGroup FROM groupInheritance WHERE childGroup=?")) {
                    inheritance.setString(1, group.getName());
                    try (ResultSet parents = inheritance.executeQuery()) {
                        while (parents.next()) group.getParents().add(parents.getString("parentGroup"));
                    }
                }
                cachedGroups.put(groupName, group);

                if (groupResults.getBoolean("defaultGroup")) defaultGroup = Optional.of(group);
            }
            groupResults.close();
            groupStatement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
    }

    @Override
    protected void loadSuperAdmins() {
        Optional<Connection> connection = getConnection();
        if (connection.isEmpty()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }
        try {
            // Load all super admins
            PreparedStatement adminStatement = connection.get().prepareStatement("SELECT uuid FROM superAdmin");
            ResultSet adminResults = adminStatement.executeQuery();
            while (adminResults.next()) {
                superAdmins.add(UUID.fromString(adminResults.getString("uuid")));
            }
            adminResults.close();
            adminStatement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
    }

    @Override
    public void drop() {
        cachedGroups = new HashMap<>();
        cachedPermissions = new HashMap<>();
        superAdmins = new ArrayList<>();
    }

    @Override
    public void loadProfile(PermissifyProfile profile) {
        Map<String, PermissionGroup> merged = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        java.util.stream.Stream.concat(cachedGroups.values().stream(), profile.getGroups().values().stream()).forEach(group -> {
            PermissionGroup combined = merged.computeIfAbsent(group.getName(), name -> new PermissionGroup(
                    group.getName(), group.getDisplayName(), group.getChatColor(), group.getPrefix(), group.getSuffix()));
            combined.getParents().addAll(group.getParents());
        });
        GroupInheritance.validate(merged);
        boolean saved = transaction(connection -> {
            for (PermissionGroup group : profile.getGroups().values()) {
                String name = getGroup(group.getName()).map(PermissionGroup::getName).orElse(group.getName());
                if (getGroup(name).isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement("INSERT INTO groups (name,displayName,prefix,suffix,chatcolor,defaultGroup) VALUES (?,?,?,?,?,?)")) {
                        statement.setString(1, name);
                        statement.setString(2, group.getDisplayName());
                        statement.setString(3, group.getPrefix());
                        statement.setString(4, group.getSuffix());
                        statement.setString(5, group.getChatColor());
                        statement.setBoolean(6, false);
                        statement.executeUpdate();
                    }
                }
                for (Permission permission : group.getPermissions()) {
                    if (!permission.isGranted()) continue;
                    try (PreparedStatement statement = connection.prepareStatement("INSERT INTO groupPermissions (groupName,permission) SELECT ?,? WHERE NOT EXISTS (SELECT 1 FROM groupPermissions WHERE groupName=? AND permission=?)")) {
                        statement.setString(1, name);
                        statement.setString(2, permission.getPermission());
                        statement.setString(3, name);
                        statement.setString(4, permission.getPermission());
                        statement.executeUpdate();
                    }
                }
                for (var member : group.getPlayers().entrySet()) {
                    try (PreparedStatement statement = connection.prepareStatement("INSERT INTO groupMembers (uuid,`group`,`primary`,ladderPosition) SELECT ?,?,?,0 WHERE NOT EXISTS (SELECT 1 FROM groupMembers WHERE uuid=? AND `group`=?)")) {
                        statement.setString(1, member.getKey().toString());
                        statement.setString(2, name);
                        statement.setBoolean(3, false);
                        statement.setString(4, member.getKey().toString());
                        statement.setString(5, name);
                        statement.executeUpdate();
                    }
                    if (member.getValue()) {
                        try (PreparedStatement statement = connection.prepareStatement("UPDATE groupMembers SET `primary`=CASE WHEN `group`=? THEN 1 ELSE 0 END WHERE uuid=?")) {
                            statement.setString(1, name);
                            statement.setString(2, member.getKey().toString());
                            statement.executeUpdate();
                        }
                    }
                }
            }
            for (PermissionGroup group : profile.getGroups().values()) {
                for (String parentName : group.getParents()) {
                    try (PreparedStatement statement = connection.prepareStatement("INSERT INTO groupInheritance (childGroup,parentGroup) SELECT ?,? WHERE NOT EXISTS (SELECT 1 FROM groupInheritance WHERE childGroup=? AND parentGroup=?)")) {
                        String child = merged.get(group.getName()).getName();
                        String parent = merged.get(parentName).getName();
                        statement.setString(1, child);
                        statement.setString(2, parent);
                        statement.setString(3, child);
                        statement.setString(4, parent);
                        statement.executeUpdate();
                    }
                }
            }
            for (var player : profile.getPlayerPermissions().entrySet()) {
                for (Permission permission : player.getValue()) {
                    try (PreparedStatement statement = connection.prepareStatement("INSERT INTO playerPermissions (uuid,permission,granted) SELECT ?,?,? WHERE NOT EXISTS (SELECT 1 FROM playerPermissions WHERE uuid=? AND permission=?)")) {
                        statement.setString(1, player.getKey().toString());
                        statement.setString(2, permission.getPermission());
                        statement.setBoolean(3, permission.isGranted());
                        statement.setString(4, player.getKey().toString());
                        statement.setString(5, permission.getPermission());
                        statement.executeUpdate();
                    }
                    try (PreparedStatement statement = connection.prepareStatement("UPDATE playerPermissions SET granted=? WHERE uuid=? AND permission=?")) {
                        statement.setBoolean(1, permission.isGranted());
                        statement.setString(2, player.getKey().toString());
                        statement.setString(3, permission.getPermission());
                        statement.executeUpdate();
                    }
                }
            }
            for (UUID uuid : profile.getSuperAdmins()) {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO superAdmin (uuid) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM superAdmin WHERE uuid=?)")) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                }
            }
        });
        if (!saved) throw new IllegalStateException("Unable to import profile");
        reload(new ArrayList<>(profile.getPlayerPermissions().keySet()));
        if (profile.getDefaultGroup() != null) setDefaultGroup(getGroup(profile.getDefaultGroup().getName()).orElse(null));
    }

    @Override
    public void addPermission(UUID uuid, String... permissions) {
        List<Permission> current = getPermissions(uuid);
        List<String> added = Arrays.stream(permissions).distinct()
                .filter(permission -> current.stream().noneMatch(existing -> existing.getPermission().equals(permission))).toList();
        boolean saved = transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO playerPermissions (uuid,permission,granted) VALUES (?,?,?)")) {
                for (String permission : added) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, permission);
                    statement.setBoolean(3, true);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
        if (saved) {
            added.forEach(permission -> current.add(new Permission(permission, true)));
            cachedPermissions.put(uuid, current);
        }
    }

    @Override
    public void removePermission(UUID uuid, String... permissions) {
        boolean saved = transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM playerPermissions WHERE uuid=? AND permission=?")) {
                for (String permission : permissions) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, permission);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
        if (saved && cachedPermissions.containsKey(uuid))
            cachedPermissions.get(uuid).removeIf(permission -> Arrays.asList(permissions).contains(permission.getPermission()));
    }

    @Override
    public boolean isGrantedPermission(UUID uuid, String permission) {
        return getPermissions(uuid).stream().anyMatch(entry -> entry.getPermission().equals(permission) && entry.isGranted());
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        return getPermissions(uuid).stream().anyMatch(entry -> entry.getPermission().equals(permission));
    }

    @Override
    public List<Permission> getPermissions(UUID uuid) {
        if (cachedPermissions.containsKey(uuid)) return cachedPermissions.get(uuid);
        Optional<Connection> connection = getConnection();
        List<Permission> permissions = new ArrayList<>();

        if (connection.isEmpty()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return permissions;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT permission,granted FROM playerPermissions WHERE uuid=?");
            statement.setString(1, uuid.toString());
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                permissions.add(new Permission(results.getString("permission"), results.getBoolean("granted")));
            }
            // Cleanup
            results.close();
            statement.close();
            connection.get().close();
            cachedPermissions.put(uuid, permissions);
            return permissions;
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public Tristate createGroup(String name, String displayName, String prefix, String suffix, String chatColor) {
        if (getGroup(name).isPresent()) return Tristate.NONE;
        boolean saved = transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO groups (name,displayName,prefix,suffix,chatcolor,defaultGroup) VALUES (?,?,?,?,?,?)")) {
                statement.setString(1, name);
                statement.setString(2, displayName);
                statement.setString(3, prefix);
                statement.setString(4, suffix);
                statement.setString(5, chatColor);
                statement.setBoolean(6, false);
                statement.executeUpdate();
            }
        });
        if (!saved) return Tristate.FALSE;
        cachedGroups.put(name, new PermissionGroup(name, displayName, chatColor, prefix, suffix));
        return Tristate.TRUE;
    }

    @Override
    public Tristate deleteGroup(String name) {
        Optional<PermissionGroup> group = getGroup(name);
        if (group.isEmpty()) return Tristate.NONE;
        String canonical = group.get().getName();
        boolean saved = transaction(connection -> {
            for (String query : List.of("DELETE FROM groupMembers WHERE `group`=?",
                    "DELETE FROM groupPermissions WHERE groupName=?", "DELETE FROM groupInheritance WHERE childGroup=?",
                    "DELETE FROM groupInheritance WHERE parentGroup=?", "DELETE FROM groups WHERE name=?")) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, canonical);
                    statement.executeUpdate();
                }
            }
        });
        if (!saved) return Tristate.FALSE;
        cachedGroups.remove(canonical);
        cachedGroups.values().forEach(current -> current.getParents().remove(canonical));
        if (defaultGroup.map(current -> current.getName().equalsIgnoreCase(canonical)).orElse(false)) defaultGroup = Optional.empty();
        return Tristate.TRUE;
    }

    @Override
    public Optional<PermissionGroup> getGroup(String name) {
        return cachedGroups.values().stream().filter(group -> group.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public Tristate addPlayerToGroup(UUID uuid, PermissionGroup group) {
        if (group.hasPlayer(uuid)) return Tristate.NONE;
        boolean saved = transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO groupMembers (uuid,`group`,`primary`,ladderPosition) VALUES (?,?,?,?)")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, group.getName());
                statement.setBoolean(3, false);
                statement.setInt(4, 0);
                statement.executeUpdate();
            }
        });
        if (!saved) return Tristate.FALSE;
        group.addPlayer(uuid, false);
        cachedGroups.put(group.getName(), group);
        return Tristate.TRUE;
    }

    @Override
    public Tristate removePlayerFromGroup(UUID uuid, PermissionGroup group) {
        if (!group.hasPlayer(uuid)) return Tristate.NONE;
        boolean saved = transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM groupMembers WHERE uuid=? AND `group`=?")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, group.getName());
                statement.executeUpdate();
            }
        });
        if (!saved) return Tristate.FALSE;
        group.removePlayer(uuid);
        return Tristate.TRUE;
    }

    @Override
    public Map<String, PermissionGroup> getGroups() {
        return cachedGroups;
    }

    @Override
    public List<PermissionGroup> getGroups(UUID uuid) {
        return cachedGroups.values().stream().filter(group -> group.hasPlayer(uuid)).collect(Collectors.toList());
    }

    @Override
    public Tristate setPrimaryGroup(PermissionGroup group, UUID uuid) {
        if (!group.hasPlayer(uuid) && addPlayerToGroup(uuid, group) != Tristate.TRUE) return Tristate.FALSE;
        if (group.isPrimaryGroup(uuid)) return Tristate.NONE;
        boolean saved = transaction(connection -> {
            try (PreparedStatement clear = connection.prepareStatement("UPDATE groupMembers SET `primary`=? WHERE uuid=?");
                 PreparedStatement set = connection.prepareStatement("UPDATE groupMembers SET `primary`=? WHERE uuid=? AND `group`=?")) {
                clear.setBoolean(1, false);
                clear.setString(2, uuid.toString());
                clear.executeUpdate();
                set.setBoolean(1, true);
                set.setString(2, uuid.toString());
                set.setString(3, group.getName());
                set.executeUpdate();
            }
        });
        if (!saved) return Tristate.FALSE;
        getGroups(uuid).forEach(current -> current.setPrimaryGroup(uuid, current == group));
        return Tristate.TRUE;
    }

    @Override
    public Optional<PermissionGroup> getPrimaryGroup(UUID uuid) {
        return getGroups(uuid).stream().filter(group -> group.isPrimaryGroup(uuid)).findFirst();
    }

    @Override
    public void updateCache(UUID uuid) {
        Optional<Connection> connection = getConnection();
        if (connection.isEmpty()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT `group` FROM groupMembers WHERE uuid=?");
            statement.setString(1, uuid.toString());
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                String groupName = results.getString("group");
                Optional<PermissionGroup> group = getGroup(groupName);
                // Get the group from the database, if we don't have have it already
                if (group.isEmpty()) {
                    PreparedStatement groupStatement = connection.get().prepareStatement("SELECT displayName,prefix,suffix,chatcolor FROM groups WHERE name=?");
                    groupStatement.setString(1, groupName);
                    ResultSet groupResults = groupStatement.executeQuery();
                    if (!groupResults.next()) return;
                    PermissionGroup permissionGroup = new PermissionGroup(
                            groupName, groupResults.getString("displayName"), groupResults.getString("chatcolor"), groupResults.getString("prefix"),
                            groupResults.getString("suffix"));
                    groupResults.close();
                    groupStatement.close();
                    PreparedStatement groupPlayersStatement = connection.get().prepareStatement("SELECT uuid,`primary` FROM groupMembers WHERE `group`=?");
                    groupPlayersStatement.setString(1, groupName);
                    ResultSet groupPlayersResult = groupPlayersStatement.executeQuery();
                    while (groupPlayersResult.next()) {
                        permissionGroup.addPlayer(UUID.fromString(groupPlayersResult.getString("uuid")),
                                groupPlayersResult.getBoolean("primary"));
                    }
                    cachedGroups.put(groupName, permissionGroup);
                }
            }
            results.close();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
    }

    @Override
    public boolean addGroupPermission(String group, String... permissions) {
        return changeGroupPermissions(group, true, permissions);
    }

    @Override
    public boolean removeGroupPermission(String group, String... permissions) {
        return changeGroupPermissions(group, false, permissions);
    }

    @Override
    public boolean hasGroupPermission(String group, String permission) {
        return getGroup(group).map(found -> getEffectivePermissions(found).stream()
                .anyMatch(entry -> entry.getPermission().equals(permission))).orElse(false);
    }

    @Override
    public void addSuperAdmin(UUID uuid) {
        if (uuid == null || superAdmins.contains(uuid)) return;
        boolean saved = transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO superAdmin (uuid) VALUES (?)")) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            }
        });
        if (saved) superAdmins.add(uuid);
    }

    @Override
    public boolean isSuperAdmin(UUID uuid) {
        return superAdmins.contains(uuid);
    }

    @Override
    public void removeSuperAdmin(UUID uuid) {
        if (uuid == null) return;
        boolean saved = transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM superAdmin WHERE uuid=?")) {
                statement.setString(1, uuid.toString());
                statement.executeUpdate();
            }
        });
        if (saved) superAdmins.remove(uuid);
    }

    @Override
    public void setDefaultGroup(PermissionGroup group) {
        boolean saved = transaction(connection -> {
            try (PreparedStatement clear = connection.prepareStatement("UPDATE groups SET defaultGroup=?");
                 PreparedStatement set = connection.prepareStatement("UPDATE groups SET defaultGroup=? WHERE name=?")) {
                clear.setBoolean(1, false);
                clear.executeUpdate();
                if (group != null) {
                    set.setBoolean(1, true);
                    set.setString(2, group.getName());
                    set.executeUpdate();
                }
            }
        });
        if (saved) defaultGroup = Optional.ofNullable(group);
    }

    @Override
    public Map<UUID, List<Permission>> getAllPermissions() {
        Optional<Connection> found = getConnection();
        if (found.isEmpty()) throw new IllegalStateException("Unable to export permissions");
        Map<UUID, List<Permission>> permissions = new HashMap<>();
        try (Connection connection = found.get();
             PreparedStatement statement = connection.prepareStatement("SELECT uuid,permission,granted FROM playerPermissions");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) permissions.computeIfAbsent(UUID.fromString(results.getString("uuid")), uuid -> new ArrayList<>())
                    .add(new Permission(results.getString("permission"), results.getBoolean("granted")));
            return permissions;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to export permissions", e);
        }
    }
    @Override
    public Tristate inheritGroup(PermissionGroup parent, PermissionGroup child) {
        if (parent == null || child == null) return Tristate.FALSE;
        PermissionGroup parentGroup = getGroup(parent.getName()).orElse(null);
        PermissionGroup childGroup = getGroup(child.getName()).orElse(null);
        if (parentGroup == null || childGroup == null) return Tristate.FALSE;
        if (GroupInheritance.inheritsFrom(childGroup, parentGroup, cachedGroups)) return Tristate.NONE;
        if (!GroupInheritance.canInherit(parentGroup, childGroup, cachedGroups)) return Tristate.FALSE;
        boolean saved = transaction(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO groupInheritance (childGroup,parentGroup) VALUES (?,?)")) {
                statement.setString(1, childGroup.getName());
                statement.setString(2, parentGroup.getName());
                statement.executeUpdate();
            }
        });
        if (!saved) return Tristate.FALSE;
        childGroup.getParents().add(parentGroup.getName());
        return Tristate.TRUE;
    }
    private boolean changeGroupPermissions(String name, boolean add, String... permissions) {
        Optional<PermissionGroup> found = getGroup(name);
        if (found.isEmpty() || permissions.length == 0) return false;
        PermissionGroup group = found.get();
        List<String> changes = Arrays.stream(permissions).distinct().toList();
        if (changes.stream().anyMatch(permission -> group.hasPermission(permission) == add)) return false;
        boolean saved = transaction(connection -> {
            String query = add ? "INSERT INTO groupPermissions (groupName,permission) VALUES (?,?)"
                    : "DELETE FROM groupPermissions WHERE groupName=? AND permission=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (String permission : changes) {
                    statement.setString(1, group.getName());
                    statement.setString(2, permission);
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        });
        if (saved) changes.forEach(permission -> {
            if (add) group.addPermission(permission);
            else group.removePermission(permission);
        });
        return saved;
    }

    private boolean transaction(SQLAction action) {
        Optional<Connection> found = getConnection();
        if (found.isEmpty()) return false;
        try (Connection connection = found.get()) {
            connection.setAutoCommit(false);
            try {
                action.run(connection);
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException | RuntimeException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            return false;
        }
    }

    @FunctionalInterface
    private interface SQLAction {
        void run(Connection connection) throws SQLException;
    }
    private boolean hasFormattingTable(Connection connection, String database) {
        try {
            if (isUsingSQLite) {
                PreparedStatement statement = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?");
                statement.setString(1, "formatting");
                return statement.executeQuery().next();
            }
            return connection.getMetaData().getTables(database, null, "formatting", new String[]{"TABLE"}).next();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
        return false;
    }
}
