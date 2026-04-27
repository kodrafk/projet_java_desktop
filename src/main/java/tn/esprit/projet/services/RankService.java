package tn.esprit.projet.services;

import tn.esprit.projet.models.UserBadge;
import tn.esprit.projet.repository.BadgeRepository;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.util.List;

/**
 * RankService — XP system based on earned badges.
 * Each badge gives XP based on rarity.
 * XP accumulates to unlock ranks with motivating titles.
 */
public class RankService {

    private final BadgeRepository badgeRepo = new BadgeRepository();

    // ── XP per rarity ──────────────────────────────────────────────────────────
    public static int xpForRarity(String rarete) {
        if (rarete == null) return 10;
        return switch (rarete.toLowerCase()) {
            case "legendary" -> 100;
            case "epic"      -> 50;
            case "rare"      -> 25;
            default          -> 10; // common
        };
    }

    // ── Rank definitions ───────────────────────────────────────────────────────
    public static final Rank[] RANKS = {
        new Rank(0,    "🌱", "Beginner",   "#64748B", "#F1F5F9", "You're just getting started!"),
        new Rank(50,   "🔥", "Active",     "#EA580C", "#FFF7ED", "You're building momentum!"),
        new Rank(150,  "💪", "Committed",  "#16A34A", "#F0FDF4", "You're serious about your health!"),
        new Rank(300,  "🏃", "Athlete",    "#2563EB", "#EFF6FF", "You're pushing your limits!"),
        new Rank(600,  "🥇", "Champion",   "#D97706", "#FEF3C7", "You're an inspiration!"),
        new Rank(1000, "👑", "Legend",     "#7C3AED", "#EDE9FE", "You've mastered NutriLife!"),
    };

    // ── Calculate total XP for a user ─────────────────────────────────────────
    public int calculateXP(int userId) {
        List<UserBadge> badges = badgeRepo.findByUser(userId);
        int xp = 0;
        for (UserBadge ub : badges) {
            if (ub.isUnlocked()) {
                xp += xpForRarity(ub.getBadge().getRarete());
            }
        }
        return xp;
    }

    // ── Get current rank ───────────────────────────────────────────────────────
    public Rank getCurrentRank(int xp) {
        Rank current = RANKS[0];
        for (Rank r : RANKS) {
            if (xp >= r.xpRequired) current = r;
            else break;
        }
        return current;
    }

    // ── Get next rank ──────────────────────────────────────────────────────────
    public Rank getNextRank(int xp) {
        for (Rank r : RANKS) {
            if (xp < r.xpRequired) return r;
        }
        return null; // already at max rank
    }

    // ── Get full rank info ─────────────────────────────────────────────────────
    public RankInfo getRankInfo(int userId) {
        int xp = calculateXP(userId);
        Rank current = getCurrentRank(xp);
        Rank next    = getNextRank(xp);

        int xpInCurrentLevel = xp - current.xpRequired;
        int xpNeededForNext  = next != null ? next.xpRequired - current.xpRequired : 1;
        double progress      = next != null ? (double) xpInCurrentLevel / xpNeededForNext : 1.0;
        int xpToNext         = next != null ? next.xpRequired - xp : 0;

        return new RankInfo(xp, current, next, progress, xpToNext, xpInCurrentLevel, xpNeededForNext);
    }

    // ── Inner classes ──────────────────────────────────────────────────────────

    public static class Rank {
        public final int    xpRequired;
        public final String emoji, title, color, bgColor, motivation;

        public Rank(int xpRequired, String emoji, String title,
                    String color, String bgColor, String motivation) {
            this.xpRequired  = xpRequired;
            this.emoji       = emoji;
            this.title       = title;
            this.color       = color;
            this.bgColor     = bgColor;
            this.motivation  = motivation;
        }
    }

    public static class RankInfo {
        public final int    totalXP, xpToNext, xpInLevel, xpNeededForNext;
        public final Rank   currentRank, nextRank;
        public final double progress; // 0.0 to 1.0

        public RankInfo(int totalXP, Rank currentRank, Rank nextRank,
                        double progress, int xpToNext, int xpInLevel, int xpNeededForNext) {
            this.totalXP          = totalXP;
            this.currentRank      = currentRank;
            this.nextRank         = nextRank;
            this.progress         = progress;
            this.xpToNext         = xpToNext;
            this.xpInLevel        = xpInLevel;
            this.xpNeededForNext  = xpNeededForNext;
        }
    }
}
