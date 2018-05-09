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
package me.innectic.permissify.spigot.utils;

import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.api.database.ConnectionInformation;
import me.innectic.permissify.api.database.handlers.FullHandler;
import me.innectic.permissify.api.database.handlers.HandlerType;
import me.innectic.permissify.api.database.handlers.SQLHandler;
import me.innectic.permissify.api.util.VerifyConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Innectic
 * @since 6/14/2017
 */
public class ConfigVerifier implements VerifyConfig {
    @Override
    public boolean verifyBasicInformation() {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin.getConfig() == null) return false;
        if (plugin.getConfig().getString("storage") == null) return false;

        Optional<HandlerType> type = HandlerType.findType(plugin.getConfig().getString("storage"));
        return type.isPresent();
    }

    @Override
    public Optional<FullHandler> verifyConnectionInformation() {
        PermissifyMain plugin = PermissifyMain.getInstance();
        Optional<HandlerType> type = HandlerType.findType(plugin.getConfig().getString("storage", "sqlite"));
        if  (!type.isPresent()) return Optional.empty();

        Optional<ConnectionInformation> connectionInformation = Optional.empty();
        if (type.get().getHandler() == SQLHandler.class) {
            if (type.get().getDisplayName().equalsIgnoreCase("mysql")) {
                if (plugin.getConfig().getString("connection.host") == null) return Optional.empty();
                if (plugin.getConfig().getString("connection.database") == null) return Optional.empty();
                if (plugin.getConfig().getString("connection.port") == null) return Optional.empty();
                if (plugin.getConfig().getString("connection.username") == null) return Optional.empty();
                if (plugin.getConfig().getString("connection.password") == null) return Optional.empty();

                connectionInformation = Optional.of(new ConnectionInformation(
                        plugin.getConfig().getString("connection.host"),
                        plugin.getConfig().getString("connection.database"),
                        plugin.getConfig().getInt("connection.port"),
                        plugin.getConfig().getString("connection.username"),
                        plugin.getConfig().getString("connection.password"),
                        new HashMap<>())
                );
            } else if (type.get().getDisplayName().equalsIgnoreCase("sqlite")) {
                if (plugin.getConfig().getString("connection.file") == null) return Optional.empty();

                Map<String, Object> sqliteMeta = new HashMap<>();
                sqliteMeta.put("file", plugin.getDataFolder() + "/" + plugin.getConfig().getString("connection.file") + ".db");
                Map<String, Object> meta = new HashMap<>();
                meta.put("sqlite", sqliteMeta);

                connectionInformation = Optional.of(new ConnectionInformation("", "", 0, "", "", meta));
            }
        }
        return Optional.of(new FullHandler(type, connectionInformation));
    }
}
