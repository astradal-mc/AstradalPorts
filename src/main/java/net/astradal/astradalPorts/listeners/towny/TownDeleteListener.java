package net.astradal.astradalPorts.listeners.towny;

import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import net.astradal.astradalPorts.helpers.PortstoneCleanupHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownDeleteListener implements Listener {
    private final PortstoneCleanupHelper cleanup;

    public TownDeleteListener(PortstoneCleanupHelper cleanup) {
        this.cleanup = cleanup;
    }

    @EventHandler
    public void onTownDelete(DeleteTownEvent event) {
        cleanup.removeByTown(event.getTownName());
    }
}
