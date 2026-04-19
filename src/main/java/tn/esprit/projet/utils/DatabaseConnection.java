package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection to nutrilife_db.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://127.0.0.1:3306/nutrilife_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connected to nutrilife_db");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("[DB] Reconnect failed: " + e.getMessage());
        }
        return connection;
    }
}
