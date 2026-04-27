package tn.esprit.projet.models;

/**
 * Modèle pour les vidéos d'échauffement associées aux événements
 */
public class VideoEchauffement {
    private int id;
    private String titre;
    private String description;
    private String urlVideo;        // URL YouTube ou locale
    private String thumbnail;       // Image de prévisualisation
    private int duree;             // Durée en secondes
    private String typeEvenement;  // yoga, musculation, cardio, etc.
    private String niveau;         // débutant, intermédiaire, avancé
    private int evenementId;       // ID de l'événement associé
    
    // Constructeurs
    public VideoEchauffement() {}
    
    public VideoEchauffement(int id, String titre, String description, String urlVideo, 
                           String thumbnail, int duree, String typeEvenement, String niveau, int evenementId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.urlVideo = urlVideo;
        this.thumbnail = thumbnail;
        this.duree = duree;
        this.typeEvenement = typeEvenement;
        this.niveau = niveau;
        this.evenementId = evenementId;
    }
    
    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getUrlVideo() { return urlVideo; }
    public void setUrlVideo(String urlVideo) { this.urlVideo = urlVideo; }
    
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    
    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }
    
    public String getTypeEvenement() { return typeEvenement; }
    public void setTypeEvenement(String typeEvenement) { this.typeEvenement = typeEvenement; }
    
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    
    public int getEvenementId() { return evenementId; }
    public void setEvenementId(int evenementId) { this.evenementId = evenementId; }
    
    // Méthodes utilitaires
    public String getDureeFormatee() {
        int minutes = duree / 60;
        int secondes = duree % 60;
        return String.format("%d:%02d", minutes, secondes);
    }
    
    public String getEmojiNiveau() {
        switch (niveau.toLowerCase()) {
            case "débutant": return "🟢";
            case "intermédiaire": return "🟡";
            case "avancé": return "🔴";
            default: return "⚪";
        }
    }
    
    public String getEmojiType() {
        switch (typeEvenement.toLowerCase()) {
            case "yoga": return "🧘‍♀️";
            case "musculation": return "💪";
            case "cardio": return "🏃‍♂️";
            case "crossfit": return "🏋️‍♀️";
            case "pilates": return "🤸‍♀️";
            case "danse": return "💃";
            case "boxe": return "🥊";
            case "natation": return "🏊‍♂️";
            default: return "🏃‍♂️";
        }
    }
    
    @Override
    public String toString() {
        return "VideoEchauffement{" +
               "id=" + id +
               ", titre='" + titre + '\'' +
               ", duree=" + getDureeFormatee() +
               ", type='" + typeEvenement + '\'' +
               ", niveau='" + niveau + '\'' +
               '}';
    }
}