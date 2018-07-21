package me.innectic.permissify.spigot.utils;

import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Innectic
 * @since 07/21/2018
 */
public class AttachmentManager {

    private Map<UUID, Map<String, PermissionAttachment>> attachments = new HashMap<>();

    public Optional<PermissionAttachment> getAttachment(UUID uuid, Optional<String> groupName) {
        return Optional.ofNullable(attachments.getOrDefault(uuid, new HashMap<>()).getOrDefault(groupName.orElse(""), null));
    }

    public void setAttachment(UUID uuid, PermissionAttachment attachment, Optional<String> groupName) {
        Map<String, PermissionAttachment> attachments = new HashMap<>();
        attachments.put(groupName.orElse(""), attachment);
        this.attachments.put(uuid, attachments);
    }

    public void removeAttachment(UUID uuid) {
        attachments.remove(uuid);
    }
}
