package tn.esprit.projet.models;

import java.time.LocalDate;

public class NutritionObjective {
    private int id;
    private String title, description, goalType, planLevel, status;
    private int targetCalories;
    private double targetProtein, targetCarbs, targetFats, targetWater;
    private LocalDate plannedStartDate, startDate, endDate, createdAt;
    private boolean autoActivate;

    public NutritionObjective() { this.status = "pending"; this.autoActivate = false; this.createdAt = LocalDate.now(); }

    public boolean isPending()   { return "pending".equals(status); }
    public boolean isActive()    { return "active".equals(status); }
    public boolean isPaused()    { return "paused".equals(status); }
    public boolean isCompleted() { return "completed".equals(status); }

    public int getProgressPercentage() {
        if (startDate == null || endDate == null) return 0;
        long total = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        long elapsed = java.time.temporal.ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1;
        if (total <= 0) return 100;
        elapsed = Math.max(0, Math.min(elapsed, total));
        return (int) ((elapsed * 100) / total);
    }

    public int getProgressPercentageFromLogs(int completedLogs) { return Math.min(100, (int) Math.round((completedLogs / 7.0) * 100)); }

    public int getDaysRemaining() {
        if (endDate == null) return 0;
        return (int) Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate));
    }

    public String getGoalLabel() {
        if (goalType == null) return "Custom";
        return switch (goalType) { case "lose_weight" -> "Lose Weight"; case "gain_weight" -> "Gain Weight"; case "maintain" -> "Maintain"; case "build_muscle" -> "Build Muscle"; case "clean_eating" -> "Clean Eating"; default -> "Custom"; };
    }

    public String getPlanLabel() {
        if (planLevel == null) return "Custom";
        return switch (planLevel) { case "light" -> "Light"; case "moderate" -> "Moderate"; case "intense" -> "Intense"; default -> "Custom"; };
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String t) { this.title = t; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public String getGoalType() { return goalType; } public void setGoalType(String g) { this.goalType = g; }
    public String getPlanLevel() { return planLevel; } public void setPlanLevel(String p) { this.planLevel = p; }
    public int getTargetCalories() { return targetCalories; } public void setTargetCalories(int c) { this.targetCalories = c; }
    public double getTargetProtein() { return targetProtein; } public void setTargetProtein(double p) { this.targetProtein = p; }
    public double getTargetCarbs() { return targetCarbs; } public void setTargetCarbs(double c) { this.targetCarbs = c; }
    public double getTargetFats() { return targetFats; } public void setTargetFats(double f) { this.targetFats = f; }
    public double getTargetWater() { return targetWater; } public void setTargetWater(double w) { this.targetWater = w; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public LocalDate getPlannedStartDate() { return plannedStartDate; } public void setPlannedStartDate(LocalDate d) { this.plannedStartDate = d; }
    public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate d) { this.startDate = d; }
    public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate d) { this.endDate = d; }
    public boolean isAutoActivate() { return autoActivate; } public void setAutoActivate(boolean a) { this.autoActivate = a; }
    public LocalDate getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDate d) { this.createdAt = d; }
}
