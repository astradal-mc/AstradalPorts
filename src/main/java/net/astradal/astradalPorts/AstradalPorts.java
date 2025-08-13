package net.astradal.astradalPorts;
import net.astradal.astradalPorts.persistence.DatabaseManager;
import net.astradal.astradalPorts.services.ConfigService;
import net.astradal.astradalPorts.utils.ConfigMigrationUtil;
import org.bukkit.plugin.java.JavaPlugin;


public final class AstradalPorts extends JavaPlugin {

    // Instance of DatabaseManager
    private DatabaseManager databaseManager;
    private ConfigService configService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigMigrationUtil.migrateConfigDefaults(this);

        this.configService = new ConfigService(this);

        // Initialize the DatabaseManager
        this.databaseManager = new DatabaseManager(this);

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
}
