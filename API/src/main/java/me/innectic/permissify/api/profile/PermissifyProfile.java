package me.innectic.permissify.api.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Innectic
 * @since 8/26/2017
 */
@AllArgsConstructor
public class PermissifyProfile implements Serializable {
    @Getter private final List<PermissionGroup> groups;
    @Getter private Map<UUID, List<Permission>> playerPermissions;
    @Getter private Optional<PermissionGroup> defaultGroup;
    @Getter private String chatFormat;
    @Getter private String whisperFormat;
    @Getter private List<UUID> superAdmins;
}
