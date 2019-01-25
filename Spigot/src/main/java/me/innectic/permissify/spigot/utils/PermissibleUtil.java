package me.innectic.permissify.spigot.utils;

import me.innectic.permissify.spigot.PermissifyMain;
import me.innectic.permissify.spigot.injector.PermissibleInjector;
import me.innectic.permissify.spigot.injector.PermissifyPermissible;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.Optional;

public class PermissibleUtil {

    private static PermissibleInjector injector = new PermissibleInjector(true);

    public static void injectPermissible(Player player) {
        if (!PermissifyMain.getInstance().isUseWildcards()) return;

        try {
            PermissifyPermissible permissible = new PermissifyPermissible(player);
            Optional<Permissible> old = injector.inject(player, permissible);
            old.ifPresent(permissible::setPreviousPermissible);

            permissible.recalculatePermissions();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void uninjectPermissible(Player player) {
        if (!PermissifyMain.getInstance().isUseWildcards()) return;

        try {
            Optional<Permissible> ours = injector.getPermissible(player);
            if (!ours.isPresent()) return;
            if (!(ours.get() instanceof PermissifyPermissible)) return;
            if (!injector.inject(player, ((PermissifyPermissible) ours.get()).getPreviousPermissible()).isPresent())
                System.out.println("Failed to uninject for " + player.getName());
            player.getEffectivePermissions().clear();
            player.recalculatePermissions();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
