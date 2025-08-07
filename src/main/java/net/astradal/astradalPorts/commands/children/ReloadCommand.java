package net.astradal.astradalPorts.commands.children;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.helpers.PortstonePermissions;
import net.astradal.astradalPorts.services.CooldownService;
import net.astradal.astradalPorts.services.HologramService;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public final class ReloadCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin, PortstoneStorage portstoneStorage, CooldownService cooldownService, HologramService hologramService) {
        return Commands.literal("reload")
            .requires(PortstonePermissions.requires("reload"))
            .executes(ctx -> execute(ctx, plugin, portstoneStorage, cooldownService, hologramService));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin, PortstoneStorage portstoneStorage, CooldownService cooldownService, HologramService hologramService) {
        plugin.reloadConfig();

        cooldownService.reload();
        portstoneStorage.reload();
        hologramService.reload();

        Audience audience = ctx.getSource().getSender();
        audience.sendMessage(Component.text("AstradalPorts reloaded.", NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

}
