package tn.esprit.projet.services;

import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * WeeklyChallengeService — generates 3 weekly challenges per user.
 * Challenges reset every Monday.
 * Completing all 3 gives a "Perfect Week" bonus.
 */
public class WeeklyChallengeService {

    // ── Challenge definitions ──────────────────────────────────────────────────
    public static final ChallengeTemplate[] TEMPLATES = {
        new ChallengeTemplate("weight_logs_week",   "⚖️", "Weigh-In Warrior",
            "Log your weight 3 times this week",
            "weight_log", 3, 50, "Weekly Tracker"),

        new ChallengeTemplate("recipe_week",        "🍽️", "Home Chef Week",
            "Create 2 new recipes this week",
            "recipe", 2, 30, null),

        new ChallengeTemplate("photo_week",         "📸", "Transformation Shot",
            "Add a progress photo with your weight log",
            "weight_photo", 1, 20, "Transformation"),

        new ChallengeTemplate("weight_logs_5",      "💪", "Consistency King",
            "Log your weight 5 times this week",
            "weight_log", 5, 80, "Dedicated"),

        new ChallengeTemplate("recipe_3",           "👨‍🍳", "Recipe Master",
            "Create 3 recipes this week",
            "recipe", 3, 60, null),

        new ChallengeTemplate("weight_goal_set",    "🎯", "Goal Setter",
            "Set or update your weight goal this week",
            "objective", 1, 40, "Goal Setter"),
    };

    // ── Ensure weekly_challenge table exists ───────────────────────────────────
    public void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `weekly_challenge` (" +
            "`id` INT AUTO_INCREMENT PRIMARY KEY," +
            "`user_id` INT NOT NULL," +
            "`week_start` DATE NOT NULL," +
            "`challenge_type` VARCHAR(50) NOT NULL," +
            "`current_value` INT NOT NULL DEFAULT 0," +
            "`target_value` INT NOT NULL," +
            "`completed` TINYINT(1) NOT NULL DEFAULT 0," +
            "`completed_at` DATETIME DEFAULT NULL," +
            "`xp_reward` INT NOT NULL DEFAULT 0," +
            "`badge_reward` VARCHAR(100) DEFAULT NULL," +
            "UNIQUE KEY `uk_user_week_type` (`user_id`,`week_start`,`challenge_type`)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Connection c = freshConn(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("[WeeklyChallenge] ERROR creating table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Get or create this week's challenges ───────────────────────────────────
    public List<Challenge> getWeeklyChallenges(int userId) {
        ensureTable();
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Seed 3 challenges for this week if not yet created
        seedWeeklyChallenges(userId, weekStart);

        // Load them
        List<Challenge> list = new ArrayList<>();
        try (Connection c = freshConn();
             PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM weekly_challenge WHERE user_id=? AND week_start=? ORDER BY id ASC")) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(weekStart));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapChallenge(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private void seedWeeklyChallenges(int userId, LocalDate weekStart) {
        // Check if already seeded
        try (Connection c = freshConn();
             PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM weekly_challenge WHERE user_id=? AND week_start=?")) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(weekStart));
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) >= 3) return; // already seeded
        } catch (SQLException e) { e.printStackTrace(); }

        // Pick 3 challenges based on week number (deterministic rotation)
        int weekNum = (int)(weekStart.toEpochDay() / 7);
        ChallengeTemplate[] picks = {
            TEMPLATES[weekNum % 3],
            TEMPLATES[(weekNum % 3) + 3 < TEMPLATES.length ? (weekNum % 3) + 3 : (weekNum % 3)],
            TEMPLATES[2]
        };
        // Always include photo challenge as 3rd
        picks[0] = TEMPLATES[0]; // weight logs
        picks[1] = TEMPLATES[1]; // recipe
        picks[2] = TEMPLATES[2]; // photo

        for (ChallengeTemplate t : picks) {
            try (Connection c = freshConn();
                 PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO weekly_challenge " +
                    "(user_id,week_start,challenge_type,current_value,target_value,xp_reward,badge_reward) " +
                    "VALUES (?,?,?,0,?,?,?)")) {
                ps.setInt(1, userId);
                ps.setDate(2, Date.valueOf(weekStart));
                ps.setString(3, t.type);
                ps.setInt(4, t.target);
                ps.setInt(5, t.xpReward);
                ps.setString(6, t.badgeReward);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ── Update progress ────────────────────────────────────────────────────────
    public List<String> updateProgress(int userId, String eventType) {
        ensureTable();
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<String> completed = new ArrayList<>();

        try (Connection c = freshConn();
             PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM weekly_challenge WHERE user_id=? AND week_start=? AND completed=0")) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(weekStart));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Challenge ch = mapChallenge(rs);
                String type = ch.challengeType;

                boolean matches = switch (eventType) {
                    case "weight_log"   -> type.equals("weight_logs_week") || type.equals("weight_logs_5");
                    case "weight_photo" -> type.equals("photo_week");
                    case "recipe"       -> type.equals("recipe_week") || type.equals("recipe_3");
                    case "objective"    -> type.equals("weight_goal_set");
                    default -> false;
                };

                if (!matches) continue;

                int newVal = ch.currentValue + 1;
                if (newVal >= ch.targetValue) {
                    // Complete!
                    try (Connection c2 = freshConn();
                         PreparedStatement ps2 = c2.prepareStatement(
                            "UPDATE weekly_challenge SET current_value=?,completed=1,completed_at=NOW() WHERE id=?")) {
                        ps2.setInt(1, newVal);
                        ps2.setInt(2, ch.id);
                        ps2.executeUpdate();
                    }
                    completed.add(ch.id + "|" + ch.xpReward + "|" + (ch.badgeReward != null ? ch.badgeReward : ""));
                } else {
                    try (Connection c2 = freshConn();
                         PreparedStatement ps2 = c2.prepareStatement(
                            "UPDATE weekly_challenge SET current_value=? WHERE id=?")) {
                        ps2.setInt(1, newVal);
                        ps2.setInt(2, ch.id);
                        ps2.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }

        return completed;
    }

    // ── Check if perfect week ──────────────────────────────────────────────────
    public boolean isPerfectWeek(int userId) {
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        try (Connection c = freshConn();
             PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM weekly_challenge WHERE user_id=? AND week_start=? AND completed=0")) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(weekStart));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) == 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Get total XP earned from weekly challenges ─────────────────────────────
    public int getTotalWeeklyXP(int userId) {
        try (Connection c = freshConn();
             PreparedStatement ps = c.prepareStatement(
                "SELECT SUM(xp_reward) FROM weekly_challenge WHERE user_id=? AND completed=1")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Mappers ────────────────────────────────────────────────────────────────
    private Challenge mapChallenge(ResultSet rs) throws SQLException {
        Challenge c = new Challenge();
        c.id            = rs.getInt("id");
        c.userId        = rs.getInt("user_id");
        c.weekStart     = rs.getDate("week_start").toLocalDate();
        c.challengeType = rs.getString("challenge_type");
        c.currentValue  = rs.getInt("current_value");
        c.targetValue   = rs.getInt("target_value");
        c.completed     = rs.getBoolean("completed");
        c.xpReward      = rs.getInt("xp_reward");
        c.badgeReward   = rs.getString("badge_reward");
        Timestamp ca = rs.getTimestamp("completed_at");
        if (ca != null) c.completedAt = ca.toLocalDateTime();

        // Find template for display info
        for (ChallengeTemplate t : TEMPLATES) {
            if (t.type.equals(c.challengeType)) {
                c.emoji       = t.emoji;
                c.title       = t.title;
                c.description = t.description;
                break;
            }
        }
        return c;
    }

    private Connection conn() { return DatabaseConnection.getInstance().getConnection(); }

    private Connection freshConn() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection c = java.sql.DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/nutrilife_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                tn.esprit.projet.utils.DatabaseConfig.USER,
                tn.esprit.projet.utils.DatabaseConfig.PASSWORD);
            c.setAutoCommit(true);
            return c;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // ── Inner classes ──────────────────────────────────────────────────────────

    public static class Challenge {
        public int    id, userId, currentValue, targetValue, xpReward;
        public String challengeType, emoji, title, description, badgeReward;
        public boolean completed;
        public java.time.LocalDate weekStart;
        public java.time.LocalDateTime completedAt;

        public double getProgress() {
            return targetValue > 0 ? Math.min(1.0, (double) currentValue / targetValue) : 0;
        }
    }

    public static class ChallengeTemplate {
        public final String type, emoji, title, description, eventType, badgeReward;
        public final int target, xpReward;

        public ChallengeTemplate(String type, String emoji, String title, String description,
                                  String eventType, int target, int xpReward, String badgeReward) {
            this.type = type; this.emoji = emoji; this.title = title;
            this.description = description; this.eventType = eventType;
            this.target = target; this.xpReward = xpReward; this.badgeReward = badgeReward;
        }
    }
}
