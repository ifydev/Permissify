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
import me.innectic.permissify.api.util.FormatterType;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 6/8/2017
 */
public class MySQLHandler extends DatabaseHandler {

    public MySQLHandler(ConnectionInformation connectionInformation) {
        super(connectionInformation);
    }

    /**
     * Get a connection to the mysql server.
     *
     * @return an optional connection, filled if successful.
     */
    private Optional<Connection> getConnection() {
        if (!connectionInformation.isPresent()) return Optional.empty();
        try {
            String connectionURL = "jdbc:mysql://" + connectionInformation.get().getUrl() + ":" + connectionInformation.get().getPort() + "/" + connectionInformation.get().getDatabase();
            return Optional.ofNullable(DriverManager.getConnection(connectionURL, connectionInformation.get().getUsername(), connectionInformation.get().getPassword()));
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
        return Optional.empty();
    }

    @Override
    public void initialize() {
        // Make sure that the cache is empty
        this.cachedPermissions = new HashMap<>();
    }

    @Override
    public boolean connect() {
        Optional<Connection> connection = getConnection();
        boolean connected = connection.isPresent();

        connection.ifPresent(c -> {
            try {
                c.close();
            } catch (SQLException ignored) {}
        });
        return connected;
    }

    @Override
    public void clear(List<UUID> onlinePlayers) {
        cachedGroups = new ArrayList<>();
        cachedPermissions = new HashMap<>();
        superAdmins = new ArrayList<>();

        chatFormat = getChatFormat(true);
        whisperFormat = getWhisperFormat(true);

        Optional<Connection> connection = getConnection();
        if (connection.isPresent()) {
            try {
                // Load all super admins
                PreparedStatement adminStatement = connection.get().prepareStatement("SELECT uuid FROM superAdmin");
                ResultSet adminResults = adminStatement.executeQuery();
                while (adminResults.next()) {
                    superAdmins.add(UUID.fromString(adminResults.getString("uuid")));
                }
                adminResults.close();
                adminStatement.close();

                // Load all group names
                PreparedStatement groupStatement = connection.get().prepareStatement("SELECT * from groups");
                ResultSet groupResults = groupStatement.executeQuery();
                while (groupResults.next()) {
                    PermissionGroup group = new PermissionGroup(groupResults.getString("name"),
                            groupResults.getString("chatcolor"),
                            groupResults.getString("prefix"),
                            groupResults.getString("suffix"));
                    PreparedStatement groupPermissionsStatement = connection.get().prepareStatement("SELECT  * FROM groupPermissions WHERE groupName=?");
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
                    cachedGroups.add(group);
                    if (groupResults.getBoolean("defaultGroup")) defaultGroup = Optional.of(group);
                }
                groupStatement.close();
                groupResults.close();
                connection.get().close();
            } catch (SQLException e) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
            }
        } else PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
    }

    @Override
    public void addPermission(UUID uuid, String... permissions) {
        // Put the permissions into the cache
        Map<String, Boolean> playerPermissions = cachedPermissions.getOrDefault(uuid, new HashMap<>());

        for (String permission : permissions) {
            playerPermissions.put(permission, true);
            Optional<Connection> connection = getConnection();
            if (!connection.isPresent()) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
                return;
            }
            try {
                PreparedStatement statement = connection.get().prepareStatement("INSERT INTO playerPermissions (uuid,permission,granted) VALUES (?,?,?)");
                statement.setString(1, uuid.toString());
                statement.setString(2, permission);
                statement.setBoolean(3, true);
                statement.execute();
                statement.close();
                connection.get().close();
            } catch (SQLException e) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
                return;
            }
        }
        cachedPermissions.put(uuid, playerPermissions);
    }

    @Override
    public void removePermission(UUID uuid, String... permissions) {
        for (String permission : permissions) {
            // Remove from cache
            Map<String, Boolean> playerPermissions = cachedPermissions.getOrDefault(uuid, new HashMap<>());
            playerPermissions.remove(permission);
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
                // Cleanup
                statement.close();
                statement = connection.get().prepareStatement("INSERT INTO playerPermissions (uuid,permission,granted) VALUES (?,?,?)");
                statement.setString(1, uuid.toString());
                statement.setString(2, permission);
                statement.setBoolean(3, false);
                // Cleanup
                statement.execute();
                statement.close();
                connection.get().close();
            } catch (SQLException e) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
            }
        }
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        // Check the cache first
        if (cachedPermissions.containsKey(uuid))
            return cachedPermissions.get(uuid).entrySet().stream()
                    .filter(entry -> entry.getKey().equals(permission))
                    .allMatch(entry -> entry.getValue().equals(true));
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
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
        return false;
    }

    @Override
    public List<Permission> getPermissions(UUID uuid) {
        if (cachedPermissions.containsKey(uuid)) {
            System.out.println("Found cached permissions for " + uuid);
            return cachedPermissions.get(uuid).entrySet().stream()
                    .map(entry -> new Permission(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        }
        System.out.println("No cache for " + uuid + " found. Getting.");
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
            return permissions;
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
        return new ArrayList<>();
    }

    @Override
    public boolean createGroup(String name, String prefix, String suffix, String chatColor) {
        // Make sure that this group doesn't already exist
        if (cachedGroups.stream().anyMatch(group -> group.getName().equalsIgnoreCase(name))) return false;
        // Add the new group to the cache
        cachedGroups.add(new PermissionGroup(name, chatColor, prefix, suffix));

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO groups (name,prefix,suffix,chatcolor) VALUES (?,?,?,?)");
            statement.setString(1, name);
            statement.setString(2, prefix);
            statement.setString(3, suffix);
            statement.setString(4, chatColor);
            // Cleanup
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }

        return true;
    }

    @Override
    public boolean deleteGroup(String name) {
        if (getGroups().stream().noneMatch(group -> group.getName().equalsIgnoreCase(name))) return false;
        // Delete from the cache
        cachedGroups.removeIf(group -> group.getName().equalsIgnoreCase(name));

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("DELETE FROM groups WHERE name=?");
            statement.setString(1, name);
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
            return false;
        }
        return true;
    }

    @Override
    public Optional<PermissionGroup> getGroup(String name) {
        return cachedGroups.stream().filter(group -> group.getName().equalsIgnoreCase(name)).findFirst();
    }

    @Override
    public boolean addPlayerToGroup(UUID uuid, PermissionGroup group) {
        if (group.hasPlayer(uuid)) return false;
        group.addPlayer(uuid, false);
        // Update the cache
        cachedGroups.removeIf(entry -> entry.getName().equals(group.getName()));
        cachedGroups.add(group);

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO groupMembers (uuid,`group`) VALUES (?,?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, group.getName());
            statement.execute();
            statement.close();
            connection.get().close();
            return true;
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
        return false;
    }

    @Override
    public boolean removePlayerFromGroup(UUID uuid, PermissionGroup group) {
        if (!group.hasPlayer(uuid)) return false;
        group.removePlayer(uuid);
        // Update the cache
        cachedGroups.removeIf(entry -> entry.getName().equals(group.getName()));
        cachedGroups.add(group);

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
        }
        return false;
    }

    @Override
    public List<PermissionGroup> getGroups() {
        return cachedGroups;
    }

    @Override
    public List<PermissionGroup> getGroups(UUID uuid) {
        return cachedGroups.stream().filter(group -> group.hasPlayer(uuid)).collect(Collectors.toList());
    }

    @Override
    public boolean setPrimaryGroup(PermissionGroup group, UUID uuid) {
        // @Return: Should primary group put the player in the group if they're not already in it?
        if (!group.hasPlayer(uuid)) return false;
        if (group.isPrimaryGroup(uuid)) return false;
        group.setPrimaryGroup(uuid, true);
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return false;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("UPDATE groupMembers SET `primary`=? WHERE uuid=? AND `group`=?");
            statement.setBoolean(1, true);
            statement.setString(2, uuid.toString());
            statement.setString(3, group.getName());
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
        return true;
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
                Optional<PermissionGroup> group = cachedGroups.stream().filter(permissionGroup -> permissionGroup .getName().equals(groupName)).findFirst();
                // Get the group from the database, if we don't have have it already
                if (!group.isPresent()) {
                    PreparedStatement groupStatement = connection.get().prepareStatement("SELECT prefix,suffix,chatcolor FROM groups WHERE name=?");
                    groupStatement.setString(1, groupName);
                    ResultSet groupResults = groupStatement.executeQuery();
                    if (!groupResults.next()) return;
                    PermissionGroup permissionGroup = new PermissionGroup(
                            groupName, groupResults.getString("chatcolor"), groupResults.getString("prefix"),
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
                    cachedGroups.add(permissionGroup);
                }
            }
            results.close();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
    }

    @Override
    public boolean addGroupPermission(String group, String... permissions) {
        // Make sure this is a valid group
        Optional<PermissionGroup> permissionGroup = getGroups().stream().filter(permission -> permission.getName().equalsIgnoreCase(group)).findFirst();
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
            } catch (SQLException e) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeGroupPermission(String group, String... permissions) {
        Optional<PermissionGroup> permissionGroup = getGroups().stream().filter(permission -> permission.getName().equalsIgnoreCase(group)).findFirst();
        if (!permissionGroup.isPresent()) return false;

        for (String permission : permissions) {
            if (!permissionGroup.get().hasPermission(permission)) return false;
            permissionGroup.get().removePermission(permission);
            Optional<Connection> connection = getConnection();
            if (!connection.isPresent()) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
                return false;
            }
            try {
                PreparedStatement statement = connection.get().prepareStatement("DELETE FROM groupPermissions WHERE groupName=? AND permission=?");
                statement.setString(1, group);
                statement.setString(2, permission);
                statement.execute();
                statement.close();
                connection.get().close();
            } catch (SQLException e) {
                PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasGroupPermission(String group, String permission) {
        Optional<PermissionGroup> permissionGroup = getGroups().stream().filter(perm -> perm.getName().equalsIgnoreCase(group)).findFirst();
        return permissionGroup.map(groupPermission -> groupPermission.hasPermission(permission)).orElse(false);
    }

    @Override
    public void addSuperAdmin(UUID uuid) {
        if (uuid == null) return;
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
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
    }

    @Override
    public boolean isSuperAdmin(UUID uuid) {
        return superAdmins.contains(uuid);
    }

    @Override
    public void setChatFormat(String format) {
        this.chatFormat = format;

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("UPDATE formatting SET format=? WHERE formatter=?");
            statement.setString(1, format);
            statement.setString(2, FormatterType.CHAT.getUsageName());
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
    }

    @Override
    public String getChatFormat(boolean skipCache) {
        if (!skipCache) return chatFormat;

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return "";
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT format FROM formatting WHERE formatter=?");
            statement.setString(1, FormatterType.CHAT.getUsageName());
            ResultSet results = statement.executeQuery();
            if (!results.next()) return "";
            String format = results.getString("format");
            results.close();
            statement.close();
            connection.get().close();
            return format;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setWhisperFormat(String format) {
        this.chatFormat = format;

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("UPDATE formatting SET format=? WHERE formatter=?");
            statement.setString(1, format);
            statement.setString(2, FormatterType.WHISPER.getUsageName());
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.of(e)));
        }
    }

    @Override
    public String getWhisperFormat(boolean skipCache) {
        if (!skipCache) return whisperFormat;

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return "";
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT format FROM formatting WHERE formatter=?");
            statement.setString(1, FormatterType.WHISPER.getUsageName());
            ResultSet results = statement.executeQuery();
            if (!results.next()) return "";
            String format = results.getString("format");
            results.close();
            statement.close();
            connection.get().close();
            return format;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setDefaultGroup(PermissionGroup group) {
        defaultGroup = Optional.of(group);

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return;
        }
        try {
            PreparedStatement removeDefaultsStatement = connection.get().prepareStatement("UPDATE groups SET defaultGroup=FALSE WHERE defaultGroup=TRUE");
            removeDefaultsStatement.execute();
            removeDefaultsStatement.close();

            PreparedStatement setDefaultStatement = connection.get().prepareStatement("UPDATE groups SET defaultGroup=? WHERE name=?");
            setDefaultStatement.setBoolean(1, true);
            setDefaultStatement.setString(2, group.getName());
            setDefaultStatement.execute();
            setDefaultStatement.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
