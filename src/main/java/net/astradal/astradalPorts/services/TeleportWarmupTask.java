package net.astradal.astradalPorts.services;


import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.model.Portstone;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class TeleportWarmupTask implements Listener {
    private final AstradalPorts plugin;
    private final Portstone target;
    private final Player player;
    private final int durationTicks;
    private final BossBar bossBar;
    private final Portstone source;

    private Location startLocation;
    private Vector relativeOffset;
    private int ticksElapsed = 0;
    private int taskId = -1;

    public TeleportWarmupTask(AstradalPorts plugin, Player player, Portstone source, Portstone target, int seconds) {
        this.plugin = plugin;
        this.player = player;
        this.source = source;
        this.target = target;
        this.durationTicks = seconds * 20;
        this.bossBar = BossBar.bossBar(
            Component.text("Teleporting to " + target.getDisplayName(), NamedTextColor.AQUA),
            0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.NOTCHED_20
        );
    }

    public void start() {
        player.showBossBar(bossBar);
        startLocation = player.getLocation();
        relativeOffset = startLocation.toVector().subtract(source.getLocation().toVector());


        Bukkit.getPluginManager().registerEvents(this, plugin);
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0L, 1L);
    }

    private void tick() {
        if (!player.isOnline()) {
            cancel("You went offline.");
            return;
        }

        if (player.getLocation().distanceSquared(startLocation) > 0.1) {
            cancel("Teleport cancelled due to movement.");
            return;
        }

        ticksElapsed++;
        float progress = (float) ticksElapsed / durationTicks;
        bossBar.progress(Math.min(progress, 1f));

        player.getWorld().spawnParticle(
            Particle.ENCHANT,
            player.getLocation().add(0, 1, 0),
            20,
            0.4, 0.6, 0.4,
            0
        );

        if (ticksElapsed >= durationTicks) {
            complete();
        }
    }

    private void complete() {
        Location base = target.getLocation().clone();
        Location destination = base.clone().add(relativeOffset);

        // Center player within the block
        destination.setX(destination.getBlockX() + 0.5);
        destination.setZ(destination.getBlockZ() + 0.5);

        // Preserve direction they were facing
        destination.setDirection(startLocation.getDirection());

        player.teleportAsync(destination.add(0,.5,0)).thenRun(() ->
            player.sendMessage(Component.text("Warped to " + target.getDisplayName(), NamedTextColor.GREEN))
        );

        stop();
    }

    private void cancel(String reason) {
        player.sendMessage(Component.text(reason, NamedTextColor.RED));
        stop();
    }

    private void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        HandlerList.unregisterAll(this);
        player.hideBossBar(bossBar);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) stop();
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getPlayer().equals(player)) stop();
    }
}
