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

    private static final String API_KEY = "AIzaSyDrPPwepFzZEC-km4L6wcV1BPdFV1pDg5Q";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private static final ObjectMapper mapper = new ObjectMapper();
    
    // Rate limiting for free tier (20 requests per day)
    private static final int DAILY_QUOTA = 20;
    private static LocalDate lastResetDate = LocalDate.now();
    private static AtomicInteger requestCount = new AtomicInteger(0);

    private static synchronized void checkAndUpdateQuota() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastResetDate)) {
            // New day, reset counter
            lastResetDate = today;
            requestCount.set(0);
            System.out.println("[GeminiService] Quota reset for new day");
        }
    }

    private static synchronized boolean canMakeRequest() {
        checkAndUpdateQuota();
        int current = requestCount.get();
        if (current >= DAILY_QUOTA) {
            System.err.println("[GeminiService] Daily quota exhausted (" + current + "/" + DAILY_QUOTA + ")");
            return false;
        }
        requestCount.incrementAndGet();
        System.out.println("[GeminiService] Request " + requestCount.get() + "/" + DAILY_QUOTA);
        return true;
    }

    public static String generateContent(String prompt) {
        // Check quota before making request
        if (!canMakeRequest()) {
            System.err.println("[generateContent] Quota limit reached for today. Please try again tomorrow.");
            return null;
        }
        
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
                    .header("X-goog-api-key", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();

            System.out.println("[generateContent] Sending request to Gemini API...");
            System.out.println("[generateContent] URL: " + API_URL);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("[generateContent] Response Status: " + response.statusCode());
            
            if (response.statusCode() != 200) {
                System.err.println("[generateContent] Gemini API Error (Code " + response.statusCode() + "): " + response.body());
                return null;
            }

            // Parse response: candidates[0].content.parts[0].text
            JsonNode rootNode = mapper.readTree(response.body());
            System.out.println("[generateContent] Response JSON: " + response.body());
            
            if (rootNode.has("candidates") && rootNode.path("candidates").size() > 0) {
                JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");
                if (textNode.isMissingNode()) {
                    System.err.println("[generateContent] Text node is missing in response");
                    return null;
                }
                String result = textNode.asText().trim();
                System.out.println("[generateContent] Successfully extracted text: " + result.substring(0, Math.min(50, result.length())) + "...");
                return result;
            } else {
                System.err.println("[generateContent] No candidates in response. Response: " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.err.println("[generateContent] Exception: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String suggestTitle(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        String prompt = "Give me ONLY a short formal title (max 6 words) for this complaint: " + description;
        System.out.println("[suggestTitle] Calling Gemini API...");
        String result = generateContent(prompt);
        if (result == null) {
            System.err.println("[suggestTitle] returned null for description: " + description);
        } else {
            System.out.println("[suggestTitle] Successfully generated title: " + result);
        }
        return result;
    }

    public static String suggestResponse(String description) {
        String prompt = "Reply to this customer complaint politely as a support admin in 2-3 sentences max: " + description;
        System.out.println("[suggestResponse] Calling Gemini API...");
        String result = generateContent(prompt);
        if (result == null) {
            System.err.println("[suggestResponse] returned null");
        }
        return result;
    }

    public static String suggestDetailedResponse(String title, String description, int rating) {
        String prompt = "As a professional customer support manager, write a polite and professional response to this complaint:\n" +
                       "Title: " + title + "\n" +
                       "Description: " + description + "\n" +
                       "Rating: " + rating + "/5\n" +
                       "Provide a response that acknowledges the issue, apologizes if appropriate, and offers a solution. Keep it to 3-4 sentences.";
        System.out.println("[suggestDetailedResponse] Calling Gemini API with prompt...");
        String result = generateContent(prompt);
        if (result == null || result.isEmpty()) {
            System.err.println("[suggestDetailedResponse] API returned null or empty response");
        } else {
            System.out.println("[suggestDetailedResponse] Successfully generated response: " + result.substring(0, Math.min(50, result.length())) + "...");
        }
        return result;
    }

    public static String analyzeEmotion(String title, String description) {
        String prompt = "Analyze the emotion in this customer complaint and respond ONLY with JSON format:\n" +
                       "{\n" +
                       "  \"emotion\": \"[ANGER/FRUSTRATION/DISAPPOINTMENT/NEUTRAL/SATISFACTION]\",\n" +
                       "  \"score\": [0-100],\n" +
                       "  \"urgency\": [1-5]\n" +
                       "}\n\n" +
                       "Complaint Title: " + title + "\n" +
                       "Complaint Description: " + description + "\n\n" +
                       "Guidelines:\n" +
                       "- emotion: Detect primary emotion (ANGER=very upset, FRUSTRATION=annoyed, DISAPPOINTMENT=let down, NEUTRAL=calm, SATISFACTION=happy)\n" +
                       "- score: 0-100 (intensity of emotion)\n" +
                       "- urgency: 1-5 (1=low priority, 5=critical/immediate attention needed)\n" +
                       "- ANGER or high frustration = urgency 4-5\n" +
                       "- DISAPPOINTMENT = urgency 2-3\n" +
                       "- NEUTRAL = urgency 1-2\n" +
                       "- SATISFACTION = urgency 1";
        return generateContent(prompt);
    }
}
