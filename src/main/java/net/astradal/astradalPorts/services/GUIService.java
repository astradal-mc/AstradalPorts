package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.services.hooks.EconomyHook;
import net.astradal.astradalPorts.utils.PortstoneFormatter;
import net.astradal.astradalPorts.utils.PortstoneGUIHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for creating and managing all plugin GUIs.
 */
public class GUIService {

    private final AstradalPorts plugin;
    private final EconomyHook economyHook;
    public final NamespacedKey destinationUuidKey;
    public final NamespacedKey specialItemKey;

    public GUIService(AstradalPorts plugin, EconomyHook economyHook) {
        this.plugin = plugin;
        this.economyHook = economyHook;
        this.destinationUuidKey = new NamespacedKey(plugin, "portstone_destination_uuid");
        this.specialItemKey = new NamespacedKey(plugin, "special_item_action");
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
        }

        // 2. Create the inventory
        // Start with 9 slots for the top row.
        // Then, add enough rows for all the portstone icons.
        int portstoneRows = (int) Math.ceil(availableDestinations.size() / 9.0);
        int size = 9 + (portstoneRows * 9);
        // Ensure the inventory size is valid (max 54)
        size = Math.min(size, 54);

        Component title = Component.text("Teleport from: " + sourcePortstone.getDisplayName());
        Inventory gui = Bukkit.createInventory(new PortstoneGUIHolder(sourcePortstone), size, title);

        // 3. Place Special Items
        ConfigService.SpecialItemConfig townSpawnConfig = plugin.getConfigService().getTownSpawnItemConfig();
        if (townSpawnConfig.enabled() && townSpawnConfig.item() != null) {
            ItemStack spawnItem = new ItemStack(townSpawnConfig.item());
            ItemMeta meta = spawnItem.getItemMeta();
            // Using MiniMessage to parse formatted text from the config
            meta.displayName(MiniMessage.miniMessage().deserialize(townSpawnConfig.name()));
            meta.lore(townSpawnConfig.lore().stream()
                .map(line -> MiniMessage.miniMessage().deserialize(line))
                .collect(Collectors.toList()));

            // Add a persistent data tag to identify this item's action
            meta.getPersistentDataContainer().set(specialItemKey, PersistentDataType.STRING, "town_spawn");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            spawnItem.setItemMeta(meta);

            gui.setItem(townSpawnConfig.slot(), spawnItem);
        }

        // --- 4 Fill the rest of the top row slots (and any other empty slots later) ---
        Material fillMaterial = plugin.getConfigService().getGuiFillItem();
        if (fillMaterial != null && fillMaterial != Material.AIR) {
            ItemStack fillItem = new ItemStack(fillMaterial);
            ItemMeta meta = fillItem.getItemMeta();
            meta.displayName(Component.text(" "));
            fillItem.setItemMeta(meta);

            // Fill the entire top row first
            for (int i = 0; i < 9; i++) {
                if (gui.getItem(i) == null) {
                    gui.setItem(i, fillItem);
                }
            }
        }


        // 5. Populate with Portstone icons
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

        // 6. Fill empty slots
        if (fillMaterial != null && fillMaterial != Material.AIR) {
            ItemStack fillItem = new ItemStack(fillMaterial); // Recreate to be safe
            ItemMeta meta = fillItem.getItemMeta();
            meta.displayName(Component.text(" "));
            fillItem.setItemMeta(meta);
            for (int i = 9; i < gui.getSize(); i++) {
                if (gui.getItem(i) == null) {
                    gui.setItem(i, fillItem);
                }
            }
        }

        player.openInventory(gui);
    }
}