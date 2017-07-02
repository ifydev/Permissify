package me.innectic.permissify.api.util;

import me.innectic.permissify.api.PermissifyAPI;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.permission.PermissionGroup;

import java.util.List;
import java.util.Optional;
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
        System.out.println("A");
        if (!PermissifyAPI.get().get().getDatabaseHandler().isPresent()) return username + ": " + message;
        System.out.println("b");
        DatabaseHandler handler = PermissifyAPI.get().get().getDatabaseHandler().get();

        Optional<PermissionGroup> group = handler.getPrimaryGroup(uuid);
        if (!group.isPresent()) return username + ": " + message;
        System.out.println("c");
        String formatter = handler.getChatFormat(false);
        String finalGroup = group.get().getPrefix() + " " + group.get().getName() + " " + group.get().getSuffix();
        String color = "&" + group.get().getChatColor();
        return formatter.replace("{group}", finalGroup)
                .replace("{username}", username).replace("{message}", color + message);
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

        Optional<PermissionGroup> senderGroup = handler.getPrimaryGroup(senderUuid);
        if (!senderGroup.isPresent()) return senderName + " > " + receiverName + ": " + message;
        Optional<PermissionGroup> receiverGroup = handler.getPrimaryGroup(senderUuid);
        if (!receiverGroup.isPresent()) return senderName + " > " + receiverName + ": " + message;
        String formatter = handler.getWhisperFormat(false);
        String sender = senderGroup.get().getPrefix() + " " + senderGroup.get().getName() + " " + senderGroup.get().getSuffix();
        String receiver = receiverGroup.get().getPrefix() + " " + receiverGroup.get().getName() + " " + receiverGroup.get().getSuffix();
        String color = "&" + senderGroup.get().getChatColor();
        return formatter.replace("{senderGroup}", sender)
                .replace("{username}", senderName).replace("{message}", color + message)
                .replace("{to}", receiverName).replace("{receiverGroup}", receiver);
    }
}
