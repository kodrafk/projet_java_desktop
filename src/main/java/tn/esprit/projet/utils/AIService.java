package tn.esprit.projet.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Service pour interagir avec l'IA Google Gemini.
 * Utilisé pour générer des titres et des hashtags.
 */
public class AIService {

    private static final String API_KEY = "AIzaSyD1d6efyplW1bN3OCry8KrUyxs321t6Ruc";

    /**
     * Génère un titre accrocheur basé sur le contenu.
     */
    public static String generateTitle(String content) {
        if (content == null || content.isBlank()) return "";
        String prompt = "Basé sur le texte suivant, génère UN SEUL titre court et accrocheur (maximum 10 mots) sans guillemets : " + content;
        return callGemini(prompt).trim();
    }

    /**
     * Génère des hashtags pertinents basés sur le contenu.
     */
    public static String generateHashtags(String content) {
        if (content == null || content.isBlank()) return "";
        String prompt = "Basé sur le texte suivant, génère uniquement une liste de 5 hashtags pertinents en français séparés par des espaces : " + content;
        return callGemini(prompt).trim();
    }

    private static String callGemini(String prompt) {
        if (API_KEY == null || API_KEY.isBlank()) return "";

        // URL exacte tirée de ton guide cURL
        String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + API_KEY;

        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            ArrayNode contentsArray = rootNode.putArray("contents");
            ObjectNode contentNode = contentsArray.addObject();
            ArrayNode partsArray = contentNode.putArray("parts");
            partsArray.addObject().put("text", prompt);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            // On peut aussi passer la clé par header pour plus de sécurité
            conn.setRequestProperty("X-goog-api-key", API_KEY);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(mapper.writeValueAsBytes(rootNode));
            }

            if (conn.getResponseCode() == 200) {
                Scanner s = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
                String response = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                s.close();

                JsonNode root = mapper.readTree(response);
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText("");
            } else {
                // Lecture de l'erreur détaillée pour la console
                Scanner s = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8);
                String err = s.useDelimiter("\\A").hasNext() ? s.next() : "";
                s.close();
                System.err.println("[AIService] Erreur API (Code " + conn.getResponseCode() + ") : " + err);
            }
        } catch (Exception e) {
            System.err.println("[AIService] Erreur : " + e.getMessage());
        }
        return "";
    }
}
