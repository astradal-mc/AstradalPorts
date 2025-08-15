package net.astradal.astradalPorts.database.repositories;


import net.astradal.astradalPorts.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Repository class for managing hologram persistence.
 * Stores and retrieves hologram entity UUIDs associated with portstones.
 */
@SuppressWarnings("ClassCanBeRecord")
public class HologramRepository {

    private final DatabaseManager databaseManager;
    private final Logger logger;

    /**
     * Constructs a HologramRepository with the specified plugin and database manager.
     *
     * @param logger          for logging errors
     * @param databaseManager the database manager to handle SQLite connections
     */
    public HologramRepository(Logger logger, DatabaseManager databaseManager) {
        this.logger = logger;
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
            logger.severe("Failed to save hologram for portstone " + portstoneId + ": " + e.getMessage());
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
            logger.severe("Failed to retrieve hologram for portstone " + portstoneId + ": " + e.getMessage());
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
            logger.severe("Failed to delete hologram for portstone " + portstoneId + ": " + e.getMessage());
        }
    }

    /**
     * Retrieves all persisted hologram mappings from the database.
     * <p>
     * This is typically used on plugin startup to populate the hologram service's cache.
     * It will safely skip any entries with invalid UUIDs.
     *
     * @return A map where the key is the Portstone's UUID and the value is the associated hologram entity's UUID.
     */
    public Map<UUID, UUID> getAllHolograms() {
        Map<UUID, UUID> holograms = new HashMap<>();
        String sql = "SELECT portstone_id, entity_uuid FROM holograms";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    UUID portstoneId = UUID.fromString(rs.getString("portstone_id"));
                    UUID entityId = UUID.fromString(rs.getString("entity_uuid"));
                    holograms.put(portstoneId, entityId);
                } catch (IllegalArgumentException e) {
                    logger.warning("Skipping invalid UUID entry in holograms table: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to retrieve all holograms: " + e.getMessage());
        }

        return holograms;
    }
}
