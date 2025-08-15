package net.astradal.astradalPorts.events;

import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneProperty;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a property on a Portstone has been changed.
 */
public class PortstonePropertyChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Portstone portstone;
    private final PortstoneProperty property;
    private final Object oldValue;
    private final Object newValue;

    public PortstonePropertyChangeEvent(Portstone portstone, PortstoneProperty property, Object oldValue, Object newValue) {
        this.portstone = portstone;
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Gets the portstone that was modified.
     */
    public Portstone getPortstone() {
        return portstone;
    }

    /**
     * Gets the {@link PortstoneProperty} that was changed.
     */
    public PortstoneProperty getProperty() {
        return property;
    }

    /**
     * Gets the value of the property before the change.
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Gets the new value of the property.
     */
    public Object getNewValue() {
        return newValue;
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