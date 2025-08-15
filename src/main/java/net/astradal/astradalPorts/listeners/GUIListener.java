package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.utils.PortstoneGUIHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class GUIListener implements Listener {
    private final AstradalPorts plugin;

    public GUIListener(AstradalPorts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PortstoneGUIHolder)) return;

        event.setCancelled(true); // Prevent players from taking items out of the GUI

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Player player = (Player) event.getWhoClicked();

        // Find the source portstone (the one they are near)
        Portstone source = plugin.getPortstoneManager()
            .findNearestPortstone(player.getLocation(), 5.0)
            .orElse(null);

        if (source == null) {
            player.closeInventory();
            // You might want to send an error message here
            return;
        }

        // Get the destination UUID from the item's metadata
        String destUuidString = clickedItem.getItemMeta().getPersistentDataContainer()
            .get(plugin.getGuiService().destinationUuidKey, PersistentDataType.STRING);

        if (destUuidString != null) {
            player.closeInventory();
            Portstone destination = plugin.getPortstoneManager().getPortstoneById(UUID.fromString(destUuidString));
            if (destination != null) {
                // We have a source and destination, start the warmup!
                plugin.getWarmupService().startWarmup(player, source, destination);
            }
        }
    }
}