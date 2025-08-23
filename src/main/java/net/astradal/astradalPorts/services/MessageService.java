package net.astradal.astradalPorts.services;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.Map;

/**
 * A centralized service for sending formatted messages to players.
 */
public class MessageService {

    private final ConfigService configService;
    private final MiniMessage miniMessage;

    public MessageService(ConfigService configService) {
        this.configService = configService;
        this.miniMessage = MiniMessage.miniMessage();
    }

    /**
     * Sends a configured message to a player, replacing placeholders.
     *
     * @param target       The CommandSender (Player, Console) to send the message to.
     * @param key          The key of the message in the config.yml.
     * @param placeholders A map of placeholders to their replacement values (e.g., "destination_name" -> "Northgate").
     */
    @SuppressWarnings("PatternValidation")
    public void sendMessage(CommandSender target, String key, Map<String, String> placeholders) {
        // Get the raw message string from the config
        String messageFormat = configService.getMessage(key, "<red>Missing message for key: " + key + "</red>");

        // Create a TagResolver to handle all placeholders
        TagResolver.Builder resolverBuilder = TagResolver.builder();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolverBuilder.resolver(Placeholder.unparsed(entry.getKey(), entry.getValue()));
        }

        // Deserialize the message with the placeholders
        Component message = miniMessage.deserialize(messageFormat, resolverBuilder.build());

        target.sendMessage(message);
    }

    // A convenience method for messages with no placeholders
    public void sendMessage(CommandSender target, String key) {
        sendMessage(target, key, Map.of());
    }

    /**
     * Gets a raw message string from the config and returns it as a formatted Component.
     *
     * @param key          The key of the message in the config.yml.
     * @param placeholders A map of placeholders to their replacement values.
     * @return The formatted, colored Component.
     */
    @SuppressWarnings("PatternValidation")
    public Component getFormattedComponent(String key, Map<String, String> placeholders) {
        String messageFormat = configService.getMessage(key, "<red>Missing message for key: " + key + "</red>");

        TagResolver.Builder resolverBuilder = TagResolver.builder();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            resolverBuilder.resolver(Placeholder.unparsed(entry.getKey(), entry.getValue()));
        }

        return miniMessage.deserialize(messageFormat, resolverBuilder.build());
    }

    // Convenience overload for no placeholders
    public Component getFormattedComponent(String key) {
        return getFormattedComponent(key, Map.of());
    }
}
