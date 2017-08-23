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

import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.commands.CommandResponse;
import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.utils.ColorUtil;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 6/15/2017
 */
public class GroupCommand {

    /**
     * Handle the group create
     *
     * @param sender the sender of the command
     * @param args   the extra arguments of the command
     * @return the response, and if it was successful
     */
    public CommandResponse handleAddGroup(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin((Player) sender, PermissifyConstants.PERMISSIFY_GROUP_CREATE))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_CREATE.replace("<TYPE>", "group").replace("<REASON>", "No database handler."), false);
        if (args.length < 4) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_GROUP_CREATE, false);
        if (!ColorUtil.isValidChatColor(args[3])) return new CommandResponse(PermissifyConstants.INVALID_CHATCOLOR.replace("<COLOR>", args[3]), true);
        // Create the new group
        boolean created = plugin.getPermissifyAPI().getDatabaseHandler().get().createGroup(args[0], args[1], args[2], args[3]);
        if (created) return new CommandResponse(PermissifyConstants.GROUP_CREATED.replace("<GROUP>", args[0]), true);
        return new CommandResponse(PermissifyConstants.UNABLE_TO_CREATE.replace("<TYPE>", "group").replace("<REASON>", "Unable to connect to database."), false);
    }

    /**
     * Handle the group remove
     *
     * @param sender the sender of the command
     * @param args the arguments of the command
     * @return the response, and if it was successful
     */
    public CommandResponse handleDeleteGroup(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        // Check permissions and arguments
        if (!PermissionUtil.hasPermissionOrSuperAdmin((Player) sender, PermissifyConstants.PERMISSIFY_GROUP_REMOVE))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_REMOVE.replace("<TYPE>", "group").replace("<REASON>", "No database handler"), false);
        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_GROUP_REMOVE, false);
        boolean removed = plugin.getPermissifyAPI().getDatabaseHandler().get().deleteGroup(args[0]);
        if (removed) return new CommandResponse(PermissifyConstants.GROUP_REMOVED.replace("<GROUP>", args[0]), false);
        return new CommandResponse(PermissifyConstants.UNABLE_TO_REMOVE.replace("<TYPE>", "group").replace("<REASON>", "Unable to connect to database"), false);
    }

    public CommandResponse handlePermissionAdd(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_CREATE.replace("<TYPE>", "group").replace("<REASON>", "No database handler."), false);
        if (!PermissionUtil.hasPermissionOrSuperAdmin((Player) sender, PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_ADD))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args.length < 2) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_ADD, false);
        boolean added = plugin.getPermissifyAPI().getDatabaseHandler().get().addGroupPermission(args[0], ArgumentUtil.getRemainingArgs(1, args));
        if (!added) return new CommandResponse(PermissifyConstants.UNABLE_TO_ADD.replace(
                "<REASON>", "Permission is already on group!"), false);
        String[] remaining = ArgumentUtil.getRemainingArgs(1, args);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent()) return;
            Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups()
                    .stream().filter(permissionGroup -> permissionGroup.getName().equals(args[0])).findFirst();
            group.ifPresent(permissionGroup -> Arrays.stream(remaining).forEach(permission ->
                    permissionGroup.getPlayers().keySet().stream().map(Bukkit::getPlayer)
                            .filter(Objects::nonNull).forEach(player -> player.addAttachment(plugin, permission, true))));
        });
        return new CommandResponse(PermissifyConstants.PERMISSION_ADDED_GROUP.replace("<PERMISSION>",
                String.join(", ", ArgumentUtil.getRemainingArgs(1, args)).replace("<GROUP>", args[0])), true);
    }

    public CommandResponse handlePermissionRemove(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin((Player) sender, PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_REMOVE))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_CREATE.replace("<TYPE>", "group").replace("<REASON>", "No database handler."), false);
        if (args.length < 2) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_REMOVE, false);
        boolean added = plugin.getPermissifyAPI().getDatabaseHandler().get().removeGroupPermission(args[0], ArgumentUtil.getRemainingArgs(1, args));
        if (!added) return new CommandResponse(PermissifyConstants.UNABLE_TO_ADD.replace("<REASON>", "Permission isn't on group!"), false);
        String[] remaining = ArgumentUtil.getRemainingArgs(1, args);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent()) return;
            Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups()
                    .stream().filter(permissionGroup -> permissionGroup.getName().equals(args[0])).findFirst();
            group.ifPresent(permissionGroup -> {
                for (String permission : remaining)
                permissionGroup.getPlayers().keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull)
                        .forEach(player -> player.addAttachment(plugin, permission, false));
            });
        });
        return new CommandResponse(PermissifyConstants.PERMISSION_REMOVED_GROUP.replace("<PERMISSION>",
                String.join(", ", remaining).replace("<GROUP>", args[0])), true);
    }

    public CommandResponse handleListPermissions(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin((Player) sender, PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_LIST))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_LIST.replace("<REASON>", "No database handler"), false);
        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_LIST, false);
        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups().stream()
                .filter(permissionGroup -> permissionGroup.getName().equals(args[0])).findFirst();
        if (!group.isPresent()) return new CommandResponse(PermissifyConstants.INVALID_GROUP.replace("<GROUP>", args[0]), false);
        List<String> groupPermissions = group.get().getPermissions().stream().map(Permission::getPermission).collect(Collectors.toList());
        return new CommandResponse(PermissifyConstants.GROUP_PERMISSIONS.replace("<GROUP>", group.get().getName())
                .replace("<PERMISSIONS>", String.join(", ", groupPermissions)), true);
    }

    public CommandResponse handleListGroups(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin((Player) sender, PermissifyConstants.PERMISSIFY_GROUP_LIST))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_LIST.replace("<REASON>", "No database handler"), false);
        List<PermissionGroup> groups = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups();
        List<String> groupNames = groups.stream().map(PermissionGroup::getName).collect(Collectors.toList());
        return new CommandResponse(PermissifyConstants.GROUP_LIST.replace("<GROUPS>", String.join(", ", groupNames)), true);
    }

    public CommandResponse handleSetDefault(CommandSender sender, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin((Player) sender, PermissifyConstants.PERMISSIFY_GROUP_DEFAULT))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler"), false);
        DatabaseHandler handler = plugin.getPermissifyAPI().getDatabaseHandler().get();
        if (args.length < 1) {
            // If we only have one, show the default group.
            String defaultGroupName = handler.getDefaultGroup().map(group -> ChatColor.getByChar(group.getChatColor()) + group.getName())
                    .orElse(PermissifyConstants.EMPTY_DEFAULT_GROUP_NAME);
            String response = PermissifyConstants.DEFAULT_GROUP_RESPONSE.replace("<GROUP>", defaultGroupName);
            return new CommandResponse(response, true);
        }
        Optional<PermissionGroup> defaultGroup = handler.getGroup(args[0]);
        if (!defaultGroup.isPresent())
            return new CommandResponse(PermissifyConstants.INVALID_GROUP.replace("<GROUP>", args[0]), false);
        handler.setDefaultGroup(defaultGroup.get());
        return new CommandResponse(PermissifyConstants.DEFAULT_GROUP_SET.replace("<GROUP>", args[0]), true);
    }
}
