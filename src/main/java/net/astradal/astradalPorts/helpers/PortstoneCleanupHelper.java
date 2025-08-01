package net.astradal.astradalPorts.helpers;

import net.astradal.astradalPorts.model.Portstone;
import net.astradal.astradalPorts.services.HologramService;
import net.astradal.astradalPorts.services.PortstoneStorage;

import java.util.List;
import java.util.function.Predicate;

public class PortstoneCleanupHelper {

    private final PortstoneStorage storage;
    private final HologramService holograms;

    public PortstoneCleanupHelper(PortstoneStorage storage, HologramService holograms) {
        this.storage = storage;
        this.holograms = holograms;
    }

    public void removeIf(Predicate<Portstone> condition) {
        List<Portstone> toRemove = storage.getAll().stream()
            .filter(condition)
            .toList();

        for (Portstone portstone : toRemove) {
            storage.removePortstone(portstone.getId());
            holograms.removeHologram(portstone.getId());
        }
    }

    public void removeByNation(String nation) {
        removeIf(p -> "air".equalsIgnoreCase(p.getType())
            && nation.equalsIgnoreCase(p.getNation()));
    }

    public void removeByTown(String town) {
        removeIf(p -> town.equalsIgnoreCase(p.getTown()) &&
            (p.getType().equalsIgnoreCase("land") || p.getType().equalsIgnoreCase("sea") || p.getType().equalsIgnoreCase("air")));
    }
}