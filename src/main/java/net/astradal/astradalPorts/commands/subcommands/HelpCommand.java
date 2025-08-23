package net.astradal.astradalPorts.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HelpCommand {

    // Updated map with all current subcommands
    private static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>() {{
        put("create", "Creates a new portstone from lodestone.");
        put("edit", "Edits the properties of an existing portstone.");
        put("remove", "Removes the portstone you are looking at.");
        put("removeall", "Removes all registered portstones. Use with caution.");
        put("list", "Lists all available portstones.");
        put("info", "Shows information about the portstone you're looking at.");
        put("teleport", "Opens the GUI to teleport to another portstone.");
        put("reload", "Reloads the plugin configuration.");
        put("version", "Displays the plugin version.");
        put("help", "Shows this help message.");
    }};

    /**
     * Builds the '/portstone help' command node.
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build(CommandDispatcher<CommandSourceStack> dispatcher, AstradalPorts plugin) {
        return Commands.literal("help")
            .requires(PortstonePermissions.requires("help"))
            .executes(ctx -> execute(ctx, dispatcher, plugin));
    }

    /**
     * Executes the help command logic.
     */
    public static int execute(CommandContext<CommandSourceStack> ctx, CommandDispatcher<CommandSourceStack> dispatcher, AstradalPorts plugin) {
        CommandSender sender = ctx.getSource().getSender();

        sender.sendMessage(Component.text("--- AstradalPorts Help ---", NamedTextColor.GOLD));

        // Find the '/portstone' command node in the command tree
        CommandNode<CommandSourceStack> portstoneNode = dispatcher.getRoot().getChild("portstone");

        if (portstoneNode == null) {
            plugin.getMessageService().sendMessage(sender, "error-command-no-info");
            return 0;
        }

        // Iterate through all registered subcommands of /portstone
        for (CommandNode<CommandSourceStack> node : portstoneNode.getChildren()) {
            if (!(node instanceof LiteralCommandNode<?> literalNode)) continue;

            String subcommand = literalNode.getLiteral();

            // Only show commands the player has permission to use
            if (!PortstonePermissions.has(sender, subcommand)) continue;

            String description = DESCRIPTIONS.getOrDefault(subcommand, plugin.getConfigService().getMessage("error-command-no-description", "No description available."));
            String usage = "/portstone " + subcommand;

            sender.sendMessage(
                Component.text(usage, NamedTextColor.YELLOW)
                    .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(description, NamedTextColor.GRAY))
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}