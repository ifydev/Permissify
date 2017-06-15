package me.innectic.permissify.api.database;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 6/14/2017
 */
@AllArgsConstructor
public enum ConnectionError {
    REJECTED("Connection rejected!");

    @Getter private String display;
}
