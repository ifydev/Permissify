/*
*
* This file is part of Permissify, licensed under the MIT License (MIT).
* Copyright (c) Innectic
* Copyright (c) contributors
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
 */
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
        if (!PermissifyAPI.get().get().getDatabaseHandler().isPresent()) return username + ": " + message;
        DatabaseHandler handler = PermissifyAPI.get().get().getDatabaseHandler().get();

        Optional<PermissionGroup> group = handler.getPrimaryGroup(uuid);
        if (!group.isPresent()) return username + ": " + message;
        String formatter = handler.getChatFormat(false);
        String finalGroup = group.get().getPrefix() + group.get().getName() + group.get().getSuffix();
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
