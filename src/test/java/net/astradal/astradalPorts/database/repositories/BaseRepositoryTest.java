package net.astradal.astradalPorts.database.repositories;

import net.astradal.astradalPorts.AstradalPorts;
import net.astradal.astradalPorts.database.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import static org.mockito.Mockito.*;

/**
 * An abstract base class for all repository tests.
 * It handles the common setup and teardown logic for creating an isolated,
 * in-memory SQLite database for each test method.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseRepositoryTest {

    @Mock
    protected DatabaseManager mockDbManager;
    @Mock
    protected AstradalPorts mockPlugin;

    protected Connection realConnection;

    @BeforeEach
    void setup() throws Exception {
        // Create a real connection to a new, private in-memory database.
        realConnection = DriverManager.getConnection("jdbc:sqlite::memory:");

        // Create a "spy" that wraps the real connection and tell it to ignore close() calls.
        Connection spyConnection = spy(realConnection);
        doNothing().when(spyConnection).close();

        // Configure mocks that subclasses might need.
        when(mockDbManager.getConnection()).thenReturn(spyConnection);

        // Load the database schema. This is common to all repository tests.
        try (Statement stmt = realConnection.createStatement()) {
            String schemaSql = new String(Objects.requireNonNull(getClass()
                .getResourceAsStream("/schema.sql")).readAllBytes());

            for (String statement : schemaSql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    stmt.execute(statement);
                }
            }
        }
    }

    @AfterEach
    void teardown() throws SQLException {
        if (realConnection != null && !realConnection.isClosed()) {
            realConnection.close();
        }
    }
}