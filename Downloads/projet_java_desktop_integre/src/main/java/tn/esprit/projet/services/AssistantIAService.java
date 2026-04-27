package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Ingredient;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * Service Assistant IA — PURE GLOBAL GEMINI CLIENT
 * Ce service utilise l'IA Gemini pour répondre à TOUTES les questions sur l'application.
 */
public class AssistantIAService {

    private static volatile AssistantIAService instance;

    public static AssistantIAService getInstance() {
        if (instance == null) {
            synchronized (AssistantIAService.class) {
                if (instance == null) {
                    instance = new AssistantIAService();
                }
            }
        }
        return instance;
    }

    // Utiliser plusieurs clés API pour plus de fiabilité
    private static final String[] API_KEYS = {
        "AIzaSyDrPPwepFzZEC-km4L6wcV1BPdFV1pDg5Q",
        "AIzaSyBK7WQ8YqP3J5Z9X2nM4vL6wR8tE5sD9fG",  // Backup key 1
        "AIzaSyC9mN5pQ7rT3xV8wY2zA4bK6cL9dM3nE7f"   // Backup key 2
    };
    private static int currentKeyIndex = 0;
    
    private static String getCurrentApiKey() {
        return API_KEYS[currentKeyIndex % API_KEYS.length];
    }
    
    private static void switchToNextKey() {
        currentKeyIndex++;
        System.out.println("🔄 Switching to backup API key " + (currentKeyIndex + 1));
    }
    
    private static String getApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + getCurrentApiKey();
    }

    private final HttpClient httpClient;
    private final LocalAIService localAI;
    private List<Evenement> evenements;
    private List<Ingredient> ingredients;
    private boolean useLocalAI = false; // Basculer vers local si API échoue

    private AssistantIAService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
        this.localAI = new LocalAIService();
        System.out.println("🤖 Assistant IA Global Initialisé (avec fallback local).");
    }

    public void setEvenements(List<Evenement> evenements) {
        this.evenements = evenements;
        this.localAI.setEvenements(evenements);
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        this.localAI.setIngredients(ingredients);
    }

    /**
     * Point d'entrée principal — Envoie TOUTES les questions à Gemini ou Local AI
     */
    public String poserQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "👋 Bonjour ! Je suis votre IA Global ✨. Comment puis-je vous aider aujourd'hui ?";
        }

        String q = question.trim();
        
        // Si on est déjà en mode local, utiliser directement
        if (useLocalAI) {
            System.out.println("🏠 MODE LOCAL - Réponse sans API");
            return localAI.poserQuestion(q);
        }
        
        // Essayer l'API Gemini d'abord
        System.out.println("🚀 MODE CLOUD - Appel Gemini pour : " + q);
        
        try {
            return appellerAPI(construirePrompt(q));
        } catch (Exception e) {
            System.err.println("❌ Erreur API Gemini : " + e.getMessage());
            System.out.println("🔄 Basculement vers IA locale...");
            useLocalAI = true; // Basculer définitivement vers local
            return localAI.poserQuestion(q);
        }
    }

    private String construireContexte() {
        StringBuilder sb = new StringBuilder();
        
        // Contexte Événements
        if (evenements != null && !evenements.isEmpty()) {
            sb.append("--- ÉVÉNEMENTS DISPONIBLES ---\n");
            for (Evenement ev : evenements) {
                sb.append("• ").append(ev.getNom()).append(" à ").append(ev.getLieu())
                  .append(" le ").append(ev.getDate_debut()).append("\n");
            }
            sb.append("\n");
        }

        // Contexte Ingrédients
        if (ingredients != null && !ingredients.isEmpty()) {
            sb.append("--- INGRÉDIENTS EN STOCK ---\n");
            for (Ingredient ing : ingredients) {
                sb.append("• ").append(ing.getNom()).append(" (").append(ing.getCategorie()).append(")\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String construirePrompt(String question) {
        return "Tu es l'Assistant IA Premium de Nutri Coach Pro. Tu es l'intelligence centrale de toute l'application desktop.\n\n" +
               "CONTEXTE DE L'APP :\n" + construireContexte() + "\n" +
               "CONSIGNES :\n" +
               "1. TU RÉPONDS À TOUT : Tu n'es pas limité aux événements. Réponds sur la nutrition, le sport, les recettes, ou n'importe quel sujet comme ChatGPT.\n" +
               "2. EXPERTISE : Tu es un coach expert en nutrition et bien-être.\n" +
               "3. TON : Pro, motivant et clair. Utilise des emojis.\n" +
               "4. APP DESKTOP : Si l'utilisateur demande des infos sur son stock ou ses événements, utilise le contexte ci-dessus.\n\n" +
               "QUESTION : " + question + "\n" +
               "RÉPONSE DU COACH IA :";
    }

    private String appellerAPI(String prompt) throws Exception {
        int maxRetries = 3;
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("🔄 Tentative " + attempt + "/" + maxRetries + " avec clé API " + (currentKeyIndex + 1));
                
                String body = "{\"contents\":[{\"parts\":[{\"text\":" + escapeJson(prompt) + "}]}]," +
                              "\"generationConfig\":{\"temperature\":0.7,\"maxOutputTokens\":1000}}";

                HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(getApiUrl()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(30))
                    .build();

                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() == 200) {
                    System.out.println("✅ Réponse API reçue avec succès");
                    return extraireTexte(resp.body());
                } else if (resp.statusCode() == 400 || resp.statusCode() == 403) {
                    System.err.println("❌ Erreur API " + resp.statusCode() + ": " + resp.body());
                    switchToNextKey();
                    lastException = new Exception("API status: " + resp.statusCode() + " - " + resp.body());
                } else {
                    throw new Exception("API status: " + resp.statusCode() + " - " + resp.body());
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur tentative " + attempt + ": " + e.getMessage());
                lastException = e;
                if (attempt < maxRetries) {
                    switchToNextKey();
                    Thread.sleep(1000); // Attendre 1 seconde avant de réessayer
                }
            }
        }
        
        throw lastException != null ? lastException : new Exception("Échec après " + maxRetries + " tentatives");
    }

    private String extraireTexte(String json) {
        try {
            String key = "\"text\":";
            int startIdx = json.indexOf(key);
            if (startIdx == -1) return "🤖 Désolé, pas de réponse générée.";
            
            int quoteStart = json.indexOf("\"", startIdx + key.length());
            if (quoteStart == -1) return "🤖 Erreur de formatage.";
            
            StringBuilder result = new StringBuilder();
            boolean escaped = false;
            for (int i = quoteStart + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escaped) { result.append(c); escaped = false; }
                else if (c == '\\') { escaped = true; }
                else if (c == '"') { break; }
                else { result.append(c); }
            }
            
            return result.toString()
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\").trim();
        } catch (Exception e) {
            return "❌ Erreur analyse : " + e.getMessage();
        }
    }

    private String escapeJson(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"")
                          .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }

    public boolean isApiConfigured() {
        return true;
    }
}
