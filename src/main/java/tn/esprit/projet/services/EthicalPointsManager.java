package tn.esprit.projet.services;

import java.util.ArrayList;
import java.util.List;

public class EthicalPointsManager {

    private static int totalPoints = 0;
    private static final List<String> history = new ArrayList<>();

    // ═══════════ COMPTEURS BADGES ═══════════
    private static int scanCount = 0;
    private static int boycottRejectCount = 0;

    // =========================
    // POINTS
    // =========================
    public static void addPoints(String action, int points) {
        totalPoints += points;
        history.add(action + " +" + points + " pts");
    }

    public static int getTotalPoints() { return totalPoints; }
    public static List<String> getHistory() { return history; }

    // =========================
    // COMPTEURS
    // =========================
    public static void incrementScanCount() { scanCount++; }
    public static void incrementBoycottRejectCount() { boycottRejectCount++; }

    public static int getScanCount() { return scanCount; }
    public static int getBoycottRejectCount() { return boycottRejectCount; }

    // =========================
    // NIVEAU
    // =========================
    public static String getLevelName() {
        if (totalPoints < 100) return "🌱 Beginner";
        if (totalPoints < 300) return "🌿 Responsible Cook";
        if (totalPoints < 500) return "🌳 Ethical Cook";
        if (totalPoints < 1000) return "🌲 Palestine Champion";
        return "👑 Ethical Legend";
    }

    public static int getProgressToNextLevel() {
        int max;
        if (totalPoints < 100) max = 100;
        else if (totalPoints < 300) max = 300;
        else if (totalPoints < 500) max = 500;
        else if (totalPoints < 1000) max = 1000;
        else max = 1500;
        return (int)((totalPoints / (double) max) * 100);
    }

    public static void reset() {
        totalPoints = 0;
        history.clear();
        scanCount = 0;
        boycottRejectCount = 0;
    }
}