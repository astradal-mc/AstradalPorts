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

import java.util.Map;

public final class ReloadCommand {

    /**
     * Builds the '/portstone reload' command node.
     *
     * @param plugin The main plugin instance.
     * @return The configured command node.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("reload")
            .requires(PortstonePermissions.requires("reload"))
            .executes(ctx -> execute(ctx, plugin));
    }

    /**
     * Executes the reload logic for the plugin.
     */
    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();
        plugin.getMessageService().sendMessage(sender, "info-command-reload-starting");

        long startTime = System.currentTimeMillis();

        // 1. Reload the ConfigService to read the new values from config.yml
        plugin.getConfigService().reload();

        // 2. Re-initialize hooks in case settings like `economy.enabled` have changed
        plugin.getTownyHook().initialize();
        plugin.getEconomyHook().initialize();

        // 3. Refresh all holograms to fix any that might be missing
        plugin.getHologramService().initializeHolograms(plugin.getPortstoneManager());

        long duration = System.currentTimeMillis() - startTime;

        plugin.getMessageService().sendMessage(sender, "success-command-reload-complete", Map.of("duration", String.valueOf(duration)));

        return Command.SINGLE_SUCCESS;
    }
}