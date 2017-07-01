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
package me.innectic.permissify.sponge;

import com.google.inject.Inject;
import me.innectic.permissify.sponge.config.PermissifyConfig;
import me.innectic.permissify.sponge.events.PlayerJoinEvent;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@Plugin(id = "permissify", name = "Permissify", version = "1.0", authors = "Innectic")
public class PermissifyMain {
    @Inject @lombok.Getter private Logger logger;

    @Inject @ConfigDir(sharedRoot = false) @lombok.Getter private Path configurationDirectory;
    @Inject @DefaultConfig(sharedRoot = false) @lombok.Getter private Path defaultConfiguration;
    @Inject @ConfigDir(sharedRoot = false) @lombok.Getter private File defaultConfigurationFile;
    private PermissifyConfig config;

    @Listener
    public void onServerPreInit(GamePreInitializationEvent event) {
        Sponge.getEventManager().registerListeners(this, new PlayerJoinEvent());
    }

    public static Optional<PermissifyMain> getPlugin() {
        // Epic one-liner ahead.
        return Sponge.getPluginManager().getPlugin("permissify").filter(pluginContainer ->
                pluginContainer.getInstance().isPresent()).filter(Objects::nonNull)
                .filter(PermissifyMain.class::isInstance).map(PermissifyMain.class::cast);
    }

    /**
     * Ensures the configuration exists, and loads all values.
     */
    private void setupConfiguration() {
        config = new PermissifyConfig();
    }
}
