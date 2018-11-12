package me.innectic.permissify.spigot.commands.subcommands;

import org.bukkit.command.CommandSender;

/**
 * @author Innectic
 * @since 11/11/2018
 */
public interface AbstractSubCommand {

    String handle(CommandSender sender, String[] args);
}
