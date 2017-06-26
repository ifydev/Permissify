package me.innectic.permissify.spigot.events;

import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.spigot.PermissifyMain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

/**
 * @author Innectic
 * @since 6/26/2017
 */
public class PlayerChat implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (!plugin.getPermissifyAPI().getDatabaseHandler().isPresent()) return;
        String originalMessage = e.getMessage();
        Player player = e.getPlayer();
        if (player == null || originalMessage == null) return;
        Optional<PermissionGroup> group = plugin.getPermissifyAPI().getDatabaseHandler().get().getGroups().stream().filter(permissionGroup -> permissionGroup.hasPlayer(player.getUniqueId())).findFirst();
        group.ifPresent(permissionGroup ->
                e.setFormat(permissionGroup.getPrefix() + player.getName() + permissionGroup.getSuffix() + permissionGroup.getChatColor()));
    }
}
