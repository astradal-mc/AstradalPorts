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
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class ListCommand  {
    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin, PortstoneStorage storage) {
        return Commands.literal("list")
            .requires(PortstonePermissions.requires("list"))
            .executes(ctx -> execute(ctx, plugin, storage, null)) // no filter
            .then(Commands.argument("type", StringArgumentType.word())
                .suggests(TypeSuggestions::portType)
                .executes(ctx -> {
                    String type = StringArgumentType.getString(ctx, "type").toLowerCase();
                    return execute(ctx, plugin, storage, type);
                }))
            ;
    }

    @SuppressWarnings("SameReturnValue")
    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin, PortstoneStorage storage, String typeFilter) {
        CommandSender sender = ctx.getSource().getSender();

        var portstones = (typeFilter == null)
            ? storage.getAll()
            : storage.getByType(typeFilter);

        if (portstones.isEmpty()) {
            sender.sendMessage(Component.text("No portstones found.", NamedTextColor.YELLOW));
            return Command.SINGLE_SUCCESS;
        }

        sender.sendMessage(Component.text(
            typeFilter == null ? "Registered Portstones:" : "Registered " + typeFilter.toUpperCase() + " Portstones:",
            NamedTextColor.GOLD
        ));

        for (Portstone p : portstones) {
            Component line = Component.text()
                .append(Component.text(getPortTypeSymbol(p.getType()) + " ", NamedTextColor.GRAY))
                .append(Component.text(p.getDisplayName(), NamedTextColor.YELLOW))
                .append(Component.text(" [" + p.getType().toUpperCase() + "]", NamedTextColor.DARK_GRAY))
                .hoverEvent(Component.text("Click to teleport"))
                .clickEvent(ClickEvent.runCommand(
                    String.format("/tppos %s %.1f %.1f %.1f",
                        p.getLocation().getWorld().getName(),
                        p.getLocation().getX(),
                        p.getLocation().getY(),
                        p.getLocation().getZ()
                    )
                ))
                .build();

            sender.sendMessage(line);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static String getPortTypeSymbol(String type) {
        return switch (type.toLowerCase()) {
            case "air" -> "✈";
            case "land" -> "⛰";
            case "sea" -> "⚓";
            default -> "❓";
        };
    }
}
