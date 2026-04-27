package tn.esprit.projet.models;

import java.time.LocalDate;

public class NutritionObjective {

    private int id;
    private String title;
    private String description;
    private String goalType;
    private String planLevel;
    private int targetCalories;
    private double targetProtein;
    private double targetCarbs;
    private double targetFats;
    private double targetWater;
    private String status; // pending, active, paused, completed
    private LocalDate plannedStartDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean autoActivate;
    private LocalDate createdAt;

    public NutritionObjective() {
        this.status = "pending";
        this.autoActivate = false;
        this.createdAt = LocalDate.now();
    }

    // ── Status helpers ──
    public boolean isPending()   { return "pending".equals(status); }
    public boolean isActive()    { return "active".equals(status); }
    public boolean isPaused()    { return "paused".equals(status); }
    public boolean isCompleted() { return "completed".equals(status); }

    public int getProgressPercentage() {
        if (startDate == null) return 0;
        // Use completed daily logs count (matches Symfony entity logic)
        // We calculate from the service — but since we don't have logs here,
        // fall back to date-based calculation
        if (endDate == null) return 0;
        long total = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long elapsed = java.time.temporal.ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1;
        if (total <= 0) return 100;
        elapsed = Math.max(0, Math.min(elapsed, total));
        return (int) ((elapsed * 100) / total);
    }

    /** Progress based on completed logs — call this when you have the logs */
    public int getProgressPercentageFromLogs(int completedLogs) {
        return Math.min(100, (int) Math.round((completedLogs / 7.0) * 100));
    }

    public int getDaysRemaining() {
        if (endDate == null) return 0;
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        return (int) Math.max(0, days);
    }

    public String getGoalLabel() {
        if (goalType == null) return "Custom";
        return switch (goalType) {
            case "lose_weight"   -> "Lose Weight";
            case "gain_weight"   -> "Gain Weight";
            case "maintain"      -> "Maintain";
            case "build_muscle"  -> "Build Muscle";
            case "clean_eating"  -> "Clean Eating";
            default              -> "Custom";
        };
    }

    public String getPlanLabel() {
        if (planLevel == null) return "Custom";
        return switch (planLevel) {
            case "light"    -> "Light";
            case "moderate" -> "Moderate";
            case "intense"  -> "Intense";
            default         -> "Custom";
        };
    }

    // ── Getters & Setters ──
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGoalType() { return goalType; }
    public void setGoalType(String goalType) { this.goalType = goalType; }

    public String getPlanLevel() { return planLevel; }
    public void setPlanLevel(String planLevel) { this.planLevel = planLevel; }

    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }

    public double getTargetProtein() { return targetProtein; }
    public void setTargetProtein(double targetProtein) { this.targetProtein = targetProtein; }

    public double getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(double targetCarbs) { this.targetCarbs = targetCarbs; }

    public double getTargetFats() { return targetFats; }
    public void setTargetFats(double targetFats) { this.targetFats = targetFats; }

    public double getTargetWater() { return targetWater; }
    public void setTargetWater(double targetWater) { this.targetWater = targetWater; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getPlannedStartDate() { return plannedStartDate; }
    public void setPlannedStartDate(LocalDate plannedStartDate) { this.plannedStartDate = plannedStartDate; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isAutoActivate() { return autoActivate; }
    public void setAutoActivate(boolean autoActivate) { this.autoActivate = autoActivate; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}
