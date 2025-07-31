package net.astradal.astradalPorts.inventory;

import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.CooldownService;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.astradal.astradalPorts.util.PortstoneKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PortstoneGUI {

    private static final int INVENTORY_SIZE = 54;
    private static final String GUI_TITLE = "Travel to...";

    public static void open(Player player, Portstone source, PortstoneStorage storage, CooldownService cooldownService) {
        String type = source.getType().toLowerCase();
        List<Portstone> destinations = storage.getByType(type).stream()
            .filter(p -> !p.getId().equals(source.getId()))
            .sorted(Comparator.comparing(Portstone::getDisplayName))
            .toList();

        Inventory menu = Bukkit.createInventory(null, INVENTORY_SIZE, Component.text(GUI_TITLE, NamedTextColor.GOLD));

        for (Portstone port : destinations) {
            ItemStack item = new ItemStack(Material.LODESTONE);
            ItemMeta meta = item.getItemMeta();

            // Set name
            meta.displayName(Component.text(port.getDisplayName(), NamedTextColor.YELLOW));

            // Build lore
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Town: " + port.getTown(), NamedTextColor.GRAY));
            lore.add(Component.text("Nation: " + port.getNation(), NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Fee: $" + port.getTravelFee(), NamedTextColor.GOLD));

            boolean onCooldown = cooldownService.isOnCooldown(player, type);
            long remaining = cooldownService.getRemaining(player, type);

            if (onCooldown) {
                lore.add(Component.text("Cooldown: " + remaining + "s remaining", NamedTextColor.RED));
            } else {
                lore.add(Component.text("Click to travel", NamedTextColor.GREEN));
            }

            meta.lore(lore);

            // Store UUID for secure targeting
            meta.getPersistentDataContainer().set(
                PortstoneKeys.PORTSTONE_ID,
                PersistentDataType.STRING,
                port.getId().toString()
            );

            item.setItemMeta(meta);
            menu.addItem(item);
        }

        player.openInventory(menu);
    }

    private PortstoneGUI() {
        // static-only utility
    }
}
