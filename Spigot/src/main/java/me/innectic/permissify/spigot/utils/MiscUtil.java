package me.innectic.permissify.spigot.utils;

import org.bukkit.Bukkit;

/**
 * @author Innectic
 * @since 8/26/2017
 */
public class MiscUtil {

    private static String version;

    static {
        Class serverClass = Bukkit.getServer().getClass();
        if (!serverClass.getSimpleName().equals("CraftServer")) {
            version = null;
        } else if (serverClass.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
            version = ".";
        } else {
            String name = serverClass.getName();
            name = name.substring("org.bukkit.craftbukkit".length());
            name = name.substring(0, name.length() - "CraftServer".length());
            version = name;
        }
    }

    public static boolean isInt(String checking) {
        try {
            Integer.parseInt(checking);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Class getBukkitClass(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit" + version + name);
        } catch (ClassNotFoundException ignored) {}
        return null;
    }
}
