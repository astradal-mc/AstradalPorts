package net.astradal.astradalPorts.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class TeleportCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("teleport")
            .requires(PortstonePermissions.requires("teleport"))
            .then(Commands.argument("destination", StringArgumentType.greedyString())
                .suggests((ctx, builder) -> suggestDestinations(ctx, builder, plugin))
                .executes(ctx -> execute(ctx, plugin))
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            plugin.getMessageService().sendMessage(sender, "error-command-player-only");
            return 0;
        }

        PortstoneManager manager = plugin.getPortstoneManager();

        // 1. Find the destination portstone from the command argument.
        String destinationIdentifier = ctx.getArgument("destination", String.class);
        Optional<Portstone> destinationOpt = manager.findPortstoneByIdentifier(destinationIdentifier);

        if (destinationOpt.isEmpty()) {
            plugin.getMessageService().sendMessage(sender, "error-command-not-found", Map.of("destination", destinationIdentifier));
            return 0;
        }
        Portstone destination = destinationOpt.get();

        // 2. Calculate a safe arrival location above the lodestone.
        Location arrivalLocation = destination.getLocation().clone().add(0.5, 1.0, 0.5);
        // Preserve the player's camera direction
        arrivalLocation.setYaw(player.getLocation().getYaw());
        arrivalLocation.setPitch(player.getLocation().getPitch());

        // 3. Teleport the player directly, bypassing all services.
        player.teleportAsync(arrivalLocation).thenRun(() ->
            plugin.getMessageService().sendMessage(player, "teleport-success", Map.of("destination_name", destinationIdentifier)));

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Provides context-aware tab-completion suggestions for available teleport destinations.
     *
     * @param ctx     The command context.
     * @param builder The suggestions builder.
     * @param plugin  The main plugin instance to access services.
     * @return A future containing the suggestions.
     */
    private static CompletableFuture<Suggestions> suggestDestinations(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, AstradalPorts plugin) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return builder.buildFuture();
        }

        PortstoneManager manager = plugin.getPortstoneManager();

        Optional<Portstone> sourceOpt = manager.findNearestPortstone(player.getLocation(), 5.0);
        if (sourceOpt.isEmpty()) {
            // If not near a source, suggest all portstones
            manager.getAllPortstones().stream()
                .map(Portstone::getDisplayName)
                .filter(name -> name.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                .forEach(builder::suggest);
        } else {
            // If near a source, suggest all portstones except the source itself
            Portstone source = sourceOpt.get();
            manager.getAllPortstones().stream()
                .filter(destination -> !destination.getId().equals(source.getId()))
                .map(Portstone::getDisplayName)
                .filter(name -> name.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                .forEach(builder::suggest);
        }

        return builder.buildFuture();
    }

}