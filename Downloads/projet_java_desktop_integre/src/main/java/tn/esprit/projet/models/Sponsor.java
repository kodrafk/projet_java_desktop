package tn.esprit.projet.models;

public class Sponsor {
    private int id;
    private String nom_partenaire;
    private String type;
    private String description;
    private String statu;
    private String logo;
    private String video_url; // URL de la vidéo sponsor (YouTube, Vimeo, etc.)
    private int evenement_id;

    public Sponsor() {}

    public Sponsor(int id, String nom_partenaire, String type, String description, String statu, String logo, String video_url, int evenement_id) {
        this.id = id;
        this.nom_partenaire = nom_partenaire;
        this.type = type;
        this.description = description;
        this.statu = statu;
        this.logo = logo;
        this.video_url = video_url;
        this.evenement_id = evenement_id;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom_partenaire() { return nom_partenaire; }
    public void setNom_partenaire(String nom_partenaire) { this.nom_partenaire = nom_partenaire; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatu() { return statu; }
    public void setStatu(String statu) { this.statu = statu; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public String getVideo_url() { return video_url; }
    public void setVideo_url(String video_url) { this.video_url = video_url; }
    public int getEvenement_id() { return evenement_id; }
    public void setEvenement_id(int evenement_id) { this.evenement_id = evenement_id; }
}
