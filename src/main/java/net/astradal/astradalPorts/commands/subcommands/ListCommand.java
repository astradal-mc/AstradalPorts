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
import net.astradal.astradalPorts.utils.PortstoneFilter;
import net.astradal.astradalPorts.utils.PortstoneFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;

public final class ListCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("list")
            .requires(PortstonePermissions.requires("list"))
            // Corresponds to /portstone list
            .executes(ctx -> execute(ctx, plugin, null))
            // Corresponds to /portstone list <filters>
            .then(Commands.argument("filters", StringArgumentType.greedyString())
                .executes(ctx -> execute(ctx, plugin, ctx.getArgument("filters", String.class)))
            );
    }

    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin, String filterString) {
        CommandSender sender = ctx.getSource().getSender();

        // 1. Get all portstones from the manager's cache
        Collection<Portstone> allPortstones = plugin.getPortstoneManager().getAllPortstones();

        // 2. Create a filter and apply it
        PortstoneFilter filter = new PortstoneFilter(filterString);
        List<Portstone> filteredPortstones = filter.apply(allPortstones);

        if (filteredPortstones.isEmpty()) {
            plugin.getMessageService().sendMessage(sender, "warning-command-none-found");
            return Command.SINGLE_SUCCESS;
        }

        // 3. Send the formatted list to the player
        sender.sendMessage(Component.text("--- Portstones (" + filteredPortstones.size() + "/" + allPortstones.size() + ") ---", NamedTextColor.GOLD));
        for (Portstone portstone : filteredPortstones) {
            sender.sendMessage(PortstoneFormatter.formatListEntry(portstone));
        }

        return Command.SINGLE_SUCCESS;
    }
}