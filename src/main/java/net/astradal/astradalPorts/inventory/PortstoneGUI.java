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

import java.util.ArrayList;
import java.util.List;

public class PortstoneGUI implements InventoryHolder {

    private final AstradalPorts plugin;
    private final Player player;
    private final Portstone source;
    private final List<Portstone> destinations;
    private final CooldownService cooldownService;
    private final Inventory inventory;

    public PortstoneGUI(AstradalPorts plugin, Player player, Portstone source, List<Portstone> destinations, CooldownService cooldownService) {
        this.plugin = plugin;
        this.player = player;
        this.source = source;
        this.destinations = destinations;
        this.cooldownService = cooldownService;

        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Travel to...", NamedTextColor.BLACK));
        buildMenu();
    }

    private void buildMenu() {
        for (Portstone port : destinations) {
            if (port.getId().equals(source.getId())) continue;

            ItemStack item = new ItemStack(port.getIcon());
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(port.getDisplayName(), NamedTextColor.YELLOW));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Town: " + port.getTown(), NamedTextColor.GRAY));
            lore.add(Component.text("Nation: " + port.getNation(), NamedTextColor.DARK_GRAY));
            lore.add(Component.text("Fee: $" + port.getTravelFee(), NamedTextColor.GOLD));

            boolean onCooldown = cooldownService.isOnCooldown(player, port.getType());
            long remaining = cooldownService.getRemaining(player, port.getType());

            if (onCooldown) {
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

    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public List<Portstone> getDestinations() {
        return destinations;
    }

    public Portstone getSource() { return source; }
}


