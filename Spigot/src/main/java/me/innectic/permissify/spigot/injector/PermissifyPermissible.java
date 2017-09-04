package me.innectic.permissify.spigot.injector;

import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.spigot.PermissifyMain;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;

import java.util.Optional;

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

    @Override
    public boolean isPermissionSet(String permission) {
        Optional<DatabaseHandler> database = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler();
        return database.map(databaseHandler -> databaseHandler.hasPermission(owner.getUniqueId(), permission)).orElse(false);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        Optional<DatabaseHandler> database = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler();
        return database.map(databaseHandler -> databaseHandler.hasPermission(owner.getUniqueId(), permission.getName()))
                .orElse(permission.getDefault().getValue(isOp()));
    }

    @Override
    public boolean hasPermission(String permission) {
        Optional<DatabaseHandler> database = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler();
        return database.map(databaseHandler -> databaseHandler.hasPermission(owner.getUniqueId(), permission)).orElse(false);
    }

    @Override
    public boolean hasPermission(Permission permission) {
        Optional<DatabaseHandler> database = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler();
        return database.map(databaseHandler -> databaseHandler.hasPermission(owner.getUniqueId(), permission.getName()))
                .orElse(permission.getDefault().getValue(isOp()));
    }

    @Override
    public void setOp(boolean isOp) {
        owner.setOp(isOp);
    }
}
