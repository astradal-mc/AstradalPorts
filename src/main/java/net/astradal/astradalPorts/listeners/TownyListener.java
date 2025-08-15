package net.astradal.astradalPorts.listeners;

import com.palmergames.bukkit.towny.event.DeleteTownEvent;
import com.palmergames.bukkit.towny.event.RenameTownEvent;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Logger;

/**
 * Listens for events from the Towny API to keep Portstone data synchronized.
 */
public class TownyListener implements Listener {

    private final PortstoneManager portstoneManager;
    private final Logger logger;

    public TownyListener(PortstoneManager portstoneManager, Logger logger) {
        this.portstoneManager = portstoneManager;
        this.logger = logger;
    }

    /**
     * Called when a town is deleted.
     * Finds and removes all portstones that belonged to that town.
     */
    @EventHandler
    public void onTownDelete(DeleteTownEvent event) {
        String deletedTownName = event.getTownName();
        logger.info("Town '" + deletedTownName + "' is being deleted. Removing associated portstones...");

        // Create a copy to avoid issues while iterating and modifying
        portstoneManager.getAllPortstones().stream()
            .filter(p -> deletedTownName.equalsIgnoreCase(p.getTown()))
            .forEach(portstone -> {
                logger.info("Removing portstone '" + portstone.getDisplayName() + "' because its town was deleted.");
                portstoneManager.removePortstone(portstone);
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
        logger.info("Town '" + oldName + "' is being renamed to '" + newName + "'. Updating associated portstones...");

        portstoneManager.getAllPortstones().stream()
            .filter(p -> oldName.equalsIgnoreCase(p.getTown()))
            .forEach(portstone -> {
                portstone.setTown(newName);
                portstoneManager.savePortstone(portstone);
            });
    }
}