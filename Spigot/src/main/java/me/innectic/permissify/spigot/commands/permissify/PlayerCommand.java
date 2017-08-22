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
package me.innectic.permissify.spigot.commands.permissify;

import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.commands.CommandResponse;
import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 6/25/2017
 */
public class PlayerCommand {

    public CommandResponse handleAddPlayerToGroup(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_ADD.replace("<REASON>", "No database handler."), false);
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_GROUP_ADD) && !plugin.getPermissifyAPI().getDatabaseHandler().get().isSuperAdmin(((Player) sender).getUniqueId()))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args.length < 2) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_GROUP, false);
        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) return new CommandResponse(PermissifyConstants.INVALID_PLAYER, false);
        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups().stream()
                .filter(permissionGroup -> permissionGroup.getName().equals(args[1])).findFirst();
        if (!group.isPresent()) return new CommandResponse(PermissifyConstants.INVALID_GROUP, false);
        plugin.getPermissifyAPI().getDatabaseHandler().get().addPlayerToGroup(targetPlayer.getUniqueId(), group.get());
        if (targetPlayer.isOnline()) group.get().getPermissions().forEach(permission ->
                targetPlayer.getPlayer().addAttachment(plugin, permission.getPermission(), true));
        return new CommandResponse(PermissifyConstants.PLAYER_ADDED_TO_GROUP
                .replace("<PLAYER>", targetPlayer.getName()).replace("<GROUP>", group.get().getName()), true);
    }

    public CommandResponse handleRemovePlayerFromGroup(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_REMOVE.replace("<REASON>", "No database handler."), false);
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_GROUP_REMOVE) && !plugin.getPermissifyAPI().getDatabaseHandler().get().isSuperAdmin(((Player) sender).getUniqueId()))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args.length < 2) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_GROUP, false);
        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) return new CommandResponse(PermissifyConstants.INVALID_PLAYER, false);
        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups().stream()
                .filter(permissionGroup -> permissionGroup.getName().equals(args[1])).findFirst();
        if (!group.isPresent()) return new CommandResponse(PermissifyConstants.INVALID_GROUP, false);
        plugin.getPermissifyAPI().getDatabaseHandler().get().removePlayerFromGroup(targetPlayer.getUniqueId(), group.get());
        if (targetPlayer.isOnline()) group.get().getPermissions().forEach(permission ->
                targetPlayer.getPlayer().addAttachment(plugin, permission.getPermission(), false));
        return new CommandResponse(PermissifyConstants.PLAYER_REMOVED_FROM_GROUP
                .replace("<PLAYER>", targetPlayer.getName()).replace("<GROUP>", group.get().getName()), true);
    }

    public CommandResponse handleSetMainGroup(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler."), false);
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_SET_MAIN_GROUP))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args.length < 2) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_SET_MAIN_GROUP, false);
        OfflinePlayer player = Bukkit.getPlayer(args[0]);
        if (player == null || !player.hasPlayedBefore()) return new CommandResponse(PermissifyConstants.INVALID_PLAYER, false);
        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups().stream()
                .filter(permissionGroup -> permissionGroup.getName().equals(args[1])).findFirst();
        if (!group.isPresent()) return new CommandResponse(PermissifyConstants.INVALID_GROUP, false);
        if (!group.get().getPlayers().containsKey(player.getUniqueId()))
            return new CommandResponse(PermissifyConstants.PLAYER_NOT_IN_GROUP.replace("<PLAYER>", player.getName()).replace("<GROUP>", group.get().getName()), false);
        boolean groupSet = plugin.getPermissifyAPI().getDatabaseHandler().get().setPrimaryGroup(group.get(), player.getUniqueId());
        if (groupSet)
            return new CommandResponse(PermissifyConstants.MAIN_GROUP_SET.replace("<PLAYER>", player.getName()).replace("<GROUP>", group.get().getName()), false);
        // Should be pretty much impossible to get here, unless the database isn't connected.
        return new CommandResponse(PermissifyConstants.PLAYER_NOT_IN_GROUP.replace("<PLAYER>", player.getName()).replace("<GROUP>", group.get().getName()), false);
    }

    public CommandResponse handleAddPermission(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_ADD.replace("<REASON>", "No database handler."), false);
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_ADD) && !plugin.getPermissifyAPI().getDatabaseHandler().get().isSuperAdmin(((Player) sender).getUniqueId()))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args.length < 2) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_PERMISSION, false);
        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) return new CommandResponse(PermissifyConstants.INVALID_PLAYER, false);
        plugin.getPermissifyAPI().getDatabaseHandler().get().addPermission(targetPlayer.getUniqueId(), args[0]);
        if (targetPlayer.isOnline()) targetPlayer.getPlayer().addAttachment(plugin, args[0], true);
        return new CommandResponse(PermissifyConstants.PERMISSION_ADDED_PLAYER
                .replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[0]), true);
    }

    public CommandResponse handleRemovePermission(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_ADD.replace("<REASON>", "No database handler."), false);
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_REMOVE) && !plugin.getPermissifyAPI().getDatabaseHandler().get().isSuperAdmin(((Player) sender).getUniqueId()))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args.length < 2) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_PERMISSION, false);
        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) return new CommandResponse(PermissifyConstants.INVALID_PLAYER, false);
        plugin.getPermissifyAPI().getDatabaseHandler().get().removePermission(targetPlayer.getUniqueId(), args[0]);
        if (targetPlayer.isOnline()) targetPlayer.getPlayer().addAttachment(plugin, args[0], false);
        return new CommandResponse(PermissifyConstants.PERMISSION_REMOVED_PLAYER
                .replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[0]), true);
    }

    public CommandResponse handleListGroups(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_LIST.replace("<REASON>", "No database handler."), false);
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_GROUP_LIST) && !plugin.getPermissifyAPI().getDatabaseHandler().get().isSuperAdmin(((Player) sender).getUniqueId()))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_GROUP, false);
        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) return new CommandResponse(PermissifyConstants.INVALID_PLAYER, false);
        List<String> groups = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups().stream()
                .filter(permissionGroup -> permissionGroup.hasPlayer(targetPlayer.getUniqueId()))
                .map(PermissionGroup::getName)
                .collect(Collectors.toList());
        return new CommandResponse(PermissifyConstants.PLAYER_GROUP_LIST.replace("<PLAYER>", targetPlayer.getName())
                .replace("<GROUPS>", String.join(", ", groups)), true);
    }

    public CommandResponse handleListPermissions(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin((Player) sender, PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_LIST))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_LIST.replace("<REASON>", "No database handler."), false);
        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_PERMISSIONS, false);
        OfflinePlayer targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) return new CommandResponse(PermissifyConstants.INVALID_PLAYER, false);
        // This sucks, can probably be cleanup up.
        List<PermissionGroup> groups = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups().stream()
                .filter(permissionGroup -> permissionGroup.hasPlayer(targetPlayer.getUniqueId()))
                .collect(Collectors.toList());
        List<String> permissions = new ArrayList<>();
        groups.forEach(group -> permissions.addAll(group.getPermissions().stream().map(Permission::getPermission).collect(Collectors.toList())));
        permissions.addAll(plugin.getPermissifyAPI().getDatabaseHandler().get().getPermissions(targetPlayer.getUniqueId())
                .stream().map(Permission::getPermission).collect(Collectors.toList()));
        return new CommandResponse(PermissifyConstants.GROUP_PERMISSIONS.replace("<GROUP>", targetPlayer.getName())
                .replace("<PERMISSIONS>", String.join(", ", permissions)), true);
    }
}
