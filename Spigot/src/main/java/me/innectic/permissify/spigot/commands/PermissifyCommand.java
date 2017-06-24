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

            return false;
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("")) {
                player.sendMessage(PermissifyConstants.INSUFFICIENT_PERMISSIONS);
                return false;
            }
            if (args.length < 2) {
                helpMenu(sender);
                return false;
            }
            if (args[0].equalsIgnoreCase("group")) {
                if (args[0].equalsIgnoreCase("create")) {
                    return true;
                } else if (args[1].equalsIgnoreCase("addpermission")) {
                    return true;
                } else if (args[1].equalsIgnoreCase("removepermission")) {
                    return true;
                } else if (args[1].equalsIgnoreCase("listpermission")) {
                    return true;
                } else if (args[1].equalsIgnoreCase("list")) {
                    return true;
                }
                helpMenu(sender);
            }
        }
        return false;
    }

    private void helpMenu(CommandSender sender) {
        PermissifyConstants.PERMISSIFY_HELP.forEach(sender::sendMessage);
    }
}
