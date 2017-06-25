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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Innectic
 * @since 6/16/2017
 *
 * Constant attributes between the different plugins.
 */
public class PermissifyConstants {
    // TODO: Create a language formatter to allow for translating.

    // Chat messages
    private static final String PERMISSIFY_PREFIX = "&a&lPermissify> ";

    // Permissions
    public static final String PERMISSIFY_BASIC = "permissify.basic";
    public static final String PERMISSIFY_GROUP_CREATE = "permissify.group.create";
    public static final String PERMISSIFY_GROUP_REMOVE = "permissify.group.create";
    public static final String PERMISSIFY_GROUP_LIST = "permissify.group.list";
    public static final String PERMISSIFY_GROUP_PERMISSION_REMOVE = "permissify.group.permission.remove";
    public static final String PERMISSIFY_GROUP_PERMISSION_ADD = "permissify.group.permission.add";
    public static final String PERMISSIFY_GROUP_PERMISSION_LIST = "permissify.group.permission.list";

    public static final String INSUFFICIENT_PERMISSIONS = PERMISSIFY_PREFIX + "&c&lInsufficient permissions!";

    public static final String GROUP_CREATED = PERMISSIFY_PREFIX + "&c&lGroup <GROUP> has been created!";
    public static final String GROUP_REMOVED = PERMISSIFY_PREFIX + "&c&lGroup <GROUP> has been removed!";
    public static final String GROUP_EDITED = PERMISSIFY_PREFIX + "&c&lGroup <GROUP> has been edited!";
    public static final String GROUP_PERMISSIONS = PERMISSIFY_PREFIX + "&c&lPermissions for <GROUP>: <PERMISSIONS>";
    public static final String GROUP_LIST = PERMISSIFY_PREFIX + "&c&lRegistered groups: <GROUPS>";

    public static final String PERMISSION_ADDED_GROUP = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been added to <GROUP>!";
    public static final String PERMISSION_REMOVED_GROUP = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been removed from <GROUP>!";
    public static final String PERMISSION_ADDED_PLAYER = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been added to <PLAYER>!";
    public static final String PERMISSION_REMOVED_PLAYER = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been removed from <PLAYER>!";

    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_ADD = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group create [name] [prefix] [suffix] [chat-color]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_REMOVE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group remove [name]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_ADD = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group addpermission [name] [permission]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_REMOVE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group removepermission [name] [permission]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_LIST = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group listpermissions [group]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player [player] [group|listpermissions|listgroups|addpermission|removepermission]";

    public static final String INVALID_CHATCOLOR = PERMISSIFY_PREFIX + "&c&lInvalid chat color <COLOR>";
    public static final String INVALID_GROUP = PERMISSIFY_PREFIX + "&c&lInvalid group!";
    public static final String CONSOLE_INVALID_COMMAND = PERMISSIFY_PREFIX + "&c&lOnly one console command available: /permissify superadmin [username]";
    public static final String INVALID_PLAYER = PERMISSIFY_PREFIX + "&c&lInvalid player!";

    public static final String UNABLE_TO_CREATE = PERMISSIFY_PREFIX + "&c&lUnable to create <TYPE>: <REASON>";
    public static final String UNABLE_TO_REMOVE = PERMISSIFY_PREFIX + "&c&lUnable to remove <TYPE>: <REASON>";
    public static final String UNABLE_TO_ADD = PERMISSIFY_PREFIX + "&c&lUnable to add: <REASON>";
    public static final String UNABLE_TO_LIST = PERMISSIFY_PREFIX + "&c&lUnable to list: <REASON>";
    public static final String UNABLE_OTHER = PERMISSIFY_PREFIX + "&c&lUnable to continue: <REASON>";

    // Help response
    public static final List<String> PERMISSIFY_HELP = new ArrayList<>(Arrays.asList(
            "&e================== &a&lPermissify Help &e==================",
            "&a&l/permissify group create [name] [prefix] [suffix] [chat-color]",
            "&a&l/permissify group addpermission [name] [permission]",
            "&a&l/permissify group removepermission [name] [permission]",
            "&a&l/permissify group listpermissions [name]",
            "&a&l/permissify group list",
            "&e====================================================="
    ));
}
