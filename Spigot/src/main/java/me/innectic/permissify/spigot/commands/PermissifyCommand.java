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
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.ColorUtil;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Innectic
 * @since 6/15/2017
 */
public class PermissifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent()) {
                sendResponse(PermissifyConstants.UNABLE_OTHER.replace("<REASON>", "No database handler"), sender);
                return;
            }
            if (sender instanceof ConsoleCommandSender) {
                if (args.length < 2 || (args.length >= 2 && !args[0].equalsIgnoreCase("superadmin"))) {
                    sendResponse(PermissifyConstants.CONSOLE_INVALID_COMMAND, sender);
                    return;
                }
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) return;
                plugin.getPermissifyAPI().getDatabaseHandler().get().addSuperAdmin(player.getUniqueId());
            } else if (sender instanceof Player) {
                Player player = (Player) sender;
                if (!PermissionUtil.hasPermissionOrSuperAdmin(player, PermissifyConstants.PERMISSIFY_BASIC)) {
                    player.sendMessage(ColorUtil.makeReadable(PermissifyConstants.INSUFFICIENT_PERMISSIONS));
                }
                if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
                    int page = 0;
                    if (args.length >= 2) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (NumberFormatException ignored) {}
                    }
                    page -= 1;
                    if (page < 0) page = 0;
                    sendHelp(player, page);
                    return;
                } else if (args.length >= 1 && args[0].equalsIgnoreCase("cache")) {
                    CommandResponse response = plugin.getCacheCommand().handleCache(sender, ArgumentUtil.getRemainingArgs(1, args));
                    sendResponse(response, player);
                    return;
                }
                if (args.length < 2) {
                    sendHelp(player);
                    return;
                }
                if (args[0].equalsIgnoreCase("group")) {
                    CommandResponse response;
                    if (args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("add"))
                        response = plugin.getGroupCommand().handleAddGroup(player, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("delete"))
                        response = plugin.getGroupCommand().handleDeleteGroup(player, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("addpermission"))
                        response = plugin.getGroupCommand().handlePermissionAdd(player, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("removepermission"))
                        response = plugin.getGroupCommand().handlePermissionRemove(player, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("list"))
                        response = plugin.getGroupCommand().handleListGroups(player, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("listpermissions"))
                        response = plugin.getGroupCommand().handleListPermissions(player, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("default"))
                        response = plugin.getGroupCommand().handleSetDefault(player, ArgumentUtil.getRemainingArgs(2, args));
                    else {
                        sendHelp(player);
                        return;
                    }
                    sendResponse(response, sender);
                } else if (args[0].equalsIgnoreCase("player")) {
                    CommandResponse response;
                    if (args.length < 3) {
                        sendResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PLAYER, player);
                        return;
                    }
                    if (args[1].equalsIgnoreCase("addpermission"))
                        response = plugin.getPlayerCommand().handleAddPermission(sender, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("removepermission"))
                        response = plugin.getPlayerCommand().handleRemovePermission(sender, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("addgroup"))
                        response = plugin.getPlayerCommand().handleAddPlayerToGroup(sender, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("listpermissions"))
                        response = plugin.getPlayerCommand().handleListPermissions(sender, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("listgroups"))
                        response = plugin.getPlayerCommand().handleListGroups(sender, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("removegroup")) response = plugin.getPlayerCommand().handleRemovePlayerFromGroup(sender, ArgumentUtil.getRemainingArgs(2, args));
                    else if (args[1].equalsIgnoreCase("setmain")) response = plugin.getPlayerCommand().handleSetMainGroup(sender, ArgumentUtil.getRemainingArgs(2, args));
                    else {
                        sendHelp(player);
                        return;
                    }
                    sendResponse(response, player);
                } else if (args[0].equalsIgnoreCase("format")) {
                    CommandResponse response = plugin.getFormatCommand().handleSetFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
                    sender.sendMessage(ColorUtil.makeReadable(response.getResponse()));
                }
            }
        });
        return false;
    }

    private void sendResponse(CommandResponse response, CommandSender source) {
        sendResponse(response.getResponse(), source);
    }

    private void sendResponse(List<String> responses, CommandSender source) {
        responses.forEach(response -> sendResponse(response, source));
    }

    private void sendResponse(String response, CommandSender source) {
        source.sendMessage(ColorUtil.makeReadable(response));  // XXX: Probably don't need ColorUtil anymore...
    }

    private void sendHelp(CommandSender player) {
        sendHelp(player, 0);
    }

    private void sendHelp(CommandSender player, int page) {
        sendResponse(PermissifyConstants.PERMISSIFY_HELP_HEADER, player);
        sendResponse(PermissifyConstants.PERMISSIFY_HELP_PAGES.get(page), player);
        sendResponse(PermissifyConstants.PERMISSIFY_HELP_FOOTER, player);
    }
}
