package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.AstradalPorts;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for accessing typed configuration values from config.yml.
 * Provides getters with defaults and caching where appropriate.
 */
public class ConfigService {

    private final AstradalPorts plugin;

    // Cached cooldowns by port type (seconds)
    private final Map<String, Integer> cooldowns = new HashMap<>();
    // Cached warmups by port type (seconds)
    private final Map<String, Integer> warmups = new HashMap<>();
    // Cached land max range
    private int landMaxRange = 2000;
    // Economy enabled
    private boolean economyEnabled = true;
    // Economy requires balance
    private boolean economyRequireBalance = true;
    // GUI title color (string)
    private String guiTitleColor = "black";

    public ConfigService(AstradalPorts plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reloads the configuration and updates cached values.
     */
    public void reload() {
        plugin.reloadConfig();
        var config = plugin.getConfig();

        // Load cooldowns
        cooldowns.clear();
        ConfigurationSection cooldownSection = config.getConfigurationSection("cooldowns");
        if (cooldownSection != null) {
            for (String key : cooldownSection.getKeys(false)) {
                cooldowns.put(key.toLowerCase(), cooldownSection.getInt(key, 0));
            }
        }

        // Load warmups
        warmups.clear();
        ConfigurationSection warmupSection = config.getConfigurationSection("warmups");
        if (warmupSection != null) {
            for (String key : warmupSection.getKeys(false)) {
                warmups.put(key.toLowerCase(), warmupSection.getInt(key, 0));
            }
        }

        // Land max range
        landMaxRange = config.getInt("land.maxRange", 2000);

        // Economy settings
        economyEnabled = config.getBoolean("economy.enabled", true);
        economyRequireBalance = config.getBoolean("economy.requireBalance", true);

        // GUI title color
        guiTitleColor = config.getString("gui.titleColor", "black");
    }

    /**
     * Gets the cooldown in seconds for the given port type.
     *
     * @param portType port type key (case-insensitive, e.g., "air", "land", "sea")
     * @return cooldown duration in seconds, or 0 if not set
     */
    public int getCooldown(String portType) {
        return cooldowns.getOrDefault(portType.toLowerCase(), 0);
    }

    /**
     * Gets the warmup duration in seconds for the given port type.
     *
     * @param portType port type key (case-insensitive)
     * @return warmup duration in seconds, or 0 if not set
     */
    public int getWarmup(String portType) {
        return warmups.getOrDefault(portType.toLowerCase(), 0);
    }

    /**
     * Gets the maximum allowed range for land portstones.
     *
     * @return max range in blocks
     */
    public int getLandMaxRange() {
        return landMaxRange;
    }

    /**
     * Checks if the economy is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEconomyEnabled() {
        return economyEnabled;
    }

    /**
     * Checks if the economy requires a player to have enough balance to pay.
     *
     * @return true if balance required, false otherwise
     */
    public boolean isEconomyRequireBalance() {
        return economyRequireBalance;
    }

    /**
     * Gets the configured GUI title color.
     *
     * @return color string (e.g., "black")
     */
    public String getGuiTitleColor() {
        return guiTitleColor;
    }
}