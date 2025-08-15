package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.database.repositories.HologramRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.Map;

/**
 * Manages the TextDisplay entities used as holograms above portstones.
 */
public class HologramService {

    private final Logger logger;
    private final HologramRepository hologramRepository;
    private final Map<UUID, UUID> hologramCache = new ConcurrentHashMap<>(); // Portstone ID -> Hologram Entity ID

    public HologramService(Logger logger, HologramRepository hologramRepository) {
        this.logger = logger;
        this.hologramRepository = hologramRepository;
    }

    /**
     * Iterates through all known portstones and ensures their holograms are active and correct.
     * Should be called on plugin startup.
     */
    public void initializeHolograms(PortstoneManager portstoneManager) {
        logger.info("Validating and creating holograms for all portstones...");

        // Ensure every existing portstone has a valid, living hologram entity.
        for (Portstone portstone : portstoneManager.getAllPortstones()) {
            createOrUpdateHologram(portstone);
        }

        logger.info("Hologram initialization complete.");
    }

    /**
     * Removes all managed hologram entities from the world.
     * Should be called on plugin shutdown.
     */
    public void removeAllHolograms() {
        hologramCache.values().forEach(entityId -> {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null) {
                entity.remove();
            }
        });
        hologramCache.clear();
    }

    /**
     * Creates a new hologram for a portstone, or updates it if one already exists.
     * @param portstone The portstone to create/update a hologram for.
     */
    public void createOrUpdateHologram(Portstone portstone) {
        UUID portstoneId = portstone.getId();
        UUID hologramId = hologramCache.get(portstoneId);
        Entity hologramEntity = (hologramId != null) ? Bukkit.getEntity(hologramId) : null;

        TextDisplay textDisplay;

        if (hologramEntity instanceof TextDisplay) {
            // This is the path for UPDATING an existing hologram.
            textDisplay = (TextDisplay) hologramEntity;
            textDisplay.teleport(getHologramLocation(portstone.getLocation()));
            textDisplay.text(generateHologramText(portstone)); // Update the text here.
        } else {
            // This is the path for CREATING a new hologram.
            removeHologram(portstoneId); // Clean up any stale DB/cache entries
            textDisplay = spawnHologram(portstone); // spawnHologram sets the initial text.
            hologramCache.put(portstoneId, textDisplay.getUniqueId());
            hologramRepository.saveHologram(portstoneId.toString(), textDisplay.getUniqueId());
        }
    }

    /**
     * Removes the hologram associated with a given portstone.
     * @param portstoneId The UUID of the portstone.
     */
    public void removeHologram(UUID portstoneId) {
        UUID hologramId = hologramCache.remove(portstoneId);
        hologramRepository.deleteHologram(portstoneId.toString());

        if (hologramId != null) {
            Entity entity = Bukkit.getEntity(hologramId);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    private TextDisplay spawnHologram(Portstone portstone) {
        Location location = getHologramLocation(portstone.getLocation());
        World world = location.getWorld();
        if (world == null) {
            throw new IllegalStateException("Cannot spawn hologram in a null world for portstone " + portstone.getId());
        }

        return world.spawn(getHologramLocation(portstone.getLocation()), TextDisplay.class, entity -> {
            entity.text(generateHologramText(portstone));
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setSeeThrough(false);
            entity.setDefaultBackground(false);
            entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0)); // Transparent
            entity.setShadowed(true);
            entity.setPersistent(true); // Make sure the hologram saves with the world
        });
    }

    private Location getHologramLocation(Location portstoneLocation) {
        // Center the hologram on the block and raise it
        return portstoneLocation.clone().add(0.5, 1.5, 0.5);
    }

    private Component generateHologramText(Portstone portstone) {
        // Line 1: The Portstone's display name, always shown.
        Component displayName = Component.text(portstone.getDisplayName(), NamedTextColor.AQUA);

        // Line 2: Always starts with the type.
        Component secondLine = Component.text("[" + portstone.getType().name() + "]", NamedTextColor.GRAY);

        // Conditionally add the "Disabled" status only if the portstone is not enabled.
        if (!portstone.isEnabled()) {
            secondLine = secondLine
                .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Disabled", NamedTextColor.RED));
        }

        // Combine the two lines.
        return displayName.append(Component.newline()).append(secondLine);
    }
}