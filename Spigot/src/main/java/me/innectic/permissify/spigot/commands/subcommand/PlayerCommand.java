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
package me.innectic.permissify.spigot.commands.subcommand;

import me.innectic.permissify.api.PermissifyAPI;
import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.language.Language;
import me.innectic.permissify.api.database.ConnectionError;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.util.Tristate;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.PlayerUtil;
import org.bukkit.scheduler.BukkitTask;
import java.util.HashMap;
import java.util.UUID;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 6/25/2017
 */
public class PlayerCommand {

    private final Map<UUID, Map<String, BukkitTask>> expirations = new HashMap<>();

    public String handleAddPlayerToGroup(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();

        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_GROUP_ADD))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (args.length < 2) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_GROUP");

        OfflinePlayer targetPlayer = PlayerUtil.findPlayer(args[0]);
        if (targetPlayer == null) return language.getMessage("INVALID_PLAYER");

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[1]);
        if (group.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[1]);

        Tristate added = plugin.getPermissifyAPI().getDatabaseHandler().get().addPlayerToGroup(targetPlayer.getUniqueId(), group.get());
        if (added == Tristate.NONE)
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("PLAYER_ALREADY_IN_GROUP"));
        else if (added == Tristate.FALSE)
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));

        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(targetPlayer.getUniqueId());

        if (targetPlayer.isOnline()) PermissionUtil.applyPermissions(targetPlayer.getPlayer());

        return language.getMessage("PLAYER_ADDED_TO_GROUP")
                .replace("<PLAYER>", targetPlayer.getName()).replace("<GROUP>", group.get().getName());
    }

    public String handleRemovePlayerFromGroup(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_REMOVE").replace("<TYPE>", language.getMessage("TYPE_PLAYER")).replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_GROUP_REMOVE))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (args.length < 2) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_GROUP");

        OfflinePlayer targetPlayer = PlayerUtil.findPlayer(args[0]);
        if (targetPlayer == null) return language.getMessage("INVALID_PLAYER");

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[1]);
        if (group.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[1]);

        Tristate removed = plugin.getPermissifyAPI().getDatabaseHandler().get().removePlayerFromGroup(targetPlayer.getUniqueId(), group.get());
        if (removed == Tristate.NONE)
            return language.getMessage("UNABLE_TO_REMOVE").replace("<TYPE>", language.getMessage("TYPE_PLAYER")).replace("<REASON>", language.getMessage("PLAYER_NOT_IN_GROUP_REASON"));
        else if (removed == Tristate.FALSE)
            return language.getMessage("UNABLE_TO_REMOVE").replace("<TYPE>", language.getMessage("TYPE_PLAYER")).replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(targetPlayer.getUniqueId());

        if (targetPlayer.isOnline()) PermissionUtil.applyPermissions(targetPlayer.getPlayer());
        return language.getMessage("PLAYER_REMOVED_FROM_GROUP")
                .replace("<PLAYER>", targetPlayer.getName()).replace("<GROUP>", group.get().getName());
    }

    public String handleSetOrGetPrimaryGroup(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_SET").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_SET_MAIN_GROUP))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (args.length < 1) return language.getMessage("NOT_ENOUGH_ARGUMENTS_SET_MAIN_GROUP");
        OfflinePlayer player = PlayerUtil.findPlayer(args[0]);
        if (player == null) return language.getMessage("INVALID_PLAYER");
        if (args.length == 1) {
            Optional<PermissionGroup> primary = plugin.getPermissifyAPI().getDatabaseHandler().get().getPrimaryGroup(player.getUniqueId());
            return language.getMessage("PRIMARY_GROUP_TEMPLATE").replace("<GROUP>", primary.map(PermissionGroup::getName)
                    .orElse(language.getMessage("EMPTY_DEFAULT_GROUP_NAME")));
        }
        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[1]);

        if (group.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[1]);
        if (!group.get().getPlayers().containsKey(player.getUniqueId()))
            return language.getMessage("PLAYER_NOT_IN_GROUP").replace("<PLAYER>", player.getName()).replace("<GROUP>", group.get().getName());

        Tristate groupSet = plugin.getPermissifyAPI().getDatabaseHandler().get().setPrimaryGroup(group.get(), player.getUniqueId());
        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(player.getUniqueId());

        if (groupSet == Tristate.TRUE) {
            if (player.isOnline()) PermissionUtil.applyPermissions(player.getPlayer());
            return language.getMessage("MAIN_GROUP_SET").replace("<PLAYER>", player.getName()).replace("<GROUP>", group.get().getName());
        }
        else if (groupSet == Tristate.FALSE) {
            PermissifyAPI.get().ifPresent(api -> api.getDisplayUtil().displayError(ConnectionError.REJECTED, Optional.empty()));
            return language.getMessage("UNABLE_TO_SET").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));
        }
        return language.getMessage("ALREADY_MAIN_GROUP").replace("<GROUP>", group.get().getName());
    }

    public String handleAddPermission(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_ADD))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (args.length < 2) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_PERMISSION");

        OfflinePlayer targetPlayer = PlayerUtil.findPlayer(args[0]);
        if (targetPlayer == null) return language.getMessage("INVALID_PLAYER");

        if (plugin.getPermissifyAPI().getDatabaseHandler().get().hasPermission(targetPlayer.getUniqueId(), args[1])) {
            // Player already has this permission.
            return language.getMessage("PLAYER_ALREADY_HAS_PERMISSION").replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]);
        }
        long ticks = 0;
        if (args.length >= 3) {
            try {
                long seconds = Long.parseLong(args[2]);
                if (seconds <= 0) throw new IllegalArgumentException();
                ticks = Math.multiplyExact(seconds, 20L);
            } catch (IllegalArgumentException | ArithmeticException e) {
                return language.getMessage("INVALID_ARGUMENT").replace("<ARGUMENT>", args[2]);
            }
        }
        var handler = plugin.getPermissifyAPI().getDatabaseHandler().get();
        handler.addPermission(targetPlayer.getUniqueId(), args[1]);
        if (!handler.hasPermission(targetPlayer.getUniqueId(), args[1]))
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));
        handler.updateCache(targetPlayer.getUniqueId());
        if (targetPlayer.isOnline()) PermissionUtil.applyPermissions(targetPlayer.getPlayer());

        cancelExpiration(targetPlayer.getUniqueId(), args[1]);
        if (ticks > 0) {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cancelExpiration(targetPlayer.getUniqueId(), args[1]);
                handler.removePermission(targetPlayer.getUniqueId(), args[1]);
                handler.updateCache(targetPlayer.getUniqueId());
                if (targetPlayer.isOnline()) PermissionUtil.applyPermissions(targetPlayer.getPlayer());
            }, ticks);
            expirations.computeIfAbsent(targetPlayer.getUniqueId(), uuid -> new HashMap<>()).put(args[1], task);
            return language.getMessage("PERMISSION_ADDED_PLAYER_TIMED")
                    .replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]).replace("<SECONDS>", args[2]);
        }
        return language.getMessage("PERMISSION_ADDED_PLAYER")
                .replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]);
    }

    public String handleRemovePermission(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_REMOVE))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (args.length < 2) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_PERMISSION");

        OfflinePlayer targetPlayer = PlayerUtil.findPlayer(args[0]);
        if (targetPlayer == null) return language.getMessage("INVALID_PLAYER");
        if (!plugin.getPermissifyAPI().getDatabaseHandler().get().hasPermission(targetPlayer.getUniqueId(), args[1])) {
            // Player doesn't have this permission
            return language.getMessage("PLAYER_DOES_NOT_HAVE_PERMISSION").replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]);
        }

        plugin.getPermissifyAPI().getDatabaseHandler().get().removePermission(targetPlayer.getUniqueId(), args[1]);
        if (plugin.getPermissifyAPI().getDatabaseHandler().get().hasPermission(targetPlayer.getUniqueId(), args[1]))
            return language.getMessage("UNABLE_TO_REMOVE").replace("<TYPE>", language.getMessage("TYPE_PERMISSION")).replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));
        cancelExpiration(targetPlayer.getUniqueId(), args[1]);
        if (targetPlayer.isOnline()) PermissionUtil.applyPermissions(targetPlayer.getPlayer());

        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(targetPlayer.getUniqueId());
        return language.getMessage("PERMISSION_REMOVED_PLAYER")
                .replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]);
    }

    public String handleListGroups(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_LIST").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_GROUP_LIST))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (args.length < 1) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_GROUP");

        OfflinePlayer targetPlayer = PlayerUtil.findPlayer(args[0]);
        if (targetPlayer == null) return language.getMessage("INVALID_PLAYER");

        List<String> groups = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups().entrySet().stream()
                .filter(permissionGroup -> permissionGroup.getValue().hasPlayer(targetPlayer.getUniqueId()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return language.getMessage("PLAYER_GROUP_LIST").replace("<PLAYER>", targetPlayer.getName())
                .replace("<GROUPS>", String.join(", ", groups));
    }

    public String handleListPermissions(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_LIST))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_LIST").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (args.length < 1) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_PERMISSIONS");
        OfflinePlayer targetPlayer = PlayerUtil.findPlayer(args[0]);
        if (targetPlayer == null) return language.getMessage("INVALID_PLAYER");

        List<PermissionGroup> groups = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups(targetPlayer.getUniqueId());
        List<String> permissions = new ArrayList<>();
        groups.forEach(group -> permissions.addAll(group.getPermissions().stream().map(Permission::getPermission).collect(Collectors.toList())));
        permissions.addAll(plugin.getPermissifyAPI().getDatabaseHandler().get().getPermissions(targetPlayer.getUniqueId())
                .stream().map(Permission::getPermission).collect(Collectors.toList()));
        return language.getMessage("GROUP_PERMISSIONS").replace("<GROUP>", targetPlayer.getName())
                .replace("<PERMISSIONS>", String.join(", ", permissions.stream().distinct().toList()));
    }
    private void cancelExpiration(UUID uuid, String permission) {
        Map<String, BukkitTask> tasks = expirations.get(uuid);
        if (tasks == null) return;
        BukkitTask task = tasks.remove(permission);
        if (task != null) task.cancel();
        if (tasks.isEmpty()) expirations.remove(uuid);
    }

    public void cancelExpirations() {
        expirations.values().forEach(tasks -> tasks.values().forEach(BukkitTask::cancel));
        expirations.clear();
    }
}