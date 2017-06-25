package me.innectic.permissify.spigot.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Innectic
 * @since 6/24/2017
 */
@AllArgsConstructor
public class CommandResponse {
    @Getter private final String response;
    @Getter private final boolean succeeded;
}
