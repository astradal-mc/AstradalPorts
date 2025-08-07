package net.astradal.astradalPorts.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CooldownExpireEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String type;

    public CooldownExpireEvent(Player player, String type) {
        this.player = player;
        this.type = type.toLowerCase(); // normalize
    }

    public Player getPlayer() {
        return player;
    }

    public String getType() {
        return type;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}