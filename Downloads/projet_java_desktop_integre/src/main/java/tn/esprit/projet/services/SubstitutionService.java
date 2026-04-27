package tn.esprit.projet.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tn.esprit.projet.models.Ingredient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SubstitutionService {

    // ✅ Clé API OpenRouter
    private static final String API_KEY = ""; // sk-or-v1-...

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    // Modèle rapide et intelligent sur OpenRouter
    private static final String MODEL = "google/gemini-2.0-flash-001";

    private final IngredientService ingredientService;

    // ═══════════════════════════════════
    // SUBSTITUTION DATA CLASS
    // ═══════════════════════════════════

    public static class Substitution {
        private String substituteName;
        private double ratio;
        private double originalReferenceQuantity;
        private String substituteUnit;
        private String rawText;

        public Substitution(String name, double ratio, double forQty, String unit, String raw) {
            this.substituteName = name;
            this.ratio = ratio;
            this.originalReferenceQuantity = forQty;
            this.substituteUnit = unit;
            this.rawText = raw;
        }

        public String getSubstituteName() { return substituteName; }
        public double getRatio() { return ratio; }
        public double getOriginalReferenceQuantity() { return originalReferenceQuantity; }
        public String getSubstituteUnit() { return substituteUnit; }
        public String getRawText() { return rawText; }
    }

    public SubstitutionService() {
        this.ingredientService = new IngredientService();
    }

    // ═══════════════════════════════════
    // MAIN PUBLIC METHOD
    // ═══════════════════════════════════

    public List<Substitution> findSubstitutes(Ingredient outOfStock) {
        List<Substitution> substitutes = new ArrayList<>();
        try {
            System.out.println("\n🔍 [DEBUG] Starting findSubstitutes for: " + outOfStock.getNom());

            List<Ingredient> allIngredients = ingredientService.getAll();
            List<Ingredient> availableStock = new ArrayList<>();

            for (Ingredient i : allIngredients) {
                if (i.getId() == outOfStock.getId()) continue;
                if (i.getQuantite() <= 0) continue;
                availableStock.add(i);
            }

            System.out.println("📦 [DEBUG] Available stock size: " + availableStock.size());

            if (availableStock.isEmpty()) {
                System.out.println("❌ [DEBUG] No ingredients in stock.");
                return substitutes;
            }

            String stockList = availableStock.stream()
                    .map(i -> i.getNom() + " (" + i.getQuantite() + " " + (i.getUnite() != null ? i.getUnite() : "") + ")")
                    .collect(Collectors.joining(", "));

            String prompt = buildPrompt(outOfStock, stockList);
            String responseText = callOpenRouterAPI(prompt);
            System.out.println("🤖 [DEBUG] AI Response:\n" + responseText);

            substitutes = parseResponse(responseText, outOfStock);
            System.out.println("✅ [DEBUG] Parsed " + substitutes.size() + " substitute(s).");

        } catch (Exception e) {
            System.err.println("❌ [ERROR] SubstitutionService: " + e.getMessage());
            e.printStackTrace();
        }
        return substitutes;
    }

    // ═══════════════════════════════════
    // PROMPT BUILDER
    // ═══════════════════════════════════

    private String buildPrompt(Ingredient outOfStock, String stockList) {
        return "You are a strict Professional Executive Chef. " +
                "The ingredient '" + outOfStock.getNom() + "' is missing. " +
                "Inventory: [" + stockList + "]. " +
                "Instruction: Provide 1 to 3 STRICT CULINARY SUBSTITUTES from the inventory. " +
                "STRICTNESS RULES: " +
                "1. A substitute must be a standard industry-recognized replacement (e.g., Honey for Sugar, Margarine for Butter). " +
                "2. NEVER suggest replacements that change the core nature of a dish (e.g., NEVER suggest Tomato Sauce or Juice for Water). " +
                "3. If the missing ingredient is a basic staple like WATER, SALT, or PEPPER, and there is no direct equivalent, suggest NOTHING. " +
                "4. If you are more than 10% unsure if the substitution is professional, suggest NOTHING. " +
                "5. If no perfect professional substitute exists, return exactly the word: NONE " +
                "FORMAT: " +
                "SUBSTITUTE: [name] | RATIO: [qty] | FOR: 100 | UNIT: [unit] " +
                "Only output the SUBSTITUTE lines or the word NONE. No conversation.";
    }

    // ═══════════════════════════════════
    // OPENROUTER API CALL
    // ═══════════════════════════════════

    private String callOpenRouterAPI(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setRequestProperty("HTTP-Referer", "http://localhost");
        connection.setRequestProperty("X-Title", "IngredientApp");
        connection.setDoOutput(true);
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);

        String jsonBody = "{"
                + "\"model\": \"" + MODEL + "\","
                + "\"messages\": [{"
                + "  \"role\": \"user\","
                + "  \"content\": \"" + escapeJson(prompt) + "\""
                + "}],"
                + "\"max_tokens\": 1000,"
                + "\"temperature\": 0.2"
                + "}";

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            String errorResponse = errorReader.lines().collect(Collectors.joining("\n"));
            errorReader.close();
            throw new RuntimeException("API Error " + responseCode + ": " + errorResponse);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String rawJson = reader.lines().collect(Collectors.joining("\n"));
        reader.close();

        return extractTextFromOpenRouter(rawJson);
    }

    // ═══════════════════════════════════
    // JSON EXTRACTION (OpenRouter format)
    // ═══════════════════════════════════

    private String extractTextFromOpenRouter(String jsonResponse) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonObject message = root
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message");

            String content = "";
            if (message.has("content") && !message.get("content").isJsonNull()) {
                content = message.get("content").getAsString();
            }

            // Fallback to 'reasoning' if 'content' is empty (common for reasoning models)
            if (content.isEmpty() && message.has("reasoning") && !message.get("reasoning").isJsonNull()) {
                content = message.get("reasoning").getAsString();
            }

            // Fallback to 'reasoning_details' if still empty
            if (content.isEmpty() && message.has("reasoning_details") && message.get("reasoning_details").isJsonArray()) {
                com.google.gson.JsonArray details = message.getAsJsonArray("reasoning_details");
                StringBuilder sb = new StringBuilder();
                for (com.google.gson.JsonElement el : details) {
                    if (el.isJsonObject() && el.getAsJsonObject().has("text")) {
                        sb.append(el.getAsJsonObject().get("text").getAsString());
                    }
                }
                content = sb.toString();
            }

            System.out.println("\n📄 [DEBUG] Text extracted (length: " + content.length() + "):\n" + content);
            return content;
        } catch (Exception e) {
            System.err.println("❌ [ERROR] Failed to parse OpenRouter response: " + e.getMessage());
            System.err.println("   Raw JSON: " + jsonResponse);
            return "";
        }
    }

    // ═══════════════════════════════════
    // RESPONSE PARSING
    // ═══════════════════════════════════

    private List<Substitution> parseResponse(String response, Ingredient outOfStock) {
        List<Substitution> substitutes = new ArrayList<>();
        if (response == null || response.isEmpty() || response.trim().equalsIgnoreCase("NONE")) return substitutes;

        // More robust regex: handle optional brackets [], optional bolding, and various spacing
        Pattern pattern = Pattern.compile(
                "SUBSTITUTE\\s*:\\s*\\[?([^|\\]\\n]+)\\]?\\s*\\|\\s*RATIO\\s*:\\s*\\[?([\\d.]+)\\]?\\s*\\|\\s*FOR\\s*:\\s*\\[?([\\d.]+)\\]?\\s*\\|\\s*UNIT\\s*:\\s*\\[?([^|\\]\\n]*)\\]?",
                Pattern.CASE_INSENSITIVE
        );

        String cleanResponse = response.replace("**", "").replace("*", "");
        Matcher matcher = pattern.matcher(cleanResponse);

        while (matcher.find()) {
            String name   = matcher.group(1).trim();
            double ratio  = safeParseDouble(matcher.group(2).trim());
            double forQty = safeParseDouble(matcher.group(3).trim());
            String unit   = matcher.group(4).trim();

            String outUnit = outOfStock.getUnite() != null ? outOfStock.getUnite() : "units";
            String raw = name + " → use " + ratio + " " + unit
                    + " for every " + forQty + " " + outUnit + " of " + outOfStock.getNom();

            System.out.println("✅ [PARSED] " + raw);
            substitutes.add(new Substitution(name, ratio, forQty, unit, raw));
        }

        if (substitutes.isEmpty()) {
            System.out.println("⚠️ [DEBUG] No matches found. AI response text was processed but regex didn't match.");
        }

        return substitutes;
    }

    // ═══════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════

    private double safeParseDouble(String text) {
        try {
            return Double.parseDouble(text.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 100.0;
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ═══════════════════════════════════
    // STOCK QUERIES
    // ═══════════════════════════════════

    public List<Ingredient> getOutOfStockIngredients() {
        return ingredientService.getAll().stream()
                .filter(i -> i.getQuantite() <= 0)
                .collect(Collectors.toList());
    }
}