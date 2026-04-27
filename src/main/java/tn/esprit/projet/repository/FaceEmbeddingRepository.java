package tn.esprit.projet.repository;

import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class FaceEmbeddingRepository {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Ensure face_embeddings table exists — called once at startup. */
    public void ensureTableExists() {
        // Create without FOREIGN KEY to avoid issues with table name differences
        String sql = "CREATE TABLE IF NOT EXISTS `face_embeddings` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NOT NULL UNIQUE," +
                "`embedding_encrypted` MEDIUMTEXT NOT NULL," +
                "`encryption_iv` VARCHAR(255) NOT NULL," +
                "`encryption_tag` VARCHAR(255) NOT NULL," +
                "`is_active` TINYINT(1) NOT NULL DEFAULT 1," +
                "`consent_given_at` DATETIME DEFAULT NULL," +
                "`consent_ip` VARCHAR(45) DEFAULT NULL," +
                "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "`last_used_at` DATETIME DEFAULT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement st = conn().createStatement()) {
            st.executeUpdate(sql);
            System.out.println("[FaceDB] ✅ face_embeddings table ready");
        } catch (SQLException e) {
            if (!e.getMessage().contains("already exists")) {
                System.err.println("[FaceDB] ❌ Could not create face_embeddings: " + e.getMessage());
            } else {
                System.out.println("[FaceDB] ✅ face_embeddings already exists");
            }
        }

        String sql2 = "CREATE TABLE IF NOT EXISTS `face_verification_attempts` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT DEFAULT NULL," +
                "`email` VARCHAR(255) DEFAULT NULL," +
                "`ip_address` VARCHAR(45) NOT NULL DEFAULT '127.0.0.1'," +
                "`success` TINYINT(1) NOT NULL DEFAULT 0," +
                "`similarity_score` DOUBLE DEFAULT NULL," +
                "`attempted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement st = conn().createStatement()) {
            st.executeUpdate(sql2);
            System.out.println("[FaceDB] ✅ face_verification_attempts table ready");
        } catch (SQLException e) {
            if (!e.getMessage().contains("already exists")) {
                System.err.println("[FaceDB] ❌ Could not create face_verification_attempts: " + e.getMessage());
            }
        }
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
    
    // ── Get all active embeddings with user info (for duplicate detection) ─────
    
    public java.util.List<UserEmbedding> findAllActiveEmbeddings() {
        java.util.List<UserEmbedding> list = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT user_id, embedding_encrypted, encryption_iv, encryption_tag FROM face_embeddings WHERE is_active=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UserEmbedding ue = new UserEmbedding();
                ue.userId = rs.getInt("user_id");
                ue.encryptedB64 = rs.getString("embedding_encrypted");
                ue.ivB64 = rs.getString("encryption_iv");
                ue.tagB64 = rs.getString("encryption_tag");
                list.add(ue);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    // Inner class for user embedding data
    public static class UserEmbedding {
        public int userId;
        public String encryptedB64;
        public String ivB64;
        public String tagB64;
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
