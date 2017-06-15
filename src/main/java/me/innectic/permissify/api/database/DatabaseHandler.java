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
package me.innectic.permissify.api.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.innectic.permissify.api.permission.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Innectic
 * @since 6/8/2017
 *
 * The base database type.
 */
@RequiredArgsConstructor
public abstract class DatabaseHandler {

    /**
     * Cached permissions from the database.
     */
    @Getter protected Map<UUID, List<String>> cachedPermissions = new HashMap<>();
    protected final ConnectionInformation connectionInformation;

    /**
     * Display an error to the server console, and the online ops. TODO: This should be converted to sending the message to players with a certain permission. `permissify.admin`?
     *
     * @param error the type of error
     */
    protected void displayError(ConnectionError error) {
        // TODO: Do something with loggers here
    }

    /**
     * Display an error to the server console, and the online ops. TODO: This should be converted to sending the message to players with a certain permission. `permissify.admin`?
     *
     * @param error the type of error
     * @param e     the exception that happened
     */
    protected void displayError(ConnectionError error, Exception e) {
        // TODO: Do something with loggers here
    }

    /**
     * Initialize the database handler
     */
    public abstract void initialize();

    /**
     * Connect to the database
     *
     * @return if the connection was successful or not
     */
    public abstract boolean connect();

    /**
     * Add a permission to a player
     *
     * @param uuid        the UUID of the player to add the permissions to
     * @param permissions the permissions to add to a player
     */
    public abstract void addPermission(UUID uuid, Permission... permissions);

    /**
     * Remove permissions from a player
     *
     * @param uuid        the uuid of the player to remove the permissions from
     * @param permissions the permissions to remove
     */
    public abstract void removePermission(UUID uuid, Permission... permissions);

    /**
     * Does a player have a permission?
     *
     * @param uuid       the uuid of the player to check
     * @param permission the permission to check
     * @return if the player has the permission
     */
    public abstract boolean hasPermission(UUID uuid, Permission permission);
}
