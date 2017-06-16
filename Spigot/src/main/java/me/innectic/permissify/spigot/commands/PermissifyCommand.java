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
            // TODO: Handle setting the groups of a player to super-admin
            return false;
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("")) {
                player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Permissify> " + ChatColor.RED + ChatColor.BOLD + "Insufficient permission!");
                return false;
            }
            if (args.length < 2) {
                helpMenu(sender);
                return false;
            }
            if (args[0].equals("group")) {
                if (args[0].equals("create")) {

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
        sender.sendMessage(ChatColor.YELLOW + "=============== " + ChatColor.GREEN + ChatColor.BOLD + "Permissify Help " + ChatColor.YELLOW + "===============");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group create [name] [prefix] [suffix] [chat-color]");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group addpermission [name] [permission]");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group removepermission [name] [permission]");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group listpermission [name]");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group list");
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "===============================================");
    }
}
