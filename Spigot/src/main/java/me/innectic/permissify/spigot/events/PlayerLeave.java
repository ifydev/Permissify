package me.innectic.permissify.spigot.events;

import me.innectic.permissify.spigot.PermissifyMain;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Innectic
 * @since 07/21/2018
 */
public class PlayerLeave implements Listener {

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin == null) return;

        if (e.getPlayer() == null) return;
        Player player = e.getPlayer();

        plugin.getAttachmentManager().removeAttachment(player.getUniqueId());
    }
}
