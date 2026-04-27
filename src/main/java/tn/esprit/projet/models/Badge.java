package tn.esprit.projet.models;

/**
 * Badge entity — mirrors Badge.php from the Symfony web app.
 * Fields: nom, description, conditionText, conditionType, conditionValue,
 *         svg (emoji/icon), couleur, couleurBg, categorie, ordre, rarete
 */
public class Badge {
    private int    id;
    private String nom;
    private String description;
    private String conditionText;
    private String conditionType;
    private int    conditionValue;
    private String svg;
    private String couleur;
    private String couleurBg;
    private String categorie;
    private int    ordre;
    private String rarete = "common";

    // ── Getters / Setters ──────────────────────────────────────────────────────
    public int    getId()                        { return id; }
    public void   setId(int id)                  { this.id = id; }
    public String getNom()                       { return nom; }
    public void   setNom(String nom)             { this.nom = nom; }
    public String getDescription()               { return description; }
    public void   setDescription(String d)       { this.description = d; }
    public String getConditionText()             { return conditionText; }
    public void   setConditionText(String t)     { this.conditionText = t; }
    public String getConditionType()             { return conditionType; }
    public void   setConditionType(String t)     { this.conditionType = t; }
    public int    getConditionValue()            { return conditionValue; }
    public void   setConditionValue(int v)       { this.conditionValue = v; }
    public String getSvg()                       { return svg; }
    public void   setSvg(String svg)             { this.svg = svg; }
    public String getCouleur()                   { return couleur; }
    public void   setCouleur(String c)           { this.couleur = c; }
    public String getCouleurBg()                 { return couleurBg; }
    public void   setCouleurBg(String c)         { this.couleurBg = c; }
    public String getCategorie()                 { return categorie; }
    public void   setCategorie(String c)         { this.categorie = c; }
    public int    getOrdre()                     { return ordre; }
    public void   setOrdre(int o)                { this.ordre = o; }
    public String getRarete()                    { return rarete; }
    public void   setRarete(String r)            { this.rarete = r; }

    // Legacy aliases
    public String getName()  { return nom; }
    public void   setName(String n) { this.nom = n; }
    public String getIcon()  { return svg; }
    public void   setIcon(String i) { this.svg = i; }

    /** Rarity weight: legendary=4, epic=3, rare=2, common=1 */
    public int getRareteOrdre() {
        if (rarete == null) return 0;
        return switch (rarete.toLowerCase()) {
            case "legendary" -> 4;
            case "epic"      -> 3;
            case "rare"      -> 2;
            default          -> 1;
        };
    }
}
