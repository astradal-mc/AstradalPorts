package net.astradal.astradalPorts.persistence;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.core.Portstone;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortstoneRepository {

    private final DatabaseManager databaseManager;
    private final AstradalPorts plugin;

    public PortstoneRepository(AstradalPorts plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    // Save or update a Portstone in the database
    public void savePortstone(Portstone portstone) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "INSERT INTO portstones (id, type, world, x, y, z, town, nation, name, fee, icon) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET " +
                "type = ?, world = ?, x = ?, y = ?, z = ?, town = ?, nation = ?, name = ?, fee = ?, icon = ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, portstone.getId());
                stmt.setString(2, portstone.getType().toString());
                stmt.setString(3, portstone.getWorld());
                stmt.setDouble(4, portstone.getX());
                stmt.setDouble(5, portstone.getY());
                stmt.setDouble(6, portstone.getZ());
                stmt.setString(7, portstone.getTown());
                stmt.setString(8, portstone.getNation());
                stmt.setString(9, portstone.getName());
                stmt.setDouble(10, portstone.getFee());
                stmt.setString(11, portstone.getIcon());

                // For UPDATE part of the query
                stmt.setString(12, portstone.getType().toString());
                stmt.setString(13, portstone.getWorld());
                stmt.setDouble(14, portstone.getX());
                stmt.setDouble(15, portstone.getY());
                stmt.setDouble(16, portstone.getZ());
                stmt.setString(17, portstone.getTown());
                stmt.setString(18, portstone.getNation());
                stmt.setString(19, portstone.getName());
                stmt.setDouble(20, portstone.getFee());
                stmt.setString(21, portstone.getIcon());

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Severe error attempting to save portstone to database.");
        }
    }

    // Get a Portstone by ID
    public Portstone getPortstoneById(String id) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT * FROM portstones WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Portstone(
                            rs.getString("id"),
                            rs.getString("type"),
                            rs.getString("world"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getString("town"),
                            rs.getString("nation"),
                            rs.getString("name"),
                            rs.getDouble("fee"),
                            rs.getString("icon")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Severe error attempting to get portstone by ID from database: " + e.getErrorCode());
        }
        return null;
    }

    // Get all Portstones (for listing, etc.)
    public List<Portstone> getAllPortstones() {
        List<Portstone> portstones = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT * FROM portstones";
            try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    portstones.add(new Portstone(
                        rs.getString("id"),
                        rs.getString("type"),
                        rs.getString("world"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getString("town"),
                        rs.getString("nation"),
                        rs.getString("name"),
                        rs.getDouble("fee"),
                        rs.getString("icon")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Severe error attempting to get all portstones from database: " + e.getErrorCode());
        }
        return portstones;
    }

    // Remove a Portstone by ID
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