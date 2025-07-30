package net.astradal.astradalPorts.commands.children;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.commands.helpers.IdSuggestions;
import net.astradal.astradalPorts.commands.helpers.PortstonePermissions;
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class InfoCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin, PortstoneStorage storage) {
        return Commands.literal("info")
            .requires(PortstonePermissions.requires("info"))
            .executes(ctx -> executeTargeted(ctx, storage)) // player-looking-at block
            .then(Commands.argument("id", StringArgumentType.word())
                .suggests(IdSuggestions::portstoneIds)
                .executes(ctx -> executeById(ctx, storage)));
    }

    @SuppressWarnings("SameReturnValue")
    private static int executeTargeted(CommandContext<CommandSourceStack> ctx, PortstoneStorage portstoneStorage) {
        CommandSender sender = ctx.getSource().getSender();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.LODESTONE) {
            player.sendMessage(Component.text("You must be looking at a lodestone portstone.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Optional<Portstone> maybePortstone = portstoneStorage.getByLocation(target.getLocation());
        if (maybePortstone.isEmpty()) {
            player.sendMessage(Component.text("No portstone is registered at this location.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Portstone p = maybePortstone.get();
        printPortstoneInfo(sender, p);

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("SameReturnValue")
    private static int executeById(CommandContext<CommandSourceStack> ctx, PortstoneStorage storage) {
        CommandSender sender = ctx.getSource().getSender();
        String idStr = StringArgumentType.getString(ctx, "id");

        UUID id;
        try {
            id = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid UUID format.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Portstone p = storage.getById(id);
        if (p == null) {
            sender.sendMessage(Component.text("No portstone found with that ID.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        printPortstoneInfo(sender, p);
        return Command.SINGLE_SUCCESS;
    }

    private static void printPortstoneInfo(CommandSender sender, Portstone p) {
        sender.sendMessage(Component.text("Portstone Info:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("✦ ID: ", NamedTextColor.GRAY).append(Component.text(p.getId().toString(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("✦ Type: ", NamedTextColor.GRAY).append(Component.text(p.getType().toUpperCase(), NamedTextColor.AQUA)));
        sender.sendMessage(Component.text("✦ Name: ", NamedTextColor.GRAY).append(Component.text(p.getDisplayName(), NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text("✦ Town: ", NamedTextColor.GRAY).append(Component.text(p.getTown(), NamedTextColor.GREEN)));
        sender.sendMessage(Component.text("✦ Nation: ", NamedTextColor.GRAY).append(Component.text(p.getNation(), NamedTextColor.BLUE)));
        sender.sendMessage(Component.text("✦ Location: ", NamedTextColor.GRAY).append(Component.text(
            String.format("%s %.0f %.0f %.0f",
                p.getLocation().getWorld().getName(),
                p.getLocation().getX(),
                p.getLocation().getY(),
                p.getLocation().getZ()
            ), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("✦ Travel Fee: ", NamedTextColor.GRAY).append(Component.text("$" + p.getTravelFee(), NamedTextColor.GOLD)));
    }

}
