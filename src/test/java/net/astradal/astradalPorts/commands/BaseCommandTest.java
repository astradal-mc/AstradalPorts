package net.astradal.astradalPorts.commands;

import io.papermc.paper.entity.LookAnchor;
import org.mockbukkit.mockbukkit.MockBukkit;
import net.astradal.astradalPorts.PluginTestHelper;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.core.PortType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

/**
 * An abstract base class for command tests that handles the MockBukkit server setup.
 */
public abstract class BaseCommandTest {

    protected PluginTestHelper.TestContext context;

    @BeforeEach
    public void setUp() {
        context = PluginTestHelper.setupTestEnvironment();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * A reusable helper method to create a portstone and place it in the world for tests.
     * @return The newly created Portstone.
     */
    protected Portstone createAndPlaceTestPortstone(String name, Location loc) {
        Block block = loc.getBlock();
        block.setType(Material.LODESTONE);
        context.player.lookAt(loc.getX(), loc.getY(), loc.getZ(), LookAnchor.EYES);

        Portstone portstone = new Portstone(
            UUID.randomUUID(), PortType.LAND,
            loc.getWorld().getName(),
            loc.getX(), loc.getY(), loc.getZ(),
            "TestTown", null, name, 0.0, Material.DIAMOND_BLOCK, true
        );
        context.portstoneManager.savePortstone(portstone);
        return portstone;
    }
}