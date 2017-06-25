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
package me.innectic.permissify.spigot.commands;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * @author Innectic
 * @since 6/15/2017
 */
public class PermissifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            if (args.length < 2 || (args.length >= 2 && !args[0].equalsIgnoreCase("superadmin"))) {
                sender.sendMessage(PermissifyConstants.CONSOLE_INVALID_COMMAND);
                return false;
            }
            PermissifyMain plugin = PermissifyMain.getInstance();
            if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent()) return false;
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) return false;
            plugin.getPermissifyAPI().getDatabaseHandler().get().setSuperAdmin(player.getUniqueId());
            sender.sendMessage("DOne: " + player.getName());

            return false;
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission(PermissifyConstants.PERMISSIFY_BASIC)) {
                player.sendMessage(PermissifyConstants.INSUFFICIENT_PERMISSIONS);
                return false;
            }
            if (args.length < 2) {
                PermissifyConstants.PERMISSIFY_HELP.forEach(sender::sendMessage);
                return false;
            }
            PermissifyMain plugin = PermissifyMain.getInstance();
            if (args[0].equalsIgnoreCase("group")) {
                if (args[1].equalsIgnoreCase("create")) {
                    CommandResponse response = plugin.getGroupCommand().handleAddGroup(sender, ArgumentUtil.getRemainingArgs(2, args));
                    sender.sendMessage(response.getResponse());
                    return response.isSucceeded();
                } else if (args[1].equalsIgnoreCase("remove")) {
                    CommandResponse response = plugin.getGroupCommand().handleDeleteGroup(sender, ArgumentUtil.getRemainingArgs(2, args));
                    sender.sendMessage(response.getResponse());
                    return response.isSucceeded();
                } else if (args[1].equalsIgnoreCase("addpermission")) {
                    CommandResponse response = plugin.getGroupCommand().handlePermissionAdd(sender, ArgumentUtil.getRemainingArgs(2, args));
                    sender.sendMessage(response.getResponse());
                    return response.isSucceeded();
                } else if (args[1].equalsIgnoreCase("removepermission")) {
                    CommandResponse response = plugin.getGroupCommand().handlePermissionRemove(sender, ArgumentUtil.getRemainingArgs(2, args));
                    sender.sendMessage(response.getResponse());
                    return response.isSucceeded();
                } else if (args[1].equalsIgnoreCase("list")) {
                    CommandResponse response = plugin.getGroupCommand().handleListGroups(sender, ArgumentUtil.getRemainingArgs(2, args));
                    sender.sendMessage(response.getResponse());
                    return response.isSucceeded();
                } else if (args[1].equalsIgnoreCase("listpermissions")) {
                    CommandResponse response = plugin.getGroupCommand().handleListPermissions(sender, ArgumentUtil.getRemainingArgs(2, args));
                    sender.sendMessage(response.getResponse());
                    return response.isSucceeded();
                }
                PermissifyConstants.PERMISSIFY_HELP.forEach(sender::sendMessage);
            }
        }
        return false;
    }
}
