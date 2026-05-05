package tn.esprit.projet.test;

import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.MyBDConnexion;
import tn.esprit.projet.utils.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Diagnostic: checks admin accounts and fixes them if needed.
 * Run once to diagnose + repair.
 */
public class DiagAdmin {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  Admin Account Diagnostic & Fix Tool  ");
        System.out.println("═══════════════════════════════════════");

        Connection cnx = MyBDConnexion.getInstance().getCnx();
        UserDAO dao = new UserDAO();

        // 1. Show all users in DB
        System.out.println("\n📋 All users in database:");
        try (PreparedStatement ps = cnx.prepareStatement("SELECT id, email, roles, is_active, password FROM user ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            while (rs.next()) {
                found = true;
                String email    = rs.getString("email");
                String roles    = rs.getString("roles");
                boolean active  = rs.getBoolean("is_active");
                String pwdHash  = rs.getString("password");
                String pwdType  = pwdHash != null && pwdHash.startsWith("$2") ? "BCrypt" : "PLAIN/OTHER";
                System.out.printf("  id=%-3d  email=%-30s  roles=%-15s  active=%-5b  pwd=%s%n",
                        rs.getInt("id"), email, roles, active, pwdType);
            }
            if (!found) System.out.println("  ⚠️  No users found in database!");
        } catch (Exception e) {
            System.err.println("  ❌ Error reading users: " + e.getMessage());
        }

        // 2. Test password verification for known accounts
        System.out.println("\n🔑 Password verification test:");
        String[][] tests = {
            {"admin@nutrilife.com", "Admin@1234"},
            {"salim@gmail.com",     "Salim@1234"},
            {"user@nutrilife.com",  "User@1234"}
        };
        for (String[] t : tests) {
            User u = dao.findByEmail(t[0]);
            if (u == null) {
                System.out.printf("  %-30s → NOT FOUND%n", t[0]);
            } else {
                boolean ok = PasswordUtil.checkPassword(t[1], u.getPassword());
                System.out.printf("  %-30s → pwd check: %-5b  role: %s  active: %b%n",
                        t[0], ok, u.getRoles(), u.isActive());
            }
        }

        // 3. Fix: ensure admin@nutrilife.com exists with correct password
        System.out.println("\n🔧 Fixing admin account...");
        User admin = dao.findByEmail("admin@nutrilife.com");
        if (admin == null) {
            // Create fresh admin
            admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("NutriLife");
            admin.setEmail("admin@nutrilife.com");
            admin.setPassword(PasswordUtil.hashPassword("Admin@1234"));
            admin.setRoles("ROLE_ADMIN");
            admin.setActive(true);
            admin.setBirthday(java.time.LocalDate.of(1990, 1, 1));
            admin.setWeight(70.0);
            admin.setHeight(175.0);
            boolean created = dao.create(admin);
            System.out.println(created
                ? "  ✅ Admin created: admin@nutrilife.com / Admin@1234"
                : "  ❌ Failed to create admin: " + dao.getLastError());
        } else {
            // Reset password and ensure role is correct
            try (PreparedStatement ps = cnx.prepareStatement(
                    "UPDATE user SET password=?, roles='ROLE_ADMIN', is_active=1 WHERE email=?")) {
                ps.setString(1, PasswordUtil.hashPassword("Admin@1234"));
                ps.setString(2, "admin@nutrilife.com");
                int rows = ps.executeUpdate();
                System.out.println("  ✅ Admin password reset. Rows updated: " + rows);
                System.out.println("  📧 Email: admin@nutrilife.com");
                System.out.println("  🔑 Password: Admin@1234");
            } catch (Exception e) {
                System.err.println("  ❌ Error: " + e.getMessage());
            }
        }

        // 4. Final verification
        System.out.println("\n✅ Final check:");
        User finalAdmin = dao.findByEmail("admin@nutrilife.com");
        if (finalAdmin != null) {
            boolean pwdOk = PasswordUtil.checkPassword("Admin@1234", finalAdmin.getPassword());
            System.out.println("  Email:    admin@nutrilife.com");
            System.out.println("  Password: Admin@1234  →  check=" + pwdOk);
            System.out.println("  Role:     " + finalAdmin.getRoles());
            System.out.println("  Active:   " + finalAdmin.isActive());
            System.out.println("  isAdmin(): " + finalAdmin.isAdmin());
        }

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("  Done. Use: admin@nutrilife.com / Admin@1234");
        System.out.println("═══════════════════════════════════════");
    }
}
