package tn.esprit.projet.repository;

import tn.esprit.projet.models.Badge;
import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BadgeRepository — mirrors BadgeRepository.php + UserBadgeRepository.php
 * from the Symfony web app.
 */
public class BadgeRepository {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── findAllOrdered (by categorie ASC, ordre ASC) ───────────────────────────

    public List<Badge> findAllOrdered() {
        List<Badge> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM badge ORDER BY categorie ASC, ordre ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBadge(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── findByUser (with badge join) ───────────────────────────────────────────

    public List<UserBadge> findByUser(int userId) {
        List<UserBadge> list = new ArrayList<>();
        String sql = "SELECT ub.*, " +
                "b.nom, b.description, b.condition_text, b.condition_type, b.condition_value, " +
                "b.svg, b.couleur, b.couleur_bg, b.categorie, b.ordre, b.rarete " +
                "FROM user_badge ub JOIN badge b ON ub.badge_id=b.id " +
                "WHERE ub.user_id=? ORDER BY b.categorie ASC, b.ordre ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapUserBadge(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<UserBadge> findVitrineByUser(int userId) {
        List<UserBadge> list = new ArrayList<>();
        String sql = "SELECT ub.*, " +
                "b.nom, b.description, b.condition_text, b.condition_type, b.condition_value, " +
                "b.svg, b.couleur, b.couleur_bg, b.categorie, b.ordre, b.rarete " +
                "FROM user_badge ub JOIN badge b ON ub.badge_id=b.id " +
                "WHERE ub.user_id=? AND ub.is_vitrine=1 AND ub.unlocked=1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapUserBadge(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countVitrineByUser(int userId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM user_badge WHERE user_id=? AND is_vitrine=1 AND unlocked=1")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── ensureUserBadges ───────────────────────────────────────────────────────

    public void ensureUserBadges(int userId) {
        List<Badge> all = findAllOrdered();
        for (Badge b : all) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT IGNORE INTO user_badge (user_id,badge_id,unlocked,current_value,is_vitrine) " +
                    "VALUES (?,?,0,0,0)")) {
                ps.setInt(1, userId);
                ps.setInt(2, b.getId());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ── unlock ─────────────────────────────────────────────────────────────────

    public void unlock(int userBadgeId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE user_badge SET unlocked=1, unlocked_at=NOW() WHERE id=?")) {
            ps.setInt(1, userBadgeId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── updateProgress ─────────────────────────────────────────────────────────

    public void updateProgress(int userBadgeId, int value) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE user_badge SET current_value=? WHERE id=?")) {
            ps.setInt(1, value);
            ps.setInt(2, userBadgeId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // ── setVitrine (max 3) ─────────────────────────────────────────────────────

    public boolean setVitrine(int userBadgeId, int userId, boolean state) {
        if (state && countVitrineByUser(userId) >= 3) return false;
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE user_badge SET is_vitrine=? WHERE id=? AND user_id=?")) {
            ps.setBoolean(1, state);
            ps.setInt(2, userBadgeId);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── seedDefaultBadges — matches the Symfony web app badges ────────────────

    public void seedDefaultBadges() {
        // Check if already seeded with our new nutrition badges
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM badge WHERE condition_type='weight_logs'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) >= 3) return; // already seeded
        } catch (SQLException e) { /* proceed */ }

        // Clear old badges and start fresh
        try (Statement st = conn().createStatement()) {
            st.executeUpdate("DELETE FROM user_badge");
            st.executeUpdate("DELETE FROM badge");
        } catch (SQLException e) { e.printStackTrace(); }

        // ── Smart & Creative Nutrition & Fitness Badges ────────────────────────
        // nom, description, condition_text, condition_type, condition_value,
        // svg, couleur, couleur_bg, categorie, ordre, rarete
        Object[][] data = {

            // ── Getting Started ───────────────────────────────────────────────
            {"Welcome!",          "You joined NutriLife - Your journey begins!",  "Create your account",       "account_created",     1,  "🌟", "#7C3AED", "#EDE9FE", "Getting Started", 0, "common"},
            {"Profile Ready",     "Complete your profile with all details",       "Complete your profile",      "profile_complete",    1,  "✅", "#059669", "#D1FAE5", "Getting Started", 1, "common"},
            {"Face Unlocked",     "Secure your account with Face ID",             "Enroll Face ID",             "face_id_enrolled",    1,  "🔐", "#1D4ED8", "#DBEAFE", "Getting Started", 2, "rare"},
            {"Say Cheese!",       "Show the world your best smile",               "Upload a profile photo",     "photo_uploaded",      1,  "📸", "#D97706", "#FEF3C7", "Getting Started", 3, "common"},

            // ── Weight Tracking ───────────────────────────────────────────────
            {"First Weigh-In",    "Your first step towards transformation",       "Log 1 weight",               "weight_logs",         1,  "⚖️", "#2E7D32", "#F0FDF4", "Weight Tracking", 4, "common"},
            {"Consistent Tracker","Building healthy habits, one log at a time",   "Log 5 weights",              "weight_logs",         5,  "📊", "#15803D", "#DCFCE7", "Weight Tracking", 5, "common"},
            {"Dedicated Logger",  "Your commitment is showing results",           "Log 10 weights",             "weight_logs",        10,  "💪", "#166534", "#BBF7D0", "Weight Tracking", 6, "rare"},
            {"Weight Warrior",    "Master of self-monitoring and discipline",     "Log 30 weights",             "weight_logs",        30,  "🏋️", "#14532D", "#86EFAC", "Weight Tracking", 7, "epic"},
            {"Data Champion",     "50 weigh-ins! You're a tracking pro",          "Log 50 weights",             "weight_logs",        50,  "📈", "#065F46", "#6EE7B7", "Weight Tracking", 8, "legendary"},

            // ── Goals & Progress ──────────────────────────────────────────────
            {"Goal Setter",       "Every journey starts with a clear goal",       "Set a weight goal",          "objective_set",       1,  "🎯", "#B45309", "#FEF3C7", "Goals",          9, "common"},
            {"Halfway Hero",      "You're halfway there - don't stop now!",       "50% of goal achieved",       "objective_50pct",     1,  "🔥", "#EA580C", "#FFF7ED", "Goals",         10, "rare"},
            {"Goal Crusher",      "GOAL ACHIEVED! Time to celebrate!",            "Goal fully achieved!",       "objective_100pct",    1,  "🏆", "#DC2626", "#FEF2F2", "Goals",         11, "legendary"},
            {"3kg Milestone",     "Every kilogram counts - keep going!",          "3 kg progress",              "kg_progress",         3,  "📉", "#0891B2", "#ECFEFF", "Goals",         12, "common"},
            {"5kg Achiever",      "5kg down! Your body is transforming",          "5 kg progress",              "kg_progress",         5,  "🎖️", "#0E7490", "#CFFAFE", "Goals",         13, "common"},
            {"10kg Champion",     "10kg milestone! You're unstoppable",           "10 kg progress",             "kg_progress",        10,  "🥇", "#0C4A6E", "#A5F3FC", "Goals",         14, "epic"},
            {"20kg Legend",       "20kg! You've achieved the extraordinary",      "20 kg progress",             "kg_progress",        20,  "👑", "#155E75", "#7DD3FC", "Goals",         15, "legendary"},

            // ── Consistency & Habits ──────────────────────────────────────────
            {"On a Roll",         "3 days strong - momentum is building",         "3-day activity streak",      "streak_days",         3,  "🔥", "#DC2626", "#FEF2F2", "Consistency",   16, "common"},
            {"Week Warrior",      "7 days in a row - you're on fire!",            "7-day activity streak",      "streak_days",         7,  "⚡", "#B91C1C", "#FEE2E2", "Consistency",   17, "rare"},
            {"Two Week Titan",    "14 days! Habits are forming",                  "14-day activity streak",     "streak_days",        14,  "💥", "#991B1B", "#FECACA", "Consistency",   18, "rare"},
            {"Monthly Master",    "30 days! You've built a lifestyle",            "30-day activity streak",     "streak_days",        30,  "💎", "#7F1D1D", "#FCA5A5", "Consistency",   19, "legendary"},

            // ── Health & Wellness ─────────────────────────────────────────────
            {"Healthy BMI",       "Your BMI is in the healthy zone!",             "BMI in healthy range",       "bmi_normal",          1,  "💚", "#059669", "#D1FAE5", "Health",        20, "epic"},
            {"One Month In",      "30 days with NutriLife - you're committed",    "30 days since joining",      "account_age_days",   30,  "🎖️", "#7C3AED", "#EDE9FE", "Health",        21, "rare"},
            {"Veteran Member",    "90 days! You're a NutriLife veteran",          "90 days since joining",      "account_age_days",   90,  "🏅", "#6D28D9", "#DDD6FE", "Health",        22, "epic"},
            {"Lifestyle Legend",  "1 year! Health is your lifestyle now",         "365 days since joining",     "account_age_days",  365,  "🌟", "#5B21B6", "#C4B5FD", "Health",        23, "legendary"},

            // ── Engagement & Community ────────────────────────────────────────
            {"Arena Rookie",      "Earned your first 100 arena points",           "Earn 100 arena points",      "arena_points",      100,  "🎮", "#0891B2", "#CFFAFE", "Engagement",    24, "common"},
            {"Arena Warrior",     "500 arena points! You're competitive",         "Earn 500 arena points",      "arena_points",      500,  "⚔️", "#0E7490", "#A5F3FC", "Engagement",    25, "rare"},
            {"Arena Champion",    "1000 points! You dominate the arena",          "Earn 1000 arena points",     "arena_points",     1000,  "🏆", "#155E75", "#7DD3FC", "Engagement",    26, "epic"},
            {"Challenge Starter", "Completed your first weekly challenge",        "Complete 1 challenge",       "challenges_done",     1,  "🎯", "#16A34A", "#DCFCE7", "Engagement",    27, "common"},
            {"Challenge Master",  "Completed 10 weekly challenges",               "Complete 10 challenges",     "challenges_done",    10,  "🏅", "#15803D", "#BBF7D0", "Engagement",    28, "epic"},

            // ── Special Achievements ──────────────────────────────────────────
            {"Early Bird",        "Logged weight before 8 AM",                    "Log weight early morning",   "early_morning_log",   1,  "🌅", "#F59E0B", "#FEF3C7", "Special",       29, "rare"},
            {"Night Owl",         "Logged weight after 10 PM",                    "Log weight late night",      "late_night_log",      1,  "🦉", "#8B5CF6", "#EDE9FE", "Special",       30, "rare"},
            {"Perfect Week",      "Logged weight every day for a week",           "7 consecutive daily logs",   "perfect_week",        1,  "✨", "#EC4899", "#FCE7F3", "Special",       31, "epic"},
            {"Comeback Kid",      "Returned after 30 days of inactivity",         "Return after break",         "comeback",            1,  "🎊", "#10B981", "#D1FAE5", "Special",       32, "rare"},
        };

        for (Object[] row : data) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO badge (nom,description,condition_text,condition_type,condition_value," +
                    "svg,couleur,couleur_bg,categorie,ordre,rarete) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
                ps.setString(1,  (String)  row[0]);
                ps.setString(2,  (String)  row[1]);
                ps.setString(3,  (String)  row[2]);
                ps.setString(4,  (String)  row[3]);
                ps.setInt(5,     (Integer) row[4]);
                ps.setString(6,  (String)  row[5]);
                ps.setString(7,  (String)  row[6]);
                ps.setString(8,  (String)  row[7]);
                ps.setString(9,  (String)  row[8]);
                ps.setInt(10,    (Integer) row[9]);
                ps.setString(11, (String)  row[10]);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        System.out.println("[Badges] Seeded " + data.length + " badges.");
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private Badge mapBadge(ResultSet rs) throws SQLException {
        Badge b = new Badge();
        b.setId(rs.getInt("id"));
        b.setNom(safe(rs, "nom", safe(rs, "name", "Badge")));
        b.setDescription(safe(rs, "description", ""));
        b.setConditionText(safe(rs, "condition_text", ""));
        b.setConditionType(safe(rs, "condition_type", ""));
        b.setConditionValue(safeInt(rs, "condition_value", 1));
        b.setSvg(safe(rs, "svg", safe(rs, "icon", "🏅")));
        b.setCouleur(safe(rs, "couleur", "#2f9e44"));
        b.setCouleurBg(safe(rs, "couleur_bg", "#e8fbe8"));
        b.setCategorie(safe(rs, "categorie", ""));
        b.setOrdre(safeInt(rs, "ordre", 0));
        b.setRarete(safe(rs, "rarete", "common"));
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
        b.setNom(safe(rs, "nom", "Badge"));
        b.setDescription(safe(rs, "description", ""));
        b.setConditionText(safe(rs, "condition_text", ""));
        b.setConditionType(safe(rs, "condition_type", ""));
        b.setConditionValue(safeInt(rs, "condition_value", 1));
        b.setSvg(safe(rs, "svg", "🏅"));
        b.setCouleur(safe(rs, "couleur", "#2f9e44"));
        b.setCouleurBg(safe(rs, "couleur_bg", "#e8fbe8"));
        b.setCategorie(safe(rs, "categorie", ""));
        b.setOrdre(safeInt(rs, "ordre", 0));
        b.setRarete(safe(rs, "rarete", "common"));
        ub.setBadge(b);
        return ub;
    }

    private String safe(ResultSet rs, String col, String def) {
        try { String v = rs.getString(col); return v != null ? v : def; }
        catch (SQLException e) { return def; }
    }

    private int safeInt(ResultSet rs, String col, int def) {
        try { return rs.getInt(col); } catch (SQLException e) { return def; }
    }
}
