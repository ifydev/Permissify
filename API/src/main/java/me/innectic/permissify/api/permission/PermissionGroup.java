package me.innectic.permissify.api.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 6/14/2017
 */
@AllArgsConstructor
public class PermissionGroup {
    @Getter private String displayName;
    @Getter private String chatColor;
    @Getter private String prefix;
    @Getter private String suffix;
}
