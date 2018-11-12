package me.innectic.permissify.spigot.commands.subcommands;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.api.util.Tristate;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        String segment = args[0].toLowerCase();
        args = ArgumentUtil.skipFirst(args);

        switch (segment) {
            case "permission":
                return permissionSubCommand(sender, args);
            case "list":
                return listGroups(sender, args);
            case "player":
                return playerSubCommand(sender, args);
            case "create":
                return createGroup(sender, args);
            case "remove":
                return removeGroup(sender, args);
            default:
                return PermissifyConstants.INVALID_ARGUMENT_GROUP;
        }
    }

    private String createGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP_CREATE)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_CREATE_DELETE;

        String name = args[0];

        // Make sure we can access the Permissify API
        PermissifyMain plugin = PermissifyMain.getInstance();
        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        // :group_rewrite
        Tristate state = handler.get().createGroup(name, "", "", "", "");
        return "THIS IS A USEFUL DEBUG MESSAGE WOW LOOK AT ME " + state;
    }

    private String removeGroup(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP_DELETE)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_CREATE_DELETE;

        String name = args[0];

        // Make sure we can access the Permissify API
        PermissifyMain plugin = PermissifyMain.getInstance();
        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        // :group_rewrite
        Tristate state = handler.get().deleteGroup(name);
        return "THIS IS A USEFUL DEBUG MESSAGE WOW LOOK AT ME 2 " + state;
    }

    private String playerSubCommand(CommandSender sender, String[] args) {
        if (args.length == 0) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_PLAYER;

        String segment = args[0].toLowerCase();
        args = ArgumentUtil.skipFirst(args);
        switch (segment) {
            case "add":
                return playerAdd(sender, args);
            case "remove":
                return playerRemove(sender, args);
            case "list":
                return playerList(sender, args);
            default:
                return PermissifyConstants.INVALID_ARGUMENT_GROUP_PLAYER;
        }
    }

    private String playerAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP_PLAYER_ADD)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_PLAYER_ADD;

        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        String groupName = args[0];
        String playerName = args[1];

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(groupName);
        if (!group.isPresent()) return PermissifyConstants.INVALID_GROUP;

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (player == null || player.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;
        if (group.get().hasPlayer(player.getUniqueId())) return PermissifyConstants.PLAYER_ALREADY_IN_GROUP;

        group.get().addPlayer(player.getUniqueId(), false);
        return PermissifyConstants.PLAYER_ADDED_TO_GROUP;
    }

    private String playerRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP_PLAYER_REMOVE)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_PLAYER_REMOVE;

        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        String groupName = args[0];
        String playerName = args[1];

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(groupName);
        if (!group.isPresent()) return PermissifyConstants.INVALID_GROUP;

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (player == null || player.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;
        if (!group.get().hasPlayer(player.getUniqueId())) return PermissifyConstants.PLAYER_NOT_IN_GROUP;

        group.get().removePlayer(player.getUniqueId());
        return PermissifyConstants.PLAYER_REMOVED_FROM_GROUP;
    }

    private String playerList(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP_PLAYER_LIST)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGS_GROUP_PLAYER_LIST;

        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        String groupName = args[0];

        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroup(groupName);
        if (!group.isPresent()) return PermissifyConstants.INVALID_GROUP;

        String playersInGroup = String.join(", ",
                group.get().getPlayers().keySet().stream()
                        .map(Bukkit::getPlayer).map(Player::getName).collect(Collectors.toList()));
        return PermissifyConstants.PLAYERS_IN_GROUP.replace("<PLAYERS>", playersInGroup);
    }

    private String listGroups(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_GROUP_LIST)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        PermissifyMain plugin = PermissifyMain.getInstance();

        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        String groups = String.join(", ", handler.get().getGroups().keySet());
        return PermissifyConstants.GROUP_LIST.replace("<GROUPS>", groups);
    }

    private String permissionSubCommand(CommandSender sender, String[] args) {
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
