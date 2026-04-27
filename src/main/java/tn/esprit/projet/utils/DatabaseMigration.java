package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utility to automatically migrate and fix database structure
 */
public class DatabaseMigration {

    public static void migrate() {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("🔧 Checking and fixing database...");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            
            // Check and fix weight_log
            fixWeightLogTable(conn, meta);
            
            // Check and fix weight_objective
            fixWeightObjectiveTable(conn, meta);
            
            // Create progress_photo if it doesn't exist
            createProgressPhotoTable(conn, meta);
            
            // Create message if it doesn't exist
            createMessageTable(conn, meta);
            
            System.out.println("✅ Database checked and fixed successfully!");
            System.out.println("═══════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.err.println("❌ Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void fixWeightLogTable(Connection conn, DatabaseMetaData meta) throws Exception {
        System.out.println("\n📊 Checking weight_log table...");
        
        ResultSet tables = meta.getTables(null, null, "weight_log", null);
        
        if (!tables.next()) {
            System.out.println("⚠️  Table weight_log doesn't exist. Creating...");
            createWeightLogTable(conn);
            return;
        }
        
        // Check columns
        ResultSet columns = meta.getColumns(null, null, "weight_log", null);
        boolean hasPhoto = false;
        boolean hasNote = false;
        boolean hasLoggedAt = false;
        boolean hasLogDate = false;
        
        while (columns.next()) {
            String colName = columns.getString("COLUMN_NAME");
            if ("photo".equals(colName)) hasPhoto = true;
            if ("note".equals(colName)) hasNote = true;
            if ("logged_at".equals(colName)) hasLoggedAt = true;
            if ("log_date".equals(colName)) hasLogDate = true;
        }
        
        if (!hasPhoto || !hasNote || !hasLoggedAt || hasLogDate) {
            System.out.println("⚠️  Incorrect structure. Recreating table...");
            Statement stmt = conn.createStatement();
            // Temporarily disable foreign key constraints
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("DROP TABLE IF EXISTS weight_log");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            createWeightLogTable(conn);
        } else {
            System.out.println("✅ Table weight_log OK");
        }
    }
    
    private static void createWeightLogTable(Connection conn) throws Exception {
        String sql = "CREATE TABLE weight_log (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "user_id INT NOT NULL, " +
                     "weight DECIMAL(5,2) NOT NULL, " +
                     "photo VARCHAR(255), " +
                     "note TEXT, " +
                     "logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                     "INDEX idx_user_logged (user_id, logged_at)" +
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        System.out.println("✅ Table weight_log created");
    }
    
    private static void fixWeightObjectiveTable(Connection conn, DatabaseMetaData meta) throws Exception {
        System.out.println("\n🎯 Checking weight_objective table...");
        
        ResultSet tables = meta.getTables(null, null, "weight_objective", null);
        
        if (!tables.next()) {
            System.out.println("⚠️  Table weight_objective doesn't exist. Creating...");
            createWeightObjectiveTable(conn);
            return;
        }
        
        // Check columns
        ResultSet columns = meta.getColumns(null, null, "weight_objective", null);
        boolean hasStartWeight = false;
        boolean hasStartPhoto = false;
        boolean hasIsActive = false;
        boolean hasStatus = false;
        
        while (columns.next()) {
            String colName = columns.getString("COLUMN_NAME");
            if ("start_weight".equals(colName)) hasStartWeight = true;
            if ("start_photo".equals(colName)) hasStartPhoto = true;
            if ("is_active".equals(colName)) hasIsActive = true;
            if ("status".equals(colName)) hasStatus = true;
        }
        
        if (!hasStartWeight || !hasStartPhoto || !hasIsActive || hasStatus) {
            System.out.println("⚠️  Incorrect structure. Recreating table...");
            Statement stmt = conn.createStatement();
            // Temporarily disable foreign key constraints
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("DROP TABLE IF EXISTS weight_objective");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
            createWeightObjectiveTable(conn);
        } else {
            System.out.println("✅ Table weight_objective OK");
        }
    }
    
    private static void createWeightObjectiveTable(Connection conn) throws Exception {
        String sql = "CREATE TABLE weight_objective (" +
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
                     ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        System.out.println("✅ Table weight_objective created");
    }
    
    private static void createProgressPhotoTable(Connection conn, DatabaseMetaData meta) throws Exception {
        System.out.println("\n📷 Checking progress_photo table...");
        
        ResultSet tables = meta.getTables(null, null, "progress_photo", null);
        
        if (!tables.next()) {
            System.out.println("⚠️  Table progress_photo doesn't exist. Creating...");
            
            String sql = "CREATE TABLE progress_photo (" +
                         "id INT AUTO_INCREMENT PRIMARY KEY, " +
                         "user_id INT NOT NULL, " +
                         "filename VARCHAR(255) NOT NULL, " +
                         "caption TEXT, " +
                         "weight DECIMAL(5,2), " +
                         "taken_at TIMESTAMP NULL, " +
                         "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                         "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE, " +
                         "INDEX idx_user_taken (user_id, taken_at)" +
                         ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("✅ Table progress_photo created");
        } else {
            System.out.println("✅ Table progress_photo OK");
        }
    }
    
    private static void createMessageTable(Connection conn, DatabaseMetaData meta) throws Exception {
        System.out.println("\n💬 Checking message table...");
        
        ResultSet tables = meta.getTables(null, null, "message", null);
        
        if (!tables.next()) {
            System.out.println("⚠️  Table message doesn't exist. Creating...");
            
            String sql = "CREATE TABLE message (" +
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
                         ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            System.out.println("✅ Table message created");
        } else {
            System.out.println("✅ Table message OK");
        }
    }
}
