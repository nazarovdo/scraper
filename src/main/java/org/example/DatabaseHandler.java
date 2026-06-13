package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {

    // должны быть вынесены в отдельный конфигурационный файл с секретами, но для простоты лежат здесь
    private static final String URL = "jdbc:postgresql://localhost:5432/scraper_database";
    private static final String USER = "etl";
    private static final String PASSWORD = "etl";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}