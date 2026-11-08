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

    private record HelpEntry(String category, String action, String key) {}

    private static final List<HelpEntry> ADDITIONS = List.of(
            new HelpEntry("group", "weight", "HELP_GROUP_WEIGHT"),
            new HelpEntry("group", "setpermission", "HELP_GROUP_SET_PERMISSION"),
            new HelpEntry("group", "unsetpermission", "HELP_GROUP_UNSET_PERMISSION"),
            new HelpEntry("player", "setpermission", "HELP_PLAYER_SET_PERMISSION"),
            new HelpEntry("player", "unsetpermission", "HELP_PLAYER_UNSET_PERMISSION"),
            new HelpEntry("player", "check", "HELP_PLAYER_CHECK"),
            new HelpEntry("log", "", "HELP_LOG"),
            new HelpEntry("group", "inherit", "HELP_GROUP_INHERIT"),
            new HelpEntry("group", "uninherit", "HELP_GROUP_UNINHERIT"),
            new HelpEntry("player", "promote", "HELP_PLAYER_PROMOTE"),
            new HelpEntry("player", "demote", "HELP_PLAYER_DEMOTE"),
            new HelpEntry("track", "create", "HELP_TRACK_CREATE"),
            new HelpEntry("track", "append", "HELP_TRACK_APPEND"),
            new HelpEntry("track", "remove", "HELP_TRACK_REMOVE"),
            new HelpEntry("track", "info", "HELP_TRACK_INFO"),
            new HelpEntry("track", "list", "HELP_TRACK_LIST"),
            new HelpEntry("track", "delete", "HELP_TRACK_DELETE"));
    static List<List<String>> getPages(CommandSender sender, Language language) {
        List<String> entries = new ArrayList<>(language.getPages("PERMISSIFY_HELP_PAGES").stream().flatMap(List::stream).toList());
        for (HelpEntry entry : ADDITIONS) {
            boolean exists = entries.stream().anyMatch(line -> {
                var command = COMMAND.matcher(ChatColor.stripColor(ColorUtil.makeReadable(line)));
                return command.find() && command.group(1).equalsIgnoreCase(entry.category()) && (entry.category().equals("log") || entry.action().equalsIgnoreCase(command.group(2)));
            });
            if (!exists) entries.add(language.getMessage(entry.key()));
        }
        List<String> lines = entries.stream().filter(line -> canShow(sender, line)).toList();
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
