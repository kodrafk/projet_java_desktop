package tn.esprit.projet.utils;

import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;

import java.time.LocalDate;

public class DataSeeder {

    public static void seed() {
        UserRepository repo = new UserRepository();

        // Seed default admin
        try {
            if (repo.countAdmins() == 0) {
                User admin = new User();
                admin.setFirstName("Admin");
                admin.setLastName("NutriLife");
                admin.setEmail("admin@nutrilife.com");
                admin.setPassword(PasswordUtil.hashPassword("Admin@1234"));
                admin.setRole("ROLE_ADMIN");
                admin.setActive(true);
                admin.setBirthday(LocalDate.of(1990, 1, 1));
                admin.setWeight(70);
                admin.setHeight(175);
                repo.save(admin);
                System.out.println("[Seeder] Admin created: admin@nutrilife.com / Admin@1234");
            }
        } catch (Exception e) {
            System.err.println("[Seeder] Admin seed failed: " + e.getMessage());
        }

        // Seed default user
        try {
            if (repo.findByEmail("user@nutrilife.com") == null) {
                User user = new User();
                user.setFirstName("Demo");
                user.setLastName("User");
                user.setEmail("user@nutrilife.com");
                user.setPassword(PasswordUtil.hashPassword("User@1234"));
                user.setRole("ROLE_USER");
                user.setActive(true);
                user.setBirthday(LocalDate.of(1995, 6, 15));
                user.setWeight(70);
                user.setHeight(175);
                repo.save(user);
                System.out.println("[Seeder] User created: user@nutrilife.com / User@1234");
            }
        } catch (Exception e) {
            System.err.println("[Seeder] User seed failed: " + e.getMessage());
        }

        // Badge seeding is done lazily when the badges screen opens
    }
}
