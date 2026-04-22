package tn.esprit.projet.utils;

import javafx.scene.control.Control;
import javafx.scene.control.Label;

import java.time.LocalDate;
import java.time.Period;

/**
 * Field-level validation helpers.
 * Each method returns null if valid, or an error message string if invalid.
 */
public class Validator {

    private static final String ERR_STYLE =
            "-fx-background-color:#FFF5F5;-fx-border-color:#EF4444;-fx-border-radius:8;" +
            "-fx-background-radius:8;-fx-padding:8 12;-fx-font-size:13px;";
    private static final String OK_STYLE =
            "-fx-background-color:#F0F7F0;-fx-border-color:#C8E6C9;-fx-border-radius:8;" +
            "-fx-background-radius:8;-fx-padding:8 12;-fx-font-size:13px;";
    private static final String NEUTRAL_STYLE =
            "-fx-background-color:#F8FAFC;-fx-border-color:#E2E8F0;-fx-border-radius:8;" +
            "-fx-background-radius:8;-fx-padding:8 12;-fx-font-size:13px;";

    public static String getErrorStyle()   { return ERR_STYLE; }
    public static String getOkStyle()      { return OK_STYLE; }
    public static String getNeutralStyle() { return NEUTRAL_STYLE; }

    /** Apply validation result to a field + error label. Returns true if valid. */
    public static boolean apply(Control field, Label errLabel, String error) {
        if (error != null) {
            if (errLabel != null) errLabel.setText(error);
            field.setStyle(ERR_STYLE);
            return false;
        }
        if (errLabel != null) errLabel.setText("");
        field.setStyle(OK_STYLE);
        return true;
    }

    public static String email(String v) {
        if (v == null || v.isBlank()) return "Email is required.";
        if (v.length() > 180) return "Email must not exceed 180 characters.";
        if (!v.matches("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)*\\.[a-zA-Z]{2,}$"))
            return "Please enter a valid email address.";
        return null;
    }

    public static String password(String v) {
        if (v == null || v.isBlank()) return "Password is required.";
        if (v.length() < 8) return "Password must be at least 8 characters.";
        return null;
    }

    public static String passwordOptional(String v) {
        if (v == null || v.isBlank()) return null;
        return password(v);
    }

    public static String confirmPassword(String pwd, String confirm) {
        if (confirm == null || confirm.isBlank()) return "Please confirm your password.";
        if (!confirm.equals(pwd)) return "Passwords do not match.";
        return null;
    }

    public static String name(String v, String label) {
        if (v == null || v.isBlank()) return label + " is required.";
        String t = v.trim();
        if (t.length() < 2) return label + " must be at least 2 characters.";
        if (t.length() > 100) return label + " must not exceed 100 characters.";
        if (!t.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$"))
            return label + " can only contain letters, spaces, and hyphens.";
        return null;
    }

    public static String birthday(LocalDate v) {
        if (v == null) return "Birthday is required.";
        if (v.isAfter(LocalDate.now())) return "Birthday must be in the past.";
        if (Period.between(v, LocalDate.now()).getYears() < 18)
            return "You must be at least 18 years old to register.";
        return null;
    }

    public static String weight(String v) {
        if (v == null || v.isBlank()) return "Weight is required.";
        try {
            double w = Double.parseDouble(v.trim());
            if (w <= 0) return "Weight must be a positive number.";
            if (w < 20 || w > 500) return "Weight must be between 20kg and 500kg.";
        } catch (NumberFormatException e) { return "Weight must be a positive number."; }
        return null;
    }

    public static String height(String v) {
        if (v == null || v.isBlank()) return "Height is required.";
        try {
            double h = Double.parseDouble(v.trim());
            if (h <= 0) return "Height must be a positive number.";
            if (h < 50 || h > 300) return "Height must be between 50cm and 300cm.";
        } catch (NumberFormatException e) { return "Height must be a positive number."; }
        return null;
    }
}
