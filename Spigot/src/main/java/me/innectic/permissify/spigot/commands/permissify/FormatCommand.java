package me.innectic.permissify.spigot.commands.permissify;

import me.innectic.permissify.api.PermissifyConstants;
import me.innectic.permissify.api.util.ArgumentUtil;
import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.commands.CommandResponse;
import org.bukkit.command.CommandSender;

/**
 * @author Innectic
 * @since 7/2/2017
 */
public class FormatCommand {

    public CommandResponse handleSetFormat(CommandSender sender, String[] args) {
        if (args.length < 2) return new CommandResponse(PermissifyConstants.NOT_ENOUGH_ARGUMENTS_SET_FORMAT, false);
        if (args[0].equals("chat")) return handleSetChatFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
        else if (args[0].equals("whisper")) return handleWhisperFormat(sender, ArgumentUtil.getRemainingArgs(1, args));
        return new CommandResponse(PermissifyConstants.INVALID_FORMATTER.replace("<ARGUMENT>", args[0]), true);
    }

    private CommandResponse handleSetChatFormat(CommandSender sender, String[] args) {
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler"), false);
        String formatted = String.join(" ", args);
        PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get().setChatFormat(formatted);
        return new CommandResponse(PermissifyConstants.INVALID_FORMATTER.replace("<FORMATTER>", "chat"), true);
    }

    private CommandResponse handleWhisperFormat(CommandSender sender, String[] args) {
        if (!PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().isPresent())
            return new CommandResponse(PermissifyConstants.UNABLE_TO_SET.replace("<REASON>", "No database handler"), false);
        String formatted = String.join(" ", args);
        PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler().get().setWhisperFormat(formatted);
        return new CommandResponse(PermissifyConstants.INVALID_FORMATTER.replace("<FORMATTER>", "whisper"), true);
    }
}
