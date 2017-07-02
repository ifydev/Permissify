package me.innectic.permissify.spigot.events;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.api.util.ChatFormatter;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class PreProcess implements Listener {

    private Set<String> validWhisperCommands = new HashSet<>(Arrays.asList("/msg", "/whisper", "/tell"));

    // @Return: Should this command's output not look like permissify ever touches it?

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        String command = e.getMessage();
        if (!validWhisperCommands.contains(command)) return;
        e.setCancelled(true);
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent()) return;
        // We don't care about the command at the beginning
        String[] arguments = ArgumentUtil.getRemainingArgs(1, command.split(" "));
        Player player = Bukkit.getPlayer(arguments[0]);
        if (player == null) {
            e.getPlayer().sendMessage(PermissifyConstants.INVALID_PLAYER);
            return;
        }
        String message = String.join(" ", ArgumentUtil.getRemainingArgs(1, arguments));
        String formatted = ChatFormatter.formatWhisper(e.getPlayer().getUniqueId(), e.getPlayer().getName(), player.getUniqueId(), player.getName(), message);
        player.sendMessage(ColorUtil.makeReadable(formatted));
        e.getPlayer().sendMessage(ColorUtil.makeReadable(formatted));
    }
}
