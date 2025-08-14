package net.astradal.astradalPorts.database;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() {
        // Use in-memory SQLite database for testing
        dbManager = new DatabaseManager("jdbc:sqlite::memory:", Logger.getLogger("Test"));

    }

    @AfterEach
    void tearDown() {
        dbManager.disconnect();
    }

    @Test
    void testConnectCreatesConnection() {
        Connection conn = dbManager.getConnection();
        assertNotNull(conn, "Connection should not be null after getConnection()");
        assertDoesNotThrow(() -> assertFalse(conn.isClosed(), "Connection should be open"));
    }

    @Test
    void testDisconnectClosesConnection() throws SQLException {
        Connection conn = dbManager.getConnection();
        dbManager.disconnect();
        assertTrue(conn.isClosed(), "Connection should be closed after disconnect()");
    }

    @Test
    void testMultipleConnectsReturnSameConnection() {
        Connection first = dbManager.getConnection();
        Connection second = dbManager.getConnection();
        assertSame(first, second, "Multiple calls to getConnection() should return the same Connection instance");
    }

    @Test
    void testExecuteSimpleQuery() throws SQLException {
        Connection conn = dbManager.getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE test_table (id INTEGER PRIMARY KEY, name TEXT)");
            stmt.execute("INSERT INTO test_table (name) VALUES ('Alice')");

            ResultSet rs = stmt.executeQuery("SELECT name FROM test_table WHERE id = 1");
            assertTrue(rs.next(), "ResultSet should have a row");
            assertEquals("Alice", rs.getString("name"), "Inserted value should match");
        }
    }

    @Test
    void testReconnectAfterDisconnect() throws SQLException {
        Connection conn1 = dbManager.getConnection();
        dbManager.disconnect();
        Connection conn2 = dbManager.getConnection();

        assertNotNull(conn2, "Connection should not be null after reconnect");
        assertFalse(conn2.isClosed(), "Reconnected connection should be open");
        assertNotSame(conn1, conn2, "New connection should be a different instance after disconnect");
    }
}