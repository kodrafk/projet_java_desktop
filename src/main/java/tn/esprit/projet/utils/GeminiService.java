package tn.esprit.projet.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

public class GeminiService {

    private static final String API_KEY = "AIzaSyBxO-t07gZhkNmm6x9XwYBNAvH5ix__c5E";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final int DAILY_QUOTA = 20;
    private static LocalDate lastResetDate = LocalDate.now();
    private static AtomicInteger requestCount = new AtomicInteger(0);

    private static synchronized void checkAndUpdateQuota() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastResetDate)) {
            lastResetDate = today;
            requestCount.set(0);
        }
    }

    private static synchronized boolean canMakeRequest() {
        checkAndUpdateQuota();
        if (requestCount.get() >= DAILY_QUOTA) {
            System.err.println("[GeminiService] Daily quota exhausted (" + requestCount.get() + "/" + DAILY_QUOTA + ")");
            return false;
        }
        requestCount.incrementAndGet();
        return true;
    }

    public static String generateContent(String prompt) {
        if (!canMakeRequest()) return null;
        try {
            HttpClient client = HttpClient.newHttpClient();
            java.util.Map<String, Object> part = java.util.Map.of("text", prompt);
            java.util.Map<String, Object> content = java.util.Map.of("parts", java.util.List.of(part));
            java.util.Map<String, Object> bodyMap = java.util.Map.of("contents", java.util.List.of(content));
            String jsonInput = mapper.writeValueAsString(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("[GeminiService] Error " + response.statusCode() + ": " + response.body());
                return null;
            }
            JsonNode rootNode = mapper.readTree(response.body());
            if (rootNode.has("candidates") && rootNode.path("candidates").size() > 0) {
                JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
                if (!textNode.isMissingNode()) return textNode.asText().trim();
            }
        } catch (Exception e) {
            System.err.println("[GeminiService] Exception: " + e.getMessage());
        }
        return null;
    }

    public static String suggestTitle(String description) {
        if (description == null || description.trim().isEmpty()) return null;
        return generateContent("Give me ONLY a short formal title (max 6 words) for this complaint: " + description);
    }

    public static String suggestDetailedResponse(String title, String description, int rating) {
        String prompt = "As a professional customer support manager, write a polite and professional response to this complaint:\n" +
                       "Title: " + title + "\nDescription: " + description + "\nRating: " + rating + "/5\n" +
                       "Provide a response that acknowledges the issue, apologizes if appropriate, and offers a solution. Keep it to 3-4 sentences.";
        return generateContent(prompt);
    }

    public static String analyzeEmotion(String title, String description) {
        String prompt = "Analyze the emotion in this customer complaint and respond ONLY with JSON format:\n" +
                       "{\"emotion\": \"[ANGER/FRUSTRATION/DISAPPOINTMENT/NEUTRAL/SATISFACTION]\", \"score\": [0-100], \"urgency\": [1-5]}\n\n" +
                       "Complaint Title: " + title + "\nComplaint Description: " + description + "\n\n" +
                       "Guidelines:\n- ANGER or high frustration = urgency 4-5\n- DISAPPOINTMENT = urgency 2-3\n- NEUTRAL = urgency 1-2\n- SATISFACTION = urgency 1";
        return generateContent(prompt);
    }
}
