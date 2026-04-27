package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * Modèle pour les photos de progression
 */
public class ProgressPhoto {
    private int id;
    private int userId;
    private String filename;
    private String caption;
    private double weight; // Poids au moment de la photo
    private LocalDateTime takenAt;
    private LocalDateTime createdAt;

    // Constructors
    public ProgressPhoto() {}

    public ProgressPhoto(int userId, String filename) {
        this.userId = userId;
        this.filename = filename;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ProgressPhoto{" +
                "id=" + id +
                ", userId=" + userId +
                ", filename='" + filename + '\'' +
                ", caption='" + caption + '\'' +
                ", weight=" + weight +
                ", takenAt=" + takenAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
