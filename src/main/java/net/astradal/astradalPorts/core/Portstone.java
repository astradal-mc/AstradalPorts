package net.astradal.astradalPorts.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.UUID;

/**
 * Represents a Portstone block used for player travel within the plugin.
 * Contains metadata about its location, ownership, travel fee, display properties, and type.
 */
public class Portstone {

    /** Unique identifier of this Portstone */
    private final UUID id;

    /** Type of Portstone (AIR, LAND, SEA) */
    private final PortType type;

    /** World name where the Portstone is located */
    private final String world;

    /** X coordinate of the Portstone location */
    private final double x;

    /** Y coordinate of the Portstone location */
    private final double y;

    /** Z coordinate of the Portstone location */
    private final double z;

    /** Town owning the Portstone; nullable if none */
    private final String town;

    /** Nation owning the Portstone; nullable if none */
    private final String nation;

    /** Travel fee charged to players when using this Portstone */
    private double travelFee;

    /** Display name of the Portstone shown in GUIs; can be null */
    private String displayName;

    /** Icon material displayed for this Portstone in GUIs; defaults to LODESTONE */
    private Material icon;

    /** Whether the portstone has been disabled */
    private boolean enabled;

    /**
     * Constructs a Portstone instance.
     *
     * @param id unique UUID of the Portstone
     * @param type portstone type (AIR, LAND, SEA)
     * @param world name of the world the Portstone is in
     * @param x x-coordinate of the Portstone location
     * @param y y-coordinate of the Portstone location
     * @param z z-coordinate of the Portstone location
     * @param town owning town; nullable if none
     * @param nation owning nation; nullable if none
     * @param displayName display name for GUIs; nullable or empty for default
     * @param fee travel fee charged for use
     * @param icon icon material shown in GUIs; nullable defaults to LODESTONE
     */
    public Portstone(UUID id, PortType type, String world, double x, double y, double z, String town, String nation, String displayName, double fee, Material icon, boolean enabled) {
        this.id = id;
        this.type = type;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.town = town;
        this.nation = nation;
        this.travelFee = fee;
        this.displayName = displayName;
        this.icon = icon;
        this.enabled = enabled;
    }

    /**
     * Gets the unique UUID of this Portstone.
     *
     * @return UUID of the Portstone
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the UUID as a string, useful for database keys.
     *
     * @return string representation of the Portstone UUID
     */
    public String getIdAsString() {
        return id.toString();
    }

    /**
     * Gets the type of this Portstone.
     *
     * @return the PortType enum (AIR, LAND, SEA)
     */
    public PortType getType() {
        return type;
    }

    /**
     * Gets the name of the world this Portstone resides in.
     *
     * @return world name
     */
    public String getWorld() {
        return world;
    }

    /**
     * Gets the X coordinate of this Portstone's location.
     *
     * @return x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the Y coordinate of this Portstone's location.
     *
     * @return y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Gets the Z coordinate of this Portstone's location.
     *
     * @return z-coordinate
     */
    public double getZ() {
        return z;
    }

    /**
     * Gets the owning town's name.
     *
     * @return town name or null if none
     */
    public String getTown() {
        return town;
    }

    /**
     * Gets the owning nation's name.
     *
     * @return nation name or null if none
     */
    public String getNation() {
        return nation;
    }

    /**
     * Gets the travel fee charged to players using this Portstone.
     *
     * @return travel fee amount
     */
    public double getTravelFee() {
        return travelFee;
    }

    /**
     * Sets the travel fee for this Portstone.
     *
     * @param travelFee new travel fee amount
     */
    public void setTravelFee(double travelFee) {
        this.travelFee = travelFee;
    }

    /**
     * Gets the display name for GUIs.
     * If none is set, generates a default name based on town, nation, or type.
     *
     * @return display name string
     */
    public String getDisplayName() {
        if (displayName == null || displayName.isEmpty()) {
            if (town != null && !town.isEmpty()) return town + " " + type.name();
            if (nation != null && !nation.isEmpty()) return nation + " " + type.name();
            return type.name() + " Portstone";
        }
        return displayName;
    }

    /**
     * Sets the display name for GUIs.
     *
     * @param displayName new display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the icon material shown for this Portstone in GUIs.
     * Returns LODESTONE if none is set.
     *
     * @return Material icon
     */
    public Material getIcon() {
        return icon != null ? icon : Material.LODESTONE;
    }

    /**
     * Sets the icon material for this Portstone.
     *
     * @param icon new Material icon
     */
    public void setIcon(Material icon) {
        this.icon = icon;
    }

    /**
     * Lazily recreates the Bukkit Location object from stored coordinates.
     *
     * @return Location object representing the Portstone's position
     * @throws IllegalStateException if the world is not loaded or does not exist
     */
    public Location getLocation() {
        World w = Bukkit.getWorld(world);
        if (w == null) {
            throw new IllegalStateException("World " + world + " not loaded or does not exist.");
        }
        return new Location(w, x, y, z);
    }

    /**
     * Gets the state of a portstone
     *
     * @return whether the portstone is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the state of a portstone
     *
     * @param enabled disables or enables a portstone
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
