package net.astradal.astradalPorts.core;

import java.util.Optional;

/**
 * Represents the editable properties of a Portstone.
 */
public enum PortstoneProperty {
    NAME,
    FEE,
    ICON,
    ENABLED;

    /**
     * A case-insensitive way to get a property from a string.
     * @param value The string to parse.
     * @return An Optional containing the matching PortstoneProperty, or empty if no match.
     */
    public static Optional<PortstoneProperty> fromString(String value) {
        for (PortstoneProperty property : values()) {
            if (property.name().equalsIgnoreCase(value)) {
                return Optional.of(property);
            }
        }
        return Optional.empty();
    }
}