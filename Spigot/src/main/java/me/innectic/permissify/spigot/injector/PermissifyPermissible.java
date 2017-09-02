package me.innectic.permissify.spigot.injector;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;

/**
 * @author Innectic
 * @since 9/2/2017
 */
public class PermissifyPermissible extends PermissibleBase {

    private final Player owner;
    private PermissibleBase before;

    public PermissifyPermissible(Player owner) {
        super(owner);
        this.owner = owner;
    }
//
//    @Override
//    public boolean isPermissionSet(String permission) {
//    }
}
