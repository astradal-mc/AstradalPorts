package net.astradal.astradalPorts.commands.children;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.helpers.PortstonePermissions;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public final class RemoveCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin, PortstoneStorage portstoneStorage) {
        return Commands.literal("remove")
            .requires(PortstonePermissions.requires("remove"))
            .executes(ctx -> executeTargeted(ctx, plugin, portstoneStorage))
            .then(Commands.argument("id", StringArgumentType.word())
                .executes(ctx -> executeByID(ctx, plugin, portstoneStorage))
            );
    }

    @SuppressWarnings("SameReturnValue")
    private static int executeTargeted(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin, PortstoneStorage storage) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command without an ID.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.LODESTONE) {
            player.sendMessage(Component.text("You must be looking at a lodestone portstone.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Location loc = target.getLocation();
        Optional<Portstone> maybePortstone = storage.getByLocation(loc);

        if (maybePortstone.isEmpty()) {
            player.sendMessage(Component.text("No portstone is registered at this location.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Portstone portstone = maybePortstone.get();
        storage.removePortstone(portstone.getId());
        plugin.getHologramService().removeHologram(portstone.getId());

        player.sendMessage(Component.text("Portstone removed.", NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("SameReturnValue")
    private static int executeByID(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin, PortstoneStorage portstoneStorage) {
        String idStr = StringArgumentType.getString(ctx, "id");
        CommandSender sender = ctx.getSource().getSender();

        UUID id;
        try {
            id = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid UUID format: " + idStr, NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (!portstoneStorage.portstoneExists(id)) {
            sender.sendMessage(Component.text("No portstone found with ID: " + idStr, NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        portstoneStorage.removePortstone(id);
        plugin.getHologramService().removeHologram(id);

        sender.sendMessage(Component.text("Portstone with ID '" + idStr + "' removed.", NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

}
