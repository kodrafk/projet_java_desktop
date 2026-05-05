package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Face ID Database Migration
 * Creates required tables for Face ID system
 */
public class FaceIDDatabaseMigration {

    public static void main(String[] args) {
        System.out.println("=== Face ID Database Migration ===");
        
        try {
            Connection conn = tn.esprit.projet.utils.MyBDConnexion.getInstance().getCnx();
            Statement stmt = conn.createStatement();

            // Create face_embeddings table
            System.out.println("Creating face_embeddings table...");
            String createEmbeddings = 
                "CREATE TABLE IF NOT EXISTS face_embeddings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER UNIQUE NOT NULL, " +
                "embedding_encrypted TEXT NOT NULL, " +
                "encryption_iv VARCHAR(255) NOT NULL, " +
                "encryption_tag VARCHAR(255) NOT NULL, " +
                "liveness_verified BOOLEAN DEFAULT 0, " +
                "enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "last_verified_at DATETIME, " +
                "is_active BOOLEAN DEFAULT 1" +
                ")";
            stmt.execute(createEmbeddings);
            System.out.println("✓ face_embeddings table created");

            // Create indexes
            System.out.println("Creating indexes...");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_id ON face_embeddings(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_active ON face_embeddings(is_active)");
            System.out.println("✓ Indexes created");

            // Create face_auth_attempts table
            System.out.println("Creating face_auth_attempts table...");
            String createAttempts = 
                "CREATE TABLE IF NOT EXISTS face_auth_attempts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "attempt_type VARCHAR(10) NOT NULL, " +
                "success BOOLEAN NOT NULL, " +
                "similarity_score REAL, " +
                "liveness_passed BOOLEAN, " +
                "failure_reason VARCHAR(255), " +
                "ip_address VARCHAR(45), " +
                "attempted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.execute(createAttempts);
            System.out.println("✓ face_auth_attempts table created");

            // Create indexes for attempts
            System.out.println("Creating attempt indexes...");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_user_attempts ON face_auth_attempts(user_id, attempted_at)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_attempt_type ON face_auth_attempts(attempt_type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_success ON face_auth_attempts(success)");
            System.out.println("✓ Attempt indexes created");

            stmt.close();

            System.out.println("\n=== Migration Complete ===");
            System.out.println("Face ID tables are ready!");
            System.out.println("\nNext steps:");
            System.out.println("1. Test enrollment: FaceIDLauncher.launchEnrollment(userId, ...)");
            System.out.println("2. Test authentication: FaceIDLauncher.launchAuthentication(...)");

        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
