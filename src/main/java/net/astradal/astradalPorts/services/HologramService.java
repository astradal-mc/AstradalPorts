package net.astradal.astradalPorts.services;


import net.astradal.astradalPorts.AstradalPorts;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HologramService {

    private final AstradalPorts plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<UUID, UUID> portstoneToHologram = new HashMap<>();

    public HologramService(AstradalPorts plugin) {
        this.file = new File(plugin.getDataFolder(), "holograms.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.plugin = plugin;
        load();
    }

    public void showHologram(Location baseLocation, Component text, UUID portstoneId) {
        Location loc = baseLocation.clone().add(0.5, 1.5, 0.5);
        World world = loc.getWorld();
        if (world == null) return;

        // Don't respawn if one already exists in memory
        if (portstoneToHologram.containsKey(portstoneId)) {
            UUID hologramId = portstoneToHologram.get(portstoneId);
            boolean entityExists = Bukkit.getWorlds().stream()
                .map(w -> w.getEntity(hologramId))
                .anyMatch(e -> e instanceof TextDisplay);

            if (entityExists) return;

            // If entity is missing, remove stale reference
            portstoneToHologram.remove(portstoneId);
            config.set(portstoneId.toString(), null);
        }

        // Check for existing holograms at the location
        List<Entity> existing = world.getNearbyEntities(loc, 0.5, 0.5, 0.5).stream()
            .filter(e -> e instanceof TextDisplay)
            .toList();

        if (!existing.isEmpty()) {
            UUID entityId = existing.getFirst().getUniqueId();
            portstoneToHologram.put(portstoneId, entityId);
            config.set(portstoneId.toString(), entityId.toString());
            saveFile();
            return;
        }


        TextDisplay display = world.spawn(loc, TextDisplay.class, entity -> {
            entity.text(text);
            entity.setBillboard(Display.Billboard.VERTICAL);
            entity.setSeeThrough(false);
            entity.setDefaultBackground(true);
            entity.setShadowed(true);
            entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0)); // fully transparent
            entity.setPersistent(true);
        });

        portstoneToHologram.put(portstoneId, display.getUniqueId());
        config.set(portstoneId.toString(), display.getUniqueId().toString());
        saveFile();
    }

    public void removeHologram(UUID portstoneId) {
        UUID hologramId = portstoneToHologram.remove(portstoneId);
        config.set(portstoneId.toString(), null);
        saveFile();

        if (hologramId == null) return;

        for (World world : Bukkit.getWorlds()) {
            Entity entity = world.getEntity(hologramId);
            if (entity == null) {
                plugin.getLogger().warning("Failed to find hologram entity with ID " + hologramId);
                return;
            }
            if (entity instanceof TextDisplay) {
                entity.remove();
                break;
            }
        }
    }

    public void validateLoadedHolograms() {
        Iterator<Map.Entry<UUID, UUID>> iter = portstoneToHologram.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            UUID hologramId = entry.getValue();
            boolean found = Bukkit.getWorlds().stream()
                .map(w -> w.getEntity(hologramId))
                .anyMatch(e -> e instanceof TextDisplay);
            if (!found) {
                plugin.getLogger().warning("Missing hologram entity for portstone ID: " + entry.getKey());
                iter.remove();
                config.set(entry.getKey().toString(), null);
            }
        }
        saveFile();
    }

    public void load() {
        if (!file.exists()) return;

        for (String key : config.getKeys(false)) {
            String value = config.getString(key);
            if (value == null) {
                plugin.getLogger().warning("Skipping hologram entry: no value for portstone ID " + key);
                continue;
            }

            try {
                UUID portstoneId = UUID.fromString(key);
                UUID hologramId = UUID.fromString(value);
                portstoneToHologram.put(portstoneId, hologramId);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in holograms.yml: " + key + " → " + value);
            }
        }
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
           plugin.getLogger().severe("Failed to save holograms.yml: " + e.getMessage());
        }
    }

    public void reload() {
        this.portstoneToHologram.clear();
        this.load();
    }
}

