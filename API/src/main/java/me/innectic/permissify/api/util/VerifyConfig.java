package me.innectic.permissify.api.util;

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
     * @return if the config is valid
     */
    boolean verifyConnectionInformation();
}
