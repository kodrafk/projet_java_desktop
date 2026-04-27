package tn.esprit.projet.repository;

import tn.esprit.projet.models.AdminMessage;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository for admin messages
 */
public class AdminMessageRepository {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Ensure admin_messages table exists
     */
    public void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS `admin_messages` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id` INT NOT NULL," +
                "`admin_id` INT NOT NULL," +
                "`message` TEXT NOT NULL," +
                "`sent_via_sms` TINYINT(1) DEFAULT 0," +
                "`is_read` TINYINT(1) DEFAULT 0," +
                "`sent_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`read_at` DATETIME DEFAULT NULL," +
                "`sms_status` VARCHAR(50) DEFAULT NULL," +
                "`sms_id` VARCHAR(255) DEFAULT NULL," +
                "INDEX `idx_user_id` (`user_id`)," +
                "INDEX `idx_admin_id` (`admin_id`)," +
                "INDEX `idx_sent_at` (`sent_at`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Statement st = conn().createStatement()) {
            st.executeUpdate(sql);
            System.out.println("[AdminMessages] ✅ Table ready");
        } catch (SQLException e) {
            if (!e.getMessage().contains("already exists")) {
                System.err.println("[AdminMessages] ❌ Could not create table: " + e.getMessage());
            }
        }
    }

    /**
     * Save a new message
     */
    public AdminMessage save(AdminMessage msg) {
        String sql = "INSERT INTO admin_messages (user_id, admin_id, message, sent_via_sms, is_read, sent_at, sms_status, sms_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, msg.getUserId());
            ps.setInt(2, msg.getAdminId());
            ps.setString(3, msg.getMessage());
            ps.setBoolean(4, msg.isSentViaSms());
            ps.setBoolean(5, msg.isRead());
            ps.setObject(6, msg.getSentAt());
            ps.setString(7, msg.getSmsStatus());
            ps.setString(8, msg.getSmsId());
            
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                msg.setId(rs.getInt(1));
            }
            
            return msg;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all messages for a specific user
     */
    public List<AdminMessage> findByUserId(int userId) {
        List<AdminMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM admin_messages WHERE user_id = ? ORDER BY sent_at DESC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }

    /**
     * Get unread messages count for a user
     */
    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) FROM admin_messages WHERE user_id = ? AND is_read = 0";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Get unread messages for a user
     */
    public List<AdminMessage> findUnreadByUserId(int userId) {
        List<AdminMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM admin_messages WHERE user_id = ? AND is_read = 0 ORDER BY sent_at DESC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }

    /**
     * Mark message as read
     */
    public void markAsRead(int messageId) {
        String sql = "UPDATE admin_messages SET is_read = 1, read_at = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, LocalDateTime.now());
            ps.setInt(2, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mark all messages as read for a user
     */
    public void markAllAsRead(int userId) {
        String sql = "UPDATE admin_messages SET is_read = 1, read_at = ? WHERE user_id = ? AND is_read = 0";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, LocalDateTime.now());
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get conversation between admin and user
     */
    public List<AdminMessage> getConversation(int userId, int adminId) {
        List<AdminMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM admin_messages WHERE user_id = ? AND admin_id = ? ORDER BY sent_at ASC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, adminId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                messages.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }

    /**
     * Update SMS status
     */
    public void updateSmsStatus(int messageId, String status) {
        String sql = "UPDATE admin_messages SET sms_status = ? WHERE id = ?";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a message
     */
    public void delete(int messageId) {
        String sql = "DELETE FROM admin_messages WHERE id = ?";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Map ResultSet to AdminMessage object
     */
    private AdminMessage mapResultSet(ResultSet rs) throws SQLException {
        AdminMessage msg = new AdminMessage();
        msg.setId(rs.getInt("id"));
        msg.setUserId(rs.getInt("user_id"));
        msg.setAdminId(rs.getInt("admin_id"));
        msg.setMessage(rs.getString("message"));
        msg.setSentViaSms(rs.getBoolean("sent_via_sms"));
        msg.setRead(rs.getBoolean("is_read"));
        msg.setSentAt(rs.getObject("sent_at", LocalDateTime.class));
        msg.setReadAt(rs.getObject("read_at", LocalDateTime.class));
        msg.setSmsStatus(rs.getString("sms_status"));
        msg.setSmsId(rs.getString("sms_id"));
        return msg;
    }
}
