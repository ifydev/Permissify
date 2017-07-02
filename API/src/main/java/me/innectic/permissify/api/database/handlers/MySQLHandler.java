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
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
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
    public void addPermission(UUID uuid, String... permissions) {
        // Put the permissions into the cache
        Map<String, Boolean> playerPermissions = cachedPermissions.getOrDefault(uuid, new HashMap<>());

        for (String permission : permissions) {
            playerPermissions.put(permission, true);
            Optional<Connection> connection = getConnection();
            if (!connection.isPresent()) {
                displayError(ConnectionError.REJECTED);
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
                displayError(ConnectionError.DATABASE_EXCEPTION, e);
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
                displayError(ConnectionError.REJECTED);
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
                displayError(ConnectionError.DATABASE_EXCEPTION, e);
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
            displayError(ConnectionError.REJECTED);
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
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
        }
        return false;
    }

    @Override
    public List<Permission> getPermissions(UUID uuid) {
        if (cachedPermissions.containsKey(uuid))
            return cachedPermissions.get(uuid).entrySet().stream()
                    .map(entry -> new Permission(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        Optional<Connection> connection = getConnection();
        List<Permission> permissions = new ArrayList<>();

        if (!connection.isPresent()) {
            displayError(ConnectionError.REJECTED);
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
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
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
            displayError(ConnectionError.REJECTED);
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
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
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
            displayError(ConnectionError.REJECTED);
            return false;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("DELETE FROM groups WHERE name=?");
            statement.setString(1, name);
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean addPlayerToGroup(UUID uuid, PermissionGroup group) {
        if (group.hasPlayer(uuid)) return false;
        group.addPlayer(uuid);
        // Update the cache
        cachedGroups.removeIf(entry -> entry.getName().equals(group.getName()));
        cachedGroups.add(group);

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            displayError(ConnectionError.REJECTED);
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
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
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
            displayError(ConnectionError.REJECTED);
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
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
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
    public void updateCache(UUID uuid) {
        // TODO: Make this method not exist. It shouldn't be needed.
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            displayError(ConnectionError.REJECTED);
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
                    System.out.println("UPDATING GROUP");
                    PreparedStatement groupStatement = connection.get().prepareStatement("SELECT prefix,suffix,chatcolor FROM groups WHERE name=?");
                    groupStatement.setString(1, groupName);
                    ResultSet groupResults = groupStatement.executeQuery();
                    if (!groupResults.next()) return;
                    System.out.println("IS NEXT");
                    PermissionGroup permissionGroup = new PermissionGroup(
                            groupName, groupResults.getString("chatcolor"), groupResults.getString("prefix"),
                            groupResults.getString("suffix"));
                    groupResults.close();
                    groupStatement.close();
                    PreparedStatement groupPlayersStatement = connection.get().prepareStatement("SELECT uuid FROM groupMembers WHERE `group`=?");
                    groupPlayersStatement.setString(1, groupName);
                    ResultSet groupPlayersResult = groupPlayersStatement.executeQuery();
                    while (groupPlayersResult.next()) {
                        permissionGroup.addPlayer(UUID.fromString(groupPlayersResult.getString("uuid")));
                    }
                    cachedGroups.add(permissionGroup);
                }
            }
            results.close();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
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
                displayError(ConnectionError.REJECTED);
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
                displayError(ConnectionError.DATABASE_EXCEPTION, e);
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
                displayError(ConnectionError.REJECTED);
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
                displayError(ConnectionError.DATABASE_EXCEPTION, e);
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
            displayError(ConnectionError.REJECTED);
            return;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("INSERT INTO superAdmin (uuid) VALUES (?)");
            statement.setString(1, uuid.toString());
            statement.execute();
            statement.close();
            connection.get().close();
        } catch (SQLException e) {
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
        }
    }

    @Override
    public boolean isSuperAdmin(UUID uuid) {
        if (superAdmins.contains(uuid)) return true;
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            displayError(ConnectionError.REJECTED);
            return false;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT uuid FROM superAdmin");
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                if (results.getString("uuid").equals(uuid.toString())) return true;
            }
        } catch (SQLException e) {
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
        }
        return false;
    }

    @Override
    public void setChatFormat(String format) {
        this.chatFormat = format;

        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            displayError(ConnectionError.REJECTED);
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
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
        }
    }

    @Override
    public String getChatFormat() {
        return chatFormat;
    }
}
