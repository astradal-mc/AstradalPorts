package net.astradal.astradalPorts;

import com.mojang.brigadier.CommandDispatcher;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.astradal.astradalPorts.commands.PortstoneCommand;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.database.DatabaseManager;
import net.astradal.astradalPorts.database.repositories.CooldownRepository;
import net.astradal.astradalPorts.database.repositories.HologramRepository;
import net.astradal.astradalPorts.database.repositories.PortstoneRepository;
import net.astradal.astradalPorts.services.ConfigService;
import net.astradal.astradalPorts.services.hooks.TownyHook;
import net.astradal.astradalPorts.utils.ConfigMigrationUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AstradalPorts extends JavaPlugin {

    // Services
    private ConfigService configService;

    // Hooks
    private static TownyHook townyHook;

    // Database Components
    private DatabaseManager databaseManager;
    private PortstoneRepository portstoneRepository;
    private CooldownRepository cooldownRepository;
    private HologramRepository hologramRepository;

    // Core Managers
    private PortstoneManager portstoneManager;

    @Override
    public void onEnable() {
        // --- 1. Configuration ---
        saveDefaultConfig();
        ConfigMigrationUtil.migrateConfigDefaults(this);
        this.configService = new ConfigService(this);

        // --- 2. Database Setup ---
        // Ensures the plugin data folder exists
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        String dbUrl = "jdbc:sqlite:" + new File(getDataFolder(), "database.db").getAbsolutePath();
        this.databaseManager = new DatabaseManager(dbUrl, getLogger());
        this.databaseManager.connect();
        // Run the schema to ensure all tables are created
        this.databaseManager.runSchemaFromResource("/schema.sql");

        // --- 3. Initialize Repositories ---
        // These classes handle direct database communication.
        this.portstoneRepository = new PortstoneRepository(this.getLogger(), this.databaseManager);
        this.cooldownRepository = new CooldownRepository(this.getLogger(), this.databaseManager);
        this.hologramRepository = new HologramRepository(this.getLogger(), this.databaseManager);

        // --- 4. Initialize Managers ---
        // These classes handle caching and business logic.
        this.portstoneManager = new PortstoneManager(this.portstoneRepository, townyHook);
        // Load all data from the database into the manager's cache for fast access.
        this.portstoneManager.loadAllPortstones();

        // --- 5. Initialize Other Services ---
        townyHook = new TownyHook(this.getLogger());
        // (e.g., GUIService, CooldownService, etc. would be initialized here)

        // --- 6. Register Commands ---
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final CommandDispatcher<CommandSourceStack> dispatcher = event.registrar().getDispatcher();
            event.registrar().register(
                PortstoneCommand.create(this, dispatcher)
            );
        });

        // --- 7. Register Event Listeners ---
        // (e.g., getServer().getPluginManager().registerEvents(new PlayerListener(), this);)

        getLogger().info("AstradalPorts has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        // Disconnect from the database when the plugin is disabled
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("AstradalPorts has been disabled.");
    }

    // --- Public Getters for other classes to use ---

    public ConfigService getConfigService() {
        return configService;
    }

    public PortstoneManager getPortstoneManager() {
        return portstoneManager;
    }

    public PortstoneRepository getPortstoneRepository() {
        return portstoneRepository;
    }

    public CooldownRepository getCooldownRepository() {
        return cooldownRepository;
    }

    public HologramRepository getHologramRepository() {
        return hologramRepository;
    }

    public TownyHook getTownyHook() {
        return townyHook;
    }
}