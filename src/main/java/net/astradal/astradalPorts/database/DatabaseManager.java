package net.astradal.astradalPorts.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class DatabaseManager {

    private final Logger logger;
    private final String dbUrl;

    /**
     * Holds the active database connection.
     */
    private final AtomicReference<Connection> connection = new AtomicReference<>();

    /**
     * Construct a DatabaseManager with a custom DB URL.
     *
     * @param dbUrl  JDBC URL to the SQLite database
     * @param logger Logger instance for logging
     */
    public DatabaseManager(String dbUrl, Logger logger) {
        this.dbUrl = dbUrl;
        this.logger = logger;
    }

    /**
     * Establish a connection if none exists.
     */
    public void connect() {
        try {
            if (connection.get() == null || connection.get().isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection.set(DriverManager.getConnection(dbUrl));
                logger.info("Database connected successfully.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.severe("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Get the current connection, auto-connect if needed.
     */
    public Connection getConnection() {
        try {
            if (connection.get() == null || connection.get().isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            logger.severe("Failed to verify or re-establish database connection: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return connection.get();
    }

    /**
     * Close the connection if open.
     */
    public void disconnect() {
        try {
            Connection conn = connection.get();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                logger.info("Database connection closed.");
            }
        } catch (SQLException e) {
            logger.severe("Failed to close database connection: " + e.getMessage());
        } finally {
            connection.set(null); // <--- clear the reference so next connect creates a new one
        }
    }
}