package me.innectic.permissify.api.util;

import me.innectic.permissify.api.database.handlers.HandlerType;

/**
 * @author Innectic
 * @since 6/14/2017
 */
public interface VerifyConfig {
    /**
     * Verify basic information about the configuration file
     *
     * @return if the config is valid
     */
    boolean verifyBasicInformation();

    /**
     * Verify the connection information for a handler type
     *
     * @param type the type of database to use
     * @return if the config is valid
     */
    boolean verifyConnectionInformation(HandlerType type);
}
