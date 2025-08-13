package net.astradal.astradalPorts.persistence;

import net.astradal.astradalPorts.AstradalPorts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Repository for managing cooldown data persistence in the SQLite database.
 * Provides CRUD operations for player cooldowns identified by player UUID and cooldown type.
 */
public class CooldownRepository {

    private final DatabaseManager databaseManager;
    private final AstradalPorts plugin;

    /**
     * Constructs a CooldownRepository with the given plugin and database manager.
     *
     * @param plugin the main AstradalPorts plugin instance for logging
     * @param databaseManager the DatabaseManager instance to handle DB connections
     */
    public CooldownRepository(AstradalPorts plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    /**
     * Retrieves all cooldown types and their last use timestamps for the specified player.
     *
     * @param playerUUID the UUID of the player whose cooldowns are requested
     * @return a map where keys are cooldown types (lowercase) and values are last use timestamps in milliseconds
     */
    public Map<String, Long> getCooldowns(UUID playerUUID) {
        Map<String, Long> cooldowns = new HashMap<>();
        String sql = "SELECT type, last_use_ms FROM cooldowns WHERE player_uuid = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cooldowns.put(rs.getString("type").toLowerCase(), rs.getLong("last_use_ms"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load cooldowns for player " + playerUUID + ": " + e.getMessage());
        }

        return cooldowns;
    }

    /**
     * Retrieves the last use timestamp of a specific cooldown type for a player.
     * Returns 0 if no record is found for the given player and cooldown type.
     *
     * @param playerUUID the UUID of the player
     * @param type the cooldown type (case-insensitive)
     * @return the last use timestamp in milliseconds, or 0 if not found
     */
    public long getLastUse(UUID playerUUID, String type) {
        String sql = "SELECT last_use_ms FROM cooldowns WHERE player_uuid = ? AND type = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, type.toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("last_use_ms");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get last use cooldown for player " + playerUUID + " type " + type + ": " + e.getMessage());
        }
        return 0L;
    }

    /**
     * Saves or updates the last use timestamp of a cooldown type for a player.
     * Uses SQLite's ON CONFLICT clause to update existing records or insert new ones.
     *
     * @param playerUUID the UUID of the player
     * @param type the cooldown type (case-insensitive)
     * @param lastUseMs the timestamp of the last cooldown usage in milliseconds
     */
    public void saveLastUse(UUID playerUUID, String type, long lastUseMs) {
        String sql = "INSERT INTO cooldowns (player_uuid, type, last_use_ms) VALUES (?, ?, ?) " +
            "ON CONFLICT(player_uuid, type) DO UPDATE SET last_use_ms = excluded.last_use_ms";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, type.toLowerCase());
            stmt.setLong(3, lastUseMs);

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save cooldown for player " + playerUUID + " type " + type + ": " + e.getMessage());
        }
    }

    /**
     * Deletes a cooldown record for a player and cooldown type from the database.
     *
     * @param playerUUID the UUID of the player
     * @param type the cooldown type (case-insensitive) to remove
     */
    public void deleteCooldown(UUID playerUUID, String type) {
        String sql = "DELETE FROM cooldowns WHERE player_uuid = ? AND type = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, type.toLowerCase());

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete cooldown for player " + playerUUID + " type " + type + ": " + e.getMessage());
        }
    }
}