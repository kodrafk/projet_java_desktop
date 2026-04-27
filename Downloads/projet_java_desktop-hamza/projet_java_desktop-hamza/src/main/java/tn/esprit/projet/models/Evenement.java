package tn.esprit.projet.models;

import java.time.LocalDateTime;

public class Evenement {
    private int id;
    private String nom, lieu, statut, image, description, coach_name, objectifs;
    private LocalDateTime date_debut, date_fin;
    private double prix;
    private int capacite;      // Nombre max de participants
    private int nbParticipants; // Nombre actuel de participants

    // ─── Constructeur complet (avec ID) ───
    public Evenement(int id, String nom, LocalDateTime date_debut, LocalDateTime date_fin,
                     String lieu, String statut, String image, String description,
                     String coach_name, String objectifs, double prix, int capacite, int nbParticipants) {
        this.id = id; this.nom = nom; this.date_debut = date_debut; this.date_fin = date_fin;
        this.lieu = lieu; this.statut = statut; this.image = image; this.description = description;
        this.coach_name = coach_name; this.objectifs = objectifs; this.prix = prix;
        this.capacite = capacite; this.nbParticipants = nbParticipants;
    }

    // ─── Constructeur sans ID (pour l'ajout) ───
    public Evenement(String nom, LocalDateTime date_debut, LocalDateTime date_fin,
                     String lieu, String statut, String image, String description,
                     String coach_name, String objectifs, double prix, int capacite) {
        this.nom = nom; this.date_debut = date_debut; this.date_fin = date_fin;
        this.lieu = lieu; this.statut = statut; this.image = image; this.description = description;
        this.coach_name = coach_name; this.objectifs = objectifs; this.prix = prix;
        this.capacite = capacite; this.nbParticipants = 0;
    }

    // ─── Constructeur rétrocompatible sans capacite ───
    public Evenement(String nom, LocalDateTime date_debut, LocalDateTime date_fin,
                     String lieu, String statut, String image, String description,
                     String coach_name, String objectifs, double prix) {
        this(nom, date_debut, date_fin, lieu, statut, image, description, coach_name, objectifs, prix, 0);
    }

    // ─── Getters ───
    public int getId()                        { return id; }
    public String getNom()                    { return nom; }
    public String getLieu()                   { return lieu; }
    public String getStatut()                 { return statut; }
    public String getImage()                  { return image; }
    public String getDescription()            { return description; }
    public String getCoach_name()             { return coach_name; }
    public String getObjectifs()              { return objectifs; }
    public LocalDateTime getDate_debut()      { return date_debut; }
    public LocalDateTime getDate_fin()        { return date_fin; }
    public double getPrix()                   { return prix; }
    public int getCapacite()                  { return capacite; }
    public int getNbParticipants()            { return nbParticipants; }

    // ─── Setters ───
    public void setId(int id)                         { this.id = id; }
    public void setNom(String nom)                    { this.nom = nom; }
    public void setLieu(String lieu)                  { this.lieu = lieu; }
    public void setStatut(String statut)              { this.statut = statut; }
    public void setImage(String image)                { this.image = image; }
    public void setDescription(String description)    { this.description = description; }
    public void setCoach_name(String coach_name)      { this.coach_name = coach_name; }
    public void setObjectifs(String objectifs)        { this.objectifs = objectifs; }
    public void setDate_debut(LocalDateTime d)        { this.date_debut = d; }
    public void setDate_fin(LocalDateTime d)          { this.date_fin = d; }
    public void setPrix(double prix)                  { this.prix = prix; }
    public void setCapacite(int capacite)             { this.capacite = capacite; }
    public void setNbParticipants(int n)              { this.nbParticipants = n; }

    // ─── Utilitaires capacité ───
    public boolean estComplet()    { return capacite > 0 && nbParticipants >= capacite; }
    public boolean estIllimite()   { return capacite <= 0; }
    public int getPlacesRestantes(){ return capacite > 0 ? Math.max(0, capacite - nbParticipants) : -1; }
    public double getTauxRemplissage() { return capacite > 0 ? (double) nbParticipants / capacite : 0; }

    @Override
    public String toString() {
        return "Evenement{id=" + id + ", nom='" + nom + "', prix=" + prix + " TND, capacite=" + capacite + "}";
    }
}