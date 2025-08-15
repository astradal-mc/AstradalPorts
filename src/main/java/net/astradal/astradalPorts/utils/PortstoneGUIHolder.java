package net.astradal.astradalPorts.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * A marker interface to identify inventories created by the GUIService.
 */
public class PortstoneGUIHolder implements InventoryHolder {
    @Override
    public Inventory getInventory() {
        return null; // The inventory is managed by the GUIService, not this holder.
    }
}