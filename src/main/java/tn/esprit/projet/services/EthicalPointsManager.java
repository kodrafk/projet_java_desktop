package tn.esprit.projet.services;

import tn.esprit.projet.utils.MyBDConnexion;
import tn.esprit.projet.utils.SessionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class EthicalPointsManager {

    private static final int BADGE_FIRST_RECIPE    = 101;
    private static final int BADGE_FIRST_SCAN      = 102;
    private static final int BADGE_BOYCOTT_HERO    = 103;
    private static final int BADGE_MEAL_CHAMP      = 104;
    private static final int BADGE_MASTER_CHEF     = 105;
    private static final int BADGE_WEEKLY_WARRIOR  = 106;
    private static final int BADGE_INGREDIENT_MASTER = 107;
    private static final int BADGE_ETHICAL_GUARDIAN = 108;
    private static final int BADGE_LEGEND_CROWN    = 109;

    private static final int[] ALL_BADGE_IDS = {
        BADGE_FIRST_RECIPE, BADGE_FIRST_SCAN, BADGE_BOYCOTT_HERO,
        BADGE_MEAL_CHAMP, BADGE_MASTER_CHEF, BADGE_WEEKLY_WARRIOR,
        BADGE_INGREDIENT_MASTER, BADGE_ETHICAL_GUARDIAN, BADGE_LEGEND_CROWN
    };

    private static final Map<Integer, Integer> badgeProgress = new HashMap<>();
    private static final Map<Integer, Boolean> badgeUnlocked = new HashMap<>();
    private static boolean loaded = false;

    public static void loadFromDatabase() {
        if (loaded) return;
        try {
            var user = SessionManager.getCurrentUser();
            if (user == null) return;
            int userId = user.getId();
            Connection cnx = MyBDConnexion.getInstance().getCnx();

            // Check if user_badge table exists
            try (Statement st = cnx.createStatement()) {
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS `user_badge` (" +
                    "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "  `user_id` INT NOT NULL," +
                    "  `badge_id` INT NOT NULL," +
                    "  `unlocked` TINYINT DEFAULT 0," +
                    "  `current_value` INT DEFAULT 0," +
                    "  `is_vitrine` TINYINT DEFAULT 0," +
                    "  `unlocked_at` DATETIME," +
                    "  UNIQUE KEY `user_badge_unique` (`user_id`, `badge_id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
                );
            } catch (SQLException ignored) {}

            String initSql = "INSERT IGNORE INTO user_badge (user_id, badge_id, unlocked, current_value, is_vitrine) VALUES (?, ?, 0, 0, 0)";
            try (PreparedStatement ps = cnx.prepareStatement(initSql)) {
                for (int badgeId : ALL_BADGE_IDS) {
                    ps.setInt(1, userId); ps.setInt(2, badgeId); ps.addBatch();
                }
                ps.executeBatch();
            }

            String loadSql = "SELECT badge_id, current_value, unlocked FROM user_badge WHERE user_id = ? AND badge_id BETWEEN 101 AND 109";
            try (PreparedStatement ps = cnx.prepareStatement(loadSql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("badge_id");
                    badgeProgress.put(id, rs.getInt("current_value"));
                    badgeUnlocked.put(id, rs.getInt("unlocked") == 1);
                }
            }
            loaded = true;
        } catch (Exception e) {
            System.err.println("EthicalPointsManager.loadFromDatabase: " + e.getMessage());
        }
    }

    private static void saveBadgeToDb(int badgeId, int newValue, boolean unlocked) {
        try {
            var user = SessionManager.getCurrentUser();
            if (user == null) return;
            Connection cnx = MyBDConnexion.getInstance().getCnx();
            String sql = "UPDATE user_badge SET current_value=?, unlocked=?, unlocked_at=? WHERE user_id=? AND badge_id=?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, newValue); ps.setInt(2, unlocked ? 1 : 0);
                ps.setTimestamp(3, unlocked ? Timestamp.valueOf(LocalDateTime.now()) : null);
                ps.setInt(4, user.getId()); ps.setInt(5, badgeId);
                ps.executeUpdate();
            }
        } catch (Exception e) { System.err.println("saveBadgeToDb: " + e.getMessage()); }
    }

    private static void checkAndUnlock(int badgeId, int currentValue, int target) {
        boolean isNowUnlocked = currentValue >= target;
        badgeProgress.put(badgeId, currentValue);
        badgeUnlocked.put(badgeId, isNowUnlocked);
        saveBadgeToDb(badgeId, currentValue, isNowUnlocked);
    }

    public static int getTotalPoints() {
        int points = 0;
        try {
            var user = SessionManager.getCurrentUser();
            if (user == null) return 0;
            int userId = user.getId();
            Connection cnx = MyBDConnexion.getInstance().getCnx();
            try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM recette WHERE user_id=?")) {
                ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
                if (rs.next()) points += rs.getInt(1) * 10;
            }
            points += getScanCount() * 15;
            points += getBoycottRejectCount() * 20;
        } catch (Exception e) { System.err.println("getTotalPoints: " + e.getMessage()); }
        return points;
    }

    public static void incrementScanCount() { checkAndUnlock(BADGE_FIRST_SCAN, getScanCount() + 1, 1); }
    public static void incrementBoycottRejectCount() {
        int val = getBoycottRejectCount() + 1;
        checkAndUnlock(BADGE_BOYCOTT_HERO, val, 3);
        checkAndUnlock(BADGE_ETHICAL_GUARDIAN, val, 10);
    }
    public static void updateRecipeCount(int count) {
        checkAndUnlock(BADGE_FIRST_RECIPE, count, 1);
        checkAndUnlock(BADGE_MASTER_CHEF, count, 20);
    }
    public static void updateCompletedPlans(int count) {
        checkAndUnlock(BADGE_MEAL_CHAMP, count, 1);
        checkAndUnlock(BADGE_WEEKLY_WARRIOR, count, 1);
    }
    public static void updateIngredientCount(int count) { checkAndUnlock(BADGE_INGREDIENT_MASTER, count, 50); }
    public static void updateTotalPoints() { checkAndUnlock(BADGE_LEGEND_CROWN, getTotalPoints(), 500); }

    public static int getScanCount() { return badgeProgress.getOrDefault(BADGE_FIRST_SCAN, 0); }
    public static int getBoycottRejectCount() { return badgeProgress.getOrDefault(BADGE_BOYCOTT_HERO, 0); }
    public static int getCompletedMealPlanCount() { return badgeProgress.getOrDefault(BADGE_MEAL_CHAMP, 0); }

    public static boolean isLevel1Complete() {
        return isBadgeUnlocked(BADGE_FIRST_RECIPE) && isBadgeUnlocked(BADGE_FIRST_SCAN) && isBadgeUnlocked(BADGE_BOYCOTT_HERO);
    }
    public static boolean isLevel2Complete() {
        return isBadgeUnlocked(BADGE_MEAL_CHAMP) && isBadgeUnlocked(BADGE_MASTER_CHEF) && isBadgeUnlocked(BADGE_WEEKLY_WARRIOR);
    }
    public static int getCurrentLevel() {
        if (isLevel2Complete()) return 3;
        if (isLevel1Complete()) return 2;
        return 1;
    }
    public static String getLevelName() {
        int pts = getTotalPoints();
        if (pts >= 500) return "👑 Ethical Legend";
        if (pts >= 100) return "🌿 Ethical Cook";
        return "🌱 Beginner";
    }
    public static int getNextLevelTarget() {
        int pts = getTotalPoints();
        if (pts >= 500) return pts;
        if (pts >= 100) return 500;
        return 100;
    }
    public static boolean isBadgeUnlocked(int badgeId) { return badgeUnlocked.getOrDefault(badgeId, false); }
    public static int getBadgeProgress(int badgeId) { return badgeProgress.getOrDefault(badgeId, 0); }
    public static void reset() { badgeProgress.clear(); badgeUnlocked.clear(); loaded = false; }

    /** Generic addPoints method used by ScannerController */
    public static void addPoints(String reason, int points) {
        System.out.println("[EthicalPoints] +" + points + " pts for: " + reason);
        // Trigger relevant badge updates based on reason
        if (reason != null && reason.toLowerCase().contains("scan")) {
            incrementScanCount();
        } else if (reason != null && reason.toLowerCase().contains("boycott")) {
            incrementBoycottRejectCount();
        }
    }
}
