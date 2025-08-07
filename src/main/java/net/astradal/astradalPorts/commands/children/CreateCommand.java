package net.astradal.astradalPorts.commands.children;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.helpers.PortstonePermissions;
import net.astradal.astradalPorts.helpers.TypeSuggestions;
import net.astradal.astradalPorts.integration.TownyHook;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.astradal.astradalPorts.util.PortstoneFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CreateCommand {

    private static final List<String> VALID_TYPES = List.of("air", "land", "sea");

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin, PortstoneStorage portstoneStorage) {
        return Commands.literal("create")
            .requires(PortstonePermissions.requires("create"))
            .then(Commands.argument("type", StringArgumentType.word())
                .suggests(TypeSuggestions::portType)
                .then(Commands.argument("name", StringArgumentType.greedyString())
                    .executes(ctx -> execute(ctx, plugin, portstoneStorage))
                )
                .executes(ctx -> execute(ctx, plugin, portstoneStorage))
            );
    }

    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin, PortstoneStorage portstoneStorage) {
        CommandSender sender = ctx.getSource().getSender();

        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can create portstones.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        String type = StringArgumentType.getString(ctx, "type").toLowerCase();
        if(!VALID_TYPES.contains(type)) {
            player.sendMessage(Component.text("Invalid portstone type. Must be: air, land, or sea.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.LODESTONE) {
            player.sendMessage(Component.text("You must be looking at a lodestone to register it as a portstone.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (portstoneStorage.isPortstone(target.getLocation())) {
            player.sendMessage(Component.text("This lodestone is already registered as a portstone.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (!TownyHook.isClaimed(target.getLocation())) {
            player.sendMessage(Component.text("Portstones can only be created on Towny-claimed land.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Optional<String> town = TownyHook.getTown(target.getLocation());
        String townName = town.orElse(null);

        Optional<String> nation = TownyHook.getNation(target.getLocation());
        String nationName = nation.orElse(null);

        if (type.equals("air")) {
            if (nationName == null || nationName.isEmpty()) {
                player.sendMessage(Component.text("Airship ports can only be created in towns that are part of a nation.", NamedTextColor.RED));
                return Command.SINGLE_SUCCESS;
            }

            if (portstoneStorage.nationHasAirshipPort(nationName)) {
                player.sendMessage(Component.text("This nation already has an airship port registered.", NamedTextColor.RED));
                return Command.SINGLE_SUCCESS;
            }
        }

        if ((type.equals("land") || type.equals("sea")) && townName != null) {
            boolean alreadyExists = portstoneStorage.getAll().stream()
                .anyMatch(p -> p.getType().equalsIgnoreCase(type) && townName.equalsIgnoreCase(p.getTown()));
            if (alreadyExists) {
                player.sendMessage(Component.text("This town already has a " + type + " portstone registered.", NamedTextColor.RED));
                return Command.SINGLE_SUCCESS;
            }
        }

        UUID id = UUID.randomUUID();

        String customName = ctx.getNodes().stream().anyMatch(n -> n.getNode().getName().equals("name"))
            ? StringArgumentType.getString(ctx, "name")
            : null;

        // Prevent naming a portstone the same name as another portstone
        if (portstoneStorage.isDisplayNameTaken(customName, null)) {
            player.sendMessage(Component.text("That portstone name is already in use.", NamedTextColor.RED));
            return 0;
        }

        String displayName = determineDisplayName(customName, type, townName, nationName);

        double defaultFee = 0.0;
        Material defaultIcon = Material.LODESTONE;

        Portstone portstone = new Portstone(
            id,
            type,
            target.getLocation(),
            townName,
            nationName,
            defaultFee,
            displayName,
            defaultIcon
        );

        portstoneStorage.addPortstone(portstone);

        Component label = PortstoneFormatter.getDisplayText(portstone);
        plugin.getHologramService().showHologram(target.getLocation(), label, portstone.getId());

        sender.sendMessage(Component.text("Portstone of type '" + type + "' registered successfully!", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }

    private static String determineDisplayName(String customName, String type, String townName, String nationName) {
        if (customName != null && !customName.isBlank()) {
            return customName;
        }

        if (type.equals("air") && nationName != null) {
            return nationName + " Air Portstone";
        }

        if ((type.equals("land") || type.equals("sea")) && townName != null) {
            return townName + " " + StringUtils.capitalize(type) + " Portstone";
        }

        return StringUtils.capitalize(type) + " Portstone";
    }

}
