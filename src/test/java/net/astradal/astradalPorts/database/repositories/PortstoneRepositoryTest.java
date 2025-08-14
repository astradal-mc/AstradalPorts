package net.astradal.astradalPorts.database.repositories;

import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.core.Portstone;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class PortstoneRepositoryTest extends BaseRepositoryTest {

    private PortstoneRepository repository;

    @BeforeEach
    void initialize() {
        // The 'mockDbManager' field is inherited from BaseRepositoryTest.
        // We pass a simple logger since this repo doesn't depend on the plugin instance directly.
        repository = new PortstoneRepository(Logger.getLogger("TestLogger"), mockDbManager);
    }

    private Portstone createTestPortstone(String name) {
        return new Portstone(
            UUID.randomUUID(), PortType.LAND, "world",
            100.0, 64.0, 100.0,
            "TestTown", "TestNation", name,
            10.0, Material.DIAMOND_BLOCK
        );
    }

    @Test
    void saveAndGetById_shouldPersistAndRetrieve() {
        Portstone original = createTestPortstone("Port Alpha");
        repository.savePortstone(original);
        Portstone fetched = repository.getPortstoneById(original.getIdAsString());
        assertNotNull(fetched);
        assertEquals(original.getId(), fetched.getId());
    }

    @Test
    void updatePortstone_shouldOverwriteFields() {
        Portstone original = createTestPortstone("Old Name");
        repository.savePortstone(original);
        Portstone toUpdate = new Portstone(original.getId(), PortType.LAND, "world", 100.0, 64.0, 100.0, "TestTown", "TestNation", "New Name", 25.5, Material.EMERALD_BLOCK);
        repository.savePortstone(toUpdate);
        Portstone updated = repository.getPortstoneById(original.getIdAsString());
        assertNotNull(updated);
        assertEquals("New Name", updated.getDisplayName());
        assertEquals(25.5, updated.getTravelFee());
    }

    @Test
    void getAllPortstones_shouldReturnAllEntries() {
        repository.savePortstone(createTestPortstone("Port One"));
        repository.savePortstone(createTestPortstone("Port Two"));
        List<Portstone> all = repository.getAllPortstones();
        assertEquals(2, all.size());
    }

    @Test
    void deletePortstone_shouldRemoveEntry() {
        Portstone portstone = createTestPortstone("Port To Delete");
        repository.savePortstone(portstone);
        assertNotNull(repository.getPortstoneById(portstone.getIdAsString()));
        repository.deletePortstone(portstone.getIdAsString());
        assertNull(repository.getPortstoneById(portstone.getIdAsString()));
    }
}