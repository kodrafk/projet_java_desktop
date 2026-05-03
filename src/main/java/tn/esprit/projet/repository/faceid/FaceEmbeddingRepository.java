package tn.esprit.projet.repository.faceid;

import tn.esprit.projet.utils.MyBDConnexion;
import tn.esprit.projet.utils.faceid.FaceIDEncryption;

import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Face Embedding Repository
 * Handles encrypted face embedding storage and retrieval
 */
public class FaceEmbeddingRepository {

    private Connection getConnection() {
        return MyBDConnexion.getInstance().getConnexion();
    }

    /**
     * Save encrypted face embedding for a user
     */
    public boolean saveEmbedding(int userId, double[] embedding, boolean livenessVerified) {
        try {
            // Encrypt embedding
            String[] encrypted = FaceIDEncryption.encryptEmbedding(embedding);
            if (encrypted == null || encrypted.length != 3) {
                System.err.println("[FaceEmbedding] Encryption failed");
                return false;
            }

            String sql = "INSERT INTO face_embeddings " +
                        "(user_id, embedding_encrypted, encryption_iv, encryption_tag, " +
                        "is_active, liveness_verified, consent_given_at, consent_ip, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, 1, ?, NOW(), '127.0.0.1', NOW(), NOW()) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "embedding_encrypted = VALUES(embedding_encrypted), " +
                        "encryption_iv = VALUES(encryption_iv), " +
                        "encryption_tag = VALUES(encryption_tag), " +
                        "liveness_verified = VALUES(liveness_verified), " +
                        "is_active = 1, " +
                        "updated_at = NOW()";

            try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, encrypted[0]); // encrypted data
                ps.setString(3, encrypted[1]); // IV
                ps.setString(4, encrypted[2]); // tag
                ps.setBoolean(5, livenessVerified);
                
                int rows = ps.executeUpdate();
                System.out.println("[FaceEmbedding] Saved embedding for user " + userId);
                return rows > 0;
            }

        } catch (Exception e) {
            System.err.println("[FaceEmbedding] Save error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all active user embeddings
     */
    public List<UserEmbedding> findAllActiveEmbeddings() {
        List<UserEmbedding> result = new ArrayList<>();

        String sql = "SELECT user_id, embedding_encrypted, encryption_iv, encryption_tag " +
                    "FROM face_embeddings WHERE is_active = 1";

        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UserEmbedding ue = new UserEmbedding();
                ue.userId = rs.getInt("user_id");
                ue.encryptedB64 = rs.getString("embedding_encrypted");
                ue.ivB64 = rs.getString("encryption_iv");
                ue.tagB64 = rs.getString("encryption_tag");
                result.add(ue);
            }

            System.out.println("[FaceEmbedding] Found " + result.size() + " active embeddings");

        } catch (SQLException e) {
            System.err.println("[FaceEmbedding] Query error: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Decrypt an embedding
     */
    public double[] decryptEmbedding(String encryptedB64, String ivB64, String tagB64) {
        try {
            return FaceIDEncryption.decryptEmbedding(encryptedB64, ivB64, tagB64);
        } catch (Exception e) {
            System.err.println("[FaceEmbedding] Decryption error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if user has an embedding
     */
    public boolean hasEmbedding(int userId) {
        String sql = "SELECT COUNT(*) FROM face_embeddings WHERE user_id = ? AND is_active = 1";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("[FaceEmbedding] Check error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Delete user's embedding
     */
    public boolean deleteEmbedding(int userId) {
        String sql = "DELETE FROM face_embeddings WHERE user_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            
            System.out.println("[FaceEmbedding] Deleted embedding for user " + userId);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[FaceEmbedding] Delete error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update last verified timestamp
     */
    public void updateLastVerified(int userId) {
        String sql = "UPDATE face_embeddings SET last_used_at = NOW() WHERE user_id = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[FaceEmbedding] Update error: " + e.getMessage());
        }
    }

    /**
     * Inner class for user embedding data
     */
    public static class UserEmbedding {
        public int userId;
        public String encryptedB64;
        public String ivB64;
        public String tagB64;
    }
}
