package tn.esprit.projet.models;

import java.time.LocalDateTime;

public class WeightLog {
    private int id;
    private int userId;
    private double weight;
    private String photo;
    private String note;
    private LocalDateTime loggedAt;

    public WeightLog() {}

    public WeightLog(int userId, double weight) {
        this.userId = userId;
        this.weight = weight;
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

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }

    @Override
    public String toString() {
        return "WeightLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", weight=" + weight +
                ", photo='" + photo + '\'' +
                ", note='" + note + '\'' +
                ", loggedAt=" + loggedAt +
                '}';
    }
}
