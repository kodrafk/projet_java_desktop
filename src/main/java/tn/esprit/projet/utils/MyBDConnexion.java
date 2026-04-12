package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBDConnexion {
    private static final String USER     = "root";
    private static final String PASSWORD = "";
    private static final String URL      = "jdbc:mysql://localhost:3306/nutrilife_db?useSSL=false&serverTimezone=UTC&connectTimeout=3000";

    private static final int MAX_RETRIES   = 5;
    private static final int RETRY_DELAY_MS = 2000; // 2s between retries

    private static MyBDConnexion instance;
    private Connection cnx;

    private MyBDConnexion() {
        cnx = tryConnect();
    }

    private static Connection tryConnect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found: " + e.getMessage());
            return null;
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ DB connected (attempt " + attempt + ")");
                return c;
            } catch (SQLException e) {
                System.err.println("⏳ DB not ready (attempt " + attempt + "/" + MAX_RETRIES + "): " + e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
                }
            }
        }
        System.err.println("❌ Could not connect to DB after " + MAX_RETRIES + " attempts.");
        return null;
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) {
            instance = new MyBDConnexion();
        }
        // Reconnect if connection dropped
        try {
            if (instance.cnx == null || instance.cnx.isClosed()) {
                System.out.println("🔄 Reconnecting to DB...");
                instance.cnx = tryConnect();
            }
        } catch (SQLException ignored) {
            instance.cnx = tryConnect();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
