package net.astradal.astradalPorts.listeners;

import net.astradal.astradalPorts.services.CooldownService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player connection events (Join/Quit).
 * <p>
 * Its primary responsibility is to manage the lifecycle of the {@link CooldownService}'s
 * in-memory cache, loading player data on join and saving it on quit to ensure
 * persistence and efficiency.
 */
public class PlayerConnectionListener implements Listener {

    private final CooldownService cooldownService;

    /**
     * Constructs the listener with a dependency on the CooldownService.
     *
     * @param cooldownService The service responsible for managing cooldowns.
     */
    public PlayerConnectionListener(CooldownService cooldownService) {
        this.cooldownService = cooldownService;
    }

    /**
     * Called when a player joins the server. Loads their cooldown data into the cache.
     *
     * @param event The PlayerJoinEvent provided by Bukkit.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        cooldownService.loadPlayerCooldowns(event.getPlayer().getUniqueId());
    }

    /**
     * Called when a player leaves the server. Saves their cached cooldown data to the database.
     *
     * @param event The PlayerQuitEvent provided by Bukkit.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldownService.savePlayerCooldowns(event.getPlayer().getUniqueId());
    }
}