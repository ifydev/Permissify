package me.innectic.permissify.spigot.commands.permissify;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.commands.CommandResponse;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class CacheCommand {

    public CommandResponse handleCache(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin((Player) sender, PermissifyConstants.PERMISSIFY_CACHE))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args[0].equalsIgnoreCase("purge")) return handleCachePurge(sender, ArgumentUtil.getRemainingArgs(1, args));
        return new CommandResponse(PermissifyConstants.INVALID_ARGUMENT.replace("<ARGUMENT>", args[0]), false);
    }

    private CommandResponse handleCachePurge(CommandSender sender, String[] args) {
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_OTHER.replace("<REASON>", "No database handler"), false);
        PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get().clear();
        return new CommandResponse(PermissifyConstants.CACHE_PURGED, true);
    }
}
