package tn.esprit.projet.models;

import java.util.List;

public class AIRecipeRequest {

    // ─── Champs form écran 1 ──────────────────────────
    private String       dishType;        // Entrée / Plat / Dessert / Drinks
    private String       difficulty;      // Easy / Medium / Hard
    private int          maxTime;         // minutes
    private int          servings;        // personnes
    private String       cuisineStyle;    // Tunisian / Italian / French...
    private String       calorieRange;    // Light / Moderate / Rich
    private String       imageStyle;      // Professional / Rustic / Minimalist...
    private boolean      isVegetarian;
    private boolean      isVegan;
    private boolean      isHalal;
    private boolean      isGlutenFree;
    private boolean      noLactose;
    private boolean      noNuts;
    private boolean      noEggs;
    private String       extraInstructions;

    // ─── Constructeur vide ────────────────────────────
    public AIRecipeRequest() {}

    // ─── Getters & Setters ────────────────────────────
    public String getDishType() { return dishType; }
    public void setDishType(String dishType) { this.dishType = dishType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getMaxTime() { return maxTime; }
    public void setMaxTime(int maxTime) { this.maxTime = maxTime; }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }

    public String getCuisineStyle() { return cuisineStyle; }
    public void setCuisineStyle(String cuisineStyle) { this.cuisineStyle = cuisineStyle; }

    public String getCalorieRange() { return calorieRange; }
    public void setCalorieRange(String calorieRange) { this.calorieRange = calorieRange; }

    public String getImageStyle() { return imageStyle; }
    public void setImageStyle(String imageStyle) { this.imageStyle = imageStyle; }

    public boolean isVegetarian() { return isVegetarian; }
    public void setVegetarian(boolean vegetarian) { isVegetarian = vegetarian; }

    public boolean isVegan() { return isVegan; }
    public void setVegan(boolean vegan) { isVegan = vegan; }

    public boolean isHalal() { return isHalal; }
    public void setHalal(boolean halal) { isHalal = halal; }

    public boolean isGlutenFree() { return isGlutenFree; }
    public void setGlutenFree(boolean glutenFree) { isGlutenFree = glutenFree; }

    public boolean isNoLactose() { return noLactose; }
    public void setNoLactose(boolean noLactose) { this.noLactose = noLactose; }

    public boolean isNoNuts() { return noNuts; }
    public void setNoNuts(boolean noNuts) { this.noNuts = noNuts; }

    public boolean isNoEggs() { return noEggs; }
    public void setNoEggs(boolean noEggs) { this.noEggs = noEggs; }

    public String getExtraInstructions() { return extraInstructions; }
    public void setExtraInstructions(String extraInstructions) {
        this.extraInstructions = extraInstructions;
    }

    // ─── Construire résumé dietary pour prompt ────────
    public String getDietarySummary() {
        StringBuilder sb = new StringBuilder();
        if (isVegetarian) sb.append("Vegetarian, ");
        if (isVegan)      sb.append("Vegan, ");
        if (isHalal)      sb.append("Halal, ");
        if (isGlutenFree) sb.append("Gluten-Free, ");
        if (noLactose)    sb.append("No Lactose, ");
        if (noNuts)       sb.append("No Nuts, ");
        if (noEggs)       sb.append("No Eggs, ");

        return sb.length() > 0
                ? sb.substring(0, sb.length() - 2)
                : "No restrictions";
    }
}