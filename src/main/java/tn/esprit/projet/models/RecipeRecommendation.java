package tn.esprit.projet.models;

import java.util.List;

public class RecipeRecommendation {
    private Recette recette;
    private double matchScore;
    private List<String> missingIngredients;
    private double stockCoverage;

    public RecipeRecommendation() {}

    public RecipeRecommendation(Recette recette, double matchScore, List<String> missingIngredients, double stockCoverage) {
        this.recette = recette; this.matchScore = matchScore;
        this.missingIngredients = missingIngredients; this.stockCoverage = stockCoverage;
    }

    public boolean isAllIngredientsAvailable() { return missingIngredients == null || missingIngredients.isEmpty(); }
    public int getMatchPercent() { return (int) Math.round(matchScore * 100); }

    public String getBadgeColor() { int p = getMatchPercent(); return p >= 80 ? "#22C55E" : p >= 50 ? "#F59E0B" : "#EF4444"; }
    public String getBadgeBackgroundColor() { int p = getMatchPercent(); return p >= 80 ? "#DCFCE7" : p >= 50 ? "#FEF3C7" : "#FEE2E2"; }
    public String getBadgeEmoji() { int p = getMatchPercent(); return p >= 80 ? "🟢" : p >= 50 ? "🟡" : "🔴"; }

    public String getIngredientStatusText() {
        if (isAllIngredientsAvailable()) return "✅ All ingredients available";
        int count = missingIngredients.size();
        if (count == 1) return "⚠️ Missing: " + missingIngredients.get(0);
        if (count == 2) return "⚠️ Missing: " + missingIngredients.get(0) + ", " + missingIngredients.get(1);
        return "❌ Missing: " + count + " ingredients";
    }

    public String getIngredientStatusBackground() { return isAllIngredientsAvailable() ? "#F0FDF4" : missingIngredients.size() == 1 ? "#FEF3C7" : "#FEE2E2"; }
    public String getIngredientStatusColor() { return isAllIngredientsAvailable() ? "#16A34A" : missingIngredients.size() == 1 ? "#D97706" : "#DC2626"; }

    public Recette getRecette() { return recette; } public void setRecette(Recette r) { this.recette = r; }
    public double getMatchScore() { return matchScore; } public void setMatchScore(double s) { this.matchScore = s; }
    public List<String> getMissingIngredients() { return missingIngredients; } public void setMissingIngredients(List<String> m) { this.missingIngredients = m; }
    public double getStockCoverage() { return stockCoverage; } public void setStockCoverage(double s) { this.stockCoverage = s; }
}
