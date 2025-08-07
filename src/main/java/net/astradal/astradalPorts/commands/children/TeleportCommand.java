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
import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TeleportCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(AstradalPorts plugin, PortstoneStorage storage) {
        return Commands.literal("tp")
            .requires(PortstonePermissions.requires("tp"))
            .then(Commands.argument("target", StringArgumentType.greedyString())
                .suggests((c, b) -> suggestDisplayNames(b, storage))
                .executes(ctx -> execute(ctx, plugin, storage)));
    }

    private static CompletableFuture<Suggestions> suggestDisplayNames(SuggestionsBuilder builder, PortstoneStorage storage) {
        String remaining = builder.getRemainingLowerCase();

        for (Portstone p : storage.getAll()) {
            String displayName = p.getDisplayName();
            if(displayName.toLowerCase().startsWith(remaining)) {
                builder.suggest(displayName);
            }
        }

        return builder.buildFuture();
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, AstradalPorts plugin, PortstoneStorage storage) {
        CommandSourceStack src = ctx.getSource();
        String input = StringArgumentType.getString(ctx, "target");

        if (!(src.getSender() instanceof Player player)) {
            src.getSender().sendMessage(Component.text("Only players can use this command."));
            return 0;
        }

        Portstone portstone = findPortstone(storage, input);
        if (portstone == null) {
            src.getSender().sendMessage(Component.text("No portstone found with that ID or name.", NamedTextColor.RED));
            return 0;
        }

        player.teleportAsync(portstone.getLocation());
        src.getSender().sendMessage(Component.text("Teleported to " + portstone.getDisplayName(), NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }

    private static Portstone findPortstone(PortstoneStorage storage, String input) {
        // Try UUID match
        try {
            UUID id = UUID.fromString(input);
            return storage.getById(id);
        } catch (IllegalArgumentException ignored) {}

        // Try case-insensitive display name match
        return storage.getAll().stream()
            .filter(p -> p.getDisplayName().equalsIgnoreCase(input))
            .findFirst()
            .orElse(null);
    }
}