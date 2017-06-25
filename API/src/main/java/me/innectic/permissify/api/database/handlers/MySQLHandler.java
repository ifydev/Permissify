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
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.database.ConnectionInformation;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.permission.PermissionGroup;

import java.sql.*;
import java.util.*;

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
    public void addPermission(UUID uuid, Permission... permissions) {
        for (Permission permission : permissions) {
            // Add it to the cache before anything else. Connection errors shouldn't stall everything.
            List<Permission> playerPermissions = cachedPermissions.getOrDefault(uuid, new ArrayList<>());
            long found = playerPermissions.stream().map(Permission::getPermission).filter(perm -> perm.equals(permission.getPermission())).count();
            if (found > 0) return;
            playerPermissions.add(permission);
            cachedPermissions.put(uuid, playerPermissions);
            // Now attempt to add to mysql
            Optional<Connection> connection = getConnection();
            if (!connection.isPresent()) {
                displayError(ConnectionError.REJECTED);
                return;
            }

            try {
                PreparedStatement statement = connection.get().prepareStatement("INSERT INTO permissions (uuid,permission,granted) VALUES (?,?,?)");
                statement.setString(1, uuid.toString());
                statement.setString(2, permission.getPermission());
                statement.setBoolean(3, permission.isGranted());
                statement.execute();
                // Cleanup
                statement.close();
                connection.get().close();
            } catch (SQLException e) {
                displayError(ConnectionError.DATABASE_EXCEPTION, e);
            }
        }
    }

    @Override
    public void removePermission(UUID uuid, Permission... permissions) {
        for (Permission permission : permissions) {
            // Remove from cache
            List<Permission> playerPermissions = cachedPermissions.getOrDefault(uuid, new ArrayList<>());
            playerPermissions.removeIf(entry -> entry.getPermission().equals(permission.getPermission()));
            cachedPermissions.put(uuid, playerPermissions);
            // Attempt to remove from MySQL
            Optional<Connection> connection = getConnection();
            if (!connection.isPresent()) {
                displayError(ConnectionError.REJECTED);
                return;
            }

            try {
                PreparedStatement statement = connection.get().prepareStatement("DELETE FROM permissions WHERE uuid=? AND permission=?");
                statement.setString(1, uuid.toString());
                statement.setString(2, permission.getPermission());
                statement.execute();
                // Cleanup
                statement.close();
                connection.get().close();
            } catch (SQLException e) {
                displayError(ConnectionError.DATABASE_EXCEPTION, e);
            }
        }
    }

    @Override
    public boolean hasPermission(UUID uuid, Permission permission) {
        // Check the cache first
        if (cachedPermissions.containsKey(uuid)) return cachedPermissions.get(uuid).contains(permission.getPermission());
        // Cache didn't have it, see if the database does.
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            displayError(ConnectionError.REJECTED);
            return false;
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT granted FROM permissions WHERE uuid=? AND permission=?");
            statement.setString(1, uuid.toString());
            statement.setString(2, permission.getPermission());
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
        if (cachedPermissions.containsKey(uuid)) return cachedPermissions.get(uuid);
        Optional<Connection> connection = getConnection();
        List<Permission> permissions = new ArrayList<>();

        if (!connection.isPresent()) {
            displayError(ConnectionError.REJECTED);
            return permissions;
        }
        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT permission,granted FROM permissions WHERE uuid=?");
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
    public List<PermissionGroup> getGroups() {
        return cachedGroups;
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
    public void setSuperAdmin(UUID uuid) {
        if (uuid == null) return;
        // Update the cache
        this.superAdmin = Optional.of(uuid);
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
    public Optional<UUID> getSuperAdmin() {
        if (superAdmin.isPresent()) return superAdmin;
        Optional<Connection> connection = getConnection();
        if (!connection.isPresent()) {
            displayError(ConnectionError.REJECTED);
            return Optional.empty();
        }

        try {
            PreparedStatement statement = connection.get().prepareStatement("SELECT uuid FROM superAdmin");
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                superAdmin = Optional.of(UUID.fromString(results.getString("uuid")));
            }
        } catch (SQLException e) {
            displayError(ConnectionError.DATABASE_EXCEPTION, e);
        }
        return superAdmin;
    }
}
