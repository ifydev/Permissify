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
@Getter
public class PlayerGroupAddEvent extends Event {

    private HandlerList handlers = new HandlerList();

    private final OfflinePlayer player;
    private final PermissionGroup toGroup;
    private final Optional<PermissionGroup> fromGroup;
}
