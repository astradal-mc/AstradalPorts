package net.astradal.astradalPorts.commands.children;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.helpers.PortstonePermissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class VersionCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("version")
            .requires(PortstonePermissions.requires("version"))
            .executes(ctx -> execute(ctx, plugin));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();
        sender.sendMessage(Component.text(plugin.getName() + " version: ", NamedTextColor.YELLOW).append(Component.text(plugin.getPluginMeta().getVersion(), NamedTextColor.GOLD)));
        return Command.SINGLE_SUCCESS;
    }
}
