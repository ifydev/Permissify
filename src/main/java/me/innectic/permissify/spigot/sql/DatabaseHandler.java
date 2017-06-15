package me.innectic.permissify.spigot.sql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.innectic.permissify.spigot.permission.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Innectic
 * @since 6/8/2017
 *
 * The base database type.
 */
public abstract class DatabaseHandler {

    /**
     * Cached permissions from the database.
     */
    @Getter protected Map<UUID, List<Permission>> cachedPermissions = new HashMap<>();

    /**
     * Initialize the database handler
     */
    public abstract void initialize();

    /**
     * Connect to the database
     *
     * @param connectionInformation the information needed to connect to the database
     * @return if the connection was successful or not
     */
    public abstract boolean connect(ConnectionInformation connectionInformation);

    /**
     * Add a permission to a player
     *
     * @param uuid        the UUID of the player to add the permissions to
     * @param permissions the permissions to add to a player
     */
    public abstract void addPermission(UUID uuid, Permission... permissions);

    /**
     * Remove permissions from a player
     *
     * @param uuid        the uuid of the player to remove the permissions from
     * @param permissions the permissions to remove
     */
    public abstract void removePermission(UUID uuid, Permission... permissions);

    /**
     * Does a player have a permission?
     *
     * @param uuid       the uuid of the player to check
     * @param permission the permission to check
     * @return if the player has the permission
     */
    public abstract boolean hasPermission(UUID uuid, Permission permission);
}
