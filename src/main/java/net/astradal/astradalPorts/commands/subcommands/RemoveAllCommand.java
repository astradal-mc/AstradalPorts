package net.astradal.astradalPorts.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public final class RemoveAllCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("removeall")
            .requires(PortstonePermissions.requires("removeall")) // A new high-level permission
            // This runs when the user just types "/portstone removeall"
            .executes(RemoveAllCommand::sendConfirmation)
            // This requires the user to type "/portstone removeall confirm"
            .then(Commands.literal("confirm")
                .executes(ctx -> executeRemoveAll(ctx, plugin))
            );
    }

    // TODO: Add configurable messages for these
    @SuppressWarnings("SameReturnValue")
    private static int sendConfirmation(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();

        sender.sendMessage(
            Component.text("Are you sure you want to remove ALL portstones?", NamedTextColor.RED, TextDecoration.BOLD)
        );
        sender.sendMessage(
            Component.text("This action cannot be undone.", NamedTextColor.GRAY)
        );
        sender.sendMessage(
            Component.text("Click here to confirm or type ", NamedTextColor.GRAY)
                .append(Component.text("/portstone removeall confirm", NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.runCommand("/portstone removeall confirm"))
                    .hoverEvent(Component.text("Click to confirm removal of all portstones.")))
        );

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("SameReturnValue")
    private static int executeRemoveAll(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();

        int count = plugin.getPortstoneManager().getAllPortstones().size();

        plugin.getPortstoneManager().removeAllPortstones();

        sender.sendMessage(Component.text("Successfully removed " + count + " portstones.", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }
}