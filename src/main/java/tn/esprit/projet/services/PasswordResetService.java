package tn.esprit.projet.services;

import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.PasswordUtil;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Handles the 3-step password reset flow:
 * 1. sendCode(email)   → generates 6-digit code, stores in DB (15 min expiry)
 * 2. verifyCode(email, code) → validates code + expiry
 * 3. resetPassword(email, newPassword) → hashes + saves, clears code
 */
public class PasswordResetService {

    private final UserDAO dao = new UserDAO();

    /** Step 1: generate and store a 6-digit verification code. Returns the code (dev mode). */
    public String sendCode(String email) {
        User u = dao.findByEmail(email);
        if (u == null) return null; // don't reveal existence

        String code = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        dao.saveVerificationCode(u.getId(), code, expiresAt);
        return code; // in production: send via email
    }

    /** Step 2: verify the code. Returns the user if valid, null otherwise. */
    public User verifyCode(String email, String code) {
        User u = dao.findByEmail(email);
        if (u == null) return null;
        if (u.getVerificationCode() == null) return null;
        if (!u.getVerificationCode().equals(code)) return null;
        if (u.getVerificationCodeExpiresAt() == null) return null;
        if (LocalDateTime.now().isAfter(u.getVerificationCodeExpiresAt())) return null;
        return u;
    }

    /** Step 3: set new password and clear the code. */
    public boolean resetPassword(int userId, String newPassword) {
        String hashed = PasswordUtil.hashPassword(newPassword);
        return dao.updatePassword(userId, hashed);
    }
}
