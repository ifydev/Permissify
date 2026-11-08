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
import me.innectic.permissify.api.language.Language;
import me.innectic.permissify.spigot.utils.LanguageUtil;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.ColorUtil;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.OfflinePlayer;
import me.innectic.permissify.spigot.utils.PlayerUtil;

import java.util.List;

/**
 * @author Innectic
 * @since 6/15/2017
 */
public class PermissifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        Language language = LanguageUtil.getLanguage(plugin.getPermissifyAPI().getLanguageManager(), sender);
        try {
            execute(sender, args, language);
        } catch (RuntimeException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Unable to run /permissify", e);
            sendResponse(language.getMessage("COMMAND_FAILED"), sender);
        }
        return true;
    }

    private void execute(CommandSender sender, String[] args, Language language) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (sender instanceof RemoteConsoleCommandSender && !plugin.getConfig().getBoolean("allow-rcon", false)) {
            sendResponse(language.getMessage("RCON_DISABLED"), sender);
            return;
        }
        if (plugin.getPermissifyAPI().getDatabaseHandler().isEmpty()) {
            sendResponse(language.getMessage("UNABLE_OTHER").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER")), sender);
            return;
        }
        if (PermissionUtil.isConsole(sender)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("superadmin")) {
                if (args.length < 3) {
                    sendResponse(language.getMessage("NOT_ENOUGH_ARGUMENTS_SUPERADMIN"), sender);
                    return;
                }
                OfflinePlayer player = PlayerUtil.findPlayer(args[2]);
                if (player == null) {
                    sendResponse(language.getMessage("INVALID_PLAYER"), sender);
                    return;
                }

                if (args[1].equalsIgnoreCase("grant")) {
                    plugin.getPermissifyAPI().getDatabaseHandler().get().addSuperAdmin(player.getUniqueId());
                    if (!plugin.getPermissifyAPI().getDatabaseHandler().get().isSuperAdmin(player.getUniqueId())) {
                        sendResponse(language.getMessage("UNABLE_OTHER").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE")), sender);
                        return;
                    }
                    sendResponse(language.getMessage("SUPERADMIN_GRANTED").replace("<PLAYER>", player.getName()), sender);
                    return;
                } else if (args[1].equalsIgnoreCase("remove")) {
                    plugin.getPermissifyAPI().getDatabaseHandler().get().removeSuperAdmin(player.getUniqueId());
                    if (plugin.getPermissifyAPI().getDatabaseHandler().get().isSuperAdmin(player.getUniqueId())) {
                        sendResponse(language.getMessage("UNABLE_OTHER").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE")), sender);
                        return;
                    }
                    sendResponse(language.getMessage("SUPERADMIN_REMOVED").replace("<PLAYER>", player.getName()), sender);
                    return;
                } else {
                    sendResponse(language.getMessage("INVALID_ARGUMENT").replace("<ARGUMENT>", args[1]), sender);
                    return;
                }
            }
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("superadmin")) {
            sendResponse(language.getMessage("INSUFFICIENT_PERMISSIONS"), sender);
            return;
        }
        if (sender instanceof BlockCommandSender && !plugin.getConfig().getBoolean("allow-command-block", false)) return;

        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_BASIC)) {
            sendResponse(language.getMessage("INSUFFICIENT_PERMISSIONS"), sender);
            return;
        }

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                int page = 0;
                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sendResponse(language.getMessage("INVALID_ARGUMENT").replace("<ARGUMENT>", args[1]), sender);
                        return;
                    }
                }
                page = page <= 1 ? 0 : page - 1;
                if (page < 0) page = 0;
                sendHelp(sender, page, language);
                return;
            } else if (args[0].equalsIgnoreCase("cache")) {
                String response = plugin.getCacheCommand().handleCache(sender, ArgumentUtil.getRemainingArgs(1, args), language);
                sendResponse(response, sender);
                return;
            }
        }

        if (args.length < 2) {
            sendHelp(sender, language);
            return;
        }
        if (args[0].equalsIgnoreCase("group")) {
            String response;
            if (args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("add"))
                response = plugin.getGroupCommand().handleAddGroup(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("inherit"))
                response = plugin.getGroupCommand().handleInherit(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("delete"))
                response = plugin.getGroupCommand().handleDeleteGroup(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("addpermission"))
                response = plugin.getGroupCommand().handlePermissionAdd(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("removepermission"))
                response = plugin.getGroupCommand().handlePermissionRemove(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("list"))
                response = plugin.getGroupCommand().handleListGroups(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("listpermissions"))
                response = plugin.getGroupCommand().handleListPermissions(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("default"))
                response = plugin.getGroupCommand().handleSetDefault(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else {
                sendHelp(sender, language);
                return;
            }
            sendResponse(response, sender);
        } else if (args[0].equalsIgnoreCase("player")) {
            String response;
            if (args[1].equalsIgnoreCase("addpermission"))
                response = plugin.getPlayerCommand().handleAddPermission(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("removepermission"))
                response = plugin.getPlayerCommand().handleRemovePermission(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("addgroup"))
                response = plugin.getPlayerCommand().handleAddPlayerToGroup(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("listpermissions"))
                response = plugin.getPlayerCommand().handleListPermissions(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if ((args[1].equalsIgnoreCase("listgroups") || args[1].equalsIgnoreCase("listgroup")))
                response = plugin.getPlayerCommand().handleListGroups(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("removegroup")) response = plugin.getPlayerCommand().handleRemovePlayerFromGroup(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else if (args[1].equalsIgnoreCase("primarygroup")) response = plugin.getPlayerCommand().handleSetOrGetPrimaryGroup(sender, ArgumentUtil.getRemainingArgs(2, args), language);
            else {
                sendHelp(sender, language);
                return;
            }
            sendResponse(response, sender);
        } else if (args[0].equalsIgnoreCase("profile")) {
            String response = plugin.getProfileCommand().handleProfile(sender, ArgumentUtil.getRemainingArgs(1, args), language);
            sendResponse(response, sender);
        } else sendHelp(sender, language);
    }

    private void sendResponse(List<String> responses, CommandSender source) {
        responses.forEach(response -> sendResponse(response, source));
    }

    private void sendResponse(String response, CommandSender source) {
        if (response.equals("")) return;
        source.sendMessage(ColorUtil.makeReadable(response));
    }

    private void sendHelp(CommandSender player, Language language) {
        sendHelp(player, 0, language);
    }

    private void sendHelp(CommandSender player, int page, Language language) {
        sendResponse(language.getMessage("PERMISSIFY_HELP_HEADER"), player);
        List<List<String>> pages = PermissifyHelp.getPages(player, language);
        sendResponse(pages.get(Math.max(0, Math.min(page, pages.size() - 1))), player);
        sendResponse(language.getMessage("PERMISSIFY_HELP_FOOTER"), player);
    }
}
