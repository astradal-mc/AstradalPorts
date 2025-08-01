package net.astradal.astradalPorts.helpers;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.function.Predicate;

public final class PortstonePermissions {
    private static final String ROOT = "astradal.portstone.command.";

    public static Predicate<CommandSourceStack> requires(String node) {
        return source -> source.getSender().hasPermission(ROOT + node);
    }

    public static boolean has(CommandSender sender, String node) {
        return sender.hasPermission(ROOT + node);
    }

    private PortstonePermissions() {

    }
}
