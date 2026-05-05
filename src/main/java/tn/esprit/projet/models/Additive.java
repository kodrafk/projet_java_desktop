package tn.esprit.projet.models;

public class Additive {

    private int id;
    private String code;
    private String name;
    private int dangerLevel;
    private String category;
    private String description;
    private String healthEffects;
    private String bannedCountries;

    // =====================
    // CONSTRUCTEURS
    // =====================

    public Additive() {}

    public Additive(String code, String name, int dangerLevel, String category,
                    String description, String healthEffects) {
        this.code = code;
        this.name = name;
        this.dangerLevel = dangerLevel;
        this.category = category;
        this.description = description;
        this.healthEffects = healthEffects;
    }

    public Additive(int id, String code, String name, int dangerLevel, String category,
                    String description, String healthEffects, String bannedCountries) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.dangerLevel = dangerLevel;
        this.category = category;
        this.description = description;
        this.healthEffects = healthEffects;
        this.bannedCountries = bannedCountries;
    }

    // =====================
    // GETTERS & SETTERS
    // =====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDangerLevel() { return dangerLevel; }
    public void setDangerLevel(int dangerLevel) { this.dangerLevel = dangerLevel; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHealthEffects() { return healthEffects; }
    public void setHealthEffects(String healthEffects) { this.healthEffects = healthEffects; }

    public String getBannedCountries() { return bannedCountries; }
    public void setBannedCountries(String bannedCountries) { this.bannedCountries = bannedCountries; }

    // =====================
    // MÉTHODE UTILITAIRE
    // =====================

    public String getDangerLabel() {
        if (dangerLevel <= 3) return "SAFE";
        if (dangerLevel <= 6) return "MODERATE";
        if (dangerLevel <= 8) return "DANGEROUS";
        return "HIGHLY DANGEROUS";
    }

    public String getDangerColor() {
        if (dangerLevel <= 3) return "#2ECC71";  // Vert
        if (dangerLevel <= 6) return "#F39C12";  // Jaune
        if (dangerLevel <= 8) return "#E67E22";  // Orange
        return "#E74C3C";                         // Rouge
    }

    // =====================
    // TO STRING
    // =====================

    @Override
    public String toString() {
        return "Additive{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", dangerLevel=" + dangerLevel +
                ", label='" + getDangerLabel() + '\'' +
                '}';
    }
}