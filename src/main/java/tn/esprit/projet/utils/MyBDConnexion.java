package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBDConnexion {

    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://localhost:3306/nutrilife_db?serverTimezone=UTC&sslMode=DISABLED&createDatabaseIfNotExist=true";
    private static MyBDConnexion instance;
    private Connection cnx;

    private MyBDConnexion() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion DB établie");
            initTable();
            initComplaintsTable();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
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
        try (java.sql.Statement st = cnx.createStatement()) {
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
            try (java.sql.PreparedStatement ps = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME=?")) {
                ps.setString(1, col[0]);
                java.sql.ResultSet rs = ps.executeQuery();
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
        try (java.sql.PreparedStatement ps = cnx.prepareStatement(
                "SELECT DATA_TYPE FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME='roles'")) {
            java.sql.ResultSet rs = ps.executeQuery();
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

    private void initComplaintsTable() {
        String create = "CREATE TABLE IF NOT EXISTS `complaint` (" +
                "`id`               INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id`          INT NOT NULL," +
                "`title`            VARCHAR(150) NOT NULL," +
                "`description`      TEXT NOT NULL," +
                "`phone_number`     VARCHAR(20)," +
                "`rate`             INT DEFAULT 0," +
                "`date_of_complaint` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`status`           VARCHAR(50) NOT NULL DEFAULT 'PENDING'," +
                "`admin_response`   TEXT," +
                "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (java.sql.Statement st = cnx.createStatement()) {
            st.executeUpdate(create);
            System.out.println("[initComplaintsTable] Complaint Table ready.");
        } catch (SQLException e) {
            System.err.println("[initComplaintsTable] error : " + e.getMessage());
        }
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) {
            instance = new MyBDConnexion();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}