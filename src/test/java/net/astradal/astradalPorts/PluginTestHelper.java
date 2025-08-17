package net.astradal.astradalPorts;

import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.services.ConfigService;
import net.astradal.astradalPorts.services.hooks.EconomyHook;
import org.bukkit.entity.Player;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.database.DatabaseManager;
import net.astradal.astradalPorts.database.repositories.PortstoneRepository;
import net.astradal.astradalPorts.services.hooks.TownyHook;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

/**
 * A utility class for setting up a standardized test environment for AstradalPorts.
 */
public final class PluginTestHelper {

    /**
     * A container for all the components needed for a test.
     */
    public static class TestContext {
        public ServerMock server;
        public AstradalPorts plugin;
        public PlayerMock player;
        public PortstoneManager portstoneManager;
        public FakeTownyHook fakeTownyHook;
        public FakeEconomyHook fakeEconomyHook;
    }

    /**
     * Sets up a complete mock server environment with the plugin loaded.
     * It uses a real in-memory database and provides fake hooks for external dependencies.
     * @return A TestContext object containing all the necessary components for a test.
     */
    public static TestContext setupTestEnvironment() {
        TestContext context = new TestContext();

        // 1. Setup MockBukkit
        context.server = MockBukkit.mock();
        context.plugin = MockBukkit.load(AstradalPorts.class);
        context.player = context.server.addPlayer();

        // 2. Set up a real, in-memory database
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite::memory:", context.plugin.getLogger());
        dbManager.connect();
        dbManager.runSchemaFromResource("/schema.sql");
        PortstoneRepository portstoneRepository = new PortstoneRepository(context.plugin.getLogger(), dbManager);

        // 3. Create Fake hooks
        // We use a mock ConfigService for the fake hooks' constructors
        ConfigService mockConfigService = mock(ConfigService.class);
        context.fakeEconomyHook = new FakeEconomyHook(context.plugin.getLogger(), mockConfigService);
        context.fakeTownyHook = new FakeTownyHook(context.plugin.getLogger(), context.fakeEconomyHook);

        // 4. Create a real manager with our test components
        context.portstoneManager = new PortstoneManager(portstoneRepository, context.fakeTownyHook);

        // 5. Inject the test components into the loaded plugin instance
        context.plugin.setTownyHook(context.fakeTownyHook);
        context.plugin.setEconomyHook(context.fakeEconomyHook);
        context.plugin.setPortstoneManager(context.portstoneManager);

        return context;
    }

    // --- Fake Class Definitions ---

    /**
     * A fake implementation of EconomyHook for testing purposes.
     */
    public static class FakeEconomyHook extends EconomyHook {
        private boolean chargeResult = true;
        private boolean enabled = true;

        public FakeEconomyHook(Logger logger, ConfigService configService) {
            super(logger, configService);
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public boolean chargeFee(Player player, double amount) {
            return this.chargeResult;
        }

        @Override
        public String format(double amount) {
            return "$" + String.format("%.2f", amount);
        }

        // Control methods for tests
        public void setChargeResult(boolean result) {
            this.chargeResult = result;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class FakeTownyHook extends TownyHook {
        private boolean canEditResult = true;

        public FakeTownyHook(Logger logger, EconomyHook economyHook) {
            super(logger, economyHook);
        }

        @Override
        public boolean canEdit(Player player, Portstone portstone) {
            return this.canEditResult;
        }

        // A helper method to control the fake's behavior from our tests
        public void setCanEditResult(boolean result) {
            this.canEditResult = result;
        }
    }


}