package net.astradal.astradalPorts.persistence;

import net.astradal.astradalPorts.AstradalPorts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseManager {

    private final AstradalPorts plugin;

    public DatabaseManager(AstradalPorts plugin) {
        this.plugin = plugin;
    }

    // SQLite database URL
    private static final String DB_URL = "jdbc:sqlite:plugins/AstradalPorts/database.db";

    // Atomic reference to hold the database connection for thread safety
    private static final AtomicReference<Connection> connection = new AtomicReference<>();

    // Connect to the SQLite database
    public void connect() {
        try {
            if (connection.get() == null || connection.get().isClosed()) {
                // Ensure the SQLite JDBC driver is loaded
                Class.forName("org.sqlite.JDBC");
                // Create a new connection if none exists or if it's closed
                connection.set(DriverManager.getConnection(DB_URL));
                plugin.getLogger().info("Database connected successfully.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Database connection failed.");
        }
    }

    // Get the current database connection
    public Connection getConnection() {
        if (connection.get() == null) {
            connect();
        }
        return connection.get();
    }

    // Close the database connection when plugin is disabled
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
