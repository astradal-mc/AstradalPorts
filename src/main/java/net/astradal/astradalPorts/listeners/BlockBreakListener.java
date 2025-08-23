package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.services.hooks.TownyHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Listens for blocks being broken to handle the removal of Portstones.
 */
public class BlockBreakListener implements Listener {

    private final PortstoneManager portstoneManager;
    private final TownyHook townyHook;

    public BlockBreakListener(PortstoneManager portstoneManager, TownyHook townyHook) {
        this.portstoneManager = portstoneManager;
        this.townyHook = townyHook;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // 1. Immediately ignore if the broken block is not a lodestone
        if (block.getType() != Material.LODESTONE) {
            return;
        }

        // 2. Check if this lodestone is a registered portstone
        Portstone portstone = portstoneManager.getPortstoneAt(block.getLocation());
        if (portstone == null) {
            return; // It's just a regular lodestone, do nothing.
        }

        Player player = event.getPlayer();

        //TODO: Add configurable messages for these

        // 3. Verify the player has permission to break this specific portstone
        if (!townyHook.canEdit(player, portstone)) {
            player.sendMessage(Component.text("You do not have permission to break this portstone.", NamedTextColor.RED));
            event.setCancelled(true); // Prevent the block from breaking
            return;
        }

        // 4. If all checks pass, remove the portstone data
        portstoneManager.removePortstone(portstone);
        player.sendMessage(Component.text("Portstone '" + portstone.getDisplayName() + "' unregistered.", NamedTextColor.GREEN));
    }
}