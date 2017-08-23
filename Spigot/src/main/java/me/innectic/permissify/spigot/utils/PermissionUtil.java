package me.innectic.permissify.spigot.utils;

import me.innectic.permissify.spigot.PermissifyMain;
import org.bukkit.entity.Player;

/**
 * @author Innectic
 * @since 8/21/2017
 */
public class PermissionUtil {

    public static boolean hasPermissionOrSuperAdmin(Player player, String permission) {
        return player.hasPermission(permission) ||
                (PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().map(handler ->
                        handler.isSuperAdmin(player.getUniqueId())).orElse(false));
    }
}
