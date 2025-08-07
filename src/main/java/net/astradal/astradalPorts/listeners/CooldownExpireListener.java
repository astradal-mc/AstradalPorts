package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.events.CooldownExpireEvent;
import net.astradal.astradalPorts.inventory.PortstoneGUI;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.CooldownService;
import net.astradal.astradalPorts.util.PortstoneKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CooldownExpireListener implements Listener {

    private final CooldownService cooldownService;

    public CooldownExpireListener(CooldownService cooldownService) {
        this.cooldownService = cooldownService;
    }

    @EventHandler
    public void onCooldownExpire(CooldownExpireEvent event) {
        Player player = event.getPlayer();
        String type = event.getType();

        InventoryView view = player.getOpenInventory();
        if (!(view.getTopInventory().getHolder(false) instanceof PortstoneGUI gui)) return;
        if (!gui.getSource().getType().equalsIgnoreCase(type)) return;

        Inventory inv = view.getTopInventory();
        List<Portstone> destinations = gui.getDestinations();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || !item.hasItemMeta()) continue;

            ItemMeta meta = item.getItemMeta();
            String idStr = meta.getPersistentDataContainer().get(PortstoneKeys.PORTSTONE_ID, PersistentDataType.STRING);
            if (idStr == null) continue;

            UUID id = UUID.fromString(idStr);
            Portstone dest = destinations.stream()
                .filter(p -> p.getId().equals(id) && p.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);

            if (dest == null) continue;

            // Clean up lore and add travel instruction
            List<Component> newLore = meta.lore().stream()
                .filter(line -> !line.toString().contains("Cooldown"))
                .collect(Collectors.toList());

            newLore.add(Component.text("Click to travel", NamedTextColor.GREEN));
            meta.lore(newLore);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
    }
}

