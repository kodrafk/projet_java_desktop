package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * Modèle pour les signalements de publications.
 */
public class PublicationReport {
    private int id;
    private int publicationId;
    private int userId;
    private String reason;
    private LocalDateTime createdAt;

    public PublicationReport() {
        this.createdAt = LocalDateTime.now();
    }

    public PublicationReport(int publicationId, int userId, String reason) {
        this.publicationId = publicationId;
        this.userId = userId;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPublicationId() { return publicationId; }
    public void setPublicationId(int publicationId) { this.publicationId = publicationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
