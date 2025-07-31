package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.AstradalPorts;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownService {
    private final File file;
    private final AstradalPorts plugin;

    private final Map<String, Integer> cooldowns; // seconds
    private final Map<UUID, Map<String, Long>> lastUse = new HashMap<>(); // player → type → timestamp

    public CooldownService(AstradalPorts plugin, Map<String, Integer> cooldowns) {
        this.plugin = plugin;
        this.cooldowns = cooldowns;
        this.file = new File(plugin.getDataFolder(), "cooldowns.yml");
        load();
    }
    public boolean isOnCooldown(Player player, String type) {
        long now = System.currentTimeMillis();
        long last = lastUse.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(type, 0L);
        int duration = cooldowns.getOrDefault(type.toLowerCase(), 0);
        return now < last + (duration * 1000L);
    }

    public long getRemaining(Player player, String type) {
        long now = System.currentTimeMillis();
        long last = lastUse.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(type, 0L);
        int duration = cooldowns.getOrDefault(type.toLowerCase(), 0);
        long end = last + (duration * 1000L);
        return Math.max(0, (end - now) / 1000);
    }

    public void markUsed(Player player, String type) {
        lastUse.computeIfAbsent(player.getUniqueId(), __ -> new HashMap<>())
            .put(type.toLowerCase(), System.currentTimeMillis());
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Map<String, Long>> entry : lastUse.entrySet()) {
            String key = entry.getKey().toString();
            for (Map.Entry<String, Long> use : entry.getValue().entrySet()) {
                config.set(key + "." + use.getKey(), use.getValue());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save cooldowns: " + e.getMessage());
        }
    }

    public void load() {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String uuidKey : config.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(uuidKey);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in cooldowns file: " + uuidKey);
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection(uuidKey);
            if (section == null) continue;

            Map<String, Long> cooldownMap = new HashMap<>();
            for (String type : section.getKeys(false)) {
                cooldownMap.put(type, section.getLong(type));
            }

            lastUse.put(playerId, cooldownMap);
        }
    }

}