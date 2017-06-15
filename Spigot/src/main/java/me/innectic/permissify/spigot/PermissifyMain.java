package me.innectic.permissify.spigot;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author Innectic
 * @since 6/14/2017
 */
public class PermissifyMain extends JavaPlugin {

    @Override
    public void onEnable() {
        createConfig();
    }

    @Override
    public void onDisable() {

    }

    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                boolean created = getDataFolder().mkdirs();
                if (!created) {
                    // Say something about this
                    return;
                }
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static PermissifyMain getInstance() {
        return PermissifyMain.getPlugin(PermissifyMain.class);
    }
}
