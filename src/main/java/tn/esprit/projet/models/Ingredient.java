package tn.esprit.projet.models;

import java.time.LocalDate;
import java.util.Objects;

public class Ingredient {

    // ═══════════ ATTRIBUTS ═══════════
    private int id;
    private String nom;
    private String nomEn;
    private String categorie;
    private double quantite;
    private String unite;
    private LocalDate datePeremption;
    private String notes;
    private String image;

    // ═══════════ CONSTRUCTEURS ═══════════

    /** Constructeur vide (pour JavaFX TableView) */
    public Ingredient() {
    }

    /** Constructeur complet (SANS id - pour création) */
    public Ingredient(String nom, String nomEn, String categorie, double quantite,
                      String unite, LocalDate datePeremption, String notes, String image) {
        this.nom = nom;
        this.nomEn = nomEn;
        this.categorie = categorie;
        this.quantite = quantite;
        this.unite = unite;
        this.datePeremption = datePeremption;
        this.notes = notes;
        this.image = image;
    }

    /** Constructeur complet (AVEC id - pour modification/affichage) */
    public Ingredient(int id, String nom, String nomEn, String categorie, double quantite,
                      String unite, LocalDate datePeremption, String notes, String image) {
        this.id = id;
        this.nom = nom;
        this.nomEn = nomEn;
        this.categorie = categorie;
        this.quantite = quantite;
        this.unite = unite;
        this.datePeremption = datePeremption;
        this.notes = notes;
        this.image = image;
    }

    // ═══════════ GETTERS / SETTERS ═══════════

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNomEn() {
        return nomEn;
    }

    public void setNomEn(String nomEn) {
        this.nomEn = nomEn;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public LocalDate getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(LocalDate datePeremption) {
        this.datePeremption = datePeremption;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // ═══════════ MÉTHODES UTILITAIRES ═══════════

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", categorie='" + categorie + '\'' +
                ", quantite=" + quantite +
                ", unite='" + unite + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}