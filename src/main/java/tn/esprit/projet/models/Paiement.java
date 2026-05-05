package tn.esprit.projet.models;

import java.time.LocalDateTime;

/**
 * Modèle représentant un paiement pour la participation à un événement
 */
public class Paiement {
    private int id;
    private int evenementId;
    private String nomParticipant;
    private String emailParticipant;
    private String telephone;
    private double montant;
    private String statut; // en_attente, valide, echoue, rembourse
    private String transactionId;
    private String methodePaiement;
    private LocalDateTime datePaiement;
    private LocalDateTime dateModification;

    // Constructeur complet (avec ID - pour récupération depuis DB)
    public Paiement(int id, int evenementId, String nomParticipant, String emailParticipant,
                    String telephone, double montant, String statut, String transactionId,
                    String methodePaiement, LocalDateTime datePaiement, LocalDateTime dateModification) {
        this.id = id;
        this.evenementId = evenementId;
        this.nomParticipant = nomParticipant;
        this.emailParticipant = emailParticipant;
        this.telephone = telephone;
        this.montant = montant;
        this.statut = statut;
        this.transactionId = transactionId;
        this.methodePaiement = methodePaiement;
        this.datePaiement = datePaiement;
        this.dateModification = dateModification;
    }

    // Constructeur sans ID (pour création)
    public Paiement(int evenementId, String nomParticipant, String emailParticipant,
                    String telephone, double montant, String statut, String transactionId,
                    String methodePaiement) {
        this.evenementId = evenementId;
        this.nomParticipant = nomParticipant;
        this.emailParticipant = emailParticipant;
        this.telephone = telephone;
        this.montant = montant;
        this.statut = statut;
        this.transactionId = transactionId;
        this.methodePaiement = methodePaiement;
    }

    // Getters
    public int getId() { return id; }
    public int getEvenementId() { return evenementId; }
    public String getNomParticipant() { return nomParticipant; }
    public String getEmailParticipant() { return emailParticipant; }
    public String getTelephone() { return telephone; }
    public double getMontant() { return montant; }
    public String getStatut() { return statut; }
    public String getTransactionId() { return transactionId; }
    public String getMethodePaiement() { return methodePaiement; }
    public LocalDateTime getDatePaiement() { return datePaiement; }
    public LocalDateTime getDateModification() { return dateModification; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setEvenementId(int evenementId) { this.evenementId = evenementId; }
    public void setNomParticipant(String nomParticipant) { this.nomParticipant = nomParticipant; }
    public void setEmailParticipant(String emailParticipant) { this.emailParticipant = emailParticipant; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setMontant(double montant) { this.montant = montant; }
    public void setStatut(String statut) { this.statut = statut; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setMethodePaiement(String methodePaiement) { this.methodePaiement = methodePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", evenementId=" + evenementId +
                ", nomParticipant='" + nomParticipant + '\'' +
                ", montant=" + montant + " TND" +
                ", statut='" + statut + '\'' +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
