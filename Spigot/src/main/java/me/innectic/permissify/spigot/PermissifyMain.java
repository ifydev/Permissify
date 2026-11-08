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

import me.innectic.permissify.spigot.commands.PermissifyCommand;
import me.innectic.permissify.spigot.commands.PermissifyTabCompleter;
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
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        permissifyAPI = new PermissifyAPI();
        createConfig();
        loadLanguage();
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
        if (playerCommand != null) playerCommand.cancelExpirations();
        if (permissifyAPI != null && permissifyAPI.getModuleProvider() != null) permissifyAPI.getModuleProvider().end(this);

        configVerifier = null;
        permissifyAPI = null;
    }

    private void loadLanguage() {
        var language = permissifyAPI.getLanguageManager();
        Path directory = getDataFolder().toPath().resolve("languages");
        try {
            Files.createDirectories(directory);
            Path english = directory.resolve("en_US.json");
            if (!Files.exists(english)) language.saveDefault(english);
            try (var files = Files.list(directory)) {
                for (Path file : files.filter(path -> path.getFileName().toString().endsWith(".json")).sorted().toList()) {
                    String name = file.getFileName().toString();
                    try {
                        language.load(name.substring(0, name.length() - 5), file);
                    } catch (Exception e) {
                        getLogger().log(Level.WARNING, "Unable to load language " + name, e);
                    }
                }
            }
            String name = getConfig().getString("language", "en_US");
            if (!name.matches("[a-zA-Z0-9_-]+")) throw new IllegalArgumentException("Invalid language name: " + name);
            language.load(directory.resolve(name + ".json"));
        } catch (Exception e) {
            language.useEnglish();
            getLogger().log(Level.WARNING, "Unable to load default language, using English", e);
        }
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
        getCommand("permissify").setTabCompleter(new PermissifyTabCompleter(this));
    }

    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new PlayerJoin(), this);
        pluginManager.registerEvents(new PlayerLeave(), this);
    }
}
