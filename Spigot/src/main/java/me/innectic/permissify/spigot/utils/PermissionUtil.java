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
package me.innectic.permissify.spigot.utils;

import me.innectic.permissify.spigot.PermissifyMain;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.Optional;

/**
 * @author Innectic
 * @since 8/21/2017
 */
public class PermissionUtil {

    public static boolean isConsole(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender;
    }

    public static boolean hasPermissionOrSuperAdmin(CommandSender sender, String permission) {
        if (sender instanceof Player player) {
            return player.hasPermission(permission) ||
                    (PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().map(handler ->
                            handler.isSuperAdmin(player.getUniqueId())).orElse(false));
        }
        return sender instanceof BlockCommandSender || isConsole(sender);
    }

    public static void applyPermissions(Player player) {
        PermissifyMain plugin = PermissifyMain.getInstance();

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> applyPermissions(player));
            return;
        }
        if (!player.isOnline()) return;
        plugin.getAttachmentManager().resetAllPermissibles(player.getUniqueId());

        plugin.getAttachmentManager().setAttachment(player.getUniqueId(), player.addAttachment(plugin), Optional.empty());

        plugin.getPermissifyAPI().getDatabaseHandler().ifPresent(handler -> {
            // Check if the player should be in a default group.
            if (handler.getDefaultGroup().isPresent() && handler.getGroups(player.getUniqueId()).isEmpty()) {
                handler.addPlayerToGroup(player.getUniqueId(), handler.getDefaultGroup().get());
            }
            handler.updateCache(player.getUniqueId());
            PermissionAttachment attachment = plugin.getAttachmentManager()
                    .getAttachment(player.getUniqueId(), Optional.empty()).orElseThrow();
            handler.getMainGroup(player.getUniqueId()).ifPresent(group ->
                    attachment.setPermission("group." + group.getName(), true));
            handler.resolvePermissions(player.getUniqueId()).forEach((node, rule) ->
                    attachment.setPermission(node, rule.granted()));

        });
        player.recalculatePermissions();
        player.updateCommands();
    }
}
