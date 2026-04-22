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

    private static final String API_KEY = "AIzaSyC-E-osVbPoQQ05jDsTBSjw7amQcTUoAl4";
    private static String activeModel = "gemini-1.5-flash"; // Modèle par défaut
    private static String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + activeModel + ":generateContent?key=" + API_KEY;
    private static boolean modelDiscovered = false;

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
        if (API_KEY == null || API_KEY.equals("VOTRE_CLE_API_GEMINI_ICI") || API_KEY.isBlank()) {
            System.err.println("[AIService] Erreur : Clé API non configurée.");
            return "";
        }

        if (!modelDiscovered) {
            discoverBestModel();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            
            // Création du corps de la requête avec Jackson
            ObjectNode rootNode = mapper.createObjectNode();
            ArrayNode contentsArray = rootNode.putArray("contents");
            ObjectNode contentNode = contentsArray.addObject();
            ArrayNode partsArray = contentNode.putArray("parts");
            partsArray.addObject().put("text", prompt);

            String jsonInputString = mapper.writeValueAsString(rootNode);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                // Lire le message d'erreur de l'API
                Scanner errorScanner = new Scanner(conn.getErrorStream(), StandardCharsets.UTF_8);
                String errorResponse = errorScanner.useDelimiter("\\A").hasNext() ? errorScanner.next() : "Pas de message d'erreur";
                errorScanner.close();
                System.err.println("[AIService] Erreur API Gemini (Code " + code + ") : " + errorResponse);
                return "";
            }

            Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            // Extraction robuste avec Jackson
            JsonNode responseJson = mapper.readTree(response);
            JsonNode candidates = responseJson.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        return parts.get(0).get("text").asText();
                    }
                }
            }
            
            System.err.println("[AIService] Structure de réponse inattendue : " + response);

        } catch (Exception e) {
            System.err.println("[AIService] Exception lors de l'appel : " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

    private static void discoverBestModel() {
        try {
            System.out.println("[AIService] Recherche des modèles disponibles...");
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                Scanner s = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8);
                String response = s.useDelimiter("\\A").next();
                s.close();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);
                JsonNode models = root.get("models");

                if (models != null && models.isArray()) {
                    for (JsonNode model : models) {
                        String name = model.get("name").asText();
                        JsonNode methods = model.get("supportedGenerationMethods");
                        boolean supportsGenerate = false;
                        if (methods != null && methods.isArray()) {
                            for (JsonNode m : methods) {
                                if (m.asText().equals("generateContent")) supportsGenerate = true;
                            }
                        }

                        if (supportsGenerate && (name.contains("flash") || name.contains("pro"))) {
                            activeModel = name.replace("models/", "");
                            API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + activeModel + ":generateContent?key=" + API_KEY;
                            System.out.println("[AIService] Modèle détecté et activé : " + activeModel);
                            modelDiscovered = true;
                            return;
                        }
                    }
                }
            } else {
                System.err.println("[AIService] Impossible de lister les modèles. Code : " + conn.getResponseCode());
            }
        } catch (Exception e) {
            System.err.println("[AIService] Erreur lors de la découverte : " + e.getMessage());
        }
        modelDiscovered = true; // On ne réessaie qu'une fois
    }
}
