package net.astradal.astradalPorts.integration;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;

import org.bukkit.Location;

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

    public static boolean isClaimed(Location loc) {
        TownBlock block = TownyAPI.getInstance().getTownBlock(loc);
        return block != null && block.hasTown();
    }
}
