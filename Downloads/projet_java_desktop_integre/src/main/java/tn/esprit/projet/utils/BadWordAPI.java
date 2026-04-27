package tn.esprit.projet.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BadWordAPI {

    private static final String API_URL = "https://vector.profanity.dev";
    private static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    // Local fallback list in case internet is down
    private static final String[] FALLBACK_WORDS = {"fuck", "shit", "bitch", "asshole", "pussy", "dick"};

    public static boolean hasProfanity(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        String lowerText = text.toLowerCase();

        // 1. Quick local check (always works)
        for (String word : FALLBACK_WORDS) {
            if (lowerText.contains(word)) return true;
        }

        // 2. External API check (vector.profanity.dev)
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonInput = "{\"text\": \"" + text.replace("\"", "\\\"") + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse response: {"isProfane": true}
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response.body());
            return rootNode.path("isProfane").asBoolean();

        } catch (Exception e) {
            System.err.println("API Check failed (Offline?): " + e.getMessage());
            return false;
        }
    }
}
