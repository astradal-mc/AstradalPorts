package net.astradal.astradalPorts.core;

import net.astradal.astradalPorts.database.repositories.PortstoneRepository;
import net.astradal.astradalPorts.events.PortstoneCreateEvent;
import net.astradal.astradalPorts.events.PortstoneRemoveEvent;
import net.astradal.astradalPorts.services.hooks.TownyHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active Portstones.
 * This class acts as a caching layer on top of the PortstoneRepository to provide
 * fast, in-memory access and business logic for portstone-related operations.
 */
public class PortstoneManager {

    private final PortstoneRepository portstoneRepository;
    private final TownyHook townyHook;

    // Caches for fast lookups. One for UUIDs, one for Locations.
    private final Map<UUID, Portstone> portstoneCacheById = new ConcurrentHashMap<>();
    private final Map<Location, Portstone> portstoneCacheByLocation = new ConcurrentHashMap<>();

    public PortstoneManager(PortstoneRepository portstoneRepository, TownyHook townyHook) {
        this.portstoneRepository = portstoneRepository;
        this.townyHook = townyHook;
    }

    /**
     * Loads all portstones from the database and populates the in-memory caches.
     * This should be called once when the plugin enables.
     */
    public void loadAllPortstones() {
        portstoneCacheById.clear();
        portstoneCacheByLocation.clear();

        List<Portstone> portstonesFromDb = portstoneRepository.getAllPortstones();
        for (Portstone portstone : portstonesFromDb) {
            portstoneCacheById.put(portstone.getId(), portstone);
            portstoneCacheByLocation.put(portstone.getLocation(), portstone);
        }
    }

    // Define custom exceptions for clear error handling
    public static class PortstoneCreationException extends Exception {
        public PortstoneCreationException(String message) { super(message); }
    }

    /**
     * Creates a new portstone with a default name, saves it to the database, and adds it to the cache.
     * This is a convenience method that calls the more specific create method.
     *
     * @param player The player creating the portstone.
     * @param type   The PortType for the new portstone.
     * @return The newly created Portstone object.
     * @throws PortstoneCreationException if any validation fails.
     */
    public Portstone createPortstone(Player player, PortType type) throws PortstoneCreationException {
        // Call the main creation method with a null custom name.
        return this.createPortstone(player, type, null);
    }

    /**
     * Creates a new portstone with an optional custom name, saves it, and adds it to the cache.
     * This method contains all the core validation and business logic for portstone creation.
     *
     * @param player     The player creating the portstone.
     * @param type       The PortType for the new portstone.
     * @param customName The optional custom name for the portstone. Can be null.
     * @return The newly created Portstone object.
     * @throws PortstoneCreationException if any validation fails.
     */
    public Portstone createPortstone(Player player, PortType type, @Nullable String customName) throws PortstoneCreationException {
        // 1. Validate the target block
        Block target = player.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.LODESTONE) {
            throw new PortstoneCreationException("You must be looking at a lodestone.");
        }
        if (getPortstoneAt(target.getLocation()) != null) {
            throw new PortstoneCreationException("This lodestone is already a portstone.");
        }

        // 2. Validate the name (if provided)
        if (customName != null && isDisplayNameTaken(customName, null)) {
            throw new PortstoneCreationException("That portstone name is already in use.");
        }

        // --- 3. Validate Towny Ownership ---
        if (!townyHook.isEnabled()) {
            throw new PortstoneCreationException("Towny is not enabled on this server.");
        }
        String townName = townyHook.getTownNameAt(target.getLocation())
            .orElseThrow(() -> new PortstoneCreationException("Portstones can only be created on Towny-claimed land."));

        String nationName = townyHook.getNationNameAt(target.getLocation()).orElse(null);

        // --- 4. Validate Business Logic ---
        if (type == PortType.AIR) {
            if (nationName == null) {
                throw new PortstoneCreationException("Airship ports can only be created in towns that are part of a nation.");
            }
            if (nationHasAirshipPort(nationName)) {
                throw new PortstoneCreationException("This nation already has an airship port registered.");
            }
        }
        // You could add similar logic for one land/sea port per town here

        // 5. If all checks pass, create the Portstone
        String displayName = determineDefaultName(customName, type, townName, nationName);

        Portstone portstone = new Portstone(
            UUID.randomUUID(), type, target.getWorld().getName(),
            target.getX(), target.getY(), target.getZ(),
            townName, nationName, displayName, 0.0, type.getDefaultIcon(), true
        );

        // 6. Persist and cache the new portstone
        portstoneRepository.savePortstone(portstone);
        portstoneCacheById.put(portstone.getId(), portstone);
        portstoneCacheByLocation.put(portstone.getLocation(), portstone);

        // 7. Fire events or call other services
        Bukkit.getPluginManager().callEvent(new PortstoneCreateEvent(portstone));

        return portstone;
    }

    /**
     * Saves changes to an existing portstone to both the database and the cache.
     * @param portstone The Portstone object with updated information.
     */
    public void savePortstone(Portstone portstone) {
        // Update the database
        portstoneRepository.savePortstone(portstone);

        // Update the caches to reflect the change
        portstoneCacheById.put(portstone.getId(), portstone);
        portstoneCacheByLocation.put(portstone.getLocation(), portstone);
    }

    // Define a custom exception for removal failures
    public static class PortstoneRemovalException extends Exception {
        public PortstoneRemovalException(String message) { super(message); }
    }

    /**
     * Removes ALL portstones from the cache and the database.
     * This is a destructive operation intended for admin use.
     */
    public void removeAllPortstones() {
        // Create a copy of the values to avoid ConcurrentModificationException while iterating
        List<Portstone> portstonesToRemove = new ArrayList<>(portstoneCacheById.values());

        // Call the single removal method for each portstone to ensure events are fired
        // and caches/holograms are cleaned up properly for each one.
        for (Portstone portstone : portstonesToRemove) {
            removePortstone(portstone);
        }
    }

    /**
     * Removes the portstone a player is looking at, after performing permission checks.
     * @param player The player attempting to remove the portstone.
     * @throws PortstoneRemovalException if the player is not looking at a portstone or lacks permission.
     */
    public void removePortstone(Player player) throws PortstoneRemovalException {
        Block target = player.getTargetBlockExact(5);
        if (target == null) {
            throw new PortstoneRemovalException("You are not looking at a portstone.");
        }

        Portstone portstone = getPortstoneAt(target.getLocation());
        if (portstone == null) {
            throw new PortstoneRemovalException("No portstone is registered at this location.");
        }

        // Perform the ownership check using the TownyHook
        if (!townyHook.canEdit(player, portstone)) {
            throw new PortstoneRemovalException("You do not have permission to remove this portstone.");
        }

        // Call the internal removal method
        removePortstone(portstone);
    }

    /**
     * Removes a portstone by its ID without permission checks (for admins).
     * @param portstoneId The UUID of the portstone to remove.
     * @throws PortstoneRemovalException if no portstone with that ID is found.
     */
    public void removePortstone(UUID portstoneId) throws PortstoneRemovalException {
        Portstone portstone = getPortstoneById(portstoneId);
        if (portstone == null) {
            throw new PortstoneRemovalException("No portstone found with the ID: " + portstoneId);
        }
        removePortstone(portstone);
    }

    /**
     * The core internal method for removing a portstone from the cache and database.
     * @param portstone The portstone to remove.
     */
    public void removePortstone(Portstone portstone) {
        // Fire the event BEFORE deleting, so listeners can react.
        Bukkit.getPluginManager().callEvent(new PortstoneRemoveEvent(portstone));

        portstoneCacheById.remove(portstone.getId());
        portstoneCacheByLocation.remove(portstone.getLocation());
        portstoneRepository.deletePortstone(portstone.getIdAsString());
    }

    // --- Getter methods that read from the fast cache ---

    public Portstone getPortstoneById(UUID id) {
        return portstoneCacheById.get(id);
    }

    /**
     * Finds a portstone by its case-insensitive display name.
     *
     * @param name The name to search for.
     * @return An Optional containing the found Portstone, or empty if no match.
     */
    public Optional<Portstone> getPortstoneByName(String name) {
        return portstoneCacheById.values().stream()
            .filter(p -> p.getDisplayName().equalsIgnoreCase(name))
            .findFirst();
    }

    /**
     * Finds a portstone by trying to parse the identifier as a UUID, then as a name.
     * @param identifier The UUID string or display name.
     * @return An Optional containing the found Portstone.
     */
    public Optional<Portstone> findPortstoneByIdentifier(String identifier) {
        // Try to parse as UUID first
        try {
            UUID id = UUID.fromString(identifier);
            return Optional.ofNullable(this.getPortstoneById(id));
        } catch (IllegalArgumentException ignored) {
            // If it fails, treat it as a name
            return this.getPortstoneByName(identifier);
        }
    }

    public Portstone getPortstoneAt(Location location) {
        // Using a block-based location to ignore pitch/yaw
        Location blockLocation = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        return portstoneCacheByLocation.get(blockLocation);
    }

    /**
     * Finds the closest portstone to a given location within a specified radius.
     * @param location The location to search from.
     * @param radius The maximum distance to search.
     * @return An Optional containing the nearest Portstone.
     */
    public Optional<Portstone> findNearestPortstone(Location location, double radius) {
        return portstoneCacheByLocation.keySet().stream()
            .filter(loc -> loc.getWorld().equals(location.getWorld()))
            .filter(loc -> loc.distanceSquared(location) <= radius * radius)
            .min(Comparator.comparingDouble(loc -> loc.distanceSquared(location)))
            .map(this::getPortstoneAt);
    }


    public Collection<Portstone> getAllPortstones() {
        return Collections.unmodifiableCollection(portstoneCacheById.values());
    }

    // --- Business logic methods ---

    /**
     * Checks if a given display name is already in use by another portstone.
     *
     * @param name The name to check.
     * @param excludeId The UUID of a portstone to exclude from the check (used when renaming).
     * @return True if the name is taken, false otherwise.
     */
    public boolean isDisplayNameTaken(String name, UUID excludeId) {
        return portstoneCacheById.values().stream()
            .filter(p -> !p.getId().equals(excludeId))
            .anyMatch(p -> p.getDisplayName().equalsIgnoreCase(name));
    }

    /**
     * Checks if a nation already owns an Airship portstone.
     *
     * @param nationName The name of the nation to check.
     * @return true if an airship port exists for the nation.
     */
    public boolean nationHasAirshipPort(String nationName) {
        if (nationName == null) return false;
        return portstoneCacheById.values().stream()
            .anyMatch(p -> p.getType() == PortType.AIR && nationName.equalsIgnoreCase(p.getNation()));
    }

    /**
     * Determines the display name for a new portstone.
     * <p>
     * If a custom name is provided, it will be used. Otherwise, a default name
     * is generated based on the town and port type (e.g., "Northgate Land Port").
     *
     * @param customName The custom name provided by the player, which can be null or blank.
     * @param type       The {@link PortType} of the new portstone.
     * @param townName   The name of the town the portstone is in.
     * @param nationName The name of the nation the portstone is in (currently unused, but available for future logic).
     * @return The chosen display name for the portstone.
     */
    private String determineDefaultName(@Nullable String customName, PortType type, String townName, String nationName) {
        if (customName != null && !customName.isBlank()) {
            return customName;
        }

        // Creates a nicely capitalized string like "Land", "Sea", or "Air"
        String typeName = type.name().charAt(0) + type.name().substring(1).toLowerCase();

        if (type == PortType.AIR) {
            // nationName is guaranteed to be non-null by the logic in createPortstone
            return nationName + " " + typeName + " Port";
        }
        return townName + " " + typeName + " Port";
    }
}