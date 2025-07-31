package net.astradal.astradalPorts;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.astradal.astradalPorts.commands.RootCommand;
import net.astradal.astradalPorts.commands.helpers.IdSuggestions;
import net.astradal.astradalPorts.listeners.PortstoneClickListener;
import net.astradal.astradalPorts.listeners.PortstoneGUIListener;
import net.astradal.astradalPorts.services.CooldownService;
import net.astradal.astradalPorts.services.HologramService;
import net.astradal.astradalPorts.services.PortstoneStorage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public final class AstradalPorts extends JavaPlugin {

    private HologramService hologramService;

    @Override
    public void onEnable() {
        // Plugin startup logic

        saveDefaultConfig();

        // Initialize services
        PortstoneStorage portstoneStorage = new PortstoneStorage(this);
        this.hologramService = new HologramService();

        // Load config cooldowns
        Map<String, Integer> cooldownMap = new HashMap<>();
        ConfigurationSection cooldownSection = getConfig().getConfigurationSection("cooldowns");
        if (cooldownSection != null) {
            for (String type : cooldownSection.getKeys(false)) {
                cooldownMap.put(type.toLowerCase(), cooldownSection.getInt(type));
            }
        }
        CooldownService cooldownService = new CooldownService(this, cooldownMap);

        // Register listeners
        getServer().getPluginManager().registerEvents(new PortstoneGUIListener(portstoneStorage, cooldownService), this);



        // Register commands
        IdSuggestions.setStorage(portstoneStorage);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var registrar = event.registrar();
            var dispatcher = registrar.getDispatcher();

            registrar.register(
                RootCommand.create(this, portstoneStorage, dispatcher)
            );
        });

        // Register Listeners
        getServer().getPluginManager().registerEvents(
            new PortstoneClickListener(portstoneStorage, cooldownService), this
        );
        getServer().getPluginManager().registerEvents(
            new PortstoneGUIListener(portstoneStorage, cooldownService), this
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public HologramService getHologramService() {
        return hologramService;
    }
}
