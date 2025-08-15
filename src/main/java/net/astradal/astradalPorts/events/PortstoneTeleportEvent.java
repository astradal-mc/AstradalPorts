package net.astradal.astradalPorts.events;

import net.astradal.astradalPorts.core.Portstone;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called just before a player is teleported via a Portstone.
 * This event is cancellable.
 */
@SuppressWarnings("unused")
public class PortstoneTeleportEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Portstone source;
    private final Portstone destination;
    private boolean cancelled;

    /**
     * Constructs a new PortstoneTeleportEvent.
     *
     * @param player      The player who is teleporting.
     * @param source      The portstone the player is teleporting from.
     * @param destination The portstone the player is teleporting to.
     */
    public PortstoneTeleportEvent(Player player, Portstone source, Portstone destination) {
        this.player = player;
        this.source = source;
        this.destination = destination;
        this.cancelled = false;
    }

    /**
     * Gets the player who is teleporting.
     * @return The teleporting player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the source portstone the player is teleporting from.
     * @return The source Portstone.
     */
    public Portstone getSource() {
        return source;
    }

    /**
     * Gets the destination portstone the player is teleporting to.
     * @return The destination Portstone.
     */
    public Portstone getDestination() {
        return destination;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}