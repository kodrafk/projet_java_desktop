package tn.esprit.projet.repository;

import tn.esprit.projet.models.UserAlert;
import tn.esprit.projet.models.UserAlert.AlertType;
import tn.esprit.projet.models.UserAlert.AlertCategory;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pour les alertes utilisateur
 */
public class UserAlertRepository {

    /**
     * Créer la table si elle n'existe pas
     */
    public void ensureTableExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_alerts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                admin_id INT NOT NULL,
                title VARCHAR(200) NOT NULL,
                message TEXT NOT NULL,
                type VARCHAR(20) NOT NULL DEFAULT 'INFO',
                category VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
                is_read BOOLEAN DEFAULT FALSE,
                is_dismissed BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP NULL,
                read_at TIMESTAMP NULL,
                action_url VARCHAR(500) NULL,
                action_label VARCHAR(100) NULL,
                INDEX idx_user_id (user_id),
                INDEX idx_admin_id (admin_id),
                INDEX idx_created_at (created_at)
            )
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[UserAlertRepository] Table 'user_alerts' ready.");
        } catch (SQLException e) {
            System.err.println("[UserAlertRepository] Error creating table: " + e.getMessage());
        }
    }

    /**
     * Sauvegarder une nouvelle alerte
     */
    public UserAlert save(UserAlert alert) {
        String sql = """
            INSERT INTO user_alerts (user_id, admin_id, title, message, type, category, expires_at, action_url, action_label)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, alert.getUserId());
            ps.setInt(2, alert.getAdminId());
            ps.setString(3, alert.getTitle());
            ps.setString(4, alert.getMessage());
            ps.setString(5, alert.getType().name());
            ps.setString(6, alert.getCategory().name());
            ps.setTimestamp(7, alert.getExpiresAt() != null ? Timestamp.valueOf(alert.getExpiresAt()) : null);
            ps.setString(8, alert.getActionUrl());
            ps.setString(9, alert.getActionLabel());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    alert.setId(rs.getInt(1));
                    return alert;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserAlertRepository] Error saving alert: " + e.getMessage());
        }
        return null;
    }

    /**
     * Récupérer toutes les alertes actives pour un utilisateur
     */
    public List<UserAlert> findActiveByUserId(int userId) {
        String sql = """
            SELECT * FROM user_alerts 
            WHERE user_id = ? 
              AND is_dismissed = FALSE 
              AND (expires_at IS NULL OR expires_at > NOW())
            ORDER BY created_at DESC
        """;
        
        List<UserAlert> alerts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserAlertRepository] Error finding alerts: " + e.getMessage());
        }
        return alerts;
    }

    /**
     * Récupérer les alertes non lues pour un utilisateur
     */
    public List<UserAlert> findUnreadByUserId(int userId) {
        String sql = """
            SELECT * FROM user_alerts 
            WHERE user_id = ? 
              AND is_read = FALSE 
              AND is_dismissed = FALSE 
              AND (expires_at IS NULL OR expires_at > NOW())
            ORDER BY created_at DESC
        """;
        
        List<UserAlert> alerts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserAlertRepository] Error finding unread alerts: " + e.getMessage());
        }
        return alerts;
    }

    /**
     * Compter les alertes non lues pour un utilisateur
     */
    public int countUnreadByUserId(int userId) {
        String sql = """
            SELECT COUNT(*) FROM user_alerts 
            WHERE user_id = ? 
              AND is_read = FALSE 
              AND is_dismissed = FALSE 
              AND (expires_at IS NULL OR expires_at > NOW())
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[UserAlertRepository] Error counting unread alerts: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Marquer une alerte comme lue
     */
    public boolean markAsRead(int alertId) {
        String sql = "UPDATE user_alerts SET is_read = TRUE, read_at = NOW() WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, alertId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserAlertRepository] Error marking alert as read: " + e.getMessage());
        }
        return false;
    }

    /**
     * Marquer une alerte comme fermée
     */
    public boolean dismiss(int alertId) {
        String sql = "UPDATE user_alerts SET is_dismissed = TRUE WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, alertId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserAlertRepository] Error dismissing alert: " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupérer toutes les alertes créées par un admin
     */
    public List<UserAlert> findByAdminId(int adminId) {
        String sql = "SELECT * FROM user_alerts WHERE admin_id = ? ORDER BY created_at DESC";
        
        List<UserAlert> alerts = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, adminId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserAlertRepository] Error finding alerts by admin: " + e.getMessage());
        }
        return alerts;
    }

    /**
     * Supprimer les alertes expirées
     */
    public int deleteExpired() {
        String sql = "DELETE FROM user_alerts WHERE expires_at IS NOT NULL AND expires_at < NOW()";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[UserAlertRepository] Error deleting expired alerts: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Mapper une ligne SQL vers un objet UserAlert
     */
    private UserAlert mapRow(ResultSet rs) throws SQLException {
        UserAlert alert = new UserAlert();
        alert.setId(rs.getInt("id"));
        alert.setUserId(rs.getInt("user_id"));
        alert.setAdminId(rs.getInt("admin_id"));
        alert.setTitle(rs.getString("title"));
        alert.setMessage(rs.getString("message"));
        alert.setType(AlertType.valueOf(rs.getString("type")));
        alert.setCategory(AlertCategory.valueOf(rs.getString("category")));
        alert.setRead(rs.getBoolean("is_read"));
        alert.setDismissed(rs.getBoolean("is_dismissed"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) alert.setCreatedAt(createdAt.toLocalDateTime());
        
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) alert.setExpiresAt(expiresAt.toLocalDateTime());
        
        Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) alert.setReadAt(readAt.toLocalDateTime());
        
        alert.setActionUrl(rs.getString("action_url"));
        alert.setActionLabel(rs.getString("action_label"));
        
        return alert;
    }
}
