package net.astradal.astradalPorts.commands.subcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.core.PortstoneProperty;
import net.astradal.astradalPorts.events.PortstonePropertyChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
            .then(Commands.argument("property", StringArgumentType.word())
                .suggests((c, b) -> suggestProperties(b))
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
                    .executes(ctx -> execute(ctx, plugin))));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendMessage(Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return 0;
        }

        PortstoneManager manager = plugin.getPortstoneManager();

        // 1. Get the target Portstone
        Portstone portstone = manager.getPortstoneAt(player.getTargetBlock(null, 5).getLocation());
        if (portstone == null) {
            player.sendMessage(Component.text("You are not looking at a portstone.", NamedTextColor.RED));
            return 0;
        }

        // 2. Check for edit permissions
        if (!plugin.getTownyHook().canEdit(player, portstone)) {
            player.sendMessage(Component.text("You do not have permission to edit this portstone.", NamedTextColor.RED));
            return 0;
        }

        // 3. Parse arguments
        String propertyArg = ctx.getArgument("property", String.class);
        Optional<PortstoneProperty> propertyOpt = PortstoneProperty.fromString(propertyArg);
        if (propertyOpt.isEmpty()) {
            player.sendMessage(Component.text("Unknown property '" + propertyArg + "'.", NamedTextColor.RED));
            return 0;
        }

        PortstoneProperty property = propertyOpt.get();
        String value = ctx.getArgument("value", String.class);

        // 4. Perform the edit and fire the event
        switch (property) {
            case NAME -> {
                if (manager.isDisplayNameTaken(value, portstone.getId())) {
                    player.sendMessage(Component.text("That display name is already in use by another portstone.", NamedTextColor.RED));
                    return 0;
                }

                String oldName = portstone.getDisplayName();
                portstone.setDisplayName(value);
                manager.savePortstone(portstone);
                Bukkit.getPluginManager().callEvent(new PortstonePropertyChangeEvent(portstone, property, oldName, value));
                player.sendMessage(Component.text("Portstone name updated.", NamedTextColor.GREEN));
            }
            case FEE -> {
                double oldFee = portstone.getTravelFee();
                try {
                    double newFee = Double.parseDouble(value);
                    if (newFee < 0) {
                        player.sendMessage(Component.text("Fee cannot be negative.", NamedTextColor.RED));
                        return 0;
                    }
                    portstone.setTravelFee(newFee);
                    manager.savePortstone(portstone);
                    Bukkit.getPluginManager().callEvent(new PortstonePropertyChangeEvent(portstone, property, oldFee, newFee));
                    player.sendMessage(Component.text("Portstone fee updated.", NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    player.sendMessage(Component.text("'" + value + "' is not a valid number.", NamedTextColor.RED));
                    return 0;
                }
            }
            case ICON -> {
                Material oldIcon = portstone.getIcon();
                Material newIcon = Material.matchMaterial(value.toUpperCase());
                if (newIcon == null || !newIcon.isItem()) {
                    player.sendMessage(Component.text("'" + value + "' is not a valid item.", NamedTextColor.RED));
                    return 0;
                }
                portstone.setIcon(newIcon);
                manager.savePortstone(portstone);
                Bukkit.getPluginManager().callEvent(new PortstonePropertyChangeEvent(portstone, property, oldIcon, newIcon));
                player.sendMessage(Component.text("Portstone icon updated.", NamedTextColor.GREEN));
            }
            case ENABLED -> {
                boolean oldStatus = portstone.isEnabled();
                boolean newStatus = Boolean.parseBoolean(value);
                portstone.setEnabled(newStatus);
                manager.savePortstone(portstone);
                Bukkit.getPluginManager().callEvent(new PortstonePropertyChangeEvent(portstone, property, oldStatus, newStatus));
                player.sendMessage(Component.text("Portstone status updated.", NamedTextColor.GREEN));
            }
        }

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