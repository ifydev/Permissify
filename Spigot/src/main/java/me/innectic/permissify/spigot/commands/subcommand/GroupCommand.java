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

import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.util.Tristate;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.language.Language;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.utils.ColorUtil;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
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
    public String handleAddGroup(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_GROUP_CREATE))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_CREATE").replace("<TYPE>", language.getMessage("TYPE_GROUP")).replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (args.length < 5) return language.getMessage("NOT_ENOUGH_ARGUMENTS_GROUP_CREATE");
        if (!ColorUtil.isValidChatColor(args[4])) return language.getMessage("INVALID_CHATCOLOR").replace("<COLOR>", args[4]);

        // Create the new group
        Tristate created = plugin.getPermissifyAPI().getDatabaseHandler().get().createGroup(args[0], args[1], args[2], args[3], args[4].substring(args[4].length() - 1).toLowerCase(java.util.Locale.ROOT));
        if (created == Tristate.TRUE) return language.getMessage("GROUP_CREATED").replace("<GROUP>", args[0]);
        else if (created == Tristate.NONE) return language.getMessage("GROUP_ALREADY_EXISTS").replace("<GROUP>", args[0]);
        return language.getMessage("UNABLE_TO_CREATE").replace("<TYPE>", language.getMessage("TYPE_GROUP")).replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE_PERIOD"));
    }

    /**
     * Handle the group remove
     *
     * @param sender the sender of the command
     * @param args the arguments of the command
     * @return the response, and if it was successful
     */
    public String handleDeleteGroup(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();

        // Check permissions and arguments
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_GROUP_REMOVE))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return language.getMessage("UNABLE_TO_REMOVE").replace("<TYPE>", language.getMessage("TYPE_GROUP")).replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER"));

        if (args.length < 1) return language.getMessage("NOT_ENOUGH_ARGUMENTS_GROUP_REMOVE");
        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[0]);
        if (group.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[0]);

        List<UUID> playersInGroup = group.get().getPlayers().keySet().stream().collect(Collectors.toList());
        Tristate removed = plugin.getPermissifyAPI().getDatabaseHandler().get().deleteGroup(args[0]);
        if (removed == Tristate.TRUE) {
            playersInGroup.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(PermissionUtil::applyPermissions);
            return language.getMessage("GROUP_REMOVED").replace("<GROUP>", args[0]);
        } else if (removed == Tristate.NONE) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[0]);
        return language.getMessage("UNABLE_TO_REMOVE").replace("<TYPE>", language.getMessage("TYPE_GROUP")).replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));
    }

    public String handlePermissionAdd(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_CREATE").replace("<TYPE>", language.getMessage("TYPE_GROUP")).replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_ADD))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");
        if (args.length < 2) return language.getMessage("NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_ADD");

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[0]);
        if (group.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[0]);
        if (Arrays.stream(ArgumentUtil.getRemainingArgs(1, args)).anyMatch(group.get()::hasPermission))
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("GROUP_ALREADY_HAS_PERMISSION"));
        boolean added = plugin.getPermissifyAPI().getDatabaseHandler().get().addGroupPermission(args[0], ArgumentUtil.getRemainingArgs(1, args));
        if (!added) return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));

        group.get().getPlayers().keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(PermissionUtil::applyPermissions);
        return language.getMessage("PERMISSION_ADDED_GROUP").replace("<PERMISSION>",
                String.join(", ", ArgumentUtil.getRemainingArgs(1, args))).replace("<GROUP>", args[0]);
    }

    public String handlePermissionRemove(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_REMOVE))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_CREATE").replace("<TYPE>", language.getMessage("TYPE_GROUP")).replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (args.length < 2) return language.getMessage("NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_REMOVE");
        String[] remaining = ArgumentUtil.getRemainingArgs(1, args);
        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[0]);
        if (group.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[0]);
        if (Arrays.stream(remaining).anyMatch(permission -> !group.get().hasPermission(permission)))
            return language.getMessage("UNABLE_TO_REMOVE").replace("<TYPE>", language.getMessage("TYPE_PERMISSION")).replace("<REASON>", language.getMessage("GROUP_MISSING_PERMISSION"));
        boolean added = plugin.getPermissifyAPI().getDatabaseHandler().get().removeGroupPermission(args[0], remaining);
        if (!added) return language.getMessage("UNABLE_TO_REMOVE").replace("<TYPE>", language.getMessage("TYPE_PERMISSION")).replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));

        group.get().getPlayers().keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(PermissionUtil::applyPermissions);

        return language.getMessage("PERMISSION_REMOVED_GROUP").replace("<PERMISSION>",
                String.join(", ", remaining)).replace("<GROUP>", args[0]);
    }

    public String handleListPermissions(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_LIST))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_LIST").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER"));

        if (args.length < 1) return language.getMessage("NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_LIST");

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[0]);
        if (group.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[0]);

        List<String> groupPermissions = group.get().getPermissions().stream().map(Permission::getPermission).collect(Collectors.toList());
        return language.getMessage("GROUP_PERMISSIONS").replace("<GROUP>", group.get().getName())
                .replace("<PERMISSIONS>", String.join(", ", groupPermissions));
    }

    public String handleListGroups(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_GROUP_LIST))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_LIST").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER"));

        Map<String, PermissionGroup> groups = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups();
        return language.getMessage("GROUP_LIST").replace("<GROUPS>", String.join(", ", groups.keySet()));
    }

    public String handleSetDefault(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_GROUP_DEFAULT))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_SET").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER"));

        DatabaseHandler handler = plugin.getPermissifyAPI().getDatabaseHandler().get();

        if (args.length < 1) {
            // If we only have one, show the default group.
            String defaultGroupName = handler.getDefaultGroup().map(group -> ChatColor.getByChar(group.getChatColor()) + group.getName())
                    .orElse(language.getMessage("EMPTY_DEFAULT_GROUP_NAME"));
            return language.getMessage("DEFAULT_GROUP_RESPONSE").replace("<GROUP>", defaultGroupName);
        }

        Optional<PermissionGroup> defaultGroup = handler.getGroup(args[0]);
        if (defaultGroup.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[0]);

        handler.setDefaultGroup(defaultGroup.get());
        if (handler.getDefaultGroup().filter(current -> current == defaultGroup.get()).isEmpty())
            return language.getMessage("UNABLE_TO_SET").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));
        return language.getMessage("DEFAULT_GROUP_SET").replace("<GROUP>", args[0]);
    }
}
