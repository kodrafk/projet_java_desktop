package tn.esprit.projet.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for BCrypt password hashing and verification.
 * Passwords are NEVER stored in plain text.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /** Hash a plain-text password using BCrypt. */
    public static String hashPassword(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(12));
    }

    /** Verify a plain-text password against a BCrypt hash. */
    public static boolean checkPassword(String plain, String hashed) {
        if (plain == null || hashed == null) return false;
        try {
            return BCrypt.checkpw(plain, hashed);
        } catch (Exception e) {
            return false;
        }
    }
}
