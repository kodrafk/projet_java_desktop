package tn.esprit.projet.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiService {

    private static final String API_KEY = "AIzaSyAllBVWNo1L2PuobmyhNqfUaTZ9oQa7fWg";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String generateContent(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Safer JSON construction for Gemini
            java.util.Map<String, Object> part = java.util.Map.of("text", prompt);
            java.util.Map<String, Object> content = java.util.Map.of("parts", java.util.List.of(part));
            java.util.Map<String, Object> bodyMap = java.util.Map.of("contents", java.util.List.of(content));
            String jsonInput = mapper.writeValueAsString(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("Gemini API Error (Code " + response.statusCode() + "): " + response.body());
                return null;
            }

            // Parse response: candidates[0].content.parts[0].text
            JsonNode rootNode = mapper.readTree(response.body());
            if (rootNode.has("candidates") && rootNode.path("candidates").size() > 0) {
                JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
                return textNode.asText().trim();
            } else {
                System.err.println("Gemini Response is empty: " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.err.println("Gemini Service Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String suggestTitle(String description) {
        String prompt = "Give me ONLY a short formal title (max 6 words) for this complaint: " + description;
        return generateContent(prompt);
    }

    public static String suggestResponse(String description) {
        String prompt = "Reply to this customer complaint politely as a support admin in 2-3 sentences max: " + description;
        return generateContent(prompt);
    }

    public static String suggestDetailedResponse(String title, String description, int rating) {
        String prompt = "As a professional customer support manager, write a polite and professional response to this complaint:\n" +
                       "Title: " + title + "\n" +
                       "Description: " + description + "\n" +
                       "Rating: " + rating + "/5\n" +
                       "Provide a response that acknowledges the issue, apologizes if appropriate, and offers a solution. Keep it to 3-4 sentences.";
        return generateContent(prompt);
    }
}
