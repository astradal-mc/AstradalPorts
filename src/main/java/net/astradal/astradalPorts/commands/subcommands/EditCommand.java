package net.astradal.astradalPorts.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.astradal.astradalPorts.core.PortstoneProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class EditCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin) {
        return Commands.literal("edit")
            .requires(PortstonePermissions.requires("edit"))
            .then(Commands.argument("value", StringArgumentType.greedyString())
                .suggests((context, builder) -> {
                    String prop = context.getArgument("property", String.class);
                    if (prop.equalsIgnoreCase("ICON")) {
                        return suggestMaterials(builder);
                    }
                    if (prop.equalsIgnoreCase("ENABLED")) {
                        return suggestBooleans(builder);
                    }
                    return Suggestions.empty();
                })
                .executes(ctx -> executeEdit(ctx, plugin)));
    }

    private static int executeEdit(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) throws CommandSyntaxException {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return 0;
        }

        String propertyArg = ctx.getArgument("property", String.class);
        Optional<PortstoneProperty> propertyOpt = PortstoneProperty.fromString(propertyArg);

        if (propertyOpt.isEmpty()) {
            player.sendMessage(Component.text("Unknown property '" + propertyArg + "'. Use NAME, FEE, or ICON.", NamedTextColor.RED));
            return 0;
        }

        PortstoneProperty property = propertyOpt.get();
        String value = ctx.getArgument("value", String.class);

        // This part needs to be filled with your PortstoneManager logic
        // For example:
        // Portstone portstone = plugin.getPortstoneManager().getPortstoneAt(player.getTargetBlock(null, 5).getLocation());
        // ... permission checks ...
        // switch(property) { ... }

        player.sendMessage(Component.text("Edit command executed (logic to be implemented).", NamedTextColor.YELLOW));
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Provides suggestions for the PortstoneProperty enum based on user input.
     *
     * @param builder The suggestions builder.
     * @return A future containing the suggestions.
     */
    private static CompletableFuture<Suggestions> suggestProperties(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        // Stream all enum values, get their names, filter by what the user has typed, and suggest them.
        Arrays.stream(PortstoneProperty.values())
            .map(Enum::name)
            .filter(name -> name.toLowerCase().startsWith(remaining))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    /**
     * Provides suggestions for item materials based on user input.
     *
     * @param builder The suggestions builder.
     * @return A future containing the suggestions.
     */
    private static CompletableFuture<Suggestions> suggestMaterials(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        Registry.MATERIAL.stream()
            .filter(Material::isItem)
            .map(material -> material.getKey().getKey())
            .filter(key -> key.toLowerCase().startsWith(remaining))
            .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestBooleans(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        if ("true".startsWith(remaining)) {
            builder.suggest("true");
        }
        if ("false".startsWith(remaining)) {
            builder.suggest("false");
        }
        return builder.buildFuture();
    }
}