package net.astradal.astradalPorts;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.astradal.astradalPorts.commands.PortstoneCommand;
import net.astradal.astradalPorts.core.PortstoneManager;
import net.astradal.astradalPorts.database.DatabaseManager;
import net.astradal.astradalPorts.database.repositories.CooldownRepository;
import net.astradal.astradalPorts.database.repositories.HologramRepository;
import net.astradal.astradalPorts.database.repositories.PortstoneRepository;
import net.astradal.astradalPorts.listeners.*;
import net.astradal.astradalPorts.services.*;
import net.astradal.astradalPorts.services.hooks.BlueMapHook;
import net.astradal.astradalPorts.services.hooks.EconomyHook;
import net.astradal.astradalPorts.services.hooks.TownyHook;
import net.astradal.astradalPorts.utils.ConfigMigrationUtil;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@SuppressWarnings("unused")
public class AstradalPorts extends JavaPlugin {

    // Services
    private ConfigService configService;
    private GUIService guiService;
    private CooldownService cooldownService;
    private HologramService hologramService;
    private WarmupService warmupService;
    private MessageService messageService;

    // Hooks
    private TownyHook townyHook;
    private EconomyHook economyHook;
    private BlueMapHook blueMapHook;

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
        // This handles adding new keys
        ConfigMigrationUtil.migrateConfigDefaults(this);
        // This handles updating the version key
        ConfigMigrationUtil.updateVersionInConfig(this);

        this.configService = new ConfigService(this);

        // --- 2. Database Setup ---
        String dbUrl = "jdbc:sqlite:" + new File(getDataFolder(), "database.db").getAbsolutePath();
        this.databaseManager = new DatabaseManager(dbUrl, getLogger());
        this.databaseManager.connect();
        this.databaseManager.runSchemaFromResource("/schema.sql");

        // --- 3. Initialize Repositories ---
        this.portstoneRepository = new PortstoneRepository(this.getLogger(), this.databaseManager);
        this.cooldownRepository = new CooldownRepository(this.getLogger(), this.databaseManager);
        this.hologramRepository = new HologramRepository(this.getLogger(), this.databaseManager);


        // --- 4. Initialize Manager ---
        this.portstoneManager = new PortstoneManager(this.portstoneRepository, null);

        // --- 5. Setup Hooks ---
        setupHooks();

        // --- 6. Initialize Services ---
        this.cooldownService = new CooldownService(this.cooldownRepository, this.configService);
        this.guiService = new GUIService(this, economyHook);
        this.hologramService = new HologramService(this.getLogger(), this.hologramRepository);
        this.warmupService = new WarmupService(this, this.configService, this.cooldownService, this.economyHook, this.townyHook);
        this.messageService = new MessageService(this.configService);

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

        // Now that the real TownyHook exists, inject it into the manager.
        this.portstoneManager.setTownyHook(this.townyHook);

        if (getServer().getPluginManager().getPlugin("BlueMap") != null) {
            this.blueMapHook = new BlueMapHook(this.getLogger(), this.portstoneManager, this.configService);
            this.blueMapHook.initialize();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final var registrar = event.registrar();
            final var dispatcher = registrar.getDispatcher();

            // 1. Get the builder for your main command.
            LiteralArgumentBuilder<CommandSourceStack> portstoneBuilder = PortstoneCommand.create(this, dispatcher);

            // 2. Build the main command node. We need this for the alias to redirect to.
            LiteralCommandNode<CommandSourceStack> portstoneNode = portstoneBuilder.build();

            // 3. Register the main command node. The registrar takes the final node.
            registrar.register(portstoneNode, "Manage Portstones");

            // 4. Create the alias node that redirects to the main node, and register it.
            LiteralCommandNode<CommandSourceStack> aliasNode = Commands.literal("ps")
                .redirect(portstoneNode)
                .build();
            registrar.register(aliasNode, "Alias for /portstone");
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

        if (this.blueMapHook != null) pm.registerEvents(this.blueMapHook, this);
        if (this.townyHook.isEnabled()) pm.registerEvents(new TownyListener(this.portstoneManager, this), this);

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

    public MessageService getMessageService() { return this.messageService; }

    // These methods are for testing purposes
    public void setTownyHook(TownyHook townyHook) { this.townyHook = townyHook; }

    public void setPortstoneManager(PortstoneManager portstoneManager) { this.portstoneManager = portstoneManager; }

    public void setEconomyHook(EconomyHook economyHook) { this.economyHook = economyHook; }
}