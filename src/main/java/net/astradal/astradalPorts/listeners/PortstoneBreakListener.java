package net.astradal.astradalPorts.listeners;

import com.palmergames.bukkit.towny.object.Resident;
import net.astradal.astradalPorts.integration.TownyHook;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.HologramService;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Optional;

public class PortstoneBreakListener implements Listener {

    private final PortstoneStorage storage;
    private final HologramService holograms;

    public PortstoneBreakListener(PortstoneStorage storage, HologramService holograms) {
        this.storage = storage;
        this.holograms = holograms;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.LODESTONE) return;

        Location loc = block.getLocation();
        Optional<Portstone> maybe = storage.getByLocation(loc);
        if (maybe.isEmpty()) return;

        Portstone portstone = maybe.get();
        Player player = event.getPlayer();

        // Check: Admin permission
        if (player.hasPermission("astradalports.admin")) {
            // allow
        }
        // Check: Mayor of the town that owns this portstone
        else if (portstone.getTown() != null && TownyHook.isMayor(player, portstone.getTown())) {
            // allow
        }
        // Not permitted
        else {
            player.sendMessage(Component.text("Only the town mayor or an admin may break this portstone.", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        // Authorized: remove the portstone
        storage.removePortstone(portstone.getId());
        holograms.removeHologram(portstone.getId());
        player.sendMessage(Component.text("Portstone removed.", NamedTextColor.RED));
    }
}

