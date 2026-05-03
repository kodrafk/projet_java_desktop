package tn.esprit.projet.services.faceid;

import tn.esprit.projet.models.faceid.FaceIDRequest;
import tn.esprit.projet.models.faceid.FaceIDResponse;
import tn.esprit.projet.repository.faceid.FaceEmbeddingRepository;
import tn.esprit.projet.repository.faceid.FaceIDAttemptRepository;

/**
 * Face ID Business Logic Service
 * Handles enrollment and verification with security checks
 */
public class FaceIDService {

    private final FaceEmbeddingRepository embeddingRepo = new FaceEmbeddingRepository();
    private final FaceIDAttemptRepository attemptRepo = new FaceIDAttemptRepository();
    
    private static final double SIMILARITY_THRESHOLD = 0.50; // ArcFace threshold

    /**
     * Enroll a new face for a user
     */
    public FaceIDResponse enroll(FaceIDRequest request) {
        try {
            // Validate request
            if (request.getUserId() == null) {
                return FaceIDResponse.error("User ID is required", "INVALID_REQUEST");
            }
            
            if (request.getEmbedding() == null || request.getEmbedding().length != 512) {
                return FaceIDResponse.error("Invalid embedding (must be 512D)", "INVALID_EMBEDDING");
            }

            // Check liveness
            boolean livenessVerified = request.getLivenessScore() >= 0.7;
            if (!livenessVerified) {
                attemptRepo.logAttempt(request.getUserId(), "enroll", false, 
                                      null, false, "Liveness check failed", request.getIpAddress());
                return FaceIDResponse.error("Liveness verification failed", "LIVENESS_FAILED");
            }

            // Save embedding
            boolean saved = embeddingRepo.saveEmbedding(
                request.getUserId(), 
                request.getEmbedding(), 
                livenessVerified
            );

            if (saved) {
                attemptRepo.logAttempt(request.getUserId(), "enroll", true, 
                                      null, true, null, request.getIpAddress());
                
                FaceIDResponse response = FaceIDResponse.success("Face ID enrolled successfully");
                response.setUserId(request.getUserId());
                return response;
            } else {
                attemptRepo.logAttempt(request.getUserId(), "enroll", false, 
                                      null, true, "Database error", request.getIpAddress());
                return FaceIDResponse.error("Failed to save Face ID", "DATABASE_ERROR");
            }

        } catch (Exception e) {
            System.err.println("[FaceID] Enrollment error: " + e.getMessage());
            e.printStackTrace();
            return FaceIDResponse.error("Internal error: " + e.getMessage(), "INTERNAL_ERROR");
        }
    }

    /**
     * Verify a face against stored embeddings
     */
    public FaceIDResponse verify(FaceIDRequest request) {
        try {
            // Validate request
            if (request.getEmbedding() == null || request.getEmbedding().length != 512) {
                return FaceIDResponse.error("Invalid embedding (must be 512D)", "INVALID_EMBEDDING");
            }

            // Check rate limiting
            if (request.getIpAddress() != null && attemptRepo.isRateLimited(request.getIpAddress())) {
                return FaceIDResponse.error("Too many attempts. Please try again later.", "RATE_LIMITED");
            }

            // Check liveness
            boolean livenessVerified = request.getLivenessScore() >= 0.7;
            if (!livenessVerified) {
                attemptRepo.logAttempt(null, "verify", false, 
                                      null, false, "Liveness check failed", request.getIpAddress());
                return FaceIDResponse.error("Liveness verification failed", "LIVENESS_FAILED");
            }

            // Find matching user
            Integer matchedUserId = findMatchingUser(request.getEmbedding());

            if (matchedUserId != null) {
                // Success
                embeddingRepo.updateLastVerified(matchedUserId);
                attemptRepo.logAttempt(matchedUserId, "verify", true, 
                                      null, true, null, request.getIpAddress());
                
                FaceIDResponse response = FaceIDResponse.success("Authentication successful");
                response.setUserId(matchedUserId);
                response.setThreshold(SIMILARITY_THRESHOLD);
                return response;
            } else {
                // No match
                attemptRepo.logAttempt(null, "verify", false, 
                                      null, true, "No matching face", request.getIpAddress());
                return FaceIDResponse.error("Face not recognized", "NO_MATCH");
            }

        } catch (Exception e) {
            System.err.println("[FaceID] Verification error: " + e.getMessage());
            e.printStackTrace();
            return FaceIDResponse.error("Internal error: " + e.getMessage(), "INTERNAL_ERROR");
        }
    }

    /**
     * Find user with matching face embedding
     */
    private Integer findMatchingUser(double[] liveEmbedding) {
        try {
            // Get all enrolled users
            var allEmbeddings = embeddingRepo.findAllActiveEmbeddings();
            
            if (allEmbeddings.isEmpty()) {
                System.out.println("[FaceID] No enrolled users found");
                return null;
            }
            
            System.out.println("[FaceID] Checking " + allEmbeddings.size() + " enrolled users");
            
            // Find best match
            Integer bestUserId = null;
            double bestSimilarity = 0.0;
            
            for (var userEmb : allEmbeddings) {
                try {
                    // Decrypt stored embedding
                    double[] storedEmbedding = embeddingRepo.decryptEmbedding(
                        userEmb.encryptedB64, 
                        userEmb.ivB64, 
                        userEmb.tagB64
                    );
                    
                    if (storedEmbedding == null || storedEmbedding.length != 512) {
                        continue;
                    }
                    
                    // Calculate similarity
                    double similarity = cosineSimilarity(liveEmbedding, storedEmbedding);
                    
                    System.out.println("[FaceID] User " + userEmb.userId + " similarity: " + similarity);
                    
                    if (similarity > bestSimilarity) {
                        bestSimilarity = similarity;
                        bestUserId = userEmb.userId;
                    }
                    
                } catch (Exception e) {
                    System.err.println("[FaceID] Error processing user " + userEmb.userId + ": " + e.getMessage());
                }
            }
            
            // Check if best match exceeds threshold
            if (bestUserId != null && bestSimilarity >= SIMILARITY_THRESHOLD) {
                System.out.println("[FaceID] Match found! User " + bestUserId + " with similarity " + bestSimilarity);
                return bestUserId;
            } else {
                System.out.println("[FaceID] No match found. Best similarity: " + bestSimilarity + " (threshold: " + SIMILARITY_THRESHOLD + ")");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("[FaceID] Error in findMatchingUser: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calculate cosine similarity between two embeddings
     */
    private double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Check if user has Face ID enrolled
     */
    public boolean isEnrolled(int userId) {
        return embeddingRepo.hasEmbedding(userId);
    }

    /**
     * Delete user's Face ID enrollment
     */
    public boolean unenroll(int userId) {
        return embeddingRepo.deleteEmbedding(userId);
    }
}
