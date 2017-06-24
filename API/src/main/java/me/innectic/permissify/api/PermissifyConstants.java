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
 */
public class PermissifyConstants {
    // TODO: Is there a better way to handle the colors in this file?

    private static final String PERMISSIFY_PREFIX = "&a&lPermissify> ";

    public static final String INSUFFICIENT_PERMISSIONS = PERMISSIFY_PREFIX + "&c&lInsufficient permissions!";
    public static final String GROUP_CREATED = PERMISSIFY_PREFIX + "&c&lGroup <GROUP> has been created!";
    public static final String GROUP_REMOVED = PERMISSIFY_PREFIX + "&c&lGroup <GROUP> has been removed!";
    public static final String GROUP_EDITED = PERMISSIFY_PREFIX + "&c&lGroup <GROUP> has been edited!";
    public static final String PERMISSION_ADDED_GROUP = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been added to <GROUP>!";
    public static final String PERMISSION_REMOVED_GROUP = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been removed from <GROUP>!";
    public static final String PERMISSION_ADDED_PLAYER = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been added to <PLAYER>!";
    public static final String PERMISSION_REMOVED_PLAYER = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been removed from <PLAYER>!";

    public static final String CONSOLE_INVALID_COMMAND = PERMISSIFY_PREFIX + "&c&lOnly one console command available: /permissify superadmin [username]";

    public static final List<String> PERMISSIFY_HELP = new ArrayList<>(Arrays.asList(
            "&e=============== &a&lPermissify Help &e===============",
            "&a&l/permissify group create [name] [prefix] [suffix] [chat-color]",
            "&a&l/permissify group addpermission [name] [permission]",
            "&a&l/permissify group removepermission [name] [permission]",
            "&a&l/permissify group listpermission [name]",
            "&a&l/permissify group list",
            "&e==============================================="
    ));
}
