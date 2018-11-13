package me.innectic.permissify.spigot.commands.subcommands;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.MiscUtil;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 11/12/2018
 */
public class PlayerSubCommand implements AbstractSubCommand {

    @Override
    public String handle(CommandSender sender, String[] args) {
        if (args.length == 0) return PermissifyConstants.NOT_ENOUGH_ARGS_PLAYER;

        String segment = args[0].toLowerCase();
        args = ArgumentUtil.skipFirst(args);

        switch (segment) {
            case "permission":
                return permissionSubCommand(sender, args);
            case "groups":
                return groupsSubCommand(sender, args);
            default:
                return PermissifyConstants.INVALID_ARGUMENT_PLAYER;
        }
    }

    private String permissionSubCommand(CommandSender sender, String[] args) {
        if (args.length == 0) return PermissifyConstants.NOT_ENOUGH_ARGS_PLAYER_PERMISSION;
        String segment = args[0].toLowerCase();
        args = ArgumentUtil.skipFirst(args);

        switch (segment) {
            case "add":
                return addPlayerPermissions(sender, args);
            case "remove":
                return removePlayerPermissions(sender, args);
            case "timed":
                return timedPermission(sender, args);
            case "list":
                return listPlayerPermissions(sender, args);
            default:
                return PermissifyConstants.INVALID_ARGUMENT_PLAYER_PERMISSION;
        }
    }

    private String listPlayerPermissions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_PERMISSIONS_LIST)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 1) return PermissifyConstants.NOT_ENOUGH_ARGS_PLAYER_PERMISSION_LIST;

        String playerName = args[0];
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (player == null || !player.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;

        PermissifyMain plugin = PermissifyMain.getInstance();
        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();

        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        List<Permission> permissions = handler.get().getPermissions(player.getUniqueId());
        if (permissions.size() == 0) return PermissifyConstants.NO_PERMISSIONS;
        String permissionsString = String.join(", ", permissions.stream().map(Permission::getPermission).collect(Collectors.toList()));
        return PermissifyConstants.PERMISSIONS_FOR_PLAYER.replace("<PLAYER>", player.getName()).replace("<PERMISSIONS>", permissionsString);
    }

    private String addPlayerPermissions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_PERMISSIONS_ADD)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGS_PLAYER_PERMISSION_ADD_REMOVE;

        String playerName = args[0];
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (player == null || !player.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;

        PermissifyMain plugin = PermissifyMain.getInstance();

        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        handler.get().addPermission(player.getUniqueId(), ArgumentUtil.skipFirst(args));
        if (player.isOnline()) PermissionUtil.applyPermissions(player.getPlayer());

        return PermissifyConstants.PERMISSION_ADDED_TO_PLAYER.replace("<PLAYER>", player.getName());
    }

    private String removePlayerPermissions(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_PERMISSIONS_REMOVE)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 2) return PermissifyConstants.NOT_ENOUGH_ARGS_PLAYER_PERMISSION_ADD_REMOVE;

        String playerName = args[0];
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (player == null || !player.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;

        PermissifyMain plugin = PermissifyMain.getInstance();

        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        handler.get().removePermission(player.getUniqueId(), ArgumentUtil.skipFirst(args));
        if (player.isOnline()) PermissionUtil.applyPermissions(player.getPlayer());

        return PermissifyConstants.PERMISSION_REMOVED_FROM_PLAYER.replace("<PLAYER>", player.getName());
    }

    private String timedPermission(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_PLAYER_PERMISSIONS_TIMED)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length < 3) return PermissifyConstants.NOT_ENOUGH_ARGS_PLAYER_PERMISSION_TIMED;

        String playerName = args[0];
        String maybeTime = args[1];

        Optional<Integer> time = MiscUtil.isInt(maybeTime);
        if (!time.isPresent()) return PermissifyConstants.THAT_IS_NOT_TIME;

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);

        if (player == null || !player.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;

        PermissifyMain plugin = PermissifyMain.getInstance();

        Optional<DatabaseHandler> handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;

        handler.get().addPermission(player.getUniqueId(), ArgumentUtil.skipFirst(args));
        if (player.isOnline()) PermissionUtil.applyPermissions(player.getPlayer());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // TODO: This permission will persist if the server restarts.
            handler.get().addPermission(player.getUniqueId(), ArgumentUtil.skipFirst(args));
            if (player.isOnline()) PermissionUtil.applyPermissions(player.getPlayer());
        }, time.get());

        return PermissifyConstants.PERMISSION_REMOVED_FROM_PLAYER.replace("<PLAYER>", player.getName());
    }

    private String groupsSubCommand(CommandSender sender, String[] args) {
        return "";
    }
}
