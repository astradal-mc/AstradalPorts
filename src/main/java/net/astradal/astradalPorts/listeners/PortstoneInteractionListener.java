package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.services.GUIService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PortstoneInteractionListener implements Listener {
    private final PortstoneManager portstoneManager;
    private final GUIService guiService;

    public PortstoneInteractionListener(PortstoneManager portstoneManager, GUIService guiService) {
        this.portstoneManager = portstoneManager;
        this.guiService = guiService;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.LODESTONE) return;

        Portstone portstone = portstoneManager.getPortstoneAt(clickedBlock.getLocation());
        if (portstone != null) {
            Player player = event.getPlayer();
            event.setCancelled(true); // Prevent default lodestone behavior

            // Check for the 'use' permission before opening the GUI.
            if (!player.hasPermission("astradal.portstone.use")) {
                player.sendMessage(Component.text("You do not have permission to use portstones.", NamedTextColor.RED));
                return;
            }

            guiService.openPortstoneGUI(player, portstone);
        }
    }
}