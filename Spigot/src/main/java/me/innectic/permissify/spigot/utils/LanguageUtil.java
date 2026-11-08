package me.innectic.permissify.spigot.utils;

import me.innectic.permissify.api.language.Language;
import me.innectic.permissify.api.language.LanguageManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LanguageUtil {

    public static Language getLanguage(LanguageManager manager, CommandSender sender) {
        return manager.getLanguage(sender instanceof Player player ? player.getLocale() : null);
    }
}
