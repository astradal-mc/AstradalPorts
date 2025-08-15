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
import net.astradal.astradalPorts.utils.PortstoneFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class InfoCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("info")
            .requires(PortstonePermissions.requires("info"))
            // Corresponds to /portstone info
            .executes(ctx -> executeTargeted(ctx, plugin))
            // Corresponds to /portstone info <id or name>
            .then(Commands.argument("identifier", StringArgumentType.greedyString())
                // We can add suggestions for portstone names/IDs later
                .executes(ctx -> executeByIdentifier(ctx, plugin))
            );
    }

    private static int executeTargeted(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(Component.text("You must be a player to get info on a portstone by looking at it.", NamedTextColor.RED));
            return 0;
        }

        Portstone portstone = plugin.getPortstoneManager().getPortstoneAt(player.getTargetBlock(null, 5).getLocation());
        if (portstone == null) {
            player.sendMessage(Component.text("You are not looking at a portstone.", NamedTextColor.RED));
            return 0;
        }

        player.sendMessage(PortstoneFormatter.formatInfo(portstone, plugin.getEconomyHook()));
        return Command.SINGLE_SUCCESS;
    }

    private static int executeByIdentifier(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();
        String identifier = ctx.getArgument("identifier", String.class);
        PortstoneManager manager = plugin.getPortstoneManager();

        Optional<Portstone> portstoneOpt;

        // Try to parse as UUID first
        try {
            UUID id = UUID.fromString(identifier);
            portstoneOpt = Optional.ofNullable(manager.getPortstoneById(id));
        } catch (IllegalArgumentException e) {
            // If it's not a UUID, treat it as a name
            portstoneOpt = manager.getPortstoneByName(identifier);
        }

        if (portstoneOpt.isEmpty()) {
            sender.sendMessage(Component.text("No portstone found with the identifier: '", NamedTextColor.RED)
                .append(Component.text(identifier, NamedTextColor.WHITE))
                .append(Component.text("'", NamedTextColor.RED)));
            return 0;
        }

        sender.sendMessage(PortstoneFormatter.formatInfo(portstoneOpt.get(), plugin.getEconomyHook()));
        return Command.SINGLE_SUCCESS;
    }
}