package net.astradal.astradalPorts.persistence;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.core.Portstone;
import org.bukkit.Material;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repository class responsible for CRUD operations on Portstone entities
 * in the SQLite database. Abstracts database access logic away from the rest of the plugin.
 */
public class PortstoneRepository {

    private final DatabaseManager databaseManager;
    private final AstradalPorts plugin;

    /**
     * Constructs a PortstoneRepository with the given plugin instance and database manager.
     *
     * @param plugin the main plugin instance for logging
     * @param databaseManager the database manager providing SQLite connections
     */
    public PortstoneRepository(AstradalPorts plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    /**
     * Saves or updates a Portstone in the database.
     * Uses SQLite's ON CONFLICT clause to update existing entries based on portstone ID.
     *
     * @param portstone the Portstone object to save or update
     */
    public void savePortstone(Portstone portstone) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "INSERT INTO portstones (id, type, world, x, y, z, town, nation, name, fee, icon) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET " +
                "type = ?, world = ?, x = ?, y = ?, z = ?, town = ?, nation = ?, name = ?, fee = ?, icon = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, portstone.getIdAsString());
                stmt.setString(2, portstone.getType().toString());
                stmt.setString(3, portstone.getWorld());
                stmt.setDouble(4, portstone.getX());
                stmt.setDouble(5, portstone.getY());
                stmt.setDouble(6, portstone.getZ());
                stmt.setString(7, portstone.getTown());
                stmt.setString(8, portstone.getNation());
                stmt.setString(9, portstone.getDisplayName());
                stmt.setDouble(10, portstone.getTravelFee());
                stmt.setString(11, portstone.getIcon().name());

                // For UPDATE part of the query
                stmt.setString(12, portstone.getType().toString());
                stmt.setString(13, portstone.getWorld());
                stmt.setDouble(14, portstone.getX());
                stmt.setDouble(15, portstone.getY());
                stmt.setDouble(16, portstone.getZ());
                stmt.setString(17, portstone.getTown());
                stmt.setString(18, portstone.getNation());
                stmt.setString(19, portstone.getDisplayName());
                stmt.setDouble(20, portstone.getTravelFee());
                stmt.setString(21, portstone.getIcon().name());

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Severe error attempting to save portstone to database.");
        }
    }

    /**
     * Retrieves a Portstone from the database by its ID.
     *
     * @param id the unique ID of the portstone
     * @return the Portstone object if found, or null if not found or on error
     */
    public Portstone getPortstoneById(String id) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT * FROM portstones WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Portstone(
                            UUID.fromString(rs.getString("id")),
                            PortType.fromString(rs.getString("type")),
                            rs.getString("world"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getString("town"),
                            rs.getString("nation"),
                            rs.getString("name"),
                            rs.getDouble("fee"),
                            Material.getMaterial(rs.getString("icon"))
                        );
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Severe error attempting to get portstone by ID from database: " + e.getErrorCode());
        }
        return null;
    }

    /**
     * Retrieves all portstones stored in the database.
     *
     * @return a list of all Portstones; empty list if none found or on error
     */
    public List<Portstone> getAllPortstones() {
        List<Portstone> portstones = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT * FROM portstones";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    portstones.add(new Portstone(
                        UUID.fromString(rs.getString("id")),
                        PortType.fromString(rs.getString("type")),
                        rs.getString("world"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getString("town"),
                        rs.getString("nation"),
                        rs.getString("name"),
                        rs.getDouble("fee"),
                        Material.getMaterial(rs.getString("icon"))
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Severe error attempting to get all portstones from database: " + e.getErrorCode());
        }
        return portstones;
    }

    /**
     * Deletes a Portstone from the database by its ID.
     *
     * @param id the unique ID of the portstone to delete
     */
    public void deletePortstone(String id) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "DELETE FROM portstones WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, id);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Severe error attempting to delete portstone from database: " + e.getErrorCode());
        }
    }
}
