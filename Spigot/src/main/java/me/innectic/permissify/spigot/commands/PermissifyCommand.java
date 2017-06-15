package me.innectic.permissify.spigot.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Innectic
 * @since 6/15/2017
 */
public class PermissifyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 2) {
            helpMenu(sender);
            return false;
        }
        if (args[0].equals("group")) {
            if (args[0].equals("create")) {
                return true;
            } else if (args[1].equalsIgnoreCase("addpermission")) {
                return true;
            } else if (args[1].equalsIgnoreCase("removepermission")) {
                return true;
            } else if (args[1].equalsIgnoreCase("listpermission")) {
                return true;
            } else if (args[1].equalsIgnoreCase("list")) {
                return true;
            }
            helpMenu(sender);
        }
        return false;
    }

    private void helpMenu(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "=============== " + ChatColor.GREEN + ChatColor.BOLD + "Permissify Help " + ChatColor.YELLOW + "===============");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group create [name] [prefix] [suffix] [chat-color]");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group addpermission [name] [permission]");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group removepermission [name] [permission]");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group listpermission [name]");
        sender.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "/permissify group list");
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "===============================================");
    }
}
