package tn.esprit.projet.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class WeightObjective {
    private int id;
    private int userId;
    private double startWeight;
    private double targetWeight;
    private LocalDate startDate;
    private LocalDate targetDate;
    private String startPhoto;
    private boolean active;
    private LocalDateTime createdAt;

    public WeightObjective() {}

    public WeightObjective(int userId, double startWeight, double targetWeight, LocalDate startDate, LocalDate targetDate) {
        this.userId = userId;
        this.startWeight = startWeight;
        this.targetWeight = targetWeight;
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.active = true;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getStartWeight() {
        return startWeight;
    }

    public void setStartWeight(double startWeight) {
        this.startWeight = startWeight;
    }

    public double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getStartPhoto() {
        return startPhoto;
    }

    public void setStartPhoto(String startPhoto) {
        this.startPhoto = startPhoto;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Calculated methods
    public boolean isLossGoal() {
        return startWeight > targetWeight;
    }

    public double getTotalKg() {
        return Math.abs(targetWeight - startWeight);
    }

    public double getProgress(double currentWeight) {
        if (getTotalKg() == 0) return 1.0;
        double achieved = Math.abs(startWeight - currentWeight);
        return Math.min(1.0, achieved / getTotalKg());
    }

    public double getKgLost(double currentWeight) {
        return startWeight - currentWeight;
    }

    public long getDaysRemaining() {
        if (targetDate == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
    }

    @Override
    public String toString() {
        return "WeightObjective{" +
                "id=" + id +
                ", userId=" + userId +
                ", startWeight=" + startWeight +
                ", targetWeight=" + targetWeight +
                ", startDate=" + startDate +
                ", targetDate=" + targetDate +
                ", startPhoto='" + startPhoto + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
