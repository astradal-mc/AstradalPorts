package net.astradal.astradalPorts.services.hooks;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.events.PortstoneCreateEvent;
import net.astradal.astradalPorts.events.PortstonePropertyChangeEvent;
import net.astradal.astradalPorts.events.PortstoneRemoveEvent;
import net.astradal.astradalPorts.services.ConfigService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Hook for integrating with the BlueMap API.
 * Manages a MarkerSet to display all portstones on the map.
 */
public class BlueMapHook implements Listener {

    private final Logger logger;
    private final PortstoneManager portstoneManager;
    private boolean enabled = false;
    private MarkerSet markerSet;
    private static final String MARKER_SET_ID = "astradal-portstones";

    // Update the constructor to take the ConfigService
    public BlueMapHook(Logger logger, PortstoneManager portstoneManager) {
        this.logger = logger;
        this.portstoneManager = portstoneManager;
    }

    public void initialize() {
        if (Bukkit.getPluginManager().getPlugin("BlueMap") == null) {
            return; // BlueMap not installed
        }

        BlueMapAPI.onEnable(api -> {
            this.enabled = true;
            logger.info("Successfully hooked into BlueMap. Creating marker set...");

            // 1. Create our single, shared MarkerSet object.
            this.markerSet = MarkerSet.builder()
                .label("Astradal Portstones")
                .toggleable(true)
                .defaultHidden(false)
                .build();

            // This makes the set "live".
            for (BlueMapWorld world : api.getWorlds()) {
                for (BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().put(MARKER_SET_ID, this.markerSet);
                }
            }

            // 3. NOW, add markers for all portstones that already exist.
            // Since the set is live, BlueMap will see these additions.
            portstoneManager.getAllPortstones().forEach(this::addOrUpdateMarker);
        });
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addOrUpdateMarker(Portstone portstone) {
        if (!enabled) return;

        String markerId = "portstone-" + portstone.getId().toString();
        Location loc = portstone.getLocation();
        Vector3d position = new Vector3d(loc.getX(), loc.getY(), loc.getZ());

        String detail = getString(portstone);

        // --- Build the Marker ---
        POIMarker marker = POIMarker.builder()
            .label(portstone.getDisplayName())
            .position(position)
            .detail(detail)
            .build();

        // REMOVED: The .icon() block

        markerSet.put(markerId, marker);
    }

    private static @NotNull String getString(Portstone portstone) {
        String statusColor = portstone.isEnabled() ? "green" : "red";
        String statusText = portstone.isEnabled() ? "Enabled" : "Disabled";
        String owner = portstone.getTown() != null ? portstone.getTown() : "Unowned";
        double fee = portstone.getTravelFee();
        if (portstone.getType() == PortType.AIR && portstone.getNation() != null) {
            owner = portstone.getNation() + " (Nation)";
        }

        return String.format("""
            <div style="text-align: center; font-family: Minecraft, sans-serif;">
              <h5 style="margin: 0; color: #55FFFF;">%s Port</h5>
              <hr style="margin: 2px 0;">
              <p style="margin: 0; padding: 0;">
                <b>Owner:</b> %s<br>
                <b>Fee:</b> $%.2f<br>
                <b>Status:</b> <span style="color: %s;">%s</span>
              </p>
            </div>
            """,
            portstone.getType().name(),
            owner,
            fee,
            statusColor,
            statusText
        );
    }

    public void removeMarker(UUID portstoneId) {
        if (!enabled) return;
        String markerId = "portstone-" + portstoneId.toString();
        this.markerSet.remove(markerId);
    }

    // --- Event Handlers to keep the map synchronized ---

    @EventHandler
    public void onPortstoneCreate(PortstoneCreateEvent event) {
        addOrUpdateMarker(event.getPortstone());
    }

    @EventHandler
    public void onPortstoneRemove(PortstoneRemoveEvent event) {
        removeMarker(event.getPortstone().getId());
    }

    @EventHandler
    public void onPortstonePropertyChange(PortstonePropertyChangeEvent event) {
        addOrUpdateMarker(event.getPortstone());
    }
}