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

import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.spigot.PermissifyMain;
import org.bukkit.Bukkit;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Innectic
 * @since 8/21/2017
 */
public class PermissionUtil {

    public static boolean hasPermissionOrSuperAdmin(CommandSender sender, String permission) {
        if (sender instanceof Player) return sender.hasPermission(permission);
        return sender instanceof CommandBlock || sender instanceof ConsoleCommandSender;
    }

    public static void applyPermissions(Player player) {
        player.getEffectivePermissions().clear();

        PermissifyMain plugin = PermissifyMain.getInstance();
        plugin.getPermissifyAPI().getDatabaseHandler().ifPresent(handler -> {
            // Check if the player should be in a default group.
            if (handler.getDefaultGroup().isPresent() && !handler.getDefaultGroup().get().hasPlayer(player.getUniqueId())) {
                handler.addPlayerToGroup(player.getUniqueId(), handler.getDefaultGroup().get());
                handler.setPrimaryGroup(handler.getDefaultGroup().get(), player.getUniqueId());
            }
            handler.updateCache(player.getUniqueId());

            // Add the player's "self" permissions
            plugin.getAttachmentManager().getAttachment(player.getUniqueId(), Optional.empty()).ifPresent(self ->
                    handler.getPermissions(player.getUniqueId()).forEach(permission -> self.setPermission(permission.getPermission(), permission.isGranted())));

            // Add the player's group permissions
            handler.getGroups(player.getUniqueId()).forEach(group -> {
                PermissionAttachment attachment = plugin.getAttachmentManager().getAttachment(player.getUniqueId(), Optional.of(group.getName())).orElse(player.addAttachment(plugin));
                attachment.getPermissions().keySet().forEach(attachment::unsetPermission);

                group.getPermissions().forEach(permission -> attachment.setPermission(permission.getPermission(), permission.isGranted()));
                plugin.getAttachmentManager().setAttachment(player.getUniqueId(), attachment, Optional.of(group.getName()));
            });
        });
    }

    public static void clearPermissions(Player player) {
        player.getEffectivePermissions().stream().filter(Objects::nonNull).map(PermissionAttachmentInfo::getAttachment).filter(Objects::nonNull).forEach(player::removeAttachment);
        player.recalculatePermissions();
    }

    public static void updateGroupPermissions(PermissionGroup group) {
        group.getPlayers().keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(PermissionUtil::applyPermissions);
    }
}
