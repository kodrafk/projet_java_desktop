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

    private static final List<String> FALLBACK_WORDS = Arrays.asList(
        "fuck", "shit", "bitch", "asshole", "pussy", "dick", "damn", "hell",
        "bastard", "crap", "piss", "whore", "slut", "fag", "nigger", "retard",
        "idiot", "stupid", "moron", "dumb", "hate", "kill", "die", "murder"
    );

    public static boolean hasProfanity(String text) {
        if (text == null || text.trim().isEmpty()) return false;

        String lowerText = text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", " ");

        // Local check first
        for (String word : FALLBACK_WORDS) {
            if (lowerText.contains(word)) {
                System.out.println("BadWordAPI: Detected inappropriate word: " + word);
                return true;
            }
        }

        // External API check
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
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response.body());
                return rootNode.path("isProfane").asBoolean();
            }
        } catch (Exception e) {
            System.err.println("BadWordAPI: " + e.getMessage());
        }
        return false;
    }
}
