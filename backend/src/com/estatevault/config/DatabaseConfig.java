package com.estatevault.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConfig {

    private static final String URL  = "jdbc:mysql://localhost:3306/estate_vault";
    private static final String USER = "root";
    private static final String PASS = "Ahmad@0315";

    private DatabaseConfig() {}

    public static Connection connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
        try (Connection bootstrap = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/", USER, PASS);
             Statement st = bootstrap.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS estate_vault");
        }
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        initSchema(conn);
        return conn;
    }

    private static void initSchema(Connection conn) throws SQLException {
        String[] ddl = {
            "CREATE TABLE IF NOT EXISTS persons (" +
                "id INT PRIMARY KEY, name VARCHAR(100) NOT NULL, phone VARCHAR(20), " +
                "email VARCHAR(100), type VARCHAR(10) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS properties (" +
                "id INT PRIMARY KEY, title VARCHAR(200) NOT NULL, location VARCHAR(100), " +
                "area DOUBLE, price DOUBLE, seller_id INT, status VARCHAR(20), type VARCHAR(20), " +
                "FOREIGN KEY (seller_id) REFERENCES persons(id))",
            "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT PRIMARY KEY, property_id INT, buyer_id INT, seller_id INT, " +
                "type VARCHAR(10), amount DOUBLE, commission DOUBLE, date VARCHAR(30), " +
                "FOREIGN KEY (property_id) REFERENCES properties(id), " +
                "FOREIGN KEY (buyer_id) REFERENCES persons(id), " +
                "FOREIGN KEY (seller_id) REFERENCES persons(id))"
        };
        try (Statement st = conn.createStatement()) {
            for (String sql : ddl) st.execute(sql);
        }
    }
}
