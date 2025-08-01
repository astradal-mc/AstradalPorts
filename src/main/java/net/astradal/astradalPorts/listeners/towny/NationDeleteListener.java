package net.astradal.astradalPorts.listeners.towny;

import com.palmergames.bukkit.towny.event.DeleteNationEvent;
import net.astradal.astradalPorts.helpers.PortstoneCleanupHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NationDeleteListener implements Listener {
    private final PortstoneCleanupHelper cleanup;

    public NationDeleteListener(PortstoneCleanupHelper cleanup) {
        this.cleanup = cleanup;
    }

    @EventHandler
    public void onNationDelete(DeleteNationEvent event) {
        cleanup.removeByNation(event.getNationName());
    }
}
