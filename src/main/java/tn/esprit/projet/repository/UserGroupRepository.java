package tn.esprit.projet.repository;

import tn.esprit.projet.models.User;
import tn.esprit.projet.models.UserGroup;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserGroupRepository {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Schema ────────────────────────────────────────────────────────────────

    public void ensureTablesExist() {
        try (Statement st = conn().createStatement()) {
            // Groups table
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `user_groups` (" +
                "`id`          INT AUTO_INCREMENT PRIMARY KEY," +
                "`name`        VARCHAR(100) NOT NULL," +
                "`description` TEXT DEFAULT NULL," +
                "`admin_id`    INT NOT NULL," +
                "`color`       VARCHAR(20) DEFAULT '#2E7D5A'," +
                "`is_pinned`   TINYINT(1) NOT NULL DEFAULT 0," +
                "`created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "INDEX `idx_admin` (`admin_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Add is_pinned column if missing (for existing DBs)
            try { st.executeUpdate("ALTER TABLE `user_groups` ADD COLUMN `is_pinned` TINYINT(1) NOT NULL DEFAULT 0"); }
            catch (SQLException ignored) {}

            // Members pivot table
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `user_group_members` (" +
                "`group_id`   INT NOT NULL," +
                "`user_id`    INT NOT NULL," +
                "`added_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`group_id`, `user_id`)," +
                "INDEX `idx_user` (`user_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        } catch (SQLException e) {
            if (!e.getMessage().contains("already exists"))
                System.err.println("[GroupRepo] ensureTables: " + e.getMessage());
        }
    }

    // ── CRUD Groups ───────────────────────────────────────────────────────────

    public UserGroup save(UserGroup g) {
        String sql = "INSERT INTO user_groups (name, description, admin_id, color) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getName());
            ps.setString(2, g.getDescription());
            ps.setInt(3, g.getAdminId());
            ps.setString(4, g.getColor());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) g.setId(rs.getInt(1));
            return g;
        } catch (SQLException e) {
            System.err.println("[GroupRepo] save: " + e.getMessage());
            return null;
        }
    }

    public boolean update(UserGroup g) {
        String sql = "UPDATE user_groups SET name=?, description=?, color=? WHERE id=? AND admin_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setString(2, g.getDescription());
            ps.setString(3, g.getColor());
            ps.setInt(4, g.getId());
            ps.setInt(5, g.getAdminId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[GroupRepo] update: " + e.getMessage());
            return false;
        }
    }

    /** Toggle pin — only affects this admin's view */
    public boolean togglePin(int groupId, int adminId) {
        String sql = "UPDATE user_groups SET is_pinned = NOT is_pinned WHERE id=? AND admin_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, groupId); ps.setInt(2, adminId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[GroupRepo] togglePin: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int groupId, int adminId) {
        try (Statement st = conn().createStatement()) {
            st.executeUpdate("DELETE FROM user_group_members WHERE group_id=" + groupId);
            PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM user_groups WHERE id=? AND admin_id=?");
            ps.setInt(1, groupId); ps.setInt(2, adminId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[GroupRepo] delete: " + e.getMessage());
            return false;
        }
    }

    /** All groups created by this admin, with member count */
    public List<UserGroup> findByAdmin(int adminId) {
        List<UserGroup> list = new ArrayList<>();
        String sql = "SELECT g.*, COUNT(m.user_id) AS member_count " +
                     "FROM user_groups g " +
                     "LEFT JOIN user_group_members m ON m.group_id = g.id " +
                     "WHERE g.admin_id=? GROUP BY g.id ORDER BY g.created_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserGroup g = mapGroup(rs);
                list.add(g);
            }
        } catch (SQLException e) {
            System.err.println("[GroupRepo] findByAdmin: " + e.getMessage());
        }
        return list;
    }

    // ── Members ───────────────────────────────────────────────────────────────

    public void addMember(int groupId, int userId) {
        String sql = "INSERT IGNORE INTO user_group_members (group_id, user_id) VALUES (?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, groupId); ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[GroupRepo] addMember: " + e.getMessage());
        }
    }

    public void removeMember(int groupId, int userId) {
        String sql = "DELETE FROM user_group_members WHERE group_id=? AND user_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, groupId); ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[GroupRepo] removeMember: " + e.getMessage());
        }
    }

    /** Get all user IDs in a group */
    public List<Integer> getMemberIds(int groupId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT user_id FROM user_group_members WHERE group_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (SQLException e) {
            System.err.println("[GroupRepo] getMemberIds: " + e.getMessage());
        }
        return ids;
    }

    /** Get full User objects for a group */
    public List<User> getMembers(int groupId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.* FROM user u " +
                     "JOIN user_group_members m ON m.user_id = u.id " +
                     "WHERE m.group_id=? ORDER BY u.first_name";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapUser(rs));
        } catch (SQLException e) {
            System.err.println("[GroupRepo] getMembers: " + e.getMessage());
        }
        return list;
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private UserGroup mapGroup(ResultSet rs) throws SQLException {
        UserGroup g = new UserGroup();
        g.setId(rs.getInt("id"));
        g.setName(rs.getString("name"));
        g.setDescription(rs.getString("description"));
        g.setAdminId(rs.getInt("admin_id"));
        g.setColor(rs.getString("color"));
        try { g.setPinned(rs.getBoolean("is_pinned")); } catch (SQLException ignored) {}
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) g.setCreatedAt(ca.toLocalDateTime());
        return g;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        String role = rs.getString("roles");
        if (role != null && role.startsWith("[")) role = role.replaceAll("[\\[\\]\"\\s]", "");
        u.setRole(role != null ? role : "ROLE_USER");
        u.setActive(rs.getBoolean("is_active"));
        try { u.setPhone(rs.getString("phone")); } catch (SQLException ignored) {}
        try { u.setPhotoFilename(rs.getString("photo_filename")); } catch (SQLException ignored) {}
        return u;
    }
}
