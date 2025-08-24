package net.astradal.astradalPorts.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import org.bukkit.command.CommandSender;

import java.util.Map;

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
    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();

        // Get the version from the plugin's description file (paper-plugin.yml)
        String version = plugin.getPluginMeta().getVersion();

        plugin.getMessageService().sendMessage(sender, "info-command-version", Map.of("version", version));

        return Command.SINGLE_SUCCESS;
    }
}