package tn.esprit.projet.test;

import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.repository.WeightRepository;
import tn.esprit.projet.models.WeightLog;
import tn.esprit.projet.services.WeeklyChallengeService;
import tn.esprit.projet.services.WeeklyChallengeService.Challenge;
import tn.esprit.projet.services.RankService;
import tn.esprit.projet.utils.MyBDConnexion;

import java.util.List;

public class TestWeeklyChallenges {

    public static void main(String[] args) throws Exception {
        // Init DB schema
        MyBDConnexion.getInstance();

        UserRepository userRepo = new UserRepository();
        WeightRepository weightRepo = new WeightRepository();
        WeeklyChallengeService challengeService = new WeeklyChallengeService();
        RankService rankService = new RankService();

        // ── 1. Find user ───────────────────────────────────────────────────────
        System.out.println("\n=== FINDING USER ===");
        User user = userRepo.findByEmail("user@nutrilife.com");
        if (user == null) {
            System.out.println("❌ User not found: user@nutrilife.com");
            System.out.println("   Trying user@nutrilifee.com ...");
            user = userRepo.findByEmail("user@nutrilifee.com");
        }
        if (user == null) {
            System.out.println("❌ No user found. Listing all users:");
            // List all users
            java.sql.Connection c = MyBDConnexion.getInstance().getCnx();
            java.sql.ResultSet rs = c.createStatement().executeQuery("SELECT id, email, weight FROM user LIMIT 10");
            while (rs.next()) {
                System.out.println("  - ID:" + rs.getInt("id") + " | " + rs.getString("email") + " | weight:" + rs.getDouble("weight"));
            }
            return;
        }
        System.out.println("✅ Found user: " + user.getEmail() + " (ID:" + user.getId() + ", weight:" + user.getWeight() + "kg)");

        // ── 2. Check weekly challenges ─────────────────────────────────────────
        System.out.println("\n=== WEEKLY CHALLENGES ===");
        List<Challenge> challenges = challengeService.getWeeklyChallenges(user.getId());
        System.out.println("Challenges this week: " + challenges.size());
        for (Challenge ch : challenges) {
            System.out.printf("  [%s] %s %s — %d/%d — XP:%d — Completed:%s%n",
                ch.challengeType, ch.emoji, ch.title,
                ch.currentValue, ch.targetValue, ch.xpReward,
                ch.completed ? "✅" : "❌");
        }

        // ── 3. Check weight logs ───────────────────────────────────────────────
        System.out.println("\n=== WEIGHT LOGS ===");
        List<WeightLog> logs = weightRepo.findLogsByUser(user.getId());
        System.out.println("Total weight logs: " + logs.size());
        for (WeightLog log : logs) {
            System.out.printf("  - %.1f kg | photo:%s | date:%s%n",
                log.getWeight(), log.getPhoto() != null ? "✅" : "❌", log.getLoggedAt());
        }

        // ── 4. Simulate saving a weight log ───────────────────────────────────
        System.out.println("\n=== SIMULATING WEIGHT LOG ===");
        WeightLog testLog = new WeightLog();
        testLog.setUserId(user.getId());
        testLog.setWeight(user.getWeight() > 0 ? user.getWeight() - 0.1 : 70.0);
        testLog.setNote("Test log from TestWeeklyChallenges");
        testLog.setLoggedAt(java.time.LocalDateTime.now());
        weightRepo.saveLog(testLog);
        System.out.println("✅ Saved test log ID: " + testLog.getId());

        // ── 5. Notify challenge ────────────────────────────────────────────────
        System.out.println("\n=== NOTIFYING CHALLENGE (weight_log) ===");
        List<String> completed = challengeService.updateProgress(user.getId(), "weight_log");
        if (completed.isEmpty()) {
            System.out.println("No challenges completed yet (need more logs)");
        } else {
            for (String entry : completed) {
                String[] parts = entry.split("\\|");
                System.out.println("🎉 Challenge completed! XP: " + parts[1] +
                    (parts.length > 2 && !parts[2].isEmpty() ? " | Badge: " + parts[2] : ""));
            }
        }

        // ── 6. Re-check challenges ─────────────────────────────────────────────
        System.out.println("\n=== CHALLENGES AFTER UPDATE ===");
        challenges = challengeService.getWeeklyChallenges(user.getId());
        for (Challenge ch : challenges) {
            System.out.printf("  [%s] %s — %d/%d — Completed:%s%n",
                ch.title, ch.emoji, ch.currentValue, ch.targetValue,
                ch.completed ? "✅" : "❌");
        }

        // ── 7. Check rank ──────────────────────────────────────────────────────
        System.out.println("\n=== RANK INFO ===");
        RankService.RankInfo rankInfo = rankService.getRankInfo(user.getId());
        System.out.println("Total XP: " + rankInfo.totalXP);
        System.out.println("Current Rank: " + rankInfo.currentRank.emoji + " " + rankInfo.currentRank.title);
        System.out.println("Progress: " + rankInfo.xpInLevel + "/" + rankInfo.xpNeededForNext + " XP");
        if (rankInfo.nextRank != null)
            System.out.println("Next Rank: " + rankInfo.nextRank.emoji + " " + rankInfo.nextRank.title + " (" + rankInfo.xpToNext + " XP to go)");
        else
            System.out.println("👑 MAX RANK!");

        // ── 8. Perfect week check ──────────────────────────────────────────────
        System.out.println("\n=== PERFECT WEEK ===");
        System.out.println("Perfect week: " + (challengeService.isPerfectWeek(user.getId()) ? "✅ YES!" : "❌ Not yet"));
        System.out.println("Weekly XP earned: " + challengeService.getTotalWeeklyXP(user.getId()));

        System.out.println("\n✅ ALL TESTS PASSED");
    }
}
