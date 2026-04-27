package tn.esprit.projet.utils;

import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;

import java.time.LocalDate;

/**
 * Seeds the database with default accounts if none exists.
 * Admin credentials: admin@nutrilife.com / Admin@1234
 */
public class DataSeeder {

    public static void seed() {
        UserDAO dao = new UserDAO();

        // Default admin
        if (dao.countByRole("ROLE_ADMIN") == 0) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("NutriLife");
            admin.setEmail("admin@nutrilife.com");
            admin.setPassword(PasswordUtil.hashPassword("Admin@1234"));
            admin.setRoles("ROLE_ADMIN");
            admin.setActive(true);
            admin.setBirthday(LocalDate.of(1990, 1, 1));
            admin.setWeight(70f);
            admin.setHeight(175f);
            if (dao.create(admin))
                System.out.println("✅ Admin created: admin@nutrilife.com / Admin@1234");
        }

        // Salim admin account
        if (dao.findByEmail("salim@gmail.com") == null) {
            User salim = new User();
            salim.setFirstName("Salim");
            salim.setLastName("Admin");
            salim.setEmail("salim@gmail.com");
            salim.setPassword(PasswordUtil.hashPassword("Salim@1234"));
            salim.setRoles("ROLE_ADMIN");
            salim.setActive(true);
            salim.setBirthday(LocalDate.of(1995, 1, 1));
            salim.setWeight(70f);
            salim.setHeight(175f);
            if (dao.create(salim))
                System.out.println("✅ Admin created: salim@gmail.com / Salim@1234");
        }

        // Default regular user
        if (dao.findByEmail("user@nutrilife.com") == null) {
            User user = new User();
            user.setFirstName("Demo");
            user.setLastName("User");
            user.setEmail("user@nutrilife.com");
            user.setPassword(PasswordUtil.hashPassword("User@1234"));
            user.setRoles("ROLE_USER");
            user.setActive(true);
            user.setBirthday(LocalDate.of(1995, 6, 15));
            user.setWeight(70f);
            user.setHeight(175f);
            if (dao.create(user))
                System.out.println("✅ User created: user@nutrilife.com / User@1234");
        }
    }
}
