import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.DatabaseHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.sql.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseHandlerTest {
    private static final String TEST_DB_URL = "jdbc:postgresql://localhost:5433/scraper_postgres_test";
    private static final String TEST_DB_USER = "etl";
    private static final String TEST_DB_PASSWORD = "etl";

    private static Connection connection;
    private final DatabaseHandler databaseHandler = new DatabaseHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUpAll() throws SQLException {
        connection = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
        importSQL(connection, "db_schema.sql");
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("TRUNCATE TABLE epbs_registry;");
        }
    }

    private static void importSQL(Connection conn, String fileName) throws SQLException {
        InputStream is = DatabaseHandlerTest.class.getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            throw new IllegalArgumentException("SQL файл не найден в ресурсах тестов: " + fileName);
        }

        try (Scanner scanner = new Scanner(is, "UTF-8").useDelimiter(";")) {
            try (Statement statement = conn.createStatement()) {
                while (scanner.hasNext()) {
                    String rawStatement = scanner.next().trim();
                    if (!rawStatement.isEmpty()) {
                        statement.execute(rawStatement);
                    }
                }
                System.out.println("[TEST] Структура БД успешно создана из файла: " + fileName);
            }
        }
    }

    @Test
    void testGetConnection() throws SQLException {
        try (Connection connection = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD)) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertTrue(connection.isValid(2));
        }
    }

    @Test
    void testUpsertLogicAndNoDuplicates() throws SQLException {
        ArrayNode dataArray = objectMapper.createArrayNode();
        ObjectNode item = objectMapper.createObjectNode();
        ObjectNode info = objectMapper.createObjectNode();

        info.put("id", "12345");
        info.put("regNum", "Ч5269");
        info.put("fullName", "Детский сад БЕРЕЗКА");
        info.put("exclusionDate", "");

        item.set("info", info);
        dataArray.add(item);

        String fromDate = "07.06.2026";
        String toDate = "08.06.2026";

        databaseHandler.clearIntervalBeforeLoad(connection, fromDate, toDate);
        databaseHandler.savePageEntries(connection, dataArray, fromDate, toDate);
        assertEquals(1, getRowsCount(), "после первой вставки должна быть ровно 1 строка");

        databaseHandler.clearIntervalBeforeLoad(connection, fromDate, toDate);
        databaseHandler.savePageEntries(connection, dataArray, fromDate, toDate);
        assertEquals(1, getRowsCount(), "данные должны были перезаписаться, а не сдублироваться");
    }

    private int getRowsCount() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM epbs_registry")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}