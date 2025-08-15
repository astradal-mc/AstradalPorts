package net.astradal.astradalPorts.utils;

import net.astradal.astradalPorts.core.Portstone;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Parses a filter string and applies it to a collection of Portstones.
 * Supports filters like 'type:land', 'owner:Astra', 'status:enabled'.
 */
public class PortstoneFilter {

    private final Map<String, String> activeFilters = new HashMap<>();

    public PortstoneFilter(String filterString) {
        if (filterString == null || filterString.isBlank()) {
            return;
        }

        // Parse the filter string, e.g., "type:land owner:Astra"
        for (String part : filterString.split(" ")) {
            String[] pair = part.split(":", 2);
            if (pair.length == 2) {
                activeFilters.put(pair[0].toLowerCase(), pair[1].toLowerCase());
            }
        }
    }

    /**
     * Applies the parsed filters to a given collection of portstones.
     * @param portstones The initial collection of portstones.
     * @return A filtered list of portstones.
     */
    public List<Portstone> apply(Collection<Portstone> portstones) {
        Stream<Portstone> stream = portstones.stream();

        if (activeFilters.containsKey("type")) {
            String typeValue = activeFilters.get("type");
            stream = stream.filter(p -> p.getType().name().equalsIgnoreCase(typeValue));
        }

        if (activeFilters.containsKey("owner")) {
            String ownerValue = activeFilters.get("owner");
            stream = stream.filter(p -> (p.getTown() != null && p.getTown().equalsIgnoreCase(ownerValue)) ||
                (p.getNation() != null && p.getNation().equalsIgnoreCase(ownerValue)));
        }

        if (activeFilters.containsKey("status")) {
            String statusValue = activeFilters.get("status");
            if (statusValue.equals("enabled")) {
                stream = stream.filter(Portstone::isEnabled);
            } else if (statusValue.equals("disabled")) {
                stream = stream.filter(p -> !p.isEnabled());
            }
        }

        return stream.toList();
    }
}