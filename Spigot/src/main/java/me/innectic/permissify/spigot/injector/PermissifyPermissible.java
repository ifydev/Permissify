package me.innectic.permissify.spigot.injector;

import lombok.Getter;
import lombok.Setter;
import me.innectic.permissify.api.database.DatabaseHandler;
import me.innectic.permissify.spigot.PermissifyMain;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;

/**
 * @author Innectic
 * @since 9/2/2017
 */
public class PermissifyPermissible extends PermissibleBase {

    private final Player owner;
    private final PermissifyMain plugin;
    @Getter @Setter private Permissible previousPermissible;

    public PermissifyPermissible(Player owner, PermissifyMain plugin) {
        super(owner);

        this.owner = owner;
        this.plugin = plugin;
    }

    @Override
    public boolean isPermissionSet(String permission) {
        Optional<DatabaseHandler> database = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler();
        return database.map(databaseHandler -> databaseHandler.hasPermission(owner.getUniqueId(), permission)).orElse(false);
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        Optional<DatabaseHandler> database = PermissifyMain.getInstance().getPermissifyAPI().getDatabaseHandler();
        return database.map(databaseHandler -> databaseHandler.hasPermission(owner.getUniqueId(), permission.getName()))
                .orElse(permission.getDefault().getValue(isOp()));
    }

    @Override
    public boolean hasPermission(String permission) {
        Set<PermissionAttachmentInfo> attachments = getEffectivePermissions();
        for (PermissionAttachmentInfo attachment : attachments) {
            String against = attachment.getPermission();
            boolean isNegative = against.charAt(0) == '-';
            if (isNegative) against = against.substring(1);

            if (isPermissionAllowedFromWildcard(permission, against) && isNegative) return false;
        }
        return true;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return hasPermission(permission.getName());
    }

    @Override
    public void setOp(boolean isOp) {
        owner.setOp(isOp);
    }

    private boolean isPermissionAllowedFromWildcard(String checking, String against) {
        if (checking.equalsIgnoreCase(against)) return true;

        Iterator<String> checkingParts = Arrays.asList(checking.split("\\.")).iterator();
        Iterator<String> againstParts = Arrays.asList(against.split("\\.")).iterator();

        while (checkingParts.hasNext() && againstParts.hasNext()) {
            String checkPart = checkingParts.next();
            String againstPart = againstParts.next();

            if (!againstParts.hasNext() && againstPart.equals("*")) return true;
            if (!againstPart.equalsIgnoreCase("*") && !againstPart.equalsIgnoreCase(checkPart)) return false;
        }
        return false;
    }
}
