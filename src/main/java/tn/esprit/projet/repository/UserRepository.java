package tn.esprit.projet.repository;

import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserRepository {

    private static final Set<String> SORTABLE_COLUMNS =
            Set.of("id", "email", "created_at", "is_active", "roles", "first_name", "last_name");

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Finders ────────────────────────────────────────────────────────────────

    public User findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM user WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User findByEmail(String email) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM user WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User findByGoogleId(String googleId) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM user WHERE google_id=?")) {
            ps.setString(1, googleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User findByVerificationCode(String email, String code) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM user WHERE email=? AND verification_code=?")) {
            ps.setString(1, email);
            ps.setString(2, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User findByResetToken(String token) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM user WHERE reset_token=?")) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY id DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[UserRepository] ERROR loading users: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[UserRepository] findAll → " + list.size() + " users");
        return list;
    }

    public List<User> searchByEmailOrName(String query) {
        List<User> list = new ArrayList<>();
        String q = "%" + query + "%";
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM user WHERE email LIKE ? OR first_name LIKE ? OR last_name LIKE ? ORDER BY id DESC")) {
            ps.setString(1, q); ps.setString(2, q); ps.setString(3, q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<User> findAllSortedBy(String column, String direction) {
        if (!SORTABLE_COLUMNS.contains(column)) column = "id";
        if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) direction = "ASC";
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY `" + column + "` " + direction;
        System.out.println("[UserRepository] findAllSortedBy: " + sql);
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[UserRepository] ERROR in findAllSortedBy: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[UserRepository] findAllSortedBy → " + list.size() + " users");
        return list;
    }

    public List<User> findAllWithFaceId() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM user WHERE face_descriptor IS NOT NULL AND is_active=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Write operations ───────────────────────────────────────────────────────

    public void save(User u) {
        String sql = "INSERT INTO user (email,password,roles,is_active,created_at," +
                "first_name,last_name,birthday,weight,height,welcome_message,photo_filename,google_id) " +
                "VALUES (?,?,?,?,NOW(),?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPassword());
            ps.setString(3, nvlRole(u.getRole()));
            ps.setBoolean(4, u.isActive());
            ps.setString(5, u.getFirstName());
            ps.setString(6, u.getLastName());
            ps.setDate(7, u.getBirthday() != null ? Date.valueOf(u.getBirthday()) : null);
            ps.setDouble(8, u.getWeight());
            ps.setDouble(9, u.getHeight());
            ps.setString(10, u.getWelcomeMessage());
            ps.setString(11, u.getPhotoFilename());
            ps.setString(12, u.getGoogleId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) u.setId(keys.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(User u) {
        String sql = "UPDATE user SET email=?,roles=?,is_active=?,first_name=?,last_name=?," +
                "birthday=?,weight=?,height=?,welcome_message=?,photo_filename=?,google_id=?," +
                "gallery_access_enabled=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, nvlRole(u.getRole()));
            ps.setBoolean(3, u.isActive());
            ps.setString(4, u.getFirstName());
            ps.setString(5, u.getLastName());
            ps.setDate(6, u.getBirthday() != null ? Date.valueOf(u.getBirthday()) : null);
            ps.setDouble(7, u.getWeight());
            ps.setDouble(8, u.getHeight());
            ps.setString(9, u.getWelcomeMessage());
            ps.setString(10, u.getPhotoFilename());
            ps.setString(11, u.getGoogleId());
            ps.setBoolean(12, u.isGalleryAccessEnabled());
            ps.setInt(13, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updatePassword(int userId, String hashedPassword) {
        exec("UPDATE user SET password=? WHERE id=?", hashedPassword, userId);
    }

    public void updatePhoto(int userId, String filename) {
        exec("UPDATE user SET photo_filename=? WHERE id=?", filename, userId);
    }

    public void setGalleryAccess(int userId, boolean enabled) {
        exec("UPDATE user SET gallery_access_enabled=? WHERE id=?", enabled, userId);
    }

    public void updateWelcomeMessage(int userId, String message) {
        exec("UPDATE user SET welcome_message=? WHERE id=?", message, userId);
    }

    public void updateFaceDescriptor(int userId, String jsonDescriptor, LocalDateTime enrolledAt) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE user SET face_descriptor=?,face_id_enrolled_at=? WHERE id=?")) {
            ps.setString(1, jsonDescriptor);
            ps.setTimestamp(2, Timestamp.valueOf(enrolledAt));
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void removeFaceDescriptor(int userId) {
        // Clear from user table
        exec("UPDATE user SET face_descriptor=NULL,face_id_enrolled_at=NULL WHERE id=?", userId);
        // Also deactivate from face_embeddings table so it can never match again
        exec("UPDATE face_embeddings SET is_active=0 WHERE user_id=?", userId);
    }

    public void setActive(int userId, boolean active) {
        exec("UPDATE user SET is_active=? WHERE id=?", active, userId);
    }

    public void setVerificationCode(int userId, String code, LocalDateTime expiresAt) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE user SET verification_code=?,verification_code_expires_at=? WHERE id=?")) {
            ps.setString(1, code);
            ps.setTimestamp(2, Timestamp.valueOf(expiresAt));
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void clearVerificationCode(int userId) {
        exec("UPDATE user SET verification_code=NULL,verification_code_expires_at=NULL WHERE id=?", userId);
    }

    public void setResetToken(int userId, String token, LocalDateTime expiresAt) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE user SET reset_token=?,reset_token_expires_at=? WHERE id=?")) {
            ps.setString(1, token);
            ps.setTimestamp(2, Timestamp.valueOf(expiresAt));
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void clearResetToken(int userId) {
        exec("UPDATE user SET reset_token=NULL,reset_token_expires_at=NULL WHERE id=?", userId);
    }

    public void delete(int userId) {
        exec("DELETE FROM user WHERE id=?", userId);
    }

    // ── Counts ─────────────────────────────────────────────────────────────────

    public int countAll() {
        return countQuery("SELECT COUNT(*) FROM user");
    }

    public int countActive() {
        return countQuery("SELECT COUNT(*) FROM user WHERE is_active=1");
    }

    public int countInactive() {
        return countQuery("SELECT COUNT(*) FROM user WHERE is_active=0");
    }

    public int countAdmins() {
        return countQuery("SELECT COUNT(*) FROM user WHERE roles='ROLE_ADMIN'");
    }

    public boolean emailExistsExcluding(String email, int excludeId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT id FROM user WHERE email=? AND id!=?")) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Returns login count from face_verification_attempts (successful logins) */
    public int countLogins(int userId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM face_verification_attempts WHERE user_id=? AND success=1")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            // Table may not exist yet — return 0
        }
        return 0;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void exec(String sql, Object... params) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Boolean) ps.setBoolean(i + 1, (Boolean) params[i]);
                else if (params[i] instanceof Integer) ps.setInt(i + 1, (Integer) params[i]);
                else ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private int countQuery(String sql) {
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));

        String role = rs.getString("roles");
        if (role != null && role.startsWith("["))
            role = role.replaceAll("[\\[\\]\"\\s]", "");
        u.setRole(role != null ? role : "ROLE_USER");

        u.setActive(rs.getBoolean("is_active"));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toLocalDateTime());

        u.setResetToken(rs.getString("reset_token"));
        Timestamp rte = rs.getTimestamp("reset_token_expires_at");
        if (rte != null) u.setResetTokenExpiresAt(rte.toLocalDateTime());

        u.setVerificationCode(rs.getString("verification_code"));
        Timestamp vce = rs.getTimestamp("verification_code_expires_at");
        if (vce != null) u.setVerificationCodeExpiresAt(vce.toLocalDateTime());

        u.setFaceDescriptor(rs.getString("face_descriptor"));
        Timestamp fie = rs.getTimestamp("face_id_enrolled_at");
        if (fie != null) u.setFaceIdEnrolledAt(fie.toLocalDateTime());

        u.setWelcomeMessage(rs.getString("welcome_message"));
        u.setGoogleId(rs.getString("google_id"));
        u.setPhotoFilename(rs.getString("photo_filename"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));

        Date bd = rs.getDate("birthday");
        if (bd != null) u.setBirthday(bd.toLocalDate());

        u.setWeight(rs.getDouble("weight"));
        u.setHeight(rs.getDouble("height"));
        try { u.setGalleryAccessEnabled(rs.getBoolean("gallery_access_enabled")); } catch (SQLException ignored) {}
        try { u.setPhone(rs.getString("phone")); } catch (SQLException ignored) {}
        return u;
    }

    private String nvlRole(String r) {
        return (r != null && !r.isBlank()) ? r : "ROLE_USER";
    }
}
