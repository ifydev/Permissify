package me.innectic.permissify.spigot.sql.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.innectic.permissify.spigot.sql.DatabaseHandler;

/**
 * @author Innectic
 * @since 6/8/2017
 */
@AllArgsConstructor
public enum HandlerType {

    MYSQL(MySQLHandler.class, "MySQL");

    @Getter private Class<? extends DatabaseHandler> handler;
    @Getter private String displayName;
}
