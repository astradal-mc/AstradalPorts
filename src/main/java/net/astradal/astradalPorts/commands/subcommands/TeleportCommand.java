package net.astradal.astradalPorts.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class TeleportCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("teleport")
            // Note: A different permission might be better for players vs. admins
            .requires(PortstonePermissions.requires("teleport"))
            .then(Commands.argument("destination", StringArgumentType.greedyString())
                .suggests((ctx, builder) -> {
                    // Suggest the names of all enabled portstones
                    plugin.getPortstoneManager().getAllPortstones().stream()
                        .filter(Portstone::isEnabled)
                        .map(Portstone::getDisplayName)
                        .filter(name -> name.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                        .forEach(builder::suggest);
                    return builder.buildFuture();
                })
                .executes(ctx -> execute(ctx, plugin))
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return 0;
        }

        PortstoneManager manager = plugin.getPortstoneManager();

        // 1. Find the destination portstone from the command argument
        String destinationIdentifier = ctx.getArgument("destination", String.class);
        Optional<Portstone> destinationOpt = manager.findPortstoneByIdentifier(destinationIdentifier);

        if (destinationOpt.isEmpty()) {
            player.sendMessage(Component.text("No portstone named '" + destinationIdentifier + "' was found.", NamedTextColor.RED));
            return 0;
        }
        Portstone destination = destinationOpt.get();

        // 2. Find the source portstone (the one the player is standing near)
        // We'll search in a small radius, e.g., 5 blocks
        Optional<Portstone> sourceOpt = manager.findNearestPortstone(player.getLocation(), 5.0);
        if (sourceOpt.isEmpty()) {
            player.sendMessage(Component.text("You are not standing near a portstone.", NamedTextColor.RED));
            return 0;
        }
        Portstone source = sourceOpt.get();

        // 3. Perform final validation checks, now with bypasses
        if (source.getId().equals(destination.getId())) {
            player.sendMessage(Component.text("You are already at this portstone.", NamedTextColor.RED));
            return 0;
        }

        // Bypass check for disabled portstones
        if (!destination.isEnabled() && !PortstonePermissions.canBypass(player, "disabled")) {
            player.sendMessage(Component.text("That portstone is currently disabled.", NamedTextColor.RED));
            return 0;
        }

        // Bypass check for cooldowns
        if (!PortstonePermissions.canBypass(player, "cooldown") && plugin.getCooldownService().isOnCooldown(player, destination.getType())) {
            long remaining = plugin.getCooldownService().getRemainingSeconds(player, destination.getType());
            player.sendMessage(Component.text("You are on cooldown for this port type. Time remaining: " + remaining + "s", NamedTextColor.RED));
            return 0;
        }

        // 4. If all checks pass, delegate to the WarmupService
        plugin.getWarmupService().startWarmup(player, source, destination);

        return Command.SINGLE_SUCCESS;
    }
}