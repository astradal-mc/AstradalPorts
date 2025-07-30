package net.astradal.astradalPorts.commands.children;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.commands.helpers.PortstonePermissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HelpCommand {

    private static final Map<String, String> COMMAND_DESCRIPTIONS = new LinkedHashMap<>() {{
        put("create", "Create a portstone of type air, land, or sea.");
        put("remove", "Remove the portstone you're looking at.");
        put("changeowner", "Change portstone town ownership.");
        put("setfee", "Set travel fee for a portstone.");
        put("list", "List all known portstones.");
        put("info", "Show information about the portstone you're looking at.");
        put("help", "Show this help message.");
    }};

    public static LiteralArgumentBuilder<CommandSourceStack> build(
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        return Commands.literal("help")
            .requires(PortstonePermissions.requires("help"))
            .executes(ctx -> execute(ctx, dispatcher));
    }

    public static int execute(
        CommandContext<CommandSourceStack> ctx,
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        CommandSender sender = ctx.getSource().getSender();

        // Find the /portstone root node
        CommandNode<CommandSourceStack> root = dispatcher.getRoot();
        CommandNode<CommandSourceStack> portstoneNode = root.getChildren().stream()
            .filter(child -> child instanceof LiteralCommandNode<?> literal && literal.getLiteral().equals("portstone"))
            .findFirst()
            .orElse(null);

        if (portstoneNode == null) {
            sender.sendMessage(Component.text("Could not find /portstone command node.", NamedTextColor.RED));
            return 0;
        }

        sender.sendMessage(Component.text("Portstone Commands:", NamedTextColor.GOLD));

        for (CommandNode<CommandSourceStack> child : portstoneNode.getChildren()) {
            if (!(child instanceof LiteralCommandNode<?> literal)) continue;
            String subcommand = literal.getLiteral();

            // Permission check (using just the literal name)
            if (!PortstonePermissions.has(sender, subcommand)) continue;

            String usage = "/portstone " + subcommand;
            String description = COMMAND_DESCRIPTIONS.getOrDefault(subcommand, "No description available.");

            sender.sendMessage(
                Component.text(usage, NamedTextColor.YELLOW)
                    .append(Component.text(" - " + description, NamedTextColor.GRAY))
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}
