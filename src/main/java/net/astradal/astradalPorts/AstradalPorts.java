package net.astradal.astradalPorts;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.astradal.astradalPorts.commands.RootCommand;
import net.astradal.astradalPorts.commands.helpers.IdSuggestions;
import net.astradal.astradalPorts.services.HologramService;
import net.astradal.astradalPorts.services.PortstoneStorage;
import org.bukkit.plugin.java.JavaPlugin;

public final class AstradalPorts extends JavaPlugin {

    private HologramService hologramService;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // Initialize services
        PortstoneStorage portstoneStorage = new PortstoneStorage(this);
        this.hologramService = new HologramService();


        // Register commands
        IdSuggestions.setStorage(portstoneStorage);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var registrar = event.registrar();
            var dispatcher = registrar.getDispatcher();

            registrar.register(
                RootCommand.create(this, portstoneStorage, dispatcher)
            );
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public HologramService getHologramService() {
        return hologramService;
    }
}
