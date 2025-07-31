package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.inventory.PortstoneGUI;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.CooldownService;
import net.astradal.astradalPorts.services.PortstoneStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class PortstoneClickListener implements Listener {
    private final PortstoneStorage storage;
    private final CooldownService cooldownService;

    public PortstoneClickListener(PortstoneStorage storage, CooldownService cooldownService) {
        this.storage = storage;
        this.cooldownService = cooldownService;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LODESTONE) return;

        Location loc = block.getLocation();
        Optional<Portstone> maybe = storage.getByLocation(loc);
        if (maybe.isEmpty()) return;

        Portstone clicked = maybe.get();
        event.setCancelled(true);

        Player player = event.getPlayer();
        PortstoneGUI.open(player, clicked, storage, cooldownService);
    }
}
