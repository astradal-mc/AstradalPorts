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
import net.astradal.astradalPorts.core.PortType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class CreateCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("create")
            .requires(PortstonePermissions.requires("create"))
            .then(Commands.argument("type", StringArgumentType.word())
                .suggests((c, b) -> PortType.suggest(b))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> execute(ctx, plugin)))
                // Also allow creating without a custom name
                .executes(ctx -> execute(ctx, plugin))
            );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            plugin.getMessageService().sendMessage(sender, "error-command-player-only");
            return 0;
        }

        // --- 1. Parse Arguments ---
        Optional<PortType> typeOpt = PortType.fromString(ctx.getArgument("type", String.class));
        if (typeOpt.isEmpty()) {
            plugin.getMessageService().sendMessage(sender, "error-command-invalid-type");
            return 0;
        }

        String customName;
        try {
            customName = ctx.getArgument("name", String.class);
        } catch (IllegalArgumentException e) {
            customName = null; // Argument was not provided
        }

        // --- 2. Delegate to Manager and Handle Results ---
        PortstoneManager manager = plugin.getPortstoneManager();
        PortType type = typeOpt.get();

        try {
            // Check if a custom name was provided and call the correct method
            if (customName != null) {
                manager.createPortstone(player, type, customName);
            } else {
                manager.createPortstone(player, type);
            }

            player.sendMessage(Component.text("Portstone created successfully!", NamedTextColor.GREEN));

        } catch (PortstoneManager.PortstoneCreationException e) {
            // The manager provides the specific error message
            player.sendMessage(Component.text(e.getMessage(), NamedTextColor.RED));
        }

        return Command.SINGLE_SUCCESS;
    }
}