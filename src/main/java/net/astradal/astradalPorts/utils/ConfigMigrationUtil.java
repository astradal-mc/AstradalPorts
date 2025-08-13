package net.astradal.astradalPorts.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility class responsible for migrating configuration files.
 * Adds missing config keys with default values without overwriting user changes.
 */
public final class ConfigMigrationUtil {

    private ConfigMigrationUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Performs config migration by adding missing default keys and saving the config if modified.
     *
     * @param plugin the JavaPlugin instance for accessing config and logging
     */
    public static void migrateConfigDefaults(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        boolean changed = false;

        changed |= addDefaultIfMissing(config, "cooldowns.air", 30);
        changed |= addDefaultIfMissing(config, "cooldowns.land", 90);
        changed |= addDefaultIfMissing(config, "cooldowns.sea", 0);

        changed |= addDefaultIfMissing(config, "warmups.air", 0);
        changed |= addDefaultIfMissing(config, "warmups.land", 3);
        changed |= addDefaultIfMissing(config, "warmups.sea", 0);

        changed |= addDefaultIfMissing(config, "land.maxRange", 2000);

        changed |= addDefaultIfMissing(config, "economy.enabled", true);
        changed |= addDefaultIfMissing(config, "economy.requireBalance", true);

        changed |= addDefaultIfMissing(config, "gui.titleColor", "black");

        if (changed) {
            plugin.getLogger().info("Config defaults migrated: adding missing keys");
            config.options().copyDefaults(true);
            plugin.saveConfig();
        }
    }

    /**
     * Adds a default config value if the given path is not already set.
     *
     * @param config the FileConfiguration instance
     * @param path the config key path to check and set
     * @param defaultValue the default value to set if missing
     * @return true if the config was modified by adding a default, false otherwise
     */
    private static boolean addDefaultIfMissing(FileConfiguration config, String path, Object defaultValue) {
        if (!config.isSet(path)) {
            config.set(path, defaultValue);
            return true;
        }
        return false;
    }
}