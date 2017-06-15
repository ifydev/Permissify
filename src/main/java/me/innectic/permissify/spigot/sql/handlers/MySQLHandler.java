package me.innectic.permissify.spigot.sql.handlers;

import me.innectic.permissify.spigot.permission.Permission;
import me.innectic.permissify.spigot.sql.ConnectionInformation;
import me.innectic.permissify.spigot.sql.DatabaseHandler;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Innectic
 * @since 6/8/2017
 */
public class MySQLHandler extends DatabaseHandler {

    @Override
    public void initialize() {
        // Make sure that the cache is empty
        this.cachedPermissions = new HashMap<>();
    }

    @Override
    public boolean connect(ConnectionInformation connectionInformation) {
        return false;
    }

    @Override
    public void addPermission(UUID uuid, Permission... permissions) {

    }

    @Override
    public void removePermission(UUID uuid, Permission... permissions) {

    }

    @Override
    public boolean hasPermission(UUID uuid, Permission permission) {
        return false;
    }
}
