package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * UserAlert — Système d'alarmes/notifications pour les utilisateurs
 * Affichées dans le front office avec différents niveaux de priorité
 */
public class UserAlert {

    private int id;
    private int userId;                  // Destinataire
    private int adminId;                 // Créateur (admin)
    private String title;                // Titre de l'alerte
    private String message;              // Message détaillé
    private AlertType type;              // Type d'alerte (INFO, WARNING, URGENT, SUCCESS)
    private AlertCategory category;      // Catégorie (HEALTH, GOAL, REMINDER, SYSTEM)
    private boolean isRead;              // Lu par l'utilisateur ?
    private boolean isDismissed;         // Fermé par l'utilisateur ?
    private LocalDateTime createdAt;     // Date de création
    private LocalDateTime expiresAt;     // Date d'expiration (optionnel)
    private LocalDateTime readAt;        // Date de lecture
    private String actionUrl;            // URL d'action (optionnel)
    private String actionLabel;          // Label du bouton d'action

    public enum AlertType {
        INFO,       // Information générale (bleu)
        WARNING,    // Avertissement (orange)
        URGENT,     // Urgent (rouge)
        SUCCESS     // Succès/Félicitations (vert)
    }

    public enum AlertCategory {
        HEALTH,     // Santé/Métriques
        GOAL,       // Objectifs
        REMINDER,   // Rappels
        SYSTEM      // Système
    }

    public UserAlert() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.isDismissed = false;
        this.type = AlertType.INFO;
        this.category = AlertCategory.SYSTEM;
    }

    public UserAlert(int userId, int adminId, String title, String message, AlertType type, AlertCategory category) {
        this();
        this.userId = userId;
        this.adminId = adminId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.category = category;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public AlertCategory getCategory() { return category; }
    public void setCategory(AlertCategory category) { this.category = category; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isDismissed() { return isDismissed; }
    public void setDismissed(boolean dismissed) { isDismissed = dismissed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public String getActionLabel() { return actionLabel; }
    public void setActionLabel(String actionLabel) { this.actionLabel = actionLabel; }

    // ── Computed ───────────────────────────────────────────────────────────────

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return !isDismissed && !isExpired();
    }

    public String getTypeIcon() {
        return switch (type) {
            case INFO -> "ℹ️";
            case WARNING -> "⚠️";
            case URGENT -> "🚨";
            case SUCCESS -> "✅";
        };
    }

    public String getCategoryIcon() {
        return switch (category) {
            case HEALTH -> "❤️";
            case GOAL -> "🎯";
            case REMINDER -> "⏰";
            case SYSTEM -> "⚙️";
        };
    }
}
