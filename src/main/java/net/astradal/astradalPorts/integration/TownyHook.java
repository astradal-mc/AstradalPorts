package net.astradal.astradalPorts.integration;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public class TownyHook {

    public static Optional<String> getTown(Location loc) {
        TownBlock block = TownyAPI.getInstance().getTownBlock(loc);
        if (block != null) {
            Town town = block.getTownOrNull();
            if (town != null) {
                return Optional.of(town.getName());
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getNation(Location loc) {
        TownBlock block = TownyAPI.getInstance().getTownBlock(loc);
        if (block != null) {
            Town town = block.getTownOrNull();
            if (town != null) {
                Nation nation = town.getNationOrNull();
                if (nation != null) {
                    return Optional.of(nation.getName());
                }
            }
        }
        return Optional.empty();
    }

    public static boolean isMayor(Player player, String townName) {
        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
        if (resident == null) return false;

        Town town;
        try {
            town = TownyUniverse.getInstance().getTown(townName);
        } catch (Exception e) {
            return false;
        }

        return town != null && town.isMayor(resident);
    }

    public static boolean isClaimed(Location loc) {
        TownBlock block = TownyAPI.getInstance().getTownBlock(loc);
        return block != null && block.hasTown();
    }
}
