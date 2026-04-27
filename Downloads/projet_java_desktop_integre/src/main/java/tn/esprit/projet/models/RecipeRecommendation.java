package tn.esprit.projet.models;

import java.util.List;

/**
 * Représente une recette recommandée avec son score de correspondance
 * et les informations sur la disponibilité des ingrédients en stock.
 */
public class RecipeRecommendation {

    // ═══════════ ATTRIBUTS ═══════════

    /** La recette recommandée */
    private Recette recette;

    /**
     * Score de correspondance entre 0.0 et 1.0
     * Calculé par similarité cosinus + bonus stock
     */
    private double matchScore;

    /**
     * Liste des noms d'ingrédients manquants dans le stock
     * Vide si tous les ingrédients sont disponibles
     */
    private List<String> missingIngredients;

    /**
     * Pourcentage d'ingrédients disponibles en stock
     * Ex: 0.75 = 75% des ingrédients sont en stock
     */
    private double stockCoverage;

    // ═══════════ CONSTRUCTEURS ═══════════

    public RecipeRecommendation() {}

    public RecipeRecommendation(Recette recette, double matchScore,
                                List<String> missingIngredients, double stockCoverage) {
        this.recette = recette;
        this.matchScore = matchScore;
        this.missingIngredients = missingIngredients;
        this.stockCoverage = stockCoverage;
    }

    // ═══════════ MÉTHODES UTILITAIRES ═══════════

    /**
     * Retourne true si tous les ingrédients sont disponibles en stock
     */
    public boolean isAllIngredientsAvailable() {
        return missingIngredients == null || missingIngredients.isEmpty();
    }

    /**
     * Retourne le score en pourcentage (0 → 100)
     * Ex: 0.98 → 98
     */
    public int getMatchPercent() {
        return (int) Math.round(matchScore * 100);
    }

    /**
     * Retourne la couleur du badge selon le score
     * >= 80% → vert
     * 50-79% → orange
     * < 50%  → rouge
     */
    public String getBadgeColor() {
        int percent = getMatchPercent();
        if (percent >= 80) return "#22C55E";
        if (percent >= 50) return "#F59E0B";
        return "#EF4444";
    }

    /**
     * Retourne la couleur de fond du badge selon le score
     */
    public String getBadgeBackgroundColor() {
        int percent = getMatchPercent();
        if (percent >= 80) return "#DCFCE7";
        if (percent >= 50) return "#FEF3C7";
        return "#FEE2E2";
    }

    /**
     * Retourne l'emoji du badge selon le score
     */
    public String getBadgeEmoji() {
        int percent = getMatchPercent();
        if (percent >= 80) return "🟢";
        if (percent >= 50) return "🟡";
        return "🔴";
    }

    /**
     * Retourne le texte du statut des ingrédients
     * Ex: "✅ All ingredients available"
     *     "⚠️ Missing: Coconut Milk"
     *     "❌ Missing: 3 ingredients"
     */
    public String getIngredientStatusText() {
        if (isAllIngredientsAvailable()) {
            return "✅ All ingredients available";
        }

        int count = missingIngredients.size();

        if (count == 1) {
            return "⚠️ Missing: " + missingIngredients.get(0);
        }

        if (count == 2) {
            return "⚠️ Missing: " + missingIngredients.get(0)
                    + ", " + missingIngredients.get(1);
        }

        // 3+
        return "❌ Missing: " + count + " ingredients";
    }

    /**
     * Retourne la couleur de fond du statut ingrédients
     */
    public String getIngredientStatusBackground() {
        if (isAllIngredientsAvailable()) return "#F0FDF4";
        if (missingIngredients.size() == 1) return "#FEF3C7";
        return "#FEE2E2";
    }

    /**
     * Retourne la couleur du texte du statut ingrédients
     */
    public String getIngredientStatusColor() {
        if (isAllIngredientsAvailable()) return "#16A34A";
        if (missingIngredients.size() == 1) return "#D97706";
        return "#DC2626";
    }

    // ═══════════ GETTERS / SETTERS ═══════════

    public Recette getRecette() {
        return recette;
    }

    public void setRecette(Recette recette) {
        this.recette = recette;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public List<String> getMissingIngredients() {
        return missingIngredients;
    }

    public void setMissingIngredients(List<String> missingIngredients) {
        this.missingIngredients = missingIngredients;
    }

    public double getStockCoverage() {
        return stockCoverage;
    }

    public void setStockCoverage(double stockCoverage) {
        this.stockCoverage = stockCoverage;
    }

    // ═══════════ TO STRING ═══════════

    @Override
    public String toString() {
        return "RecipeRecommendation{" +
                "recette=" + (recette != null ? recette.getNom() : "null") +
                ", matchScore=" + getMatchPercent() + "%" +
                ", missing=" + (missingIngredients != null ? missingIngredients.size() : 0) +
                '}';
    }
}