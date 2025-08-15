package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.services.hooks.EconomyHook;
import net.astradal.astradalPorts.utils.PortstoneFormatter;
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

import java.util.List;

/**
 * Service responsible for creating and managing all plugin GUIs.
 */
public class GUIService {

    private final AstradalPorts plugin;
    private final EconomyHook economyHook;
    public final NamespacedKey destinationUuidKey;

    public GUIService(AstradalPorts plugin, EconomyHook economyHook) {
        this.plugin = plugin;
        this.economyHook = economyHook;
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
            // Rule: Not in a hostile town
            .filter(p -> !plugin.getTownyHook().isHostile(sourcePortstone.getTown(), p.getTown()))
            // Rule: Not in another world if disabled
            .filter(p -> config.isCrossWorldTravelAllowed() || p.getWorld().equals(sourcePortstone.getWorld()))
            // Rule: Not in disabled world, if cross world portstones is enabled
            .filter(p -> !config.isWorldDisabled(p.getWorld()))
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

            meta.lore(PortstoneFormatter.formatLore(sourcePortstone, destination, economyHook));

            // IMPORTANT: Store the destination UUID on the item
            meta.getPersistentDataContainer().set(destinationUuidKey, PersistentDataType.STRING, destination.getId().toString());
            icon.setItemMeta(meta);

            gui.addItem(icon);
        }

        player.openInventory(gui);
    }
}