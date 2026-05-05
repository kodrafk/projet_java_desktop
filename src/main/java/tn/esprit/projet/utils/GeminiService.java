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

    private static final String API_KEY = System.getenv().getOrDefault("GEMINI_API_KEY", "YOUR_GEMINI_API_KEY_HERE");
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent";
    private static final ObjectMapper mapper = new ObjectMapper();
    
    // Rate limiting for free tier
    private static final int DAILY_QUOTA = 1500;
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
        if (!canMakeRequest()) {
            System.err.println("[generateContent] Quota limit reached for today.");
            return null;
        }

        // Retry once after 429 with exponential backoff
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                HttpClient client = HttpClient.newHttpClient();

                java.util.Map<String, Object> part    = java.util.Map.of("text", prompt);
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

                if (response.statusCode() == 429) {
                    System.err.println("[generateContent] 429 Rate limit on attempt " + attempt + ". Waiting 20s...");
                    if (attempt < 2) {
                        Thread.sleep(20000);
                        continue;
                    }
                    System.err.println("[generateContent] Quota exhausted after retry.");
                    return null;
                }

                if (response.statusCode() != 200) {
                    System.err.println("[generateContent] Gemini API Error (Code " + response.statusCode() + "): " + response.body());
                    return null;
                }

                JsonNode rootNode = mapper.readTree(response.body());
                if (rootNode.has("candidates") && rootNode.path("candidates").size() > 0) {
                    JsonNode textNode = rootNode.path("candidates").get(0)
                            .path("content").path("parts").get(0).path("text");
                    if (!textNode.isMissingNode()) {
                        return textNode.asText().trim();
                    }
                }
                return null;

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                System.err.println("[generateContent] Exception: " + e.getMessage());
                return null;
            }
        }
        return null;
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
        // ── Groq config ──
        final String GROQ_KEY = System.getenv().getOrDefault("GROQ_API_KEY", "YOUR_GROQ_API_KEY_HERE");
        final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
        final String MODEL    = "meta-llama/llama-4-scout-17b-16e-instruct";

        String prompt = "As a professional customer support manager, write a polite and professional response to this complaint:\n" +
                "Title: " + title + "\n" +
                "Description: " + description + "\n" +
                "Rating: " + rating + "/5\n" +
                "Provide a response that acknowledges the issue, apologizes if appropriate, and offers a solution. Keep it to 3-4 sentences.";

        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonBody = mapper.writeValueAsString(java.util.Map.of(
                "model", MODEL,
                "messages", java.util.List.of(
                    java.util.Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 300,
                "temperature", 0.7
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + GROQ_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("[suggestDetailedResponse] Groq Error (" + response.statusCode() + "): " + response.body());
                return null;
            }

            JsonNode root = mapper.readTree(response.body());
            String content = root.path("choices").get(0).path("message").path("content").asText().trim();
            System.out.println("[suggestDetailedResponse] Groq response: " + content.substring(0, Math.min(80, content.length())) + "...");
            return content;

        } catch (Exception e) {
            System.err.println("[suggestDetailedResponse] Exception: " + e.getMessage());
            return null;
        }
    }

    public static String analyzeEmotion(String title, String description) {
        // ── Groq config (clé dédiée, pas de quota Gemini) ──
        final String GROQ_KEY = System.getenv().getOrDefault("GROQ_API_KEY", "YOUR_GROQ_API_KEY_HERE");
        final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
        final String MODEL    = "meta-llama/llama-4-scout-17b-16e-instruct";

        String prompt = "Analyze the emotion in this customer complaint and respond ONLY with JSON format:\n" +
                "{\n" +
                "  \"emotion\": \"[ANGER/FRUSTRATION/DISAPPOINTMENT/NEUTRAL/SATISFACTION]\",\n" +
                "  \"score\": [0-100],\n" +
                "  \"urgency\": [1-5]\n" +
                "}\n\n" +
                "Complaint Title: " + title + "\n" +
                "Complaint Description: " + description + "\n\n" +
                "Guidelines:\n" +
                "- emotion: ANGER=very upset, FRUSTRATION=annoyed, DISAPPOINTMENT=let down, NEUTRAL=calm, SATISFACTION=happy\n" +
                "- score: 0-100 intensity\n" +
                "- urgency: 1=low, 5=critical\n" +
                "- ANGER/high frustration = urgency 4-5\n" +
                "- DISAPPOINTMENT = urgency 2-3\n" +
                "- NEUTRAL = urgency 1-2\n" +
                "- SATISFACTION = urgency 1\n" +
                "Respond ONLY with the JSON object, no extra text.";

        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonBody = mapper.writeValueAsString(java.util.Map.of(
                "model", MODEL,
                "messages", java.util.List.of(
                    java.util.Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 200,
                "temperature", 0.1
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + GROQ_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("[analyzeEmotion] Groq Error (" + response.statusCode() + "): " + response.body());
                return null;
            }

            JsonNode root = mapper.readTree(response.body());
            String content = root.path("choices").get(0).path("message").path("content").asText().trim();

            // Nettoyer les balises markdown si présentes
            content = content.replace("```json", "").replace("```", "").trim();
            System.out.println("[analyzeEmotion] Groq response: " + content);
            return content;

        } catch (Exception e) {
            System.err.println("[analyzeEmotion] Exception: " + e.getMessage());
            return null;
        }
    }

    public static String generateEventDescription(String title, String coach, String lieu) {
        String prompt = "Write a professional and catchy description (max 3 sentences) for a sports/nutrition event called '" + title + 
                       "' with coach '" + coach + "' at location '" + lieu + "'.";
        return generateContent(prompt);
    }

    // ── Blog helpers using Groq (fast, no quota issues) ──────────────────────

    private static String callGroq(String prompt) {
        final String GROQ_KEY = System.getenv().getOrDefault("GROQ_API_KEY", "YOUR_GROQ_API_KEY_HERE");
        final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
        final String MODEL    = "meta-llama/llama-4-scout-17b-16e-instruct";

        try {
            HttpClient client = HttpClient.newHttpClient();

            String jsonBody = mapper.writeValueAsString(java.util.Map.of(
                "model", MODEL,
                "messages", java.util.List.of(
                    java.util.Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 150,
                "temperature", 0.7
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + GROQ_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("[callGroq] Error (" + response.statusCode() + "): " + response.body());
                return null;
            }

            JsonNode root = mapper.readTree(response.body());
            return root.path("choices").get(0).path("message").path("content").asText().trim();

        } catch (Exception e) {
            System.err.println("[callGroq] Exception: " + e.getMessage());
            return null;
        }
    }

    public static String generateBlogTitle(String content) {
        if (content == null || content.isBlank()) return null;
        String prompt = "Basé sur le texte suivant, génère UN SEUL titre court et accrocheur (maximum 10 mots) sans guillemets ni ponctuation finale : " + content;
        String result = callGroq(prompt);
        if (result != null) result = result.replaceAll("^\"|\"$", "").trim();
        return result;
    }

    public static String generateBlogHashtags(String content) {
        if (content == null || content.isBlank()) return null;
        String prompt = "Basé sur le texte suivant, génère uniquement une liste de 5 hashtags pertinents en français séparés par des espaces (format: #mot1 #mot2 ...) sans aucun autre texte : " + content;
        String result = callGroq(prompt);
        if (result != null) result = result.trim();
        return result;
    }
}

