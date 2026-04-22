package tn.esprit.projet.models;

public class RecetteIngredient {
    private int id;
    private String quantite;
    private int recetteId;
    private int ingredientId;
    private String ingredientNom;

    public RecetteIngredient() {}

    public RecetteIngredient(int id, String quantite, int recetteId, int ingredientId) {
        this.id = id;
        this.quantite = quantite;
        this.recetteId = recetteId;
        this.ingredientId = ingredientId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuantite() { return quantite; }
    public void setQuantite(String quantite) { this.quantite = quantite; }

    public int getRecetteId() { return recetteId; }
    public void setRecetteId(int recetteId) { this.recetteId = recetteId; }

    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }

    public String getIngredientNom() { return ingredientNom; }
    public void setIngredientNom(String ingredientNom) { this.ingredientNom = ingredientNom; }

    @Override
    public String toString() {
        return quantite + " - " + ingredientNom;
    }
}