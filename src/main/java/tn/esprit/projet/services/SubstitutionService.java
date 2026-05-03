package tn.esprit.projet.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import tn.esprit.projet.models.Ingredient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SubstitutionService {

    private static final String API_KEY = "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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String MODEL = "google/gemini-2.0-flash-001";
    private static final ObjectMapper mapper = new ObjectMapper();

    private final IngredientService ingredientService;

    public static class Substitution {
        private String substituteName;
        private double ratio;
        private double originalReferenceQuantity;
        private String substituteUnit;
        private String rawText;

        public Substitution(String name, double ratio, double forQty, String unit, String raw) {
            this.substituteName = name; this.ratio = ratio;
            this.originalReferenceQuantity = forQty; this.substituteUnit = unit; this.rawText = raw;
        }

        public String getSubstituteName() { return substituteName; }
        public double getRatio() { return ratio; }
        public double getOriginalReferenceQuantity() { return originalReferenceQuantity; }
        public String getSubstituteUnit() { return substituteUnit; }
        public String getRawText() { return rawText; }
    }

    public SubstitutionService() { this.ingredientService = new IngredientService(); }

    public List<Substitution> findSubstitutes(Ingredient outOfStock) {
        List<Substitution> substitutes = new ArrayList<>();
        try {
            List<Ingredient> availableStock = ingredientService.getAll().stream()
                    .filter(i -> i.getId() != outOfStock.getId() && i.getQuantite() > 0)
                    .collect(Collectors.toList());
            if (availableStock.isEmpty()) return substitutes;

            String stockList = availableStock.stream()
                    .map(i -> i.getNom() + " (" + i.getQuantite() + " " + (i.getUnite() != null ? i.getUnite() : "") + ")")
                    .collect(Collectors.joining(", "));

            String prompt = "You are a Professional Chef. The ingredient '" + outOfStock.getNom() + "' is missing. " +
                    "Inventory: [" + stockList + "]. Provide 1-3 culinary substitutes from the inventory. " +
                    "FORMAT: SUBSTITUTE: [name] | RATIO: [qty] | FOR: 100 | UNIT: [unit]. " +
                    "If no substitute exists, return: NONE";

            String responseText = callAPI(prompt);
            substitutes = parseResponse(responseText, outOfStock);
        } catch (Exception e) { System.err.println("SubstitutionService: " + e.getMessage()); }
        return substitutes;
    }

    private String callAPI(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("HTTP-Referer", "http://localhost");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000); conn.setReadTimeout(60000);

        String jsonBody = "{\"model\":\"" + MODEL + "\",\"messages\":[{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}],\"max_tokens\":500,\"temperature\":0.2}";
        try (OutputStream os = conn.getOutputStream()) { os.write(jsonBody.getBytes(StandardCharsets.UTF_8)); }

        if (conn.getResponseCode() != 200) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        String rawJson = reader.lines().collect(Collectors.joining("\n")); reader.close();

        JsonNode root = mapper.readTree(rawJson);
        JsonNode message = root.path("choices").get(0).path("message");
        String content = message.path("content").asText("");
        return content.isEmpty() ? message.path("reasoning").asText("") : content;
    }

    private List<Substitution> parseResponse(String response, Ingredient outOfStock) {
        List<Substitution> substitutes = new ArrayList<>();
        if (response == null || response.trim().equalsIgnoreCase("NONE")) return substitutes;
        Pattern pattern = Pattern.compile("SUBSTITUTE\\s*:\\s*\\[?([^|\\]\\n]+)\\]?\\s*\\|\\s*RATIO\\s*:\\s*\\[?([\\d.]+)\\]?\\s*\\|\\s*FOR\\s*:\\s*\\[?([\\d.]+)\\]?\\s*\\|\\s*UNIT\\s*:\\s*\\[?([^|\\]\\n]*)\\]?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(response.replace("**", "").replace("*", ""));
        while (matcher.find()) {
            String name = matcher.group(1).trim(); double ratio = safeDouble(matcher.group(2));
            double forQty = safeDouble(matcher.group(3)); String unit = matcher.group(4).trim();
            String outUnit = outOfStock.getUnite() != null ? outOfStock.getUnite() : "units";
            String raw = name + " → use " + ratio + " " + unit + " for every " + forQty + " " + outUnit + " of " + outOfStock.getNom();
            substitutes.add(new Substitution(name, ratio, forQty, unit, raw));
        }
        return substitutes;
    }

    private double safeDouble(String s) { try { return Double.parseDouble(s.replaceAll("[^0-9.]", "")); } catch (Exception e) { return 100.0; } }
    private String escapeJson(String t) { return t == null ? "" : t.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r"); }

    public List<Ingredient> getOutOfStockIngredients() {
        return ingredientService.getAll().stream().filter(i -> i.getQuantite() <= 0).collect(Collectors.toList());
    }
}
