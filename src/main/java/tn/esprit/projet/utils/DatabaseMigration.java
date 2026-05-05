package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Automatically creates or fixes missing database tables at startup.
 * Uses SQL queries directly instead of DatabaseMetaData to avoid
 * MySQL Connector/J ResultSet conflicts.
 */
public class DatabaseMigration {

    public static void migrate() {
        System.out.println("===================================================");
        System.out.println("[DB] Checking database tables...");
        System.out.println("===================================================");

        try {
            Connection conn = MyBDConnexion.getInstance().getCnx();

            ensureWeightLogTable(conn);
            ensureWeightObjectiveTable(conn);
            ensureProgressPhotoTable(conn);
            ensureMessageTable(conn);
            ensureBlogTables(conn);

            System.out.println("[DB] All tables OK.");
            System.out.println("===================================================");

        } catch (Exception e) {
            System.err.println("[DB] Migration error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Helper: check if a table exists via SQL ──────────────────────────────
    private static boolean tableExists(Connection conn, String tableName) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = '" + tableName + "'")) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Helper: check if a column exists in a table ──────────────────────────
    private static boolean columnExists(Connection conn, String tableName, String columnName) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() " +
                "AND table_name = '" + tableName + "' " +
                "AND column_name = '" + columnName + "'")) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ── weight_log ────────────────────────────────────────────────────────────
    private static void ensureWeightLogTable(Connection conn) throws Exception {
        System.out.println("[DB] Checking weight_log...");
        if (!tableExists(conn, "weight_log")) {
            System.out.println("[DB] Creating weight_log...");
            conn.createStatement().executeUpdate(
                "CREATE TABLE weight_log (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "weight DECIMAL(5,2) NOT NULL, " +
                "photo VARCHAR(255), " +
                "note TEXT, " +
                "logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                "INDEX idx_user_logged (user_id, logged_at)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            System.out.println("[DB] weight_log created.");
            return;
        }

        // Table exists — check structure and fix if needed
        boolean hasPhoto    = columnExists(conn, "weight_log", "photo");
        boolean hasNote     = columnExists(conn, "weight_log", "note");
        boolean hasLoggedAt = columnExists(conn, "weight_log", "logged_at");
        boolean hasLogDate  = columnExists(conn, "weight_log", "log_date");

        if (!hasPhoto || !hasNote || !hasLoggedAt || hasLogDate) {
            System.out.println("[DB] weight_log has wrong structure. Recreating...");
            conn.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS weight_log");
            conn.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            conn.createStatement().executeUpdate(
                "CREATE TABLE weight_log (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "weight DECIMAL(5,2) NOT NULL, " +
                "photo VARCHAR(255), " +
                "note TEXT, " +
                "logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                "INDEX idx_user_logged (user_id, logged_at)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            System.out.println("[DB] weight_log recreated.");
        } else {
            System.out.println("[DB] weight_log OK.");
        }
    }

    // ── weight_objective ──────────────────────────────────────────────────────
    private static void ensureWeightObjectiveTable(Connection conn) throws Exception {
        System.out.println("[DB] Checking weight_objective...");
        if (!tableExists(conn, "weight_objective")) {
            System.out.println("[DB] Creating weight_objective...");
            conn.createStatement().executeUpdate(
                "CREATE TABLE weight_objective (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "start_weight DECIMAL(5,2) NOT NULL, " +
                "target_weight DECIMAL(5,2) NOT NULL, " +
                "start_date DATE NOT NULL, " +
                "target_date DATE NOT NULL, " +
                "start_photo VARCHAR(255), " +
                "is_active TINYINT(1) DEFAULT 1, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                "INDEX idx_user_active (user_id, is_active)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            System.out.println("[DB] weight_objective created.");
            return;
        }

        boolean hasStartWeight = columnExists(conn, "weight_objective", "start_weight");
        boolean hasStartPhoto  = columnExists(conn, "weight_objective", "start_photo");
        boolean hasIsActive    = columnExists(conn, "weight_objective", "is_active");
        boolean hasStatus      = columnExists(conn, "weight_objective", "status");

        if (!hasStartWeight || !hasStartPhoto || !hasIsActive || hasStatus) {
            System.out.println("[DB] weight_objective has wrong structure. Recreating...");
            conn.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            conn.createStatement().executeUpdate("DROP TABLE IF EXISTS weight_objective");
            conn.createStatement().executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            conn.createStatement().executeUpdate(
                "CREATE TABLE weight_objective (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "start_weight DECIMAL(5,2) NOT NULL, " +
                "target_weight DECIMAL(5,2) NOT NULL, " +
                "start_date DATE NOT NULL, " +
                "target_date DATE NOT NULL, " +
                "start_photo VARCHAR(255), " +
                "is_active TINYINT(1) DEFAULT 1, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                "INDEX idx_user_active (user_id, is_active)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            System.out.println("[DB] weight_objective recreated.");
        } else {
            System.out.println("[DB] weight_objective OK.");
        }
    }

    // ── progress_photo ────────────────────────────────────────────────────────
    private static void ensureProgressPhotoTable(Connection conn) throws Exception {
        System.out.println("[DB] Checking progress_photo...");
        if (!tableExists(conn, "progress_photo")) {
            System.out.println("[DB] Creating progress_photo...");
            conn.createStatement().executeUpdate(
                "CREATE TABLE progress_photo (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "filename VARCHAR(255) NOT NULL, " +
                "caption TEXT, " +
                "weight DECIMAL(5,2), " +
                "taken_at TIMESTAMP NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                "INDEX idx_user_taken (user_id, taken_at)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            System.out.println("[DB] progress_photo created.");
        } else {
            System.out.println("[DB] progress_photo OK.");
        }
    }

    // ── message ───────────────────────────────────────────────────────────────
    private static void ensureMessageTable(Connection conn) throws Exception {
        System.out.println("[DB] Checking message...");
        if (!tableExists(conn, "message")) {
            System.out.println("[DB] Creating message...");
            conn.createStatement().executeUpdate(
                "CREATE TABLE message (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "sender_id INT NOT NULL, " +
                "receiver_id INT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "is_read TINYINT(1) DEFAULT 0, " +
                "sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "read_at TIMESTAMP NULL, " +
                "FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (receiver_id) REFERENCES user(id) ON DELETE CASCADE, " +
                "INDEX idx_receiver_read (receiver_id, is_read), " +
                "INDEX idx_conversation (sender_id, receiver_id, sent_at)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            System.out.println("[DB] message created.");
        } else {
            System.out.println("[DB] message OK.");
        }
    }

    // ── Blog tables ───────────────────────────────────────────────────────────
    private static void ensureBlogTables(Connection conn) throws Exception {
        System.out.println("[DB] Checking blog tables...");

        // publication
        if (!tableExists(conn, "publication")) {
            System.out.println("[DB] Creating publication...");
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS publication (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "titre VARCHAR(150) NOT NULL, " +
                "contenu TEXT NOT NULL, " +
                "description TEXT DEFAULT NULL, " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "author_name VARCHAR(150) DEFAULT NULL, " +
                "author_avatar VARCHAR(255) DEFAULT NULL, " +
                "is_admin TINYINT(1) NOT NULL DEFAULT 0, " +
                "image VARCHAR(255) DEFAULT NULL, " +
                "view_count INT NOT NULL DEFAULT 0, " +
                "share_count INT NOT NULL DEFAULT 0, " +
                "visibility VARCHAR(20) NOT NULL DEFAULT 'public', " +
                "scheduled_at DATETIME DEFAULT NULL, " +
                "shared_from_id INT DEFAULT NULL, " +
                "user_id INT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );
            System.out.println("[DB] publication created.");
        } else {
            System.out.println("[DB] publication OK.");
        }

        // publication_comment
        if (!tableExists(conn, "publication_comment")) {
            System.out.println("[DB] Creating publication_comment...");
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS publication_comment (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "publication_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "contenu TEXT NOT NULL, " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "author_name VARCHAR(150) DEFAULT NULL, " +
                "author_avatar VARCHAR(255) DEFAULT NULL, " +
                "is_admin TINYINT(1) NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );
            System.out.println("[DB] publication_comment created.");
        } else {
            System.out.println("[DB] publication_comment OK.");
        }

        // publication_like
        if (!tableExists(conn, "publication_like")) {
            System.out.println("[DB] Creating publication_like...");
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS publication_like (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "publication_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "is_like TINYINT(1) NOT NULL DEFAULT 1, " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE KEY unique_user_pub (publication_id, user_id), " +
                "FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );
            System.out.println("[DB] publication_like created.");
        } else {
            System.out.println("[DB] publication_like OK.");
        }

        // publication_report
        if (!tableExists(conn, "publication_report")) {
            System.out.println("[DB] Creating publication_report...");
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS publication_report (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "publication_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "reason VARCHAR(255) NOT NULL DEFAULT 'Contenu inapproprie', " +
                "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE KEY unique_user_report (publication_id, user_id), " +
                "FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );
            System.out.println("[DB] publication_report created.");
        } else {
            System.out.println("[DB] publication_report OK.");
        }
    }
}
