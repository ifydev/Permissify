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
package me.innectic.permissify.api.database;

import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;

import java.util.*;

/**
 * @author Innectic
 * @since 6/8/2017
 *
 * The base database type.
 */
public abstract class DatabaseHandler {

    /**
     * Cached permissions from the database.
     */
    protected Map<UUID, List<Permission>> cachedPermissions = new HashMap<>();
    protected List<PermissionGroup> cachedGroups = new ArrayList<>();
    protected Optional<UUID> superAdmin = Optional.empty();
    protected final Optional<ConnectionInformation> connectionInformation;

    public DatabaseHandler(ConnectionInformation connectionInformation) {
        this.connectionInformation = Optional.ofNullable(connectionInformation);
    }

    /**
     * Display an error to the server console, and the online ops. TODO: This should be converted to sending the message to players with a certain permission. `permissify.admin`?
     *
     * @param error the type of error
     */
    protected void displayError(ConnectionError error) {
        System.out.println(error.getDisplay());
    }

    /**
     * Display an error to the server console, and the online ops. TODO: This should be converted to sending the message to players with a certain permission. `permissify.admin`?
     *
     * @param error the type of error
     * @param e     the exception that happened
     */
    protected void displayError(ConnectionError error, Exception e) {
        System.out.println(error.getDisplay());
        e.printStackTrace();
    }

    /**
     * Initialize the database handler
     */
    public abstract void initialize();

    /**
     * Connect to the database
     *
     * @return if the connection was successful or not
     */
    public abstract boolean connect();

    /**
     * Add a permission to a player
     *
     * @param uuid        the UUID of the player to add the permissions to
     * @param permissions the permissions to add to a player
     */
    public abstract void addPermission(UUID uuid, Permission... permissions);

    /**
     * Remove permissions from a player
     *
     * @param uuid        the uuid of the player to remove the permissions from
     * @param permissions the permissions to remove
     */
    public abstract void removePermission(UUID uuid, Permission... permissions);

    /**
     * Does a player have a permission?
     *
     * @param uuid       the uuid of the player to check
     * @param permission the permission to check
     * @return           if the player has the permission
     */
    public abstract boolean hasPermission(UUID uuid, Permission permission);

    /**
     * Get the permissions of a uuid
     *
     * @param uuid the uuid to get the permissions of
     * @return     the permissions the uuid has
     */
    public abstract List<Permission> getPermissions(UUID uuid);

    /**
     * Create a new permission group.
     *
     * @param name      the name of the group
     * @param prefix    the prefix of the name
     * @param suffix    the suffix of the name
     * @param chatColor the color of the chat message
     * @return          if the group was created
     */
    public abstract boolean createGroup(String name, String prefix, String suffix, String chatColor);

    /**
     * Delete a permission group
     *
     * @param name the name of the group
     */
    public abstract boolean deleteGroup(String name);

    /**
     * Get all permissions groups.
     *
     * @return the registered permission groups
     */
    public abstract List<PermissionGroup> getGroups();

    /**
     * Add a set of permissions to the group.
     *
     * @param group the group to add to
     * @param permissions the permissions to add
     * @return if it was added or not
     */
    public abstract boolean addGroupPermission(String group, String... permissions);

    /**
     * Remove a set of permission from a group
     *
     * @param group the group to remove from
     * @param permissions the permissions to remove
     * @return if the permissions were removed
     */
    public abstract boolean removeGroupPermission(String group, String... permissions);

    /**
     * Check if a group has a permission.
     *
     * @param group the group to check
     * @param permission the permission to check
     * @return if the group has the permission
     */
    public abstract boolean hasGroupPermission(String group, String permission);

    /**
     * Set the super admin
     *
     * @param uuid the uuid of the player to be super admin
     */
    public abstract void setSuperAdmin(UUID uuid);

    /**
     * Get the player who's super admin
     *
     * @return the uuid of the player who's super admin
     */
    public abstract Optional<UUID> getSuperAdmin();
}
