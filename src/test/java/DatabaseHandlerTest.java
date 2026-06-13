import org.example.DatabaseHandler;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseHandlerTest {

    @Test
    void testGetConnection() throws SQLException {
        try (Connection connection = DatabaseHandler.getConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertTrue(connection.isValid(2));
        }
    }
}