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
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Player player = (Player) event.getWhoClicked();
        player.closeInventory();

        Portstone source = plugin.getPortstoneManager()
            .findNearestPortstone(player.getLocation(), 5.0)
            .orElse(null);

        String destUuidString = clickedItem.getItemMeta().getPersistentDataContainer()
            .get(plugin.getGuiService().destinationUuidKey, PersistentDataType.STRING);

        if (source != null && destUuidString != null) {
            Portstone destination = plugin.getPortstoneManager().getPortstoneById(UUID.fromString(destUuidString));
            if (destination != null) {
                // Delegate everything to the WarmupService
                plugin.getWarmupService().requestTeleport(player, source, destination);
            }
        }
    }
}