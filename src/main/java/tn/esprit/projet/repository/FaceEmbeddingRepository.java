package tn.esprit.projet.repository;

import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class FaceEmbeddingRepository {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Save or update embedding ───────────────────────────────────────────────

    public void saveEmbedding(int userId, String encryptedB64, String ivB64, String tagB64) {
        String sql = "INSERT INTO face_embeddings " +
                "(user_id, embedding_encrypted, encryption_iv, encryption_tag, " +
                " is_active, consent_given_at, consent_ip, created_at, updated_at) " +
                "VALUES (?,?,?,?,1,NOW(),'127.0.0.1',NOW(),NOW()) " +
                "ON DUPLICATE KEY UPDATE " +
                "embedding_encrypted=VALUES(embedding_encrypted)," +
                "encryption_iv=VALUES(encryption_iv)," +
                "encryption_tag=VALUES(encryption_tag)," +
                "is_active=1, updated_at=NOW()";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, encryptedB64);
            ps.setString(3, ivB64);
            ps.setString(4, tagB64);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Find by user ───────────────────────────────────────────────────────────

    public String[] findByUserId(int userId) {
        // Returns [encryptedB64, ivB64, tagB64] or null
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT embedding_encrypted, encryption_iv, encryption_tag " +
                "FROM face_embeddings WHERE user_id=? AND is_active=1")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("embedding_encrypted"),
                    rs.getString("encryption_iv"),
                    rs.getString("encryption_tag")
                };
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Get all active embeddings (for login matching) ─────────────────────────

    public java.util.List<int[]> findAllActiveUserIds() {
        java.util.List<int[]> list = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT user_id FROM face_embeddings WHERE is_active=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new int[]{rs.getInt("user_id")});
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Remove embedding ───────────────────────────────────────────────────────

    public void removeByUserId(int userId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM face_embeddings WHERE user_id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Update last_used_at ────────────────────────────────────────────────────

    public void updateLastUsed(int userId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE face_embeddings SET last_used_at=NOW() WHERE user_id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Log verification attempt ───────────────────────────────────────────────

    public void logAttempt(Integer userId, String email, boolean success, Double similarityScore) {
        String sql = "INSERT INTO face_verification_attempts " +
                "(user_id, email, ip_address, success, similarity_score, attempted_at) " +
                "VALUES (?,?,?,?,?,NOW())";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, email);
            ps.setString(3, "127.0.0.1");
            ps.setBoolean(4, success);
            if (similarityScore != null) ps.setDouble(5, similarityScore); else ps.setNull(5, Types.DOUBLE);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── Count recent failed attempts (rate limiting) ───────────────────────────

    public int countRecentFailures(String email, int minutes) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM face_verification_attempts " +
                "WHERE email=? AND success=0 AND attempted_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE)")) {
            ps.setString(1, email);
            ps.setInt(2, minutes);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
