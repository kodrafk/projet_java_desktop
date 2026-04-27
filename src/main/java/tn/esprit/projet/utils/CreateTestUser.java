package tn.esprit.projet.utils;

import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;

import java.time.LocalDate;

/**
 * Utility to create a test USER account
 * Run this class to create the test@user.com account
 */
public class CreateTestUser {

    public static void main(String[] args) {
        System.out.println("============================================================================");
        System.out.println("Creating a test USER account");
        System.out.println("============================================================================");
        System.out.println();

        UserRepository repo = new UserRepository();

        // Check if user already exists
        User existing = repo.findByEmail("test@user.com");
        if (existing != null) {
            System.out.println("⚠️  User test@user.com already exists (ID: " + existing.getId() + ")");
            System.out.println("Deleting old account...");
            repo.delete(existing.getId());
            System.out.println("✅ Old account deleted");
            System.out.println();
        }

        // Create new account
        User user = new User();
        user.setEmail("test@user.com");
        user.setPassword("user123");  // Plain text password
        user.setRole("ROLE_USER");
        user.setActive(true);
        user.setFirstName("Marie");
        user.setLastName("Dupont");
        user.setBirthday(LocalDate.of(1998, 3, 20));
        user.setWeight(65.0);
        user.setHeight(168.0);

        try {
            repo.save(user);
            System.out.println("✅ USER ACCOUNT CREATED SUCCESSFULLY!");
            System.out.println();
            System.out.println("============================================================================");
            System.out.println("ACCOUNT INFORMATION");
            System.out.println("============================================================================");
            System.out.println("📧 Email    : test@user.com");
            System.out.println("🔑 Password : user123");
            System.out.println("👤 Role     : ROLE_USER");
            System.out.println("✅ Status   : Active");
            System.out.println("👤 Name     : Marie Dupont");
            System.out.println("🎂 Born     : 20/03/1998");
            System.out.println("⚖️  Weight   : 65.0 kg");
            System.out.println("📏 Height   : 168.0 cm");
            System.out.println("🆔 ID       : " + user.getId());
            System.out.println("============================================================================");
            System.out.println();
            System.out.println("You can now login with this account!");
            System.out.println("============================================================================");
        } catch (Exception e) {
            System.err.println("❌ ERROR creating account:");
            e.printStackTrace();
        }
    }
}
