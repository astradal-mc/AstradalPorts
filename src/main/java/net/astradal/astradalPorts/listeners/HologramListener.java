package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.core.PortstoneProperty;
import net.astradal.astradalPorts.events.PortstoneCreateEvent;
import net.astradal.astradalPorts.events.PortstonePropertyChangeEvent;
import net.astradal.astradalPorts.events.PortstoneRemoveEvent;
import net.astradal.astradalPorts.services.HologramService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HologramListener implements Listener {

    private final HologramService hologramService;

    public HologramListener(HologramService hologramService) {
        this.hologramService = hologramService;
    }

    @EventHandler
    public void onPortstoneCreate(PortstoneCreateEvent event) {
        hologramService.createOrUpdateHologram(event.getPortstone());
    }

    @EventHandler
    public void onPortstoneRemove(PortstoneRemoveEvent event) {
        hologramService.removeHologram(event.getPortstone().getId());
    }

    @EventHandler
    public void onPortstonePropertyChange(PortstonePropertyChangeEvent event) {
        // We only need to update the hologram if the name changes.
        // The service can handle other property changes in the future if needed.
        if (event.getProperty() == PortstoneProperty.NAME) {
            hologramService.createOrUpdateHologram(event.getPortstone());
        }
    }
}