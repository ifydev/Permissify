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
import me.innectic.permissify.api.PermissifyAPI;
import me.innectic.permissify.api.database.handlers.FullHandler;
import me.innectic.permissify.spigot.commands.PermissifyCommand;
import me.innectic.permissify.spigot.events.PlayerJoin;
import me.innectic.permissify.spigot.events.PlayerLeave;
import me.innectic.permissify.spigot.utils.AttachmentManager;
import me.innectic.permissify.spigot.utils.ConfigVerifier;
import me.innectic.permissify.spigot.utils.DisplayUtil;
import me.innectic.permissify.spigot.utils.PermissibleUtil;
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

    @Getter @Setter private boolean useWildcards;
    @Getter @Setter private boolean debugMode;
    @Getter private AttachmentManager attachmentManager;

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
        this.useWildcards = !getConfig().getBoolean("disable-wildcard-permissions", false);
        getLogger().info(useWildcards ? "Using wildcards!" : "Not using wildcards!");

        Optional<FullHandler> handler = configVerifier.verifyConnectionInformation();
        // Initialize the API
        if (!handler.isPresent() || !handler.get().getHandlerType().isPresent()) {
            getLogger().log(Level.SEVERE, ChatColor.RED + "Internal Permissify Error: No handler / type present!");
            return;
        }

        try {
            permissifyAPI.initialize(handler.get().getHandlerType().get(), handler.get().getConnectionInformation(), new DisplayUtil(), getLogger());
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

        Bukkit.getOnlinePlayers().forEach(PermissibleUtil::injectPermissible);
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(PermissibleUtil::uninjectPermissible);

        configVerifier = null;
        permissifyAPI = null;
    }

    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                boolean created = getDataFolder().mkdirs();
                if (!created) getLogger().log(Level.SEVERE, "Could not create config!");
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static PermissifyMain getInstance() {
        return PermissifyMain.getPlugin(PermissifyMain.class);
    }

    private void registerCommands() {
        getCommand("permissify").setExecutor(new PermissifyCommand());
    }

    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new PlayerJoin(), this);
        pluginManager.registerEvents(new PlayerLeave(), this);
    }
}
