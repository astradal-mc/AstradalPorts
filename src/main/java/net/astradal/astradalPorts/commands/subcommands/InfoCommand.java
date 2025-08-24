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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
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
        CommandSender sender = ctx.getSource().getSender();
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            plugin.getMessageService().sendMessage(sender, "error-command-player-only");
            return 0;
        }

        Portstone portstone = plugin.getPortstoneManager().getPortstoneAt(player.getTargetBlock(null, 5).getLocation());
        if (portstone == null) {
            plugin.getMessageService().sendMessage(sender, "error-command-not-in-view");
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
            plugin.getMessageService().sendMessage(sender, "error-command-invalid-identifier", Map.of("identifier", identifier));
            return 0;
        }

        sender.sendMessage(PortstoneFormatter.formatInfo(portstoneOpt.get(), plugin.getEconomyHook()));
        return Command.SINGLE_SUCCESS;
    }
}