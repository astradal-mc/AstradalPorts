package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.inventory.PortstoneGUI;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.CooldownService;
import net.astradal.astradalPorts.services.TeleportWarmupTask;
import net.astradal.astradalPorts.util.PortstoneKeys;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PortstoneGUIListener implements Listener {

    private final AstradalPorts plugin;
    private final CooldownService cooldownService;

    public PortstoneGUIListener(AstradalPorts plugin, CooldownService cooldownService) {
        this.plugin = plugin;
        this.cooldownService = cooldownService;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder(false) instanceof PortstoneGUI gui)) return;

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

        Portstone target = gui.getDestinations().stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);

        if (target == null) {
            player.sendMessage(Component.text("That portstone no longer exists.", NamedTextColor.RED));
            return;
        }

        String type = target.getType().toLowerCase();
        if (cooldownService.isOnCooldown(player, type)) {
            long remaining = cooldownService.getRemaining(player, type);
            player.sendMessage(Component.text("You must wait " + remaining + "s before using another " + type + " portstone.", NamedTextColor.RED));
            return;
        }

        int warmupSeconds = plugin.getConfig().getInt("warmups." + type, 0);
        cooldownService.markUsed(player, type);

        Portstone source = gui.getSource();

        if (warmupSeconds <= 0) {
            player.teleport(target.getLocation());
            player.sendMessage(Component.text("Warped to " + target.getDisplayName(), NamedTextColor.GREEN));
        } else {
            new TeleportWarmupTask(plugin, player, source, target, warmupSeconds).start();
        }

        player.closeInventory();
    }
}

