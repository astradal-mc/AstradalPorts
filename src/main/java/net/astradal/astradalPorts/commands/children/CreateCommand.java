package net.astradal.astradalPorts.commands.children;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.helpers.PortstonePermissions;
import net.astradal.astradalPorts.commands.helpers.TypeSuggestions;
import net.astradal.astradalPorts.integration.TownyHook;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.astradal.astradalPorts.util.PortstoneFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class CreateCommand {

    private static final List<String> VALID_TYPES = List.of("air", "land", "sea");

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin, PortstoneStorage portstoneStorage) {
        return Commands.literal("create")
            .requires(PortstonePermissions.requires("create"))
            .then(Commands.argument("type", StringArgumentType.word())
                .suggests(TypeSuggestions::portType)
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

        String type = StringArgumentType.getString(ctx, "type".toLowerCase());
        if(!VALID_TYPES.contains(type)) {
            player.sendMessage(Component.text("Invalid portstone type. Must be: air, land, or sea.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.LODESTONE) {
            player.sendMessage(Component.text("YOu must be looking at a lodestone to register it as a portstone.", NamedTextColor.RED));
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


        String town = TownyHook.getTown(target.getLocation()).orElse("UnknownTown");
        String nation = TownyHook.getNation(target.getLocation()).orElse("NoNation");

        if (type.equals("air") && portstoneStorage.nationHasAirshipPort(nation)) {
            player.sendMessage(Component.text("This nation already has an airship port registered.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        UUID id = UUID.randomUUID();
        String displayName = target.getLocation().getBlockX() + ", " + target.getLocation().getBlockZ();
        double defaultFee = 0.0;

        Portstone portstone = new Portstone(
            id,
            type,
            target.getLocation(),
            town,
            nation,
            defaultFee,
            displayName
        );

        portstoneStorage.addPortstone(portstone);

        Component label = PortstoneFormatter.getDisplayText(portstone);
        plugin.getHologramService().showHologram(target.getLocation(), label, portstone.getId());

        sender.sendMessage(Component.text("Portstone of type '" + type + "' registered successfully!", NamedTextColor.GREEN));

        return Command.SINGLE_SUCCESS;
    }
}
