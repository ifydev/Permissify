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

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.commands.CommandResponse;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class CacheCommand {

    public CommandResponse handleCache(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_CACHE))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args.length >= 1 && args[0].equalsIgnoreCase("purge")) return handleCachePurge(sender, ArgumentUtil.getRemainingArgs(1, args));
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_OTHER.replace("<REASON>", "No database handler"), false);
        // Show information about the current cache
        DatabaseHandler handler = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get();
        String response = PermissifyConstants.CACHE_INFORMATION.replace("<GROUPS>", Integer.toString(handler.getCachedGroups().size()))
                .replace("<PERMISSIONS>", Integer.toString(handler.getCachedPermissions().size()))
                .replace("<DEFAULT>", handler.getDefaultGroup().map(PermissionGroup::getName).orElse(PermissifyConstants.EMPTY_DEFAULT_GROUP_NAME));
        return new CommandResponse(response, false);
    }

    private CommandResponse handleCachePurge(CommandSender sender, String[] args) {
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_OTHER.replace("<REASON>", "No database handler"), false);
        PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get().clear(Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toList()));
        Bukkit.getScheduler().runTaskAsynchronously(PermissifyMain.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(PermissionUtil::applyPermissions));
        return new CommandResponse(PermissifyConstants.CACHE_PURGED, true);
    }
}
