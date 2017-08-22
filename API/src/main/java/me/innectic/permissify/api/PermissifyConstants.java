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
 * Constant attributes between the different server implementations.
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
    public static final String PERMISSIFY_PLAYER_PERMISSION_LIST = "permissify.player.permission.list";
    public static final String PERMISSIFY_PLAYER_PERMISSION_ADD = "permissify.player.permission.add";
    public static final String PERMISSIFY_PLAYER_PERMISSION_REMOVE = "permissify.player.permission.remove";
    public static final String PERMISSIFY_PLAYER_GROUP_ADD = "permissify.player.group.add";
    public static final String PERMISSIFY_PLAYER_GROUP_REMOVE = "permissify.player.group.remove";
    public static final String PERMISSIFY_PLAYER_GROUP_LIST = "permissify.player.group.list";
    public static final String PERMISSIFY_PLAYER_SET_MAIN_GROUP = "permissify.player.group.setmain";
    public static final String PERMISSIFY_FORMAT = "permissify.format";
    public static final String PERMISSIFY_CACHE = "permissify.cache";
    public static final String PERMISSIFY_ADMIN = "permissify.admin";

    public static final String INSUFFICIENT_PERMISSIONS = PERMISSIFY_PREFIX + "&c&lInsufficient permissions!";

    public static final String GROUP_CREATED = PERMISSIFY_PREFIX + "&c&lGroup <GROUP> has been created!";
    public static final String GROUP_REMOVED = PERMISSIFY_PREFIX + "&c&lGroup <GROUP> has been removed!";
    public static final String GROUP_PERMISSIONS = PERMISSIFY_PREFIX + "&c&lPermissions for <GROUP>: <PERMISSIONS>";
    public static final String GROUP_LIST = PERMISSIFY_PREFIX + "&c&lRegistered groups: <GROUPS>";
    public static final String PLAYER_GROUP_LIST = PERMISSIFY_PREFIX + "&c&lGroups for <PLAYER>: <GROUPS>";
    public static final String CACHE_PURGED = PERMISSIFY_PREFIX + "&e&lCache has been cleared!";

    public static final String PERMISSION_ADDED_GROUP = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been added to <GROUP>!";
    public static final String PERMISSION_REMOVED_GROUP = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been removed from <GROUP>!";

    public static final String PERMISSION_ADDED_PLAYER = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been added to <PLAYER>!";
    public static final String PERMISSION_REMOVED_PLAYER = PERMISSIFY_PREFIX + "&c&lPermission <PERMISSION> has been removed from <PLAYER>!";
    public static final String PLAYER_ADDED_TO_GROUP = PERMISSIFY_PREFIX + "&c&lAdded <PLAYER> to <GROUP>";
    public static final String PLAYER_REMOVED_FROM_GROUP = PERMISSIFY_PREFIX + "&c&lRemoved <PLAYER> from <GROUP>";
    public static final String FORMATTER_SET = PERMISSIFY_PREFIX + "&e&lFormatter <FORMATTER> set!";
    public static final String MAIN_GROUP_SET = PERMISSIFY_PREFIX + "&e&lSet main group for <PLAYER> to <GROUP>!";

    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_CREATE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group create [name] [prefix] [suffix] [chat-color]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_REMOVE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group remove [name]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_ADD = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group addpermission [name] [permission]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_REMOVE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group removepermission [name] [permission]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_LIST = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group listpermissions [group]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player [addgroup|removegroup|listpermissions|listgroups|addpermission|removepermission] [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_PERMISSION = PERMISSIFY_PREFIX  + "&c&lNot enough arguments! &e&l/permissify player addpermission [permission] [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_PERMISSION = PERMISSIFY_PREFIX  + "&c&lNot enough arguments! &e&l/permissify player removepermission [permission] [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_PERMISSIONS = PERMISSIFY_PREFIX  + "&c&lNot enough arguments! &e&l/permissify player listpermissions [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_GROUP= PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player addgroup [group] [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_GROUP= PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player removegroup [group] [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_GROUP = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player listgroup [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_SET_FORMAT = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify format [whisper|chat] [format...]";
    public static final String NOT_ENOUGH_ARGUMENTS_SET_MAIN_GROUP = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player setmain [player] [group]";

    public static final String INVALID_CHATCOLOR = PERMISSIFY_PREFIX + "&c&lInvalid chat color <COLOR>";
    public static final String INVALID_GROUP = PERMISSIFY_PREFIX + "&c&lInvalid group!";
    public static final String CONSOLE_INVALID_COMMAND = PERMISSIFY_PREFIX + "&c&lOnly one console command available: /permissify superadmin [username]";
    public static final String INVALID_PLAYER = PERMISSIFY_PREFIX + "&c&lInvalid player!";
    public static final String INVALID_ARGUMENT = PERMISSIFY_PREFIX + "&c&lInvalid argument: <ARGUMENT>";

    public static final String PLAYER_NOT_IN_GROUP = PERMISSIFY_PREFIX + "&e&l<PLAYER> isn't in the group <GROUP>!";

    public static final String UNABLE_TO_CREATE = PERMISSIFY_PREFIX + "&c&lUnable to create <TYPE>: <REASON>";
    public static final String UNABLE_TO_REMOVE = PERMISSIFY_PREFIX + "&c&lUnable to remove <TYPE>: <REASON>";
    public static final String UNABLE_TO_ADD = PERMISSIFY_PREFIX + "&c&lUnable to add: <REASON>";
    public static final String UNABLE_TO_LIST = PERMISSIFY_PREFIX + "&c&lUnable to list: <REASON>";
    public static final String UNABLE_OTHER = PERMISSIFY_PREFIX + "&c&lUnable to continue: <REASON>";
    public static final String UNABLE_TO_SET = PERMISSIFY_PREFIX + "&c&lUnable to set: <REASON>";

    public static final String PERMISSIFY_HELP_HEADER = "&e================== &a&lPermissify Help &e==================";
    public static final String PERMISSIFY_HELP_FOOTER = "&e=====================================================";
//    public static final int LINES_PER_PAGE = 10;   TODO: Pagination should happen by `len(pages) / lines_per_page`.

    public static final List<List<String>> PERMISSIFY_HELP_PAGES = new ArrayList<>(Arrays.asList(Arrays.asList(
            "&a&l/permissify help [page]",
            "&a&l/permissify superadmin [player] - &c&lWARNING: &e&lSUPERADMIN GIVES PERMISSION FOR EVERYTHING!",
            "&a&l/permissify cache",
            "&a&l/permissify cache purge",
            "&a&l/permissify format chat [format]",
            "&a&l/permissify format whisper [format]",
            "&a&l/permissify group create [name] [prefix] [suffix] [chatcolor]",
            "&a&l/permissify group remove [name]",
            "&a&l/permissify group addpermission [group] [permissions...]",
            "&a&l/permissify group removepermission [group] [permissions...]"
    ), Arrays.asList(
            "&a&l/permissify group listpermissions [group]",
            "&a&l/permissify group list",
            "&a&l/permissify player addpermission ",
            "&a&l/permissify player removepermission ",
            "&a&l/permissify player addgroup ",
            "&a&l/permissify player listpermissions",
            "&a&l/permissify player listgroups ",
            "&a&l/permissify player removegroup ",
            "&a&l/permissify player setmain "
    )));

    // Error response
    public static final List<String> PERMISSIFY_ERROR = new ArrayList<>(Arrays.asList(
            "&c&lError encountered: <ERROR_TYPE>",
            "&c&lShould this be reported?: <SHOULD_REPORT>"
    ));
}
