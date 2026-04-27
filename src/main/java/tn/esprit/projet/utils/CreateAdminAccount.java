package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Utility to create the admin account in the database.
 * Run this class directly to create the account.
 */
public class CreateAdminAccount {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  ADMIN ACCOUNT CREATION");
        System.out.println("========================================");
        System.out.println();
        
        try {
            MyBDConnexion.getInstance();
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            String deleteSQL = "DELETE FROM user WHERE email = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL);
            deleteStmt.setString(1, "admin@nutrilife.com");
            deleteStmt.executeUpdate();
            System.out.println("[1/3] Old account deleted (if existed)");
            
            // Create admin account
            String insertSQL = "INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height, created_at) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
            
            PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
            insertStmt.setString(1, "admin@nutrilife.com");
            insertStmt.setString(2, "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // Admin@1234
            insertStmt.setString(3, "ROLE_ADMIN");
            insertStmt.setInt(4, 1);
            insertStmt.setString(5, "Admin");
            insertStmt.setString(6, "NutriLife");
            insertStmt.setString(7, "1990-01-01");
            insertStmt.setDouble(8, 75.0);
            insertStmt.setDouble(9, 175.0);
            
            insertStmt.executeUpdate();
            System.out.println("[2/3] Admin account created");
            
            String selectSQL = "SELECT id, email, roles, first_name, last_name FROM user WHERE email = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
            selectStmt.setString(1, "admin@nutrilife.com");
            ResultSet rs = selectStmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("[3/3] Verification OK");
                System.out.println();
                System.out.println("========================================");
                System.out.println("  ADMIN ACCOUNT CREATED SUCCESSFULLY!");
                System.out.println("========================================");
                System.out.println();
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Password: Admin@1234");
                System.out.println("Role: " + rs.getString("roles"));
                System.out.println("Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
                System.out.println();
                System.out.println("You can now log in!");
            } else {
                System.err.println("❌ Error: Account was not created");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
