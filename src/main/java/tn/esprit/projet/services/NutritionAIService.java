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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * AI Nutrition Service using GROQ API
 * Generates nutrition objective suggestions based on user goals
 */
public class NutritionAIService {
    
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = System.getenv().getOrDefault("GROQ_API_KEY", "YOUR_GROQ_API_KEY_HERE");
    private static final String MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Random random;
    
    public NutritionAIService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
        this.random = new Random();
    }
    
    /**
     * Generates nutrition plan suggestions based on user's goal
     * @param title The user's nutrition goal/objective title
     * @param variation Variation number for different suggestions (0-9)
     * @return Task that resolves to NutritionSuggestion
     */
    public Task<NutritionSuggestion> suggestPlanAsync(String title, int variation) {
        return new Task<NutritionSuggestion>() {
            @Override
            protected NutritionSuggestion call() throws Exception {
                updateMessage("Analyzing your goal...");
                updateProgress(0, 100);
                
                // Quick validation first
                NutritionSuggestion preCheck = preValidate(title);
                if (!preCheck.isValid()) {
                    return preCheck;
                }
                
                updateProgress(25, 100);
                updateMessage("Generating nutrition plan...");
                
                try {
                    // Create the request payload
                    Map<String, Object> requestBody = createRequestBody(title, variation);
                    String jsonBody = objectMapper.writeValueAsString(requestBody);
                    
                    updateProgress(50, 100);
                    updateMessage("Consulting AI nutritionist...");
                    
                    // Send HTTP request
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(GROQ_API_URL))
                            .header("Authorization", "Bearer " + API_KEY)
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofSeconds(30))
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .build();
                    
                    updateProgress(75, 100);
                    
                    HttpResponse<String> response = httpClient.send(request, 
                            HttpResponse.BodyHandlers.ofString());
                    
                    updateProgress(90, 100);
                    updateMessage("Processing recommendations...");
                    
                    if (response.statusCode() != 200) {
                        throw new IOException("API request failed with status: " + response.statusCode());
                    }
                    
                    // Parse the response
                    NutritionSuggestion result = parseResponse(response.body(), title, variation);
                    
                    updateProgress(100, 100);
                    updateMessage("Plan ready!");
                    
                    return result;
                    
                } catch (Exception e) {
                    updateMessage("Using fallback plan...");
                    return getFallback(title, variation);
                }
            }
        };
    }
    
    private NutritionSuggestion preValidate(String title) {
        String clean = title.toLowerCase().trim();
        
        // Too short
        if (clean.length() < 3) {
            return NutritionSuggestion.invalid("\"" + title + "\" is too short! Try something like \"Weight Loss\", \"Muscle Gain\", or \"Keto Diet\".");
        }
        
        // Only numbers/symbols
        if (clean.matches("^[^a-zA-Z]*$")) {
            return NutritionSuggestion.invalid("Please type a goal using words, for example \"Lose 10kg\", \"High Protein Diet\", or \"Lean Bulk\".");
        }
        
        // Repeated characters
        if (clean.matches("^(.)\\1+$")) {
            return NutritionSuggestion.invalid("That doesn't look like a nutrition goal. Try \"Lose Weight\", \"Build Muscle\", or \"Clean Eating\".");
        }
        
        return NutritionSuggestion.valid(); // Passed validation
    }
    
    private Map<String, Object> createRequestBody(String title, int variation) {
        String[] styles = {
            "high protein, moderate carbs, lower fat",
            "balanced 40% carbs, 30% protein, 30% fat", 
            "high carb for energy and performance",
            "moderate fat with controlled carbs",
            "low carb, higher healthy fats",
            "athletic performance-oriented",
            "recovery-focused with anti-inflammatory foods",
            "volume eating - high food volume, controlled calories",
            "Mediterranean-inspired with healthy fats",
            "plant-forward with diverse protein sources"
        };
        
        String style = styles[variation % styles.length];
        int seed = 1000 + random.nextInt(9000);
        int calorieShift = (variation * 127) % 400 - 200;
        String[] proteinBias = {"higher", "moderate", "lower", "very high", "balanced"};
        String proteinPref = proteinBias[variation % proteinBias.length];
        
        String systemPrompt = String.format("""
            You are NutriBot, an expert sports nutritionist.
            
            Goal: "%s"
            
            VALIDATION:
            - Accept ANY nutrition/fitness/health/diet/body goal, even with typos.
            - "gaint weight" = "gain weight", "loose fat" = "lose fat"
            - ONLY reject if it's truly NOT about nutrition, fitness, health, or body goals.
            
            If INVALID: {"valid": false, "reason": "Friendly explanation of what went wrong, then suggest 2-3 real goals they could try instead."}
            
            If VALID, generate a plan:
            
            IMPORTANT - THIS IS VARIATION #%d (seed: %d)
            - Macro style: %s
            - Protein preference: %s
            - Calorie adjustment: %d kcal from baseline
            - You MUST generate DIFFERENT numbers than any previous suggestion
            - DO NOT use round numbers like 2000, 150, 200. Use specific numbers like 2137, 163, 218.
            - Every field must differ by at least 10%% from a "default" plan.
            
            Return ONLY raw JSON:
            {"valid": true, "description": "1-2 sentence coaching message about '%s'. Be warm and specific about WHY this approach.", "calories": integer, "protein": integer, "carbs": integer, "fats": integer, "water": float}
            
            NO markdown. NO code blocks. ONLY raw JSON.
            """, title, variation, seed, style, proteinPref, calorieShift, title);
        
        Map<String, Object> message = Map.of(
            "role", "user",
            "content", systemPrompt
        );
        
        return Map.of(
            "model", MODEL,
            "messages", new Object[]{
                Map.of("role", "system", "content", "You are a JSON API. Output ONLY raw JSON. Never use markdown."),
                message
            },
            "temperature", 1.2,
            "max_tokens", 500,
            "top_p", 0.95,
            "seed", seed
        );
    }
    
    private NutritionSuggestion parseResponse(String responseBody, String title, int variation) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            
            if (choices == null || choices.size() == 0) {
                return getFallback(title, variation);
            }
            
            String content = choices.get(0).get("message").get("content").asText();
            
            // Extract JSON from response
            String jsonContent = extractJson(content);
            if (jsonContent == null) {
                return getFallback(title, variation);
            }
            
            JsonNode data = objectMapper.readTree(jsonContent);
            
            // Check for error in response
            if (data.has("error")) {
                return NutritionSuggestion.invalid(data.get("error").asText());
            }
            
            if (!data.get("valid").asBoolean()) {
                return NutritionSuggestion.invalid(data.get("reason").asText());
            }
            
            // Parse and validate nutrition data
            int calories = clamp(data.get("calories").asInt(), 800, 5000);
            int protein = clamp(data.get("protein").asInt(), 10, 400);
            int carbs = clamp(data.get("carbs").asInt(), 10, 600);
            int fats = clamp(data.get("fats").asInt(), 10, 250);
            double water = clamp(data.get("water").asDouble(), 1.0, 5.0);
            
            // Light macro correction
            int macroCalories = (protein * 4) + (carbs * 4) + (fats * 9);
            if (Math.abs(macroCalories - calories) > 200) {
                int remaining = calories - (protein * 4) - (fats * 9);
                carbs = Math.max(10, remaining / 4);
            }
            
            String description = data.get("description").asText();
            if (description.length() > 500) {
                description = description.substring(0, 500);
            }
            
            return NutritionSuggestion.success(description, calories, protein, carbs, fats, water);
            
        } catch (Exception e) {
            return getFallback(title, variation);
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
    
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, Math.round(value * 10.0) / 10.0));
    }
    
    private NutritionSuggestion getFallback(String title, int variation) {
        String clean = title.toLowerCase().trim();
        
        NutritionSuggestion[][] presets = {
            // Weight Loss
            {
                NutritionSuggestion.success("Tailored for \"" + title + "\": high-protein deficit to burn fat while keeping muscle.", 1800, 170, 140, 55, 3.0),
                NutritionSuggestion.success("For \"" + title + "\": moderate carbs with steady calorie deficit.", 1650, 145, 160, 50, 2.8),
                NutritionSuggestion.success("\"" + title + "\" plan: low-carb with higher fats for appetite control.", 1700, 155, 80, 95, 3.2),
                NutritionSuggestion.success("Aggressive \"" + title + "\": very high protein, controlled calories.", 1550, 180, 100, 50, 3.5)
            },
            // Muscle Gain
            {
                NutritionSuggestion.success("Clean bulk for \"" + title + "\": high protein with moderate surplus.", 2800, 185, 330, 75, 3.5),
                NutritionSuggestion.success("\"" + title + "\" fuel: carb-heavy for max training energy.", 3000, 170, 400, 70, 3.0),
                NutritionSuggestion.success("Lean \"" + title + "\": controlled surplus with extra protein.", 2600, 200, 280, 70, 3.2),
                NutritionSuggestion.success("Aggressive \"" + title + "\": big surplus for max gains.", 3200, 190, 380, 90, 3.5)
            },
            // Keto/Low Carb
            {
                NutritionSuggestion.success("Your \"" + title + "\": very low carb, high fat for ketosis.", 2000, 130, 30, 155, 3.0),
                NutritionSuggestion.success("Modified \"" + title + "\": more protein for active days.", 2100, 160, 35, 145, 3.2),
                NutritionSuggestion.success("Targeted \"" + title + "\" with peri-workout carbs.", 2200, 150, 50, 150, 2.8),
                NutritionSuggestion.success("Strict \"" + title + "\": ultra high protein, near-zero carbs.", 1900, 180, 20, 120, 3.5)
            },
            // General/Balanced
            {
                NutritionSuggestion.success("Balanced plan for \"" + title + "\": well-rounded macros.", 2200, 150, 250, 70, 2.5),
                NutritionSuggestion.success("Performance \"" + title + "\": higher carbs for energy.", 2400, 140, 300, 75, 3.0),
                NutritionSuggestion.success("Protein-first \"" + title + "\": great for recomp.", 2100, 175, 210, 65, 2.8),
                NutritionSuggestion.success("Mediterranean \"" + title + "\": healthy fats focus.", 2300, 130, 270, 85, 2.5)
            }
        };
        
        int presetIndex = 3; // Default to general
        if (clean.contains("loss") || clean.contains("cut") || clean.contains("lean") || clean.contains("diet") || clean.contains("lose")) {
            presetIndex = 0;
        } else if (clean.contains("bulk") || clean.contains("muscle") || clean.contains("gain") || clean.contains("mass")) {
            presetIndex = 1;
        } else if (clean.contains("keto") || clean.contains("low carb")) {
            presetIndex = 2;
        }
        
        return presets[presetIndex][variation % presets[presetIndex].length];
    }
    
    /**
     * Result class for nutrition suggestions
     */
    public static class NutritionSuggestion {
        private final boolean valid;
        private final String reason;
        private final String description;
        private final int calories;
        private final int protein;
        private final int carbs;
        private final int fats;
        private final double water;
        
        private NutritionSuggestion(boolean valid, String reason, String description,
                                  int calories, int protein, int carbs, int fats, double water) {
            this.valid = valid;
            this.reason = reason;
            this.description = description;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fats = fats;
            this.water = water;
        }
        
        public static NutritionSuggestion success(String description, int calories, int protein, int carbs, int fats, double water) {
            return new NutritionSuggestion(true, null, description, calories, protein, carbs, fats, water);
        }
        
        public static NutritionSuggestion invalid(String reason) {
            return new NutritionSuggestion(false, reason, null, 0, 0, 0, 0, 0);
        }
        
        public static NutritionSuggestion valid() {
            return new NutritionSuggestion(true, null, null, 0, 0, 0, 0, 0);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getReason() { return reason; }
        public String getDescription() { return description; }
        public int getCalories() { return calories; }
        public int getProtein() { return protein; }
        public int getCarbs() { return carbs; }
        public int getFats() { return fats; }
        public double getWater() { return water; }
        
        @Override
        public String toString() {
            if (!valid) {
                return "Invalid: " + reason;
            }
            return String.format("%s - %d cal, %dg protein, %dg carbs, %dg fats, %.1fL water",
                               description, calories, protein, carbs, fats, water);
        }
    }
}