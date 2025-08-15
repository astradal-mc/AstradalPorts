package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.utils.PortstoneGUIHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for creating and managing all plugin GUIs.
 */
public class GUIService {

    private final AstradalPorts plugin;
    public final NamespacedKey destinationUuidKey;

    public GUIService(AstradalPorts plugin) {
        this.plugin = plugin;
        this.destinationUuidKey = new NamespacedKey(plugin, "portstone_destination_uuid");
    }

    public void openPortstoneGUI(Player player, Portstone sourcePortstone) {
        PortstoneManager manager = plugin.getPortstoneManager();
        ConfigService config = plugin.getConfigService();

        // 1. Get all portstones and filter them based on the rules
        List<Portstone> availableDestinations = manager.getAllPortstones().stream()
            // Rule: Not the portstone you're standing at
            .filter(p -> !p.getId().equals(sourcePortstone.getId()))
            // Rule: Must be the same type
            .filter(p -> p.getType() == sourcePortstone.getType())
            // Rule: Must be enabled
            .filter(Portstone::isEnabled)
            // Rule: Must be within range (if range is set)
            .filter(p -> {
                int range = config.getRange(p.getType().name());
                return range == -1 || p.getLocation().distanceSquared(sourcePortstone.getLocation()) <= range * range;
            })
            // Rule: Not in a hostile town (placeholder for Towny hook logic)
            // .filter(p -> !plugin.getTownyHook().isHostile(sourcePortstone.getTown(), p.getTown()))
            .toList();

        if (availableDestinations.isEmpty()) {
            player.sendMessage(Component.text("No available destinations of this type.", NamedTextColor.YELLOW));
            return;
        }

        // 2. Create the inventory
        int size = (int) (Math.ceil(availableDestinations.size() / 9.0) * 9);
        Component title = Component.text("Teleport from: " + sourcePortstone.getDisplayName());
        Inventory gui = Bukkit.createInventory(new PortstoneGUIHolder(), size, title);

        // 3. Populate the inventory with icons
        for (Portstone destination : availableDestinations) {
            ItemStack icon = new ItemStack(destination.getIcon());
            ItemMeta meta = icon.getItemMeta();

            meta.displayName(Component.text(destination.getDisplayName(), NamedTextColor.AQUA));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Owner: ", NamedTextColor.GRAY).append(Component.text(destination.getTown() != null ? destination.getTown() : "Wilderness", NamedTextColor.WHITE)));
            double fee = destination.getTravelFee();
            if (fee > 0) {
                lore.add(Component.text("Fee: ", NamedTextColor.GRAY).append(Component.text(plugin.getEconomyHook().format(fee), NamedTextColor.GOLD)));
            }
            lore.add(Component.empty());
            lore.add(Component.text("Click to travel!", NamedTextColor.GREEN));
            meta.lore(lore);

            // IMPORTANT: Store the destination UUID on the item
            meta.getPersistentDataContainer().set(destinationUuidKey, PersistentDataType.STRING, destination.getId().toString());
            icon.setItemMeta(meta);

            gui.addItem(icon);
        }

        player.openInventory(gui);
    }
}