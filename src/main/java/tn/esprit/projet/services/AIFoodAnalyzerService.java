package tn.esprit.projet.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * AI Food Analyzer Service using GROQ API
 * Analyzes food images to extract nutritional information
 */
public class AIFoodAnalyzerService {
    
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = loadApiKey();

    private static String loadApiKey() {
        // 1. Try local config.properties file (gitignored)
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File f = new java.io.File("config.properties");
            if (f.exists()) {
                props.load(new java.io.FileInputStream(f));
                String key = props.getProperty("GROQ_API_KEY");
                if (key != null && !key.isBlank()) return key.trim();
            }
        } catch (Exception ignored) {}
        // 2. Fall back to environment variable
        String env = System.getenv("GROQ_API_KEY");
        return env != null ? env : "";
    }
    private static final String MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public AIFoodAnalyzerService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Analyzes a food image and returns nutritional information
     * @param imageBytes The image as byte array
     * @param mimeType The MIME type of the image (e.g., "image/jpeg")
     * @return Task that resolves to FoodAnalysisResult
     */
    public Task<FoodAnalysisResult> analyzeImageAsync(byte[] imageBytes, String mimeType) {
        return new Task<FoodAnalysisResult>() {
            @Override
            protected FoodAnalysisResult call() throws Exception {
                updateMessage("Analyzing food image...");
                updateProgress(0, 100);
                
                try {
                    // Convert image to base64
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    String dataUri = "data:" + mimeType + ";base64," + base64Image;
                    
                    updateProgress(25, 100);
                    updateMessage("Sending to AI service...");
                    
                    // Create the request payload
                    Map<String, Object> requestBody = createRequestBody(dataUri);
                    String jsonBody = objectMapper.writeValueAsString(requestBody);
                    
                    updateProgress(50, 100);
                    
                    // Send HTTP request
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(GROQ_API_URL))
                            .header("Authorization", "Bearer " + API_KEY)
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofSeconds(30))
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();
                    
                    updateProgress(75, 100);
                    updateMessage("Processing AI response...");
                    
                    HttpResponse<String> response = httpClient.send(request, 
                            HttpResponse.BodyHandlers.ofString());
                    
                    updateProgress(90, 100);
                    
                    if (response.statusCode() != 200) {
                        throw new IOException("API request failed with status: " + response.statusCode() + 
                                            " - " + response.body());
                    }
                    
                    // Parse the response
                    FoodAnalysisResult result = parseResponse(response.body());
                    
                    updateProgress(100, 100);
                    updateMessage("Analysis complete!");
                    
                    return result;
                    
                } catch (Exception e) {
                    updateMessage("Analysis failed: " + e.getMessage());
                    throw e;
                }
            }
        };
    }
    
    private Map<String, Object> createRequestBody(String dataUri) {
        String prompt = """
            Analyze this food image. Identify the food and estimate its nutritional content per visible serving.
            
            You MUST respond ONLY with valid JSON in this EXACT format, no extra text:
            {
              "name": "Food Name",
              "serving": "estimated serving size (e.g. 1 plate, 200g, 1 bowl)",
              "calories": 350,
              "protein": 25.0,
              "carbs": 30.0,
              "fats": 12.0,
              "fiber": 5.0,
              "sugar": 8.0
            }
            
            Rules:
            - calories must be a whole number between 1 and 2000
            - protein, carbs, fats, fiber, sugar must be numbers between 0 and 300
            - serving should be a short human-readable string
            - name should be concise (e.g. "Grilled Chicken Salad")
            - If multiple foods are visible, combine them into one meal entry
            - If this is NOT a food image, respond with: {"error": "Could not identify food in this image"}
            """;
        
        Map<String, Object> content1 = Map.of(
            "type", "text",
            "text", prompt
        );
        
        Map<String, Object> imageUrl = Map.of("url", dataUri);
        Map<String, Object> content2 = Map.of(
            "type", "image_url",
            "image_url", imageUrl
        );
        
        Map<String, Object> message = Map.of(
            "role", "user",
            "content", new Object[]{content1, content2}
        );
        
        return Map.of(
            "model", MODEL,
            "max_tokens", 300,
            "temperature", 0.1,
            "messages", new Object[]{message}
        );
    }
    
    private FoodAnalysisResult parseResponse(String responseBody) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            
            if (choices == null || choices.size() == 0) {
                return FoodAnalysisResult.error("No response from AI service");
            }
            
            String content = choices.get(0).get("message").get("content").asText();
            
            // Extract JSON from response (handle markdown code blocks)
            String jsonContent = extractJson(content);
            if (jsonContent == null) {
                return FoodAnalysisResult.error("Could not parse AI response");
            }
            
            JsonNode foodData = objectMapper.readTree(jsonContent);
            
            // Check for error in response
            if (foodData.has("error")) {
                return FoodAnalysisResult.error(foodData.get("error").asText());
            }
            
            // Validate and create result
            return FoodAnalysisResult.success(
                sanitizeString(foodData.get("name").asText(), "Unknown Food"),
                sanitizeString(foodData.get("serving").asText(), "1 serving"),
                clamp(foodData.get("calories").asInt(), 1, 2000),
                clamp(foodData.get("protein").asDouble(), 0, 200),
                clamp(foodData.get("carbs").asDouble(), 0, 300),
                clamp(foodData.get("fats").asDouble(), 0, 150),
                clamp(foodData.get("fiber").asDouble(0), 0, 50),
                clamp(foodData.get("sugar").asDouble(0), 0, 100)
            );
            
        } catch (Exception e) {
            return FoodAnalysisResult.error("Failed to parse response: " + e.getMessage());
        }
    }
    
    private String extractJson(String content) {
        // Look for JSON object in the content
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        
        if (start != -1 && end != -1 && end > start) {
            return content.substring(start, end + 1);
        }
        
        return null;
    }
    
    private String sanitizeString(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim().substring(0, Math.min(value.trim().length(), 100));
    }
    
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, Math.round(value * 10.0) / 10.0));
    }
    
    /**
     * Result class for food analysis
     */
    public static class FoodAnalysisResult {
        private final boolean success;
        private final String error;
        private final String name;
        private final String serving;
        private final int calories;
        private final double protein;
        private final double carbs;
        private final double fats;
        private final double fiber;
        private final double sugar;
        
        private FoodAnalysisResult(boolean success, String error, String name, String serving,
                                 int calories, double protein, double carbs, double fats,
                                 double fiber, double sugar) {
            this.success = success;
            this.error = error;
            this.name = name;
            this.serving = serving;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fats = fats;
            this.fiber = fiber;
            this.sugar = sugar;
        }
        
        public static FoodAnalysisResult success(String name, String serving, int calories,
                                               double protein, double carbs, double fats,
                                               double fiber, double sugar) {
            return new FoodAnalysisResult(true, null, name, serving, calories, 
                                        protein, carbs, fats, fiber, sugar);
        }
        
        public static FoodAnalysisResult error(String error) {
            return new FoodAnalysisResult(false, error, null, null, 0, 0, 0, 0, 0, 0);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public String getName() { return name; }
        public String getServing() { return serving; }
        public int getCalories() { return calories; }
        public double getProtein() { return protein; }
        public double getCarbs() { return carbs; }
        public double getFats() { return fats; }
        public double getFiber() { return fiber; }
        public double getSugar() { return sugar; }
        
        @Override
        public String toString() {
            if (!success) {
                return "Error: " + error;
            }
            return String.format("%s (%s) - %d cal, %.1fg protein, %.1fg carbs, %.1fg fats",
                               name, serving, calories, protein, carbs, fats);
        }
    }
}