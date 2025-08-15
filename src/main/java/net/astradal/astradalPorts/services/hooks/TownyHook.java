package net.astradal.astradalPorts.services.hooks;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import net.astradal.astradalPorts.commands.PortstonePermissions;
import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.core.Portstone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * A hook for interacting with the Towny API.
 * This class should be instantiated once and safely handles cases where Towny is not present.
 */
public class TownyHook {

    private final Logger logger;
    private boolean enabled = false;
    private TownyAPI townyAPI;
    private final EconomyHook economy;

    public TownyHook(Logger logger, EconomyHook economy) {
        this.logger = logger;
        this.economy = economy;
    }

    /**
     * Initializes the hook by checking for the Towny plugin.
     * This should be called from the main plugin's onEnable method.
     */
    public void initialize() {
        Plugin townyPlugin = Bukkit.getPluginManager().getPlugin("Towny");
        if (townyPlugin != null && townyPlugin.isEnabled()) {
            this.townyAPI = TownyAPI.getInstance();
            this.enabled = true;
            logger.info("Successfully hooked into Towny.");
        } else {
            logger.warning("Towny not found. Portstone ownership features will be disabled.");
        }
    }

    /**
     * Checks if the Towny hook is active.
     * @return true if Towny was found and the hook is enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Gets the name of the town at a specific location.
     * @param location The location to check.
     * @return An Optional containing the town name, or empty if no town exists.
     */
    public Optional<String> getTownNameAt(Location location) {
        if (!enabled) return Optional.empty();
        return Optional.ofNullable(townyAPI.getTownBlock(location))
            .map(TownBlock::getTownOrNull)
            .map(Town::getName);
    }

    /**
     * Gets the name of the nation at a specific location.
     * @param location The location to check.
     * @return An Optional containing the nation name, or empty if no nation exists.
     */
    public Optional<String> getNationNameAt(Location location) {
        if (!enabled) return Optional.empty();
        return Optional.ofNullable(townyAPI.getTownBlock(location))
            .map(TownBlock::getTownOrNull)
            .flatMap(town -> Optional.ofNullable(town.getNationOrNull()))
            .map(Nation::getName);
    }

    /**
     * A high-level check to see if a player can edit a specific portstone.
     * This is an example of a business logic method that belongs in a hook.
     *
     * @param player The player attempting to edit.
     * @param portstone The portstone being edited.
     * @return true if the player has permission to edit.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canEdit(Player player, Portstone portstone) {
        if (!enabled) return false;

        // Always allow admins to edit
        if (PortstonePermissions.has(player, "admin")) {
            return true;
        }

        // Check if the portstone is in a town
        Optional<String> townNameOpt = getTownNameAt(portstone.getLocation());
        if (townNameOpt.isEmpty()) {
            return false; // Portstone is not in a town, so no non-admin can edit it
        }

        // Check if the player is a resident of any town
        Resident resident = townyAPI.getResident(player);
        if (resident == null) {
            return false;
        }

        // Logic for AIR ports (Nation-level)
        if (portstone.getType() == PortType.AIR) {
            String portstoneNationName = portstone.getNation();
            if (portstoneNationName == null || !resident.hasNation()) {
                return false;
            }

            // Get the resident's nation and perform an explicit null check
            Nation residentNation = resident.getNationOrNull();
            if (residentNation == null) {
                return false;
            }

            return residentNation.getName().equals(portstoneNationName) &&
                (resident.isKing() || resident.hasNationRank("assistant"));
        }

        // Logic for LAND and SEA ports (Town-level)
        else {
            String portstoneTownName = portstone.getTown();
            // We can combine the permission and null check here
            if (portstoneTownName == null || !resident.isMayor()) {
                return false;
            }

            // Get the resident's town and perform an explicit null check
            Town residentTown = resident.getTownOrNull();
            if (residentTown == null) {
                return false;
            }

            return residentTown.getName().equals(portstoneTownName);
        }
    }

    public void depositToTownBank(String townName, double amount) {
        if (!enabled || amount <= 0) return;

        Town town = townyAPI.getTown(townName);
        if (town != null) {
            town.getAccount();
            OfflinePlayer townAccount = Bukkit.getOfflinePlayer(town.getAccount().getUUID());
            // This assumes you have an EconomyHook to do the actual deposit
            economy.deposit(townAccount, amount);
            logger.info("Deposited " + amount + " to " + townName + "'s town bank.");
        }
    }
}