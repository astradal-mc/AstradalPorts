package net.astradal.astradalPorts.persistence;

import net.astradal.astradalPorts.AstradalPorts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Manages the SQLite database connection for the AstradalPorts plugin.
 * Handles connection initialization, retrieval, and proper closure.
 */
public class DatabaseManager {

    private final AstradalPorts plugin;

    /**
     * SQLite database file URL.
     * The database file is located inside the plugin's data folder.
     */
    private static final String DB_URL = "jdbc:sqlite:plugins/AstradalPorts/database.db";

    /**
     * Atomic reference holding the active database connection.
     * Ensures thread safety when accessing or modifying the connection.
     */
    private static final AtomicReference<Connection> connection = new AtomicReference<>();

    /**
     * Constructs a DatabaseManager instance with the specified plugin.
     *
     * @param plugin the main AstradalPorts plugin instance for logging
     */
    public DatabaseManager(AstradalPorts plugin) {
        this.plugin = plugin;
    }

    /**
     * Establishes a connection to the SQLite database if one does not already exist
     * or if the existing connection has been closed.
     * Logs success or failure to the plugin logger.
     */
    public void connect() {
        try {
            if (connection.get() == null || connection.get().isClosed()) {
                // Load SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");
                // Create a new connection
                connection.set(DriverManager.getConnection(DB_URL));
                plugin.getLogger().info("Database connected successfully.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Database connection failed.");
        }
    }

    /**
     * Returns the current SQLite database connection.
     * Automatically attempts to connect if no active connection exists.
     *
     * @return the active Connection instance
     */
    public Connection getConnection() {
        if (connection.get() == null) {
            connect();
        }
        return connection.get();
    }

    /**
     * Closes the SQLite database connection if it is open.
     * Logs the closure or any failure during disconnection.
     */
    public void disconnect() {
        try {
            if (connection.get() != null && !connection.get().isClosed()) {
                connection.get().close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection.");
        }
    }
}