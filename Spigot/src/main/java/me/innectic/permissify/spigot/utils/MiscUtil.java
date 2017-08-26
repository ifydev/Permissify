package me.innectic.permissify.spigot.utils;

/**
 * @author Innectic
 * @since 8/26/2017
 */
public class MiscUtil {

    public static boolean isInt(String checking) {
        try {
            Integer.parseInt(checking);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
