package me.innectic.permissify.sponge.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;

/**
 * @author Innectic
 * @since 6/26/2017
 */
public class PlayerJoinEvent {

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player player) {
    }
}
