package tn.esprit.projet.utils;

import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;

public class CheckAdminAccount {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  ADMIN ACCOUNT VERIFICATION");
        System.out.println("========================================");
        System.out.println();
        
        try {
            UserRepository repo = new UserRepository();
            User admin = repo.findByEmail("admin@nutrilife.com");
            
            if (admin == null) {
                System.err.println("❌ No account found with email: admin@nutrilife.com");
                return;
            }
            
            System.out.println("✅ Account found:");
            System.out.println("ID: " + admin.getId());
            System.out.println("Email: " + admin.getEmail());
            System.out.println("Name: " + admin.getFirstName() + " " + admin.getLastName());
            System.out.println("Roles: " + admin.getRoles());
            System.out.println("Active: " + admin.isActive());
            System.out.println("Password hash: " + admin.getPassword());
            System.out.println();
            
            String testPassword = "Admin@1234";
            System.out.println("Testing password: " + testPassword);
            
            boolean valid = false;
            try {
                valid = BCrypt.checkpw(testPassword, admin.getPassword());
                System.out.println("BCrypt check: " + (valid ? "✅ VALID" : "❌ INVALID"));
            } catch (Exception e) {
                System.out.println("BCrypt check: ❌ ERROR - " + e.getMessage());
                valid = testPassword.equals(admin.getPassword());
                System.out.println("Plain text check: " + (valid ? "✅ VALID" : "❌ INVALID"));
            }
            
            if (!valid) {
                System.out.println();
                System.out.println("⚠️ Password does not match!");
                System.out.println("Generating new hash...");
                String newHash = BCrypt.hashpw(testPassword, BCrypt.gensalt(10));
                System.out.println("New hash: " + newHash);
                System.out.println();
                System.out.println("Updating account...");
                repo.updatePassword(admin.getId(), newHash);
                System.out.println("✅ Password updated!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
