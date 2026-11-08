package me.innectic.permissify.spigot.commands;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.command.CommandSender;

import java.util.Locale;
import java.util.Map;

final class CommandAccess {

    static final Map<String, String> GROUP_COMMANDS = Map.of(
            "create", PermissifyConstants.PERMISSIFY_GROUP_CREATE,
            "inherit", PermissifyConstants.PERMISSIFY_GROUP_INHERIT,
            "remove", PermissifyConstants.PERMISSIFY_GROUP_REMOVE,
            "list", PermissifyConstants.PERMISSIFY_GROUP_LIST,
            "addpermission", PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_ADD,
            "removepermission", PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_REMOVE,
            "listpermissions", PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_LIST,
            "default", PermissifyConstants.PERMISSIFY_GROUP_DEFAULT);
    static final Map<String, String> PLAYER_COMMANDS = Map.of(
            "addgroup", PermissifyConstants.PERMISSIFY_PLAYER_GROUP_ADD,
            "removegroup", PermissifyConstants.PERMISSIFY_PLAYER_GROUP_REMOVE,
            "listgroups", PermissifyConstants.PERMISSIFY_PLAYER_GROUP_LIST,
            "primarygroup", PermissifyConstants.PERMISSIFY_PLAYER_SET_MAIN_GROUP,
            "addpermission", PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_ADD,
            "removepermission", PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_REMOVE,
            "listpermissions", PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_LIST);
    static boolean canUse(CommandSender sender, String category, String action) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_BASIC)) return false;
        action = action.toLowerCase(Locale.ROOT);
        String permission = switch (category.toLowerCase(Locale.ROOT)) {
            case "help" -> PermissifyConstants.PERMISSIFY_BASIC;
            case "cache" -> action.isEmpty() || action.equals("purge") ? PermissifyConstants.PERMISSIFY_CACHE : null;
            case "profile" -> action.equals("save") || action.equals("load") ? PermissifyConstants.PERMISSIFY_PROFILE : null;
            case "superadmin" -> PermissionUtil.isConsole(sender) && (action.equals("grant") || action.equals("remove"))
                    ? PermissifyConstants.PERMISSIFY_BASIC : null;
            case "group" -> GROUP_COMMANDS.get(switch (action) {
                case "add" -> "create";
                case "delete" -> "remove";
                default -> action;
            });
            case "player" -> PLAYER_COMMANDS.get(action.equals("listgroup") ? "listgroups" : action);
            default -> null;
        };
        return permission != null && PermissionUtil.hasPermissionOrSuperAdmin(sender, permission);
    }
}