package tn.esprit.projet.services;

import tn.esprit.projet.models.Paiement;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service de gestion des paiements pour les événements
 * Simule une API de paiement professionnelle (style Stripe/Paymee)
 */
public class PaiementService {
    private Connection connection;

    public PaiementService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
        creerTableSiNecessaire();
    }

    /**
     * Crée la table paiement si elle n'existe pas encore
     */
    private void creerTableSiNecessaire() {
        String sql = "CREATE TABLE IF NOT EXISTS paiement (" +
            "id INT PRIMARY KEY AUTO_INCREMENT," +
            "evenement_id INT NOT NULL," +
            "nom_participant VARCHAR(255) NOT NULL," +
            "email_participant VARCHAR(255) NOT NULL," +
            "telephone VARCHAR(20)," +
            "montant DOUBLE NOT NULL," +
            "statut VARCHAR(50) NOT NULL," +
            "transaction_id VARCHAR(100) NOT NULL," +
            "methode_paiement VARCHAR(50) NOT NULL," +
            "date_paiement DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "date_modification DATETIME ON UPDATE CURRENT_TIMESTAMP" +
            ")";
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(sql);
            System.out.println("✅ Table paiement prête");
        } catch (SQLException e) {
            System.err.println("⚠️ Impossible de créer la table paiement : " + e.getMessage());
        }
    }

    /**
     * Récupère tous les paiements
     */
    public List<Paiement> getAll() {
        List<Paiement> liste = new ArrayList<>();
        String sql = "SELECT * FROM paiement ORDER BY date_paiement DESC";
        
        try (Statement st = connection.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSetToPaiement(rs));
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur récupération paiements : " + ex.getMessage());
            ex.printStackTrace();
        }
        return liste;
    }

    /**
     * Récupère les paiements pour un événement spécifique
     */
    public List<Paiement> getPaiementsByEvenement(int evenementId) {
        List<Paiement> liste = new ArrayList<>();
        String sql = "SELECT * FROM paiement WHERE evenement_id = ? ORDER BY date_paiement DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToPaiement(rs));
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur récupération paiements événement : " + ex.getMessage());
            ex.printStackTrace();
        }
        return liste;
    }

    /**
     * Récupère un paiement par son ID
     */
    public Paiement getPaiementById(int id) {
        String sql = "SELECT * FROM paiement WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaiement(rs);
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur récupération paiement : " + ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Traite un nouveau paiement (simulation d'API de paiement)
     * @return Le paiement créé avec son ID et transaction ID
     */
    public Paiement traiterPaiement(int evenementId, String nomParticipant, String emailParticipant,
                                    String telephone, double montant, String methodePaiement,
                                    String numeroCarte, String cvv, String dateExpiration) {
        
        // 🔐 SIMULATION D'API DE PAIEMENT PROFESSIONNELLE
        System.out.println("\n💳 ═══════════════════════════════════════════════════");
        System.out.println("   TRAITEMENT DU PAIEMENT");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("👤 Client      : " + nomParticipant);
        System.out.println("📧 Email       : " + emailParticipant);
        System.out.println("💰 Montant     : " + montant + " TND");
        System.out.println("💳 Méthode     : " + methodePaiement);
        System.out.println("🔢 Carte       : **** **** **** " + (numeroCarte != null && numeroCarte.length() >= 4 ? numeroCarte.substring(numeroCarte.length() - 4) : "****"));
        System.out.println("═══════════════════════════════════════════════════");
        
        // Simulation de validation de carte
        boolean paiementReussi = simulerValidationPaiement(numeroCarte, cvv, dateExpiration, montant);
        
        String statut = paiementReussi ? "valide" : "echoue";
        String transactionId = genererTransactionId();
        
        if (paiementReussi) {
            System.out.println("✅ PAIEMENT VALIDÉ");
            System.out.println("🔑 Transaction ID : " + transactionId);
        } else {
            System.out.println("❌ PAIEMENT REFUSÉ");
            System.out.println("⚠️  Raison : Carte invalide ou fonds insuffisants");
        }
        System.out.println("═══════════════════════════════════════════════════\n");
        
        // Créer l'objet paiement
        Paiement paiement = new Paiement(
            evenementId, nomParticipant, emailParticipant,
            telephone, montant, statut, transactionId, methodePaiement
        );
        
        // Enregistrer dans la base de données (si disponible)
        if (connection != null) {
            String sql = "INSERT INTO paiement (evenement_id, nom_participant, email_participant, " +
                         "telephone, montant, statut, transaction_id, methode_paiement, date_paiement) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, evenementId);
                ps.setString(2, nomParticipant);
                ps.setString(3, emailParticipant);
                ps.setString(4, telephone);
                ps.setDouble(5, montant);
                ps.setString(6, statut);
                ps.setString(7, transactionId);
                ps.setString(8, methodePaiement);
                ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
                
                int affectedRows = ps.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            paiement.setId(generatedKeys.getInt(1));
                            paiement.setDatePaiement(LocalDateTime.now());
                        }
                    }
                }
                System.out.println("💾 Paiement enregistré (ID: " + paiement.getId() + ")");
            } catch (SQLException ex) {
                System.err.println("⚠️ Paiement non enregistré en DB (table manquante?) : " + ex.getMessage());
                // On continue quand même — le paiement est validé même sans DB
            }
        }
        
        return paiement;
    }

    /**
     * Simule la validation d'un paiement par carte bancaire
     * Validation stricte des champs — toujours validé si les données sont correctes
     */
    private boolean simulerValidationPaiement(String numeroCarte, String cvv, String dateExpiration, double montant) {
        try {
            // Délai réaliste de traitement
            Thread.sleep(1200);

            // Pour les événements gratuits (montant = 0), toujours valider
            if (montant <= 0) {
                return true;
            }

            // Validation du numéro de carte (doit avoir 16 chiffres)
            if (numeroCarte == null || numeroCarte.replaceAll("\\s", "").length() < 16) {
                System.out.println("⚠️ Numéro de carte invalide");
                return false;
            }

            // Validation CVV (doit avoir 3 chiffres)
            if (cvv == null || cvv.trim().length() != 3) {
                System.out.println("⚠️ CVV invalide");
                return false;
            }

            // Validation date expiration (format MM/AA)
            if (dateExpiration == null || dateExpiration.trim().length() != 5) {
                System.out.println("⚠️ Date expiration invalide");
                return false;
            }

            // ✅ Toutes les validations passées → paiement TOUJOURS validé
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true; // En cas d'interruption, valider quand même
        }
    }

    /**
     * Génère un ID de transaction unique (style Stripe)
     */
    private String genererTransactionId() {
        return "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    /**
     * Rembourse un paiement
     */
    public boolean rembourserPaiement(int paiementId) {
        String sql = "UPDATE paiement SET statut = 'rembourse', date_modification = ? WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, paiementId);
            
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows > 0) {
                System.out.println("💸 Paiement remboursé (ID: " + paiementId + ")");
                return true;
            }
            
        } catch (SQLException ex) {
            System.err.println("❌ Erreur remboursement : " + ex.getMessage());
            ex.printStackTrace();
        }
        
        return false;
    }

    /**
     * Calcule le total des paiements pour un événement
     */
    public double getTotalPaiementsEvenement(int evenementId) {
        String sql = "SELECT SUM(montant) as total FROM paiement WHERE evenement_id = ? AND statut = 'valide'";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur calcul total : " + ex.getMessage());
            ex.printStackTrace();
        }
        
        return 0.0;
    }

    /**
     * Compte le nombre de participants ayant payé pour un événement
     */
    public int getNombreParticipantsPayes(int evenementId) {
        String sql = "SELECT COUNT(*) as count FROM paiement WHERE evenement_id = ? AND statut = 'valide'";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur comptage participants : " + ex.getMessage());
            ex.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Mappe un ResultSet vers un objet Paiement
     */
    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        Timestamp datePaiement = rs.getTimestamp("date_paiement");
        Timestamp dateModification = rs.getTimestamp("date_modification");
        
        return new Paiement(
            rs.getInt("id"),
            rs.getInt("evenement_id"),
            rs.getString("nom_participant"),
            rs.getString("email_participant"),
            rs.getString("telephone"),
            rs.getDouble("montant"),
            rs.getString("statut"),
            rs.getString("transaction_id"),
            rs.getString("methode_paiement"),
            datePaiement != null ? datePaiement.toLocalDateTime() : null,
            dateModification != null ? dateModification.toLocalDateTime() : null
        );
    }

    /**
     * Vérifie si un participant a déjà payé pour un événement
     */
    public boolean aDejaPayePourEvenement(String email, int evenementId) {
        String sql = "SELECT COUNT(*) as count FROM paiement " +
                     "WHERE email_participant = ? AND evenement_id = ? AND statut = 'valide'";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException ex) {
            System.err.println("❌ Erreur vérification paiement : " + ex.getMessage());
            ex.printStackTrace();
        }
        
        return false;
    }
}
