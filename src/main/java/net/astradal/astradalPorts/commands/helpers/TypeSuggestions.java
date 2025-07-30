package net.astradal.astradalPorts.commands.helpers;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class TypeSuggestions {
    private static final String[] PORT_TYPES = { "air", "land", "sea" };

    public static CompletableFuture<Suggestions> portType(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        for (String type : PORT_TYPES) {
            if (type.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(type);
            }
        }
        return builder.buildFuture();
    }

    //prevent instantiation
    private TypeSuggestions() {

    }
}
