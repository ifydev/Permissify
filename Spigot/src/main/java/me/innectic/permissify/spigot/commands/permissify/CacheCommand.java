package me.innectic.permissify.spigot.commands.permissify;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.commands.CommandResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class CacheCommand {

    public CommandResponse handleCache(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PermissifyConstants.PERMISSIFY_CACHE))
            return new CommandResponse(PermissifyConstants.INSUFFICIENT_PERMISSIONS, false);
        if (args[0].equalsIgnoreCase("clear")) return handleCacheClear(sender, ArgumentUtil.getRemainingArgs(1, args));
        return new CommandResponse(PermissifyConstants.INVALID_ARGUMENT.replace("<ARGUMENT>", args[0]), false);
    }

    private CommandResponse handleCacheClear(CommandSender sender, String[] args) {
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_OTHER.replace("<REASON>", "No database handler"), false);
        return new CommandResponse(PermissifyConstants.CACHE_CLEARED, true);
    }
}
