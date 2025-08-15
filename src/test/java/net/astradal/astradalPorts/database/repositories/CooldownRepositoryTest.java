package net.astradal.astradalPorts.database.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class CooldownRepositoryTest extends BaseRepositoryTest {

    private CooldownRepository repository;

    private static final UUID TEST_PLAYER_UUID = UUID.randomUUID();

    @BeforeEach
    void initialize() {
        repository = new CooldownRepository(Logger.getLogger("TestLogger"), mockDbManager);
    }

    @Test
    void saveAndGetLastUse_shouldPersistAndReturnCorrectTimestamp() {
        long expectedTimestamp = System.currentTimeMillis();
        repository.saveLastUse(TEST_PLAYER_UUID, "land", expectedTimestamp);
        long actualTimestamp = repository.getLastUse(TEST_PLAYER_UUID, "land");
        assertEquals(expectedTimestamp, actualTimestamp);
    }

    @Test
    void updateLastUse_shouldOverwriteExistingTimestamp() {
        repository.saveLastUse(TEST_PLAYER_UUID, "sea", 1000L);
        repository.saveLastUse(TEST_PLAYER_UUID, "sea", 5000L);
        long actualTimestamp = repository.getLastUse(TEST_PLAYER_UUID, "sea");
        assertEquals(5000L, actualTimestamp);
    }

    @Test
    void getLastUse_shouldBeCaseInsensitive() {
        long expectedTimestamp = System.currentTimeMillis();
        repository.saveLastUse(TEST_PLAYER_UUID, "land", expectedTimestamp);
        long actualTimestamp = repository.getLastUse(TEST_PLAYER_UUID, "LAND");
        assertEquals(expectedTimestamp, actualTimestamp);
    }

    @Test
    void getCooldowns_shouldReturnAllCooldownsForPlayer() {
        repository.saveLastUse(TEST_PLAYER_UUID, "land", 12345L);
        repository.saveLastUse(TEST_PLAYER_UUID, "sea", 67890L);
        repository.saveLastUse(UUID.randomUUID(), "land", 99999L);
        Map<String, Long> cooldowns = repository.getCooldowns(TEST_PLAYER_UUID);
        assertEquals(2, cooldowns.size());
        assertEquals(12345L, cooldowns.get("land"));
        assertEquals(67890L, cooldowns.get("sea"));
    }

    @Test
    void deleteCooldown_shouldRemoveEntryFromDatabase() {
        repository.saveLastUse(TEST_PLAYER_UUID, "air", 1L);
        assertNotEquals(0L, repository.getLastUse(TEST_PLAYER_UUID, "air"));
        repository.deleteCooldown(TEST_PLAYER_UUID, "air");
        assertEquals(0L, repository.getLastUse(TEST_PLAYER_UUID, "air"));
    }
}