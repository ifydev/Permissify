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
import me.innectic.permissify.spigot.commands.CommandResponse;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class FormatCommand {

    public CommandResponse handleSetFormat(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_FORMAT))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_SET_FORMAT, false);
        if (args[0].equalsIgnoreCase("chat")) return handleSetChatFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
        else if (args[0].equalsIgnoreCase("whisper")) return handleWhisperFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
        else if (args[0].equalsIgnoreCase("enable")) return handleEnableFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
        else if (args[0].equalsIgnoreCase("disable")) return handleDisableFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
        return new CommandResponse(PermissifyConstants.INVALID_ARGUMENT.replace("<ARGUMENT>", args[0]), true);
    }

    private CommandResponse handleSetChatFormat(CommandSender sender, String[] args) {
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler"), false);
        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_SET_FORMAT, false);
        String format = String.join(" ", args).trim();
        PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get().setChatFormat(format);
        return new CommandResponse(PermissifyConstants.FORMATTER_SET.replace("<FORMATTER>", "chat"), true);
    }

    private CommandResponse handleWhisperFormat(CommandSender sender, String[] args) {
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler"), false);
        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_SET_FORMAT, false);
        String format = String.join(" ", args).trim();
        PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get().setWhisperFormat(format);
        return new CommandResponse(PermissifyConstants.FORMATTER_SET.replace("<FORMATTER>", "whisper"), true);
    }

    private CommandResponse handleDisableFormat(CommandSender sender, String[] args) {
        PermissifyMain.getInstance().getConfig().set("handleChat", false);
        PermissifyMain.getInstance().saveConfig();
        PermissifyMain.getInstance().setHandleChat(false);
        return new CommandResponse(PermissifyConstants.TOGGLED_CHAT_HANDLE.replace("<STATE>", "Disabled"), true);
    }

    private CommandResponse handleEnableFormat(CommandSender sender, String[] args) {
        PermissifyMain.getInstance().getConfig().set("handleChat", true);
        PermissifyMain.getInstance().saveConfig();
        PermissifyMain.getInstance().setHandleChat(true);
        return new CommandResponse(PermissifyConstants.TOGGLED_CHAT_HANDLE.replace("<STATE>", "Enabled"), true);
    }
}
