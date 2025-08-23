package net.astradal.astradalPorts.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.command.TownCommand;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.utils.PortstoneGUIHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class GUIListener implements Listener {
    private final AstradalPorts plugin;

    public GUIListener(AstradalPorts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) throws TownyException {
        if (!(event.getInventory().getHolder() instanceof PortstoneGUIHolder)) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Player player = (Player) event.getWhoClicked();
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        // Check for a special item action first
        String specialAction = meta.getPersistentDataContainer().get(plugin.getGuiService().specialItemKey, PersistentDataType.STRING);
        if (specialAction != null) {
            player.closeInventory();
            if (specialAction.equals("town_spawn")) {
                // Use the Towny API to send the player to their town's spawn
                Resident resident = TownyAPI.getInstance().getResident(player);
                if (resident != null && resident.hasTown() && resident.getTownOrNull().hasSpawn()) {
                    try {
                        // This respects all of Towny's internal cooldowns, costs, and warmups.
                        TownCommand.townSpawn(player, new String[0], false, true);
                    } catch (TownyException e) {
                        // Towny already sends the error message to the player (e.g., "You are on cooldown.")
                        // We can optionally send a fallback message or log it if needed.
                        player.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
                    }
                } else {
                    // TODO: Configurable message
                    player.sendMessage(Component.text("You are not in a town or your town has no spawn set.", NamedTextColor.RED));
                }
            }
            return;
        }

        // If it's not a special item, handle it as a portstone destination
        String destUuidString = meta.getPersistentDataContainer().get(plugin.getGuiService().destinationUuidKey, PersistentDataType.STRING);
        if (destUuidString != null) {
            player.closeInventory();
            Portstone source = plugin.getPortstoneManager().findNearestPortstone(player.getLocation(), 5.0).orElse(null);
            Portstone destination = plugin.getPortstoneManager().getPortstoneById(UUID.fromString(destUuidString));

            if (source != null && destination != null) {
                plugin.getWarmupService().requestTeleport(player, source, destination);
            }
        }
    }
}
