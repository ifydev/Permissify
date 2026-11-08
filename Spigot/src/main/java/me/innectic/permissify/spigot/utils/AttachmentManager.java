package me.innectic.permissify.spigot.utils;

import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * @author Innectic
 * @since 07/21/2018
 */
public class AttachmentManager {

    private static final Map<UUID, Map<String, PermissionAttachment>> attachments = new HashMap<>();

    public Optional<PermissionAttachment> getAttachment(UUID uuid, Optional<String> groupName) {
        return Optional.ofNullable(attachments.getOrDefault(uuid, new HashMap<>()).getOrDefault(groupName.orElse(""), null));
    }

    public void setAttachment(UUID uuid, PermissionAttachment attachment, Optional<String> groupName) {
        attachments.computeIfAbsent(uuid, key -> new HashMap<>()).put(groupName.orElse(""), attachment);
    }

    public void removeAttachment(UUID uuid) {
        attachments.remove(uuid);
    }

    public Collection<PermissionAttachment> getAllAttachmentsForPlayer(UUID uuid) {
        return List.copyOf(attachments.getOrDefault(uuid, Map.of()).values());
    }

    public void resetAllPermissibles(UUID uuid) {
        Map<String, PermissionAttachment> current = attachments.remove(uuid);
        if (current != null) current.values().forEach(PermissionAttachment::remove);
    }
}