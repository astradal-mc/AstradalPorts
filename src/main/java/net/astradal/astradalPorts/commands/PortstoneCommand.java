package net.astradal.astradalPorts.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.subcommands.*;

public final class PortstoneCommand {

    /**
     * Creates and builds the root '/portstone' command node with all its subcommands.
     * This static method acts as a factory for the entire command structure.
     *
     * @param plugin     The main plugin instance, which is passed to subcommands
     * so they can access plugin services and managers.
     * @param dispatcher The command dispatcher, used here to build the default help command's
     * usage message.
     * @return The fully constructed command node, ready to be registered with Paper's
     * command system.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> create(
        AstradalPorts plugin,
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        var rootNode = Commands.literal("portstone")
            .executes(ctx -> HelpCommand.execute(ctx, dispatcher)); // Default action

        // Register all subcommands
        rootNode.then(HelpCommand.build(dispatcher));
        rootNode.then(CreateCommand.build(plugin));
        rootNode.then(EditCommand.build(plugin));
        rootNode.then(RemoveCommand.build(plugin));
        rootNode.then(ListCommand.build(plugin));
        rootNode.then(InfoCommand.build(plugin));
        rootNode.then(TeleportCommand.build(plugin));
        rootNode.then(RemoveAllCommand.build(plugin));
        rootNode.then(ReloadCommand.build(plugin));
        rootNode.then(VersionCommand.build(plugin));

        return rootNode;
    }
}
