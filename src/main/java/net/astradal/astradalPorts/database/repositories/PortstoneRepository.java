package net.astradal.astradalPorts.database.repositories;

import net.astradal.astradalPorts.core.PortType;
import net.astradal.astradalPorts.core.Portstone;
import net.astradal.astradalPorts.database.DatabaseManager;
import org.bukkit.Material;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages CRUD (Create, Read, Update, Delete) operations for Portstone objects
 * in the database. This class handles all direct SQL interaction for the 'portstones' table.
 */
@SuppressWarnings("ClassCanBeRecord")
public class PortstoneRepository {

    private final DatabaseManager databaseManager;
    private final Logger logger;

    /**
     * Constructs a new PortstoneRepository.
     *
     * @param logger          The logger instance for logging database errors.
     * @param databaseManager The manager for handling database connections.
     */
    public PortstoneRepository(Logger logger, DatabaseManager databaseManager) {
        this.logger = logger;
        this.databaseManager = databaseManager;
    }

    /**
     * Saves or updates a Portstone in the database.
     * If a Portstone with the same ID already exists, it will be updated.
     * Otherwise, a new record will be inserted.
     *
     * @param portstone The Portstone object to save.
     */
    public void savePortstone(Portstone portstone) {
        String query = """
            INSERT INTO portstones (id, type, world, x, y, z, town, nation, name, fee, icon, enabled)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) -- FIXED: Added 12th question mark
            ON CONFLICT(id) DO UPDATE SET
                type = excluded.type,
                world = excluded.world,
                x = excluded.x,
                y = excluded.y,
                z = excluded.z,
                town = excluded.town,
                nation = excluded.nation,
                name = excluded.name,
                fee = excluded.fee,
                icon = excluded.icon, -- FIXED: Added comma
                enabled = excluded.enabled
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

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
            stmt.setString(11, portstone.getIcon() != null ? portstone.getIcon().name() : Material.STONE.name());
            stmt.setBoolean(12, portstone.isEnabled());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Error saving portstone: " + e);
            throw new RuntimeException("Failed to save portstone", e);
        }
    }

    /**
     * Retrieves a single Portstone from the database by its UUID.
     *
     * @param id The UUID string of the Portstone to retrieve.
     * @return The found Portstone object, or null if no Portstone with the given ID exists.
     */
    public Portstone getPortstoneById(String id) {
        String query = "SELECT * FROM portstones WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Material mat = Material.getMaterial(rs.getString("icon"));
                    if (mat == null) mat = Material.STONE;

                    // 1. Get the type string from the database.
                    String typeString = rs.getString("type");

                    // 2. Safely parse the string into an enum, providing a default if it fails.
                    PortType type = PortType.fromString(typeString).orElseGet(() -> {
                        logger.warning("Invalid portstone type '" + typeString + "' in database for ID " + id + ". Defaulting to LAND.");
                        return PortType.LAND;
                    });

                    // 3. Pass the resolved enum to the constructor.
                    return new Portstone(
                        UUID.fromString(rs.getString("id")),
                        type, // Now using the correct type
                        rs.getString("world"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getString("town"),
                        rs.getString("nation"),
                        rs.getString("name"),
                        rs.getDouble("fee"),
                        mat,
                        rs.getBoolean("enabled")
                    );
                }
            }
        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Error fetching portstone by ID: " + e);
            throw new RuntimeException("Failed to fetch portstone by ID", e);
        }

        return null;
    }

    /**
     * Retrieves all Portstones from the database.
     *
     * @return A List of all Portstone objects. The list will be empty if no portstones are found.
     */
    public List<Portstone> getAllPortstones() {
        List<Portstone> portstones = new ArrayList<>();
        String query = "SELECT * FROM portstones";

        try (Connection connection = databaseManager.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Material mat = Material.getMaterial(rs.getString("icon"));
                if (mat == null) mat = Material.STONE;

                String typeString = rs.getString("type");
                String id = rs.getString("id"); // Get ID for logging, just in case

                // Safely parse the enum, providing a default for invalid data
                PortType type = PortType.fromString(typeString).orElseGet(() -> {
                    logger.warning("Invalid portstone type '" + typeString + "' in database for ID " + id + ". Defaulting to LAND.");
                    return PortType.LAND;
                });

                portstones.add(new Portstone(
                    UUID.fromString(id),
                    type, // Use the resolved enum
                    rs.getString("world"),
                    rs.getDouble("x"),
                    rs.getDouble("y"),
                    rs.getDouble("z"),
                    rs.getString("town"),
                    rs.getString("nation"),
                    rs.getString("name"),
                    rs.getDouble("fee"),
                    mat,
                    rs.getBoolean("enabled")
                ));
            }
        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Error fetching all portstones: " + e);
            throw new RuntimeException("Failed to fetch all portstones", e);
        }
        return portstones;
    }

    /**
     * Deletes a Portstone from the database using its UUID.
     *
     * @param id The UUID string of the Portstone to delete.
     */
    public void deletePortstone(String id) {
        String query = "DELETE FROM portstones WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.severe("Error deleting portstone: " + e);
            throw new RuntimeException("Failed to delete portstone", e); // Rethrow for tests
        }
    }
}