package net.astradal.astradalPorts.persistence;


import net.astradal.astradalPorts.AstradalPorts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Repository class for managing hologram persistence.
 * Stores and retrieves hologram entity UUIDs associated with portstones.
 */
public class HologramRepository {

    private final DatabaseManager databaseManager;
    private final AstradalPorts plugin;

    /**
     * Constructs a HologramRepository with the specified plugin and database manager.
     *
     * @param plugin the main plugin instance for logging
     * @param databaseManager the database manager to handle SQLite connections
     */
    public HologramRepository(AstradalPorts plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    /**
     * Saves or updates the entity UUID of a hologram linked to a portstone.
     *
     * @param portstoneId the unique ID of the portstone
     * @param entityUuid the UUID of the hologram entity
     */
    public void saveHologram(String portstoneId, UUID entityUuid) {
        String sql = "INSERT INTO holograms (portstone_id, entity_uuid) VALUES (?, ?) " +
            "ON CONFLICT(portstone_id) DO UPDATE SET entity_uuid = excluded.entity_uuid";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, portstoneId);
            stmt.setString(2, entityUuid.toString());

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save hologram for portstone " + portstoneId + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves the entity UUID of a hologram associated with the given portstone ID.
     *
     * @param portstoneId the unique ID of the portstone
     * @return the UUID of the hologram entity if found, or null if none exists or on error
     */
    public UUID getHologramEntityUuid(String portstoneId) {
        String sql = "SELECT entity_uuid FROM holograms WHERE portstone_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, portstoneId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("entity_uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to retrieve hologram for portstone " + portstoneId + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Deletes the hologram entry associated with the given portstone ID.
     *
     * @param portstoneId the unique ID of the portstone whose hologram entry is to be deleted
     */
    public void deleteHologram(String portstoneId) {
        String sql = "DELETE FROM holograms WHERE portstone_id = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, portstoneId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete hologram for portstone " + portstoneId + ": " + e.getMessage());
        }
    }
}
