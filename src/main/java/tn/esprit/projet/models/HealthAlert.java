package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * Représente une alerte prédictive générée par le système ML
 */
public class HealthAlert {
    
    public enum AlertPriority {
        LOW("Faible", "#17a2b8"),
        MEDIUM("Moyenne", "#ffc107"),
        HIGH("Élevée", "#fd7e14"),
        CRITICAL("Critique", "#dc3545");
        
        private final String label;
        private final String color;
        
        AlertPriority(String label, String color) {
            this.label = label;
            this.color = color;
        }
        
        public String getLabel() { return label; }
        public String getColor() { return color; }
    }
    
    private int id;
    private int userId;
    private int anomalyId;             // Lié à une anomalie
    private String title;
    private String message;
    private AlertPriority priority;
    private double riskScore;          // Score de risque ML 0-100
    private String recommendation;     // Recommandation automatique
    private boolean sent;              // Envoyé à l'admin
    private boolean acknowledged;      // Pris en compte par admin
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    
    public HealthAlert() {}
    
    public HealthAlert(int userId, int anomalyId, String title, String message, AlertPriority priority, double riskScore) {
        this.userId = userId;
        this.anomalyId = anomalyId;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.riskScore = riskScore;
        this.sent = false;
        this.acknowledged = false;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getAnomalyId() { return anomalyId; }
    public void setAnomalyId(int anomalyId) { this.anomalyId = anomalyId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public AlertPriority getPriority() { return priority; }
    public void setPriority(AlertPriority priority) { this.priority = priority; }
    
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    
    public boolean isSent() { return sent; }
    public void setSent(boolean sent) { this.sent = sent; }
    
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }
    
    // Méthodes utilitaires
    public boolean isPending() {
        return !acknowledged;
    }
    
    public boolean requiresAction() {
        return priority == AlertPriority.CRITICAL || priority == AlertPriority.HIGH;
    }
    
    public String getAgeInHours() {
        if (createdAt == null) return "0h";
        long hours = java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
        if (hours < 1) return "< 1h";
        if (hours < 24) return hours + "h";
        return (hours / 24) + "j";
    }
    
    @Override
    public String toString() {
        return String.format("HealthAlert{id=%d, userId=%d, priority=%s, riskScore=%.1f, acknowledged=%b}",
                id, userId, priority, riskScore, acknowledged);
    }
}
