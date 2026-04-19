package tn.esprit.projet.dao;

import tn.esprit.projet.models.Badge;
import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BadgeDAO {

    private final Connection cnx = MyBDConnexion.getInstance().getCnx();

    // ── All badges ─────────────────────────────────────────────────────────────
    public List<Badge> findAllBadges() {
        List<Badge> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement("SELECT * FROM badge ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBadge(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── UserBadges for a user (with badge info joined) ─────────────────────────
    public List<UserBadge> findByUser(int userId) {
        List<UserBadge> list = new ArrayList<>();
        String sql = "SELECT ub.*, b.name, b.description, b.condition_type, b.condition_value, b.icon " +
                     "FROM user_badge ub JOIN badge b ON ub.badge_id=b.id WHERE ub.user_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapUserBadge(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── Ensure all badges exist for a user (init) ──────────────────────────────
    public void ensureUserBadges(int userId) {
        List<Badge> all = findAllBadges();
        for (Badge b : all) {
            try (PreparedStatement ps = cnx.prepareStatement(
                    "INSERT IGNORE INTO user_badge (user_id,badge_id,unlocked,current_value,is_vitrine) VALUES (?,?,0,0,0)")) {
                ps.setInt(1, userId);
                ps.setInt(2, b.getId());
                ps.executeUpdate();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ── Toggle vitrine (max 3) ─────────────────────────────────────────────────
    public boolean toggleVitrine(int userBadgeId, int userId, boolean newState) {
        if (newState) {
            // Count current vitrine badges
            try (PreparedStatement ps = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM user_badge WHERE user_id=? AND is_vitrine=1 AND unlocked=1")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) >= 3) return false; // max 3
            } catch (Exception e) { e.printStackTrace(); return false; }
        }
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE user_badge SET is_vitrine=? WHERE id=? AND user_id=?")) {
            ps.setBoolean(1, newState);
            ps.setInt(2, userBadgeId);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Unlock a badge ─────────────────────────────────────────────────────────
    public boolean unlock(int userBadgeId) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE user_badge SET unlocked=1,unlocked_at=NOW() WHERE id=?")) {
            ps.setInt(1, userBadgeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Update progress ────────────────────────────────────────────────────────
    public boolean updateProgress(int userBadgeId, int value) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "UPDATE user_badge SET current_value=? WHERE id=?")) {
            ps.setInt(1, value);
            ps.setInt(2, userBadgeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Seed default badges if none exist ─────────────────────────────────────
    public void seedDefaultBadges() {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM badge");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (Exception e) { e.printStackTrace(); return; }

        String[][] badges = {
            {"🌱", "First Steps",       "Log your first meal",          "meals",    "1"},
            {"🔥", "On Fire",           "Log meals 7 days in a row",    "streak",   "7"},
            {"💪", "Fitness Freak",     "Complete 10 workouts",         "workouts", "10"},
            {"🥗", "Salad Lover",       "Log 20 healthy meals",         "healthy",  "20"},
            {"⚖",  "Balance Master",   "Maintain BMI in normal range", "bmi",      "30"},
            {"🏆", "Champion",         "Reach 100 logged meals",       "meals",    "100"},
            {"🌟", "Star User",        "Be active for 30 days",        "days",     "30"},
            {"🎯", "Goal Crusher",     "Hit your calorie goal 5 times","goals",    "5"},
        };
        for (String[] b : badges) {
            try (PreparedStatement ps = cnx.prepareStatement(
                    "INSERT INTO badge (icon,name,description,condition_type,condition_value) VALUES (?,?,?,?,?)")) {
                ps.setString(1, b[0]);
                ps.setString(2, b[1]);
                ps.setString(3, b[2]);
                ps.setString(4, b[3]);
                ps.setInt(5, Integer.parseInt(b[4]));
                ps.executeUpdate();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ── Mappers ────────────────────────────────────────────────────────────────
    private Badge mapBadge(ResultSet rs) throws Exception {
        Badge b = new Badge();
        b.setId(rs.getInt("id"));
        b.setName(rs.getString("name"));
        b.setDescription(rs.getString("description"));
        b.setConditionType(rs.getString("condition_type"));
        b.setConditionValue(rs.getInt("condition_value"));
        b.setIcon(rs.getString("icon"));
        return b;
    }

    private UserBadge mapUserBadge(ResultSet rs) throws Exception {
        UserBadge ub = new UserBadge();
        ub.setId(rs.getInt("id"));
        ub.setUserId(rs.getInt("user_id"));
        ub.setUnlocked(rs.getBoolean("unlocked"));
        Timestamp ua = rs.getTimestamp("unlocked_at");
        if (ua != null) ub.setUnlockedAt(ua.toLocalDateTime());
        ub.setCurrentValue(rs.getInt("current_value"));
        ub.setVitrine(rs.getBoolean("is_vitrine"));

        Badge b = new Badge();
        b.setId(rs.getInt("badge_id"));
        b.setName(rs.getString("name"));
        b.setDescription(rs.getString("description"));
        b.setConditionType(rs.getString("condition_type"));
        b.setConditionValue(rs.getInt("condition_value"));
        b.setIcon(rs.getString("icon"));
        ub.setBadge(b);
        return ub;
    }
}
