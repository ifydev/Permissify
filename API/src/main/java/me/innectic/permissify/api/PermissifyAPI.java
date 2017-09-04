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
package me.innectic.permissify.api;

import lombok.Getter;
import me.innectic.permissify.api.database.ConnectionInformation;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.database.handlers.HandlerType;
import me.innectic.permissify.api.module.registry.ModuleLoader;
import me.innectic.permissify.api.module.registry.ModuleProvider;
import me.innectic.permissify.api.profile.ProfileSerializer;
import me.innectic.permissify.api.util.DisplayUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Innectic
 * @since 6/8/2017
 */
public class PermissifyAPI {

    private static Optional<PermissifyAPI> instance;

    @Getter private Optional<DatabaseHandler> databaseHandler;
    @Getter private DisplayUtil displayUtil;
    @Getter private ProfileSerializer profileSerializer;
    @Getter private Logger logger;

    @Getter private ModuleProvider moduleProvider;
    @Getter private ModuleLoader moduleLoader;

    /**
     * Initialize Permissify's API
     */
    public void initialize(HandlerType type, Optional<ConnectionInformation> connectionInformation, DisplayUtil displayUtil, Logger logger, String moduleLocation) throws Exception {
        instance = Optional.of(this);
        this.logger = logger;
        this.displayUtil = displayUtil;
        profileSerializer = new ProfileSerializer();
        moduleProvider = new ModuleProvider();
        moduleLoader = new ModuleLoader(moduleLocation);

        try {
            databaseHandler = Optional.of(type.getHandler().getConstructor(ConnectionInformation.class).newInstance(connectionInformation.orElse(null)));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (!databaseHandler.isPresent()) throw new Exception("No data handler present.");
        databaseHandler.ifPresent(handler -> {
            handler.initialize();
            handler.reload(new ArrayList<>());
            boolean connected = handler.connect();
            if (connected) logger.info("Connected to the database.");
            else logger.log(Level.SEVERE, "Unable to connect to the database.");
        });
        logger.info("Registering modules...");
//        moduleProvider.registerModule(ChatModule.class, "permissify");
        moduleLoader.loadModules();
    }

    public static Optional<PermissifyAPI> get() {
        return instance;
    }
}
