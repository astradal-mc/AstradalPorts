package net.astradal.astradalPorts.events;

import net.astradal.astradalPorts.core.Portstone;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a Portstone is about to be removed.
 */
@SuppressWarnings("unused")
public class PortstoneRemoveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Portstone portstone;

    public PortstoneRemoveEvent(Portstone portstone) {
        this.portstone = portstone;
    }

    /**
     * Gets the portstone that is being removed.
     * @return The removed Portstone.
     */
    public Portstone getPortstone() {
        return this.portstone;
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