package net.astradal.astradalPorts.services;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import net.astradal.astradalPorts.AstradalPorts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ConfigService using the MockBukkit framework.
 * MockBukkit provides a simulated server environment, allowing for realistic
 * testing against the Bukkit API.
 */
public class ConfigServiceTest {

    private AstradalPorts plugin;
    private ConfigService configService;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        // 1. Mock the Bukkit server
        ServerMock server = MockBukkit.mock();

        // 2. Load your plugin. MockBukkit will find your paper-plugin.yml
        //    and automatically load the config.yml from src/test/resources.
        plugin = MockBukkit.load(AstradalPorts.class);

        // 3. Get the service instance from your plugin.
        //    (This assumes your AstradalPorts class has a getter for it)
        configService = plugin.getConfigService();
    }

    @AfterEach
    void tearDown() {
        // 4. Unload the server after each test
        MockBukkit.unmock();
    }

    @Test
    void getCooldown_shouldReturnCorrectValueFromConfigFile() {
        assertEquals(1200, configService.getCooldown("land"));
    }

    @Test
    void getRange_shouldReturnCorrectValueFromConfigFile() {
        assertEquals(-1, configService.getRange("air"));
        assertEquals(1000, configService.getRange("land"));
    }

    @Test
    void getGuiTitleColor_shouldReturnCorrectValueFromConfigFile() {
        assertEquals("GRAY_STAINED_GLASS_PANE", configService.getGuiFillItem().toString());
    }

    @Test
    void isEconomyRequireBalance_shouldReturnCorrectValueFromConfigFile() {
        assertTrue(configService.isEconomyRequireBalance());
    }

    @Test
    void getters_shouldReturnDefaultValuesWhenConfigIsEmpty() {
        // Arrange: To test defaults, we manually give the plugin a blank config.
        // We can do this by directly manipulating the plugin instance provided by MockBukkit.
        plugin.getConfig().set("portstones", null);
        plugin.getConfig().set("economy", null);
        plugin.getConfig().set("gui", null);

        // Act: Reload the service to read the now-empty config
        configService.reload();

        // Assert: Verify the service falls back to its hardcoded defaults
        assertEquals(0, configService.getCooldown("any_type"));
        assertEquals(-1, configService.getRange("any_type"));
        assertTrue(configService.isEconomyEnabled());
        assertTrue(configService.isEconomyRequireBalance());
        assertEquals("GRAY_STAINED_GLASS_PANE", configService.getGuiFillItem().toString());
    }
}