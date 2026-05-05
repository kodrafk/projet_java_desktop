package tn.esprit.projet.models;

import java.time.LocalDateTime;

public class PublicationLike {
    private int id;
    private LocalDateTime createdAt;
    private int publicationId;
    private int userId;
    private boolean isLike; // true = like, false = dislike

    public PublicationLike() {
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getPublicationId() { return publicationId; }
    public void setPublicationId(int publicationId) { this.publicationId = publicationId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public boolean isLike() { return isLike; }
    public void setLike(boolean like) { isLike = like; }
}
