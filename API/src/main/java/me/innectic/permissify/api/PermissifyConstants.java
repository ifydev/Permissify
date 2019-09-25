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
            "/permissify group player <add|remove> <group> <players...>",
            "/permissify group player list <group>",
            "/permissify group option <option_name> <value>",
            "/permissify player permission <add|remove> <player> <permissions...>",
            "/permissify player permission timed <player> <time> <permissions...>",
            "/permissify player permission list <player>",
            "/permissify player group list <player>",
            "/permissify player group main <player> [group]",
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
    public static final String PERMISSION_ADDED_TO_GROUP = PERMISSIFY_PREFIX + "&e&lPermission<s> added to group!";
    public static final String PERMISSION_REMOVED_FROM_GROUP = PERMISSIFY_PREFIX + "&e&lPermission<s> removed from group!";
    public static final String GROUP_PERMISSION_LIST = PERMISSIFY_PREFIX + "&e&lPermissions for <GROUP>: <GROUP_PERMISSION_LIST>";
    public static final String GROUP_LIST = PERMISSIFY_PREFIX + "&e&lList of groups: <GROUPS>";
    public static final String PLAYER_ADDED_TO_GROUP = PERMISSIFY_PREFIX + "&e&lPlayer has been added to the group!";
    public static final String PLAYER_REMOVED_FROM_GROUP = PERMISSIFY_PREFIX + "&e&lPlayer has been removed from the group!";
    public static final String PLAYERS_IN_GROUP = PERMISSIFY_PREFIX + "&e&lPlayers in <GROUP>: <PLAYERS>";
    public static final String PERMISSIONS_FOR_PLAYER = PERMISSIFY_PREFIX + "&e&lPermissions for <PLAYER>: <PERMISSIONS>";
    public static final String NO_PERMISSIONS = PERMISSIFY_PREFIX + "&e&lNo permissions present!";
    public static final String PERMISSION_ADDED_TO_PLAYER = PERMISSIFY_PREFIX + "&e&lPermissions have been added to <PLAYER>!";
    public static final String PERMISSION_REMOVED_FROM_PLAYER = PERMISSIFY_PREFIX + "&e&lPermissions have been removed from <PLAYER>!";
    public static final String NO_GROUPS = PERMISSIFY_PREFIX + "&e&lNo groups present!";
    public static final String PLAYER_GROUPS = PERMISSIFY_PREFIX + "&e&lGroup<s> for player: <GROUPS>";
    public static final String PLAYER_DOES_NOT_HAVE_MAIN_GROUP = PERMISSIFY_PREFIX + "&e&lPlayer does not have a main group!";
    public static final String PLAYER_MAIN_GROUP = PERMISSIFY_PREFIX + "&e&lMain group for <PLAYER>: <GROUP>";
    public static final String PLAYER_MAIN_GROUP_SET = PERMISSIFY_PREFIX + "&e&lMain group has been set!";

    private static final String NOT_ENOUGH_ARGUMENTS = PERMISSIFY_PREFIX + "&c&lNot enough arguments! ";
    public static final String NOT_ENOUGH_ARGS_SUPER_ADMIN = NOT_ENOUGH_ARGUMENTS + PERMISSIFY_HELP_PAGES.get(11);
    public static final String NOT_ENOUGH_ARGS_GROUP = NOT_ENOUGH_ARGUMENTS + "/permissify group <permission|create|delete|list> <args...>";
    public static final String NOT_ENOUGH_ARGS_GROUP_PERMISSION = NOT_ENOUGH_ARGUMENTS + "/permissify group permission <add|remove|list> <args...>";
    public static final String NOT_ENOUGH_ARGS_GROUP_PERMISSION_ADD_REMOVE = NOT_ENOUGH_ARGUMENTS + PERMISSIFY_HELP_PAGES.get(0);
    public static final String NOT_ENOUGH_ARGS_GROUP_LIST_PERMISSIONS = NOT_ENOUGH_ARGUMENTS + PERMISSIFY_HELP_PAGES.get(1);
    public static final String NOT_ENOUGH_ARGS_GROUP_PLAYER = NOT_ENOUGH_ARGUMENTS + "/permissify group player <add|remove|list> <args...>";
    public static final String NOT_ENOUGH_ARGS_GROUP_PLAYER_ADD = NOT_ENOUGH_ARGUMENTS + "/permissify group player add <group> <players...>";
    public static final String NOT_ENOUGH_ARGS_GROUP_PLAYER_REMOVE = NOT_ENOUGH_ARGUMENTS + "/permissify group player remove <group> <players...>";
    public static final String NOT_ENOUGH_ARGS_GROUP_PLAYER_LIST = NOT_ENOUGH_ARGUMENTS + "/permissify group player list <group>";
    public static final String NOT_ENOUGH_ARGS_GROUP_CREATE_DELETE = NOT_ENOUGH_ARGUMENTS + "/permissify group <create|delete> <name>";
    public static final String NOT_ENOUGH_ARGS_GROUP_OPTION = NOT_ENOUGH_ARGUMENTS + "/permissify group <option> <value>";
    public static final String NOT_ENOUGH_ARGS_PLAYER = NOT_ENOUGH_ARGUMENTS + "/permissify player <permission|groups|timed|list> <args...>";
    public static final String NOT_ENOUGH_ARGS_PLAYER_PERMISSION = NOT_ENOUGH_ARGUMENTS + "/permissify player permission <add|remove|timed> <player> <permissions...>";
    public static final String NOT_ENOUGH_ARGS_PLAYER_PERMISSION_LIST = NOT_ENOUGH_ARGUMENTS + "/permissify player permission list <player>";
    public static final String NOT_ENOUGH_ARGS_PLAYER_PERMISSION_ADD_REMOVE = NOT_ENOUGH_ARGUMENTS + "/permissify player permission <add|remove|timed> <player> <permissions...>";
    public static final String NOT_ENOUGH_ARGS_PLAYER_PERMISSION_TIMED = NOT_ENOUGH_ARGUMENTS + "/permissify player permission timed <player> <time> <permissions...>";
    public static final String NOT_ENOUGH_ARGS_PLAYER_GROUP = NOT_ENOUGH_ARGUMENTS + "/permissify player group <list|group> <args...>";
    public static final String NOT_ENOUGH_ARGS_PLAYER_GROUP_LIST = NOT_ENOUGH_ARGUMENTS + "/permissify player group list <player>";
    public static final String NOT_ENOUGH_ARGS_PLAYER_GROUP_MAIN = NOT_ENOUGH_ARGUMENTS + "/permissify player group main <player> [group]";

    private static final String INVALID_ARGUMENT = PERMISSIFY_PREFIX + "&c&lInvalid argument! ";
    public static final String INVALID_ARGUMENT_SUPER_ADMIN = INVALID_ARGUMENT + PERMISSIFY_HELP_PAGES.get(11);
    public static final String INVALID_ARGUMENT_GROUP = INVALID_ARGUMENT + "/permissify group <permission|create|delete|list> <args...>";
    public static final String INVALID_ARGUMENT_GROUP_PLAYER = INVALID_ARGUMENT + "/permissify group player <add|remove|list> <args...>";
    public static final String INVALID_GROUP = PERMISSIFY_PREFIX + "&c&lInvalid group!";
    public static final String INVALID_ARGUMENT_PLAYER = INVALID_ARGUMENT + "/permissify player <permission|groups|timed|list> <args...>";
    public static final String INVALID_ARGUMENT_PLAYER_PERMISSION = INVALID_ARGUMENT + "/permissify player permission <add|remove|timed|list> <args...>";
    public static final String INVALID_ARGUMENT_PLAYER_GROUP = INVALID_ARGUMENT + "/permissify player group <list|main> <args...>";

    public static final String INVALID_PLAYER = INVALID_ARGUMENT + "That player has not played before!";
    public static final String MUST_PROVIDE_PLAYER = INVALID_ARGUMENT + "Must specify a player!";
    public static final String MUST_PROVIDE_GROUP = INVALID_ARGUMENT + "Must specify a group!";
    public static final String PLAYER_ALREADY_IN_GROUP = PERMISSIFY_PREFIX + "&c&lPlayer is already in group!";
    public static final String PLAYER_NOT_IN_GROUP = PERMISSIFY_PREFIX + "&c&lPlayer is not in group!";
    public static final String THAT_IS_NOT_TIME = PERMISSIFY_PREFIX + "&c&lMust provide a valid time in seconds.";

    // Permissions
    public static final String PERMISSIFY_ADMIN = "permissify.admin";
    public static final String PERMISSIFY_STAR = "permissify.star";
    public static final String PERMISSIFY_SUPERADMIN_GRANT = "permissify.superadmin.grant";
    public static final String PERMISSIFY_SUPERADMIN_REVOKE = "permissify.superadmin.revoke";
    public static final String PERMISSIFY_GROUP_ADD_PERMS = "permissify.group.permission.add";
    public static final String PERMISSIFY_GROUP_REMOVE_PERMS = "permissify.group.permission.remove";
    public static final String PERMISSIFY_GROUP_LIST_PERMS = "permissify.group.permission.list";
    public static final String PERMISSIFY_GROUP_LIST = "permissify.group.list";
    public static final String PERMISSIFY_GROUP_PLAYER_ADD = "permissify.group.player.add";
    public static final String PERMISSIFY_GROUP_PLAYER_REMOVE = "permissify.group.player.remove";
    public static final String PERMISSIFY_GROUP_PLAYER_LIST = "permissify.group.player.list";
    public static final String PERMISSIFY_GROUP_CREATE = "permissify.group.create";
    public static final String PERMISSIFY_GROUP_DELETE = "permissify.group.delete";
    public static final String PERMISSIFY_GROUP_OPTION = "permissify.group.option";
    public static final String PERMISSIFY_PLAYER_PERMISSIONS_LIST = "permissify.player.permissions.list";
    public static final String PERMISSIFY_PLAYER_PERMISSIONS_ADD = "permissify.player.permissions.add";
    public static final String PERMISSIFY_PLAYER_PERMISSIONS_REMOVE = "permissify.player.permissions.remove";
    public static final String PERMISSIFY_PLAYER_PERMISSIONS_TIMED = "permissify.player.permission.timed";
    public static final String PERMISSIFY_PLAYER_GROUP_LIST = "permissify.player.group.list";
    public static final String PERMISSIFY_PLAYER_GROUP_MAIN = "permissify.player.group.main";

    public static final String PERMISSIFY_PLAYER_SET_MAIN_GROUP = "permissify.player.group.primarygroup";
    public static final String PERMISSIFY_GROUP_DEFAULT = "permissify.group.default";
    public static final String PERMISSIFY_CACHE = "permissify.cache";
    public static final String PERMISSIFY_PROFILE = "permissify.profile";

    public static final String INSUFFICIENT_PERMISSIONS = PERMISSIFY_PREFIX + "&c&lInsufficient permissions!";

    public static final String GROUP_CREATED = PERMISSIFY_PREFIX + "&e&lGroup <GROUP> has been created!";
    public static final String GROUP_REMOVED = PERMISSIFY_PREFIX + "&e&lGroup <GROUP> has been removed!";
    public static final String GROUP_PERMISSIONS = PERMISSIFY_PREFIX + "&e&lPermissions for <GROUP>: <PERMISSIONS>";
    public static final String PLAYER_GROUP_LIST = PERMISSIFY_PREFIX + "&e&lGroups for <PLAYER>: <GROUPS>";
    public static final String CACHE_PURGED = PERMISSIFY_PREFIX + "&e&lCache has been purged!";
    public static final String CACHE_INFORMATION = PERMISSIFY_PREFIX + "&e&lCached groups: <GROUPS>, cached permissions: <PERMISSIONS>, default group: <DEFAULT>";
    public static final String PRIMARY_GROUP_TEMPLATE = PERMISSIFY_PREFIX + "&e&lCurrent primary group: <GROUP>";

    public static final String PERMISSION_ADDED_GROUP = PERMISSIFY_PREFIX + "&e&lPermission <PERMISSION> has been added to <GROUP>!";
    public static final String PERMISSION_REMOVED_GROUP = PERMISSIFY_PREFIX + "&e&lPermission <PERMISSION> has been removed from <GROUP>!";

    public static final String PERMISSION_ADDED_PLAYER = PERMISSIFY_PREFIX + "&e&lPermission <PERMISSION> has been added to <PLAYER>!";
    public static final String PERMISSION_ADDED_PLAYER_TIMED = PERMISSIFY_PREFIX + "&e&lPermission <PERMISSION> has been added to <PLAYER>, and will expire in <SECONDS>!";
    public static final String PERMISSION_REMOVED_PLAYER = PERMISSIFY_PREFIX + "&e&lPermission <PERMISSION> has been removed from <PLAYER>!";
    public static final String MAIN_GROUP_SET = PERMISSIFY_PREFIX + "&e&lSet main group for <PLAYER> to <GROUP>!";
    public static final String PLAYER_ALREADY_HAS_PERMISSION = PERMISSIFY_PREFIX + "&c&lPlayer <PLAYER> already has permission <PERMISSION>";
    public static final String PLAYER_DOES_NOT_HAVE_PERMISSION = PERMISSIFY_PREFIX + "&c&lPlayer <PLAYER> does not have permission <PERMISSION>";
    public static final String ALREADY_MAIN_GROUP = PERMISSIFY_PREFIX + "&c&l<GROUP> is already the main group!";
    public static final String GROUP_ALREADY_EXISTS = PERMISSIFY_PREFIX + "&c&l<GROUP> already exists!";

    public static final String EMPTY_DEFAULT_GROUP_NAME = "&c&lNONE";
    public static final String DEFAULT_GROUP_RESPONSE = PERMISSIFY_PREFIX + "&e&lThe current default group is '<GROUP>&e&l'.";
    public static final String DEFAULT_GROUP_SET = PERMISSIFY_PREFIX + "&e&lThe default group has been set to '<GROUP>&e&l'.";

    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_CREATE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group create [name] [display_name] [prefix] [suffix] [chat_color]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_REMOVE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group remove [name]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_ADD = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group addpermission [group] [permission]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_REMOVE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group removepermission [group] [permission]";
    public static final String NOT_ENOUGH_ARGUMENTS_GROUP_PERMISSION_LIST = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify group listpermissions [group]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player [addgroup|removegroup|listpermissions|listgroups|addpermission|removepermission] [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_PERMISSION = PERMISSIFY_PREFIX  + "&c&lNot enough arguments! &e&l/permissify player addpermission [player] [permission]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_PERMISSION = PERMISSIFY_PREFIX  + "&c&lNot enough arguments! &e&l/permissify player removepermission [player] [permission]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_PERMISSIONS = PERMISSIFY_PREFIX  + "&c&lNot enough arguments! &e&l/permissify player listpermissions [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_ADD_GROUP= PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player addgroup [player] [group]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_REMOVE_GROUP= PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player removegroup [player] [group]";
    public static final String NOT_ENOUGH_ARGUMENTS_PLAYER_LIST_GROUP = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player listgroup [player]";
    public static final String NOT_ENOUGH_ARGUMENTS_SET_MAIN_GROUP = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify player primarygroup <player> [group]";
    public static final String NOT_ENOUGH_ARGUMENTS_PROFILE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify profile [save|load] [profile]";
    public static final String NOT_ENOUGH_ARGUMENTS_PROFILE_SAVE = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify profile save [fileName]";
    public static final String NOT_ENOUGH_ARGUMENTS_PROFILE_LOAD = PERMISSIFY_PREFIX + "&c&lNot enough arguments! &e&l/permissify profile load [source]";
    public static final String NOT_ENOUGH_ARGUMENTS_SUPERADMIN = PERMISSIFY_PREFIX + "&c&l/Not enough arguments! &e&l/permissify superadmin <grant|remove> <player>";

    public static final String PROFILE_SAVED = PERMISSIFY_PREFIX + "&e&lSaved profile '<PROFILE>'.";
    public static final String PROFILE_NOT_SAVED = PERMISSIFY_PREFIX + "&c&lUnable to save profile '<PROFILE>'";
    public static final String PROFILE_LOADED = PERMISSIFY_PREFIX + "&e&lLoaded profile '<PROFILE>' in <TIME> ms.";
    public static final String PROFILE_NOT_LOADED = PERMISSIFY_PREFIX + "&c&lUnable to load profile '<PROFILE>'";

    public static final String INVALID_CHATCOLOR = PERMISSIFY_PREFIX + "&c&lInvalid chat color <COLOR>";
    public static final String INVALID_HELP_PAGE = PERMISSIFY_PREFIX + "&c&lInvalid page: <PAGE>";

    public static final String UNABLE_TO_CREATE = PERMISSIFY_PREFIX + "&c&lUnable to create <TYPE>: <REASON>";
    public static final String UNABLE_TO_REMOVE = PERMISSIFY_PREFIX + "&c&lUnable to remove <TYPE>: <REASON>";
    public static final String UNABLE_TO_ADD = PERMISSIFY_PREFIX + "&c&lUnable to add: <REASON>";
    public static final String UNABLE_TO_LIST = PERMISSIFY_PREFIX + "&c&lUnable to list: <REASON>";
    public static final String UNABLE_OTHER = PERMISSIFY_PREFIX + "&c&lUnable to continue: <REASON>";
    public static final String UNABLE_TO_SET = PERMISSIFY_PREFIX + "&c&lUnable to set: <REASON>";

    public static final String DEBUG_ENABLED = PERMISSIFY_PREFIX + "&e&lDebug mode &2&lenabled!";
    public static final String DEBUG_DISABLED = PERMISSIFY_PREFIX + "&e&lDebug mode &c&ldisabled!";

    // Error response
    public static final List<String> PERMISSIFY_ERROR = new ArrayList<>(Arrays.asList(
            "&c&lError encountered: <ERROR_TYPE>",
            "&c&lShould this be reported?: <SHOULD_REPORT>"
    ));
}
