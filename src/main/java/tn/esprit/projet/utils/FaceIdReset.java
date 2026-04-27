package tn.esprit.projet.utils;

import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Reset Face ID system - deletes all enrollments
 */
public class FaceIdReset {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              FACE ID SYSTEM RESET                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            System.out.println("🗑️  Deleting all Face ID enrollments...");
            
            Statement stmt = conn.createStatement();
            int deleted = stmt.executeUpdate("DELETE FROM face_embeddings");
            
            System.out.println("✅ Deleted " + deleted + " Face ID enrollment(s)");
            System.out.println();
            System.out.println("✅ Face ID system has been reset!");
            System.out.println();
            System.out.println("All users can now enroll their Face ID again.");
            
        } catch (Exception e) {
            System.err.println("❌ Error resetting Face ID system: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
