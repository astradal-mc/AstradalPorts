package net.astradal.astradalPorts.commands.children;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.helpers.PortstonePermissions;
import net.astradal.astradalPorts.integration.TownyHook;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


public final class EditCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin, PortstoneStorage storage) {
        return Commands.literal("edit")
            .requires(PortstonePermissions.requires("edit"))
            .then(Commands.argument("property", StringArgumentType.word())
                .suggests((c, b) -> suggest(b, "name", "fee", "icon"))
                .then(Commands.argument("value", StringArgumentType.greedyString())
                    .suggests((context, builder) -> {
                        String prop = StringArgumentType.getString(context, "property");
                        if (!prop.equalsIgnoreCase("icon")) return builder.buildFuture();

                        // Suggest only item-like materials
                        for (Material material : Material.values()) {
                            if (material.isItem()) {
                                builder.suggest(material.name().toLowerCase());
                            }
                        }
                        return builder.buildFuture();
                    })
                    .executes(ctx -> executeEdit(ctx, plugin, storage))));

    }

    private static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, String... options) {
        for (String option : options) {
            if (option.startsWith(builder.getRemaining())) {
                builder.suggest(option);
            }
        }
        return builder.buildFuture();
    }

    private static int executeEdit(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin, PortstoneStorage storage) {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return 0;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.LODESTONE) {
            sender.sendMessage(Component.text("You must be looking at a portstone.", NamedTextColor.RED));
            return 0;
        }

        Optional<Portstone> opt = storage.getByLocation(target.getLocation());
        if (opt.isEmpty()) {
            sender.sendMessage(Component.text("That lodestone is not a portstone.", NamedTextColor.RED));
            return 0;
        }

        Portstone portstone = opt.get();

        if (!sender.hasPermission("astradalports.admin") && !TownyHook.isMayor(player, portstone.getTown())) {
            sender.sendMessage(Component.text("Only the town mayor or an admin may edit this portstone.", NamedTextColor.RED));
            return 0;
        }

        String property = StringArgumentType.getString(ctx, "property").toLowerCase();
        String value = StringArgumentType.getString(ctx, "value").trim();

        switch (property) {
            case "name" -> {
                portstone.setDisplayName(value);
                sender.sendMessage(Component.text("Portstone name updated to '" + value + "'.", NamedTextColor.GREEN));
            }
            case "fee" -> {
                try {
                    double fee = Double.parseDouble(value);
                    portstone.setTravelFee(fee);
                    sender.sendMessage(Component.text("Portstone fee updated to $" + fee, NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Invalid number: " + value, NamedTextColor.RED));
                    return 0;
                }
            }
            case "icon" -> {
                Material newIcon;

                if (value.isBlank()) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand.getType().isAir()) {
                        player.sendMessage(Component.text("Hold an item or specify a material to use as the portstone icon.", NamedTextColor.RED));
                        return 0;
                    }
                    newIcon = hand.getType();
                } else {
                    newIcon = Material.matchMaterial(value.toUpperCase());
                    if (newIcon == null || !newIcon.isItem()) {
                        player.sendMessage(Component.text("Invalid material: " + value, NamedTextColor.RED));
                        return 0;
                    }
                }

                portstone.setIcon(newIcon);
                storage.save(portstone);
                player.sendMessage(Component.text("Portstone icon updated to " + newIcon.name(), NamedTextColor.GREEN));
            }
            default -> {
                sender.sendMessage(Component.text("Unknown property: " + property, NamedTextColor.RED));
                return 0;
            }
        }

        storage.save(portstone);
        return Command.SINGLE_SUCCESS;
    }


}
