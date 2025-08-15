package net.astradal.astradalPorts.database.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class HologramRepositoryTest extends BaseRepositoryTest {

    private HologramRepository repository;

    private static final UUID TEST_PORTSTONE_ID = UUID.randomUUID();
    private static final UUID TEST_ENTITY_UUID = UUID.randomUUID();

    @BeforeEach
    void initialize() {
        repository = new HologramRepository(Logger.getLogger("TestLogger"), mockDbManager);
    }

    @Test
    void saveAndGet_shouldPersistAndReturnCorrectUuid() {
        repository.saveHologram(TEST_PORTSTONE_ID.toString(), TEST_ENTITY_UUID);
        UUID fetchedUuid = repository.getHologramEntityUuid(TEST_PORTSTONE_ID.toString());
        assertEquals(TEST_ENTITY_UUID, fetchedUuid);
    }

    @Test
    void updateHologram_shouldOverwriteExistingUuid() {
        UUID newEntityUuid = UUID.randomUUID();
        repository.saveHologram(TEST_PORTSTONE_ID.toString(), TEST_ENTITY_UUID);
        repository.saveHologram(TEST_PORTSTONE_ID.toString(), newEntityUuid);
        UUID fetchedUuid = repository.getHologramEntityUuid(TEST_PORTSTONE_ID.toString());
        assertEquals(newEntityUuid, fetchedUuid);
    }

    @Test
    void getHologram_shouldReturnNullForNonExistentEntry() {
        UUID nonExistentId = UUID.randomUUID();
        UUID fetchedUuid = repository.getHologramEntityUuid(nonExistentId.toString());
        assertNull(fetchedUuid);
    }

    @Test
    void deleteHologram_shouldRemoveEntryFromDatabase() {
        repository.saveHologram(TEST_PORTSTONE_ID.toString(), TEST_ENTITY_UUID);
        assertNotNull(repository.getHologramEntityUuid(TEST_PORTSTONE_ID.toString()));
        repository.deleteHologram(TEST_PORTSTONE_ID.toString());
        assertNull(repository.getHologramEntityUuid(TEST_PORTSTONE_ID.toString()));
    }

    @Test
    void getAllHolograms_shouldReturnAllEntriesAsMap() {
        // Arrange
        UUID portstoneId2 = UUID.randomUUID();
        UUID entityId2 = UUID.randomUUID();
        repository.saveHologram(TEST_PORTSTONE_ID.toString(), TEST_ENTITY_UUID);
        repository.saveHologram(portstoneId2.toString(), entityId2);

        // Act
        Map<UUID, UUID> allHolograms = repository.getAllHolograms();

        // Assert
        assertNotNull(allHolograms);
        assertEquals(2, allHolograms.size());
        assertEquals(TEST_ENTITY_UUID, allHolograms.get(TEST_PORTSTONE_ID));
        assertEquals(entityId2, allHolograms.get(portstoneId2));
    }
}