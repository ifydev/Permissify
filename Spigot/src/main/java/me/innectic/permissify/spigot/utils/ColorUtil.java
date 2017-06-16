package me.innectic.permissify.spigot.utils;

import org.bukkit.ChatColor;

/**
 * @author Innectic
 * @since 6/15/2017
 */
public class ColorUtil {

    /**
     * Is a string a valid chat color?
     *
     * @param check the string to check
     * @return if the string is a valid chatcolor
     */
    public static boolean isValidChatColor(String check) {
        for (ChatColor color : ChatColor.values()) {
            if (color.toString().equals(check)) return true;
        }
        return false;

    }
}
