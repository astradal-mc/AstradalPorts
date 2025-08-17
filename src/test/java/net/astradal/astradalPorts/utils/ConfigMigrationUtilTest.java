package net.astradal.astradalPorts.utils;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import net.astradal.astradalPorts.AstradalPorts;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ConfigMigrationUtilTest {

    private ServerMock server;
    private AstradalPorts plugin;
    private File configFile;
    private YamlConfiguration defaultConfig;

    @BeforeEach
    public void setUp() throws Exception {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(AstradalPorts.class);
        configFile = new File(plugin.getDataFolder(), "config.yml");

        // Load the default config from resources to use as our "source of truth"
        try (InputStream stream = plugin.getResource("config.yml")) {
            assert stream != null;
            defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
        }
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void migrateConfigDefaults_addsMissingKeys_andPreservesExisting() throws IOException {
        // Arrange: Create a fake "old" config file
        YamlConfiguration oldConfig = new YamlConfiguration();
        oldConfig.set("portstones.land.cooldown", 9999); // A custom, user-set value
        oldConfig.set("messages.teleport-success", "<gold>Woosh!</gold>"); // Another custom value
        oldConfig.save(configFile);

        // Act: Run the migration on the "old" config
        ConfigMigrationUtil.migrateConfigDefaults(plugin);

        // Assert: Load the final config from the mock file system
        FileConfiguration finalConfig = YamlConfiguration.loadConfiguration(configFile);

        // 1. Check that the user's custom values were preserved
        assertEquals(9999, finalConfig.getInt("portstones.land.cooldown"));
        assertEquals("<gold>Woosh!</gold>", finalConfig.getString("messages.teleport-success"));

        // 2. Check that a new, missing key was added from the defaults
        String expectedDefaultMessage = defaultConfig.getString("messages.error-cant-afford");
        assertEquals(expectedDefaultMessage, finalConfig.getString("messages.error-cant-afford"));

        // 3. Check that a new, missing section was added
        assertEquals("BLOCK_BEACON_ACTIVATE", finalConfig.getString("effects.sounds.warmup-start"));
    }

    @Test
    public void updateVersionInConfig_updatesMismatchedVersion() throws IOException {
        // Arrange
        String jarVersion = plugin.getPluginMeta().getVersion();
        String oldVersion = "old-version";
        assertNotEquals(jarVersion, oldVersion);

        // Create a config file with an old version number
        YamlConfiguration config = new YamlConfiguration();
        config.set("plugin-version", oldVersion);
        config.save(configFile);

        // Act
        ConfigMigrationUtil.updateVersionInConfig(plugin);

        // Assert
        FileConfiguration finalConfig = YamlConfiguration.loadConfiguration(configFile);
        assertEquals(jarVersion, finalConfig.getString("plugin-version"));
    }
}