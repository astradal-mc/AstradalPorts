package net.astradal.astradalPorts.listeners;

import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.logging.Logger;

/**
 * Listens for events from the Towny API to keep Portstone data synchronized.
 */
public class TownyListener implements Listener {

    private final PortstoneManager portstoneManager;
    private final AstradalPorts plugin;

    public TownyListener(PortstoneManager portstoneManager, AstradalPorts plugin) {
        this.portstoneManager = portstoneManager;
        this.plugin = plugin;
    }

    /**
     * Called when a town is deleted.
     * Finds and removes all portstones that belonged to that town.
     */
    @EventHandler
    public void onTownDelete(DeleteTownEvent event) {
        String deletedTownName = event.getTownName();

        // 1. Find all portstones that need to be deleted.
        List<Portstone> portstonesToRemove = portstoneManager.getAllPortstones().stream()
            .filter(p -> p.getTown() != null && deletedTownName.equalsIgnoreCase(p.getTown()))
            .toList();

        if (portstonesToRemove.isEmpty()) {
            return;
        }

        plugin.getLogger().info("Town '" + deletedTownName + "' is being deleted. Scheduling removal of " + portstonesToRemove.size() + " portstone(s).");

        // 2. Schedule a task to run on the main server thread.
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Portstone portstone : portstonesToRemove) {
                plugin.getLogger().info("Removing portstone '" + portstone.getDisplayName() + "' because its town was deleted.");
                portstoneManager.removePortstone(portstone);
            }
        });
    }

    /**
     * Called when a town is renamed.
     * Finds all portstones belonging to the town and updates their owner information.
     */
    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        String oldName = event.getOldName();
        String newName = event.getTown().getName();
        plugin.getLogger().info("Town '" + oldName + "' is being renamed to '" + newName + "'. Updating associated portstones...");

        portstoneManager.getAllPortstones().stream()
            .filter(p -> oldName.equalsIgnoreCase(p.getTown()))
            .forEach(portstone -> {
                portstone.setTown(newName);
                portstoneManager.savePortstone(portstone);
            });
    }
}