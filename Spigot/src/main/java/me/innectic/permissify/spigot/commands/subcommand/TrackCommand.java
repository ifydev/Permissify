package me.innectic.permissify.spigot.commands.subcommand;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.language.Language;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.permission.RankTrack;
import me.innectic.permissify.api.util.Tristate;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Locale;

public class TrackCommand {

    public String handleTrack(CommandSender sender, String[] args, Language language) {
        if (args.length == 0) return language.getMessage("USAGE_TRACK");
        String action = args[0].toLowerCase(Locale.ROOT);
        String permission = switch (action) {
            case "create" -> PermissifyConstants.PERMISSIFY_TRACK_CREATE;
            case "delete" -> PermissifyConstants.PERMISSIFY_TRACK_DELETE;
            case "append" -> PermissifyConstants.PERMISSIFY_TRACK_APPEND;
            case "remove" -> PermissifyConstants.PERMISSIFY_TRACK_REMOVE;
            case "info" -> PermissifyConstants.PERMISSIFY_TRACK_INFO;
            case "list" -> PermissifyConstants.PERMISSIFY_TRACK_LIST;
            default -> null;
        };
        if (permission == null) return language.getMessage("INVALID_ARGUMENT").replace("<ARGUMENT>", args[0]);
        if (!PermissionUtil.hasPermissionOrSuperAdmin(sender, permission)) return language.getMessage("INSUFFICIENT_PERMISSIONS");
        var handler = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().orElseThrow();
        if (action.equals("list")) {
            if (args.length != 1) return language.getMessage("USAGE_TRACK_LIST");
            return language.getMessage("TRACK_LIST").replace("<TRACKS>", String.join(", ", handler.getTracks().keySet()));
        }
        int expected = action.equals("append") || action.equals("remove") ? 3 : 2;
        if (args.length != expected) return language.getMessage("USAGE_TRACK_" + action.toUpperCase(Locale.ROOT));
        if (action.equals("create")) {
            if (!args[1].matches("[a-zA-Z0-9_-]{1,100}")) return language.getMessage("INVALID_ARGUMENT").replace("<ARGUMENT>", args[1]);
            Tristate created = handler.createTrack(args[1]);
            if (created == Tristate.FALSE) return failure(language);
            return language.getMessage(created == Tristate.NONE ? "TRACK_EXISTS" : "TRACK_CREATED").replace("<TRACK>", args[1]);
        }
        var found = handler.getTrack(args[1]);
        if (found.isEmpty()) return language.getMessage("TRACK_MOVE_NOT_FOUND").replace("<TRACK>", args[1]);
        RankTrack track = found.get();
        if (action.equals("info")) return language.getMessage("TRACK_INFO").replace("<TRACK>", track.name()).replace("<GROUPS>", String.join(" -> ", track.groups()));
        if (action.equals("delete")) {
            if (handler.deleteTrack(track.name()) != Tristate.TRUE) return failure(language);
            return language.getMessage("TRACK_DELETED").replace("<TRACK>", track.name());
        }
        var group = handler.getGroup(args[2]);
        if (group.isEmpty()) return language.getMessage("INVALID_GROUP").replace("<GROUP>", args[2]);
        PermissionGroup selected = group.get();
        var groups = new ArrayList<>(track.groups());
        if (action.equals("append")) {
            if (groups.contains(selected.getName())) return language.getMessage("TRACK_GROUP_EXISTS").replace("<TRACK>", track.name()).replace("<GROUP>", selected.getName());
            groups.add(selected.getName());
        } else {
            if (!groups.remove(selected.getName())) return language.getMessage("TRACK_GROUP_MISSING").replace("<TRACK>", track.name()).replace("<GROUP>", selected.getName());
        }
        if (!handler.setTrackGroups(track.name(), groups)) return failure(language);
        return language.getMessage("TRACK_UPDATED").replace("<TRACK>", track.name()).replace("<GROUPS>", String.join(" -> ", groups));
    }

    private String failure(Language language) {
        return language.getMessage("UNABLE_TO_SET").replace("<REASON>", language.getMessage("DATABASE_UNAVAILABLE"));
    }
}
