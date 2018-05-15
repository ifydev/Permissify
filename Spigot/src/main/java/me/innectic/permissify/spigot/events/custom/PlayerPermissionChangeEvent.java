package me.innectic.permissify.spigot.events.custom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author Innectic
 * @since 5/14/18
 */
@RequiredArgsConstructor
@Getter
public class PlayerPermissionChangeEvent extends Event {

    private HandlerList handlers = new HandlerList();

    private final OfflinePlayer player;
    private final String permission;
    private final ChangeType type;
}
