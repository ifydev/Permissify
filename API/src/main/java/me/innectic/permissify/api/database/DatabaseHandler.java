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

import lombok.Getter;
import me.innectic.permissify.api.group.Permission;
import me.innectic.permissify.api.group.group.PermissionGroup;
import me.innectic.permissify.api.group.ladder.AbstractLadder;
import me.innectic.permissify.api.profile.PermissifyProfile;

import java.util.*;

/**
 * @author Innectic
 * @since 6/8/2017
 *
 * The base database type.
 */
public abstract class DatabaseHandler {

    @Getter protected Map<UUID, List<Permission>> cachedPermissions = new HashMap<>();
    @Getter protected Map<String, PermissionGroup> cachedGroups = new HashMap<>();
    @Getter protected Optional<PermissionGroup> defaultGroup = Optional.empty();
    @Getter protected final Optional<ConnectionInformation> connectionInformation;
    @Getter protected List<UUID> superAdmins = new ArrayList<>();
    protected String chatFormat = "{group} {username}: {message}";
    protected String whisperFormat = "{senderGroup} {username} > {receiverGroup} {to}: {message}";

    public DatabaseHandler(ConnectionInformation connectionInformation) {
        this.connectionInformation = Optional.ofNullable(connectionInformation);
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
     * Clear the handler's cache and reload all needed values.
     *
     * @param onlinePlayers the current players online who will need permissions.
     */
    public abstract void reload(List<UUID> onlinePlayers);

    /**
     * Drop all values from the handler.
     */
    public abstract void drop();

    /**
     * Load a profile into the handler.
     *
     * @param profile the profile to load
     */
    public abstract void loadProfile(PermissifyProfile profile);

    /**
     * Add a permission to a player
     *
     * @param uuid        the UUID of the player to add the permissions to
     * @param permissions the permissions to add to a player
     */
    public abstract void addPermission(UUID uuid, String... permissions);

    /**
     * Remove permissions from a player
     *
     * @param uuid        the uuid of the player to remove the permissions from
     * @param permissions the permissions to remove
     */
    public abstract void removePermission(UUID uuid, String... permissions);

    /**
     * Does a player have a permission?
     *
     * @param uuid       the uuid of the player to check
     * @param permission the permission to check
     * @return           if the player has the permission
     */
    public abstract boolean hasPermission(UUID uuid, String permission);

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
     * Get the permission group from name.
     *
     * @param name the name of the group.
     * @return     fulfilled if exists, empty otherwise
     */
    public abstract Optional<PermissionGroup> getGroup(String name);

    /**
     * Add a player to a permission group, and grant permissions.
     *
     * @param uuid  the uuid of the player
     * @param group the group to add them to
     * @return      if they were added
     */
    public abstract boolean addPlayerToGroup(UUID uuid, PermissionGroup group);

    /**
     * Remove a player from a group.
     *
     * @param uuid  the player to remove
     * @param group the group to remove from
     * @return      if the player was removed
     */
    public abstract boolean removePlayerFromGroup(UUID uuid, PermissionGroup group);

    /**
     * Get all permissions groups.
     *
     * @return the registered permission groups
     */
    public abstract Map<String, PermissionGroup> getGroups();

    /**
     * Get all permission groups a player is in.
     *
     * @param uuid the uuid of the player
     * @return     the groups the player is in
     */
    public abstract List<PermissionGroup> getGroups(UUID uuid);

    /**
     * Set the primary group of the player
     *
     * @param group the group to set as the primary
     * @param uuid  the uuid of the player to set the primary of
     */
    public abstract boolean setPrimaryGroup(PermissionGroup group, UUID uuid);

    /**
     * Get the primary group of a player.
     *
     * @param uuid the uuid of the player to get
     * @return     the primary group of the player
     */
    public abstract Optional<PermissionGroup> getPrimaryGroup(UUID uuid);

    /**
     * Update a player's cache.
     *
     * @param uuid the uuid to update
     */
    public abstract void updateCache(UUID uuid);

    /**
     * Add a set of permissions to the group.
     *
     * @param group       the group to add to
     * @param permissions the permissions to add
     * @return            if it was added or not
     */
    public abstract boolean addGroupPermission(String group, String... permissions);

    /**
     * Remove a set of permission from a group
     *
     * @param group       the group to remove from
     * @param permissions the permissions to remove
     * @return            if the permissions were removed
     */
    public abstract boolean removeGroupPermission(String group, String... permissions);

    /**
     * Check if a group has a permission.
     *
     * @param group      the group to check
     * @param permission the permission to check
     * @return           if the group has the permission
     */
    public abstract boolean hasGroupPermission(String group, String permission);

    /**
     * Set the super admin
     *
     * @param uuid the uuid of the player to be super admin
     */
    public abstract void addSuperAdmin(UUID uuid);

    /**
     * Get the player who's super admin
     *
     * @return if the uuid is a super admin
     */
    public abstract boolean isSuperAdmin(UUID uuid);

    /**
     * Set the format for chat messages
     *
     * @param format the format for chat.
     */
    public abstract void setChatFormat(String format);

    /**
     * Get the format for public chat messages
     *
     * @param skipCache should the cache be skipped? Forces getting from the database
     * @return          the format
     */
    public abstract String getChatFormat(boolean skipCache);

    /**
     * Set the format of whispers
     *
     * @param format the format for whispers.
     */
    public abstract void setWhisperFormat(String format);

    /**
     * Get the format for whispers.
     *
     * @param skipCache should the cache be skipped? Forces getting from the database
     * @return          the format
     */
    public abstract String getWhisperFormat(boolean skipCache);

    /**
     * Set the default permission group to the one provided.
     *
     * @param group the new group to become the default.
     */
    public abstract void setDefaultGroup(PermissionGroup group);

    public abstract void setGroupLadder(String group, AbstractLadder ladder);
    public abstract Optional<AbstractLadder> getGroupLadder(String group);
}
