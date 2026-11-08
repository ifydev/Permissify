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
package me.innectic.permissify.spigot.commands.subcommand;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.language.Language;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.profile.PermissifyProfile;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Innectic
 * @since 8/26/2017
 */
public class ProfileCommand {

    public String handleProfile(CommandSender sender, String[] args, Language language) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PROFILE))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");

        if (args.length < 1) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PROFILE");
        if (args[0].equalsIgnoreCase("save")) return handleSaveProfile(ArgumentUtil.getRemainingArgs(1, args), language);
        else if (args[0].equalsIgnoreCase("load")) return handleLoadProfile(ArgumentUtil.getRemainingArgs(1, args), language);
        return language.getMessage("INVALID_ARGUMENT").replace("<ARGUMENT>", args[0]);
    }

    private String handleSaveProfile(String[] args, Language language) {
        if (args.length < 1) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PROFILE_SAVE");
        if (PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_SET").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER"));

        boolean saved = saveProfile(args[0]);
        if (saved) return language.getMessage("PROFILE_SAVED").replace("<PROFILE>", args[0]);
        return language.getMessage("PROFILE_NOT_SAVED").replace("<PROFILE>", args[0]);
    }

    private String handleLoadProfile(String[] args, Language language) {
        if (args.length < 1) return language.getMessage("NOT_ENOUGH_ARGUMENTS_PROFILE_LOAD");
        if (PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isEmpty())
            return language.getMessage("UNABLE_TO_SET").replace("<REASON>", language.getMessage("NO_DATABASE_HANDLER"));
        Logger logger = PermissifyMain.getInstance().getPermissifyAPI().getLogger();

        boolean saved = saveProfile(args[0] + "-pre-load");
        if (!saved) return language.getMessage("PROFILE_NOT_SAVED").replace("<PROFILE>", args[0]);

        DatabaseHandler handler = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get();

        logger.info("Loading profile...");
        long originalStart = System.currentTimeMillis();
        String baseDir = PermissifyMain.getInstance().getDataFolder().getAbsolutePath();
        Optional<PermissifyProfile> profile = PermissifyMain.getInstance().getPermissifyAPI().getProfileSerializer().deserialize(args[0], baseDir);
        long end = System.currentTimeMillis();
        logger.info("Loaded profile in " + (end - originalStart) + " ms.");
        if (profile.isEmpty()) return language.getMessage("PROFILE_NOT_LOADED").replace("<PROFILE>", args[0]);

        long start = System.currentTimeMillis();
        try {
            handler.loadProfile(profile.get());
        } catch (RuntimeException e) {
            logger.log(java.util.logging.Level.SEVERE, "Unable to load profile", e);
            return language.getMessage("PROFILE_NOT_LOADED").replace("<PROFILE>", args[0]);
        }
        PermissifyMain.getInstance().getPlayerCommand().cancelExpirations();
        org.bukkit.Bukkit.getOnlinePlayers().forEach(PermissionUtil::applyPermissions);
        end = System.currentTimeMillis();
        logger.info("Parsed profile in " + (end - start) + " ms.");

        return language.getMessage("PROFILE_LOADED").replace("<PROFILE>", args[0])
                .replace("<TIME>", Long.toString(end - originalStart));
    }

    private boolean saveProfile(String name) {
        if (PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isEmpty()) return false;
        DatabaseHandler handler = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get();
        Logger logger = PermissifyMain.getInstance().getPermissifyAPI().getLogger();

        // Generate the profile
        logger.info("Generating profile...");
        long start = System.currentTimeMillis();
        PermissifyProfile profile = new PermissifyProfile(handler.getGroups(), handler.getAllPermissions(),
                handler.getDefaultGroup().isPresent() ? handler.getDefaultGroup().get() : null, handler.getSuperAdmins(),
                PermissifyConstants.PERMISSIFY_PROFILE_VERSION);
        long end = System.currentTimeMillis();
        logger.info("Generated profile in " + (end - start) + " ms.");

        // Save the profile to a file
        logger.info("Saving profile...");
        start = System.currentTimeMillis();
        String baseDir = PermissifyMain.getInstance().getDataFolder().getAbsolutePath();
        boolean saved = PermissifyMain.getInstance().getPermissifyAPI().getProfileSerializer().serialize(profile, baseDir, name);
        end = System.currentTimeMillis();
        logger.info("Serialized profile in " + (end - start) + " ms.");
        return saved;
    }
}
