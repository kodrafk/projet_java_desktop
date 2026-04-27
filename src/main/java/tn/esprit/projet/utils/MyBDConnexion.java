package tn.esprit.projet.utils;

import java.sql.*;

public class MyBDConnexion {

    private static MyBDConnexion instance;
    private Connection cnx;
    private boolean usingSQLite = false;

    private MyBDConnexion() {
        // Try MySQL first, fallback to SQLite
        try {
            System.out.println("[MyBDConnexion] Attempting MySQL...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.USER,
                    DatabaseConfig.PASSWORD);
            System.out.println("✅ [MyBDConnexion] MySQL connected");
            usingSQLite = false;
        } catch (Exception e) {
            System.err.println("⚠️  [MyBDConnexion] MySQL unavailable: " + e.getMessage());
            System.out.println("[MyBDConnexion] Switching to SQLite...");
            
            try {
                Class.forName("org.sqlite.JDBC");
                cnx = DriverManager.getConnection("jdbc:sqlite:nutrilife.db");
                System.out.println("✅ [MyBDConnexion] SQLite connected: nutrilife.db");
                usingSQLite = true;
            } catch (Exception ex) {
                System.err.println("❌ [MyBDConnexion] SQLite failed: " + ex.getMessage());
                throw new RuntimeException("Unable to connect to MySQL or SQLite", ex);
            }
        }
        
        initSchema();
    }

    private void initSchema() {
        if (usingSQLite) {
            initSQLiteSchema();
        } else {
            initMySQLSchema();
        }
    }

    private void initSQLiteSchema() {
        // SQLite schema (simplified, no ENGINE, no AUTO_INCREMENT keyword)
        exec("CREATE TABLE IF NOT EXISTS user (" +
             "id INTEGER PRIMARY KEY AUTOINCREMENT," +
             "email TEXT NOT NULL UNIQUE," +
             "password TEXT NOT NULL," +
             "roles TEXT NOT NULL DEFAULT 'ROLE_USER'," +
             "is_active INTEGER NOT NULL DEFAULT 1," +
             "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "reset_token TEXT DEFAULT NULL," +
             "reset_token_expires_at TEXT DEFAULT NULL," +
             "verification_code TEXT DEFAULT NULL," +
             "verification_code_expires_at TEXT DEFAULT NULL," +
             "face_descriptor TEXT DEFAULT NULL," +
             "face_id_enrolled_at TEXT DEFAULT NULL," +
             "welcome_message TEXT DEFAULT NULL," +
             "google_id TEXT DEFAULT NULL," +
             "photo_filename TEXT DEFAULT NULL," +
             "first_name TEXT NOT NULL DEFAULT ''," +
             "last_name TEXT NOT NULL DEFAULT ''," +
             "birthday TEXT NOT NULL DEFAULT '2000-01-01'," +
             "weight REAL NOT NULL DEFAULT 70," +
             "height REAL NOT NULL DEFAULT 170)");
        
        // Add missing columns if table already exists
        addColumnIfMissingSQLite("user", "welcome_message", "TEXT DEFAULT NULL");
        addColumnIfMissingSQLite("user", "google_id", "TEXT DEFAULT NULL");
        addColumnIfMissingSQLite("user", "photo_filename", "TEXT DEFAULT NULL");

        exec("CREATE TABLE IF NOT EXISTS face_embeddings (" +
             "id INTEGER PRIMARY KEY AUTOINCREMENT," +
             "user_id INTEGER NOT NULL UNIQUE," +
             "embedding_encrypted TEXT NOT NULL," +
             "encryption_iv TEXT NOT NULL," +
             "encryption_tag TEXT NOT NULL," +
             "is_active INTEGER NOT NULL DEFAULT 1," +
             "consent_given_at TEXT DEFAULT NULL," +
             "consent_ip TEXT DEFAULT NULL," +
             "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "last_used_at TEXT DEFAULT NULL," +
             "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE)");

        exec("CREATE TABLE IF NOT EXISTS face_verification_attempts (" +
             "id INTEGER PRIMARY KEY AUTOINCREMENT," +
             "user_id INTEGER DEFAULT NULL," +
             "email TEXT DEFAULT NULL," +
             "ip_address TEXT NOT NULL DEFAULT '127.0.0.1'," +
             "success INTEGER NOT NULL DEFAULT 0," +
             "similarity_score REAL DEFAULT NULL," +
             "attempted_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL)");

        exec("CREATE TABLE IF NOT EXISTS weight_objective (" +
             "id INTEGER PRIMARY KEY AUTOINCREMENT," +
             "user_id INTEGER NOT NULL UNIQUE," +
             "start_weight REAL NOT NULL," +
             "target_weight REAL NOT NULL," +
             "start_date TEXT NOT NULL," +
             "target_date TEXT NOT NULL," +
             "start_photo TEXT DEFAULT NULL," +
             "is_active INTEGER NOT NULL DEFAULT 1," +
             "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE)");

        exec("CREATE TABLE IF NOT EXISTS weight_log (" +
             "id INTEGER PRIMARY KEY AUTOINCREMENT," +
             "user_id INTEGER NOT NULL," +
             "weight REAL NOT NULL," +
             "photo TEXT DEFAULT NULL," +
             "note TEXT DEFAULT NULL," +
             "logged_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE)");

        exec("CREATE TABLE IF NOT EXISTS badge (" +
             "id INTEGER PRIMARY KEY AUTOINCREMENT," +
             "nom TEXT NOT NULL," +
             "description TEXT DEFAULT NULL," +
             "condition_text TEXT DEFAULT NULL," +
             "condition_type TEXT DEFAULT NULL," +
             "condition_value INTEGER DEFAULT 0," +
             "svg TEXT DEFAULT NULL," +
             "couleur TEXT DEFAULT '#2E7D32'," +
             "couleur_bg TEXT DEFAULT '#F0FDF4'," +
             "categorie TEXT DEFAULT NULL," +
             "ordre INTEGER DEFAULT 0," +
             "rarete TEXT DEFAULT 'common')");

        exec("CREATE TABLE IF NOT EXISTS user_badge (" +
             "id INTEGER PRIMARY KEY AUTOINCREMENT," +
             "user_id INTEGER NOT NULL," +
             "badge_id INTEGER NOT NULL," +
             "unlocked INTEGER NOT NULL DEFAULT 0," +
             "unlocked_at TEXT DEFAULT NULL," +
             "current_value INTEGER NOT NULL DEFAULT 0," +
             "is_vitrine INTEGER NOT NULL DEFAULT 0," +
             "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE," +
             "FOREIGN KEY (badge_id) REFERENCES badge(id) ON DELETE CASCADE)");

        System.out.println("✅ [SQLite] Tables created");
    }
    
    private void addColumnIfMissingSQLite(String table, String column, String definition) {
        try {
            // Try to add the column
            exec("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (Exception e) {
            // Column already exists, ignore
        }
    }

    private void initMySQLSchema() {
        // ── Auto-fix tables BEFORE creating them ──────────────────────────────
        autoFixMySQLTables();

        // Original MySQL schema (unchanged)
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

        exec("CREATE TABLE IF NOT EXISTS `weight_objective` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL UNIQUE," +
             "`start_weight` DOUBLE NOT NULL," +
             "`target_weight` DOUBLE NOT NULL," +
             "`start_date` DATE NOT NULL," +
             "`target_date` DATE NOT NULL," +
             "`start_photo` VARCHAR(255) DEFAULT NULL," +
             "`is_active` TINYINT(1) NOT NULL DEFAULT 1," +
             "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        exec("CREATE TABLE IF NOT EXISTS `weight_log` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`weight` DOUBLE NOT NULL," +
             "`photo` VARCHAR(255) DEFAULT NULL," +
             "`note` VARCHAR(255) DEFAULT NULL," +
             "`logged_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        exec("CREATE TABLE IF NOT EXISTS `badge` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`nom` VARCHAR(100) NOT NULL," +
             "`description` VARCHAR(255) DEFAULT NULL," +
             "`condition_text` VARCHAR(255) DEFAULT NULL," +
             "`condition_type` VARCHAR(50) DEFAULT NULL," +
             "`condition_value` INT DEFAULT 0," +
             "`svg` VARCHAR(30) DEFAULT NULL," +
             "`couleur` VARCHAR(20) DEFAULT '#2E7D32'," +
             "`couleur_bg` VARCHAR(20) DEFAULT '#F0FDF4'," +
             "`categorie` VARCHAR(50) DEFAULT NULL," +
             "`ordre` INT DEFAULT 0," +
             "`rarete` VARCHAR(20) DEFAULT 'common'" +
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

        exec("CREATE TABLE IF NOT EXISTS `progress_photo` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`filename` VARCHAR(255) NOT NULL," +
             "`caption` TEXT DEFAULT NULL," +
             "`weight` DOUBLE DEFAULT NULL," +
             "`taken_at` DATETIME DEFAULT NULL," +
             "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        exec("CREATE TABLE IF NOT EXISTS `message` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`sender_id` INT NOT NULL," +
             "`receiver_id` INT NOT NULL," +
             "`content` TEXT NOT NULL," +
             "`is_read` TINYINT(1) NOT NULL DEFAULT 0," +
             "`sent_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "`read_at` DATETIME DEFAULT NULL," +
             "FOREIGN KEY (`sender_id`) REFERENCES `user`(`id`) ON DELETE CASCADE," +
             "FOREIGN KEY (`receiver_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        System.out.println("✅ [MySQL] Tables created");

        // ── AI Anomaly Detection Tables ────────────────────────────────────────
        exec("CREATE TABLE IF NOT EXISTS `health_anomalies` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`type` VARCHAR(50) NOT NULL," +
             "`description` TEXT," +
             "`severity` DOUBLE NOT NULL DEFAULT 0," +
             "`confidence` DOUBLE DEFAULT 0," +
             "`details` TEXT," +
             "`resolved` TINYINT(1) DEFAULT 0," +
             "`detected_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
             "`resolved_at` TIMESTAMP NULL," +
             "`resolved_by` VARCHAR(100)," +
             "`resolution` TEXT" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        exec("CREATE TABLE IF NOT EXISTS `health_alerts` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL," +
             "`anomaly_id` INT DEFAULT 0," +
             "`title` VARCHAR(200)," +
             "`message` TEXT NOT NULL," +
             "`priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM'," +
             "`risk_score` DOUBLE DEFAULT 0," +
             "`recommendation` TEXT," +
             "`sent` TINYINT(1) DEFAULT 0," +
             "`acknowledged` TINYINT(1) DEFAULT 0," +
             "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
             "`sent_at` TIMESTAMP NULL," +
             "`acknowledged_at` TIMESTAMP NULL," +
             "`acknowledged_by` VARCHAR(100)" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        exec("CREATE TABLE IF NOT EXISTS `user_health_metrics` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL UNIQUE," +
             "`current_weight` DOUBLE DEFAULT 0," +
             "`weight_change_7days` DOUBLE DEFAULT 0," +
             "`weight_change_30days` DOUBLE DEFAULT 0," +
             "`days_since_last_log` INT DEFAULT 0," +
             "`total_logs` INT DEFAULT 0," +
             "`weight_variance` DOUBLE DEFAULT 0," +
             "`avg_weekly_change` DOUBLE DEFAULT 0," +
             "`has_active_goal` TINYINT(1) DEFAULT 0," +
             "`goal_realistic_score` DOUBLE DEFAULT 100," +
             "`abandonment_risk` DOUBLE DEFAULT 0," +
             "`activity_score` DOUBLE DEFAULT 0," +
             "`calculated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        exec("CREATE TABLE IF NOT EXISTS `anomaly_detection_history` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`detection_type` VARCHAR(20) NOT NULL," +
             "`anomalies_found` INT DEFAULT 0," +
             "`users_scanned` INT DEFAULT 0," +
             "`execution_time_ms` BIGINT DEFAULT 0," +
             "`status` VARCHAR(20) NOT NULL," +
             "`error_message` TEXT," +
             "`executed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        System.out.println("✅ [AI] Anomaly detection tables ready");

        // Ensure face recognition tables exist
        exec("CREATE TABLE IF NOT EXISTS `face_embeddings` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT NOT NULL UNIQUE," +
             "`embedding_encrypted` MEDIUMTEXT NOT NULL," +
             "`encryption_iv` VARCHAR(255) NOT NULL," +
             "`encryption_tag` VARCHAR(255) NOT NULL," +
             "`is_active` TINYINT(1) NOT NULL DEFAULT 1," +
             "`consent_given_at` DATETIME DEFAULT NULL," +
             "`consent_ip` VARCHAR(45) DEFAULT NULL," +
             "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
             "`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
             "`last_used_at` DATETIME DEFAULT NULL" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        exec("CREATE TABLE IF NOT EXISTS `face_verification_attempts` (" +
             "`id` INT AUTO_INCREMENT PRIMARY KEY," +
             "`user_id` INT DEFAULT NULL," +
             "`email` VARCHAR(255) DEFAULT NULL," +
             "`ip_address` VARCHAR(45) NOT NULL DEFAULT '127.0.0.1'," +
             "`success` TINYINT(1) NOT NULL DEFAULT 0," +
             "`similarity_score` DOUBLE DEFAULT NULL," +
             "`attempted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        System.out.println("[FaceDB] ✅ Face tables ready");
    }

    /**
     * Detects and fixes incorrect table structures automatically at startup.
     * Uses information_schema queries — reliable across all MySQL versions.
     */
    private void autoFixMySQLTables() {
        System.out.println("[AutoFix] Checking table structure...");
        try {
            // Get current database name
            String dbName = null;
            try (Statement st = cnx.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) dbName = rs.getString(1);
            }
            if (dbName == null) { System.err.println("[AutoFix] Cannot determine database name"); return; }
            System.out.println("[AutoFix] Database: " + dbName);

            // ── weight_log ────────────────────────────────────────────────────
            boolean wlExists   = tableExistsSql(dbName, "weight_log");
            boolean wlHasPhoto = wlExists && columnExistsSql(dbName, "weight_log", "photo");
            boolean wlHasNote  = wlExists && columnExistsSql(dbName, "weight_log", "note");
            boolean wlHasLogAt = wlExists && columnExistsSql(dbName, "weight_log", "logged_at");

            System.out.println("[AutoFix] weight_log: exists=" + wlExists
                    + " photo=" + wlHasPhoto + " note=" + wlHasNote + " logged_at=" + wlHasLogAt);

            if (wlExists && (!wlHasPhoto || !wlHasNote || !wlHasLogAt)) {
                System.out.println("[AutoFix] weight_log incorrect structure → recreating...");
                execRaw("SET FOREIGN_KEY_CHECKS=0");
                execRaw("DROP TABLE IF EXISTS `weight_log`");
                execRaw("SET FOREIGN_KEY_CHECKS=1");
                execRaw("CREATE TABLE `weight_log` (" +
                        "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                        "`user_id` INT NOT NULL," +
                        "`weight` DOUBLE NOT NULL," +
                        "`photo` VARCHAR(255) DEFAULT NULL," +
                        "`note` VARCHAR(255) DEFAULT NULL," +
                        "`logged_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                System.out.println("[AutoFix] ✅ weight_log recreated");
            } else if (wlExists) {
                System.out.println("[AutoFix] ✅ weight_log OK");
            }

            // ── weight_objective ──────────────────────────────────────────────
            boolean woExists         = tableExistsSql(dbName, "weight_objective");
            boolean woHasStartWeight = woExists && columnExistsSql(dbName, "weight_objective", "start_weight");
            boolean woHasStartPhoto  = woExists && columnExistsSql(dbName, "weight_objective", "start_photo");
            boolean woHasIsActive    = woExists && columnExistsSql(dbName, "weight_objective", "is_active");

            System.out.println("[AutoFix] weight_objective: exists=" + woExists
                    + " start_weight=" + woHasStartWeight + " start_photo=" + woHasStartPhoto + " is_active=" + woHasIsActive);

            if (woExists && (!woHasStartWeight || !woHasStartPhoto || !woHasIsActive)) {
                System.out.println("[AutoFix] weight_objective incorrect structure → recreating...");
                execRaw("SET FOREIGN_KEY_CHECKS=0");
                execRaw("DROP TABLE IF EXISTS `weight_objective`");
                execRaw("SET FOREIGN_KEY_CHECKS=1");
                execRaw("CREATE TABLE `weight_objective` (" +
                        "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                        "`user_id` INT NOT NULL UNIQUE," +
                        "`start_weight` DOUBLE NOT NULL," +
                        "`target_weight` DOUBLE NOT NULL," +
                        "`start_date` DATE NOT NULL," +
                        "`target_date` DATE NOT NULL," +
                        "`start_photo` VARCHAR(255) DEFAULT NULL," +
                        "`is_active` TINYINT(1) NOT NULL DEFAULT 1," +
                        "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                System.out.println("[AutoFix] ✅ weight_objective recreated");
            } else if (woExists) {
                System.out.println("[AutoFix] ✅ weight_objective OK");
            }

        } catch (Exception e) {
            System.err.println("[AutoFix] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean tableExistsSql(String db, String table) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?")) {
            ps.setString(1, db); ps.setString(2, table);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) { return false; }
    }

    private boolean columnExistsSql(String db, String table, String column) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND COLUMN_NAME=?")) {
            ps.setString(1, db); ps.setString(2, table); ps.setString(3, column);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) { return false; }
    }

    private void execRaw(String sql) throws SQLException {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    private void exec(String sql) {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            if (!e.getMessage().contains("already exists") &&
                !e.getMessage().contains("Duplicate column")) {
                System.err.println("[DB] SQL error: " + e.getMessage().substring(0, Math.min(120, e.getMessage().length())));
            }
        }
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) instance = new MyBDConnexion();
        return instance;
    }

    /**
     * Returns a valid, live connection.
     * If the connection is closed or stale (e.g. MySQL 8h timeout),
     * it automatically reconnects before returning.
     */
    public Connection getCnx() {
        ensureConnected();
        return cnx;
    }

    /**
     * Validates the current connection and reconnects if needed.
     * Called before every DB operation to guarantee a live connection.
     */
    private void ensureConnected() {
        try {
            // isValid(2) sends a lightweight ping — returns false if connection is dead
            if (cnx != null && !cnx.isClosed() && cnx.isValid(2)) {
                return; // Connection is alive, nothing to do
            }
        } catch (SQLException ignored) {}

        // Connection is dead — reconnect
        System.out.println("[MyBDConnexion] 🔄 Connection lost — reconnecting...");
        if (!usingSQLite) {
            try {
                cnx = DriverManager.getConnection(
                        DatabaseConfig.getUrl(),
                        DatabaseConfig.USER,
                        DatabaseConfig.PASSWORD);
                System.out.println("[MyBDConnexion] ✅ MySQL reconnected");
                return;
            } catch (Exception e) {
                System.err.println("[MyBDConnexion] ⚠️ MySQL reconnect failed: " + e.getMessage());
            }
        }
        // Fallback to SQLite
        try {
            cnx = DriverManager.getConnection("jdbc:sqlite:nutrilife.db");
            usingSQLite = true;
            System.out.println("[MyBDConnexion] ✅ SQLite reconnected");
        } catch (Exception ex) {
            System.err.println("[MyBDConnexion] ❌ All reconnect attempts failed: " + ex.getMessage());
            throw new RuntimeException("Database connection unavailable", ex);
        }
    }

    public boolean isUsingSQLite() { return usingSQLite; }
}
