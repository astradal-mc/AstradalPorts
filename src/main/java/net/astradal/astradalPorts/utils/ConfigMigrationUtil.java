package net.astradal.astradalPorts.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class responsible for migrating configuration files.
 * It ensures that new keys from the plugin's default config are added to
 * the user's existing config without overwriting their changes.
 */
public final class ConfigMigrationUtil {

    private ConfigMigrationUtil() {
        // Utility class
    }

    /**
     * Updates the user's config.yml by adding any missing default keys
     * from the version included in the plugin JAR.
     *
     * @param plugin the JavaPlugin instance for accessing the config
     */
    public static void migrateConfigDefaults(JavaPlugin plugin) {
        // 1. Get the plugin's live configuration
        FileConfiguration config = plugin.getConfig();

        // 2. Load the default config from the JAR resources
        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));

            // 3. Set the loaded config as the defaults for the live config
            config.setDefaults(defaultConfig);
        }

        // 4. Copy any missing defaults into the live config
        config.options().copyDefaults(true);

        // 5. Save the live config to disk, which writes the new keys
        plugin.saveConfig();
    }

    /**
     * Checks the version in the config file against the plugin's actual version
     * and updates it if they do not match.
     *
     * @param plugin The JavaPlugin instance.
     */
    public static void updateVersionInConfig(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        String jarVersion = plugin.getPluginMeta().getVersion();
        String configVersion = config.getString("plugin-version");

        // If the versions don't match, update the config
        if (!Objects.equals(jarVersion, configVersion)) {
            config.set("plugin-version", jarVersion);
            plugin.saveConfig();
            plugin.getLogger().info("Updated plugin-version in config.yml to " + jarVersion);
        }
    }

}