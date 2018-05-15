package me.innectic.permissify.spigot.events.custom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.innectic.permissify.api.permission.PermissionGroup;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Optional;

/**
 * @author Innectic
 * @since 5/14/18
 */
@RequiredArgsConstructor
public class PlayerGroupChangeEvent extends Event {

    private static HandlerList handlers = new HandlerList();

    @Getter private final OfflinePlayer player;
    @Getter private final PermissionGroup toGroup;
    @Getter private final Optional<PermissionGroup> fromGroup;

    @Override
    public HandlerList getHandlers() {
        return null;
    }
}
