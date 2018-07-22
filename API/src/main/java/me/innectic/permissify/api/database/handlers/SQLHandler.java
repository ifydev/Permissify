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
import me.innectic.permissify.api.profile.PermissifyProfile;
import me.innectic.permissify.api.util.Tristate;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 6/8/2017
 */
public class SQLHandler extends DatabaseHandler {

    private boolean isUsingSQLite = false;
    private String baseConnectionUrl;

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

        loadSuperAdmins();
        loadGroups();

        onlinePlayers.forEach(this::getPermissions);
    }

    @Override
    protected void loadGroups() {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
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
                cachedGroups.put(groupName, group);

                if (groupResults.getBoolean("defaultGroup")) defaultGroup = Optional.of(group);
            }
            groupResults.close();
            groupStatement.close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
    }

    @Override
    protected void loadSuperAdmins() {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
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
        // Load player permissions
        profile.getPlayerPermissions().forEach((uuid, permissions) -> permissions.forEach(permission -> {
            if (!permission.isGranted()) addPermission(uuid, permission.getPermission());
            else removePermission(uuid, permission.getPermission());
        }));
        // Create groups
        profile.getGroups().forEach((key, group) -> {
            createGroup(group.getName(), group.getDisplayName(), group.getPrefix(), group.getSuffix(), group.getChatColor());
            Optional<PermissionGroup> created = getGroup(group.getName());
            if (!created.isPresent()) {
                PermissifyAPI.get().ifPresent(api -> api.getLogger().log(Level.WARNING, "Profile group was never created?"));
                return;
            }
            // Add permissions to the group
            group.getPermissions().forEach(permission -> {
                if (permission.isGranted()) created.get().addPermission(permission.getPermission());
                else created.get().removePermission(permission.getPermission());
            });
            // Add the players to the group
            group.getPlayers().forEach(created.get()::addPlayer);
        });
        superAdmins = profile.getSuperAdmins();
        defaultGroup = Optional.ofNullable(profile.getDefaultGroup());
    }

    @Override
    public void addPermission(UUID uuid, String... permissions) {
        // Put the permissions into the cache
        List<Permission> playerPermissions = cachedPermissions.getOrDefault(uuid, new ArrayList<>());

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }
        for (String permission : permissions) {
            Permission perm = new Permission(permission, true);
            playerPermissions.add(perm);

            try {
                PreparedStatement statement = connection.get().prepareStatement("INSERT INTO playerPermissions (uuid,permission,granted) VALUES (?,?,?)");
                statement.setString(1, uuid.toString());
                statement.setString(2, permission);
                statement.setBoolean(3, true);
                statement.execute();
                statement.close();
                connection.get().close();
            } catch (SQLException e) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
                e.printStackTrace();
                return;
            }
        }
        cachedPermissions.put(uuid, playerPermissions);
    }

    @Override
    public void removePermission(UUID uuid, String... permissions) {
        for (String permission : permissions) {
            // Remove from cache
            List<Permission> playerPermissions = cachedPermissions.getOrDefault(uuid, new ArrayList<>());
            playerPermissions.removeIf(perm -> perm.getPermission().equals(permission));
            cachedPermissions.put(uuid, playerPermissions);
            // Attempt to remove from MySQL
            Optional<Connection> connection = getConnection();
            if (!connection.isPresent()) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
                return;
            }

            try {
                PreparedStatement statement = connection.get().prepareStatement("DELETE FROM playerPermissions WHERE uuid=? AND permission=?");
                statement.setString(1, uuid.toString());
                statement.setString(2, permission);

                statement.execute();
                statement.close();
                connection.get().close();
            } catch (SQLException e) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isGrantedPermission(UUID uuid, String permission) {
        // Check the cache first
        if (cachedPermissions.containsKey(uuid))
            return cachedPermissions.get(uuid).stream()
                    .filter(entry -> entry.getPermission().equals(permission))
                    .allMatch(Permission::isGranted);
        // Cache didn't have it, see if the database does.
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT granted FROM playerPermissions WHERE uuid=? AND permission=?");
            statement.setString(1, uuid.toString());
            statement.setString(2, permission);
            // Does the player have the permission for this?
            ResultSet results = statement.executeQuery();
            boolean granted = false;
            if (results.next()) granted = results.getBoolean("granted");
            // Cleanup
            results.close();
            statement.close();
            connection.get().close();
            return granted;
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        return cachedPermissions.containsKey(uuid) && cachedPermissions.get(uuid).stream().filter(entry -> entry.getPermission().equals(permission)).count() == 1;
    }

    @Override
    public List<Permission> getPermissions(UUID uuid) {
        if (cachedPermissions.containsKey(uuid)) return cachedPermissions.get(uuid);
        Optional<Connection> connection = getConnection();
        List<Permission> permissions = new ArrayList<>();

        if (!connection.isPresent()) {
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
        // Make sure that this group doesn't already exist
        if (cachedGroups.getOrDefault(name, null) != null) return Tristate.NONE;
        // Add the new group to the cache
        cachedGroups.put(name, new PermissionGroup(name, displayName, chatColor, prefix, suffix));

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return Tristate.FALSE;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO groups (name,displayName,prefix,suffix,chatcolor,defaultGroup) VALUES (?,?,?,?,?,?)");
            statement.setString(1, name);
            statement.setString(2, displayName);
            statement.setString(3, prefix);
            statement.setString(4, suffix);
            statement.setString(5, chatColor);
            statement.setBoolean(6, false);
            // Cleanup
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }

        return Tristate.TRUE;
    }

    @Override
    public Tristate deleteGroup(String name) {
        Optional<PermissionGroup> group = getGroups().entrySet().stream().map(Map.Entry::getValue).filter(g -> g.getName().equalsIgnoreCase(name)).findFirst();
        if (!group.isPresent())
            return Tristate.NONE;

        if (defaultGroup.isPresent() && defaultGroup.get().getName().equalsIgnoreCase(name)) {
            setDefaultGroup(null);
        }
        Set<UUID> players = group.get().getPlayers().keySet();
        // Delete from the cache
        cachedGroups.remove(name);
        players.forEach(uuid -> this.removePlayerFromGroup(uuid, group.get()));

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return Tristate.FALSE;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("DELETE FROM groups WHERE name=?");
            statement.setString(1, name);
            statement.execute();
            statement.close();
            connection.get().close();

            return Tristate.TRUE;
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
        return Tristate.FALSE;
    }

    @Override
    public Optional<PermissionGroup> getGroup(String name) {
        return Optional.ofNullable(cachedGroups.getOrDefault(name, null));
    }

    @Override
    public boolean addPlayerToGroup(UUID uuid, PermissionGroup group) {
        if (group.hasPlayer(uuid)) return false;
        group.addPlayer(uuid, false);
        // Update the cache
        cachedGroups.put(group.getName(), group);

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO groupMembers (uuid,`group`,`primary`,ladderPosition) VALUES (?,?,?,?)");

            statement.setString(1, uuid.toString());
            statement.setString(2, group.getName());
            statement.setBoolean(3, false);
            statement.setInt(4, 0); // TODO: 1.1

            statement.execute();
            statement.close();
            connection.get().close();
            return true;
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removePlayerFromGroup(UUID uuid, PermissionGroup group) {
        if (!group.hasPlayer(uuid)) return false;
        group.removePlayer(uuid);
        // Update the cache
        cachedGroups.remove(group.getName());
        cachedGroups.put(group.getName(), group);

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("DELETE FROM groupMembers WHERE uuid=? AND `group`=?");
            statement.setString(1, uuid.toString());
            statement.setString(2, group.getName());
            statement.execute();
            statement.close();
            connection.get().close();
            return true;
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Map<String, PermissionGroup> getGroups() {
        return cachedGroups;
    }

    @Override
    public List<PermissionGroup> getGroups(UUID uuid) {
        return cachedGroups.entrySet().stream().map(Map.Entry::getValue).filter(group -> group.hasPlayer(uuid)).collect(Collectors.toList());
    }

    @Override
    public Tristate setPrimaryGroup(PermissionGroup group, UUID uuid) {
        if (!group.hasPlayer(uuid)) addPlayerToGroup(uuid, group);
        if (group.isPrimaryGroup(uuid)) return Tristate.NONE;
        group.setPrimaryGroup(uuid, true);

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return Tristate.FALSE;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("UPDATE groupMembers SET `primary`=? WHERE uuid=? AND `group`=?");
            statement.setBoolean(1, true);
            statement.setString(2, uuid.toString());
            statement.setString(3, group.getName());

            statement.execute();
            statement.close();
            connection.get().close();

            return Tristate.TRUE;
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
        return Tristate.FALSE;
    }

    @Override
    public Optional<PermissionGroup> getPrimaryGroup(UUID uuid) {
        return getGroups(uuid).stream().filter(group -> group.isPrimaryGroup(uuid)).findFirst();
    }

    @Override
    public void updateCache(UUID uuid) {
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
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
                if (!group.isPresent()) {
                    PreparedStatement groupStatement = connection.get().prepareStatement("SELECT prefix,suffix,chatcolor FROM groups WHERE name=?");
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
        // Make sure this is a valid group
        Optional<PermissionGroup> permissionGroup = getGroups().entrySet().stream().map(Map.Entry::getValue)
                .filter(permission -> permission.getName().equalsIgnoreCase(group)).findFirst();
        if (!permissionGroup.isPresent()) return false;
        // Update the cache
        for (String permission : permissions) {
            Optional<Connection> connection = getConnection();
            if (!connection.isPresent()) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
                return false;
            }
            try {
                if (permissionGroup.get().hasPermission(permission)) return false;
                PreparedStatement statement = connection.get().prepareStatement("INSERT INTO groupPermissions (groupName,permission) VALUES (?,?)");
                statement.setString(1, group);
                statement.setString(2, permission);
                statement.execute();
                statement.close();
                connection.get().close();

                permissionGroup.get().addPermission(permission);

                return true;
            } catch (SQLException e) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean removeGroupPermission(String group, String... permissions) {
        Optional<PermissionGroup> permissionGroup = getGroups().entrySet().stream().map(Map.Entry::getValue)
                .filter(permission -> permission.getName().equalsIgnoreCase(group)).findFirst();
        if (!permissionGroup.isPresent()) return false;

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }
        try {
            for (String permission : permissions) {
                if (!permissionGroup.get().hasPermission(permission)) return false;
                permissionGroup.get().removePermission(permission);

                PreparedStatement statement = connection.get().prepareStatement("DELETE FROM groupPermissions WHERE groupName=? AND permission=?");
                statement.setString(1, group);
                statement.setString(2, permission);
                statement.execute();
                statement.close();
            }
            connection.get().close();
            return true;
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean hasGroupPermission(String group, String permission) {
        Optional<PermissionGroup> permissionGroup = getGroups().entrySet().stream().map(Map.Entry::getValue)
                .filter(perm -> perm.getName().equalsIgnoreCase(group)).findFirst();
        return permissionGroup.map(groupPermission -> groupPermission.hasPermission(permission)).orElse(false);
    }

    @Override
    public void addSuperAdmin(UUID uuid) {
        if (uuid == null) return;
        if (superAdmins.contains(uuid)) return;
        // Update the cache
        superAdmins.add(uuid);
        // Update mysql
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO superAdmin (uuid) VALUES (?)");
            statement.setString(1, uuid.toString());
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
    }

    @Override
    public boolean isSuperAdmin(UUID uuid) {
        return superAdmins.contains(uuid);
    }

    @Override
    public void removeSuperAdmin(UUID uuid) {
        if (uuid == null) return;
        superAdmins.removeIf(u -> u.equals(uuid));

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("DELETE FROM superAdmin WHERE uuid=?");
            statement.setString(1, uuid.toString());
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
    }

    @Override
    public void setDefaultGroup(PermissionGroup group) {
        defaultGroup = Optional.ofNullable(group);

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }
        try {
            PreparedStatement removeDefaultsStatement = connection.get().prepareStatement("UPDATE groups SET defaultGroup=? WHERE defaultGroup=?");
            removeDefaultsStatement.setBoolean(1, false);
            removeDefaultsStatement.setBoolean(2, true);
            removeDefaultsStatement.execute();
            removeDefaultsStatement.close();

            PreparedStatement setDefaultStatement = connection.get().prepareStatement("UPDATE groups SET defaultGroup=? WHERE name=?");
            setDefaultStatement.setBoolean(1, true);
            setDefaultStatement.setString(2, defaultGroup.map(PermissionGroup::getName).orElse(""));
            setDefaultStatement.execute();
            setDefaultStatement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.DATABASE_EXCEPTION, Optional.of(e)));
            e.printStackTrace();
        }
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