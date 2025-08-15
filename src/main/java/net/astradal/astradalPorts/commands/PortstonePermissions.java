package net.astradal.astradalPorts.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * A utility class for handling portstone command permissions.
 * Centralizes permission nodes to avoid "magic strings".
 */
public final class PortstonePermissions {

    private static final String BASE_COMMAND_NODE = "astradal.portstone.command.";
    private static final String BASE_BYPASS_NODE = "astradal.portstone.bypass.";

    /**
     * Checks if a CommandSender has a specific portstone subcommand permission.
     *
     * @param sender The sender to check.
     * @param subcommand The name of the subcommand (e.g., "create", "help").
     * @return True if the sender has the permission, false otherwise.
     */
    public static boolean has(CommandSender sender, String subcommand) {
        return sender.hasPermission(BASE_COMMAND_NODE + subcommand.toLowerCase());
    }

    /**
     * Returns a Predicate for use in Brigadier's .requires() method.
     *
     * @param subcommand The name of the subcommand.
     * @return A Predicate that tests if the source has the required permission.
     */
    public static Predicate<CommandSourceStack> requires(String subcommand) {
        return source -> has(source.getSender(), subcommand);
    }

    /**
     * Checks if a player has a specific bypass permission.
     * Also checks for the wildcard bypass permission.
     *
     * @param player The player to check.
     * @param type The bypass type (e.g., "fee", "cooldown").
     * @return True if the player has the specific or wildcard bypass permission.
     */
    public static boolean canBypass(Player player, String type) {
        return player.hasPermission(BASE_BYPASS_NODE + type.toLowerCase()) ||
            player.hasPermission(BASE_BYPASS_NODE + "*");
    }
}