package net.astradal.astradalPorts.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the PortstonePermissions utility class.
 * This test uses Mockito to mock a CommandSender and verify permission checks.
 */
@ExtendWith(MockitoExtension.class)
public class PortstonePermissionsTest {

    @Mock
    private CommandSender mockSender; // Mocks a generic command sender (e.g., a Player or Console)

    @Mock
    private CommandSourceStack mockSource; // Mocks Brigadier's command source

    @Test
    void has_shouldReturnTrue_whenSenderHasPermission() {
        // Arrange: Tell the mock sender it has the specific permission node.
        when(mockSender.hasPermission("astradal.commands.portstone.create")).thenReturn(true);

        // Act: Call the method we are testing.
        boolean result = PortstonePermissions.has(mockSender, "create");

        // Assert: The result should be true.
        assertTrue(result, "Should return true when the sender has the permission.");
    }

    @Test
    void has_shouldReturnFalse_whenSenderLacksPermission() {
        // Arrange: Tell the mock sender it does NOT have the permission.
        when(mockSender.hasPermission("astradal.commands.portstone.remove")).thenReturn(false);

        // Act
        boolean result = PortstonePermissions.has(mockSender, "remove");

        // Assert
        assertFalse(result, "Should return false when the sender lacks the permission.");
    }

    @Test
    void requires_shouldReturnTruePredicate_whenSenderHasPermission() {
        // Arrange: Link the Brigadier source to our mock sender
        when(mockSource.getSender()).thenReturn(mockSender);
        // Set up the permission on the mock sender
        when(mockSender.hasPermission("astradal.commands.portstone.edit")).thenReturn(true);

        // Act: Get the predicate from the method we are testing.
        Predicate<CommandSourceStack> predicate = PortstonePermissions.requires("edit");

        // Assert: Test the predicate with our mock source.
        assertTrue(predicate.test(mockSource), "The predicate should return true.");
    }

    @Test
    void requires_shouldReturnFalsePredicate_whenSenderLacksPermission() {
        // Arrange
        when(mockSource.getSender()).thenReturn(mockSender);
        when(mockSender.hasPermission("astradal.commands.portstone.teleport")).thenReturn(false);

        // Act
        Predicate<CommandSourceStack> predicate = PortstonePermissions.requires("teleport");

        // Assert
        assertFalse(predicate.test(mockSource), "The predicate should return false.");
    }
}