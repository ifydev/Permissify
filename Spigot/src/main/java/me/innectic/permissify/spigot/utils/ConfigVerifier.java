package me.innectic.permissify.spigot.utils;

import me.innectic.permissify.api.database.handlers.HandlerType;
import me.innectic.permissify.api.database.handlers.MySQLHandler;
import me.innectic.permissify.api.util.VerifyConfig;
import me.innectic.permissify.spigot.PermissifyMain;

import java.util.Optional;

/**
 * @author Innectic
 * @since 6/14/2017
 */
public class ConfigVerifier implements VerifyConfig {
    @Override
    public boolean verifyBasicInformation() {
        PermissifyMain plugin = PermissifyMain.getInstance();
        if (plugin.getConfig() == null) return false;
        if (plugin.getConfig().getString("storage") == null) return false;

        Optional<HandlerType> type = HandlerType.findType(plugin.getConfig().getString("storage"));
        return type.isPresent();
    }

    @Override
    public boolean verifyConnectionInformation() {
        PermissifyMain plugin = PermissifyMain.getInstance();
        Optional<HandlerType> type = HandlerType.findType(plugin.getConfig().getString("storage"));
        if  (!type.isPresent()) return false;

        if (type.get().getHandler() == MySQLHandler.class) {
            if (plugin.getConfig().getString("connection.host") == null) return false;
            if (plugin.getConfig().getString("connection.username") == null) return false;
            if (plugin.getConfig().getString("connection.password") == null) return false;
            if (plugin.getConfig().getString("connection.table") == null) return false;
        }
        return false;
    }
}
