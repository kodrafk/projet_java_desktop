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
    private static final String API_KEY  = System.getProperty("openrouter.api.key", "YOUR_OPENROUTER_API_KEY");
    private static final String API_URL  = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL    = "google/gemini-2.0-flash-001";

    // ─── Unsplash Config ──────────────────────────────
    private static final String UNSPLASH_URL = "https://api.unsplash.com/photos/random";
    private static final String UNSPLASH_KEY = System.getProperty("unsplash.api.key", "YOUR_UNSPLASH_KEY");

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

        System.out.println("🤖 Étape 4 : Génération image Unsplash...");
        String imageUrl = fetchUnsplashImage(result.getImageKeywords(),
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
                    "Step 2 description",
                    "Step 3 description"
                  ],
                  "nutrition": {
                    "calories": 450,
                    "proteines": 18.5,
                    "lipides": 12.0,
                    "glucides": 62.0
                  },
                  "image_keywords": "pizza tunisian spicy food photography"
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
    // ÉTAPE 4 — Fetch image depuis Unsplash
    // ═════════════════════════════════════════════════
    private String fetchUnsplashImage(String keywords, String imageStyle) {

        try {
            // Construire query selon style visuel
            String styleQuery = getStyleQuery(imageStyle);
            String fullQuery  = keywords + " " + styleQuery + " food";

            // Encoder la query
            String encodedQuery = fullQuery.replace(" ", "%20");

            String url = UNSPLASH_URL
                    + "?query="       + encodedQuery
                    + "&orientation=" + "landscape"
                    + "&content_filter=high"
                    + "&client_id="   + UNSPLASH_KEY;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept-Version", "v1")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode json     = mapper.readTree(response.body());
                String   imageUrl = json.path("urls").path("regular").asText();

                System.out.println("✅ Image Unsplash : " + imageUrl);
                return imageUrl;
            }

        } catch (Exception e) {
            System.err.println("❌ Unsplash error : " + e.getMessage());
        }

        // Fallback → image par défaut food
        return "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"
                + "?w=800&q=80";
    }

    // ─── Style visuel → query Unsplash ───────────────
    private String getStyleQuery(String imageStyle) {
        if (imageStyle == null) return "professional";

        switch (imageStyle) {
            case "Professional" -> { return "professional studio photography"; }
            case "Rustic"       -> { return "rustic wooden background"; }
            case "Minimalist"   -> { return "minimalist clean white background"; }
            case "Dark & Moody" -> { return "dark moody dramatic lighting"; }
            case "Colorful"     -> { return "colorful bright vibrant"; }
            default             -> { return "professional food photography"; }
        }
    }

    // ═════════════════════════════════════════════════
    // REGÉNÉRER IMAGE SEULEMENT
    // ═════════════════════════════════════════════════
    public String regenerateImage(String keywords, String imageStyle) {
        try {
            return fetchUnsplashImage(keywords, imageStyle);
        } catch (Exception e) {
            System.err.println("❌ regenerateImage : " + e.getMessage());
            return "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"
                    + "?w=800&q=80";
        }
    }
}