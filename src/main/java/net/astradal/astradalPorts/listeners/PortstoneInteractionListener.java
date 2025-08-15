package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.services.GUIService;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PortstoneInteractionListener implements Listener {
    private final PortstoneManager portstoneManager;
    private final GUIService guiService;

    public PortstoneInteractionListener(PortstoneManager portstoneManager, GUIService guiService) {
        this.portstoneManager = portstoneManager;
        this.guiService = guiService;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.LODESTONE) return;

        Portstone portstone = portstoneManager.getPortstoneAt(clickedBlock.getLocation());
        if (portstone != null) {
            event.setCancelled(true); // Prevent default lodestone behavior
            guiService.openPortstoneGUI(event.getPlayer(), portstone);
        }
    }
}