package tn.esprit.projet.repository.faceid;

import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;

/**
 * Face ID Attempt Repository
 * Logs authentication attempts for security and rate limiting
 */
public class FaceIDAttemptRepository {

    private Connection getConnection() {
        return MyBDConnexion.getInstance().getConnexion();
    }

    /**
     * Log a Face ID attempt
     */
    public void logAttempt(Integer userId, String attemptType, boolean success, 
                          Double similarityScore, boolean livenessVerified, 
                          String failureReason, String ipAddress) {
        
        String sql = "INSERT INTO face_id_attempts " +
                    "(user_id, attempt_type, success, similarity_score, liveness_verified, " +
                    "failure_reason, ip_address, attempted_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            if (userId != null) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            
            ps.setString(2, attemptType);
            ps.setBoolean(3, success);
            
            if (similarityScore != null) {
                ps.setDouble(4, similarityScore);
            } else {
                ps.setNull(4, Types.DOUBLE);
            }
            
            ps.setBoolean(5, livenessVerified);
            ps.setString(6, failureReason);
            ps.setString(7, ipAddress != null ? ipAddress : "127.0.0.1");
            
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[FaceIDAttempt] Log error: " + e.getMessage());
        }
    }

    /**
     * Check if IP is rate limited (too many failed attempts)
     */
    public boolean isRateLimited(String ipAddress) {
        String sql = "SELECT COUNT(*) FROM face_id_attempts " +
                    "WHERE ip_address = ? " +
                    "AND success = 0 " +
                    "AND attempted_at >= DATE_SUB(NOW(), INTERVAL 15 MINUTE)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, ipAddress);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int failedAttempts = rs.getInt(1);
                return failedAttempts >= 5; // Max 5 failed attempts in 15 minutes
            }

        } catch (SQLException e) {
            System.err.println("[FaceIDAttempt] Rate limit check error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get recent failed attempts for a user
     */
    public int getRecentFailedAttempts(int userId, int minutes) {
        String sql = "SELECT COUNT(*) FROM face_id_attempts " +
                    "WHERE user_id = ? " +
                    "AND success = 0 " +
                    "AND attempted_at >= DATE_SUB(NOW(), INTERVAL ? MINUTE)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, minutes);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("[FaceIDAttempt] Failed attempts check error: " + e.getMessage());
        }

        return 0;
    }
}
