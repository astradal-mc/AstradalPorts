package net.astradal.astradalPorts.utils;

import net.astradal.astradalPorts.core.Portstone;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A marker interface to identify inventories created by the GUIService.
 */
public class PortstoneGUIHolder implements InventoryHolder {
    // Add the source portstone here for context, if needed
    private final Portstone sourcePortstone;

    public PortstoneGUIHolder(Portstone sourcePortstone) {
        this.sourcePortstone = sourcePortstone;
    }

    @Override
    public @NotNull Inventory getInventory() {
        // This method is part of the InventoryHolder interface but is not used in our
        // implementation. Throwing an exception is the standard way to handle this.
        throw new UnsupportedOperationException("PortstoneGUIHolder is a marker and does not hold an inventory.");
    }
}