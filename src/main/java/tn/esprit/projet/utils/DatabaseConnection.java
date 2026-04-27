package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides fresh JDBC connections to nutrilife_db.
 * Each call to getConnection() returns a validated, live connection.
 * If the shared connection is stale, it reconnects automatically.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private DatabaseConnection() {
        // Force MyBDConnexion initialization (schema creation)
        MyBDConnexion.getInstance();
        System.out.println("[DB] DatabaseConnection initialized");
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns a validated, live connection.
     * Delegates to MyBDConnexion which handles auto-reconnect.
     */
    public Connection getConnection() {
        return MyBDConnexion.getInstance().getCnx();
    }

    /**
     * Returns a brand-new independent JDBC connection.
     * Use this when you need isolation from the shared connection.
     * IMPORTANT: caller must close this connection after use.
     */
    public Connection getFreshConnection() throws SQLException {
        if (MyBDConnexion.getInstance().isUsingSQLite()) {
            return DriverManager.getConnection("jdbc:sqlite:nutrilife.db");
        }
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD);
    }
}
