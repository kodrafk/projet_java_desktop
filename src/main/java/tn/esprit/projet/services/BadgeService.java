package tn.esprit.projet.services;

import tn.esprit.projet.models.User;
import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.repository.BadgeRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * BadgeService — mirrors BadgeService.php from the Symfony web app.
 *
 * Methods:
 * - calculateCurrentValue(user, conditionType) → int
 * - refreshBadges(user) → List<String> newly unlocked
 * - getBadgesForDisplay(user) → BadgesDisplay (unlocked/inProgress/locked/stats)
 * - spinRoue(userId) → SpinResult (3 spins/day, weighted random)
 * - toggleVitrine(userBadgeId, userId) → VitrineResult (max 3)
 * - getArenaStats(userId) → ArenaStats (niveau, XP, streak, points, badgesCount)
 */
public class BadgeService {

    private final BadgeRepository badgeRepo = new BadgeRepository();
    private final UserRepository  userRepo  = new UserRepository();

    // In-memory session state (per user, per app session)
    private static final Map<Integer, Integer>  arenaPoints = new HashMap<>();
    private static final Map<Integer, SpinState> spinStates = new HashMap<>();

    // ── Spin wheel segments (exact same as Symfony) ────────────────────────────
    public static final SpinSegment[] SEGMENTS = {
        new SpinSegment("+10 pts",          "points",     10, 35),
        new SpinSegment("Smart Sort",        "jeu",         0,  8),
        new SpinSegment("+25 pts",          "points",     25, 25),
        new SpinSegment("×2 Points",        "multiplier",  2, 10),
        new SpinSegment("+50 pts",          "points",     50, 15),
        new SpinSegment("Save Ingredient",  "jeu",         0,  5),
        new SpinSegment("Surprise Challenge","defi",        0,  2),
    };

    // ── calculateCurrentValue ──────────────────────────────────────────────────

    public int calculateCurrentValue(User user, String conditionType) {
        if (conditionType == null) return 0;
        return switch (conditionType) {
            // Core tracking
            case "weight_logs"         -> countWeightLogs(user.getId());
            case "streak_days"         -> calculateStreak(user.getId());
            
            // Profile & setup
            case "account_created"     -> 1;
            case "photo_uploaded"      -> (user.getPhotoFilename() != null && !user.getPhotoFilename().isBlank()) ? 1 : 0;
            case "profile_complete"    -> isProfileComplete(user) ? 1 : 0;
            case "face_id_enrolled"    -> user.hasFaceId() ? 1 : 0;
            
            // Goals & progress
            case "objective_set"       -> countObjectives(user.getId()) > 0 ? 1 : 0;
            case "kg_progress"         -> (int) Math.floor(getKgProgress(user.getId(), user.getWeight()));
            case "objective_50pct"     -> getObjectiveProgress(user.getId(), user.getWeight()) >= 0.5 ? 1 : 0;
            case "objective_100pct"    -> getObjectiveProgress(user.getId(), user.getWeight()) >= 1.0 ? 1 : 0;
            
            // Health metrics
            case "bmi_normal"          -> { double b = user.getBmi(); yield (b >= 18.5 && b < 25.0) ? 1 : 0; }
            case "account_age_days"    -> (int) ChronoUnit.DAYS.between(
                    user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate() : LocalDate.now(),
                    LocalDate.now());
            
            // Engagement
            case "arena_points"        -> getArenaPoints(user.getId());
            case "challenges_done"     -> countChallengesCompleted(user.getId());
            
            // Special achievements
            case "early_morning_log"   -> hasEarlyMorningLog(user.getId()) ? 1 : 0;
            case "late_night_log"      -> hasLateNightLog(user.getId()) ? 1 : 0;
            case "perfect_week"        -> hasPerfectWeek(user.getId()) ? 1 : 0;
            case "comeback"            -> hasComeback(user.getId()) ? 1 : 0;
            
            default -> 0;
        };
    }

    // ── refreshBadges ──────────────────────────────────────────────────────────

    public List<String> refreshBadges(User user) {
        List<String> newlyUnlocked = new ArrayList<>();
        badgeRepo.seedDefaultBadges();
        badgeRepo.ensureUserBadges(user.getId());

        List<UserBadge> badges = badgeRepo.findByUser(user.getId());
        for (UserBadge ub : badges) {
            if (ub.isUnlocked()) continue;
            int current = calculateCurrentValue(user, ub.getBadge().getConditionType());
            if (current != ub.getCurrentValue()) {
                badgeRepo.updateProgress(ub.getId(), current);
            }
            if (current >= ub.getBadge().getConditionValue()) {
                badgeRepo.unlock(ub.getId());
                newlyUnlocked.add(ub.getBadge().getSvg() + " " + ub.getBadge().getNom());
            }
        }
        return newlyUnlocked;
    }

    // ── getBadgesForDisplay ────────────────────────────────────────────────────

    public BadgesDisplay getBadgesForDisplay(User user) {
        List<UserBadge> all = badgeRepo.findByUser(user.getId());
        List<UserBadge> unlocked   = new ArrayList<>();
        List<UserBadge> inProgress = new ArrayList<>();
        List<UserBadge> locked     = new ArrayList<>();

        for (UserBadge ub : all) {
            if (ub.isUnlocked()) {
                unlocked.add(ub);
            } else if (ub.getCurrentValue() > 0) {
                inProgress.add(ub);
            } else {
                locked.add(ub);
            }
        }

        // Sort: unlocked by date DESC, inProgress by progression DESC, locked by ordre
        unlocked.sort((a, b) -> {
            if (a.getUnlockedAt() == null) return 1;
            if (b.getUnlockedAt() == null) return -1;
            return b.getUnlockedAt().compareTo(a.getUnlockedAt());
        });
        inProgress.sort((a, b) -> b.getProgression() - a.getProgression());
        locked.sort(Comparator.comparingInt(ub -> ub.getBadge().getOrdre()));

        int total   = all.size();
        int unlockedCount = unlocked.size();
        int percent = total > 0 ? (int) Math.round(unlockedCount * 100.0 / total) : 0;

        return new BadgesDisplay(unlocked, inProgress, locked, total, unlockedCount, percent);
    }

    // ── spinRoue (3 spins/day, weighted random) ────────────────────────────────

    public SpinResult spinRoue(int userId) {
        SpinState state = spinStates.computeIfAbsent(userId, k -> new SpinState());
        String today = LocalDate.now().toString();
        if (!today.equals(state.date)) { state.date = today; state.count = 0; }

        if (state.count >= 3) {
            return new SpinResult(false, "You have already used your 3 spins today!", null, 0, getArenaPoints(userId));
        }

        // Weighted random — exact same algorithm as Symfony
        int rand = new Random().nextInt(100) + 1;
        int cumul = 0;
        SpinSegment result = SEGMENTS[SEGMENTS.length - 1];
        for (SpinSegment seg : SEGMENTS) {
            cumul += seg.prob;
            if (rand <= cumul) { result = seg; break; }
        }

        state.count++;

        int totalPoints = getArenaPoints(userId);
        if ("points".equals(result.type)) {
            totalPoints += result.valeur;
            arenaPoints.put(userId, totalPoints);
        } else if ("multiplier".equals(result.type)) {
            totalPoints *= result.valeur;
            arenaPoints.put(userId, totalPoints);
        }

        String message = switch (result.type) {
            case "points"     -> "🎉 Congrats! You earn " + result.valeur + " points!";
            case "multiplier" -> "🔥 Amazing! Your points are ×" + result.valeur + "!";
            case "jeu"        -> "🎮 Mini-game: " + result.label + "!";
            case "defi"       -> "⚡ Surprise Challenge! Take on today's challenge!";
            default           -> "🎁 Good luck!";
        };

        return new SpinResult(true, message, result, result.valeur, totalPoints);
    }

    // ── toggleVitrine ──────────────────────────────────────────────────────────

    public VitrineResult toggleVitrine(int userBadgeId, int userId) {
        List<UserBadge> all = badgeRepo.findByUser(userId);
        UserBadge target = all.stream().filter(ub -> ub.getId() == userBadgeId).findFirst().orElse(null);
        if (target == null || !target.isUnlocked())
            return new VitrineResult(false, "Badge not found or not unlocked.", false);

        if (target.isVitrine()) {
            badgeRepo.setVitrine(userBadgeId, userId, false);
            return new VitrineResult(true, "Badge removed from showcase.", false);
        }
        boolean ok = badgeRepo.setVitrine(userBadgeId, userId, true);
        if (!ok) return new VitrineResult(false, "You already have 3 badges in your showcase. Remove one first.", false);
        return new VitrineResult(true, "Badge added to showcase!", true);
    }

    // ── getArenaStats ──────────────────────────────────────────────────────────

    public ArenaStats getArenaStats(User user) {
        int points      = getArenaPoints(user.getId());
        int streak      = calculateStreak(user.getId());
        int niveau      = Math.max(1, points / 100 + 1);
        int xpCurrent   = points % 100;
        int xpNext      = 100;
        int xpPct       = (int) Math.round(xpCurrent * 100.0 / xpNext);
        int badgesCount = (int) badgeRepo.findByUser(user.getId()).stream().filter(UserBadge::isUnlocked).count();
        return new ArenaStats(points, streak, niveau, xpCurrent, xpNext, xpPct, badgesCount);
    }

    // ── DB helpers ─────────────────────────────────────────────────────────────

    /** Streak = consecutive days with weight logs */
    public int calculateStreak(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT DISTINCT DATE(logged_at) as d FROM weight_log WHERE user_id=? ORDER BY d DESC")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            List<LocalDate> dates = new ArrayList<>();
            while (rs.next()) {
                java.sql.Date d = rs.getDate("d");
                if (d != null) dates.add(d.toLocalDate());
            }
            if (dates.isEmpty()) return 0;
            int streak = 1;
            for (int i = 0; i < dates.size() - 1; i++) {
                if (ChronoUnit.DAYS.between(dates.get(i + 1), dates.get(i)) == 1) streak++;
                else break;
            }
            return streak;
        } catch (SQLException e) { return 0; }
    }

    private boolean isProfileComplete(User user) {
        return user.getFirstName() != null && !user.getFirstName().isBlank()
            && user.getLastName()  != null && !user.getLastName().isBlank()
            && user.getBirthday()  != null
            && user.getWeight()    > 0
            && user.getHeight()    > 0
            && user.getPhotoFilename() != null && !user.getPhotoFilename().isBlank();
    }

    private int getArenaPoints(int userId) { return arenaPoints.getOrDefault(userId, 0); }

    private int countObjectives(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT COUNT(*) FROM weight_objective WHERE user_id=?")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* table may not exist */ }
        return 0;
    }

    private int countWeightLogs(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT COUNT(*) FROM weight_log WHERE user_id=?")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* table may not exist */ }
        return 0;
    }

    private int countChallengesCompleted(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT COUNT(*) FROM user_challenge WHERE user_id=? AND completed=1")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* table may not exist */ }
        return 0;
    }

    private boolean hasEarlyMorningLog(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT COUNT(*) FROM weight_log WHERE user_id=? AND HOUR(logged_at) < 8")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
        return false;
    }

    private boolean hasLateNightLog(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT COUNT(*) FROM weight_log WHERE user_id=? AND HOUR(logged_at) >= 22")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
        return false;
    }

    private boolean hasPerfectWeek(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT DATE(logged_at) as d FROM weight_log WHERE user_id=? " +
                "AND logged_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) GROUP BY d")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) count++;
            return count >= 7;
        } catch (SQLException e) { return false; }
    }

    private boolean hasComeback(int userId) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT MAX(logged_at) as last_log FROM weight_log WHERE user_id=? " +
                "AND logged_at < DATE_SUB(NOW(), INTERVAL 30 DAY)")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getTimestamp("last_log") != null) {
                // Check if there's a recent log after the gap
                try (PreparedStatement ps2 = DatabaseConnection.getInstance().getConnection().prepareStatement(
                        "SELECT COUNT(*) FROM weight_log WHERE user_id=? AND logged_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)")) {
                    ps2.setInt(1, userId);
                    ResultSet rs2 = ps2.executeQuery();
                    if (rs2.next()) return rs2.getInt(1) > 0;
                }
            }
        } catch (SQLException e) { return false; }
        return false;
    }

    private double getKgProgress(int userId, double currentWeight) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT start_weight, target_weight FROM weight_objective WHERE user_id=? AND is_active=1 LIMIT 1")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double start = rs.getDouble("start_weight"), target = rs.getDouble("target_weight");
                return target < start ? Math.max(0, start - currentWeight) : Math.max(0, currentWeight - start);
            }
        } catch (SQLException e) { /* table may not exist */ }
        return 0;
    }

    private double getObjectiveProgress(int userId, double currentWeight) {
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(
                "SELECT start_weight, target_weight FROM weight_objective WHERE user_id=? AND is_active=1 LIMIT 1")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double start = rs.getDouble("start_weight"), target = rs.getDouble("target_weight");
                if (start == target) return 1.0;
                return Math.min(1.0, Math.max(0.0, (start - currentWeight) / (start - target)));
            }
        } catch (SQLException e) { /* table may not exist */ }
        return 0;
    }

    // ── Inner classes ──────────────────────────────────────────────────────────

    public static class SpinSegment {
        public final String label, type; public final int valeur, prob;
        public SpinSegment(String label, String type, int valeur, int prob) {
            this.label = label; this.type = type; this.valeur = valeur; this.prob = prob;
        }
    }

    public static class SpinResult {
        public final boolean success; public final String message;
        public final SpinSegment segment; public final int pointsGagnes, totalPoints;
        public SpinResult(boolean success, String message, SpinSegment segment, int pointsGagnes, int totalPoints) {
            this.success = success; this.message = message; this.segment = segment;
            this.pointsGagnes = pointsGagnes; this.totalPoints = totalPoints;
        }
    }

    public static class SpinState { public String date = ""; public int count = 0; }

    public static class ArenaStats {
        public final int points, streak, niveau, xpCurrent, xpNext, xpPct, badgesCount;
        public ArenaStats(int points, int streak, int niveau, int xpCurrent, int xpNext, int xpPct, int badgesCount) {
            this.points = points; this.streak = streak; this.niveau = niveau;
            this.xpCurrent = xpCurrent; this.xpNext = xpNext; this.xpPct = xpPct; this.badgesCount = badgesCount;
        }
    }

    public static class VitrineResult {
        public final boolean success, isVitrine; public final String message;
        public VitrineResult(boolean success, String message, boolean isVitrine) {
            this.success = success; this.message = message; this.isVitrine = isVitrine;
        }
    }

    public static class BadgesDisplay {
        public final List<UserBadge> unlocked, inProgress, locked;
        public final int total, unlockedCount, percent;
        public BadgesDisplay(List<UserBadge> unlocked, List<UserBadge> inProgress, List<UserBadge> locked,
                             int total, int unlockedCount, int percent) {
            this.unlocked = unlocked; this.inProgress = inProgress; this.locked = locked;
            this.total = total; this.unlockedCount = unlockedCount; this.percent = percent;
        }
    }
}
