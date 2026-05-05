package tn.esprit.projet.services.faceid;

import tn.esprit.projet.models.faceid.LivenessChallenge;

import java.util.ArrayList;
import java.util.List;

/**
 * Liveness Detection Service
 * Implements anti-spoofing checks
 */
public class LivenessDetectionService {

    private List<Double> recentBrightness = new ArrayList<>();
    private int frameCount = 0;
    private boolean blinkDetected = false;

    /**
     * Analyze frame for liveness indicators
     */
    public LivenessChallenge analyzeFrame(byte[] imageJpeg) {
        LivenessChallenge challenge = new LivenessChallenge();
        
        try {
            frameCount++;

            // Simple brightness analysis for blink detection
            double brightness = calculateBrightness(imageJpeg);
            recentBrightness.add(brightness);

            // Keep only last 10 frames
            if (recentBrightness.size() > 10) {
                recentBrightness.remove(0);
            }

            // Detect blink (brightness variation)
            if (recentBrightness.size() >= 5) {
                double variance = calculateVariance(recentBrightness);
                blinkDetected = variance > 100.0; // Threshold for blink
            }

            // Set challenge results
            challenge.setBlinkDetected(blinkDetected);
            challenge.setHeadMovementDetected(frameCount > 10); // Simplified
            challenge.setTextureScore(0.85); // Simplified - would need real texture analysis

            // Calculate overall score
            double score = 0.0;
            if (challenge.isBlinkDetected()) score += 0.4;
            if (challenge.isHeadMovementDetected()) score += 0.3;
            score += challenge.getTextureScore() * 0.3;

            challenge.setOverallScore(score);
            challenge.setStatus(score >= 0.7 ? "PASSED" : "PENDING");

        } catch (Exception e) {
            System.err.println("[Liveness] Analysis error: " + e.getMessage());
            challenge.setStatus("ERROR");
        }

        return challenge;
    }

    /**
     * Reset detection state
     */
    public void reset() {
        recentBrightness.clear();
        frameCount = 0;
        blinkDetected = false;
    }

    /**
     * Calculate average brightness (simplified)
     */
    private double calculateBrightness(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return 0.0;
        }

        // Simplified: use data length as proxy
        // Real implementation would analyze actual pixel values
        return imageData.length / 1000.0;
    }

    /**
     * Calculate variance of brightness values
     */
    private double calculateVariance(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);

        return variance;
    }

    /**
     * Create a simple passing challenge (for testing)
     */
    public LivenessChallenge createPassingChallenge() {
        LivenessChallenge challenge = new LivenessChallenge();
        challenge.setBlinkDetected(true);
        challenge.setHeadMovementDetected(true);
        challenge.setTextureScore(0.9);
        challenge.setOverallScore(1.0);
        challenge.setStatus("PASSED");
        return challenge;
    }
}
