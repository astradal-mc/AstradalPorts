package net.astradal.astradalPorts.core;

public enum PortType {
    AIR,
    LAND,
    SEA;

    // Optional: add utility methods if needed

    /**
     * Returns a PortType from a string, ignoring case.
     * Returns null if no match found.
     */
    public static PortType fromString(String type) {
        if (type == null) return null;
        try {
            return PortType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
