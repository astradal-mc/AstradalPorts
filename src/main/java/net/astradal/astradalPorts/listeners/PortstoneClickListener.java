package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.AstradalPorts;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PortstoneClickListener implements Listener {
    private final PortstoneStorage storage;
    private final CooldownService cooldownService;
    private final AstradalPorts plugin;

    public PortstoneClickListener(AstradalPorts plugin, PortstoneStorage storage, CooldownService cooldownService) {
        this.plugin = plugin;
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

        event.setCancelled(true);
        Player player = event.getPlayer();

        Portstone clicked = maybe.get();
        String type = clicked.getType().toLowerCase();

        List<Portstone> destinations = storage.getByType(type).stream()
            .filter(p -> !p.getId().equals(clicked.getId()))
            .sorted(Comparator.comparing(Portstone::getDisplayName))
            .toList();

        PortstoneGUI gui = new PortstoneGUI(plugin, player, clicked, destinations, cooldownService);
        gui.open(player);
    }
}
