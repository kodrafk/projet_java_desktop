package tn.esprit.projet.models;

public class MealPlanItem {

    // ─── Enums ────────────────────────────────────────────────
    public enum JourNom {
        Lundi, Mardi, Mercredi, Jeudi, Vendredi, Samedi, Dimanche
    }

    public enum MomentRepas {
        PETIT_DEJEUNER, DEJEUNER, DINER
    }

    public enum UrgenceNiveau {
        URGENT,
        BIENTOT,
        OK
    }

    // ─── Attributs ────────────────────────────────────────────
    private int            id;
    private int            mealPlanId;
    private JourNom        jourNom;
    private MomentRepas    momentRepas;
    private int            recetteId;
    private String         recetteNom;
    private int            recetteCalories;
    private String         recetteImage;      // 🆕 URL image
    private UrgenceNiveau  urgenceNiveau;
    private boolean        isEaten;

    // ─── Constructeur vide ────────────────────────────────────
    public MealPlanItem() {
        this.isEaten       = false;
        this.urgenceNiveau = UrgenceNiveau.OK;
        this.recetteImage  = "";
    }

    // ─── Constructeur complet ─────────────────────────────────
    public MealPlanItem(int mealPlanId, JourNom jourNom, MomentRepas momentRepas,
                        int recetteId, String recetteNom, int recetteCalories,
                        UrgenceNiveau urgenceNiveau) {
        this.mealPlanId      = mealPlanId;
        this.jourNom         = jourNom;
        this.momentRepas     = momentRepas;
        this.recetteId       = recetteId;
        this.recetteNom      = recetteNom;
        this.recetteCalories = recetteCalories;
        this.urgenceNiveau   = urgenceNiveau;
        this.isEaten         = false;
        this.recetteImage    = "";
    }

    // ─── Constructeur avec image ──────────────────────────────
    public MealPlanItem(int mealPlanId, JourNom jourNom, MomentRepas momentRepas,
                        int recetteId, String recetteNom, int recetteCalories,
                        String recetteImage, UrgenceNiveau urgenceNiveau) {
        this.mealPlanId      = mealPlanId;
        this.jourNom         = jourNom;
        this.momentRepas     = momentRepas;
        this.recetteId       = recetteId;
        this.recetteNom      = recetteNom;
        this.recetteCalories = recetteCalories;
        this.recetteImage    = recetteImage;
        this.urgenceNiveau   = urgenceNiveau;
        this.isEaten         = false;
    }

    // ─── Getters & Setters ────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMealPlanId() { return mealPlanId; }
    public void setMealPlanId(int mealPlanId) { this.mealPlanId = mealPlanId; }

    public JourNom getJourNom() { return jourNom; }
    public void setJourNom(JourNom jourNom) { this.jourNom = jourNom; }

    public MomentRepas getMomentRepas() { return momentRepas; }
    public void setMomentRepas(MomentRepas momentRepas) { this.momentRepas = momentRepas; }

    public int getRecetteId() { return recetteId; }
    public void setRecetteId(int recetteId) { this.recetteId = recetteId; }

    public String getRecetteNom() { return recetteNom; }
    public void setRecetteNom(String recetteNom) { this.recetteNom = recetteNom; }

    public int getRecetteCalories() { return recetteCalories; }
    public void setRecetteCalories(int recetteCalories) { this.recetteCalories = recetteCalories; }

    public String getRecetteImage() { return recetteImage; }
    public void setRecetteImage(String recetteImage) { this.recetteImage = recetteImage; }

    public UrgenceNiveau getUrgenceNiveau() { return urgenceNiveau; }
    public void setUrgenceNiveau(UrgenceNiveau urgenceNiveau) { this.urgenceNiveau = urgenceNiveau; }

    public boolean isEaten() { return isEaten; }
    public void setEaten(boolean eaten) { isEaten = eaten; }

    // ─── Méthodes utilitaires ─────────────────────────────────
    public String getUrgenceEmoji() {
        switch (urgenceNiveau) {
            case URGENT:  return "🔴";
            case BIENTOT: return "🟠";
            case OK:      return "🟢";
            default:      return "⚪";
        }
    }

    public String getMomentLabel() {
        switch (momentRepas) {
            case PETIT_DEJEUNER: return "Breakfast";
            case DEJEUNER:       return "Lunch";
            case DINER:          return "Dinner";
            default:             return momentRepas.toString();
        }
    }

    public String getStatutLabel() {
        return isEaten ? "Done" : "Pending";
    }

    @Override
    public String toString() {
        return "MealPlanItem{" +
                "id=" + id +
                ", jour=" + jourNom +
                ", moment=" + momentRepas +
                ", recette=" + recetteNom +
                ", calories=" + recetteCalories +
                ", image=" + recetteImage +
                ", urgence=" + urgenceNiveau +
                ", eaten=" + isEaten +
                '}';
    }
}