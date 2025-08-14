package net.astradal.astradalPorts;
import net.astradal.astradalPorts.database.DatabaseManager;
import net.astradal.astradalPorts.services.ConfigService;
import net.astradal.astradalPorts.utils.ConfigMigrationUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;


public class AstradalPorts extends JavaPlugin {

    // Instance of DatabaseManager
    private DatabaseManager databaseManager;
    private ConfigService configService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigMigrationUtil.migrateConfigDefaults(this);

        this.configService = new ConfigService(this);

        // Build the database URL pointing to the plugin's data folder
        // Ensures the folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize the DatabaseManager
        String dbUrl = "jdbc:sqlite:" + new File(getDataFolder(), "database.db").getAbsolutePath();
        databaseManager = new DatabaseManager(dbUrl, getLogger());

        // Connect to the database
        databaseManager.connect();

        // Connect to the database
        databaseManager.connect();
    }

    @Override
    public void onDisable() {
        // Disconnect from the database when the plugin is disabled
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }

    public ConfigService getConfigService() {
        return this.configService;
    }
}
