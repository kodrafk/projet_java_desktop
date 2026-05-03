package tn.esprit.projet.services;

import tn.esprit.projet.models.Ingredient;
import tn.esprit.projet.models.Recette;
import tn.esprit.projet.models.RecetteIngredient;
import tn.esprit.projet.models.RecipeRecommendation;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeRecommendationService {

    private final RecetteService recetteService;
    private final IngredientService ingredientService;
    private final RecetteFavorisService favorisService;

    private static final double WEIGHT_COSINE = 0.6;
    private static final double WEIGHT_STOCK = 0.4;
    private static final double FAVORITE_PENALTY = 0.3;
    private static final List<String> ALL_TYPES = Arrays.asList("entree", "main dish", "dessert", "drinks");
    private static final List<String> ALL_DIFFICULTIES = Arrays.asList("easy", "medium", "hard");
    private static final double MAX_TIME = 180.0;

    public RecipeRecommendationService() {
        this.recetteService = new RecetteService();
        this.ingredientService = new IngredientService();
        this.favorisService = new RecetteFavorisService();
    }

    public List<RecipeRecommendation> getRecommendations(int userId, int limit) {
        List<Recette> allRecettes = recetteService.getAll();
        List<Ingredient> stockIngredients = ingredientService.getAll();
        List<Integer> favoriteIds = favorisService.getFavoriteIds(userId);

        if (allRecettes.isEmpty()) return new ArrayList<>();

        Set<Integer> allIngredientIdsSet = new LinkedHashSet<>();
        for (Recette r : allRecettes) {
            if (r.getRecetteIngredients() != null)
                for (RecetteIngredient ri : r.getRecetteIngredients())
                    allIngredientIdsSet.add(ri.getIngredientId());
        }
        List<Integer> allIngredientIds = new ArrayList<>(allIngredientIdsSet);

        Set<Integer> stockIds = stockIngredients.stream()
                .filter(i -> i.getQuantite() > 0).map(Ingredient::getId).collect(Collectors.toSet());

        double[] userProfile = buildUserProfile(allRecettes, favoriteIds, userId, allIngredientIds);
        if (isVectorEmpty(userProfile)) userProfile = buildStockBasedProfile(stockIds, allIngredientIds);

        List<RecipeRecommendation> recommendations = new ArrayList<>();
        for (Recette recette : allRecettes) {
            double[] recipeVector = buildRecipeVector(recette, allIngredientIds);
            double cosine = cosineSimilarity(userProfile, recipeVector);
            double stockCoverage = calculateStockCoverage(recette, stockIds);
            double finalScore = (WEIGHT_COSINE * cosine) + (WEIGHT_STOCK * stockCoverage);
            if (favoriteIds.contains(recette.getId())) finalScore *= (1.0 - FAVORITE_PENALTY);
            finalScore = Math.max(0.0, Math.min(1.0, finalScore));
            List<String> missing = getMissingIngredients(recette, stockIds);
            recommendations.add(new RecipeRecommendation(recette, finalScore, missing, stockCoverage));
        }

        return recommendations.stream()
                .sorted(Comparator.comparingDouble(RecipeRecommendation::getMatchScore).reversed())
                .limit(limit).collect(Collectors.toList());
    }

    private double[] buildUserProfile(List<Recette> allRecettes, List<Integer> favoriteIds, int userId, List<Integer> allIngredientIds) {
        int vectorSize = getVectorSize(allIngredientIds);
        double[] profile = new double[vectorSize];
        double totalWeight = 0.0;
        for (Recette recette : allRecettes) {
            double weight = 0.0;
            if (favoriteIds.contains(recette.getId())) weight += 2.0;
            if (recette.getUserId() == userId) weight += 1.0;
            if (weight > 0) {
                double[] v = buildRecipeVector(recette, allIngredientIds);
                for (int i = 0; i < vectorSize; i++) profile[i] += v[i] * weight;
                totalWeight += weight;
            }
        }
        if (totalWeight > 0) for (int i = 0; i < vectorSize; i++) profile[i] /= totalWeight;
        return profile;
    }

    private double[] buildStockBasedProfile(Set<Integer> stockIds, List<Integer> allIngredientIds) {
        double[] profile = new double[getVectorSize(allIngredientIds)];
        for (int i = 0; i < allIngredientIds.size(); i++)
            if (stockIds.contains(allIngredientIds.get(i))) profile[i] = 1.0;
        return profile;
    }

    private double[] buildRecipeVector(Recette recette, List<Integer> allIngredientIds) {
        int vectorSize = getVectorSize(allIngredientIds);
        double[] vector = new double[vectorSize];
        Set<Integer> recipeIngredientIds = new HashSet<>();
        if (recette.getRecetteIngredients() != null)
            for (RecetteIngredient ri : recette.getRecetteIngredients())
                recipeIngredientIds.add(ri.getIngredientId());
        for (int i = 0; i < allIngredientIds.size(); i++)
            vector[i] = recipeIngredientIds.contains(allIngredientIds.get(i)) ? 1.0 : 0.0;
        int offset = allIngredientIds.size();
        String type = recette.getType() != null ? recette.getType().toLowerCase() : "";
        for (int i = 0; i < ALL_TYPES.size(); i++) vector[offset + i] = ALL_TYPES.get(i).equals(type) ? 1.0 : 0.0;
        offset += ALL_TYPES.size();
        String diff = recette.getDifficulte() != null ? recette.getDifficulte().toLowerCase() : "";
        for (int i = 0; i < ALL_DIFFICULTIES.size(); i++) vector[offset + i] = ALL_DIFFICULTIES.get(i).equals(diff) ? 1.0 : 0.0;
        offset += ALL_DIFFICULTIES.size();
        vector[offset] = Math.min(1.0, recette.getTempsPreparation() / MAX_TIME);
        return vector;
    }

    private int getVectorSize(List<Integer> allIngredientIds) { return allIngredientIds.size() + ALL_TYPES.size() + ALL_DIFFICULTIES.size() + 1; }

    private double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) { dot += a[i] * b[i]; normA += a[i] * a[i]; normB += b[i] * b[i]; }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double calculateStockCoverage(Recette recette, Set<Integer> stockIds) {
        if (recette.getRecetteIngredients() == null || recette.getRecetteIngredients().isEmpty()) return 1.0;
        long available = recette.getRecetteIngredients().stream().filter(ri -> stockIds.contains(ri.getIngredientId())).count();
        return (double) available / recette.getRecetteIngredients().size();
    }

    private List<String> getMissingIngredients(Recette recette, Set<Integer> stockIds) {
        if (recette.getRecetteIngredients() == null) return new ArrayList<>();
        return recette.getRecetteIngredients().stream()
                .filter(ri -> !stockIds.contains(ri.getIngredientId()))
                .map(ri -> ri.getIngredientNom() != null ? ri.getIngredientNom() : "Ingredient #" + ri.getIngredientId())
                .collect(Collectors.toList());
    }

    private boolean isVectorEmpty(double[] vector) { for (double v : vector) if (v != 0.0) return false; return true; }
}
