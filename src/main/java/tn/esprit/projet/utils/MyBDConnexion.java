package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyBDConnexion {

    private static MyBDConnexion instance;
    private Connection cnx;

    private MyBDConnexion() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.USER,
                    DatabaseConfig.PASSWORD);
            System.out.println("Connexion OK");
            initTable();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    /** Ensure the user table exists with the correct schema. */
    private void initTable() {
        // Always ensure table exists first
        String create = "CREATE TABLE IF NOT EXISTS `user` (" +
                "`id`               INT AUTO_INCREMENT PRIMARY KEY," +
                "`email`            VARCHAR(180)  NOT NULL UNIQUE," +
                "`password`         VARCHAR(255)  NOT NULL," +
                "`roles`            VARCHAR(50)   NOT NULL DEFAULT 'ROLE_USER'," +
                "`is_active`        TINYINT(1)    NOT NULL DEFAULT 1," +
                "`created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`first_name`       VARCHAR(100)  NOT NULL DEFAULT ''," +
                "`last_name`        VARCHAR(100)  NOT NULL DEFAULT ''," +
                "`birthday`         DATE          NOT NULL DEFAULT '2000-01-01'," +
                "`weight`           FLOAT         DEFAULT NULL," +
                "`height`           FLOAT         DEFAULT NULL," +
                "`phone_number`     VARCHAR(20)   DEFAULT NULL," +
                "`phone_verified`   TINYINT(1)    NOT NULL DEFAULT 0," +
                "`photo_filename`   VARCHAR(255)  DEFAULT NULL," +
                "`welcome_message`  TEXT          DEFAULT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(create);
        } catch (SQLException e) {
            System.err.println("[initTable] create: " + e.getMessage());
        }

        // Add any missing columns (handles old schema upgrades)
        String[][] columns = {
            {"first_name",     "VARCHAR(100) NOT NULL DEFAULT ''"},
            {"last_name",      "VARCHAR(100) NOT NULL DEFAULT ''"},
            {"birthday",       "DATE NOT NULL DEFAULT '2000-01-01'"},
            {"weight",         "FLOAT DEFAULT NULL"},
            {"height",         "FLOAT DEFAULT NULL"},
            {"phone_number",   "VARCHAR(20) DEFAULT NULL"},
            {"phone_verified", "TINYINT(1) NOT NULL DEFAULT 0"},
            {"photo_filename", "VARCHAR(255) DEFAULT NULL"},
            {"welcome_message","TEXT DEFAULT NULL"},
        };
        for (String[] col : columns) {
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME=?")) {
                ps.setString(1, col[0]);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    cnx.createStatement().executeUpdate(
                            "ALTER TABLE `user` ADD COLUMN `" + col[0] + "` " + col[1]);
                    System.out.println("[initTable] Added column: " + col[0]);
                }
            } catch (SQLException e) {
                System.err.println("[initTable] col " + col[0] + ": " + e.getMessage());
            }
        }

        // Fix roles column if it's still JSON type or has CHECK constraint
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT DATA_TYPE FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME='roles'")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Always force correct type — removes any CHECK constraint
                cnx.createStatement().executeUpdate(
                        "ALTER TABLE `user` MODIFY COLUMN `roles` VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER'");
                // Fix any remaining JSON-format values in existing rows
                cnx.createStatement().executeUpdate(
                        "UPDATE `user` SET `roles` = 'ROLE_ADMIN' WHERE `roles` LIKE '%ROLE_ADMIN%' AND `roles` != 'ROLE_ADMIN'");
                cnx.createStatement().executeUpdate(
                        "UPDATE `user` SET `roles` = 'ROLE_USER' WHERE `roles` NOT IN ('ROLE_USER','ROLE_ADMIN')");
            }
        } catch (SQLException e) {
            System.err.println("[initTable] roles fix: " + e.getMessage());
        }

        System.out.println("[initTable] Table ready.");
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) instance = new MyBDConnexion();
        return instance;
    }

    public Connection getCnx() { return cnx; }
}
