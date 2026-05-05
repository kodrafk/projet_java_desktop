package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBDConnexion {

    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://localhost:3306/integration?serverTimezone=UTC&sslMode=DISABLED&createDatabaseIfNotExist=true&zeroDateTimeBehavior=convertToNull";
    private static MyBDConnexion instance;
    private Connection cnx;

    private MyBDConnexion() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion DB établie");
            initTable();
            initComplaintsTable();
            initComplaintResponseTable();
            initExtendedTables();
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
                {"first_name",                    "VARCHAR(100) NOT NULL DEFAULT ''"},
                {"last_name",                     "VARCHAR(100) NOT NULL DEFAULT ''"},
                {"birthday",                      "DATE NOT NULL DEFAULT '2000-01-01'"},
                {"weight",                        "FLOAT DEFAULT NULL"},
                {"height",                        "FLOAT DEFAULT NULL"},
                {"phone_number",                  "VARCHAR(20) DEFAULT NULL"},
                {"phone_verified",                "TINYINT(1) NOT NULL DEFAULT 0"},
                {"photo_filename",                "VARCHAR(255) DEFAULT NULL"},
                {"welcome_message",               "TEXT DEFAULT NULL"},
                // gestion_user fields
                {"face_descriptor",               "TEXT DEFAULT NULL"},
                {"face_id_enrolled_at",           "DATETIME DEFAULT NULL"},
                {"google_id",                     "VARCHAR(255) DEFAULT NULL"},
                {"reset_token",                   "VARCHAR(255) DEFAULT NULL"},
                {"reset_token_expires_at",        "DATETIME DEFAULT NULL"},
                {"verification_code",             "VARCHAR(10) DEFAULT NULL"},
                {"verification_code_expires_at",  "DATETIME DEFAULT NULL"},
                {"gallery_access_enabled",        "TINYINT(1) NOT NULL DEFAULT 0"},
                {"phone",                         "VARCHAR(20) DEFAULT NULL"},
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
            
            // Add image_path to TEXT type safely
            try (java.sql.PreparedStatement ps = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='complaint' AND COLUMN_NAME='image_path'")) {
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    st.executeUpdate("ALTER TABLE `complaint` ADD COLUMN `image_path` TEXT");
                } else {
                    st.executeUpdate("ALTER TABLE `complaint` MODIFY COLUMN `image_path` TEXT");
                }
            }

            // Add incident_date safely
            try (java.sql.PreparedStatement ps = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='complaint' AND COLUMN_NAME='incident_date'")) {
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    st.executeUpdate("ALTER TABLE `complaint` ADD COLUMN `incident_date` DATE");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("[initComplaintsTable] error : " + e.getMessage());
        }
    }

    private void initComplaintResponseTable() {
        String create = "CREATE TABLE IF NOT EXISTS `complaint_response` (" +
                "`id`               INT AUTO_INCREMENT PRIMARY KEY," +
                "`complaint_id`     INT NOT NULL," +
                "`response_content` TEXT NOT NULL," +
                "`response_date`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (`complaint_id`) REFERENCES `complaint`(`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (java.sql.Statement st = cnx.createStatement()) {
            st.executeUpdate(create);
            System.out.println("[initComplaintResponseTable] Complaint Response Table ready.");
        } catch (SQLException e) {
            System.err.println("[initComplaintResponseTable] error : " + e.getMessage());
        }
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) {
            instance = new MyBDConnexion();
        }
        return instance;
    }

    /** Force a fresh reconnection (called when connection is stale) */
    public static void resetInstance() {
        try {
            if (instance != null && instance.cnx != null && !instance.cnx.isClosed()) {
                instance.cnx.close();
            }
        } catch (Exception ignored) {}
        instance = null;
        System.out.println("[MyBDConnexion] Instance reset — will reconnect on next call.");
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed() || !cnx.isValid(1)) {
                System.out.println("[MyBDConnexion] Reconnecting...");
                cnx = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[MyBDConnexion] Reconnected ✅");
            }
        } catch (SQLException e) {
            System.err.println("[MyBDConnexion] Reconnect failed: " + e.getMessage());
        }
        return cnx;
    }

    /** Alias for getCnx() to maintain compatibility with Event/Sponsor services */
    public Connection getConnection() {
        return cnx;
    }

    /** Alias for getCnx() used by some gestion_user repositories */
    public Connection getConnexion() {
        return cnx;
    }

    /** Check if using SQLite (always false for integration project) */
    public boolean isUsingSQLite() { return false; }

    private void initExtendedTables() {
        // weight_log
        exec("CREATE TABLE IF NOT EXISTS `weight_log` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`weight` DECIMAL(5,2) NOT NULL," +
             "`photo` VARCHAR(255) DEFAULT NULL," +
             "`note` TEXT DEFAULT NULL," +
             "`logged_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // weight_objective
        exec("CREATE TABLE IF NOT EXISTS `weight_objective` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`start_weight` DECIMAL(5,2) NOT NULL," +
             "`target_weight` DECIMAL(5,2) NOT NULL," +
             "`start_date` DATE NOT NULL," +
             "`target_date` DATE NOT NULL," +
             "`start_photo` VARCHAR(255) DEFAULT NULL," +
             "`is_active` TINYINT(1) DEFAULT 1," +
             "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // progress_photo
        exec("CREATE TABLE IF NOT EXISTS `progress_photo` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`filename` VARCHAR(255) NOT NULL," +
             "`caption` TEXT DEFAULT NULL," +
             "`weight` DECIMAL(5,2) DEFAULT NULL," +
             "`taken_at` TIMESTAMP NULL," +
             "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // badge
        exec("CREATE TABLE IF NOT EXISTS `badge` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`nom` VARCHAR(100) NOT NULL," +
             "`description` TEXT," +
             "`condition_text` VARCHAR(255)," +
             "`condition_type` VARCHAR(50)," +
             "`condition_value` INT DEFAULT 0," +
             "`svg` VARCHAR(50)," +
             "`couleur` VARCHAR(20)," +
             "`couleur_bg` VARCHAR(20)," +
             "`categorie` VARCHAR(50)," +
             "`ordre` INT DEFAULT 0," +
             "`rarete` VARCHAR(20) DEFAULT 'common'" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // user_badge
        exec("CREATE TABLE IF NOT EXISTS `user_badge` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`badge_id` INT NOT NULL," +
             "`unlocked` TINYINT(1) DEFAULT 0," +
             "`unlocked_at` DATETIME DEFAULT NULL," +
             "`current_value` INT DEFAULT 0," +
             "`is_vitrine` TINYINT(1) DEFAULT 0," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE," +
             "FOREIGN KEY (`badge_id`) REFERENCES `badge`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // user_alert
        exec("CREATE TABLE IF NOT EXISTS `user_alert` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`admin_id` INT DEFAULT NULL," +
             "`title` VARCHAR(200) NOT NULL," +
             "`message` TEXT NOT NULL," +
             "`type` VARCHAR(20) DEFAULT 'INFO'," +
             "`category` VARCHAR(20) DEFAULT 'SYSTEM'," +
             "`is_read` TINYINT(1) DEFAULT 0," +
             "`is_dismissed` TINYINT(1) DEFAULT 0," +
             "`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
             "`expires_at` DATETIME DEFAULT NULL," +
             "`read_at` DATETIME DEFAULT NULL," +
             "`action_url` VARCHAR(255) DEFAULT NULL," +
             "`action_label` VARCHAR(100) DEFAULT NULL," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // user_group
        exec("CREATE TABLE IF NOT EXISTS `user_group` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`name` VARCHAR(100) NOT NULL," +
             "`description` TEXT," +
             "`admin_id` INT NOT NULL," +
             "`color` VARCHAR(20) DEFAULT '#2E7D5A'," +
             "`pinned` TINYINT(1) DEFAULT 0," +
             "`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // user_group_member
        exec("CREATE TABLE IF NOT EXISTS `user_group_member` (" +
             "`group_id` INT NOT NULL," +
             "`user_id` INT NOT NULL," +
             "PRIMARY KEY (`group_id`, `user_id`)," +
             "FOREIGN KEY (`group_id`) REFERENCES `user_group`(`id`) ON DELETE CASCADE," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // admin_message
        exec("CREATE TABLE IF NOT EXISTS `admin_message` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`admin_id` INT NOT NULL," +
             "`message` TEXT NOT NULL," +
             "`sent_via_sms` TINYINT(1) DEFAULT 0," +
             "`is_read` TINYINT(1) DEFAULT 0," +
             "`sent_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
             "`read_at` DATETIME DEFAULT NULL," +
             "`sms_status` VARCHAR(50) DEFAULT NULL," +
             "`sms_id` VARCHAR(100) DEFAULT NULL," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // personalized_message
        exec("CREATE TABLE IF NOT EXISTS `personalized_message` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`admin_id` INT NOT NULL," +
             "`content` TEXT NOT NULL," +
             "`send_via_sms` TINYINT(1) DEFAULT 0," +
             "`sms_status` VARCHAR(50) DEFAULT NULL," +
             "`sms_id` VARCHAR(100) DEFAULT NULL," +
             "`is_read` TINYINT(1) DEFAULT 0," +
             "`sent_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
             "`read_at` DATETIME DEFAULT NULL," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // health_anomaly
        exec("CREATE TABLE IF NOT EXISTS `health_anomaly` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`type` VARCHAR(50) NOT NULL," +
             "`description` TEXT," +
             "`severity` DOUBLE DEFAULT 0," +
             "`confidence` DOUBLE DEFAULT 0," +
             "`details` TEXT," +
             "`resolved` TINYINT(1) DEFAULT 0," +
             "`detected_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
             "`resolved_at` DATETIME DEFAULT NULL," +
             "`resolved_by` VARCHAR(100) DEFAULT NULL," +
             "`resolution` TEXT DEFAULT NULL," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // health_alert
        exec("CREATE TABLE IF NOT EXISTS `health_alert` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`anomaly_id` INT DEFAULT NULL," +
             "`title` VARCHAR(200) NOT NULL," +
             "`message` TEXT NOT NULL," +
             "`priority` VARCHAR(20) DEFAULT 'LOW'," +
             "`risk_score` DOUBLE DEFAULT 0," +
             "`recommendation` TEXT DEFAULT NULL," +
             "`sent` TINYINT(1) DEFAULT 0," +
             "`acknowledged` TINYINT(1) DEFAULT 0," +
             "`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
             "`sent_at` DATETIME DEFAULT NULL," +
             "`acknowledged_at` DATETIME DEFAULT NULL," +
             "`acknowledged_by` VARCHAR(100) DEFAULT NULL," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // face_embeddings
        exec("CREATE TABLE IF NOT EXISTS `face_embeddings` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL UNIQUE," +
             "`embedding_encrypted` TEXT NOT NULL," +
             "`algorithm` VARCHAR(50) DEFAULT 'ArcFace'," +
             "`is_active` TINYINT(1) DEFAULT 1," +
             "`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
             "`updated_at` DATETIME DEFAULT NULL," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // face_verification_attempts
        exec("CREATE TABLE IF NOT EXISTS `face_verification_attempts` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT DEFAULT NULL," +
             "`success` TINYINT(1) DEFAULT 0," +
             "`confidence` DOUBLE DEFAULT 0," +
             "`ip_address` VARCHAR(45) DEFAULT NULL," +
             "`attempted_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE SET NULL" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // chat_message
        exec("CREATE TABLE IF NOT EXISTS `chat_message` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`sender_id` INT NOT NULL," +
             "`receiver_id` INT NOT NULL," +
             "`sender_type` VARCHAR(10) NOT NULL DEFAULT 'USER'," +
             "`content` TEXT DEFAULT NULL," +
             "`image_path` VARCHAR(255) DEFAULT NULL," +
             "`edited` TINYINT(1) DEFAULT 0," +
             "`deleted` TINYINT(1) DEFAULT 0," +
             "`is_read` TINYINT(1) DEFAULT 0," +
             "`sent_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
             "`edited_at` DATETIME DEFAULT NULL," +
             "`read_at` DATETIME DEFAULT NULL," +
             "`sent_via_sms` TINYINT(1) DEFAULT 0," +
             "`sms_status` VARCHAR(50) DEFAULT NULL," +
             "FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`) ON DELETE CASCADE," +
             "FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // ── Blog tables ──────────────────────────────────────────────────────
        // Réparer les tables blog existantes si elles ont été créées sans AUTO_INCREMENT/PRIMARY KEY
        fixBlogTable("publication");
        fixBlogTable("publication_comment");
        fixBlogTable("publication_like");
        fixBlogTable("publication_report");

        // publication
        exec("CREATE TABLE IF NOT EXISTS `publication` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`titre` VARCHAR(100) NOT NULL," +
             "`contenu` TEXT NOT NULL," +
             "`description` TEXT DEFAULT NULL," +
             "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
             "`author_name` VARCHAR(100) DEFAULT NULL," +
             "`author_avatar` VARCHAR(255) DEFAULT NULL," +
             "`is_admin` TINYINT(1) DEFAULT 0," +
             "`image` VARCHAR(255) DEFAULT NULL," +
             "`view_count` INT DEFAULT 0," +
             "`share_count` INT DEFAULT 0," +
             "`visibility` VARCHAR(20) DEFAULT 'public'," +
             "`scheduled_at` TIMESTAMP NULL DEFAULT NULL," +
             "`shared_from_id` INT DEFAULT NULL," +
             "`user_id` INT NOT NULL," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // publication_comment
        exec("CREATE TABLE IF NOT EXISTS `publication_comment` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`contenu` TEXT NOT NULL," +
             "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
             "`author_name` VARCHAR(100) DEFAULT NULL," +
             "`author_avatar` VARCHAR(255) DEFAULT NULL," +
             "`is_admin` TINYINT(1) DEFAULT 0," +
             "`publication_id` INT NOT NULL," +
             "`user_id` INT NOT NULL," +
             "FOREIGN KEY (`publication_id`) REFERENCES `publication`(`id`) ON DELETE CASCADE," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // publication_like
        exec("CREATE TABLE IF NOT EXISTS `publication_like` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
             "`publication_id` INT NOT NULL," +
             "`user_id` INT NOT NULL," +
             "`is_like` TINYINT(1) NOT NULL," +
             "FOREIGN KEY (`publication_id`) REFERENCES `publication`(`id`) ON DELETE CASCADE," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE," +
             "UNIQUE KEY `unique_user_publication` (`publication_id`, `user_id`)" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        // publication_report
        exec("CREATE TABLE IF NOT EXISTS `publication_report` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`publication_id` INT NOT NULL," +
             "`user_id` INT NOT NULL," +
             "`reason` VARCHAR(255) DEFAULT NULL," +
             "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (`publication_id`) REFERENCES `publication`(`id`) ON DELETE CASCADE," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE," +
             "UNIQUE KEY `unique_user_report` (`publication_id`, `user_id`)" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

        System.out.println("[MyBDConnexion] Extended tables initialized.");
    }

    /**
     * Répare une table blog qui aurait été créée sans PRIMARY KEY / AUTO_INCREMENT.
     * Stratégie : si id n'a pas auto_increment → DROP et recréer proprement.
     */
    private void fixBlogTable(String tableName) {
        try {
            // Vérifier si la table existe
            java.sql.ResultSet tables = cnx.getMetaData().getTables(null, null, tableName, null);
            if (!tables.next()) return; // table n'existe pas encore, rien à faire

            // Vérifier si id a déjà auto_increment
            try (java.sql.PreparedStatement ps = cnx.prepareStatement(
                    "SELECT EXTRA, COLUMN_KEY FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME='id'")) {
                ps.setString(1, tableName);
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String extra = rs.getString("EXTRA");
                    String key   = rs.getString("COLUMN_KEY");
                    boolean hasAutoInc = extra != null && extra.contains("auto_increment");
                    boolean hasPK      = "PRI".equals(key);

                    if (!hasAutoInc || !hasPK) {
                        System.out.println("[fixBlogTable] Dropping and recreating " + tableName + " (missing AUTO_INCREMENT/PK)");
                        // Désactiver les FK checks pour pouvoir dropper
                        cnx.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS=0");
                        cnx.createStatement().executeUpdate("DROP TABLE IF EXISTS `" + tableName + "`");
                        cnx.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS=1");
                        System.out.println("[fixBlogTable] Dropped " + tableName + " — will be recreated with correct schema.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[fixBlogTable] " + tableName + ": " + e.getMessage());
        }
    }

    private void exec(String sql) {
        try (java.sql.Statement st = cnx.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            // Ignore "already exists" errors
            if (!e.getMessage().contains("already exists") && !e.getMessage().contains("Duplicate")) {
                System.err.println("[MyBDConnexion] exec: " + e.getMessage());
            }
        }
    }
}

