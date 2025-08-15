package net.astradal.astradalPorts.utils;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import net.astradal.astradalPorts.AstradalPorts;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigMigrationUtil
 */
public class ConfigMigrationUtilTest {

    private ServerMock server;
    private AstradalPorts plugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AstradalPorts.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testMigrateConfigDefaults_AddsMissingDefaults() {
        FileConfiguration config = plugin.getConfig();

        // Remove a key to simulate missing config
        config.set("cooldowns.air", null);
        config.set("economy.enabled", null);
        plugin.saveConfig();

        ConfigMigrationUtil.migrateConfigDefaults(plugin);

        assertEquals(30, config.getInt("cooldowns.air"));
        assertTrue(config.getBoolean("economy.enabled"));
    }

    @Test
    public void testMigrateConfigDefaults_DoesNotOverwriteExisting() {
        FileConfiguration config = plugin.getConfig();

        config.set("cooldowns.air", 99);
        config.set("economy.enabled", false);
        plugin.saveConfig();

        ConfigMigrationUtil.migrateConfigDefaults(plugin);

        // Values should remain unchanged
        assertEquals(99, config.getInt("cooldowns.air"));
        assertFalse(config.getBoolean("economy.enabled"));
    }
}
