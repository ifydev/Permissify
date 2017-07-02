package me.innectic.permissify.api.util;

import me.innectic.permissify.api.PermissifyAPI;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.permission.PermissionGroup;

import java.util.List;
import java.util.UUID;

/**
 * @author Innectic
 * @since 7/1/2017
 */
public class ChatFormatter {

    /**
     * Format a chat message into the requested format.
     *
     * @param uuid     the uuid of the player who sent
     * @param username the username of the player who sent
     * @param message  the message that was sent
     * @return         the formatted string
     */
    public static String formatChat(UUID uuid, String username, String message) {
        if (!PermissifyAPI.get().isPresent()) return username + ": " + message;
        if (!PermissifyAPI.get().get().getDatabaseHandler().isPresent()) return username + ": " + message;
        DatabaseHandler handler = PermissifyAPI.get().get().getDatabaseHandler().get();

        // TODO: Do something about primary groups that would be the "display" group
        List<PermissionGroup> group = handler.getGroups(uuid);
        String formatter = handler.getChatFormat();
        return formatter.replace("{group}", group.get(0).getName())
                .replace("{username}", username).replace("{message}", message);
    }
}
