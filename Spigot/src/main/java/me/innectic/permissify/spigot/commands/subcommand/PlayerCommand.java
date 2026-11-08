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

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.language.Language;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.util.Tristate;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.PlayerUtil;
import me.innectic.permissify.spigot.utils.DurationUtil;
import me.innectic.permissify.api.permission.RankChange;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 6/25/2017
 */
public class PlayerCommand {

    public String handleAddPlayerToGroup(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();

        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_GROUP_ADD))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (args.length < 2 || args.length > 3) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_GROUP");

        OfflinePlayer targetPlayer = PlayerUtil.findPlayer(args[0]);
        if (targetPlayer == null) return language.getMessage("INVALID_PLAYER");

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(args[1]);
        if (group.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[1]);

        long expiry = 0;
        if (args.length == 3) {
            try {
                expiry = DurationUtil.expiresAt(args[2]);
            } catch (IllegalArgumentException | ArithmeticException e) {
                return language.getMessage("INVALID_ARGUMENT").replace("<ARGUMENT>", args[2]);
            }
        }
        Tristate added = plugin.getPermissifyAPI().getDatabaseHandler().get().addPlayerToGroup(targetPlayer.getUniqueId(), group.get(), expiry);
        if (added == Tristate.NONE)
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("PLAYER_ALREADY_IN_GROUP"));
        else if (added == Tristate.FALSE)
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));

        plugin.getPermissifyAPI().getDatabaseHandler().get().updateCache(targetPlayer.getUniqueId());

        if (targetPlayer.isOnline()) PermissionUtil.applyPermissions(targetPlayer.getPlayer());

        if (expiry > 0) return language.getMessage("PLAYER_ADDED_TO_GROUP_TIMED")
                .replace("<PLAYER>", targetPlayer.getName()).replace("<GROUP>", group.get().getName()).replace("<SECONDS>", args[2]);
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

    public String handleAddPermission(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER_PERIOD"));

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_ADD))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (args.length < 2 || args.length > 3) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_PERMISSION");

        OfflinePlayer targetPlayer = PlayerUtil.findPlayer(args[0]);
        if (targetPlayer == null) return language.getMessage("INVALID_PLAYER");

        if (plugin.getPermissifyAPI().getDatabaseHandler().get().hasPermission(targetPlayer.getUniqueId(), args[1])) {
            // Player already has this permission.
            return language.getMessage("PLAYER_ALREADY_HAS_PERMISSION").replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]);
        }
        long expiry = 0;
        if (args.length == 3) {
            try {
                expiry = DurationUtil.expiresAt(args[2]);
            } catch (IllegalArgumentException | ArithmeticException e) {
                return language.getMessage("INVALID_ARGUMENT").replace("<ARGUMENT>", args[2]);
            }
        }
        var handler = plugin.getPermissifyAPI().getDatabaseHandler().get();
        if (expiry > 0) {
            if (handler.addTemporaryPermission(targetPlayer.getUniqueId(), args[1], expiry) != Tristate.TRUE)
                return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));
        } else handler.addPermission(targetPlayer.getUniqueId(), args[1]);
        if (!handler.hasPermission(targetPlayer.getUniqueId(), args[1]))
            return language.getMessage("UNABLE_TO_ADD").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));
        if (targetPlayer.isOnline()) PermissionUtil.applyPermissions(targetPlayer.getPlayer());
        if (expiry > 0) return language.getMessage("PERMISSION_ADDED_PLAYER_TIMED")
                .replace("<PLAYER>", targetPlayer.getName()).replace("<PERMISSION>", args[1]).replace("<SECONDS>", args[2]);
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

        List<String> permissions = plugin.getPermissifyAPI().getDatabaseHandler().get().resolvePermissions(targetPlayer.getUniqueId())
                .values().stream().map(rule -> (rule.granted() ? "" : "-") + rule.permission()).toList();
        return language.getMessage("GROUP_PERMISSIONS").replace("<GROUP>", targetPlayer.getName())
                .replace("<PERMISSIONS>", String.join(", ", permissions.stream().distinct().toList()));
    }
    public String handleRankMove(CommandSender sender, String[] args, Language language, boolean promote) {
        String permission = promote ? PermissifyConstants.PERMISSIFY_PLAYER_PROMOTE : PermissifyConstants.PERMISSIFY_PLAYER_DEMOTE;
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, permission)) return language.getMessage("INSUFFICIENT_PERMISSIONS");
        if (args.length != 2) return language.getMessage(promote ? "USAGE_PLAYER_PROMOTE" : "USAGE_PLAYER_DEMOTE");
        OfflinePlayer player = PlayerUtil.findPlayer(args[0]);
        if (player == null) return language.getMessage("INVALID_PLAYER");
        RankChange change = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().orElseThrow()
                .moveOnTrack(player.getUniqueId(), args[1], promote);
        if (change.status() != RankChange.Status.SUCCESS)
            return language.getMessage("TRACK_MOVE_" + change.status().name()).replace("<TRACK>", args[1]).replace("<PLAYER>", player.getName());
        if (player.isOnline()) PermissionUtil.applyPermissions(player.getPlayer());
        return language.getMessage("TRACK_MOVE_SUCCESS").replace("<PLAYER>", player.getName()).replace("<TRACK>", args[1])
                .replace("<FROM>", change.from() == null ? language.getMessage("EMPTY_DEFAULT_GROUP_NAME") : change.from()).replace("<TO>", change.to());
    }
}