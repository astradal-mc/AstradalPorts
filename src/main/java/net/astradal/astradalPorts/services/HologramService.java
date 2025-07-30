package net.astradal.astradalPorts.services;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramService {

    private final Map<UUID, UUID> portstoneToHologram = new HashMap<>();

    public void showHologram(Location baseLocation, Component text, UUID portstoneId) {
        Location loc = baseLocation.clone().add(0.5, 1.5, 0.5);
        World world = loc.getWorld();
        if (world == null) return;

        TextDisplay display = world.spawn(loc, TextDisplay.class, entity -> {
            entity.text(text);
            entity.setBillboard(Display.Billboard.VERTICAL);
            entity.setSeeThrough(true);
            entity.setDefaultBackground(false);
            entity.setShadowed(true);
            entity.setBackgroundColor(Color.fromARGB(0, 0, 0,0 )); //fully transparent
            entity.setPersistent(true);
        });

        portstoneToHologram.put(portstoneId, display.getUniqueId());
    }

    public void removeHologram(UUID portstoneId) {
        UUID hologramId = portstoneToHologram.remove(portstoneId);
        if (hologramId == null) { return; }

        for (World world : Bukkit.getWorlds()) {
            Entity entity = world.getEntity(hologramId);
            if (entity instanceof TextDisplay) {
                entity.remove();
                break;
            }
        }
    }

    public void clearAll() {
        for (UUID hologramId : portstoneToHologram.values()) {
            for (World world : Bukkit.getWorlds()) {
                Entity entity = world.getEntity(hologramId);
                if (entity instanceof TextDisplay) {
                    entity.remove();
                }
            }
        }
        portstoneToHologram.clear();
    }

}
