package me.innectic.permissify.spigot.commands;

import me.innectic.permissify.api.language.Language;
import me.innectic.permissify.spigot.utils.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class PermissifyHelp {

    private static final int LINES_PER_PAGE = 10;
    private static final Pattern COMMAND = Pattern.compile("/(?:permissify|perm)\\s+(\\S+)(?:\\s+(\\S+))?", Pattern.CASE_INSENSITIVE);

    static List<List<String>> getPages(CommandSender sender, Language language) {
        List<String> lines = language.getPages("PERMISSIFY_HELP_PAGES").stream().flatMap(List::stream)
                .filter(line -> canShow(sender, line)).toList();
        if (lines.isEmpty()) return List.of(List.of(language.getMessage("HELP_NO_COMMANDS")));
        List<List<String>> pages = new ArrayList<>();
        for (int start = 0; start < lines.size(); start += LINES_PER_PAGE)
            pages.add(List.copyOf(lines.subList(start, Math.min(start + LINES_PER_PAGE, lines.size()))));
        return List.copyOf(pages);
    }

    private static boolean canShow(CommandSender sender, String line) {
        var command = COMMAND.matcher(ChatColor.stripColor(ColorUtil.makeReadable(line)));
        if (!command.find()) return false;
        return CommandAccess.canUse(sender, command.group(1), command.group(2) == null ? "" : command.group(2));
    }
}
