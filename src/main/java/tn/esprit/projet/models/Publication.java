package tn.esprit.projet.models;

import java.time.LocalDateTime;

public class Publication {
    private int id;
    private String titre;
    private String contenu;
    private String description;
    private LocalDateTime createdAt;
    private String authorName;
    private String authorAvatar;
    private boolean isAdmin;
    private String image;
    private int viewCount;
    private int shareCount;
    private String visibility;
    private LocalDateTime scheduledAt;
    private Integer sharedFromId;
    private int userId;

    public Publication() {
        this.createdAt = LocalDateTime.now();
        this.visibility = "public";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getShareCount() { return shareCount; }
    public void setShareCount(int shareCount) { this.shareCount = shareCount; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public Integer getSharedFromId() { return sharedFromId; }
    public void setSharedFromId(Integer sharedFromId) { this.sharedFromId = sharedFromId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
