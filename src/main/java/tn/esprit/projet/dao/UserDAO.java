package tn.esprit.projet.dao;

import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private final Connection cnx = MyBDConnexion.getInstance().getCnx();
    private String lastError = "";

    public String getLastError() { return lastError; }

    // ── findAll ────────────────────────────────────────────────────────────────
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement("SELECT * FROM user ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── findById ───────────────────────────────────────────────────────────────
    public User findById(int id) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT * FROM user WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── findByEmail ────────────────────────────────────────────────────────────
    public User findByEmail(String email) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT * FROM user WHERE email=?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── findByVerificationCode ─────────────────────────────────────────────────
    public User findByVerificationCode(String code) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM user WHERE verification_code=?")) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── create ─────────────────────────────────────────────────────────────────
    public boolean create(User u) {
        String sql = "INSERT INTO user " +
                "(email,password,roles,is_active,created_at,first_name,last_name," +
                "birthday,weight,height,welcome_message,photo_filename,google_id) " +
                "VALUES (?,?,?,?,NOW(),?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPassword());
            ps.setString(3, nvlRole(u.getRoles()));
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
            if (keys.next()) { u.setId(keys.getInt(1)); return true; }
        } catch (Exception e) {
            lastError = e.getMessage();
            e.printStackTrace();
        }
        return false;
    }

    // ── update (profile fields, no password change here) ──────────────────────
    public boolean update(User u) {
        boolean changePwd = u.getPassword() != null && !u.getPassword().isBlank();
        String sql = "UPDATE user SET email=?,roles=?,is_active=?,first_name=?,last_name=?," +
                "birthday=?,weight=?,height=?,welcome_message=?,photo_filename=?,google_id=?" +
                (changePwd ? ",password=?" : "") +
                " WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getEmail());
            ps.setString(i++, nvlRole(u.getRoles()));
            ps.setBoolean(i++, u.isActive());
            ps.setString(i++, u.getFirstName());
            ps.setString(i++, u.getLastName());
            ps.setDate(i++, u.getBirthday() != null ? Date.valueOf(u.getBirthday()) : null);
            ps.setDouble(i++, u.getWeight());
            ps.setDouble(i++, u.getHeight());
            ps.setString(i++, u.getWelcomeMessage());
            ps.setString(i++, u.getPhotoFilename());
            ps.setString(i++, u.getGoogleId());
            if (changePwd) ps.setString(i++, u.getPassword());
            ps.setInt(i, u.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { lastError = e.getMessage(); e.printStackTrace(); }
        return false;
    }

    // ── delete ─────────────────────────────────────────────────────────────────
    public boolean delete(int id) {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM user WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── toggleActive ───────────────────────────────────────────────────────────
    public boolean toggleActive(int id, boolean newState) {
        try (PreparedStatement ps = cnx.prepareStatement("UPDATE user SET is_active=? WHERE id=?")) {
            ps.setBoolean(1, newState);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── emailExists ────────────────────────────────────────────────────────────
    public boolean emailExists(String email, int excludeId) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT id FROM user WHERE email=? AND id!=?")) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── saveVerificationCode ───────────────────────────────────────────────────
    public boolean saveVerificationCode(int userId, String code, java.time.LocalDateTime expiresAt) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE user SET verification_code=?,verification_code_expires_at=? WHERE id=?")) {
            ps.setString(1, code);
            ps.setTimestamp(2, Timestamp.valueOf(expiresAt));
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── clearVerificationCode ──────────────────────────────────────────────────
    public boolean clearVerificationCode(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE user SET verification_code=NULL,verification_code_expires_at=NULL WHERE id=?")) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── updatePassword ─────────────────────────────────────────────────────────
    public boolean updatePassword(int userId, String hashedPassword) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE user SET password=?,reset_token=NULL,reset_token_expires_at=NULL," +
                "verification_code=NULL,verification_code_expires_at=NULL WHERE id=?")) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── saveFaceDescriptor ─────────────────────────────────────────────────────
    public boolean saveFaceDescriptor(int userId, String descriptorJson) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE user SET face_descriptor=?,face_id_enrolled_at=NOW() WHERE id=?")) {
            ps.setString(1, descriptorJson);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── clearFaceDescriptor ────────────────────────────────────────────────────
    public boolean clearFaceDescriptor(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE user SET face_descriptor=NULL,face_id_enrolled_at=NULL WHERE id=?")) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── findAllWithFaceId ──────────────────────────────────────────────────────
    public List<User> findAllWithFaceId() {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM user WHERE face_descriptor IS NOT NULL AND is_active=1");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── counts ─────────────────────────────────────────────────────────────────
    public int countAll() {
        return count("SELECT COUNT(*) FROM user");
    }
    public int countActive() {
        return count("SELECT COUNT(*) FROM user WHERE is_active=1");
    }
    public int countByRole(String role) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM user WHERE roles=?")) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
    public int countRegisteredThisMonth() {
        return count("SELECT COUNT(*) FROM user WHERE MONTH(created_at)=MONTH(NOW()) AND YEAR(created_at)=YEAR(NOW())");
    }
    private int count(String sql) {
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ── ResultSet → User ───────────────────────────────────────────────────────
    private User map(ResultSet rs) throws Exception {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));

        String roles = rs.getString("roles");
        if (roles != null && roles.startsWith("["))
            roles = roles.replaceAll("[\\[\\]\"\\s]", "");
        u.setRoles(roles != null ? roles : "ROLE_USER");

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

        return u;
    }

    private String nvlRole(String r) {
        return (r != null && !r.isBlank()) ? r : "ROLE_USER";
    }
}
