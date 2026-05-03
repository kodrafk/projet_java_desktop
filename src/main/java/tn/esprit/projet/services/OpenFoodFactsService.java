package tn.esprit.projet.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class OpenFoodFactsService {

    private static final String API_URL = "https://world.openfoodfacts.org/api/v2/product/";

    public JsonObject getProductInfo(String barcode) {
        try {
            // 1. Préparer l'URL
            URL url = new URL(API_URL + barcode + ".json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 2. Vérifier la réponse (200 = OK)
            if (conn.getResponseCode() != 200) {
                return null;
            }

            // 3. Parser le flux JSON avec GSON
            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();

            // 4. Vérifier si le produit a été trouvé par l'API
            if (jsonResponse.has("product")) {
                return jsonResponse.getAsJsonObject("product");
            }

        } catch (Exception e) {
            System.err.println("Erreur API Open Food Facts: " + e.getMessage());
        }
        return null;
    }

    // --- Méthodes utilitaires pour extraire les données facilement ---

    public String getProductName(JsonObject product) {
        return product.has("product_name") ? product.get("product_name").getAsString() : "Unknown Product";
    }

    public String getBrand(JsonObject product) {
        return product.has("brands") ? product.get("brands").getAsString() : "Unknown Brand";
    }

    public String getEcoScore(JsonObject product) {
        return product.has("ecoscore_grade") ? product.get("ecoscore_grade").getAsString().toUpperCase() : "N/A";
    }

    public String getImageUrl(JsonObject product) {
        return product.has("image_url") ? product.get("image_url").getAsString() : null;
    }

    public List<String> getAdditives(JsonObject product) {
        List<String> additives = new ArrayList<>();
        if (product.has("additives_tags")) {
            JsonArray tags = product.getAsJsonArray("additives_tags");
            for (JsonElement tag : tags) {
                // "en:e322" -> "E322"
                String code = tag.getAsString().replace("en:", "").toUpperCase();
                additives.add(code);
            }
        }
        return additives;
    }
}