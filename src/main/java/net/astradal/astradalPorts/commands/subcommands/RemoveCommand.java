package net.astradal.astradalPorts.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class RemoveCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("remove")
            .requires(PortstonePermissions.requires("remove"))
            // Corresponds to /portstone remove
            .executes(ctx -> executeTargeted(ctx, plugin))
            // Corresponds to /portstone remove <id>
            .then(Commands.argument("id", StringArgumentType.word())
                // We can create a suggestion provider for known portstone IDs later
                .executes(ctx -> executeById(ctx, plugin))
            );
    }

    private static int executeTargeted(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            plugin.getMessageService().sendMessage(sender, "error-command-player-only");
            return 0;
        }

        try {
            plugin.getPortstoneManager().removePortstone(player);
            plugin.getMessageService().sendMessage(sender, "success-command-removed");
        } catch (PortstoneManager.PortstoneRemovalException e) {
            player.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int executeById(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();
        String idStr = ctx.getArgument("id", String.class);
        UUID portstoneId;

        try {
            portstoneId = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            plugin.getMessageService().sendMessage(sender, "error-command-invalid-id");
            return 0;
        }

        try {
            plugin.getPortstoneManager().removePortstone(portstoneId);
            plugin.getMessageService().sendMessage(sender, "success-command-removed-id", Map.of("id", idStr));
        } catch (PortstoneManager.PortstoneRemovalException e) {
            sender.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
        }

        return Command.SINGLE_SUCCESS;
    }
}