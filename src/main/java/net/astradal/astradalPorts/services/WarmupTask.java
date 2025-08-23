package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.core.Portstone;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Represents a single, active teleport warmup for a player.
 * This class is a Runnable managed by the Bukkit scheduler.
 */
public class WarmupTask implements Runnable {

    private final WarmupService warmupService;
    private final Player player;
    private final Portstone destination;
    private final Portstone source;
    private final Location startLocation;
    private final int durationTicks;
    private final BossBar bossBar;
    private final Particle particle;
    private final int particleCount;

    private int taskId;
    private int ticksElapsed = 0;

    public WarmupTask(WarmupService warmupService, Player player, Portstone source, Portstone target, int seconds, Particle particle, int particleCount) {
        this.warmupService = warmupService;
        this.player = player;
        this.source = source;
        this.destination = target;
        this.durationTicks = seconds * 20;
        this.startLocation = player.getLocation().getBlock().getLocation(); // Use block location to prevent tiny movements from cancelling

        // TODO: Configurable message
        this.bossBar = BossBar.bossBar(
            Component.text("Teleporting to " + target.getDisplayName(), NamedTextColor.AQUA),
            0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS
        );
        this.particle = particle;
        this.particleCount = particleCount;
    }

    @Override
    public void run() {
        ticksElapsed++;

        // Update visuals
        float progress = (float) ticksElapsed / durationTicks;
        bossBar.progress(Math.min(progress, 1.0f));
        player.getWorld().spawnParticle(this.particle, player.getLocation().add(0, 1, 0), particleCount, 0.4, 0.6, 0.4, 0);

        // Check for completion
        if (ticksElapsed >= durationTicks) {
            warmupService.completeWarmup(this);
        }
    }

    public void start(int taskId) {
        this.taskId = taskId;
        player.showBossBar(bossBar);
    }

    public void cancel() {
        player.hideBossBar(bossBar);
    }

    // Getters for the service to use
    public Player getPlayer() { return player; }
    public Portstone getDestination() { return destination; }
    public int getTaskId() { return taskId; }
    public Location getStartLocation() { return startLocation; }

    public Portstone getSource() {
        return source;
    }
}