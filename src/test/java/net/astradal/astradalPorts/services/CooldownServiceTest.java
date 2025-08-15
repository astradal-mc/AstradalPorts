package net.astradal.astradalPorts.services;

import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.database.repositories.CooldownRepository;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the CooldownService.
 * Uses Mockito to test the business logic in isolation.
 */
@ExtendWith(MockitoExtension.class)
public class CooldownServiceTest {

    @Mock
    private CooldownRepository mockCooldownRepository;
    @Mock
    private ConfigService mockConfigService;
    @Mock
    private Player mockPlayer;

    private CooldownService cooldownService;
    private final UUID playerUUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        cooldownService = new CooldownService(mockCooldownRepository, mockConfigService);
        when(mockPlayer.getUniqueId()).thenReturn(playerUUID);
    }

    @Test
    void savePlayerCooldowns_shouldCallRepositoryWithCorrectData() {
        // Arrange: Put the player on a cooldown
        cooldownService.applyCooldown(mockPlayer, PortType.AIR);

        // Act: Save the player's data
        cooldownService.savePlayerCooldowns(playerUUID);

        // Assert: Verify that the repository's save method was called with the correct info
        // We use eq() for specific values and anyLong() for the timestamp since we can't know the exact millisecond.
        verify(mockCooldownRepository).saveLastUse(eq(playerUUID), eq("AIR"), anyLong());
    }


    @Test
    void isOnCooldown_shouldReturnFalse_whenNotOnCooldown() {
        // Arrange
        when(mockConfigService.getCooldown("LAND")).thenReturn(60);

        // Act
        boolean onCooldown = cooldownService.isOnCooldown(mockPlayer, PortType.LAND);

        // Assert
        assertFalse(onCooldown);
    }


    @Test
    void applyCooldown_then_isOnCooldown_shouldReturnTrue() {
        // Arrange
        when(mockConfigService.getCooldown("LAND")).thenReturn(60);

        // Act
        cooldownService.applyCooldown(mockPlayer, PortType.LAND);
        boolean onCooldown = cooldownService.isOnCooldown(mockPlayer, PortType.LAND);

        // Assert
        assertTrue(onCooldown);
    }

    @Test
    void getRemainingSeconds_shouldCalculateCorrectly_whenOnCooldown() {
        // Arrange
        // Simulate that the config specifies a 60-second cooldown
        when(mockConfigService.getCooldown("SEA")).thenReturn(60);
        // Simulate that the player last used this 15 seconds ago
        long fifteenSecondsAgo = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(15);
        // Tell our mock repository that this is the data it has saved for the player
        when(mockCooldownRepository.getCooldowns(playerUUID)).thenReturn(Map.of("SEA", fifteenSecondsAgo));

        // Act
        // Load the data into the service using its public method, just like a real PlayerJoinEvent would
        cooldownService.loadPlayerCooldowns(playerUUID);
        // Now, test the public getRemainingSeconds method
        long remaining = cooldownService.getRemainingSeconds(mockPlayer, PortType.SEA);

        // Assert
        // The remaining time should be 45 seconds (60 - 15)
        assertTrue(remaining >= 44 && remaining <= 45, "Remaining time should be approximately 45 seconds.");
    }

    @Test
    void getRemainingSeconds_shouldReturnZero_whenCooldownExpired() {
        // Arrange
        when(mockConfigService.getCooldown("AIR")).thenReturn(30);
        long fortySecondsAgo = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(40);
        when(mockCooldownRepository.getCooldowns(playerUUID)).thenReturn(Map.of("AIR", fortySecondsAgo));

        // Act
        cooldownService.loadPlayerCooldowns(playerUUID);
        long remaining = cooldownService.getRemainingSeconds(mockPlayer, PortType.AIR);

        // Assert
        assertEquals(0, remaining, "Remaining time should be 0 if the cooldown has expired.");
    }

    @Test
    void loadPlayerCooldowns_shouldPopulateCacheFromRepository() {
        // Arrange
        long timestamp = System.currentTimeMillis();
        when(mockCooldownRepository.getCooldowns(playerUUID)).thenReturn(Map.of("LAND", timestamp));
        // We also need to mock the config for this to work when we check the remaining time
        when(mockConfigService.getCooldown("LAND")).thenReturn(60);

        // Act
        cooldownService.loadPlayerCooldowns(playerUUID);

        // Assert by checking the public behavior, not the private state
        assertTrue(cooldownService.isOnCooldown(mockPlayer, PortType.LAND));
    }
}