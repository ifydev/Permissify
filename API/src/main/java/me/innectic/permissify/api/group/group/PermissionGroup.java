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
package me.innectic.permissify.api.group.group;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.innectic.permissify.api.group.Permission;
import me.innectic.permissify.api.group.ladder.AbstractLadder;

import java.io.Serializable;
import java.util.*;

/**
 * @author Innectic
 * @since 6/14/2017
 */
@RequiredArgsConstructor
public class PermissionGroup implements Serializable {
    @Getter private final String name;
    @Getter private final String chatColor;
    @Getter private final String prefix;
    @Getter private final String suffix;
    @NonNull @Getter @Setter private AbstractLadder ladder;
    @Getter private List<Permission> permissions = new ArrayList<>();
    @Getter private Map<UUID, Boolean> players = new HashMap<>();

    /**
     * Remove a permission from the group
     *
     * @param permission the permission to remove
     */
    public void removePermission(String permission) {
        Optional<Permission> perm = permissions.stream().filter(groupPermission -> groupPermission.getPermission().equals(permission)).findFirst();
        perm.ifPresent(groupPermission -> groupPermission.setGranted(false));
    }

    /**
     * Add a permission to the group
     *
     * @param permission the permission to add
     */
    public void addPermission(String permission) {
        Optional<Permission> perm = permissions.stream().filter(groupPermission -> groupPermission.getPermission().equals(permission)).findFirst();
        if (perm.isPresent()) perm.get().setGranted(true);
        else permissions.add(new Permission(permission, true));
    }

    public boolean hasPermission(String permission) {
        return permissions.stream().anyMatch(perm -> perm.getPermission().equals(permission));
    }

    public void addPlayer(UUID uuid, boolean isPrimary) {
        players.put(uuid, isPrimary);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public boolean hasPlayer(UUID uuid) {
        return players.containsKey(uuid);
    }

    public boolean isPrimaryGroup(UUID uuid) {
        return players.containsKey(uuid) && players.get(uuid).equals(true);
    }

    public void setPrimaryGroup(UUID uuid, boolean isPrimary) {
        players.put(uuid, isPrimary);
    }
}
