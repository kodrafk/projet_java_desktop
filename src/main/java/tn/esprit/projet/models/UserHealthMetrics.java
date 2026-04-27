package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * Métriques de santé calculées pour l'analyse ML
 */
public class UserHealthMetrics {
    
    private int userId;
    private double currentWeight;
    private double weightChange7Days;      // Changement sur 7 jours
    private double weightChange30Days;     // Changement sur 30 jours
    private int daysSinceLastLog;          // Jours depuis dernier log
    private int totalLogs;                 // Nombre total de logs
    private double weightVariance;         // Variance du poids (yo-yo)
    private double avgWeeklyChange;        // Changement moyen par semaine
    private boolean hasActiveGoal;
    private double goalRealisticScore;     // Score 0-100 (100 = réaliste)
    private double abandonmentRisk;        // Score ML 0-100
    private double activityScore;          // Score d'activité 0-100
    private LocalDateTime calculatedAt;
    
    public UserHealthMetrics() {
        this.calculatedAt = LocalDateTime.now();
    }
    
    public UserHealthMetrics(int userId) {
        this.userId = userId;
        this.calculatedAt = LocalDateTime.now();
    }
    
    // Getters & Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(double currentWeight) { this.currentWeight = currentWeight; }
    
    public double getWeightChange7Days() { return weightChange7Days; }
    public void setWeightChange7Days(double weightChange7Days) { this.weightChange7Days = weightChange7Days; }
    
    public double getWeightChange30Days() { return weightChange30Days; }
    public void setWeightChange30Days(double weightChange30Days) { this.weightChange30Days = weightChange30Days; }
    
    public int getDaysSinceLastLog() { return daysSinceLastLog; }
    public void setDaysSinceLastLog(int daysSinceLastLog) { this.daysSinceLastLog = daysSinceLastLog; }
    
    public int getTotalLogs() { return totalLogs; }
    public void setTotalLogs(int totalLogs) { this.totalLogs = totalLogs; }
    
    public double getWeightVariance() { return weightVariance; }
    public void setWeightVariance(double weightVariance) { this.weightVariance = weightVariance; }
    
    public double getAvgWeeklyChange() { return avgWeeklyChange; }
    public void setAvgWeeklyChange(double avgWeeklyChange) { this.avgWeeklyChange = avgWeeklyChange; }
    
    public boolean isHasActiveGoal() { return hasActiveGoal; }
    public void setHasActiveGoal(boolean hasActiveGoal) { this.hasActiveGoal = hasActiveGoal; }
    
    public double getGoalRealisticScore() { return goalRealisticScore; }
    public void setGoalRealisticScore(double goalRealisticScore) { this.goalRealisticScore = goalRealisticScore; }
    
    public double getAbandonmentRisk() { return abandonmentRisk; }
    public void setAbandonmentRisk(double abandonmentRisk) { this.abandonmentRisk = abandonmentRisk; }
    
    public double getActivityScore() { return activityScore; }
    public void setActivityScore(double activityScore) { this.activityScore = activityScore; }
    
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    
    // Méthodes d'analyse
    public boolean hasRapidWeightLoss() {
        return weightChange7Days < -2.0;  // Plus de 2kg perdus en 7 jours
    }
    
    public boolean hasRapidWeightGain() {
        return weightChange7Days > 2.0;   // Plus de 2kg gagnés en 7 jours
    }
    
    public boolean isInactive() {
        return daysSinceLastLog > 14;     // Plus de 14 jours sans log
    }
    
    public boolean hasYoYoPattern() {
        return weightVariance > 3.0;      // Variance élevée
    }
    
    public boolean hasUnrealisticGoal() {
        return goalRealisticScore < 40;   // Score faible
    }
    
    public boolean isAtRiskOfAbandonment() {
        return abandonmentRisk > 60;      // Risque élevé
    }
    
    public String getHealthStatus() {
        if (abandonmentRisk > 70) return "CRITIQUE";
        if (hasRapidWeightLoss() || hasRapidWeightGain()) return "ATTENTION";
        if (isInactive()) return "INACTIF";
        if (activityScore > 70) return "EXCELLENT";
        if (activityScore > 40) return "BON";
        return "MOYEN";
    }
    
    @Override
    public String toString() {
        return String.format("UserHealthMetrics{userId=%d, weight=%.1f, change7d=%.2f, daysInactive=%d, abandonmentRisk=%.1f}",
                userId, currentWeight, weightChange7Days, daysSinceLastLog, abandonmentRisk);
    }
}
