package net.astradal.astradalPorts.events;

import net.astradal.astradalPorts.model.Portstone;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PortstoneRenameEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Portstone portstone;
    private final String oldName;
    private final String newName;

    public PortstoneRenameEvent(Portstone portstone, String oldName, String newName) {
        this.portstone = portstone;
        this.oldName = oldName;
        this.newName = newName;
    }

    public Portstone getPortstone() {
        return portstone;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
