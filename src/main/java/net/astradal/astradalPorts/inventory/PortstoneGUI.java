package net.astradal.astradalPorts.inventory;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.CooldownService;
import net.astradal.astradalPorts.util.PortstoneKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PortstoneGUI implements InventoryHolder {

    private final AstradalPorts plugin;
    private final Player player;
    private final Portstone source;
    private final List<Portstone> destinations;
    private final CooldownService cooldownService;
    private final Inventory inventory;

    public PortstoneGUI(AstradalPorts plugin, Player player, Portstone source, List<Portstone> allDestinations, CooldownService cooldownService) {
        this.plugin = plugin;
        this.player = player;
        this.source = source;
        this.cooldownService = cooldownService;

        // Filter destinations based on port type and land range
        if (source.getType().equalsIgnoreCase("land")) {
            int maxDistance = plugin.getLandPortstoneRangeLimit();
            this.destinations = allDestinations.stream()
                .filter(p -> !p.getId().equals(source.getId()))
                .filter(p -> p.getType().equalsIgnoreCase("land"))
                .filter(p -> p.getLocation().getWorld().equals(source.getLocation().getWorld()))
                .filter(p -> p.getLocation().distance(source.getLocation()) <= maxDistance)
                .toList();
        } else {
            this.destinations = allDestinations.stream()
                .filter(p -> !p.getId().equals(source.getId()))
                .toList();
        }

        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Travel to...", NamedTextColor.BLACK));
        buildMenu();
    }

    private void buildMenu() {
        for (Portstone port : destinations) {
            ItemStack item = new ItemStack(port.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            meta.displayName(Component.text(port.getDisplayName(), NamedTextColor.YELLOW));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Town: " + port.getTown(), NamedTextColor.GRAY));
            lore.add(Component.text("Nation: " + port.getNation(), NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Fee: $" + port.getTravelFee(), NamedTextColor.GOLD));

            String type = port.getType().toLowerCase();
            if (cooldownService.isOnCooldown(player, type)) {
                long remaining = cooldownService.getRemaining(player, type);
                lore.add(Component.text("Cooldown: " + remaining + "s remaining", NamedTextColor.RED));
            } else {
                lore.add(Component.text("Click to travel", NamedTextColor.GREEN));
            }

            meta.lore(lore);

            meta.getPersistentDataContainer().set(
                PortstoneKeys.PORTSTONE_ID,
                PersistentDataType.STRING,
                port.getId().toString()
            );

            item.setItemMeta(meta);
            inventory.addItem(item);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    public List<Portstone> getDestinations() {
        return destinations;
    }

    public Portstone getSource() {
        return source;
    }
}




