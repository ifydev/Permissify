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
        try {
            return Optional.ofNullable(DriverManager.getConnection(connectionInformation.getUrl(), connectionInformation.getUsername(), connectionInformation.getPassword()));
        } catch (SQLException e) {
            // Unable to connect, display an error.
            e.printStackTrace();
            displayError(ConnectionError.REJECTED);
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
            List<String> playerPermissions = cachedPermissions.getOrDefault(uuid, new ArrayList<>());
            if (playerPermissions.contains(permission.getPermission())) return;
            playerPermissions.add(permission.getPermission());
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
                displayError(ConnectionError.REJECTED, e);
            }
        }
    }

    @Override
    public void removePermission(UUID uuid, Permission... permissions) {
        for (Permission permission : permissions) {
            // Remove from cache
            List<String> playerPermissions = cachedPermissions.getOrDefault(uuid, new ArrayList<>());
            playerPermissions.removeIf(entry -> entry.equals(permission.getPermission()));
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
                e.printStackTrace();
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
            displayError(ConnectionError.REJECTED, e);
        }
        return false;
    }
}
