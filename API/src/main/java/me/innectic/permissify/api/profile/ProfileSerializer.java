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
package me.innectic.permissify.api.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.permission.GroupInheritance;
import me.innectic.permissify.api.permission.RankTrack;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Innectic
 * @since 8/26/2017
 */
public class ProfileSerializer {

    private final Gson gson;

    public ProfileSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    /**
     * Serialize a permissify profile into a permissify profile file
     *
     * @param profile       the profile to serialize
     * @param baseDirectory the base of the directory to store the profiles in
     * @param profileName   the name of the profile
     * @return              if the profile was successfully stored
     */
    public boolean serialize(PermissifyProfile profile, String baseDirectory, String profileName) {
        try {
            Path file = profilePath(baseDirectory, profileName);
            Files.createDirectories(file.getParent());
            Files.writeString(file, gson.toJson(profile), StandardCharsets.UTF_8);
            return true;
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Deserialize a permissify profile file into a real permissify profile
     *
     * @param profile       the profile file
     * @param baseDirectory the base directory that files are located in
     * @return              the profile, fulfilled if done, empty otherwise.
     */
    public Optional<PermissifyProfile> deserialize(String profile, String baseDirectory) {
        try (Reader reader = Files.newBufferedReader(profilePath(baseDirectory, profile), StandardCharsets.UTF_8)) {
            ProfileData data = gson.fromJson(reader, ProfileData.class);
            if (data == null || data.version() != PermissifyConstants.PERMISSIFY_PROFILE_VERSION
                    || data.groups() == null || data.playerPermissions() == null || data.superAdmins() == null)
                return Optional.empty();
            Map<String, PermissionGroup> groups = new HashMap<>();
            for (var entry : data.groups().entrySet()) {
                GroupData saved = entry.getValue();
                if (saved == null || !entry.getKey().equals(saved.name())) return Optional.empty();
                PermissionGroup group = new PermissionGroup(saved.name(), saved.displayName(), saved.chatColor(), saved.prefix(), saved.suffix());
                group.setWeight(saved.weight());
                if (saved.parents() != null) group.getParents().addAll(saved.parents());
                saved.permissions().forEach(permission -> group.getPermissions().add(permission.toPermission()));
                saved.players().keySet().forEach(uuid -> group.addPlayer(java.util.Objects.requireNonNull(uuid),
                        saved.playerExpirations() == null ? 0 : saved.playerExpirations().getOrDefault(uuid, 0L)));
                groups.put(entry.getKey(), group);
            }
            GroupInheritance.validate(groups);
            PermissionGroup defaultGroup = null;
            if (data.defaultGroup() != null) {
                defaultGroup = groups.get(data.defaultGroup().name());
                if (defaultGroup == null) return Optional.empty();
            }
            Map<UUID, List<Permission>> permissions = new HashMap<>();
            data.playerPermissions().forEach((uuid, entries) -> permissions.put(java.util.Objects.requireNonNull(uuid),
                    entries.stream().map(PermissionData::toPermission).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new))));
            Map<String, RankTrack> tracks = data.tracks() == null ? Map.of() : data.tracks();
            for (var entry : tracks.entrySet()) {
                RankTrack track = entry.getValue();
                if (track == null || !entry.getKey().equals(track.name()) || !track.name().matches("[a-zA-Z0-9_-]{1,100}")) return Optional.empty();
                java.util.Set<String> seen = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                for (String group : track.groups()) if (!groups.containsKey(group) || !seen.add(group)) return Optional.empty();
            }
            return Optional.of(new PermissifyProfile(groups, permissions, defaultGroup, List.copyOf(data.superAdmins()), data.version(), tracks));
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    private record PermissionData(String permission, boolean granted, long expiresAt) {
        Permission toPermission() {
            if (expiresAt < 0) throw new IllegalArgumentException("Invalid permission expiry");
            return new Permission(java.util.Objects.requireNonNull(permission), granted, expiresAt);
        }
    }

    private record GroupData(String name, String displayName, String chatColor, String prefix, String suffix,
                             List<PermissionData> permissions, Map<UUID, Boolean> players, List<String> parents, Map<UUID, Long> playerExpirations, int weight) {}

    private record ProfileData(Map<String, GroupData> groups, Map<UUID, List<PermissionData>> playerPermissions,
                               GroupData defaultGroup, List<UUID> superAdmins, int version, Map<String, RankTrack> tracks) {}
    private Path profilePath(String baseDirectory, String name) {
        if (!name.matches("[a-zA-Z0-9_-]+")) throw new IllegalArgumentException("Invalid profile name");
        return Paths.get(baseDirectory).resolve("profiles").resolve(name + ".permissify");
    }
}