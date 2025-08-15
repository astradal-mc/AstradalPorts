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
import net.astradal.astradalPorts.listeners.*;
import net.astradal.astradalPorts.services.*;
import net.astradal.astradalPorts.services.hooks.EconomyHook;
import net.astradal.astradalPorts.services.hooks.TownyHook;
import net.astradal.astradalPorts.utils.ConfigMigrationUtil;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@SuppressWarnings("unused")
public class AstradalPorts extends JavaPlugin {

    // Services & Hooks
    private ConfigService configService;
    private GUIService guiService;
    private CooldownService cooldownService;
    private HologramService hologramService;
    private WarmupService warmupService;
    private TownyHook townyHook;
    private EconomyHook economyHook;

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
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        String dbUrl = "jdbc:sqlite:" + new File(getDataFolder(), "database.db").getAbsolutePath();
        this.databaseManager = new DatabaseManager(dbUrl, getLogger());
        this.databaseManager.connect();
        this.databaseManager.runSchemaFromResource("/schema.sql");

        // --- 3. Initialize Repositories ---
        this.portstoneRepository = new PortstoneRepository(this.getLogger(), this.databaseManager);
        this.cooldownRepository = new CooldownRepository(this.getLogger(), this.databaseManager);
        this.hologramRepository = new HologramRepository(this.getLogger(), this.databaseManager);

        // --- 4. Setup Hooks ---
        setupHooks();

        // --- 5. Initialize Managers ---
        this.portstoneManager = new PortstoneManager(this.portstoneRepository, this.townyHook);

        // --- 6. Initialize Services ---
        this.cooldownService = new CooldownService(this.cooldownRepository, this.configService);
        this.guiService = new GUIService(this, economyHook);
        this.hologramService = new HologramService(this.getLogger(), this.hologramRepository);
        this.warmupService = new WarmupService(this, this.configService, this.cooldownService, this.economyHook, this.townyHook);

        // --- 7. Load Data and Initialize Runtime Components ---
        this.portstoneManager.loadAllPortstones();
        this.hologramService.initializeHolograms(this.portstoneManager);

        // --- 8. Register Commands & Listeners ---
        registerCommands();
        registerListeners();

        getLogger().info("AstradalPorts has been enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (hologramService != null) {
            hologramService.removeAllHolograms();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("AstradalPorts has been disabled.");
    }

    /**
     * Initializes and enables integration hooks with other plugins.
     */
    private void setupHooks() {
        this.economyHook = new EconomyHook(this.getLogger(), this.configService);
        this.economyHook.initialize();

        this.townyHook = new TownyHook(this.getLogger(), this.economyHook);
        this.townyHook.initialize();
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final CommandDispatcher<CommandSourceStack> dispatcher = event.registrar().getDispatcher();
            event.registrar().register(
                PortstoneCommand.create(this, dispatcher)
            );
        });
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerConnectionListener(cooldownService), this);
        pm.registerEvents(new HologramListener(hologramService), this);
        pm.registerEvents(new PortstoneInteractionListener(this.portstoneManager, this.guiService), this);
        pm.registerEvents(new GUIListener(this), this);
        pm.registerEvents(this.warmupService, this);
        pm.registerEvents(new BlockBreakListener(this.portstoneManager, this.townyHook), this);

        if (this.townyHook.isEnabled()) {
            pm.registerEvents(new TownyListener(this.portstoneManager, this.getLogger()), this);
        }
    }

    // --- Public Getters for other classes to use ---

    public ConfigService getConfigService() {
        return configService;
    }

    public CooldownService getCooldownService() {
        return this.cooldownService;
    }

    public HologramService getHologramService() {
        return this.hologramService;
    }

    public WarmupService getWarmupService() {
        return this.warmupService;
    }

    public GUIService getGuiService() {
        return this.guiService;
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

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public TownyHook getTownyHook() {
        return townyHook;
    }
}