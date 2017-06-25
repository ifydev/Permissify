package me.innectic.permissify.spigot.commands.permissify;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.commands.CommandResponse;
import org.bukkit.entity.Player;

/**
 * @author Innectic
 * @since 6/25/2017
 */
public class PlayerCommand {

    public CommandResponse handleAddPlayerToGroup(String group, Player player) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_ADD.replace("<REASON>", "No database handler."), false);
        return null;
    }

    public CommandResponse handleRemovePlayerFromGroup(String group, Player player) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_REMOVE.replace("<REASON>", "No database handler."), false);
        return null;
    }

    public CommandResponse handleAddPermission(String permission, Player player) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_REMOVE.replace("<REASON>", "No database handler."), false);
        return null;
    }

    public CommandResponse handleRemovePermission(String permission, Player player) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_REMOVE.replace("<REASON>", "No database handler."), false);
        plugin.getPermissifyAPI().getDatabaseHandler().get().removePermission(player.getUniqueId(), permission);
        return null;
    }
}
