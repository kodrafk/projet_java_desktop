package tn.esprit.projet.models;

import java.time.LocalDateTime;
import java.util.List;

public class MealPlan {

    // ─── Enums ────────────────────────────────────────────────
    public enum Objectif {
        PERTE_POIDS,
        MAINTIEN,
        PRISE_MASSE
    }

    public enum Regime {
        STANDARD,
        VEGETARIEN,
        VEGAN,
        HALAL
    }

    // ─── Attributs ────────────────────────────────────────────
    private int           id;
    private int           userId;
    private LocalDateTime dateCreation;
    private Objectif      objectif;
    private Regime        regime;

    // Allergies
    private boolean allergieLactose;
    private boolean allergieGluten;
    private boolean allergieNuts;
    private boolean allergieEggs;

    // Items du plan (21 repas)
    private List<MealPlanItem> items;

    // ─── Constructeur vide ────────────────────────────────────
    public MealPlan() {
        this.dateCreation = LocalDateTime.now();
    }

    // ─── Constructeur complet ─────────────────────────────────
    public MealPlan(int userId, Objectif objectif, Regime regime,
                    boolean allergieLactose, boolean allergieGluten,
                    boolean allergieNuts, boolean allergieEggs) {
        this.userId          = userId;
        this.objectif        = objectif;
        this.regime          = regime;
        this.allergieLactose = allergieLactose;
        this.allergieGluten  = allergieGluten;
        this.allergieNuts    = allergieNuts;
        this.allergieEggs    = allergieEggs;
        this.dateCreation    = LocalDateTime.now();
    }

    // ─── Getters & Setters ────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Objectif getObjectif() { return objectif; }
    public void setObjectif(Objectif objectif) { this.objectif = objectif; }

    public Regime getRegime() { return regime; }
    public void setRegime(Regime regime) { this.regime = regime; }

    public boolean isAllergieLactose() { return allergieLactose; }
    public void setAllergieLactose(boolean allergieLactose) { this.allergieLactose = allergieLactose; }

    public boolean isAllergieGluten() { return allergieGluten; }
    public void setAllergieGluten(boolean allergieGluten) { this.allergieGluten = allergieGluten; }

    public boolean isAllergieNuts() { return allergieNuts; }
    public void setAllergieNuts(boolean allergieNuts) { this.allergieNuts = allergieNuts; }

    public boolean isAllergieEggs() { return allergieEggs; }
    public void setAllergieEggs(boolean allergieEggs) { this.allergieEggs = allergieEggs; }

    public List<MealPlanItem> getItems() { return items; }
    public void setItems(List<MealPlanItem> items) { this.items = items; }

    // ─── Méthodes utilitaires ─────────────────────────────────

    // Calcul total calories semaine
    public int getTotalCalories() {
        if (items == null) return 0;
        return items.stream()
                .mapToInt(MealPlanItem::getRecetteCalories)
                .sum();
    }

    // Nombre repas effectués
    public long getNbRepasEffectues() {
        if (items == null) return 0;
        return items.stream()
                .filter(MealPlanItem::isEaten)
                .count();
    }

    // ─── toString ─────────────────────────────────────────────
    @Override
    public String toString() {
        return "MealPlan{" +
                "id="              + id              +
                ", userId="        + userId          +
                ", dateCreation="  + dateCreation    +
                ", objectif="      + objectif        +
                ", regime="        + regime          +
                ", lactose="       + allergieLactose +
                ", gluten="        + allergieGluten  +
                ", nuts="          + allergieNuts    +
                ", eggs="          + allergieEggs    +
                ", nbItems="       + (items != null ? items.size() : 0) +
                '}';
    }
}