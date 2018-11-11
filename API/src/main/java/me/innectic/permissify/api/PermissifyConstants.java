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

    public static final int PERMISSIFY_PROFILE_VERSION = 2;

    public static final String PERMISSIFY_HELP_HEADER = "&e================== &a&lPermissify Help &e==================";
    public static final String PERMISSIFY_HELP_FOOTER = "&e=====================================================";
    public static final int LINES_PER_PAGE = 10;

    public static final List<String> PERMISSIFY_HELP_PAGES = new ArrayList<>(Arrays.asList(
            "/permissify group permission <add|remove> <name> <permissions...>",
            "/permissify group permission list <name>",
            "/permissify group <create|delete> <name>",
            "/permissify group list",
            "/permissify group player <add|remove> <players...>",
            "/permissify group player list <group>",
            "/permissify group option <option_name> <value>",
            "/permissify player permission <add|remove> <player> <permissions...> <seconds>",
            "/permissify player permission list <player>",
            "/permissify player groups list <player>",
            "/permissify player groups main [group]",
            "/permissify superadmin <grant|revoke> <name>",
            "/permissify cache",
            "/permissify cache purge",
            "/permissify help [page]"
    ));
    public static final int MAX_PAGES = PERMISSIFY_HELP_PAGES.size() / LINES_PER_PAGE;

    // Chat messages
    private static final String PERMISSIFY_PREFIX = "&a&lPermissify> ";
    public static final String INVALID_HELP_PAGE_INDEX = PERMISSIFY_PREFIX + "&c&lInvalid page: <PAGE>";
    public static final String YOU_DONT_HAVE_PERMISSION = PERMISSIFY_PREFIX + "&c&lYou don't have permission for this command!";
    public static final String HANDLER_IS_NOT_PRESENT = PERMISSIFY_PREFIX + "&c&lINTERNAL ERROR: Database Handler is not present!";

    public static final String PLAYER_ADDED_AS_SUPER_ADMIN = PERMISSIFY_PREFIX + "&e&l<PLAYER> added as superadmin!";
    public static final String PLAYER_REMOVED_FROM_SUPER_ADMIN = PERMISSIFY_PREFIX + "&e&l<PLAYER> is no longer superadmin!";

    private static final String INVALID_ARGUMENT = PERMISSIFY_PREFIX + "&c&lInvalid argument! ";
    private static final String NOT_ENOUGH_ARGUMENTS = PERMISSIFY_PREFIX + "&c&lNot enough arguments! ";
    public static final String NEA_SUPER_ADMIN = NOT_ENOUGH_ARGUMENTS + PERMISSIFY_HELP_PAGES.get(11);
    public static final String INV_SUPER_ADMIN = INVALID_ARGUMENT + PERMISSIFY_HELP_PAGES.get(11);

    public static final String INVALID_PLAYER = INVALID_ARGUMENT + "That player is not online!";
    public static final String MUST_PROVIDE_PLAYER = INVALID_ARGUMENT + "Must specify a player!";

    // Permissions
    public static final String PERMISSIFY_ADMIN = "permissify.admin";
    public static final String PERMISSIFY_SUPERADMIN_GRANT = "permissify.superadmin.grant";
    public static final String PERMISSIFY_SUPERADMIN_REVOKE = "permissify.superadmin.revoke";

    // Error response
    public static final List<String> PERMISSIFY_ERROR = new ArrayList<>(Arrays.asList(
            "&c&lError encountered: <ERROR_TYPE>",
            "&c&lShould this be reported?: <SHOULD_REPORT>"
    ));
}
