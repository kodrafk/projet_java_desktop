package tn.esprit.projet.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.projet.models.AIRecipeRequest;
import tn.esprit.projet.models.AIRecipeResult;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class AIRecipeService {

    // ─── OpenRouter Config ────────────────────────────
    private static final String API_KEY  = "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"; // ← ta clé
    private static final String API_URL  = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL    = "google/gemini-2.0-flash-001";

    // ─── Unsplash Config ──────────────────────────────
    private static final String PEXELS_URL = "https://api.pexels.com/v1/search";
    private static final String PEXELS_KEY = "K6f0ozU0tpZgDw69Zo1mb7nLKGN6ECr7GivPxbgcQeYuxhFv8ONOaV5W";
    // ↑ Inscription gratuite sur unsplash.com/developers

    private final HttpClient     httpClient;
    private final ObjectMapper   mapper;

    public AIRecipeService() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper     = new ObjectMapper();
    }

    // ═════════════════════════════════════════════════
    // MÉTHODE PRINCIPALE
    // ═════════════════════════════════════════════════
    public AIRecipeResult generateFullRecipe(AIRecipeRequest request) throws Exception {

        System.out.println("🤖 Étape 1 : Construction du prompt...");
        String prompt = buildPrompt(request);

        System.out.println("🤖 Étape 2 : Appel Gemini API...");
        String jsonResponse = callGeminiAPI(prompt);

        System.out.println("🤖 Étape 3 : Parsing réponse...");
        AIRecipeResult result = parseGeminiResponse(jsonResponse);

        System.out.println("🤖 Étape 4 : Génération image Pexels...");
        String imageUrl = fetchPexelsImage(result.getImageKeywords(),
                request.getImageStyle());
        result.setImageUrl(imageUrl);

        // Copier les diet flags depuis request → result
        result.setVegetarian   (request.isVegetarian());
        result.setVegan        (request.isVegan());
        result.setHalal        (request.isHalal());
        result.setContainsGluten (!request.isGlutenFree());
        result.setContainsLactose(!request.isNoLactose());
        result.setContainsNuts   (!request.isNoNuts());
        result.setContainsEggs   (!request.isNoEggs());

        System.out.println("✅ Recette générée : " + result.getNom());
        return result;
    }

    // ═════════════════════════════════════════════════
    // ÉTAPE 1 — Construire le Prompt
    // ═════════════════════════════════════════════════
    private String buildPrompt(AIRecipeRequest req) {
        return """
            You are a professional chef AI. Generate a complete recipe in JSON format.
            
            REQUIREMENTS:
            - Dish Type      : %s
            - Cuisine Style  : %s
            - Difficulty     : %s
            - Max Time       : %d minutes
            - Servings       : %d people
            - Calorie Range  : %s
            - Dietary        : %s
            - Extra Notes    : %s
            
            RESPOND ONLY WITH THIS EXACT JSON FORMAT (no markdown, no extra text):
            {
              "nom": "Recipe Name",
              "description": "Short appetizing description (2-3 sentences)",
              "type": "%s",
              "difficulte": "%s",
              "temps_preparation": %d,
              "servings": %d,
              "ingredients": [
                "250g Flour",
                "150ml Warm Water",
                "2 tbsp Olive Oil"
              ],
              "steps": [
                "Step 1 description",
                "Step 2 description"
              ],
              "nutrition": {
                "calories": 450,
                "proteines": 18.5,
                "lipides": 12.0,
                "glucides": 62.0
              },
              "image_keywords": "ONLY 2-3 words: the exact dish name and main ingredient. Example: pasta carbonara, chicken curry, chocolate cake. NO style words."
            }
            """.formatted(
                req.getDishType(),
                req.getCuisineStyle(),
                req.getDifficulty(),
                req.getMaxTime(),
                req.getServings(),
                req.getCalorieRange(),
                req.getDietarySummary(),
                req.getExtraInstructions() != null
                        ? req.getExtraInstructions() : "None",
                req.getDishType(),
                req.getDifficulty(),
                req.getMaxTime(),
                req.getServings()
        );
    }

    // ═════════════════════════════════════════════════
    // ÉTAPE 2 — Appeler Gemini via OpenRouter
    // ═════════════════════════════════════════════════
    private String callGeminiAPI(String prompt) throws Exception {

        // Corps de la requête JSON
        String requestBody = """
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "temperature": 0.8,
                  "max_tokens": 2000
                }
                """.formatted(
                MODEL,
                mapper.writeValueAsString(prompt)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type",  "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .header("HTTP-Referer",  "http://localhost")
                .header("X-Title",       "RecipeApp")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        System.out.println("📡 Status : " + response.statusCode());

        if (response.statusCode() != 200) {
            throw new Exception("❌ API Error : " + response.statusCode()
                    + " → " + response.body());
        }

        return response.body();
    }

    // ═════════════════════════════════════════════════
    // ÉTAPE 3 — Parser la réponse Gemini
    // ═════════════════════════════════════════════════
    private AIRecipeResult parseGeminiResponse(String jsonResponse) throws Exception {

        // Extraire le contenu texte de la réponse OpenRouter
        JsonNode root    = mapper.readTree(jsonResponse);
        String   content = root
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText();

        System.out.println("📄 Contenu reçu : " + content.substring(0,
                Math.min(100, content.length())) + "...");

        // Nettoyer le JSON (Gemini peut ajouter ```json ... ```)
        content = cleanJsonContent(content);

        // Parser le JSON recette
        JsonNode recipeJson = mapper.readTree(content);

        AIRecipeResult result = new AIRecipeResult();

        // ── Infos de base ──
        result.setNom              (recipeJson.path("nom").asText());
        result.setDescription      (recipeJson.path("description").asText());
        result.setType             (recipeJson.path("type").asText());
        result.setDifficulte       (recipeJson.path("difficulte").asText());
        result.setTempsPreparation (recipeJson.path("temps_preparation").asInt());
        result.setServings         (recipeJson.path("servings").asInt());
        result.setImageKeywords    (recipeJson.path("image_keywords").asText());

        // ── Ingrédients ──
        List<String> ingredients = new ArrayList<>();
        JsonNode ingredientsNode = recipeJson.path("ingredients");
        if (ingredientsNode.isArray()) {
            ingredientsNode.forEach(node -> ingredients.add(node.asText()));
        }
        result.setIngredients(ingredients);

        // ── Étapes ──
        List<String> steps = new ArrayList<>();
        JsonNode stepsNode = recipeJson.path("steps");
        if (stepsNode.isArray()) {
            stepsNode.forEach(node -> steps.add(node.asText()));
        }
        result.setSteps(steps);

        // ── Nutrition ──
        JsonNode nutrition = recipeJson.path("nutrition");
        result.setCalories  (nutrition.path("calories").asInt());
        result.setProteines ((float) nutrition.path("proteines").asDouble());
        result.setLipides   ((float) nutrition.path("lipides").asDouble());
        result.setGlucides  ((float) nutrition.path("glucides").asDouble());

        return result;
    }

    // ─── Nettoyer JSON (supprimer ```json ... ```) ────
    private String cleanJsonContent(String content) {
        content = content.trim();

        // Supprimer ```json au début
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }

        // Supprimer ``` à la fin
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }

        return content.trim();
    }


    // ═════════════════════════════════════════════════
// ÉTAPE 4 — Fetch image depuis Pexels
// ═════════════════════════════════════════════════
    private String fetchPexelsImage(String keywords, String imageStyle) {
        try {
            // Construire query précise
            String query = buildPexelsQuery(keywords, imageStyle);
            String encodedQuery = query.replace(" ", "%20");

            // Numéro de page aléatoire pour diversité
            int randomPage = (int)(Math.random() * 5) + 1;

            String url = PEXELS_URL
                    + "?query="       + encodedQuery
                    + "&per_page=5"
                    + "&page="        + randomPage
                    + "&orientation=landscape";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", PEXELS_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Pexels Status : " + response.statusCode());

            if (response.statusCode() == 200) {
                JsonNode json   = mapper.readTree(response.body());
                JsonNode photos = json.path("photos");

                if (photos.isArray() && photos.size() > 0) {
                    // Choisir une photo aléatoire parmi les résultats
                    int randomIndex = (int)(Math.random() * photos.size());
                    JsonNode photo  = photos.get(randomIndex);
                    String imageUrl = photo.path("src").path("large").asText();

                    System.out.println("✅ Pexels image : " + imageUrl);
                    return imageUrl;
                }
            }

            System.err.println("⚠️ Pexels no results → fallback");

        } catch (Exception e) {
            System.err.println("❌ Pexels error : " + e.getMessage());
        }

        return getFallbackImage();
    }

    /**
     * Construit une query Pexels précise
     * basée sur les keywords de la recette
     */
    private String buildPexelsQuery(String keywords, String imageStyle) {
        if (keywords == null || keywords.isBlank()) {
            return "food dish meal";
        }

        // Garder les 3 premiers mots les plus pertinents
        String[] words = keywords.trim().split("\\s+");
        StringBuilder query = new StringBuilder();

        int maxWords = Math.min(3, words.length);
        for (int i = 0; i < maxWords; i++) {
            String word = words[i]
                    .toLowerCase()
                    .replaceAll("[^a-zA-Z0-9]", "");
            if (!word.isBlank()) {
                if (query.length() > 0) query.append(" ");
                query.append(word);
            }
        }

        // Toujours ajouter food pour rester dans le contexte
        query.append(" food");

        System.out.println("🔍 Pexels query : " + query);
        return query.toString();
    }

    /**
     * Image fallback si Pexels échoue
     */
    private String getFallbackImage() {
        // Images food Pexels directes comme fallback
        String[] fallbacks = {
                "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg",
                "https://images.pexels.com/photos/1640772/pexels-photo-1640772.jpeg",
                "https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg",
                "https://images.pexels.com/photos/1279330/pexels-photo-1279330.jpeg",
                "https://images.pexels.com/photos/2097090/pexels-photo-2097090.jpeg"
        };
        return fallbacks[(int)(Math.random() * fallbacks.length)];
    }

    // ═════════════════════════════════════════════════
    // REGÉNÉRER IMAGE SEULEMENT
    // ═════════════════════════════════════════════════
    // ═════════════════════════════════════════════════
// REGÉNÉRER IMAGE — avec Pexels
// ═════════════════════════════════════════════════
    public String regenerateImage(String keywords, String imageStyle) {
        try {
            // Pexels retourne image différente grâce à la page aléatoire
            String newUrl = fetchPexelsImage(keywords, imageStyle);
            System.out.println("🔄 Regenerated image : " + newUrl);
            return newUrl;
        } catch (Exception e) {
            System.err.println("❌ regenerateImage : " + e.getMessage());
            return getFallbackImage();
        }
    }
}