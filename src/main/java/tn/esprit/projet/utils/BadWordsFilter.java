package tn.esprit.projet.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Vérifie si un texte contient des mots inappropriés.
 * Utilise l'API Groq (LLaMA) comme backend — rapide, fiable, sans quota strict.
 * Fallback : fail-open (autorise le contenu si l'API est indisponible).
 */
public class BadWordsFilter {

    private static final String GROQ_KEY = System.getenv().getOrDefault("GROQ_API_KEY", "YOUR_GROQ_API_KEY_HERE");
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL    = "meta-llama/llama-4-scout-17b-16e-instruct";

    private static final boolean DEBUG = true;

    public static class Result {
        public final boolean isProfanity;
        public final double score;

        public Result(boolean isProfanity, double score) {
            this.isProfanity = isProfanity;
            this.score = score;
        }
    }

    /**
     * Vérifie si le texte contient des mots inappropriés via Groq/LLaMA.
     * @return Result avec isProfanity=true si le texte est inapproprié.
     *         En cas d'erreur réseau, retourne isProfanity=false (fail-open).
     */
    public static Result check(String text) {
        if (text == null || text.isBlank()) return new Result(false, 0.0);

        // Ignorer les textes qui sont juste des répétitions (aaaa, bbbb, ffff, etc.)
        String cleanText = text.replaceAll("\\s+", "").toLowerCase();
        if (cleanText.matches("(.)\\1{10,}")) {
            if (DEBUG) System.out.println("[BadWordsFilter] Ignored: repetitive text");
            return new Result(false, 0.0);
        }

        // Texte trop court — pas de vérification nécessaire
        if (text.trim().length() < 3) return new Result(false, 0.0);

        try {
            ObjectMapper mapper = new ObjectMapper();

            String prompt = "You are a content moderation system. Analyze the following text and determine if it contains profanity, hate speech, or highly inappropriate content.\n\n" +
                    "Text: \"" + text.replace("\"", "'") + "\"\n\n" +
                    "Respond ONLY with a JSON object in this exact format:\n" +
                    "{\"isProfanity\": true/false, \"score\": 0.0-1.0}\n\n" +
                    "Rules:\n" +
                    "- isProfanity: true only for clear profanity, hate speech, or explicit sexual content\n" +
                    "- score: confidence level (0.0 = clean, 1.0 = definitely inappropriate)\n" +
                    "- Normal words, hashtags, and marketing text are NOT profanity\n" +
                    "- score >= 0.90 means block the content\n" +
                    "Respond ONLY with the JSON, no other text.";

            String jsonBody = mapper.writeValueAsString(java.util.Map.of(
                "model", MODEL,
                "messages", java.util.List.of(
                    java.util.Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 60,
                "temperature", 0.0
            ));

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(6))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + GROQ_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("[BadWordsFilter] Groq API error " + response.statusCode() + " — allowing content (fail-open)");
                return new Result(false, 0.0);
            }

            JsonNode root = mapper.readTree(response.body());
            String content = root.path("choices").get(0).path("message").path("content").asText().trim();

            // Nettoyer les balises markdown si présentes
            content = content.replace("```json", "").replace("```", "").trim();

            if (DEBUG) System.out.println("[BadWordsFilter] Groq response: " + content);

            JsonNode resultNode = mapper.readTree(content);
            boolean isProfanity = resultNode.path("isProfanity").asBoolean(false);
            double score = resultNode.path("score").asDouble(0.0);

            // Seuil strict : bloquer seulement si score >= 0.90
            if (isProfanity && score < 0.90) {
                if (DEBUG) System.out.println("[BadWordsFilter] Score below threshold (0.90), allowing content");
                isProfanity = false;
            }

            if (DEBUG) System.out.println("[BadWordsFilter] isProfanity=" + isProfanity + ", score=" + score);
            return new Result(isProfanity, score);

        } catch (Exception e) {
            System.err.println("[BadWordsFilter] Error: " + e.getMessage());
            // Fail-open : autoriser le contenu si l'API est indisponible
            return new Result(false, 0.0);
        }
    }

    /**
     * Vérifie plusieurs champs en une seule passe (concatène avec espace).
     */
    public static Result checkAll(String... texts) {
        StringBuilder sb = new StringBuilder();
        for (String t : texts) {
            if (t != null && !t.isBlank()) {
                sb.append(t).append(" ");
            }
        }
        return check(sb.toString().trim());
    }
}
