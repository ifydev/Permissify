package me.innectic.permissify.spigot.commands.subcommands;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 11/11/2018
 */
public class GroupSubCommand implements AbstractSubCommand {

    @Override
    public String handle(CommandSender sender, String[] args) {
        if (args.length == 0) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP;

        String segment = args[1].toLowerCase();
        args = ArgumentUtil.skipFirst(args);

        if (segment.equals("permission")) return permissionSubCommand(sender, args);
        else if (segment.equals("list")) return listGroups(sender, args);
        return PermissifyConstants.INVALID_ARGUMENT_SUPER_ADMIN;
    }

    private String listGroups(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.GROUP_LIST)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        PermissifyMain plugin = PermissifyMain.getInstance();

        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        String groups = String.join(", ", handler.get().getGroups().keySet());
        return PermissifyConstants.GROUP_LIST.replace("<GROUPS>", groups);
    }

    private String permissionSubCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length == 0) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_PERMISSION;

        String segment = args[0].toLowerCase();
        args = ArgumentUtil.skipFirst(args);
        switch (segment) {
            case "add":
                return addPermissionToGroup(sender, args);
            case "remove":
                return removePermissionFromGroup(sender, args);
            case "list":
                return listPermissionsForGroup(sender, args);
            default:
                return PermissifyConstants.INVALID_ARGUMENT_GROUP;
        }
    }

    private String addPermissionToGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP_ADD_PERMS)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 1) return PermissifyConstants.MUST_PROVIDE_GROUP;
        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_PERMISSION_ADD_REMOVE;

        PermissifyMain plugin = PermissifyMain.getInstance();
        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        String name = args[0];
        String[] permissions = ArgumentUtil.skipFirst(args);

        // Attempt to get the group
        Optional<PermissionGroup> group = handler.get().getGroup(name);
        if (!group.isPresent()) return PermissifyConstants.INVALID_GROUP;
        for (String permission : permissions) group.get().addPermission(permission);

        PermissionUtil.updateGroupPermissions(group.get());

        return PermissifyConstants.PERMISSION_ADDED_TO_GROUP.replace("<s>", permissions.length == 1 ? "" : "s");
    }

    private String removePermissionFromGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP_REMOVE_PERMS)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 1) return PermissifyConstants.MUST_PROVIDE_GROUP;
        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_PERMISSION_ADD_REMOVE;

        PermissifyMain plugin = PermissifyMain.getInstance();
        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        String name = args[0];
        String[] permissions = ArgumentUtil.skipFirst(args);

        // Attempt to get the group
        Optional<PermissionGroup> group = handler.get().getGroup(name);
        if (!group.isPresent()) return PermissifyConstants.INVALID_GROUP;
        for (String permission : permissions) group.get().removePermission(permission);

        PermissionUtil.updateGroupPermissions(group.get());

        return PermissifyConstants.PERMISSION_REMOVED_FROM_GROUP.replace("<s>", permissions.length == 1 ? "" : "s");
    }

    private String listPermissionsForGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP_LIST_PERMS)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_LIST_PERMISSIONS;

        PermissifyMain plugin = PermissifyMain.getInstance();
        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        String name = args[0];

        // Attempt to get the group
        Optional<PermissionGroup> group = handler.get().getGroup(name);
        if (!group.isPresent()) return PermissifyConstants.INVALID_GROUP;

        String permissions = String.join(", ", group.get().getPermissions().stream()
                .map(Permission::getPermission).collect(Collectors.toList()));

        return PermissifyConstants.GROUP_PERMISSION_LIST
                .replace("<GROUP>", name)
                .replace("<PERMISSIONS>", permissions);
    }
}
