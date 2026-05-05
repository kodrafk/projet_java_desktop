package tn.esprit.projet.security.faceid;

import tn.esprit.projet.models.faceid.LivenessChallenge;

/**
 * Liveness Detection Validator
 * Anti-spoofing verification for Face ID
 */
public class LivenessValidator {

    private static final double BLINK_WEIGHT = 0.4;
    private static final double MOVEMENT_WEIGHT = 0.3;
    private static final double TEXTURE_WEIGHT = 0.3;
    private static final double PASS_THRESHOLD = 0.7;

    /**
     * Validate liveness challenge results
     */
    public static boolean validate(LivenessChallenge challenge) {
        if (challenge == null) {
            return false;
        }

        double score = calculateScore(challenge);
        challenge.setOverallScore(score);

        if (score >= PASS_THRESHOLD) {
            challenge.setStatus("PASSED");
            return true;
        } else {
            challenge.setStatus("FAILED");
            return false;
        }
    }

    /**
     * Calculate weighted liveness score
     */
    private static double calculateScore(LivenessChallenge challenge) {
        double score = 0.0;

        // Blink detection (40%)
        if (challenge.isBlinkDetected()) {
            score += BLINK_WEIGHT;
        }

        // Head movement (30%)
        if (challenge.isHeadMovementDetected()) {
            score += MOVEMENT_WEIGHT;
        }

        // Texture analysis (30%)
        score += challenge.getTextureScore() * TEXTURE_WEIGHT;

        return Math.min(1.0, score);
    }

    /**
     * Create a simple passing challenge (for development/testing)
     */
    public static LivenessChallenge createPassingChallenge() {
        LivenessChallenge challenge = new LivenessChallenge();
        challenge.setBlinkDetected(true);
        challenge.setHeadMovementDetected(true);
        challenge.setTextureScore(0.9);
        challenge.setOverallScore(1.0);
        challenge.setStatus("PASSED");
        return challenge;
    }

    /**
     * Create a failing challenge
     */
    public static LivenessChallenge createFailingChallenge(String reason) {
        LivenessChallenge challenge = new LivenessChallenge();
        challenge.setBlinkDetected(false);
        challenge.setHeadMovementDetected(false);
        challenge.setTextureScore(0.2);
        challenge.setOverallScore(0.2);
        challenge.setStatus("FAILED: " + reason);
        return challenge;
    }
}
