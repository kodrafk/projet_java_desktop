package tn.esprit.projet.utils;

import java.sql.*;

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
            initSchema();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }

    private void initSchema() {
        // Create user table
        exec("CREATE TABLE IF NOT EXISTS `user` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`email` VARCHAR(180) NOT NULL UNIQUE," +
             "`password` VARCHAR(255) NOT NULL," +
             "`roles` VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER'," +
             "`is_active` TINYINT(1) NOT NULL DEFAULT 1," +
             "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "`reset_token` VARCHAR(255) DEFAULT NULL," +
             "`reset_token_expires_at` DATETIME DEFAULT NULL," +
             "`verification_code` VARCHAR(6) DEFAULT NULL," +
             "`verification_code_expires_at` DATETIME DEFAULT NULL," +
             "`face_descriptor` TEXT DEFAULT NULL," +
             "`face_id_enrolled_at` DATETIME DEFAULT NULL," +
             "`welcome_message` TEXT DEFAULT NULL," +
             "`google_id` VARCHAR(255) DEFAULT NULL," +
             "`photo_filename` VARCHAR(255) DEFAULT NULL," +
             "`first_name` VARCHAR(100) NOT NULL DEFAULT ''," +
             "`last_name` VARCHAR(100) NOT NULL DEFAULT ''," +
             "`birthday` DATE NOT NULL DEFAULT '2000-01-01'," +
             "`weight` DOUBLE NOT NULL DEFAULT 70," +
             "`height` DOUBLE NOT NULL DEFAULT 170" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // Face embeddings secure storage
        exec("CREATE TABLE IF NOT EXISTS `face_embeddings` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL UNIQUE," +
             "`embedding_encrypted` TEXT NOT NULL," +
             "`encryption_iv` VARCHAR(255) NOT NULL," +
             "`encryption_tag` VARCHAR(255) NOT NULL," +
             "`is_active` TINYINT(1) NOT NULL DEFAULT 1," +
             "`consent_given_at` DATETIME DEFAULT NULL," +
             "`consent_ip` VARCHAR(45) DEFAULT NULL," +
             "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
             "`last_used_at` DATETIME DEFAULT NULL," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        exec("CREATE TABLE IF NOT EXISTS `face_verification_attempts` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT DEFAULT NULL," +
             "`email` VARCHAR(255) DEFAULT NULL," +
             "`ip_address` VARCHAR(45) NOT NULL DEFAULT '127.0.0.1'," +
             "`success` TINYINT(1) NOT NULL DEFAULT 0," +
             "`similarity_score` DOUBLE DEFAULT NULL," +
             "`attempted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // Badge tables
        exec("CREATE TABLE IF NOT EXISTS `badge` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`name` VARCHAR(100) NOT NULL," +
             "`description` TEXT," +
             "`condition_type` VARCHAR(50)," +
             "`condition_value` INT DEFAULT 0," +
             "`icon` VARCHAR(20) DEFAULT NULL" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        exec("CREATE TABLE IF NOT EXISTS `user_badge` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`badge_id` INT NOT NULL," +
             "`unlocked` TINYINT(1) NOT NULL DEFAULT 0," +
             "`unlocked_at` DATETIME DEFAULT NULL," +
             "`current_value` INT NOT NULL DEFAULT 0," +
             "`is_vitrine` TINYINT(1) NOT NULL DEFAULT 0," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE," +
             "FOREIGN KEY (`badge_id`) REFERENCES `badge`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // Add missing columns to existing tables (migration)
        String[][] cols = {
            {"reset_token",                  "VARCHAR(255) DEFAULT NULL"},
            {"reset_token_expires_at",       "DATETIME DEFAULT NULL"},
            {"verification_code",            "VARCHAR(6) DEFAULT NULL"},
            {"verification_code_expires_at", "DATETIME DEFAULT NULL"},
            {"face_descriptor",              "TEXT DEFAULT NULL"},
            {"face_id_enrolled_at",          "DATETIME DEFAULT NULL"},
            {"welcome_message",              "TEXT DEFAULT NULL"},
            {"google_id",                    "VARCHAR(255) DEFAULT NULL"},
            {"photo_filename",               "VARCHAR(255) DEFAULT NULL"},
            {"first_name",                   "VARCHAR(100) NOT NULL DEFAULT ''"},
            {"last_name",                    "VARCHAR(100) NOT NULL DEFAULT ''"},
            {"birthday",                     "DATE NOT NULL DEFAULT '2000-01-01'"},
            {"weight",                       "DOUBLE NOT NULL DEFAULT 70"},
            {"height",                       "DOUBLE NOT NULL DEFAULT 170"},
        };
        for (String[] col : cols) addColumnIfMissing("user", col[0], col[1]);

        // Fix any NOT NULL columns from old Symfony schema that might block inserts
        exec("ALTER TABLE `user` MODIFY COLUMN `first_name` VARCHAR(100) NOT NULL DEFAULT ''");
        exec("ALTER TABLE `user` MODIFY COLUMN `last_name` VARCHAR(100) NOT NULL DEFAULT ''");
        exec("ALTER TABLE `user` MODIFY COLUMN `birthday` DATE NOT NULL DEFAULT '2000-01-01'");
        exec("ALTER TABLE `user` MODIFY COLUMN `weight` DOUBLE NOT NULL DEFAULT 70");
        exec("ALTER TABLE `user` MODIFY COLUMN `height` DOUBLE NOT NULL DEFAULT 170");

        // If old Symfony columns exist (nom, prenom, etc.), make them nullable to avoid insert failures
        exec("ALTER TABLE `user` MODIFY COLUMN `nom` VARCHAR(100) DEFAULT NULL");
        exec("ALTER TABLE `user` MODIFY COLUMN `prenom` VARCHAR(100) DEFAULT NULL");
        exec("ALTER TABLE `user` MODIFY COLUMN `date_naissance` DATE DEFAULT NULL");
        exec("ALTER TABLE `user` MODIFY COLUMN `poids` DOUBLE DEFAULT NULL");
        exec("ALTER TABLE `user` MODIFY COLUMN `taille` DOUBLE DEFAULT NULL");
        exec("UPDATE `user` SET `roles`='ROLE_ADMIN' WHERE `roles` LIKE '%ROLE_ADMIN%' AND `roles`!='ROLE_ADMIN'");
        exec("UPDATE `user` SET `roles`='ROLE_USER'  WHERE `roles` NOT IN ('ROLE_USER','ROLE_ADMIN')");

        // Remove old phone columns if they exist (cleanup)
        dropColumnIfExists("user", "phone_number");
        dropColumnIfExists("user", "phone_verified");

        // Add icon column to badge if missing
        addColumnIfMissing("badge", "icon", "VARCHAR(20) DEFAULT NULL");
        addColumnIfMissing("badge", "name", "VARCHAR(100) NOT NULL DEFAULT ''");
        addColumnIfMissing("badge", "description", "TEXT DEFAULT NULL");
        addColumnIfMissing("badge", "condition_type", "VARCHAR(50) DEFAULT NULL");
        addColumnIfMissing("badge", "condition_value", "INT DEFAULT 0");

        System.out.println("[initSchema] Done.");
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) {
            ps.setString(1, table);
            ps.setString(2, column);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                exec("ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
                System.out.println("[schema] Added column: " + column);
            }
        } catch (SQLException e) {
            System.err.println("[schema] addColumn " + column + ": " + e.getMessage());
        }
    }

    private void dropColumnIfExists(String table, String column) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?")) {
            ps.setString(1, table);
            ps.setString(2, column);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                exec("ALTER TABLE `" + table + "` DROP COLUMN `" + column + "`");
                System.out.println("[schema] Dropped column: " + column);
            }
        } catch (SQLException e) {
            System.err.println("[schema] dropColumn " + column + ": " + e.getMessage());
        }
    }

    private void exec(String sql) {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            // Ignore "duplicate column" / "already exists" errors silently
            if (!e.getMessage().contains("Duplicate column") &&
                !e.getMessage().contains("already exists") &&
                !e.getMessage().contains("Can't DROP")) {
                System.err.println("[schema] " + e.getMessage());
            }
        }
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) instance = new MyBDConnexion();
        return instance;
    }

    public Connection getCnx() { return cnx; }
}
