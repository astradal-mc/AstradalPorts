package net.astradal.astradalPorts.core;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the different types of portstones available.
 */
public enum PortType {
    AIR,
    LAND,
    SEA;

    /**
     * A case-insensitive way to get a portstone type from a string.
     *
     * @param value The string to parse.
     * @return An Optional containing the matching PortType, or empty if no match.
     */
    public static Optional<PortType> fromString(String value) {
        for (PortType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * Provides tab-completion suggestions for this enum's values.
     * This method filters suggestions based on the user's current input.
     *
     * @param builder The suggestions builder provided by Brigadier.
     * @return A future containing the filtered suggestions.
     */
    public static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();

        Arrays.stream(PortType.values())
            .map(Enum::name) // Get the string names: "AIR", "LAND", "SEA"
            .filter(name -> name.toLowerCase().startsWith(remaining)) // Find matches
            .forEach(builder::suggest); // Add matches to suggestions

        return builder.buildFuture();
    }

    /**
     * Gets the default icon material for a portstone.
     * <p>
     * This provides a standard fallback icon to be used during portstone creation
     * or if a configured icon is invalid.
     *
     * @return The default material, which is always {@code Material.LODESTONE}.
     */
    public Material getDefaultIcon() {
        return Material.LODESTONE;
    }
}