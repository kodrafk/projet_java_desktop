package tn.esprit.projet.models;

import java.time.LocalDate;

public class MealPlan {
    private int id;
    private int userId;
    private String name;
    private LocalDate createdAt;
    private boolean active;

    public MealPlan() {}

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; } public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public LocalDate getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDate d) { this.createdAt = d; }
    public boolean isActive() { return active; } public void setActive(boolean active) { this.active = active; }
}
