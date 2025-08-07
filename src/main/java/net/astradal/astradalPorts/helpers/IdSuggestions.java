package net.astradal.astradalPorts.helpers;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.astradal.astradalPorts.services.PortstoneStorage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class IdSuggestions {

    public static PortstoneStorage portstoneStorage;

    public static void setStorage(PortstoneStorage s) {
        portstoneStorage = s;
    }

    public static CompletableFuture<Suggestions> portstoneIds(
        CommandContext<CommandSourceStack> ctx,
        SuggestionsBuilder builder
    ) {
        if (portstoneStorage == null) return Suggestions.empty();

        for (UUID id : portstoneStorage.getAllIds()) {
            builder.suggest(id.toString());
        }

        return builder.buildFuture();
    }

    //prevent instantiation
    private IdSuggestions() {}
}
