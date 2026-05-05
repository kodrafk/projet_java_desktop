package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * Represents an anomaly detected in user health data
 */
public class HealthAnomaly {
    
    public enum AnomalyType {
        RAPID_WEIGHT_LOSS("Rapid weight loss", "danger"),
        RAPID_WEIGHT_GAIN("Rapid weight gain", "danger"),
        PROLONGED_INACTIVITY("Prolonged inactivity", "warning"),
        YO_YO_PATTERN("Yo-yo pattern detected", "warning"),
        UNREALISTIC_GOAL("Unrealistic goal", "info"),
        ABANDONMENT_RISK("Abandonment risk", "danger"),
        ABNORMAL_BEHAVIOR("Abnormal behavior", "warning");
        
        private final String label;
        private final String severity;
        
        AnomalyType(String label, String severity) {
            this.label = label;
            this.severity = severity;
        }
        
        public String getLabel() { return label; }
        public String getSeverity() { return severity; }
    }
    
    private int id;
    private int userId;
    private AnomalyType type;
    private String description;
    private double severity;           // Score 0-100
    private double confidence;         // ML confidence 0-1
    private String details;            // JSON with technical details
    private boolean resolved;
    private LocalDateTime detectedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;         // Admin who resolved
    private String resolution;         // Resolution note
    
    public HealthAnomaly() {}
    
    public HealthAnomaly(int userId, AnomalyType type, String description, double severity, double confidence) {
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.severity = severity;
        this.confidence = confidence;
        this.resolved = false;
        this.detectedAt = LocalDateTime.now();
    }
    
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public AnomalyType getType() { return type; }
    public void setType(AnomalyType type) { this.type = type; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getSeverity() { return severity; }
    public void setSeverity(double severity) { this.severity = severity; }
    
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    
    // Utility methods
    public String getSeverityLevel() {
        if (severity >= 80) return "CRITICAL";
        if (severity >= 60) return "HIGH";
        if (severity >= 40) return "MEDIUM";
        return "LOW";
    }
    
    public String getSeverityColor() {
        if (severity >= 80) return "#dc3545";  // Rouge
        if (severity >= 60) return "#fd7e14";  // Orange
        if (severity >= 40) return "#ffc107";  // Jaune
        return "#17a2b8";                      // Bleu
    }
    
    public boolean isCritical() {
        return severity >= 80;
    }
    
    public boolean requiresImmediate() {
        return severity >= 70 && !resolved;
    }
    
    @Override
    public String toString() {
        return String.format("HealthAnomaly{id=%d, userId=%d, type=%s, severity=%.1f, confidence=%.2f, resolved=%b}",
                id, userId, type, severity, confidence, resolved);
    }
}
