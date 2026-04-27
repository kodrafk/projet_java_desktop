package tn.esprit.projet.repository;

import tn.esprit.projet.models.Message;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pour gérer les messages entre admin et users
 */
public class MessageRepository {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Récupérer tous les messages entre deux utilisateurs
     */
    public List<Message> findConversation(int userId1, int userId2) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE " +
                     "(sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?) " +
                     "ORDER BY sent_at ASC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                messages.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MessageRepository] findConversation ERROR: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Récupérer les messages non lus pour un utilisateur
     */
    public List<Message> findUnreadByReceiver(int receiverId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE receiver_id=? AND is_read=0 ORDER BY sent_at DESC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, receiverId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                messages.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("[MessageRepository] findUnreadByReceiver ERROR: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Sauvegarder un nouveau message
     */
    public void save(Message message) {
        String sql = "INSERT INTO message (sender_id, receiver_id, content, is_read, sent_at) " +
                     "VALUES (?, ?, ?, 0, NOW())";
        
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getSenderId());
            ps.setInt(2, message.getReceiverId());
            ps.setString(3, message.getContent());
            ps.executeUpdate();
            
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                message.setId(keys.getInt(1));
            }
            System.out.println("[MessageRepository] Message saved with ID: " + message.getId());
        } catch (SQLException e) {
            System.err.println("[MessageRepository] save ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Marquer un message comme lu
     */
    public void markAsRead(int messageId) {
        String sql = "UPDATE message SET is_read=1, read_at=NOW() WHERE id=?";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MessageRepository] markAsRead ERROR: " + e.getMessage());
        }
    }

    /**
     * Marquer tous les messages d'une conversation comme lus
     */
    public void markConversationAsRead(int receiverId, int senderId) {
        String sql = "UPDATE message SET is_read=1, read_at=NOW() " +
                     "WHERE receiver_id=? AND sender_id=? AND is_read=0";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, receiverId);
            ps.setInt(2, senderId);
            int updated = ps.executeUpdate();
            System.out.println("[MessageRepository] Marked " + updated + " messages as read");
        } catch (SQLException e) {
            System.err.println("[MessageRepository] markConversationAsRead ERROR: " + e.getMessage());
        }
    }

    /**
     * Compter les messages non lus pour un utilisateur
     */
    public int countUnread(int receiverId) {
        String sql = "SELECT COUNT(*) FROM message WHERE receiver_id=? AND is_read=0";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, receiverId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[MessageRepository] countUnread ERROR: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Supprimer un message
     */
    public void delete(int messageId) {
        String sql = "DELETE FROM message WHERE id=?";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MessageRepository] delete ERROR: " + e.getMessage());
        }
    }

    /**
     * Mapper ResultSet vers Message
     */
    private Message map(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setReceiverId(rs.getInt("receiver_id"));
        message.setContent(rs.getString("content"));
        message.setRead(rs.getBoolean("is_read"));
        
        Timestamp sentAt = rs.getTimestamp("sent_at");
        if (sentAt != null) {
            message.setSentAt(sentAt.toLocalDateTime());
        }
        
        Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) {
            message.setReadAt(readAt.toLocalDateTime());
        }
        
        return message;
    }
}
