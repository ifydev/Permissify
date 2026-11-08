package me.innectic.permissify.spigot.commands.subcommand;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.language.Language;
import me.innectic.permissify.api.permission.PermissionDecision;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.DurationUtil;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import me.innectic.permissify.spigot.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PermissionCommand {

    private final DatabaseHandler handler;

    public PermissionCommand(PermissifyMain plugin) {
        handler = plugin.getPermissifyAPI().getDatabaseHandler().orElseThrow();
    }

    public String handleWeight(CommandSender sender, String[] args, Language language) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_GROUP_WEIGHT))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");
        if (args.length < 1 || args.length > 2) return language.getMessage("HELP_GROUP_WEIGHT");
        var group = handler.getGroup(args[0]).orElse(null);
        if (group == null) return language.format("INVALID_GROUP", Map.of("GROUP", args[0]));
        if (args.length == 2) {
            int weight;
            try {
                weight = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                return language.format("INVALID_ARGUMENT", Map.of("ARGUMENT", args[1]));
            }
            if (!handler.setGroupWeight(group.getName(), weight)) return failure(language);
            Bukkit.getOnlinePlayers().forEach(PermissionUtil::applyPermissions);
        }
        return language.format("GROUP_WEIGHT", Map.of("GROUP", group.getName(), "WEIGHT", group.getWeight()));
    }

    public String handleSetPermission(CommandSender sender, String[] args, Language language, boolean groupRule) {
        String permission = groupRule ? PermissifyConstants.PERMISSIFY_GROUP_PERMISSION_SET : PermissifyConstants.PERMISSIFY_PLAYER_PERMISSION_SET;
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, permission)) return language.getMessage("INSUFFICIENT_PERMISSIONS");
        if (args.length < 3 || args.length > (groupRule ? 3 : 4))
            return language.getMessage(groupRule ? "HELP_GROUP_SET_PERMISSION" : "HELP_PLAYER_SET_PERMISSION");
        if (!args[2].equalsIgnoreCase("true") && !args[2].equalsIgnoreCase("false"))
            return language.format("INVALID_ARGUMENT", Map.of("ARGUMENT", args[2]));
        boolean granted = Boolean.parseBoolean(args[2]);
        String node = args[1].toLowerCase(Locale.ROOT);
        String target;
        if (groupRule) {
            var group = handler.getGroup(args[0]).orElse(null);
            if (group == null) return language.format("INVALID_GROUP", Map.of("GROUP", args[0]));
            target = group.getName();
            if (!handler.setGroupPermission(target, node, granted)) return failure(language);
            Bukkit.getOnlinePlayers().forEach(PermissionUtil::applyPermissions);
        } else {
            OfflinePlayer player = PlayerUtil.findPlayer(args[0]);
            if (player == null) return language.getMessage("INVALID_PLAYER");
            long expiry = 0;
            if (args.length == 4) {
                try {
                    expiry = DurationUtil.expiresAt(args[3]);
                } catch (IllegalArgumentException | ArithmeticException e) {
                    return language.format("INVALID_ARGUMENT", Map.of("ARGUMENT", args[3]));
                }
            }
            target = player.getName();
            if (!handler.setPermission(player.getUniqueId(), node, granted, expiry)) return failure(language);
            if (player.isOnline()) PermissionUtil.applyPermissions(player.getPlayer());
        }
        return language.format("PERMISSION_SET", Map.of("TARGET", target, "PERMISSION", node, "VALUE", language.getMessage(granted ? "RULE_ALLOW" : "RULE_DENY")));
    }

    public String handleCheck(CommandSender sender, String[] args, Language language) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_PLAYER_CHECK))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");
        if (args.length != 2) return language.getMessage("HELP_PLAYER_CHECK");
        OfflinePlayer player = PlayerUtil.findPlayer(args[0]);
        if (player == null) return language.getMessage("INVALID_PLAYER");
        String node = args[1].toLowerCase(Locale.ROOT);
        PermissionDecision decision = handler.resolvePermissions(player.getUniqueId()).get(node);
        List<String> lines = new ArrayList<>();
        lines.add(language.format("CHECK_HEADER", Map.of("PLAYER", player.getName(), "PERMISSION", node)));
        if (decision == null) {
            lines.add(language.getMessage("CHECK_UNSET"));
        } else {
            lines.add(language.format("CHECK_RULE", Map.of("VALUE", language.getMessage(decision.granted() ? "RULE_ALLOW" : "RULE_DENY"))));
            if (decision.membership() == null) lines.add(language.getMessage("CHECK_DIRECT"));
            else lines.add(language.format("CHECK_GROUP", Map.of("GROUP", decision.membership(), "WEIGHT", decision.weight(),
                    "PATH", String.join(" -> ", decision.path()))));
            lines.add(language.format("CHECK_EXPIRY", Map.of("EXPIRY", decision.expiresAt() == 0
                    ? language.getMessage("EXPIRY_NEVER") : Instant.ofEpochMilli(decision.expiresAt()).toString())));
        }
        var main = handler.getMainGroup(player.getUniqueId());
        if (decision == null && main.isPresent() && node.equalsIgnoreCase("group." + main.get().getName()))
            lines.add(language.format("CHECK_GROUP_NODE", Map.of("GROUP", main.get().getName())));
        lines.add(player.isOnline() ? language.format("CHECK_ACTUAL", Map.of("VALUE",
                language.getMessage(player.getPlayer().hasPermission(node) ? "RULE_ALLOW" : "RULE_DENY"))) : language.getMessage("CHECK_OFFLINE"));
        lines.add(language.format("CHECK_MAIN", Map.of("GROUP", main.map(group -> group.getName())
                .orElse(language.getMessage("EMPTY_DEFAULT_GROUP_NAME")))));
        return String.join("\n", lines);
    }

    public String handleLog(CommandSender sender, String[] args, Language language) {
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, PermissifyConstants.PERMISSIFY_LOG))
            return language.getMessage("INSUFFICIENT_PERMISSIONS");
        String query = "";
        int page = 1;
        try {
            if (args.length > 0 && args[0].equalsIgnoreCase("search")) {
                if (args.length < 2 || args.length > 3) return language.getMessage("HELP_LOG");
                query = args[1];
                if (args.length == 3) page = Integer.parseInt(args[2]);
            } else {
                if (args.length > 1) return language.getMessage("HELP_LOG");
                if (args.length == 1) page = Integer.parseInt(args[0]);
            }
            if (page < 1 || page > Integer.MAX_VALUE / 10) return language.getMessage("HELP_LOG");
        } catch (NumberFormatException e) {
            return language.getMessage("HELP_LOG");
        }
        var entries = handler.getAuditLog(query, page);
        if (entries.isEmpty()) return language.getMessage("LOG_EMPTY");
        List<String> lines = new ArrayList<>();
        lines.add(language.format("LOG_HEADER", Map.of("PAGE", page)));
        entries.forEach(entry -> lines.add(language.format("LOG_ENTRY", Map.of("ID", entry.id(),
                "TIME", Instant.ofEpochMilli(entry.timestamp()), "ACTOR", entry.actor(), "TYPE", entry.type(),
                "ACTION", entry.action(), "TARGET", entry.target(), "DETAILS", entry.details(), "COMMAND", entry.command()))));
        return String.join("\n", lines);
    }

    private String failure(Language language) {
        return language.format("UNABLE_OTHER", Map.of("REASON", language.getMessage("DATABASE_UNAVAILABLE")));
    }
}
