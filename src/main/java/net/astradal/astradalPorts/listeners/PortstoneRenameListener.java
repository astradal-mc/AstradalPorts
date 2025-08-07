package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.events.PortstoneRenameEvent;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.HologramService;
import net.astradal.astradalPorts.util.PortstoneFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public final class PortstoneRenameListener implements Listener {
    private final HologramService hologramService;

    public PortstoneRenameListener(HologramService hologramService) {
        this.hologramService = hologramService;
    }

    @EventHandler
    public void onPortstoneRename(PortstoneRenameEvent event) {
        Portstone portstone = event.getPortstone();
        UUID id = portstone.getId();

        // Remove old hologram entity
        hologramService.removeHologram(id);

        // Respawn updated hologram with new name
        Component text = PortstoneFormatter.getDisplayText(portstone);
        hologramService.showHologram(portstone.getLocation(), text, id);
    }

}
