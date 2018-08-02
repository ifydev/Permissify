package me.innectic.permissify.spigot.utils;

import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * @author Innectic
 * @since 07/21/2018
 */
public class AttachmentManager {

    private static Map<UUID, Map<String, PermissionAttachment>> attachments = new HashMap<>();

    public Optional<PermissionAttachment> getAttachment(UUID uuid, Optional<String> groupName) {
        return Optional.ofNullable(attachments.getOrDefault(uuid, new HashMap<>()).getOrDefault(groupName.orElse(""), null));
    }

    public void setAttachment(UUID uuid, PermissionAttachment attachment, Optional<String> groupName) {
        Map<String, PermissionAttachment> attachments = new HashMap<>();
        attachments.put(groupName.orElse(""), attachment);
        AttachmentManager.attachments.put(uuid, attachments);
    }

    public void removeAttachment(UUID uuid) {
        attachments.remove(uuid);
    }

    public Collection<PermissionAttachment> getAllAttachmentsForPlayer(UUID uuid) {
        return attachments.get(uuid).values();
    }

    public void resetAllPermissibles(UUID uuid) {
        attachments.get(uuid).values().forEach(attachment -> attachment.getPermissions().keySet().forEach(attachment::unsetPermission));
        attachments.remove(uuid);
    }
}
