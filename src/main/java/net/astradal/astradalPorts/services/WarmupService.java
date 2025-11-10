package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.events.PortstoneTeleportEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import net.astradal.astradalPorts.services.hooks.EconomyHook;
import net.astradal.astradalPorts.services.hooks.TownyHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

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
    private final MessageService messageService;


    // Update the constructor to accept the new dependencies
    public WarmupService(AstradalPorts plugin, ConfigService configService, CooldownService cooldownService, EconomyHook economyHook, TownyHook townyHook, MessageService messageService) {
        this.plugin = plugin;
        this.configService = configService;
        this.cooldownService = cooldownService;
        this.economyHook = economyHook;
        this.townyHook = townyHook;
        this.messageService = messageService;
    }

    public void startWarmup(Player player, Portstone source, Portstone destination) {
        // Cancel any existing warmup for this player
        if (activeWarmups.containsKey(player.getUniqueId())) {
            cancelWarmup(player, "teleport-cancelled-new");
        }

        int warmupSeconds = configService.getWarmup(destination.getType().name());
        if (warmupSeconds <= 0 || PortstonePermissions.canBypass(player, "warmup")) {
            // No warmup, teleport immediately
            teleportPlayer(player, source, destination);
            return;
        }

        // Play the warmup start sound
        playSound(player, configService.getSoundWarmupStart());

        // Get particle info from config and pass it to the task
        Particle particle = configService.getWarmupParticle();
        int particleCount = configService.getWarmupParticleCount();
        WarmupTask task = new WarmupTask(this, player, source, destination, warmupSeconds, particle, particleCount);

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, task, 0L, 1L).getTaskId();
        task.start(taskId);

        activeWarmups.put(player.getUniqueId(), task);
        plugin.getMessageService().sendMessage(player, "teleport-warmup", Map.of("seconds", String.valueOf(warmupSeconds)));
    }

    public void completeWarmup(WarmupTask task) {
        Player player = task.getPlayer();
        if (activeWarmups.remove(player.getUniqueId()) != null) {
            Bukkit.getScheduler().cancelTask(task.getTaskId());
            task.cancel();
            teleportPlayer(player, task.getSource(), task.getDestination());
            playSound(player, configService.getSoundTeleportSuccess());
        }
    }

    /**
     * Cancels a warmup task for a player and plays the cancellation sound.
     * @param player The player whose task is being cancelled.
     * @param messageKey The config.yml message key to send (e.g., "teleport-cancelled-move"). Can be null.
     */
    public void cancelWarmup(Player player, @Nullable String messageKey) {
        WarmupTask task = activeWarmups.remove(player.getUniqueId());
        if (task != null) {
            Bukkit.getScheduler().cancelTask(task.getTaskId());
            task.cancel();

            if (messageKey != null) {
                messageService.sendMessage(player, messageKey);
            }
            playSound(player, configService.getSoundTeleportCancel());
        }
    }

    private void teleportPlayer(Player player, Portstone source, Portstone destination) {
        double fee = destination.getTravelFee();

        // --- 1. Charge Economy Fee ---
        if (economyHook.isEnabled() && fee > 0 && !PortstonePermissions.canBypass(player, "fee")) {
            if (!economyHook.chargeFee(player, fee)) {
                plugin.getMessageService().sendMessage(player, "error-cant-afford", Map.of("fee", economyHook.format(fee)));
                return; // Stop the teleport
            }
            messageService.sendMessage(player, "teleport-fee-paid", Map.of("fee", economyHook.format(fee)));

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
            messageService.sendMessage(player, "teleport-cancelled-other");
            return;
        }

        // --- Calculate the Safe Arrival Location ---
        // Get the destination block's location and add to it for a safe arrival spot.
        Location arrivalLocation = destination.getLocation().clone().add(0.5, 1.0, 0.5);
        // Preserve the player's camera direction
        arrivalLocation.setYaw(player.getLocation().getYaw());
        arrivalLocation.setPitch(player.getLocation().getPitch());

        // Teleport the player to the calculated safe spot
        player.teleportAsync(arrivalLocation).thenRun(() ->
            plugin.getMessageService().sendMessage(player, "teleport-success", Map.of("destination_name", destination.getDisplayName())));

        // --- 4. Apply Cooldown ---
        cooldownService.applyCooldown(player, destination.getType());
    }

    // --- Sound Player Helper ---
    private void playSound(Player player, String soundName) {
        if (soundName == null || soundName.isBlank() || soundName.equalsIgnoreCase("none")) {
            return;
        }

        // Convert the Bukkit enum style (BLOCK_BEACON_ACTIVATE) to a Minecraft key style (block.beacon.activate)
        String keyName = soundName.toLowerCase().replace('_', '.');
        NamespacedKey soundKey = NamespacedKey.minecraft(keyName);

        // Look up the sound in the server's registry
        Sound sound = Registry.SOUNDS.get(soundKey);

        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } else {
            plugin.getLogger().warning("Invalid sound name in config.yml: '" + soundName + "'");
        }
    }

    /**
     * The main entry point for initiating a teleport.
     * This method performs all necessary validation checks before starting a warmup.
     *
     * @param player      The player requesting to teleport.
     * @param source      The portstone the player is at.
     * @param destination The portstone the player wants to go to.
     */
    public void requestTeleport(Player player, Portstone source, Portstone destination) {
        // --- Perform all validation checks ---
        if (source.getId().equals(destination.getId())) {
            messageService.sendMessage(player, "error-already-at-portstone");
            return;
        }

        if (source.getType() != destination.getType()) {
            messageService.sendMessage(player, "error-different-type");
            return;
        }

        if (source.getType() == PortType.SEA && ! player.getWorld().isClearWeather()) {
            plugin.getMessageService().sendMessage(player, "error-stormy-seas");
            return;
        }

        if (!destination.isEnabled() && !PortstonePermissions.canBypass(player, "disabled")) {
            plugin.getMessageService().sendMessage(player, "error-portstone-disabled");
            return;
        }

        if (!PortstonePermissions.canBypass(player, "cooldown") && cooldownService.isOnCooldown(player, destination.getType())) {
            long remaining = cooldownService.getRemainingSeconds(player, destination.getType());
            plugin.getMessageService().sendMessage(player, "error-on-cooldown", Map.of("time", String.valueOf(remaining)));
            return;
        }

        if (configService.isWorldDisabled(source.getWorld()) || configService.isWorldDisabled(destination.getWorld())) {
            messageService.sendMessage(player, "error-world-disabled");
            return;
        }

        if (!configService.isCrossWorldTravelAllowed() && !source.getWorld().equals(destination.getWorld())) {
            messageService.sendMessage(player, "error-cross-world-disabled");
            return;
        }

        // --- If all checks pass, start the warmup ---
        this.startWarmup(player, source, destination);
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
            // TODO: Configurable message
            cancelWarmup(event.getPlayer(), "teleport-cancelled-move");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (activeWarmups.containsKey(event.getPlayer().getUniqueId())) {
            cancelWarmup(event.getPlayer(), null); // No message needed, they're offline
        }
    }

}