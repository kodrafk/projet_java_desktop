package tn.esprit.projet.services;

import tn.esprit.projet.models.Ingredient;
import tn.esprit.projet.models.Recette;
import tn.esprit.projet.models.RecetteIngredient;
import tn.esprit.projet.models.RecipeRecommendation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Moteur de recommandation de recettes basé sur Content-Based Filtering.
 *
 * Algorithme :
 * 1. Construire le profil utilisateur (favoris + recettes créées + types préférés)
 * 2. Vectoriser chaque recette (ingrédients + type + difficulté + temps)
 * 3. Calculer similarité cosinus entre profil utilisateur et chaque recette
 * 4. Appliquer bonus si ingrédients disponibles en stock
 * 5. Retourner Top N recettes triées par score décroissant
 */
public class RecipeRecommendationService {

    // ═══════════ SERVICES ═══════════
    private final RecetteService recetteService;
    private final IngredientService ingredientService;
    private final RecetteFavorisService favorisService;

    // ═══════════ CONSTANTES ═══════════

    /** Poids de la similarité cosinus dans le score final */
    private static final double WEIGHT_COSINE = 0.6;

    /** Poids du bonus stock dans le score final */
    private static final double WEIGHT_STOCK = 0.4;

    /** Bonus appliqué si recette déjà favorite (éviter de recommander ce qu'on connaît) */
    private static final double FAVORITE_PENALTY = 0.3;

    /** Types de recettes disponibles (ordre fixe pour vectorisation one-hot) */
    private static final List<String> ALL_TYPES = Arrays.asList(
            "entree", "main dish", "dessert", "drinks"
    );

    /** Difficultés disponibles (ordre fixe pour vectorisation one-hot) */
    private static final List<String> ALL_DIFFICULTIES = Arrays.asList(
            "easy", "medium", "hard"
    );

    /** Temps maximum pour normalisation (en minutes) */
    private static final double MAX_TIME = 180.0;

    // ═══════════ CONSTRUCTEUR ═══════════

    public RecipeRecommendationService() {
        this.recetteService = new RecetteService();
        this.ingredientService = new IngredientService();
        this.favorisService = new RecetteFavorisService();
    }

    // ═══════════ MÉTHODE PRINCIPALE ═══════════

    /**
     * Retourne les N meilleures recettes recommandées pour un utilisateur.
     *
     * @param userId ID de l'utilisateur
     * @param limit  Nombre maximum de recommandations (ex: 5)
     * @return Liste triée par score décroissant
     */
    public List<RecipeRecommendation> getRecommendations(int userId, int limit) {

        // ── Étape 1 : Charger les données ──────────────────────────────────
        List<Recette> allRecettes = recetteService.getAll();
        List<Ingredient> stockIngredients = ingredientService.getAll();
        List<Integer> favoriteIds = favorisService.getFavoriteIds(userId);

        // Si pas de données → retourner liste vide
        if (allRecettes.isEmpty()) {
            return new ArrayList<>();
        }

        // ── Étape 2 : Construire l'index global des ingrédients ────────────
        // On collecte tous les ingredientId distincts présents dans toutes les recettes
        Set<Integer> allIngredientIdsSet = new LinkedHashSet<>();
        for (Recette r : allRecettes) {
            if (r.getRecetteIngredients() != null) {
                for (RecetteIngredient ri : r.getRecetteIngredients()) {
                    allIngredientIdsSet.add(ri.getIngredientId());
                }
            }
        }
        List<Integer> allIngredientIds = new ArrayList<>(allIngredientIdsSet);

        // ── Étape 3 : Construire le stock sous forme de Set d'IDs ─────────
        // On garde seulement les ingrédients avec quantité > 0
        Set<Integer> stockIds = stockIngredients.stream()
                .filter(i -> i.getQuantite() > 0)
                .map(Ingredient::getId)
                .collect(Collectors.toSet());

        // ── Étape 4 : Construire le profil utilisateur ────────────────────
        double[] userProfile = buildUserProfile(
                allRecettes, favoriteIds, userId, allIngredientIds
        );

        // Si profil vide (pas de favoris, pas de recettes créées)
        // → utiliser le stock comme profil de base
        if (isVectorEmpty(userProfile)) {
            userProfile = buildStockBasedProfile(stockIds, allIngredientIds);
        }

        // ── Étape 5 : Calculer le score pour chaque recette ───────────────
        List<RecipeRecommendation> recommendations = new ArrayList<>();

        for (Recette recette : allRecettes) {

            // Vectoriser la recette
            double[] recipeVector = buildRecipeVector(recette, allIngredientIds);

            // Calculer similarité cosinus
            double cosine = cosineSimilarity(userProfile, recipeVector);

            // Calculer couverture stock
            double stockCoverage = calculateStockCoverage(recette, stockIds);

            // Score final pondéré
            double finalScore = (WEIGHT_COSINE * cosine) + (WEIGHT_STOCK * stockCoverage);

            // Pénalité si déjà en favoris
            // (on évite de recommander ce que l'utilisateur connaît déjà)
            if (favoriteIds.contains(recette.getId())) {
                finalScore *= (1.0 - FAVORITE_PENALTY);
            }

            // Clamp entre 0 et 1
            finalScore = Math.max(0.0, Math.min(1.0, finalScore));

            // Calculer ingrédients manquants
            List<String> missingIngredients = getMissingIngredients(recette, stockIds);

            // Créer la recommandation
            RecipeRecommendation recommendation = new RecipeRecommendation(
                    recette,
                    finalScore,
                    missingIngredients,
                    stockCoverage
            );

            recommendations.add(recommendation);
        }

        // ── Étape 6 : Trier par score décroissant et retourner Top N ──────
        return recommendations.stream()
                .sorted(Comparator.comparingDouble(RecipeRecommendation::getMatchScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ═══════════ CONSTRUCTION DU PROFIL UTILISATEUR ═══════════

    /**
     * Construit le vecteur profil utilisateur basé sur :
     * - Les recettes en favoris (poids fort)
     * - Les recettes créées par l'utilisateur (poids moyen)
     * - La fréquence des types préférés
     *
     * Le profil est la moyenne pondérée des vecteurs des recettes "aimées".
     */
    private double[] buildUserProfile(List<Recette> allRecettes,
                                      List<Integer> favoriteIds,
                                      int userId,
                                      List<Integer> allIngredientIds) {

        int vectorSize = getVectorSize(allIngredientIds);
        double[] profile = new double[vectorSize];
        double totalWeight = 0.0;

        for (Recette recette : allRecettes) {

            double weight = 0.0;

            // Recette favorite → poids fort
            if (favoriteIds.contains(recette.getId())) {
                weight += 2.0;
            }

            // Recette créée par l'utilisateur → poids moyen
            if (recette.getUserId() == userId) {
                weight += 1.0;
            }

            if (weight > 0) {
                double[] recipeVector = buildRecipeVector(recette, allIngredientIds);
                for (int i = 0; i < vectorSize; i++) {
                    profile[i] += recipeVector[i] * weight;
                }
                totalWeight += weight;
            }
        }

        // Normaliser le profil
        if (totalWeight > 0) {
            for (int i = 0; i < vectorSize; i++) {
                profile[i] /= totalWeight;
            }
        }

        return profile;
    }

    /**
     * Profil de secours basé uniquement sur le stock disponible.
     * Utilisé quand l'utilisateur n'a pas encore de favoris ni de recettes créées.
     */
    private double[] buildStockBasedProfile(Set<Integer> stockIds,
                                            List<Integer> allIngredientIds) {

        int vectorSize = getVectorSize(allIngredientIds);
        double[] profile = new double[vectorSize];

        // Mettre 1.0 pour chaque ingrédient disponible en stock
        for (int i = 0; i < allIngredientIds.size(); i++) {
            if (stockIds.contains(allIngredientIds.get(i))) {
                profile[i] = 1.0;
            }
        }

        return profile;
    }

    // ═══════════ VECTORISATION D'UNE RECETTE ═══════════

    /**
     * Transforme une recette en vecteur numérique de caractéristiques :
     *
     * Structure du vecteur :
     * [0 .. N-1]     → ingrédients (binary : 1.0 si présent, 0.0 sinon)
     * [N .. N+3]     → type (one-hot : entree, main dish, dessert, drinks)
     * [N+4 .. N+6]  → difficulté (one-hot : easy, medium, hard)
     * [N+7]         → temps normalisé (tempsPreparation / MAX_TIME, clampé à 1.0)
     */
    private double[] buildRecipeVector(Recette recette, List<Integer> allIngredientIds) {

        int vectorSize = getVectorSize(allIngredientIds);
        double[] vector = new double[vectorSize];

        int offset = 0;

        // ── Ingrédients (binary) ──────────────────────────────────────────
        Set<Integer> recipeIngredientIds = new HashSet<>();
        if (recette.getRecetteIngredients() != null) {
            for (RecetteIngredient ri : recette.getRecetteIngredients()) {
                recipeIngredientIds.add(ri.getIngredientId());
            }
        }

        for (int i = 0; i < allIngredientIds.size(); i++) {
            vector[i] = recipeIngredientIds.contains(allIngredientIds.get(i)) ? 1.0 : 0.0;
        }
        offset += allIngredientIds.size();

        // ── Type (one-hot) ────────────────────────────────────────────────
        String type = recette.getType() != null ? recette.getType().toLowerCase() : "";
        for (int i = 0; i < ALL_TYPES.size(); i++) {
            vector[offset + i] = ALL_TYPES.get(i).equals(type) ? 1.0 : 0.0;
        }
        offset += ALL_TYPES.size();

        // ── Difficulté (one-hot) ──────────────────────────────────────────
        String diff = recette.getDifficulte() != null ? recette.getDifficulte().toLowerCase() : "";
        for (int i = 0; i < ALL_DIFFICULTIES.size(); i++) {
            vector[offset + i] = ALL_DIFFICULTIES.get(i).equals(diff) ? 1.0 : 0.0;
        }
        offset += ALL_DIFFICULTIES.size();

        // ── Temps normalisé ───────────────────────────────────────────────
        double normalizedTime = recette.getTempsPreparation() / MAX_TIME;
        vector[offset] = Math.min(1.0, normalizedTime);

        return vector;
    }

    /**
     * Retourne la taille totale du vecteur.
     * = nb ingrédients + nb types + nb difficultés + 1 (temps)
     */
    private int getVectorSize(List<Integer> allIngredientIds) {
        return allIngredientIds.size()
                + ALL_TYPES.size()
                + ALL_DIFFICULTIES.size()
                + 1;
    }

    // ═══════════ SIMILARITÉ COSINUS ═══════════

    /**
     * Calcule la similarité cosinus entre deux vecteurs.
     *
     * cos(θ) = (A · B) / (||A|| × ||B||)
     *
     * @return valeur entre 0.0 (aucune similarité) et 1.0 (identiques)
     */
    private double cosineSimilarity(double[] vectorA, double[] vectorB) {

        if (vectorA.length != vectorB.length) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // ═══════════ COUVERTURE STOCK ═══════════

    /**
     * Calcule le pourcentage d'ingrédients de la recette disponibles en stock.
     *
     * @return 0.0 → 1.0 (ex: 0.75 = 75% des ingrédients disponibles)
     */
    private double calculateStockCoverage(Recette recette, Set<Integer> stockIds) {

        if (recette.getRecetteIngredients() == null
                || recette.getRecetteIngredients().isEmpty()) {
            return 1.0; // Pas d'ingrédients requis → score neutre
        }

        long available = recette.getRecetteIngredients().stream()
                .filter(ri -> stockIds.contains(ri.getIngredientId()))
                .count();

        return (double) available / recette.getRecetteIngredients().size();
    }

    /**
     * Retourne la liste des noms d'ingrédients manquants dans le stock.
     */
    private List<String> getMissingIngredients(Recette recette, Set<Integer> stockIds) {

        if (recette.getRecetteIngredients() == null) {
            return new ArrayList<>();
        }

        return recette.getRecetteIngredients().stream()
                .filter(ri -> !stockIds.contains(ri.getIngredientId()))
                .map(ri -> ri.getIngredientNom() != null
                        ? ri.getIngredientNom()
                        : "Ingredient #" + ri.getIngredientId())
                .collect(Collectors.toList());
    }

    // ═══════════ UTILITAIRES ═══════════

    /**
     * Vérifie si un vecteur est entièrement à zéro (profil vide).
     */
    private boolean isVectorEmpty(double[] vector) {
        for (double v : vector) {
            if (v != 0.0) return false;
        }
        return true;
    }
}