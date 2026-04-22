package tn.esprit.projet.dao;

import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private Connection cnx() {
        return MyBDConnexion.getInstance().getCnx();
    }

    // ── findAll ────────────────────────────────────────────────────────────────
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY id DESC";
        try (PreparedStatement ps = cnx().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── findById ───────────────────────────────────────────────────────────────
    public User findById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── findByEmail ────────────────────────────────────────────────────────────
    public User findByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── create ─────────────────────────────────────────────────────────────────
    public boolean create(User u) {
        String sql = "INSERT INTO user " +
                "(email, password, roles, is_active, created_at, first_name, last_name, " +
                " birthday, weight, height, phone_number, phone_verified, photo_filename, welcome_message) " +
                "VALUES (?,?,?,?,NOW(),?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPassword());
            ps.setString(3, (u.getRoles() != null && !u.getRoles().isBlank()) ? u.getRoles() : "ROLE_USER");
            ps.setBoolean(4, u.isActive());
            ps.setString(5, u.getFirstName());
            ps.setString(6, u.getLastName());
            ps.setDate(7, u.getBirthday() != null ? Date.valueOf(u.getBirthday()) : null);
            ps.setFloat(8, u.getWeight());
            ps.setFloat(9, u.getHeight());
            ps.setString(10, u.getPhoneNumber());
            ps.setBoolean(11, u.isPhoneVerified());
            ps.setString(12, u.getPhotoFilename());
            ps.setString(13, u.getWelcomeMessage());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) { u.setId(keys.getInt(1)); return true; }
        } catch (Exception e) {
            lastError = e.getMessage();
            System.err.println("[UserDAO.create] SQL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private String lastError = "";
    public String getLastError() { return lastError; }

    // ── update ─────────────────────────────────────────────────────────────────
    public boolean update(User u) {
        boolean changePassword = u.getPassword() != null && !u.getPassword().isBlank();
        String sql = "UPDATE user SET email=?, roles=?, is_active=?, first_name=?, last_name=?, " +
                "birthday=?, weight=?, height=?, phone_number=?, phone_verified=?, " +
                "photo_filename=?, welcome_message=?" +
                (changePassword ? ", password=?" : "") +
                " WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, u.getEmail());
            ps.setString(i++, (u.getRoles() != null && !u.getRoles().isBlank()) ? u.getRoles() : "ROLE_USER");
            ps.setBoolean(i++, u.isActive());
            ps.setString(i++, u.getFirstName());
            ps.setString(i++, u.getLastName());
            ps.setDate(i++, u.getBirthday() != null ? Date.valueOf(u.getBirthday()) : null);
            ps.setFloat(i++, u.getWeight());
            ps.setFloat(i++, u.getHeight());
            ps.setString(i++, u.getPhoneNumber());
            ps.setBoolean(i++, u.isPhoneVerified());
            ps.setString(i++, u.getPhotoFilename());
            ps.setString(i++, u.getWelcomeMessage());
            if (changePassword) ps.setString(i++, u.getPassword());
            ps.setInt(i, u.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── delete ─────────────────────────────────────────────────────────────────
    public boolean delete(int id) {
        String sql = "DELETE FROM user WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── toggleActive ───────────────────────────────────────────────────────────
    public boolean toggleActive(int id, boolean newState) {
        String sql = "UPDATE user SET is_active=? WHERE id=?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setBoolean(1, newState);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── emailExists ────────────────────────────────────────────────────────────
    public boolean emailExists(String email, int excludeId) {
        String sql = "SELECT id FROM user WHERE email=? AND id != ?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ── countAll ───────────────────────────────────────────────────────────────
    public int countAll() {
        try (PreparedStatement ps = cnx().prepareStatement("SELECT COUNT(*) FROM user");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ── countActive ────────────────────────────────────────────────────────────
    public int countActive() {
        try (PreparedStatement ps = cnx().prepareStatement("SELECT COUNT(*) FROM user WHERE is_active=1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ── countByRole ────────────────────────────────────────────────────────────
    public int countByRole(String role) {
        try (PreparedStatement ps = cnx().prepareStatement("SELECT COUNT(*) FROM user WHERE roles=?")) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ── countRegisteredThisMonth ───────────────────────────────────────────────
    public int countRegisteredThisMonth() {
        String sql = "SELECT COUNT(*) FROM user WHERE MONTH(created_at)=MONTH(NOW()) AND YEAR(created_at)=YEAR(NOW())";
        try (PreparedStatement ps = cnx().prepareStatement(sql);
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
        // Normalize roles — handle old JSON array format ["ROLE_USER"]
        String roles = rs.getString("roles");
        if (roles != null && roles.startsWith("[")) {
            roles = roles.replaceAll("[\\[\\]\"\\s]", "");
            if (roles.isEmpty()) roles = "ROLE_USER";
        }
        u.setRoles(roles != null ? roles : "ROLE_USER");
        u.setActive(rs.getBoolean("is_active"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));

        Date bd = rs.getDate("birthday");
        if (bd != null) u.setBirthday(bd.toLocalDate());

        u.setWeight(rs.getFloat("weight"));
        u.setHeight(rs.getFloat("height"));
        u.setPhoneNumber(rs.getString("phone_number"));
        u.setPhoneVerified(rs.getBoolean("phone_verified"));
        u.setPhotoFilename(rs.getString("photo_filename"));
        u.setWelcomeMessage(rs.getString("welcome_message"));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toLocalDateTime());

        return u;
    }
}

