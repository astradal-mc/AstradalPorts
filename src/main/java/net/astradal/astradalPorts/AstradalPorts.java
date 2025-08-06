package net.astradal.astradalPorts;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.astradal.astradalPorts.commands.RootCommand;
import net.astradal.astradalPorts.helpers.IdSuggestions;
import net.astradal.astradalPorts.helpers.PortstoneCleanupHelper;
import net.astradal.astradalPorts.listeners.*;
import net.astradal.astradalPorts.listeners.towny.NationDeleteListener;
import net.astradal.astradalPorts.listeners.towny.TownDeleteListener;
import net.astradal.astradalPorts.listeners.towny.TownLeaveNationListener;
import net.astradal.astradalPorts.services.CooldownService;
import net.astradal.astradalPorts.services.HologramService;
import net.astradal.astradalPorts.services.PortstoneStorage;
import net.astradal.astradalPorts.util.PortstoneKeys;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class AstradalPorts extends JavaPlugin {

    private PortstoneStorage portstoneStorage;
    private HologramService hologramService;
    private CooldownService cooldownService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize services
        this.portstoneStorage = new PortstoneStorage(this);
        this.hologramService = new HologramService(this);
        PortstoneKeys.init(this);
        PortstoneCleanupHelper cleanup = new PortstoneCleanupHelper(portstoneStorage, hologramService);

        // Load cooldowns from config
        Map<String, Integer> cooldownMap = new HashMap<>();
        ConfigurationSection cooldownSection = getConfig().getConfigurationSection("cooldowns");
        if (cooldownSection != null) {
            for (String type : cooldownSection.getKeys(false)) {
                cooldownMap.put(type.toLowerCase(), cooldownSection.getInt(type));
            }
        }
        this.cooldownService = new CooldownService(this, cooldownMap);

        // Register plugin listeners
        PluginManager pm = getServer().getPluginManager();

        // Portstone is broken
        pm.registerEvents(new PortstoneBreakListener(portstoneStorage, hologramService), this);
        // Portstone is clicked
        pm.registerEvents(new PortstoneClickListener(this, portstoneStorage, cooldownService), this);
        // Gui elements are clicked
        pm.registerEvents(new PortstoneGUIListener(this, cooldownService), this);

        // Town is deleted
        pm.registerEvents(new TownDeleteListener(cleanup), this);
        // Nation is deleted (airship ports)
        pm.registerEvents(new NationDeleteListener(cleanup), this);
        // Town leaves nation (airship ports)
        pm.registerEvents(new TownLeaveNationListener(cleanup), this);

        // Register commands
        IdSuggestions.setStorage(portstoneStorage);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var registrar = event.registrar();
            var dispatcher = registrar.getDispatcher();

            registrar.register(
                RootCommand.create(this, portstoneStorage, cooldownService, hologramService, dispatcher)
            );
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        cooldownService.save();
    }

    public HologramService getHologramService() {
        return this.hologramService;
    }

    public PortstoneStorage getPortstoneStorage() {
        return this.portstoneStorage;
    }

    public CooldownService getCooldownService() {
        return this.cooldownService;
    }
}
