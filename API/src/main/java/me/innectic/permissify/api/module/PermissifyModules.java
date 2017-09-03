package me.innectic.permissify.api.module;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 9/2/2017
 */
@AllArgsConstructor
public enum PermissifyModules {
    CHAT("chat");

    @Getter private String registryName;
}
