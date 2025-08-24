package net.astradal.astradalPorts.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A marker interface to identify inventories created by the GUIService.
 */
public class PortstoneGUIHolder implements InventoryHolder {
    // Add the source portstone here for context, if needed

    public PortstoneGUIHolder() {
    }

    @Override
    public @NotNull Inventory getInventory() {
        // This method is part of the InventoryHolder interface but is not used in our
        // implementation. Throwing an exception is the standard way to handle this.
        throw new UnsupportedOperationException("PortstoneGUIHolder is a marker and does not hold an inventory.");
    }
}