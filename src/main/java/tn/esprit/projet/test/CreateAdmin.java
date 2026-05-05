package tn.esprit.projet.test;

import tn.esprit.projet.utils.MyBDConnexion;
import tn.esprit.projet.utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Run this once to create a fresh admin account.
 * Email:    admin2@nutrilife.com
 * Password: admin123
 */
public class CreateAdmin {
    public static void main(String[] args) throws Exception {
        String email    = "admin2@nutrilife.com";
        String password = "admin123";
        String hash     = PasswordUtil.hashPassword(password);

        Connection cnx = MyBDConnexion.getInstance().getCnx();

        // Remove old entry if exists
        PreparedStatement del = cnx.prepareStatement("DELETE FROM `user` WHERE email = ?");
        del.setString(1, email);
        del.executeUpdate();

        // Insert fresh
        PreparedStatement ps = cnx.prepareStatement(
            "INSERT INTO `user` (email, password, roles, first_name, last_name, birthday, is_active) " +
            "VALUES (?, ?, 'ROLE_ADMIN', 'Admin', 'NutriLife', '1990-01-01', 1)"
        );
        ps.setString(1, email);
        ps.setString(2, hash);
        ps.executeUpdate();

        System.out.println("✅ Admin created!");
        System.out.println("   Email:    " + email);
        System.out.println("   Password: " + password);
        System.out.println("   Hash:     " + hash);
    }
}
