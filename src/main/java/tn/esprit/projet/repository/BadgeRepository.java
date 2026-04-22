package tn.esprit.projet.repository;

import tn.esprit.projet.models.Badge;
import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BadgeRepository {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Badge> findAll() {
        List<Badge> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM badge ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBadge(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<UserBadge> findByUser(int userId) {
        List<UserBadge> list = new ArrayList<>();
        String sql = "SELECT ub.*, b.name, b.description, b.condition_type, b.condition_value, b.icon " +
                     "FROM user_badge ub JOIN badge b ON ub.badge_id=b.id WHERE ub.user_id=? ORDER BY b.id";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapUserBadge(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<UserBadge> findVitrineByUser(int userId) {
        List<UserBadge> list = new ArrayList<>();
        String sql = "SELECT ub.*, b.name, b.description, b.condition_type, b.condition_value, b.icon " +
                     "FROM user_badge ub JOIN badge b ON ub.badge_id=b.id " +
                     "WHERE ub.user_id=? AND ub.is_vitrine=1 AND ub.unlocked=1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapUserBadge(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void ensureUserBadges(int userId) {
        List<Badge> all = findAll();
        for (Badge b : all) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT IGNORE INTO user_badge (user_id,badge_id,unlocked,current_value,is_vitrine) VALUES (?,?,0,0,0)")) {
                ps.setInt(1, userId);
                ps.setInt(2, b.getId());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean setVitrine(int userBadgeId, int userId, boolean state) {
        if (state) {
            int count = 0;
            try (PreparedStatement ps = conn().prepareStatement(
                    "SELECT COUNT(*) FROM user_badge WHERE user_id=? AND is_vitrine=1 AND unlocked=1")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) count = rs.getInt(1);
            } catch (SQLException e) { e.printStackTrace(); }
            if (count >= 3) return false;
        }
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE user_badge SET is_vitrine=? WHERE id=? AND user_id=?")) {
            ps.setBoolean(1, state);
            ps.setInt(2, userBadgeId);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public void unlock(int userBadgeId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE user_badge SET unlocked=1,unlocked_at=NOW() WHERE id=?")) {
            ps.setInt(1, userBadgeId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateProgress(int userBadgeId, int value) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE user_badge SET current_value=? WHERE id=?")) {
            ps.setInt(1, value);
            ps.setInt(2, userBadgeId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void seedDefaultBadges() {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM badge");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException e) { e.printStackTrace(); return; }

        String[][] data = {
            {"🌱","First Steps",      "Log your first meal",           "meals",    "1"},
            {"🔥","On Fire",          "Log meals 7 days in a row",     "streak",   "7"},
            {"💪","Fitness Freak",    "Complete 10 workouts",          "workouts", "10"},
            {"🥗","Salad Lover",      "Log 20 healthy meals",          "healthy",  "20"},
            {"⚖", "Balance Master",  "Maintain BMI in normal range",  "bmi",      "30"},
            {"🏆","Champion",         "Reach 100 logged meals",        "meals",    "100"},
            {"🌟","Star User",        "Be active for 30 days",         "days",     "30"},
            {"🎯","Goal Crusher",     "Hit your calorie goal 5 times", "goals",    "5"},
        };
        for (String[] row : data) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO badge (icon,name,description,condition_type,condition_value) VALUES (?,?,?,?,?)")) {
                ps.setString(1, row[0]); ps.setString(2, row[1]);
                ps.setString(3, row[2]); ps.setString(4, row[3]);
                ps.setInt(5, Integer.parseInt(row[4]));
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private Badge mapBadge(ResultSet rs) throws SQLException {
        Badge b = new Badge();
        b.setId(rs.getInt("id"));
        b.setName(rs.getString("name"));
        b.setDescription(rs.getString("description"));
        b.setConditionType(rs.getString("condition_type"));
        b.setConditionValue(rs.getInt("condition_value"));
        b.setIcon(rs.getString("icon"));
        return b;
    }

    private UserBadge mapUserBadge(ResultSet rs) throws SQLException {
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
