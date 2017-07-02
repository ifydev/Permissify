package me.innectic.permissify.spigot.utils;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.database.ConnectionError;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class DisplayUtil implements me.innectic.permissify.api.util.DisplayUtil {

    @Override
    public void displayError(ConnectionError error, Optional<Exception> exception) {
        List<String> messages = PermissifyConstants.PERMISSIFY_ERROR.stream().map(ColorUtil::makeReadable).collect(Collectors.toList());
        List<Player> players = Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission(PermissifyConstants.PERMISSIFY_ADMIN)).collect(Collectors.toList());
        messages.forEach(message -> players.forEach(player -> player.sendMessage(message)));
    }
}
