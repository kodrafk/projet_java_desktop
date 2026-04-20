package tn.esprit.projet.utils;

import java.time.LocalDate;
import java.time.Period;

/**
 * Static validation methods for User fields.
 * Each method returns null if valid, or a specific error message string if invalid.
 */
public class UserValidator {

    private static final String FIELD_STYLE_ERROR =
            "-fx-background-color: #FFF5F5; -fx-border-color: #EF4444; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10; -fx-font-size: 13px;";

    private static final String FIELD_STYLE_OK =
            "-fx-background-color: #F0F7F0; -fx-border-color: #C8E6C9; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10; -fx-font-size: 13px;";

    private static final String FIELD_STYLE_NEUTRAL =
            "-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; " +
            "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10; -fx-font-size: 13px;";

    public static String getErrorStyle()   { return FIELD_STYLE_ERROR;   }
    public static String getOkStyle()      { return FIELD_STYLE_OK;      }
    public static String getNeutralStyle() { return FIELD_STYLE_NEUTRAL; }

    // ── Email ──────────────────────────────────────────────────────────────────
    public static String validateEmail(String email) {
        if (email == null || email.isBlank())
            return "Email is required.";
        if (email.length() > 180)
            return "Email must not exceed 180 characters.";
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}$"))
            return "Invalid email format (must contain @ and a valid domain).";
        return null;
    }

    // ── Password (new / register) ──────────────────────────────────────────────
    public static String validatePassword(String password) {
        if (password == null || password.isBlank())
            return "Password is required.";
        if (password.length() < 8)
            return "Password must be at least 8 characters.";
        if (!password.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter.";
        if (!password.matches(".*[a-z].*"))
            return "Password must contain at least one lowercase letter.";
        if (!password.matches(".*\\d.*"))
            return "Password must contain at least one digit.";
        if (!password.matches(".*[@$!%*?&].*"))
            return "Password must contain at least one special character (@$!%*?&).";
        return null;
    }

    // ── Password optional (edit — blank = keep current) ───────────────────────
    public static String validatePasswordOptional(String password) {
        if (password == null || password.isBlank()) return null; // blank = keep current
        return validatePassword(password);
    }

    // ── Confirm password ───────────────────────────────────────────────────────
    public static String validateConfirmPassword(String password, String confirm) {
        if (confirm == null || confirm.isBlank())
            return "Please confirm your password.";
        if (!confirm.equals(password))
            return "Passwords do not match.";
        return null;
    }

    // ── First / Last name ──────────────────────────────────────────────────────
    public static String validateName(String name, String fieldLabel) {
        if (name == null || name.isBlank())
            return fieldLabel + " is required.";
        String trimmed = name.trim();
        if (trimmed.length() < 2)
            return fieldLabel + " must be at least 2 characters.";
        if (trimmed.length() > 100)
            return fieldLabel + " must not exceed 100 characters.";
        if (!trimmed.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$"))
            return fieldLabel + " must contain only letters, spaces, or hyphens.";
        return null;
    }

    // ── Birthday ───────────────────────────────────────────────────────────────
    public static String validateBirthday(LocalDate birthday) {
        if (birthday == null)
            return "Birthday is required.";
        if (birthday.isAfter(LocalDate.now()))
            return "Birthday must be in the past.";
        int age = Period.between(birthday, LocalDate.now()).getYears();
        if (age < 18)
            return "You must be at least 18 years old.";
        if (age > 120)
            return "Birthday cannot be more than 120 years in the past.";
        return null;
    }

    // ── Weight ─────────────────────────────────────────────────────────────────
    public static String validateWeight(String weightStr) {
        if (weightStr == null || weightStr.isBlank())
            return "Weight is required.";
        try {
            float w = Float.parseFloat(weightStr.trim());
            if (w < 20 || w > 500)
                return "Weight must be between 20 and 500 kg.";
        } catch (NumberFormatException e) {
            return "Weight must be a valid number.";
        }
        return null;
    }

    // ── Height ─────────────────────────────────────────────────────────────────
    public static String validateHeight(String heightStr) {
        if (heightStr == null || heightStr.isBlank())
            return "Height is required.";
        try {
            float h = Float.parseFloat(heightStr.trim());
            if (h < 50 || h > 300)
                return "Height must be between 50 and 300 cm.";
        } catch (NumberFormatException e) {
            return "Height must be a valid number.";
        }
        return null;
    }

    // ── Phone (optional) ───────────────────────────────────────────────────────
    public static String validatePhone(String phone) {
        if (phone == null || phone.isBlank()) return null; // optional
        if (!phone.trim().matches("^\\+[0-9]{7,15}$"))
            return "Phone must start with + followed by 7–15 digits.";
        return null;
    }

    // ── Role ───────────────────────────────────────────────────────────────────
    public static String validateRole(String role) {
        if (role == null || role.isBlank())
            return "Role is required.";
        if (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN"))
            return "Role must be ROLE_USER or ROLE_ADMIN.";
        return null;
    }
}
