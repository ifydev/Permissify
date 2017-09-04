package me.innectic.permissify.api.module;

import lombok.Getter;

/**
 * @author Innectic
 * @since 9/2/2017
 */
public abstract class PermissifyModule {

    @Getter private final String moduleName;

    /**
     * Create a new Permissify Module.
     *
     * <p>
     * Modules can be overridden, meaning that internal permissify modules can be overridden by other plugins.
     * The only requirement is that `moduleName` matches up.
     *
     * @param moduleName the name of the module to register
     */
    public PermissifyModule(String moduleName) {
        this.moduleName = moduleName;
    }

    public abstract void initialize();
    public abstract void deinitialize();
}
