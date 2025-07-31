package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.CooldownService;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.astradal.astradalPorts.util.PortstoneKeys;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PortstoneGUIListener implements Listener {
    private static final Component GUI_TITLE = Component.text("Travel to...", NamedTextColor.GOLD);

    private final PortstoneStorage storage;
    private final CooldownService cooldowns;

    public PortstoneGUIListener(PortstoneStorage storage, CooldownService cooldowns) {
        this.storage = storage;
        this.cooldowns = cooldowns;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        PersistentDataContainer container = clicked.getItemMeta().getPersistentDataContainer();
        String idString = container.get(PortstoneKeys.PORTSTONE_ID, PersistentDataType.STRING);
        if (idString == null) return;

        UUID id;
        try {
            id = UUID.fromString(idString);
        } catch (IllegalArgumentException e) {
            player.sendMessage(Component.text("Invalid portstone ID.", NamedTextColor.RED));
            return;
        }

        Portstone target = storage.getById(id);
        if (target == null) {
            player.sendMessage(Component.text("That portstone no longer exists.", NamedTextColor.RED));
            return;
        }

        // ✅ Check cooldown
        if (cooldowns.isOnCooldown(player, target.getType())) {
            long remaining = cooldowns.getRemaining(player, target.getType());
            player.sendMessage(Component.text("You must wait " + remaining + "s before using another " + target.getType() + " portstone.", NamedTextColor.RED));
            return;
        }

        // ✅ Teleport and start cooldown
        player.teleport(target.getLocation());
        cooldowns.markUsed(player, target.getType());

        player.sendMessage(Component.text("Warped to " + target.getDisplayName(), NamedTextColor.GREEN));
        player.closeInventory();
    }
}
