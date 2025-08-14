package net.astradal.astradalPorts.database.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

// No more @ExtendWith, it's inherited from the parent class!
public class HologramRepositoryTest extends BaseRepositoryTest {

    private HologramRepository repository;

    // We still need a @BeforeEach here to initialize the specific repository.
    @BeforeEach
    void initialize() {
        // The 'mockPlugin' and 'mockDbManager' fields are inherited and already
        // configured by the setup() method in BaseRepositoryTest.
        repository = new HologramRepository(Logger.getLogger("TestLogger"), mockDbManager);
    }

    // Test constants
    private static final String TEST_PORTSTONE_ID = "portstone-alpha";
    private static final UUID TEST_ENTITY_UUID = UUID.randomUUID();

    @Test
    void saveAndGet_shouldPersistAndReturnCorrectUuid() {
        repository.saveHologram(TEST_PORTSTONE_ID, TEST_ENTITY_UUID);
        UUID fetchedUuid = repository.getHologramEntityUuid(TEST_PORTSTONE_ID);
        assertEquals(TEST_ENTITY_UUID, fetchedUuid);
    }

    // ... all other test methods remain exactly the same ...
    @Test
    void updateHologram_shouldOverwriteExistingUuid() {
        UUID newEntityUuid = UUID.randomUUID();
        repository.saveHologram(TEST_PORTSTONE_ID, TEST_ENTITY_UUID);
        repository.saveHologram(TEST_PORTSTONE_ID, newEntityUuid);
        UUID fetchedUuid = repository.getHologramEntityUuid(TEST_PORTSTONE_ID);
        assertEquals(newEntityUuid, fetchedUuid);
    }

    @Test
    void getHologram_shouldReturnNullForNonExistentEntry() {
        String nonExistentId = "some-other-id";
        UUID fetchedUuid = repository.getHologramEntityUuid(nonExistentId);
        assertNull(fetchedUuid);
    }

    @Test
    void deleteHologram_shouldRemoveEntryFromDatabase() {
        repository.saveHologram(TEST_PORTSTONE_ID, TEST_ENTITY_UUID);
        assertNotNull(repository.getHologramEntityUuid(TEST_PORTSTONE_ID));
        repository.deleteHologram(TEST_PORTSTONE_ID);
        assertNull(repository.getHologramEntityUuid(TEST_PORTSTONE_ID));
    }
}