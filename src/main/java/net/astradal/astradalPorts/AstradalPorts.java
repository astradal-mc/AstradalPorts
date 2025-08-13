package net.astradal.astradalPorts;
import net.astradal.astradalPorts.persistence.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;


public final class AstradalPorts extends JavaPlugin {

    // Instance of DatabaseManager
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

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
