package tn.esprit.projet.services.faceid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * SIMPLE Face Recognition Service
 * Communicates with Python Flask server
 * NO COMPLEX DEPENDENCIES - JUST WORKS!
 */
public class SimpleFaceRecognitionService {

    private static final String SERVER_URL = "http://localhost:5000";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Check if server is running
     */
    public boolean isServerRunning() {
        try {
            URL url = new URL(SERVER_URL + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            return responseCode == 200;
            
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Detect if face is present in image
     */
    public String detectFace(byte[] imageJpeg) {
        try {
            // Encode image to base64
            String base64Image = Base64.getEncoder().encodeToString(imageJpeg);
            
            // Create JSON request
            ObjectNode request = mapper.createObjectNode();
            request.put("image", base64Image);
            
            // Send request
            JsonNode response = sendPostRequest("/detect", request);
            
            if (response != null && response.has("status")) {
                return response.get("status").asText();
            }
            
            return "ERROR";
            
        } catch (Exception e) {
            System.err.println("[SimpleFaceRec] Detect error: " + e.getMessage());
            return "ERROR";
        }
    }

    /**
     * Enroll a face for a user
     */
    public boolean enrollFace(int userId, byte[] imageJpeg) {
        try {
            // Encode image to base64
            String base64Image = Base64.getEncoder().encodeToString(imageJpeg);
            
            // Create JSON request
            ObjectNode request = mapper.createObjectNode();
            request.put("user_id", userId);
            request.put("image", base64Image);
            
            // Send request
            JsonNode response = sendPostRequest("/enroll", request);
            
            if (response != null && response.has("success")) {
                boolean success = response.get("success").asBoolean();
                
                if (success) {
                    System.out.println("[SimpleFaceRec] ✅ Enrolled user " + userId);
                } else {
                    String message = response.has("message") ? response.get("message").asText() : "Unknown error";
                    System.err.println("[SimpleFaceRec] ❌ Enrollment failed: " + message);
                }
                
                return success;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("[SimpleFaceRec] Enroll error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verify a face and return matched user ID
     */
    public Integer verifyFace(byte[] imageJpeg) {
        try {
            // Encode image to base64
            String base64Image = Base64.getEncoder().encodeToString(imageJpeg);
            
            // Create JSON request
            ObjectNode request = mapper.createObjectNode();
            request.put("image", base64Image);
            
            // Send request
            JsonNode response = sendPostRequest("/verify", request);
            
            if (response != null) {
                boolean success = response.has("success") && response.get("success").asBoolean();
                
                if (success && response.has("matched_user_id")) {
                    int userId = response.get("matched_user_id").asInt();
                    double similarity = response.has("similarity") ? response.get("similarity").asDouble() : 0.0;
                    
                    System.out.println("[SimpleFaceRec] ✅ Match found! User " + userId + " (similarity: " + similarity + ")");
                    return userId;
                } else {
                    String message = response.has("message") ? response.get("message").asText() : "No match";
                    System.out.println("[SimpleFaceRec] ❌ " + message);
                    return null;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("[SimpleFaceRec] Verify error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract face encoding from image
     */
    public double[] extractEncoding(byte[] imageJpeg) {
        try {
            // Encode image to base64
            String base64Image = Base64.getEncoder().encodeToString(imageJpeg);
            
            // Create JSON request
            ObjectNode request = mapper.createObjectNode();
            request.put("image", base64Image);
            
            // Send request
            JsonNode response = sendPostRequest("/extract", request);
            
            if (response != null && response.has("success") && response.get("success").asBoolean()) {
                if (response.has("encoding")) {
                    JsonNode encodingNode = response.get("encoding");
                    
                    // Convert JSON array to double array
                    double[] encoding = new double[encodingNode.size()];
                    for (int i = 0; i < encodingNode.size(); i++) {
                        encoding[i] = encodingNode.get(i).asDouble();
                    }
                    
                    System.out.println("[SimpleFaceRec] ✅ Extracted " + encoding.length + "D encoding");
                    return encoding;
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("[SimpleFaceRec] Extract error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Send POST request to Python server
     */
    private JsonNode sendPostRequest(String endpoint, ObjectNode requestBody) {
        HttpURLConnection conn = null;
        try {
            // Create connection
            URL url = new URL(SERVER_URL + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            // Send request
            String jsonRequest = mapper.writeValueAsString(requestBody);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Read response
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                JsonNode response = mapper.readTree(conn.getInputStream());
                return response;
            } else {
                // Try to read error message
                try {
                    JsonNode error = mapper.readTree(conn.getErrorStream());
                    System.err.println("[SimpleFaceRec] Server error: " + error);
                } catch (Exception e) {
                    System.err.println("[SimpleFaceRec] HTTP error: " + responseCode);
                }
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("[SimpleFaceRec] Request error: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Get server status
     */
    public String getServerStatus() {
        try {
            URL url = new URL(SERVER_URL + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                JsonNode response = mapper.readTree(conn.getInputStream());
                conn.disconnect();
                return response.toString();
            }
            
            conn.disconnect();
            return "Server not responding";
            
        } catch (Exception e) {
            return "Server offline: " + e.getMessage();
        }
    }
}
