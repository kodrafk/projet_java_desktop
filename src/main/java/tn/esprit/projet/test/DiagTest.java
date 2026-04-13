package tn.esprit.projet.test;

import tn.esprit.projet.utils.MyBDConnexion;
import java.sql.*;

public class DiagTest {
    public static void main(String[] args) throws Exception {
        Connection cnx = MyBDConnexion.getInstance().getCnx();

        // Show table structure
        System.out.println("\n=== TABLE STRUCTURE ===");
        ResultSet rs = cnx.createStatement().executeQuery(
            "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT " +
            "FROM information_schema.COLUMNS " +
            "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' ORDER BY ORDINAL_POSITION");
        while (rs.next()) {
            System.out.printf("%-20s %-15s %-10s %s%n",
                rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
        }

        // Try a test insert
        System.out.println("\n=== TEST INSERT ===");
        try {
            PreparedStatement ps = cnx.prepareStatement(
                "INSERT INTO user (email,password,roles,is_active,created_at," +
                "first_name,last_name,birthday,weight,height,phone_number,phone_verified," +
                "photo_filename,welcome_message) VALUES (?,?,?,?,NOW(),?,?,?,?,?,?,?,?,?)");
            ps.setString(1, "diagtest@test.com");
            ps.setString(2, "$2a$10$test");
            ps.setString(3, "ROLE_USER");
            ps.setBoolean(4, true);
            ps.setString(5, "Test");
            ps.setString(6, "User");
            ps.setDate(7, Date.valueOf("1990-01-01"));
            ps.setFloat(8, 70f);
            ps.setFloat(9, 175f);
            ps.setString(10, null);
            ps.setBoolean(11, false);
            ps.setString(12, null);
            ps.setString(13, null);
            ps.executeUpdate();
            System.out.println("INSERT OK");
            cnx.createStatement().executeUpdate("DELETE FROM user WHERE email='diagtest@test.com'");
        } catch (SQLException e) {
            System.out.println("INSERT FAILED: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
        }
    }
}
