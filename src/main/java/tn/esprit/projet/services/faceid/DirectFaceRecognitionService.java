package tn.esprit.projet.services.faceid;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * DIRECT Face Recognition Service - DEMO VERSION
 * NO PYTHON, NO OPENCV NEEDED
 * Just shows the professional interface!
 */
public class DirectFaceRecognitionService {

    private Map<Integer, String> enrolledFaces = new HashMap<>();
    private Random random = new Random();

    public DirectFaceRecognitionService() {
        System.out.println("[DirectFaceRec] ✅ Initialized (DEMO MODE - No Python needed!)");
    }

    public boolean isInitialized() {
        return true;
    }

    /**
     * Detect if face is present in image (DEMO - always returns SUCCESS)
     */
    public String detectFace(byte[] imageJpeg) {
        try {
            // Simulate detection delay
            Thread.sleep(100);
            
            // Randomly return face detected (80% of the time)
            if (random.nextInt(100) < 80) {
                return "SUCCESS";
            } else {
                return "NO_FACE";
            }
            
        } catch (Exception e) {
            return "ERROR";
        }
    }

    /**
     * Enroll a face for a user (DEMO - just stores user ID)
     */
    public boolean enrollFace(int userId, byte[] imageJpeg) {
        try {
            // Simulate processing
            Thread.sleep(500);
            
            // Store user
            enrolledFaces.put(userId, "enrolled");
            
            System.out.println("[DirectFaceRec] ✅ Enrolled user " + userId + " (DEMO MODE)");
            return true;
            
        } catch (Exception e) {
            System.err.println("[DirectFaceRec] Enroll error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verify a face and return matched user ID (DEMO - returns first enrolled user)
     */
    public Integer verifyFace(byte[] imageJpeg) {
        try {
            // Simulate processing
            Thread.sleep(1000);
            
            if (enrolledFaces.isEmpty()) {
                System.out.println("[DirectFaceRec] ❌ No enrolled faces (DEMO MODE)");
                System.out.println("[DirectFaceRec] 💡 TIP: Enroll a face first in Profile settings");
                return null;
            }
            
            // Return first enrolled user (DEMO)
            Integer userId = enrolledFaces.keySet().iterator().next();
            System.out.println("[DirectFaceRec] ✅ Match found! User " + userId + " (DEMO MODE)");
            return userId;
            
        } catch (Exception e) {
            System.err.println("[DirectFaceRec] Verify error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if server is running (always true for demo)
     */
    public boolean isServerRunning() {
        return true;
    }
}
