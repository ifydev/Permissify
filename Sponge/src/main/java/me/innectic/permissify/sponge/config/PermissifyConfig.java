package me.innectic.permissify.sponge.config;

import lombok.Getter;
import me.innectic.permissify.sponge.PermissifyMain;
import me.innectic.permissify.api.database.ConnectionInformation;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Innectic
 * @since 7/1/2017
 */
public class PermissifyConfig {

    private ConfigurationLoader<CommentedConfigurationNode> configurationLoader;
    private CommentedConfigurationNode configuration;

    @Getter private String storageType;
    @Getter private ConnectionInformation connectionInformation;

    public PermissifyConfig() {
        PermissifyMain.getPlugin().ifPresent(plugin -> {
            configurationLoader = HoconConfigurationLoader.builder().setPath(plugin.getDefaultConfiguration()).build();
            try {
                configuration = configurationLoader.load();
                verifyConfiguration();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void verifyConfiguration() {
        PermissifyMain.getPlugin().ifPresent(plugin -> {
            if (!plugin.getDefaultConfigurationFile().exists()) {
                // Create the basic config
                try {
                    boolean createdFile = plugin.getDefaultConfigurationFile().createNewFile();
                    if (!createdFile) throw new RuntimeException("Unable to create basic configuration!");
                    plugin.getLogger().info("Created default configuration!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            storageType = checkNode(configuration.getNode("storage", "mysql"), "mysql", "The medium to store plugin information in").getString();

            String host = checkNode(configuration.getNode("connection.host", ""), "", "The URL to connect to").getString();
            String username = checkNode(configuration.getNode("connection.username", ""), "", "The username for the database").getString();
            String password = checkNode(configuration.getNode("connection.password", ""), "", "The password for the database").getString();
            String database = checkNode(configuration.getNode("connection.database", ""), "", "The database to connect to").getString();
            int port = checkNode(configuration.getNode("connection.port", ""), "", "The port to connect to").getInt();
            connectionInformation = new ConnectionInformation(host, database, port, username, password, new HashMap<>());
        });
    }

    /**
     * Check the config for a configuration node. Create the node if it doesn't exist.
     *
     * @param node the node to check
     * @param defaultValue the default value to set for the node if it doesn't exist
     * @param comment what does this node represent?
     * @return the node from the configuration.
     */
    private CommentedConfigurationNode checkNode(CommentedConfigurationNode node, Object defaultValue, String comment) {
         if (node.isVirtual()) node.setValue(defaultValue).setComment(comment);
         return node;
    }
}
