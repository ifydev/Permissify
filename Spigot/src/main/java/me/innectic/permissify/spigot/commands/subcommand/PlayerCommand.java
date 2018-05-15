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

import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.spigot.events.custom.ChangeType;
import me.innectic.permissify.spigot.events.custom.PlayerGroupAddEvent;
import me.innectic.permissify.spigot.events.custom.PlayerGroupRemoveEvent;
import me.innectic.permissify.spigot.events.custom.PlayerPermissionChangeEvent;
import me.innectic.permissify.spigot.utils.MiscUtil;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

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

    public String handleAddPlayerToGroup(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();

        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return PermissifyConstants.UNABLE_TO_ADD.replace("<REASON>", "No database handler.");

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_GROUP_ADD))
            return PermissifyConstants.INSUFFICIENT_PERMISSIONS;

        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_GROUP;

        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;
        Optional<PermissionGroup> oldGroup = plugin.getPermissifyAPI().getDatabaseHandler().get()
                .getGroups(targetPlayer.getUniqueId()).stream()
                .filter(g -> g.isPrimaryGroup(targetPlayer.getUniqueId())).findFirst();

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[1]);
        if (!group.isPresent()) return PermissifyConstants.INVALID_GROUP.replace("<GROUP>", args[1]);

        plugin.getPermissifyAPI().getDatabaseHandler().get().addPlayerToGroup(targetPlayer.getUniqueId(), group.get());
        if (targetPlayer.isOnline()) group.get().getPermissions().forEach(permission ->
                targetPlayer.getPlayer().addAttachment(plugin, permission.getPermission(), permission.isGranted()));

        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(targetPlayer.getUniqueId());
        Bukkit.getServer().getPluginManager().callEvent(new PlayerGroupAddEvent(targetPlayer, group.get(), oldGroup));
        return PermissifyConstants.PLAYER_ADDED_TO_GROUP
                .replace("<PLAYER>", targetPlayer.getName()).replace("<GROUP>", group.get().getName());
    }

    public String handleRemovePlayerFromGroup(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return PermissifyConstants.UNABLE_TO_REMOVE.replace("<REASON>", "No database handler.");

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_GROUP_REMOVE))
            return PermissifyConstants.INSUFFICIENT_PERMISSIONS;

        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_GROUP;

        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[1]);
        if (!group.isPresent()) return PermissifyConstants.INVALID_GROUP.replace("<GROUP>", args[1]);
        plugin.getPermissifyAPI().getDatabaseHandler().get().removePlayerFromGroup(targetPlayer.getUniqueId(), group.get());

        if (targetPlayer.isOnline()) group.get().getPermissions().forEach(permission ->
                targetPlayer.getPlayer().addAttachment(plugin, permission.getPermission(), false));

        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(targetPlayer.getUniqueId());
        Bukkit.getServer().getPluginManager().callEvent(new PlayerGroupRemoveEvent(targetPlayer, group.get()));

        return PermissifyConstants.PLAYER_REMOVED_FROM_GROUP
                .replace("<PLAYER>", targetPlayer.getName()).replace("<GROUP>", group.get().getName());
    }

    public String handleSetMainGroup(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler.");

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_SET_MAIN_GROUP))
            return PermissifyConstants.INSUFFICIENT_PERMISSIONS;

        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_SET_MAIN_GROUP;

        OfflinePlayer player = Bukkit.getPlayer(args[0]);
        if (player == null || !player.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;
        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[1]);

        if (!group.isPresent()) return PermissifyConstants.INVALID_GROUP.replace("<GROUP>", args[1]);
        if (!group.get().getPlayers().containsKey(player.getUniqueId()))
            return PermissifyConstants.PLAYER_NOT_IN_GROUP.replace("<PLAYER>", player.getName()).replace("<GROUP>", group.get().getName());

        boolean groupSet = plugin.getPermissifyAPI().getDatabaseHandler().get().setPrimaryGroup(group.get(), player.getUniqueId());
        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(player.getUniqueId());

        if (groupSet)
            return PermissifyConstants.MAIN_GROUP_SET.replace("<PLAYER>", player.getName()).replace("<GROUP>", group.get().getName());
        // Should be pretty much impossible to get here, unless the database isn't connected.
        return PermissifyConstants.PLAYER_NOT_IN_GROUP.replace("<PLAYER>", player.getName()).replace("<GROUP>", group.get().getName());
    }

    public String handleAddPermission(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return PermissifyConstants.UNABLE_TO_ADD.replace("<REASON>", "No database handler.");

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_ADD))
            return PermissifyConstants.INSUFFICIENT_PERMISSIONS;

        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_PERMISSION;

        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && targetPlayer.isOnline())) return PermissifyConstants.INVALID_PLAYER;

        if (plugin.getPermissifyAPI().getDatabaseHandler().get().hasPermission(targetPlayer.getUniqueId(), args[1])) {
            // Player already has this permission.
            return PermissifyConstants.PLAYER_ALREADY_HAS_PERMISSION.replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]);
        }
        plugin.getPermissifyAPI().getDatabaseHandler().get().addPermission(targetPlayer.getUniqueId(), args[1]);
        if (targetPlayer.isOnline()) {
            targetPlayer.getPlayer().addAttachment(plugin, args[1], true);
            ((Player) targetPlayer).recalculatePermissions();
        }

        if (args.length >= 3) {
            // Timed permission.
            if (!MiscUtil.isInt(args[2])) return PermissifyConstants.INVALID_ARGUMENT.replace("<ARGUMENT>", args[2]);
            int time = Integer.parseInt(args[2]);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                targetPlayer.getPlayer().addAttachment(plugin, args[1], false);
                if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent()) return;
                plugin.getPermissifyAPI().getDatabaseHandler().get().removePermission(targetPlayer.getUniqueId(), args[1]);
                plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(targetPlayer.getUniqueId());
            }, time * 1000);

            return PermissifyConstants.PERMISSION_ADDED_PLAYER_TIMED
                    .replace("<PLAYER>", targetPlayer.getName())
                    .replace("<PERMISSION>", args[1])
                    .replace("<SECONDS>", args[2]);
        }

        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(targetPlayer.getUniqueId());
        Bukkit.getPluginManager().callEvent(new PlayerPermissionChangeEvent(targetPlayer, args[1], ChangeType.ADDED));
        return PermissifyConstants.PERMISSION_ADDED_PLAYER
                .replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]);
    }

    public String handleRemovePermission(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return PermissifyConstants.UNABLE_TO_ADD.replace("<REASON>", "No database handler.");

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_REMOVE))
            return PermissifyConstants.INSUFFICIENT_PERMISSIONS;

        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_PERMISSION;

        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && targetPlayer.isOnline())) return PermissifyConstants.INVALID_PLAYER;
        if (!plugin.getPermissifyAPI().getDatabaseHandler().get().hasPermission(targetPlayer.getUniqueId(), args[1])) {
            // Player doesn't have this permission
            return PermissifyConstants.PLAYER_DOES_NOT_HAVE_PERMISSION.replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]);
        }

        plugin.getPermissifyAPI().getDatabaseHandler().get().removePermission(targetPlayer.getUniqueId(), args[1]);
        if (targetPlayer.isOnline()) {
            Optional<PermissionAttachment> attachment = PermissionUtil.findAttachmentByPermission(targetPlayer.getPlayer(), args[1]);
            attachment.ifPresent(targetPlayer.getPlayer()::removeAttachment);
        }

        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(targetPlayer.getUniqueId());
        Bukkit.getPluginManager().callEvent(new PlayerPermissionChangeEvent(targetPlayer, args[1], ChangeType.REMOVED));
        return PermissifyConstants.PERMISSION_REMOVED_PLAYER
                .replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]);
    }

    public String handleListGroups(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return PermissifyConstants.UNABLE_TO_LIST.replace("<REASON>", "No database handler.");

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_GROUP_LIST))
            return PermissifyConstants.INSUFFICIENT_PERMISSIONS;

        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_GROUP;

        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;

        List<String> groups = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups().entrySet().stream()
                .filter(permissionGroup -> permissionGroup.getValue().hasPlayer(targetPlayer.getUniqueId()))
                .filter(entry -> entry.getValue().hasPlayer(targetPlayer.getUniqueId()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return PermissifyConstants.PLAYER_GROUP_LIST.replace("<PLAYER>", targetPlayer.getName())
                .replace("<GROUPS>", String.join(", ", groups));
    }

    public String handleListPermissions(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_LIST))
            return PermissifyConstants.INSUFFICIENT_PERMISSIONS;

        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return PermissifyConstants.UNABLE_TO_LIST.replace("<REASON>", "No database handler.");

        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_PERMISSIONS;
        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) return PermissifyConstants.INVALID_PLAYER;

        // This sucks, can probably be cleaned up.
        List<PermissionGroup> groups = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups(targetPlayer.getUniqueId());
        List<String> permissions = new ArrayList<>();
        groups.forEach(group -> permissions.addAll(group.getPermissions().stream().map(Permission::getPermission).collect(Collectors.toList())));
        permissions.addAll(plugin.getPermissifyAPI().getDatabaseHandler().get().getPermissions(targetPlayer.getUniqueId())
                .stream().map(Permission::getPermission).collect(Collectors.toList()));
        return PermissifyConstants.GROUP_PERMISSIONS.replace("<GROUP>", targetPlayer.getName())
                .replace("<PERMISSIONS>", String.join(", ", permissions));
    }
}
