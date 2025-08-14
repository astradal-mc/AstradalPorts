package net.astradal.astradalPorts.events;

import net.astradal.astradalPorts.core.Portstone;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a new Portstone has been successfully created and saved.
 */
public class PortstoneCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Portstone portstone;

    /**
     * Constructs a new PortstoneCreateEvent.
     *
     * @param portstone The new portstone that was just created.
     */
    public PortstoneCreateEvent(Portstone portstone) {
        this.portstone = portstone;
    }

    /**
     * Gets the portstone that was created in this event.
     *
     * @return The newly created Portstone.
     */
    public Portstone getPortstone() {
        return this.portstone;
    }

    /**
     * Gets the handler list for this event. Required by the Bukkit API.
     *
     * @return The handler list.
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the static handler list for this event. Required by the Bukkit API.
     *
     * @return The static handler list.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}