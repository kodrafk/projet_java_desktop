package tn.esprit.projet.models;

import java.util.List;

public class AIRecipeResult {

    // ─── Données recette générées par Gemini ──────────
    private String       nom;
    private String       description;
    private String       type;
    private String       difficulte;
    private int          tempsPreparation;
    private int          servings;
    private List<String> ingredients;     // ["250g Flour", "150ml Water"...]
    private List<String> steps;           // ["Mix flour...", "Bake at..."]

    // ─── Nutrition estimée ────────────────────────────
    private int    calories;
    private float  proteines;
    private float  lipides;
    private float  glucides;

    // ─── Image ────────────────────────────────────────
    private String imageUrl;             // URL Unsplash
    private String imageKeywords;        // mots clés pour Unsplash

    // ─── Diet flags ───────────────────────────────────
    private boolean isVegetarian;
    private boolean isVegan;
    private boolean isHalal;
    private boolean containsGluten;
    private boolean containsLactose;
    private boolean containsNuts;
    private boolean containsEggs;

    // ─── Constructeur vide ────────────────────────────
    public AIRecipeResult() {}

    // ─── Getters & Setters ────────────────────────────
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDifficulte() { return difficulte; }
    public void setDifficulte(String difficulte) { this.difficulte = difficulte; }

    public int getTempsPreparation() { return tempsPreparation; }
    public void setTempsPreparation(int tempsPreparation) {
        this.tempsPreparation = tempsPreparation;
    }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public float getProteines() { return proteines; }
    public void setProteines(float proteines) { this.proteines = proteines; }

    public float getLipides() { return lipides; }
    public void setLipides(float lipides) { this.lipides = lipides; }

    public float getGlucides() { return glucides; }
    public void setGlucides(float glucides) { this.glucides = glucides; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageKeywords() { return imageKeywords; }
    public void setImageKeywords(String imageKeywords) { this.imageKeywords = imageKeywords; }

    public boolean isVegetarian() { return isVegetarian; }
    public void setVegetarian(boolean vegetarian) { isVegetarian = vegetarian; }

    public boolean isVegan() { return isVegan; }
    public void setVegan(boolean vegan) { isVegan = vegan; }

    public boolean isHalal() { return isHalal; }
    public void setHalal(boolean halal) { isHalal = halal; }

    public boolean isContainsGluten() { return containsGluten; }
    public void setContainsGluten(boolean containsGluten) { this.containsGluten = containsGluten; }

    public boolean isContainsLactose() { return containsLactose; }
    public void setContainsLactose(boolean containsLactose) {
        this.containsLactose = containsLactose;
    }

    public boolean isContainsNuts() { return containsNuts; }
    public void setContainsNuts(boolean containsNuts) { this.containsNuts = containsNuts; }

    public boolean isContainsEggs() { return containsEggs; }
    public void setContainsEggs(boolean containsEggs) { this.containsEggs = containsEggs; }

    // ─── Méthode utilitaire ───────────────────────────
    public String getStepsAsJson() {
        if (steps == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < steps.size(); i++) {
            sb.append("\"").append(steps.get(i)
                    .replace("\"", "'")).append("\"");
            if (i < steps.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "AIRecipeResult{" +
                "nom='"         + nom             + '\'' +
                ", type='"      + type            + '\'' +
                ", difficulte='"+ difficulte      + '\'' +
                ", temps="      + tempsPreparation +
                ", calories="   + calories         +
                ", imageUrl='"  + imageUrl        + '\'' +
                '}';
    }
}