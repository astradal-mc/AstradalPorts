package net.astradal.astradalPorts.model;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Portstone {
    private final UUID id;
    private final String type;
    private final String world;
    private final double x, y, z;
    private final String town;
    private final String nation;
    private double fee;
    private String displayName;

    public Portstone(UUID id, String type, Location location, String town, String nation, double fee, String displayName) {
        this.id = id;
        this.type = type;
        this.world = location.getWorld().getName();
        this.x = location.x();
        this.y = location.y();
        this.z = location.z();
        this.town = town;
        this.nation = nation;
        this.fee = fee;
        this.displayName = displayName;
    }

    public UUID getId() { return id; }
    public String getType() { return type; }
    public String getTown() { return town; }
    public String getNation() { return nation; }
    public double getTravelFee() { return fee; }
    public void setTravelFee(double fee) { this.fee = fee; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Location getLocation() {
        World w = Bukkit.getWorld(world);
        return new Location(w, x, y, z);
    }

}
