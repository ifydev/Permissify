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
import me.innectic.permissify.api.profile.PermissifyProfile;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.commands.CommandResponse;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * @author Innectic
 * @since 8/26/2017
 */
public class ProfileCommand {

    public CommandResponse handleProfile(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PROFILE))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);

        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PROFILE, false);
        if (args[0].equalsIgnoreCase("save")) return handleSaveProfile(sender, ArgumentUtil.getRemainingArgs(1, args));
        else if (args[0].equalsIgnoreCase("load")) return handleLoadProfile(sender, ArgumentUtil.getRemainingArgs(1, args));
        return new CommandResponse(PermissifyConstants.INVALID_ARGUMENT.replace("<ARGUMENT>", args[0]), true);
    }

    private CommandResponse handleSaveProfile(CommandSender sender, String[] args) {
        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PROFILE_SAVE, false);
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler"), false);

        DatabaseHandler handler = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get();

        // Generate the profile
        System.out.println("Generating profile...");
        long start = System.currentTimeMillis();
        PermissifyProfile profile = new PermissifyProfile(handler.getGroups(), handler.getCachedPermissions(),
                handler.getDefaultGroup().isPresent() ? handler.getDefaultGroup().get() : null,
                handler.getChatFormat(false), handler.getWhisperFormat(false), handler.getSuperAdmins());
        long end = System.currentTimeMillis();
        System.out.println("Generated profile in " + (end - start) + " ms.");

        // Save the profile to a file
        System.out.println("Saving profile...");
        start = System.currentTimeMillis();
        String baseDir = PermissifyMain.getInstance().getDataFolder().getAbsolutePath();
        boolean saved = PermissifyMain.getInstance().getPermissifyAPI().getSerializer().serialize(profile, baseDir, args[0]);
        end = System.currentTimeMillis();
        System.out.println("Serialized profile in " + (end - start) + " ms.");

        if (saved) return new CommandResponse(PermissifyConstants.PROFILE_SAVED.replace("<PROFILE>", args[0]), true);
        return new CommandResponse(PermissifyConstants.PROFILE_NOT_SAVED.replace("<PROFILE>", args[0]), false);
    }

    private CommandResponse handleLoadProfile(CommandSender sender, String[] args) {
        if (args.length < 1) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_PROFILE_LOAD, false);
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler"), false);

        DatabaseHandler handler = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get();

        System.out.println("Loading profile...");
        long originalStart = System.currentTimeMillis();
        String baseDir = PermissifyMain.getInstance().getDataFolder().getAbsolutePath();
        Optional<PermissifyProfile> profile = PermissifyMain.getInstance().getPermissifyAPI().getSerializer().deserialize(args[0], baseDir);
        long end = System.currentTimeMillis();
        System.out.println("Loaded profile in " + (end - originalStart) + " ms.");
        if (!profile.isPresent()) return new CommandResponse(PermissifyConstants.PROFILE_NOT_LOADED.replace("<PROFILE>", args[0]), false);

        long start = System.currentTimeMillis();
        handler.drop();
        handler.loadProfile(profile.get());
        end = System.currentTimeMillis();
        System.out.println("Parsed profile in " + (end - start) + " ms.");

        return new CommandResponse(PermissifyConstants.PROFILE_LOADED.replace("<PROFILE>", args[0])
                .replace("<TIME>", Long.toString(end - originalStart)), true);
    }
}
