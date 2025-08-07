package net.astradal.astradalPorts.util;

import net.astradal.astradalPorts.model.Portstone;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class PortstoneFormatter {
    private PortstoneFormatter() {} // Prevent instantiation

    public static Component getDisplayText(Portstone p) {
        return switch (p.getType().toLowerCase()) {
            case "air" -> Component.text("✈ Airship Port: ", NamedTextColor.AQUA)
                .append(Component.text(p.getDisplayName(), NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("Right click to use", NamedTextColor.GRAY));

            case "land" -> Component.text("⛰ Land Port: ", NamedTextColor.GREEN)
                .append(Component.text(p.getDisplayName(), NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("Right click to use", NamedTextColor.GRAY));

            case "sea" -> Component.text("⚓ Sea Port: ", NamedTextColor.BLUE)
                .append(Component.text(p.getDisplayName(), NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("Right click to use", NamedTextColor.GRAY));

            default -> Component.text("Portstone", NamedTextColor.WHITE)
                .appendNewline()
                .append(Component.text("Right click to use", NamedTextColor.GRAY));
        };
    }
}
