package me.innectic.permissify.spigot.commands.subcommands;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * @author Innectic
 * @since 11/09/2018
 */
public class SuperAdminSubCommand implements AbstractSubCommand {

    public String handle(CommandSender sender, String[] args) {
        if (args.length == 0) return PermissifyConstants.NOT_ENOUGH_ARGS_SUPER_ADMIN;

        String segment = args[1].toLowerCase();
        args = ArgumentUtil.skipFirst(args);

        if (segment.equals("grant")) return grantSuperAdmin(sender, args);
        else if (segment.equals("revoke")) return revokeSuperAdmin(sender, args);
        return PermissifyConstants.INVALID_ARGUMENT_SUPER_ADMIN;
    }

    private String grantSuperAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_SUPERADMIN_GRANT)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length == 0) return PermissifyConstants.MUST_PROVIDE_PLAYER;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player == null) return PermissifyConstants.INVALID_PLAYER;
        if (!player.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;
        // Since the player does exist, and has played before we can actually grant.
        Optional<DatabaseHandler> handler = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;
        // We have a handler, so we can make it happen!
        handler.get().addSuperAdmin(player.getUniqueId());
        return PermissifyConstants.PLAYER_ADDED_AS_SUPER_ADMIN.replace("<PLAYER>", player.getName());
    }

    private String revokeSuperAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_SUPERADMIN_REVOKE)) return PermissifyConstants.YOU_DONT_HAVE_PERMISSION;
        if (args.length == 0) return PermissifyConstants.MUST_PROVIDE_PLAYER;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
        if (player == null) return PermissifyConstants.INVALID_PLAYER;
        if (!player.hasPlayedBefore()) return PermissifyConstants.INVALID_PLAYER;
        // Since the player does exist, and has played before we can actually grant.
        Optional<DatabaseHandler> handler = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler();
        if (!handler.isPresent()) return PermissifyConstants.HANDLER_IS_NOT_PRESENT;
        // We have a handler, so we can make it happen!
        handler.get().removeSuperAdmin(player.getUniqueId());
        return PermissifyConstants.PLAYER_REMOVED_FROM_SUPER_ADMIN.replace("<PLAYER>", player.getName());
    }
}
