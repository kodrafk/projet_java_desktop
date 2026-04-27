package tn.esprit.projet.models;

public class RecetteInfoPlus {

    // ─── Enums ────────────────────────────────────────────────
    public enum MomentRepas {
        PETIT_DEJEUNER,
        DEJEUNER,
        DINER
    }

    // ─── Attributs ────────────────────────────────────────────
    private int     recetteId;
    private int     calories;
    private float   proteines;
    private float   lipides;
    private float   glucides;
    private MomentRepas momentRepas;

    // Régime
    private boolean isVegetarien;
    private boolean isVegan;
    private boolean isHalal;

    // Allergènes
    private boolean containsGluten;
    private boolean containsLactose;
    private boolean containsNuts;
    private boolean containsEggs;

    // ─── Constructeur vide ────────────────────────────────────
    public RecetteInfoPlus() {}

    // ─── Constructeur complet ─────────────────────────────────
    public RecetteInfoPlus(int recetteId, int calories, float proteines,
                           float lipides, float glucides, MomentRepas momentRepas,
                           boolean isVegetarien, boolean isVegan, boolean isHalal,
                           boolean containsGluten, boolean containsLactose,
                           boolean containsNuts, boolean containsEggs) {
        this.recetteId       = recetteId;
        this.calories        = calories;
        this.proteines       = proteines;
        this.lipides         = lipides;
        this.glucides        = glucides;
        this.momentRepas     = momentRepas;
        this.isVegetarien    = isVegetarien;
        this.isVegan         = isVegan;
        this.isHalal         = isHalal;
        this.containsGluten  = containsGluten;
        this.containsLactose = containsLactose;
        this.containsNuts    = containsNuts;
        this.containsEggs    = containsEggs;
    }

    // ─── Getters & Setters ────────────────────────────────────
    public int getRecetteId() { return recetteId; }
    public void setRecetteId(int recetteId) { this.recetteId = recetteId; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public float getProteines() { return proteines; }
    public void setProteines(float proteines) { this.proteines = proteines; }

    public float getLipides() { return lipides; }
    public void setLipides(float lipides) { this.lipides = lipides; }

    public float getGlucides() { return glucides; }
    public void setGlucides(float glucides) { this.glucides = glucides; }

    public MomentRepas getMomentRepas() { return momentRepas; }
    public void setMomentRepas(MomentRepas momentRepas) { this.momentRepas = momentRepas; }

    public boolean isVegetarien() { return isVegetarien; }
    public void setVegetarien(boolean vegetarien) { isVegetarien = vegetarien; }

    public boolean isVegan() { return isVegan; }
    public void setVegan(boolean vegan) { isVegan = vegan; }

    public boolean isHalal() { return isHalal; }
    public void setHalal(boolean halal) { isHalal = halal; }

    public boolean isContainsGluten() { return containsGluten; }
    public void setContainsGluten(boolean containsGluten) { this.containsGluten = containsGluten; }

    public boolean isContainsLactose() { return containsLactose; }
    public void setContainsLactose(boolean containsLactose) { this.containsLactose = containsLactose; }

    public boolean isContainsNuts() { return containsNuts; }
    public void setContainsNuts(boolean containsNuts) { this.containsNuts = containsNuts; }

    public boolean isContainsEggs() { return containsEggs; }
    public void setContainsEggs(boolean containsEggs) { this.containsEggs = containsEggs; }

    // ─── toString ─────────────────────────────────────────────
    @Override
    public String toString() {
        return "RecetteInfoPlus{" +
                "recetteId="       + recetteId       +
                ", calories="      + calories         +
                ", proteines="     + proteines        +
                ", lipides="       + lipides          +
                ", glucides="      + glucides         +
                ", momentRepas="   + momentRepas      +
                ", isVegetarien="  + isVegetarien     +
                ", isVegan="       + isVegan          +
                ", isHalal="       + isHalal          +
                ", gluten="        + containsGluten   +
                ", lactose="       + containsLactose  +
                ", nuts="          + containsNuts     +
                ", eggs="          + containsEggs     +
                '}';
    }
}