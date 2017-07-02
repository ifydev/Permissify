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

    /**
     * Format a whisper message
     *
     * @param senderUuid   the uuid of the player who sent it
     * @param senderName   the name of the player who sent it
     * @param receiverUuid the uuid of the receiver of the message
     * @param receiverName the name of the receiver of the message
     * @param message      the message sent
     * @return             the final formatted message
     */
    public static String formatWhisper(UUID senderUuid, String senderName, UUID receiverUuid, String receiverName, String message) {
        if (!PermissifyAPI.get().isPresent()) return senderName + " > " + receiverName + ": " + message;
        if (!PermissifyAPI.get().get().getDatabaseHandler().isPresent()) return senderName + " > " + receiverName + ": " + message;
        DatabaseHandler handler = PermissifyAPI.get().get().getDatabaseHandler().get();

        // TODO: Do something about primary groups that would be the "display" group
        List<PermissionGroup> senderGroups = handler.getGroups(senderUuid);
        List<PermissionGroup> receiverGroups = handler.getGroups(receiverUuid);
        String formatter = handler.getWhisperFormat();
        return formatter.replace("{senderGroup}", senderGroups.get(0).getName())
                .replace("{username}", senderName).replace("{message}", message)
                .replace("{to}", receiverName).replace("{receiverGroup}", receiverGroups.get(0).getName());
    }
}
