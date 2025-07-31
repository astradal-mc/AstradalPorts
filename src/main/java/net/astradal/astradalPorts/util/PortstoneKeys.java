package net.astradal.astradalPorts.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class PortstoneKeys {
    public static NamespacedKey PORTSTONE_ID;

    public static void init(JavaPlugin plugin) {
        PORTSTONE_ID = new NamespacedKey(plugin, "portstone_id");
    }

    private PortstoneKeys() {}
}