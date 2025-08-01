package net.astradal.astradalPorts.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.children.*;
import net.astradal.astradalPorts.helpers.PortstonePermissions;
import net.astradal.astradalPorts.services.PortstoneStorage;

public final class RootCommand {

    public static LiteralCommandNode<CommandSourceStack> create(
        AstradalPorts plugin,
        PortstoneStorage portstoneStorage,
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        return Commands.literal("portstone")
            .requires(PortstonePermissions.requires("portstone"))
            .executes(ctx -> HelpCommand.execute(ctx, dispatcher))
            .then(HelpCommand.build(dispatcher))
            .then(CreateCommand.build(plugin, portstoneStorage))
            .then(RemoveCommand.build(plugin, portstoneStorage))
            .then(SetFeeCommand.build(plugin, portstoneStorage))
            .then(ListCommand.build(plugin, portstoneStorage))
            .then(InfoCommand.build(plugin, portstoneStorage))
            .build();
    }
}
