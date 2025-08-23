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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;
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
        CommandSender sender = ctx.getSource().getSender();
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            plugin.getMessageService().sendMessage(sender, "error-command-player-only");
            return 0;
        }

        PortstoneManager manager = plugin.getPortstoneManager();

        // 1. Get the target Portstone
        Portstone portstone = manager.getPortstoneAt(player.getTargetBlock(null, 5).getLocation());
        if (portstone == null) {
            plugin.getMessageService().sendMessage(sender, "error-command-not-in-view");
            return 0;
        }

        // 2. Check for edit permissions
        if (!plugin.getTownyHook().canEdit(player, portstone)) {
            plugin.getMessageService().sendMessage(sender, "error-command-not-mayor");
            return 0;
        }

        // 3. Parse arguments
        String propertyArg = ctx.getArgument("property", String.class);
        Optional<PortstoneProperty> propertyOpt = PortstoneProperty.fromString(propertyArg);
        if (propertyOpt.isEmpty()) {
            plugin.getMessageService().sendMessage(sender, "error-command-invalid-property", Map.of("property", propertyArg));
            return 0;
        }

        PortstoneProperty property = propertyOpt.get();
        String value = ctx.getArgument("value", String.class);

        // 4. Perform the edit and fire the event
        switch (property) {
            case NAME -> {
                if (manager.isDisplayNameTaken(value, portstone.getId())) {
                    plugin.getMessageService().sendMessage(sender, "error-command-already-taken");
                    return 0;
                }

                String oldName = portstone.getDisplayName();
                portstone.setDisplayName(value);
                manager.savePortstone(portstone);
                Bukkit.getPluginManager().callEvent(new PortstonePropertyChangeEvent(portstone, property, oldName, value));
                plugin.getMessageService().sendMessage(sender, "success-command-name-edited");
            }
            case FEE -> {
                double oldFee = portstone.getTravelFee();
                try {
                    double newFee = Double.parseDouble(value);
                    if (newFee < 0) {
                        plugin.getMessageService().sendMessage(player, "error-command-invalid-value");
                        return 0;
                    }
                    portstone.setTravelFee(newFee);
                    manager.savePortstone(portstone);
                    Bukkit.getPluginManager().callEvent(new PortstonePropertyChangeEvent(portstone, property, oldFee, newFee));
                    plugin.getMessageService().sendMessage(sender, "success-command-fee-edited");
                } catch (NumberFormatException e) {
                    plugin.getMessageService().sendMessage(sender, "error-command-invalid-value", Map.of("value", value));
                    return 0;
                }
            }
            case ICON -> {
                Material oldIcon = portstone.getIcon();
                Material newIcon = Material.matchMaterial(value.toUpperCase());
                if (newIcon == null || !newIcon.isItem()) {
                    plugin.getMessageService().sendMessage(sender, "error-command-invalid-item", Map.of("value", value));
                    return 0;
                }
                portstone.setIcon(newIcon);
                manager.savePortstone(portstone);
                Bukkit.getPluginManager().callEvent(new PortstonePropertyChangeEvent(portstone, property, oldIcon, newIcon));
                plugin.getMessageService().sendMessage(sender, "success-command-icon-edited");
            }
            case ENABLED -> {
                boolean oldStatus = portstone.isEnabled();
                boolean newStatus = Boolean.parseBoolean(value);
                portstone.setEnabled(newStatus);
                manager.savePortstone(portstone);
                Bukkit.getPluginManager().callEvent(new PortstonePropertyChangeEvent(portstone, property, oldStatus, newStatus));
                plugin.getMessageService().sendMessage(sender, "success-command-status-edited");
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