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
import me.innectic.permissify.api.util.HelpUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.commands.subcommands.SuperAdminSubCommand;
import me.innectic.permissify.spigot.utils.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

/**
 * @author Innectic
 * @since 6/15/2017
 */
public class PermissifyCommand implements CommandExecutor {

    private SuperAdminSubCommand superAdminSubCommand = new SuperAdminSubCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        PermissifyMain plugin = PermissifyMain.getInstance();

        // Handle no arguments
        if (args.length == 0) {
            Optional<List<String>> helpData = HelpUtil.getHelpInformationAtPage(0);
            if (!helpData.isPresent()) {
                sender.sendMessage(ColorUtil.makeReadable(PermissifyConstants.INVALID_HELP_PAGE_INDEX.replace("<PAGE>", "0")));
                return false;
            }
            helpData.get().stream().map(ColorUtil::makeReadable).forEach(sender::sendMessage);
            return true;
        }

        String section = args[0].toLowerCase();

        if (section.equals("superadmin")) sender.sendMessage(
                ColorUtil.makeReadable(superAdminSubCommand.handle(sender, ArgumentUtil.skipFirst(args))));
        else sendDefaultHelpInformation(sender);

        return false;
    }

    private void sendDefaultHelpInformation(CommandSender sender) {
        Optional<List<String>> helpData = HelpUtil.getHelpInformationAtPage(0);
        if (!helpData.isPresent()) {
            sender.sendMessage(ColorUtil.makeReadable(PermissifyConstants.INVALID_HELP_PAGE_INDEX.replace("<PAGE>", "0")));
            return;
        }
        helpData.get().stream().map(ColorUtil::makeReadable).forEach(sender::sendMessage);
    }
}