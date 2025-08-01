package net.astradal.astradalPorts.listeners.towny;

import com.palmergames.bukkit.towny.event.NationRemoveTownEvent;
import net.astradal.astradalPorts.helpers.PortstoneCleanupHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TownLeaveNationListener implements Listener {
    private final PortstoneCleanupHelper cleanup;

    public TownLeaveNationListener(PortstoneCleanupHelper cleanup) {
        this.cleanup = cleanup;
    }

    @EventHandler
    public void onTownLeaveNation(NationRemoveTownEvent event) {
        cleanup.removeIf(p -> "air".equalsIgnoreCase(p.getType())
            && p.getTown().equalsIgnoreCase(event.getTown().getName()));
    }
}