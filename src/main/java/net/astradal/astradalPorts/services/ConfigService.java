package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.AstradalPorts;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for accessing typed configuration values from config.yml.
 * Provides getters with defaults and caching where appropriate.
 */
public class ConfigService {

    private final AstradalPorts plugin;

    // Cached values by port type (all lowercase keys)
    private final Map<String, Integer> cooldowns = new HashMap<>();
    private final Map<String, Integer> warmups = new HashMap<>();
    private final Map<String, Integer> ranges = new HashMap<>();

    // Cached global settings
    private boolean economyEnabled = true;
    private boolean economyRequireBalance = true;
    private String guiTitleColor = "black";

    // --- Cached Teleport Rules ---
    private boolean allowCrossWorldTravel = false;
    private Set<String> disabledWorlds = new HashSet<>();

    // --- Cached Effect Settings ---
    private Particle warmupParticle = Particle.ENCHANT;
    private int warmupParticleCount = 10;
    private String soundWarmupStart = "BLOCK_BEACON_ACTIVATE";
    private String soundTeleportSuccess = "ENTITY_ENDERMAN_TELEPORT";
    private String soundTeleportCancel = "BLOCK_REDSTONE_TORCH_BURNOUT";

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

        // Clear all cached maps before loading new values
        cooldowns.clear();
        warmups.clear();
        ranges.clear();

        // --- Load Portstone Settings  ---
        ConfigurationSection portstoneSection = config.getConfigurationSection("portstones");
        if (portstoneSection != null) {
            // Loop through each port type key (e.g., "air", "sea", "land")
            for (String typeKey : portstoneSection.getKeys(false)) {
                String key = typeKey.toLowerCase();
                // Get the cooldown, warmup, and range for each type
                cooldowns.put(key, portstoneSection.getInt(typeKey + ".cooldown", 0));
                warmups.put(key, portstoneSection.getInt(typeKey + ".warmup", 0));
                ranges.put(key, portstoneSection.getInt(typeKey + ".range", -1));
            }
        }

        // --- Load Teleport Rules ---
        this.allowCrossWorldTravel = config.getBoolean("teleport-rules.allow-cross-world-travel", false);
        List<String> worldsList = config.getStringList("teleport-rules.disabled-worlds");
        this.disabledWorlds = worldsList.stream().map(String::toLowerCase).collect(Collectors.toSet());

        // --- Load Economy and GUI settings ---
        economyEnabled = config.getBoolean("economy.enabled", true);
        economyRequireBalance = config.getBoolean("economy.requireBalance", true);
        guiTitleColor = config.getString("gui.titleColor", "black");


        // --- Load Effect Settings ---
        try {
            String particleName = config.getString("effects.warmup.particle", "ENCHANT").toUpperCase();
            this.warmupParticle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle name in config.yml. Defaulting to ENCHANT.");
            this.warmupParticle = Particle.ENCHANT;
        }
        this.warmupParticleCount = config.getInt("effects.warmup.count", 10);
        this.soundWarmupStart = config.getString("effects.sounds.warmup-start", "BLOCK_BEACON_ACTIVATE");
        this.soundTeleportSuccess = config.getString("effects.sounds.teleport-success", "ENTITY_ENDERMAN_TELEPORT");
        this.soundTeleportCancel = config.getString("effects.sounds.teleport-cancel", "BLOCK_REDSTONE_TORCH_BURNOUT");
    }

    // --- Getters for Effect Settings ---
    public Particle getWarmupParticle() {
        return warmupParticle;
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
     * Gets the maximum allowed range for a given port type.
     *
     * @param portType port type key (case-insensitive)
     * @return max range in blocks, or -1 if disabled/not set
     */
    public int getRange(String portType) {
        return ranges.getOrDefault(portType.toLowerCase(), -1);
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

    /**
     * Checks if teleporting between different worlds is allowed.
     * @return true if cross-world travel is enabled.
     */
    public boolean isCrossWorldTravelAllowed() {
        return allowCrossWorldTravel;
    }

    /**
     * Checks if Portstones are disabled in a specific world.
     * @param worldName The name of the world to check.
     * @return true if the world is on the disabled list.
     */
    public boolean isWorldDisabled(String worldName) {
        return disabledWorlds.contains(worldName.toLowerCase());
    }

    public int getWarmupParticleCount() {
        return warmupParticleCount;
    }

    public String getSoundWarmupStart() {
        return soundWarmupStart;
    }

    public String getSoundTeleportSuccess() {
        return soundTeleportSuccess;
    }

    public String getSoundTeleportCancel() {
        return soundTeleportCancel;
    }
}