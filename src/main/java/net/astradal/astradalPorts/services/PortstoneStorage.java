package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.model.Portstone;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PortstoneStorage {
    private final AstradalPorts plugin;
    private final File file;
    private final YamlConfiguration config;

    private final Map<UUID, Portstone> portstones = new HashMap<>();

    public PortstoneStorage(AstradalPorts plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "portstones.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        loadAll();
    }

    public void addPortstone(Portstone portstone) {
        portstones.put(portstone.getId(), portstone);
        save(portstone);
    }

    public void removePortstone(UUID id) {
        Portstone p = portstones.remove(id);
        if (p != null) {
            config.set(id.toString(), null);
            saveFile();
        }
    }
    public void removePortstone(Location loc) {
        Optional<UUID> match = portstones.values().stream()
            .filter(p -> p.getLocation().equals(loc))
            .map(Portstone::getId)
            .findFirst();

        match.ifPresent(this::removePortstone);
    }

    public boolean isPortstone(Location loc) {
        return portstones.values().stream().anyMatch(p -> p.getLocation().equals(loc));
    }

    public boolean portstoneExists(UUID id) {
        return portstones.containsKey(id);
    }

    public Portstone getById(UUID id) {
        return portstones.get(id);
    }

    public Optional<Portstone> getByLocation(Location loc) {
        return portstones.values().stream()
            .filter(p -> p.getLocation().equals(loc))
            .findFirst();
    }

    public Collection<Portstone> getAll() {
        return portstones.values();
    }

    public Set<UUID> getAllIds() {
        return portstones.keySet();
    }

    public List<Portstone> getByType(String type) {
        return portstones.values().stream()
            .filter(p -> p.getType().equalsIgnoreCase(type))
            .toList();
    }

    public void save(Portstone portstone) {
        String id = portstone.getId().toString();
        Location loc = portstone.getLocation();

        config.set(id + ".type", portstone.getType());
        config.set(id + ".world", loc.getWorld().getName());
        config.set(id + ".x", loc.getX());
        config.set(id + ".y", loc.getY());
        config.set(id + ".z", loc.getZ());
        config.set(id + ".town", portstone.getTown());
        config.set(id + ".nation", portstone.getNation());
        config.set(id + ".fee", portstone.getTravelFee());
        config.set(id + ".display-name", portstone.getDisplayName());
        config.set(id + ".icon", portstone.getIcon().name());

        saveFile();
    }

    private void saveFile() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save portstones.yml: " + e.getMessage());
        }
    }

    public boolean nationHasAirshipPort(String nationName) {
        return portstones.values().stream()
            .anyMatch(p -> p.getType().equalsIgnoreCase("air") &&
                p.getNation().equalsIgnoreCase(nationName));
    }


    private void loadAll() {
        if (!file.exists()) return;

        for (String key : config.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                String type = config.getString(key + ".type");
                String world = config.getString(key + ".world");
                double x = config.getDouble(key + ".x");
                double y = config.getDouble(key + ".y");
                double z = config.getDouble(key + ".z");
                String town = config.getString(key + ".town");
                String nation = config.getString(key + ".nation");
                double fee = config.getDouble(key + ".fee");
                String displayName = config.getString(key + ".display-name");
                String iconName = config.getString(key + ".icon");
                Material icon = iconName != null ? Material.matchMaterial(iconName) : Material.LODESTONE;

                String worldName = config.getString(key + ".world");
                if (worldName == null) {
                    plugin.getLogger().warning("Skipping portstone " + key + ": missing world name in config.");
                    continue;
                }

                World bukkitWorld = plugin.getServer().getWorld(worldName);
                if (bukkitWorld == null) {
                    plugin.getLogger().warning("Skipping portstone " + key + ": world '" + worldName + "' not found.");
                    continue;
                }
                Location loc = new Location(bukkitWorld, x, y, z);
                Portstone portstone = new Portstone(id, type, loc, town, nation, fee, displayName, icon);
                portstones.put(id, portstone);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load portstone: " + key + " (" + e.getMessage() + ")");
            }
        }
    }

    public void reload() {
        this.portstones.clear();
        YamlConfiguration.loadConfiguration(file);
        this.loadAll();
    }

}
