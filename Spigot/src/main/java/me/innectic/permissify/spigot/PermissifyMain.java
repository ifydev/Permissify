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
package me.innectic.permissify.spigot;

import lombok.Getter;
import lombok.Setter;
import me.innectic.permissify.spigot.commands.PermissifyCommand;
import me.innectic.permissify.spigot.commands.subcommand.*;
import me.innectic.permissify.spigot.events.PlayerJoin;
import me.innectic.permissify.api.PermissifyAPI;
import me.innectic.permissify.api.database.handlers.FullHandler;
import me.innectic.permissify.spigot.events.PlayerLeave;
import me.innectic.permissify.spigot.utils.AttachmentManager;
import me.innectic.permissify.spigot.utils.ConfigVerifier;
import me.innectic.permissify.spigot.utils.DisplayUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author Innectic
 * @since 6/14/2017
 */
public class PermissifyMain extends JavaPlugin {

    private ConfigVerifier configVerifier;
    @Getter private PermissifyAPI permissifyAPI;

    @Getter private GroupCommand groupCommand;
    @Getter private PlayerCommand playerCommand;
    @Getter private CacheCommand cacheCommand;
    @Getter private ProfileCommand profileCommand;

    @Getter private AttachmentManager attachmentManager;

    @Getter @Setter private boolean handleChat = false;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        permissifyAPI = new PermissifyAPI();
        createConfig();
        // Verify the config
        configVerifier = new ConfigVerifier();
        if (!configVerifier.verifyBasicInformation()) {
            getLogger().log(Level.SEVERE, ChatColor.RED + "Internal Permissify Error: Could not verify basic information!");
            return;
        }
        Optional<FullHandler> handler = configVerifier.verifyConnectionInformation();
        // Initialize the API
        if (handler.isEmpty() || handler.get().getHandlerType().isEmpty()) {
            getLogger().log(Level.SEVERE, ChatColor.RED + "Internal Permissify Error: No handler / type present!");
            return;
        }
        handleChat = getConfig().getBoolean("handleChat");
        try {
            permissifyAPI.initialize(handler.get().getHandlerType().get(), handler.get().getConnectionInformation(), new DisplayUtil(), getLogger(), getDataFolder().getAbsolutePath() + "/modules", this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Create the attachment manager
        attachmentManager = new AttachmentManager();
        // Register commands
        registerCommands();
        // Register listeners
        registerListeners();
        long timeTaken = System.currentTimeMillis() - start;

        getLogger().info("Permissify initialized in " + ((double) timeTaken / 1000) + " seconds (" + timeTaken + " ms)!");
    }

    @Override
    public void onDisable() {
        permissifyAPI.getModuleProvider().end(this);

        configVerifier = null;
        permissifyAPI = null;
    }

    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                boolean created = getDataFolder().mkdirs();
                if (!created) getLogger().log(Level.SEVERE, "Could not create config!");
                getDataFolder().toPath().resolve("profiles").toFile().mkdir();
            }

            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else getLogger().info("Config.yml found, loading!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PermissifyMain getInstance() {
        return PermissifyMain.getPlugin(PermissifyMain.class);
    }

    private void registerCommands() {
        groupCommand = new GroupCommand();
        playerCommand = new PlayerCommand();
        cacheCommand = new CacheCommand();
        profileCommand = new ProfileCommand();

        getCommand("permissify").setExecutor(new PermissifyCommand());
    }

    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new PlayerJoin(), this);
        pluginManager.registerEvents(new PlayerLeave(), this);
    }
}
