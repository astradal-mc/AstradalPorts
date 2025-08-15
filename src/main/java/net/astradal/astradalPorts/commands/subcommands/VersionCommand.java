package net.astradal.astradalPorts.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class VersionCommand {

    /**
     * Builds the '/portstone version' command node.
     *
     * @param plugin The main plugin instance.
     * @return The configured command node.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("version")
            .requires(PortstonePermissions.requires("version"))
            .executes(ctx -> execute(ctx, plugin));
    }

    /**
     * Executes the version command, sending the plugin's version to the sender.
     */
    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();

        // Get the version from the plugin's description file (paper-plugin.yml)
        String version = plugin.getPluginMeta().getVersion();

        Component message = Component.text()
            .append(Component.text("AstradalPorts ", NamedTextColor.GOLD))
            .append(Component.text("version ", NamedTextColor.GRAY))
            .append(Component.text(version, NamedTextColor.AQUA))
            .build();

        sender.sendMessage(message);

        return Command.SINGLE_SUCCESS;
    }
}