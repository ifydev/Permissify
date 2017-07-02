package me.innectic.permissify.api.util;

import me.innectic.permissify.api.database.ConnectionError;

import java.util.Optional;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public interface DisplayUtil {
    /**
     * Display an error to online players with the `permissify.admin` permission.
     *
     * @param error     type type of error thrown.
     * @param exception the exception thrown
     */
    void displayError(ConnectionError error, Optional<Exception> exception);
}
