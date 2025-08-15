package net.astradal.astradalPorts.utils;

import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.services.hooks.EconomyHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for creating formatted text component displays for Portstones.
 */
public final class PortstoneFormatter {

    /**
     * Creates a detailed, multi-line component for the /portstone info command.
     *
     * @param portstone The portstone to format.
     * @param economyHook The economy hook for formatting currency.
     * @return A formatted text component with the portstone's details.
     */
    public static Component formatInfo(Portstone portstone, EconomyHook economyHook) {
        // --- Create the Status Component ---
        // This creates a colored text component based on the portstone's enabled status.
        Component status = portstone.isEnabled()
            ? Component.text("Enabled", NamedTextColor.GREEN)
            : Component.text("Disabled", NamedTextColor.RED);

        // --- Create the Owner String ---
        // This builds a string like "TownName (NationName)" or just "Unowned".
        String ownerString = portstone.getTown() != null ? portstone.getTown() : "Unowned";
        if (portstone.getNation() != null) {
            ownerString += " (" + portstone.getNation() + ")";
        }

        // --- Assemble the final message ---
        return Component.newline()
            .append(Component.text("--- Portstone Info ---", NamedTextColor.GOLD, TextDecoration.BOLD)).append(Component.newline())
            .append(formatKeyValue("Name", Component.text(portstone.getDisplayName()))).append(Component.newline())
            .append(formatKeyValue("ID", Component.text(portstone.getId().toString()).color(NamedTextColor.GRAY))).append(Component.newline())
            .append(formatKeyValue("Type", Component.text(portstone.getType().name()))).append(Component.newline())
            .append(formatKeyValue("Location", Component.text(String.format("%s (%d, %d, %d)",
                portstone.getWorld(), portstone.getLocation().getBlockX(), portstone.getLocation().getBlockY(), portstone.getLocation().getBlockZ())))).append(Component.newline())
            .append(formatKeyValue("Owner", Component.text(ownerString))).append(Component.newline())
            .append(formatKeyValue("Fee", Component.text(economyHook.format(portstone.getTravelFee())))).append(Component.newline())
            .append(formatKeyValue("Status", status)).append(Component.newline()) // Now 'status' exists
            .append(Component.text("--------------------", NamedTextColor.GOLD, TextDecoration.BOLD));
    }

    /**
     * Creates a compact, single-line component for the /portstone list command.
     *
     * @param portstone The portstone to format.
     * @return A formatted text component for a list entry.
     */
    public static Component formatListEntry(Portstone portstone) {
        // Use green for enabled, red for disabled
        NamedTextColor statusColor = portstone.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED;
        String statusText = portstone.isEnabled() ? "Enabled" : "Disabled";

        // Build the hover text, which shows the full info
        Component hoverText = Component.text("Click to copy ID:\n", NamedTextColor.GRAY)
            .append(Component.text(portstone.getId().toString(), NamedTextColor.WHITE));

        return Component.text()
            .append(Component.text(" • ", statusColor))
            .append(Component.text(portstone.getDisplayName(), NamedTextColor.YELLOW))
            .append(Component.text(" [" + portstone.getType().name() + "]", NamedTextColor.DARK_GRAY))
            .hoverEvent(hoverText)
            .clickEvent(ClickEvent.copyToClipboard(portstone.getId().toString()))
            .build();
    }

    /**
     * Creates a detailed lore for a portstone icon in the GUI.
     *
     * @param source      The portstone the player is teleporting from.
     * @param destination The portstone this lore is for.
     * @param economyHook The economy hook for formatting currency.
     * @return A list of components representing the item's lore.
     */
    public static List<Component> formatLore(Portstone source, Portstone destination, EconomyHook economyHook) {
        List<Component> lore = new ArrayList<>();

        // Owner Info
        String owner = destination.getTown() != null ? destination.getTown() : "Wilderness";
        lore.add(formatLoreLine("Owner", Component.text(owner, NamedTextColor.WHITE)));

        // Location Info
        Location loc = destination.getLocation();
        String locString = String.format("%s (%d, %d, %d)", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        lore.add(formatLoreLine("Location", Component.text(locString, NamedTextColor.WHITE)));

        // Distance Info
        int distance = (int) source.getLocation().distance(destination.getLocation());
        lore.add(formatLoreLine("Distance", Component.text(distance + " blocks", NamedTextColor.WHITE)));

        // Fee Info
        double fee = destination.getTravelFee();
        if (fee > 0) {
            lore.add(formatLoreLine("Fee", Component.text(economyHook.format(fee), NamedTextColor.GOLD)));
        }

        // Final prompt
        lore.add(Component.empty());
        lore.add(Component.text("Click to travel!", NamedTextColor.GREEN));

        return lore;
    }

    /**
     * Helper to create a "Key: Value" lore line without decoration.
     * */
    private static Component formatLoreLine(String key, Component value) {
        return Component.text(key + ": ", NamedTextColor.GRAY).append(value).decoration(TextDecoration.ITALIC, false);
    }
    /**
     * Helper to create a "Key: Value" component line.
     */
    private static Component formatKeyValue(String key, Component value) {
        return Component.text(key + ": ", NamedTextColor.AQUA).append(value.color(NamedTextColor.WHITE));
    }
}