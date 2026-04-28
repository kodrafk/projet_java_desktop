package tn.esprit.projet.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class BadWordAPI {

    private static final String API_URL = "https://vector.profanity.dev";
    private static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    // Extended local fallback list for better coverage
    private static final List<String> FALLBACK_WORDS = Arrays.asList(
        "fuck", "shit", "bitch", "asshole", "pussy", "dick", "damn", "hell", 
        "bastard", "crap", "piss", "whore", "slut", "fag", "nigger", "retard",
        "idiot", "stupid", "moron", "dumb", "hate", "kill", "die", "murder"
    );

    public static boolean hasProfanity(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ");

        // 1. Quick local check (always works offline)
        for (String word : FALLBACK_WORDS) {
            if (lowerText.contains(word)) {
                System.out.println("⚠️ BadWordAPI: Detected inappropriate word: " + word);
                return true;
            }
        }

        // 2. External API check with timeout and error handling
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
                    
            String jsonInput = String.format("{\"text\": \"%s\"}", 
                text.replace("\"", "\\\"").replace("\n", "\\n"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "JavaApp/1.0")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response.body());
                boolean isProfane = rootNode.path("isProfane").asBoolean();
                
                if (isProfane) {
                    System.out.println("⚠️ BadWordAPI: External API detected inappropriate content");
                }
                
                return isProfane;
            } else {
                System.err.println("BadWordAPI: External API returned status: " + response.statusCode());
                return false; // Fallback to local check only
            }

        } catch (java.net.ConnectException e) {
            System.err.println("BadWordAPI: No internet connection - using local filter only");
            return false;
        } catch (java.net.http.HttpTimeoutException e) {
            System.err.println("BadWordAPI: API timeout - using local filter only");
            return false;
        } catch (Exception e) {
            System.err.println("BadWordAPI: API Check failed: " + e.getMessage());
            return false; // Don't block user if API fails
        }
    }

    // Method to test the API functionality
    public static void testAPI() {
        System.out.println("=== Testing BadWordAPI ===");
        
        // Test cases
        String[] testTexts = {
            "This is a clean message",
            "This contains a bad word: fuck",
            "Hello world",
            "You are an idiot",
            "This is shit quality"
        };
        
        for (String text : testTexts) {
            boolean result = hasProfanity(text);
            System.out.println("Text: \"" + text + "\" -> " + (result ? "❌ BLOCKED" : "✅ ALLOWED"));
        }
    }
}
