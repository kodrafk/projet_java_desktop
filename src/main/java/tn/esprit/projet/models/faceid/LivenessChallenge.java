package tn.esprit.projet.models.faceid;

/**
 * Liveness Detection Challenge
 * Tracks anti-spoofing verification state
 */
public class LivenessChallenge {
    private boolean blinkDetected;
    private boolean headMovementDetected;
    private double textureScore;
    private double overallScore;
    private String status;

    public LivenessChallenge() {
        this.status = "PENDING";
    }

    public boolean isPassed() {
        return overallScore >= 0.7;
    }

    // Getters and Setters
    public boolean isBlinkDetected() { return blinkDetected; }
    public void setBlinkDetected(boolean blinkDetected) { this.blinkDetected = blinkDetected; }

    public boolean isHeadMovementDetected() { return headMovementDetected; }
    public void setHeadMovementDetected(boolean headMovementDetected) { 
        this.headMovementDetected = headMovementDetected; 
    }

    public double getTextureScore() { return textureScore; }
    public void setTextureScore(double textureScore) { this.textureScore = textureScore; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
