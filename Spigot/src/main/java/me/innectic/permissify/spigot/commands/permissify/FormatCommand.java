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
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.command.CommandSender;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class FormatCommand {

    public String handleSetFormat(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_FORMAT))
            return PermissifyConstants.INSUFFICIENT_PERMISSIONS;

        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_SET_FORMAT;
        if (args[0].equalsIgnoreCase("chat")) return handleSetChatFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
        else if (args[0].equalsIgnoreCase("whisper")) return handleWhisperFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
        else if (args[0].equalsIgnoreCase("enable")) return handleEnableFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
        else if (args[0].equalsIgnoreCase("disable")) return handleDisableFormat(sender, ArgumentUtil.getRemainingArgs(1, args));

        return PermissifyConstants.INVALID_ARGUMENT.replace("<ARGUMENT>", args[0]);
    }

    private String handleSetChatFormat(CommandSender sender, String[] args) {
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler");
        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_SET_FORMAT;
        String format = String.join(" ", args).trim();
        PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get().setChatFormat(format);
        return PermissifyConstants.FORMATTER_SET.replace("<FORMATTER>", "chat");
    }

    private String handleWhisperFormat(CommandSender sender, String[] args) {
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler");
        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGUMENTS_SET_FORMAT;
        String format = String.join(" ", args).trim();
        PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get().setWhisperFormat(format);
        return PermissifyConstants.FORMATTER_SET.replace("<FORMATTER>", "whisper");
    }

    private String handleDisableFormat(CommandSender sender, String[] args) {
        PermissifyMain.getInstance().getConfig().set("handleChat", false);
        PermissifyMain.getInstance().saveConfig();
        PermissifyMain.getInstance().setHandleChat(false);
        return PermissifyConstants.TOGGLED_CHAT_HANDLE.replace("<STATE>", "Disabled");
    }

    private String handleEnableFormat(CommandSender sender, String[] args) {
        PermissifyMain.getInstance().getConfig().set("handleChat", true);
        PermissifyMain.getInstance().saveConfig();
        PermissifyMain.getInstance().setHandleChat(true);
        return PermissifyConstants.TOGGLED_CHAT_HANDLE.replace("<STATE>", "Enabled");
    }
}
