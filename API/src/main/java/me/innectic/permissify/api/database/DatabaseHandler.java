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
import lombok.RequiredArgsConstructor;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionDecision;
import me.innectic.permissify.api.audit.AuditEntry;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.permission.GroupInheritance;
import me.innectic.permissify.api.permission.RankTrack;
import me.innectic.permissify.api.permission.RankChange;
import me.innectic.permissify.api.profile.PermissifyProfile;
import me.innectic.permissify.api.util.Tristate;

import java.util.*;

/**
 * @author Innectic
 * @since 6/8/2017
 *
 * The base database type.
 */
@RequiredArgsConstructor
public abstract class DatabaseHandler {

    @Getter protected Map<UUID, List<Permission>> cachedPermissions = new HashMap<>();
    @Getter protected Map<String, PermissionGroup> cachedGroups = new HashMap<>();
    @Getter protected Optional<PermissionGroup> defaultGroup = Optional.empty();
    @Getter protected final ConnectionInformation connectionInformation;
    @Getter protected List<UUID> superAdmins = new ArrayList<>();
    @Getter protected Map<String, RankTrack> tracks = new LinkedHashMap<>();

    /**
     * Initialize the database handler
     */
    public abstract void initialize();

    /**
     * Clear the handler's cache and reload all needed values.
     *
     * @param onlinePlayers the current players online who will need permissions.
     */
    public abstract void reload(List<UUID> onlinePlayers);

    /**
     * Load all groups
     */
    protected abstract void loadGroups();

    /**
     * Load all super admins
     */
    protected abstract void loadSuperAdmins();

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
     * @return if the player has the permission, regardless of the granted status
     */
    public abstract boolean hasPermission(UUID uuid, String permission);

    /**
     * Does a player have a permission granted to them?
     *
     * @param uuid       the uuid of the player to check
     * @param permission the permission to check for
     * @return if the player has the permission
     */
    public abstract boolean isGrantedPermission(UUID uuid, String permission);

    /**
     * Get the permissions of a uuid
     *
     * @param uuid the uuid to get the permissions of
     * @return the permissions the uuid has
     */
    public abstract List<Permission> getPermissions(UUID uuid);

    public Map<UUID, List<Permission>> getAllPermissions() {
        return new HashMap<>(cachedPermissions);
    }

    /**
     * Create a new permission group.
     *
     * @param name        the name of the group
     * @param displayName the display name of the group
     * @param prefix      the prefix of the name
     * @param suffix      the suffix of the name
     * @param chatColor   the color of the chat message
     * @return if the group was created
     */
    public abstract Tristate createGroup(String name, String displayName, String prefix, String suffix, String chatColor);

    /**
     * Delete a permission group
     *
     * @param name the name of the group
     */
    public abstract Tristate deleteGroup(String name);

    /**
     * Get the permission group from name.
     *
     * @param name the name of the group.
     * @return fulfilled if exists, empty otherwise
     */
    public abstract Optional<PermissionGroup> getGroup(String name);

    /**
     * The child inherits the parent's permissions.
     */
    public abstract Tristate inheritGroup(PermissionGroup parent, PermissionGroup child);

    public abstract Tristate uninheritGroup(PermissionGroup parent, PermissionGroup child);

    public abstract Tristate addTemporaryPermission(UUID uuid, String permission, long expiresAt);

    public abstract Tristate addPlayerToGroup(UUID uuid, PermissionGroup group, long expiresAt);

    public abstract Set<UUID> expireEntries(long now);

    public Optional<RankTrack> getTrack(String name) {
        return tracks.values().stream().filter(track -> track.name().equalsIgnoreCase(name)).findFirst();
    }

    public abstract Tristate createTrack(String name);

    public abstract Tristate deleteTrack(String name);

    public abstract boolean setTrackGroups(String name, List<String> groups);

    public abstract RankChange moveOnTrack(UUID uuid, String track, boolean promote);

    public List<Permission> getEffectivePermissions(PermissionGroup group) {
        return GroupInheritance.getPermissions(group, getGroups());
    }

    /**
     * Add a player to a permission group, and grant permissions.
     *
     * @param uuid  the uuid of the player
     * @param group the group to add them to
     * @return if they were added
     */
    public abstract Tristate addPlayerToGroup(UUID uuid, PermissionGroup group);

    /**
     * Remove a player from a group.
     *
     * @param uuid  the player to remove
     * @param group the group to remove from
     * @return if the player was removed
     */
    public abstract Tristate removePlayerFromGroup(UUID uuid, PermissionGroup group);

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
     * @return the groups the player is in
     */
    public abstract List<PermissionGroup> getGroups(UUID uuid);

    public Optional<PermissionGroup> getMainGroup(UUID uuid) {
        return getGroups(uuid).stream().sorted(Comparator.comparingInt(PermissionGroup::getWeight).reversed()
                .thenComparing(PermissionGroup::getName, String.CASE_INSENSITIVE_ORDER)).findFirst();
    }

    public Map<String, PermissionDecision> resolvePermissions(UUID uuid) {
        Map<String, PermissionDecision> result = new TreeMap<>();
        getGroups(uuid).stream().sorted(Comparator.comparing(PermissionGroup::getName, String.CASE_INSENSITIVE_ORDER))
                .forEach(group -> GroupInheritance.resolve(group, getGroups()).forEach((node, rule) -> {
                    var candidate = new PermissionDecision(node, rule.granted(),
                            group.getName(), rule.source(), rule.path(), group.getWeight(),
                            group.getPlayerExpirations().getOrDefault(uuid, 0L));
                    var current = result.get(node);
                    if (current == null || GroupInheritance.wins(candidate, current)) result.put(node, candidate);
                }));
        getPermissions(uuid).forEach(rule -> result.put(rule.getPermission().toLowerCase(Locale.ROOT),
                new PermissionDecision(rule.getPermission().toLowerCase(Locale.ROOT), rule.isGranted(),
                        null, null, List.of(), 0, rule.getExpiresAt())));
        return Collections.unmodifiableMap(result);
    }

    public abstract boolean setGroupWeight(String group, int weight);

    public abstract boolean setGroupPermission(String group, String permission, boolean granted);

    public abstract boolean setPermission(UUID uuid, String permission, boolean granted, long expiresAt);

    public abstract List<AuditEntry> getAuditLog(String query, int page);

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
     * @return if it was added or not
     */
    public abstract boolean addGroupPermission(String group, String... permissions);

    /**
     * Remove a set of permission from a group
     *
     * @param group       the group to remove from
     * @param permissions the permissions to remove
     * @return if the permissions were removed
     */
    public abstract boolean removeGroupPermission(String group, String... permissions);

    /**
     * Check if a group has a permission.
     *
     * @param group      the group to check
     * @param permission the permission to check
     * @return if the group has the permission
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

    /***
     * Remove a superadmin.
     *
     * @param uuid the uuid of the player to remove
     */
    public abstract void removeSuperAdmin(UUID uuid);

    /**
     * Set the default group players are in when they join for the first time
     *
     * @param group the default group
     */
    public abstract void setDefaultGroup(PermissionGroup group);
}