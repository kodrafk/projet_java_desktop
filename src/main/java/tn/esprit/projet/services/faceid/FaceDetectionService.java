package tn.esprit.projet.services.faceid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.projet.services.FaceEmbeddingService;

import java.util.Base64;

/**
 * Face Detection and Embedding Extraction Service
 * Uses Python DeepFace + ArcFace backend
 */
public class FaceDetectionService {

    private final FaceEmbeddingService embeddingService = new FaceEmbeddingService();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Extract 512D ArcFace embedding from image
     */
    public double[] extractEmbedding(byte[] imageJpeg) throws Exception {
        if (imageJpeg == null || imageJpeg.length == 0) {
            throw new Exception("Empty image data");
        }

        // Encode image to base64
        String base64Image = Base64.getEncoder().encodeToString(imageJpeg);

        // Create JSON request for Python service
        String jsonRequest = String.format(
            "{\"command\":\"encode\",\"image\":\"%s\"}", 
            base64Image
        );

        // Call Python service
        return embeddingService.callPythonForEmbedding(jsonRequest);
    }

    /**
     * Detect if a face is present in the image
     */
    public boolean detectFace(byte[] imageJpeg) {
        try {
            double[] embedding = extractEmbedding(imageJpeg);
            return embedding != null && embedding.length == 512;
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("face could not be detected") || 
                msg.contains("no face")) {
                return false;
            }
            System.err.println("[FaceDetection] Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get face detection status message
     */
    public String getDetectionStatus(byte[] imageJpeg) {
        try {
            double[] embedding = extractEmbedding(imageJpeg);
            if (embedding != null && embedding.length == 512) {
                return "FACE_DETECTED";
            }
            return "NO_FACE";
        } catch (Exception e) {
            String msg = e.getMessage().toLowerCase();
            
            if (msg.contains("face could not be detected")) {
                return "NO_FACE";
            } else if (msg.contains("multiple faces")) {
                return "MULTIPLE_FACES";
            } else if (msg.contains("low light") || msg.contains("dark")) {
                return "LOW_LIGHT";
            } else {
                return "ERROR: " + e.getMessage();
            }
        }
    }
}
