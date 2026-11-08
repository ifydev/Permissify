package me.innectic.permissify.spigot.commands;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.api.permission.Permission;
import me.innectic.permissify.api.permission.PermissionGroup;
import me.innectic.permissify.api.permission.GroupInheritance;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.utils.LanguageUtil;
import me.innectic.permissify.spigot.utils.PermissionUtil;
import me.innectic.permissify.spigot.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

public class PermissifyTabCompleter implements TabCompleter {

    private final PermissifyMain plugin;

    public PermissifyTabCompleter(PermissifyMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof RemoteConsoleCommandSender && !plugin.getConfig().getBoolean("allow-rcon", false)) return List.of();
        if (args.length == 0 || !allowed(sender, PermissifyConstants.PERMISSIFY_BASIC)) return List.of();
        if (sender instanceof BlockCommandSender && !plugin.getConfig().getBoolean("allow-command-block", false)) return List.of();
        var handler = plugin.getPermissifyAPI().getDatabaseHandler();
        if (handler.isEmpty()) return List.of();
        List<String> options = suggestions(sender, args, handler.get());
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().distinct().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix))
                .sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    private List<String> suggestions(CommandSender sender, String[] args, DatabaseHandler handler) {
        if (args.length == 1) {
            List<String> commands = new ArrayList<>(List.of("help"));
            if (CommandAccess.GROUP_COMMANDS.values().stream().anyMatch(permission -> allowed(sender, permission))) commands.add("group");
            if (CommandAccess.PLAYER_COMMANDS.values().stream().anyMatch(permission -> allowed(sender, permission))) commands.add("player");
            if (allowed(sender, PermissifyConstants.PERMISSIFY_LOG)) commands.add("log");
            if (allowed(sender, PermissifyConstants.PERMISSIFY_CACHE)) commands.add("cache");
            if (allowed(sender, PermissifyConstants.PERMISSIFY_PROFILE)) commands.add("profile");
            if (CommandAccess.TRACK_COMMANDS.values().stream().anyMatch(permission -> allowed(sender, permission))) commands.add("track");
            if (PermissionUtil.isConsole(sender)) commands.add("superadmin");
            return commands;
        }
        String category = args[0].toLowerCase(Locale.ROOT);
        String action = args[1].toLowerCase(Locale.ROOT);
        if (category.equals("group")) {
            if (args.length == 2) return actions(sender, CommandAccess.GROUP_COMMANDS);
            if (action.equals("add")) action = "create";
            if (action.equals("delete")) action = "remove";
            if (!CommandAccess.GROUP_COMMANDS.containsKey(action) || !allowed(sender, CommandAccess.GROUP_COMMANDS.get(action))) return List.of();
            if (action.equals("create")) return args.length == 7 ? List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f") : List.of();
            if (action.equals("list")) return List.of();
            if (args.length == 3) return new ArrayList<>(handler.getGroups().keySet());
            if (handler.getGroup(args[2]).isEmpty()) return List.of();
            if (action.equals("uninherit") && args.length == 4) {
                String parent = handler.getGroup(args[2]).orElseThrow().getName();
                return handler.getGroups().values().stream().filter(child -> child.getParents().contains(parent)).map(PermissionGroup::getName).toList();
            }
            if (action.equals("inherit") && args.length == 4) {
                PermissionGroup parent = handler.getGroup(args[2]).orElseThrow();
                return handler.getGroups().values().stream()
                        .filter(child -> GroupInheritance.canInherit(parent, child, handler.getGroups()))
                        .filter(child -> !GroupInheritance.inheritsFrom(child, parent, handler.getGroups()))
                        .map(PermissionGroup::getName).toList();
            }
            if (action.equals("weight") && args.length == 4) return List.of("0", "10", "100");
            if (action.equals("setpermission")) {
                if (args.length == 4) return registeredPermissions(args, 3);
                if (args.length == 5) return List.of("true", "false");
                return List.of();
            }
            if (action.equals("addpermission")) return registeredPermissions(args, 3);
            if (action.equals("removepermission") || action.equals("unsetpermission")) return handler.getGroup(args[2])
                    .map(group -> unused(group.getPermissions().stream().map(Permission::getPermission).toList(), args, 3)).orElse(List.of());
        } else if (category.equals("player")) {
            if (args.length == 2) return actions(sender, CommandAccess.PLAYER_COMMANDS);
            if (action.equals("listgroup")) action = "listgroups";
            if (!CommandAccess.PLAYER_COMMANDS.containsKey(action) || !allowed(sender, CommandAccess.PLAYER_COMMANDS.get(action))) return List.of();
            if (args.length == 3) return playerNames(sender);
            if ((action.equals("promote") || action.equals("demote")) && args.length == 4) return new ArrayList<>(handler.getTracks().keySet());
            if (action.equals("addgroup") && args.length == 5) return List.of("60", "300", "3600");
            if (action.equals("addgroup") && args.length == 4) return new ArrayList<>(handler.getGroups().keySet());
            if (action.equals("removegroup") && args.length == 4) {
                OfflinePlayer player = PlayerUtil.findPlayer(args[2]);
                return player == null ? List.of() : handler.getGroups(player.getUniqueId()).stream().map(PermissionGroup::getName).toList();
            }
            if (action.equals("check") && args.length == 4) {
                OfflinePlayer player = PlayerUtil.findPlayer(args[2]);
                List<String> nodes = new ArrayList<>(registeredPermissions(args, 3));
                if (player != null) {
                    nodes.addAll(handler.resolvePermissions(player.getUniqueId()).keySet());
                    handler.getMainGroup(player.getUniqueId()).ifPresent(group -> nodes.add("group." + group.getName()));
                }
                return nodes;
            }
            if (action.equals("setpermission")) {
                if (args.length == 4) return registeredPermissions(args, 3);
                if (args.length == 5) return List.of("true", "false");
                if (args.length == 6) return List.of("60", "300", "3600");
            }
            if (action.equals("addpermission")) {
                if (args.length == 4) return registeredPermissions(args, 3);
                if (args.length == 5) return List.of("60", "300", "3600");
            }
            if ((action.equals("removepermission") || action.equals("unsetpermission")) && args.length == 4) {
                OfflinePlayer player = PlayerUtil.findPlayer(args[2]);
                return player == null ? List.of() : handler.getPermissions(player.getUniqueId())
                        .stream().map(Permission::getPermission).toList();
            }
        } else if (category.equals("track")) {
            if (args.length == 2) return actions(sender, CommandAccess.TRACK_COMMANDS);
            if (!CommandAccess.TRACK_COMMANDS.containsKey(action) || !allowed(sender, CommandAccess.TRACK_COMMANDS.get(action))) return List.of();
            if (action.equals("create") || action.equals("list")) return List.of();
            if (args.length == 3) return new ArrayList<>(handler.getTracks().keySet());
            var track = handler.getTrack(args[2]);
            if (track.isEmpty() || args.length != 4) return List.of();
            if (action.equals("append")) return handler.getGroups().keySet().stream().filter(group -> !track.get().groups().contains(group)).toList();
            if (action.equals("remove")) return track.get().groups();
        } else if (category.equals("log") && allowed(sender, PermissifyConstants.PERMISSIFY_LOG)) {
            if (args.length == 2) return List.of("search", "1");
            if (action.equals("search") && args.length == 3) {
                List<String> targets = new ArrayList<>(handler.getGroups().keySet());
                targets.addAll(playerNames(sender));
                return targets;
            }
            if (action.equals("search") && args.length == 4) return List.of("1");
        } else if (category.equals("cache") && args.length == 2 && allowed(sender, PermissifyConstants.PERMISSIFY_CACHE)) {
            return List.of("purge");
        } else if (category.equals("profile") && allowed(sender, PermissifyConstants.PERMISSIFY_PROFILE)) {
            if (args.length == 2) return List.of("save", "load");
            if (args.length == 3 && action.equals("load")) {
                var directory = plugin.getDataFolder().toPath().resolve("profiles");
                if (!Files.isDirectory(directory)) return List.of();
                try (var files = Files.list(directory)) {
                    return files.filter(Files::isRegularFile).map(path -> path.getFileName().toString())
                            .filter(name -> name.endsWith(".permissify")).map(name -> name.substring(0, name.length() - 11)).toList();
                } catch (IOException e) {
                    return List.of();
                }
            }
        } else if (category.equals("superadmin") && PermissionUtil.isConsole(sender)) {
            if (args.length == 2) return List.of("grant", "remove");
            if (args.length == 3 && (action.equals("grant") || action.equals("remove"))) return playerNames(sender);
        } else if (category.equals("help") && args.length == 2) {
            int pages = PermissifyHelp.getPages(sender, LanguageUtil.getLanguage(plugin.getPermissifyAPI().getLanguageManager(), sender)).size();
            return IntStream.rangeClosed(1, pages).mapToObj(Integer::toString).toList();
        }
        return List.of();
    }

    private boolean allowed(CommandSender sender, String permission) {
        return PermissionUtil.hasPermissionOrSuperAdmin(sender, permission);
    }

    private List<String> actions(CommandSender sender, Map<String, String> commands) {
        return commands.entrySet().stream().filter(entry -> allowed(sender, entry.getValue())).map(Map.Entry::getKey).toList();
    }

    private List<String> playerNames(CommandSender sender) {
        return Bukkit.getOnlinePlayers().stream().filter(player -> !(sender instanceof Player viewer) || viewer.canSee(player))
                .map(Player::getName).toList();
    }

    private List<String> registeredPermissions(String[] args, int start) {
        return unused(Bukkit.getPluginManager().getPermissions().stream().map(org.bukkit.permissions.Permission::getName).toList(), args, start);
    }

    private List<String> unused(List<String> options, String[] args, int start) {
        List<String> selected = Arrays.asList(args).subList(start, args.length - 1);
        return options.stream().filter(option -> !selected.contains(option)).toList();
    }
}
