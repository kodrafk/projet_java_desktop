package tn.esprit.projet.repository;

import tn.esprit.projet.models.PersonalizedMessage;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pour les messages personnalisés admin → user
 */
public class PersonalizedMessageRepository {

    /**
     * Créer la table si elle n'existe pas (compatible MySQL)
     */
    public void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS `personalized_messages` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NOT NULL," +
                "`admin_id` INT NOT NULL," +
                "`content` TEXT NOT NULL," +
                "`send_via_sms` TINYINT(1) DEFAULT 0," +
                "`sms_status` VARCHAR(50) DEFAULT NULL," +
                "`sms_id` VARCHAR(255) DEFAULT NULL," +
                "`is_read` TINYINT(1) DEFAULT 0," +
                "`sent_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`read_at` DATETIME DEFAULT NULL," +
                "INDEX `idx_user_id` (`user_id`)," +
                "INDEX `idx_admin_id` (`admin_id`)," +
                "INDEX `idx_sent_at` (`sent_at`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[PersonalizedMessageRepository] ✅ Table 'personalized_messages' ready.");
        } catch (SQLException e) {
            // Ignore "already exists" errors
            if (!e.getMessage().contains("already exists")) {
                System.err.println("[PersonalizedMessageRepository] Error creating table: " + e.getMessage());
            }
        }
    }

    /**
     * Sauvegarder un nouveau message
     */
    public PersonalizedMessage save(PersonalizedMessage msg) {
        String sql = """
            INSERT INTO personalized_messages (user_id, admin_id, content, send_via_sms, sms_status, sms_id, sent_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, msg.getUserId());
            ps.setInt(2, msg.getAdminId());
            ps.setString(3, msg.getContent());
            ps.setBoolean(4, msg.isSendViaSms());
            ps.setString(5, msg.getSmsStatus());
            ps.setString(6, msg.getSmsId());
            ps.setObject(7, msg.getSentAt());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        msg.setId(rs.getInt(1));
                        return msg;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error saving message: " + e.getMessage());
        }
        return null;
    }

    /**
     * Récupérer tous les messages pour un utilisateur (ordre chronologique ASC — pour l'historique backoffice)
     */
    public List<PersonalizedMessage> findByUserIdAsc(int userId) {
        String sql = "SELECT * FROM personalized_messages WHERE user_id = ? ORDER BY sent_at ASC";
        List<PersonalizedMessage> messages = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) messages.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error findByUserIdAsc: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Récupérer tous les messages pour un utilisateur (triés par date décroissante)
     */
    public List<PersonalizedMessage> findByUserId(int userId) {
        String sql = """
            SELECT * FROM personalized_messages 
            WHERE user_id = ? 
            ORDER BY sent_at DESC
        """;
        
        List<PersonalizedMessage> messages = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error finding messages: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Récupérer les messages non lus pour un utilisateur
     */
    public List<PersonalizedMessage> findUnreadByUserId(int userId) {
        String sql = """
            SELECT * FROM personalized_messages 
            WHERE user_id = ? AND is_read = 0 
            ORDER BY sent_at DESC
        """;
        
        List<PersonalizedMessage> messages = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error finding unread messages: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Compter les messages non lus pour un utilisateur
     */
    public int countUnreadByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM personalized_messages WHERE user_id = ? AND is_read = 0";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error counting unread: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Marquer un message comme lu
     */
    public void markAsRead(int messageId) {
        String sql = "UPDATE personalized_messages SET is_read = 1, read_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, LocalDateTime.now());
            ps.setInt(2, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error marking as read: " + e.getMessage());
        }
    }

    /**
     * Marquer tous les messages d'un utilisateur comme lus
     */
    public void markAllAsReadByUserId(int userId) {
        String sql = "UPDATE personalized_messages SET is_read = 1, read_at = ? WHERE user_id = ? AND is_read = 0";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, LocalDateTime.now());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error marking all as read: " + e.getMessage());
        }
    }

    /**
     * Mettre à jour le statut SMS
     */
    public void updateSmsStatus(int messageId, String status) {
        String sql = "UPDATE personalized_messages SET sms_status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error updating SMS status: " + e.getMessage());
        }
    }

    /**
     * Récupérer l'historique des messages envoyés par un admin
     */
    public List<PersonalizedMessage> findByAdminId(int adminId) {
        String sql = """
            SELECT * FROM personalized_messages 
            WHERE admin_id = ? 
            ORDER BY sent_at DESC
        """;
        
        List<PersonalizedMessage> messages = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error finding admin messages: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Supprimer un message
     */
    public boolean delete(int messageId) {
        String sql = "DELETE FROM personalized_messages WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, messageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PersonalizedMessageRepository] Error deleting message: " + e.getMessage());
        }
        return false;
    }

    /**
     * Mapper une ligne SQL vers un objet PersonalizedMessage
     */
    private PersonalizedMessage mapRow(ResultSet rs) throws SQLException {
        PersonalizedMessage msg = new PersonalizedMessage();
        msg.setId(rs.getInt("id"));
        msg.setUserId(rs.getInt("user_id"));
        msg.setAdminId(rs.getInt("admin_id"));
        msg.setContent(rs.getString("content"));
        msg.setSendViaSms(rs.getBoolean("send_via_sms"));
        msg.setSmsStatus(rs.getString("sms_status"));
        msg.setSmsId(rs.getString("sms_id"));
        msg.setRead(rs.getBoolean("is_read"));
        
        Timestamp sentTs = rs.getTimestamp("sent_at");
        if (sentTs != null) msg.setSentAt(sentTs.toLocalDateTime());
        
        Timestamp readTs = rs.getTimestamp("read_at");
        if (readTs != null) msg.setReadAt(readTs.toLocalDateTime());
        
        return msg;
    }
}
