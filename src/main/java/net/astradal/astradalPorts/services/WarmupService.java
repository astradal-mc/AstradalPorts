package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.events.PortstoneTeleportEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import net.astradal.astradalPorts.services.hooks.EconomyHook;
import net.astradal.astradalPorts.services.hooks.TownyHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active teleport warmups. Acts as a central listener
 * to cancel warmups if a player moves or disconnects.
 */
public class WarmupService implements Listener {

    private final AstradalPorts plugin;
    private final Map<UUID, WarmupTask> activeWarmups = new ConcurrentHashMap<>();

    // Add fields for the dependencies
    private final ConfigService configService;
    private final CooldownService cooldownService;
    private final EconomyHook economyHook;
    private final TownyHook townyHook; // Needed to find the town bank

    // Update the constructor to accept the new dependencies
    public WarmupService(AstradalPorts plugin, ConfigService configService, CooldownService cooldownService, EconomyHook economyHook, TownyHook townyHook) {
        this.plugin = plugin;
        this.configService = configService;
        this.cooldownService = cooldownService;
        this.economyHook = economyHook;
        this.townyHook = townyHook;
    }

    public void startWarmup(Player player, Portstone source, Portstone destination) {
        // Cancel any existing warmup for this player
        if (activeWarmups.containsKey(player.getUniqueId())) {
            cancelWarmup(player, "You started a new teleport.", false);
        }

        int warmupSeconds = configService.getWarmup(destination.getType().name());
        if (warmupSeconds <= 0 || PortstonePermissions.canBypass(player, "warmup")) {
            // No warmup, teleport immediately
            teleportPlayer(player, source, destination);
            return;
        }

        WarmupTask task = new WarmupTask(this, player, source, destination, warmupSeconds);
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, task, 0L, 1L).getTaskId();
        task.start(taskId);

        activeWarmups.put(player.getUniqueId(), task);
        player.sendMessage(Component.text("Teleporting in " + warmupSeconds + " seconds. Don't move!", NamedTextColor.YELLOW));
    }

    public void completeWarmup(WarmupTask task) {
        Player player = task.getPlayer();
        if (activeWarmups.remove(player.getUniqueId()) != null) {
            Bukkit.getScheduler().cancelTask(task.getTaskId());
            task.cancel();
            teleportPlayer(player, task.getSource(), task.getDestination());
        }
    }

    public void cancelWarmup(Player player, String reason, boolean showMessage) {
        WarmupTask task = activeWarmups.remove(player.getUniqueId());
        if (task != null) {
            Bukkit.getScheduler().cancelTask(task.getTaskId());
            task.cancel();
            if (showMessage) {
                player.sendMessage(Component.text(reason, NamedTextColor.RED));
            }
        }
    }

    private void teleportPlayer(Player player, Portstone source, Portstone destination) {
        double fee = destination.getTravelFee();

        // --- 1. Charge Economy Fee ---
        if (economyHook.isEnabled() && fee > 0 && !PortstonePermissions.canBypass(player, "fee")) {
            if (!economyHook.chargeFee(player, fee)) {
                player.sendMessage(Component.text("You can't afford the " + economyHook.format(fee) + " travel fee!", NamedTextColor.RED));
                return; // Stop the teleport
            }
            player.sendMessage(Component.text("You paid a travel fee of " + economyHook.format(fee) + ".", NamedTextColor.GRAY));

            // --- 2. Deposit Fee into Town Bank (if applicable) ---
            if (townyHook.isEnabled() && destination.getTown() != null) {
                townyHook.depositToTownBank(destination.getTown(), fee);
            }
        }

        // Fire the cancellable event
        PortstoneTeleportEvent event = new PortstoneTeleportEvent(player, source, destination);
        Bukkit.getPluginManager().callEvent(event);

        // Check if another plugin or listener cancelled the event
        if (event.isCancelled()) {
            player.sendMessage(Component.text("Teleportation was cancelled by another process.", NamedTextColor.RED));
            return;
        }

        // Teleport the player if the event was not cancelled
        player.teleportAsync(destination.getLocation());

        // --- 4. Apply Cooldown ---
        cooldownService.applyCooldown(player, destination.getType());
    }

    // --- Event Handlers for the Service ---

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only cancel if the player moves to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        if (activeWarmups.containsKey(event.getPlayer().getUniqueId())) {
            cancelWarmup(event.getPlayer(), "Teleport cancelled. You moved.", true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (activeWarmups.containsKey(event.getPlayer().getUniqueId())) {
            cancelWarmup(event.getPlayer(), "", false); // No message needed, they're offline
        }
    }
}