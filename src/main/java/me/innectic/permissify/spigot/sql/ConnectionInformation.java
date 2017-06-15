package me.innectic.permissify.spigot.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 6/8/2017
 *
 * Information used to connect to a database handler
 */
@AllArgsConstructor
public class ConnectionInformation {
    @Getter private String url;
    @Getter private int port;
    @Getter private String table;
    @Getter private String username;
    @Getter private String password;
}
