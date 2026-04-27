package tn.esprit.projet.repository;

import tn.esprit.projet.models.ChatMessage;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageRepository {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Schema ────────────────────────────────────────────────────────────────

    public void ensureTableExists() {
        String sql =
            "CREATE TABLE IF NOT EXISTS `chat_messages` (" +
            "`id`           INT AUTO_INCREMENT PRIMARY KEY," +
            "`sender_id`    INT NOT NULL," +
            "`receiver_id`  INT NOT NULL," +
            "`sender_type`  ENUM('ADMIN','USER') NOT NULL," +
            "`content`      TEXT DEFAULT NULL," +
            "`image_path`   VARCHAR(500) DEFAULT NULL," +
            "`is_read`      TINYINT(1) NOT NULL DEFAULT 0," +
            "`edited`       TINYINT(1) NOT NULL DEFAULT 0," +
            "`deleted`      TINYINT(1) NOT NULL DEFAULT 0," +
            "`sent_via_sms` TINYINT(1) NOT NULL DEFAULT 0," +
            "`sms_status`   VARCHAR(50) DEFAULT NULL," +
            "`sent_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "`edited_at`    DATETIME DEFAULT NULL," +
            "`read_at`      DATETIME DEFAULT NULL," +
            "INDEX `idx_conv` (`sender_id`,`receiver_id`,`sent_at`)," +
            "INDEX `idx_recv` (`receiver_id`,`is_read`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement st = conn().createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            if (!e.getMessage().contains("already exists"))
                System.err.println("[ChatRepo] ensureTable: " + e.getMessage());
        }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public ChatMessage save(ChatMessage m) {
        String sql =
            "INSERT INTO chat_messages " +
            "(sender_id,receiver_id,sender_type,content,image_path,sent_via_sms,sms_status,sent_at) " +
            "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getSenderId());
            ps.setInt(2, m.getReceiverId());
            ps.setString(3, m.getSenderType().name());
            ps.setString(4, m.getContent());
            ps.setString(5, m.getImagePath());
            ps.setBoolean(6, m.isSentViaSms());
            ps.setString(7, m.getSmsStatus());
            ps.setObject(8, m.getSentAt());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) m.setId(rs.getInt(1));
            return m;
        } catch (SQLException e) {
            System.err.println("[ChatRepo] save: " + e.getMessage());
            return null;
        }
    }

    /** Edit message content */
    public boolean edit(int id, String newContent) {
        String sql = "UPDATE chat_messages SET content=?, edited=1, edited_at=? WHERE id=? AND deleted=0";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, newContent);
            ps.setObject(2, LocalDateTime.now());
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ChatRepo] edit: " + e.getMessage());
            return false;
        }
    }

    /** Soft-delete: marks deleted=1, clears content and image */
    public boolean delete(int id) {
        String sql = "UPDATE chat_messages SET deleted=1, content='[Message deleted]', image_path=NULL WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ChatRepo] delete: " + e.getMessage());
            return false;
        }
    }

    public void updateSmsStatus(int id, String status) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE chat_messages SET sms_status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ChatRepo] updateSmsStatus: " + e.getMessage());
        }
    }

    /** Mark all messages from sender as read by receiver */
    public void markAsRead(int senderId, int receiverId) {
        String sql = "UPDATE chat_messages SET is_read=1, read_at=? " +
                     "WHERE sender_id=? AND receiver_id=? AND is_read=0";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, LocalDateTime.now());
            ps.setInt(2, senderId);
            ps.setInt(3, receiverId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ChatRepo] markAsRead: " + e.getMessage());
        }
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Full conversation between admin and user, chronological order.
     * Includes both directions (admin→user and user→admin).
     */
    public List<ChatMessage> getConversation(int adminId, int userId) {
        String sql =
            "SELECT * FROM chat_messages " +
            "WHERE (sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?) " +
            "ORDER BY sent_at ASC";
        List<ChatMessage> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, adminId); ps.setInt(2, userId);
            ps.setInt(3, userId);  ps.setInt(4, adminId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[ChatRepo] getConversation: " + e.getMessage());
        }
        return list;
    }

    /** Count unread messages sent by admin to user */
    public int countUnread(int senderId, int receiverId) {
        String sql = "SELECT COUNT(*) FROM chat_messages WHERE sender_id=? AND receiver_id=? AND is_read=0 AND deleted=0";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, senderId); ps.setInt(2, receiverId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ChatRepo] countUnread: " + e.getMessage());
        }
        return 0;
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ChatMessage map(ResultSet rs) throws SQLException {
        ChatMessage m = new ChatMessage();
        m.setId(rs.getInt("id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        m.setSenderType(ChatMessage.SenderType.valueOf(rs.getString("sender_type")));
        m.setContent(rs.getString("content"));
        m.setImagePath(rs.getString("image_path"));
        m.setRead(rs.getBoolean("is_read"));
        m.setEdited(rs.getBoolean("edited"));
        m.setDeleted(rs.getBoolean("deleted"));
        m.setSentViaSms(rs.getBoolean("sent_via_sms"));
        m.setSmsStatus(rs.getString("sms_status"));
        Timestamp sa = rs.getTimestamp("sent_at");
        if (sa != null) m.setSentAt(sa.toLocalDateTime());
        Timestamp ea = rs.getTimestamp("edited_at");
        if (ea != null) m.setEditedAt(ea.toLocalDateTime());
        Timestamp ra = rs.getTimestamp("read_at");
        if (ra != null) m.setReadAt(ra.toLocalDateTime());
        return m;
    }
}
