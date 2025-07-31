package net.astradal.astradalPorts.commands.children;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.astradal.astradalPorts.AstradalPorts;
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

public final class SetFeeCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin, PortstoneStorage storage) {
        return Commands.literal("setfee")
            .requires(PortstonePermissions.requires("setfee"))
            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                .executes(ctx -> execute(ctx, plugin, storage)));
    }

    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin, PortstoneStorage storage) {
        CommandSender sender = ctx.getSource().getSender();

        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null || target.getType() != Material.LODESTONE) {
            sender.sendMessage(Component.text("You must be looking at a lodestone portstone.", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        Optional<Portstone> maybe = storage.getByLocation(target.getLocation());
        if (maybe.isEmpty()) {
            sender.sendMessage(Component.text("No portstone is registered at this location", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        double fee = DoubleArgumentType.getDouble(ctx, "amount");
        Portstone p = maybe.get();
        p.setTravelFee(fee);

        storage.addPortstone(p);

        sender.sendMessage(Component.text("Set travel fee to $" + fee, NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }
}
