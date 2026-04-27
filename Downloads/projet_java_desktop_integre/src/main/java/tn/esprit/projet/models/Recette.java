package tn.esprit.projet.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Recette {
    private int id;
    private String nom;
    private String type;           // entree, main dish, dessert, drinks
    private String difficulte;     // easy, medium, hard
    private int tempsPreparation;  // en minutes
    private int portions;          // nombre de portions
    private String description;
    private String image;
    private int userId;
    private LocalDateTime createdAt;
    private List<String> etapes;
    private List<RecetteIngredient> recetteIngredients;

    // Constructeurs
    public Recette() {
        this.etapes = new ArrayList<>();
        this.recetteIngredients = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.portions = 4; // valeur par défaut
    }

    public Recette(int id, String nom, String type, String difficulte,
                   int tempsPreparation, int portions, String description, String image, int userId) {
        this();
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.difficulte = difficulte;
        this.tempsPreparation = tempsPreparation;
        this.portions = portions;
        this.description = description;
        this.image = image;
        this.userId = userId;
    }

    // Méthodes utilitaires
    public String getFormattedTime() {
        if (tempsPreparation < 60) {
            return tempsPreparation + " min";
        } else {
            int hours = tempsPreparation / 60;
            int mins = tempsPreparation % 60;
            return hours + "h" + (mins > 0 ? " " + mins + "min" : "");
        }
    }

    public String getDifficultyIcon() {
        return switch (difficulte) {
            case "easy" -> "⭐";
            case "medium" -> "⭐⭐";
            case "hard" -> "⭐⭐⭐";
            default -> "⭐";
        };
    }

    public String getTypeIcon() {
        return switch (type) {
            case "entree" -> "🥗";
            case "main dish" -> "🍽️";
            case "dessert" -> "🍰";
            case "drinks" -> "🥤";
            default -> "🍴";
        };
    }

    public String getTypeLabel() {
        if (type == null) return "Unknown";
        return switch (type.toLowerCase()) {
            case "entree" -> "Entree";
            case "main dish", "plat" -> "Main Dish";
            case "dessert" -> "Dessert";
            case "drinks", "boisson" -> "Drinks";
            default -> type;
        };
    }

    public String getDifficultyLabel() {
        if (difficulte == null) return "Unknown";
        return switch (difficulte.toLowerCase()) {
            case "easy", "facile" -> "Easy";
            case "medium", "moyen" -> "Medium";
            case "hard", "difficile" -> "Hard";
            default -> difficulte;
        };
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDifficulte() { return difficulte; }
    public void setDifficulte(String difficulte) { this.difficulte = difficulte; }

    public int getTempsPreparation() { return tempsPreparation; }
    public void setTempsPreparation(int tempsPreparation) { this.tempsPreparation = tempsPreparation; }

    public int getPortions() { return portions; }
    public void setPortions(int portions) { this.portions = portions; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getEtapes() { return etapes; }
    public void setEtapes(List<String> etapes) { this.etapes = etapes; }

    public List<RecetteIngredient> getRecetteIngredients() { return recetteIngredients; }
    public void setRecetteIngredients(List<RecetteIngredient> recetteIngredients) {
        this.recetteIngredients = recetteIngredients;
    }

    @Override
    public String toString() {
        return nom + " (" + getTypeLabel() + ") - " + portions + " portions";
    }
}