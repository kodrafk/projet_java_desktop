package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.utils.MyBDConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService {
    private Connection connection;

    public EvenementService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
        if (this.connection != null) {
            ajouterColonnePrixSiNecessaire();
        } else {
            System.err.println("⚠️ EvenementService : connexion DB null, vérifiez MySQL");
        }
    }

    /**
     * Ajoute la colonne prix si elle n'existe pas encore
     * Compatible MySQL et MariaDB
     */
    private void ajouterColonnePrixSiNecessaire() {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            String catalog = connection.getCatalog();

            // Ajouter colonne prix
            ResultSet cols = meta.getColumns(catalog, null, "evenement", "prix");
            if (!cols.next()) {
                try (Statement st = connection.createStatement()) {
                    st.executeUpdate("ALTER TABLE evenement ADD COLUMN prix DOUBLE DEFAULT 0.0");
                    System.out.println("✅ Colonne prix ajoutée");
                }
            }
            cols.close();

            // Ajouter colonne capacite
            ResultSet cols2 = meta.getColumns(catalog, null, "evenement", "capacite");
            if (!cols2.next()) {
                try (Statement st = connection.createStatement()) {
                    st.executeUpdate("ALTER TABLE evenement ADD COLUMN capacite INT DEFAULT 0");
                    System.out.println("✅ Colonne capacite ajoutée");
                }
            }
            cols2.close();

            // Ajouter colonne nb_participants
            ResultSet cols3 = meta.getColumns(catalog, null, "evenement", "nb_participants");
            if (!cols3.next()) {
                try (Statement st = connection.createStatement()) {
                    st.executeUpdate("ALTER TABLE evenement ADD COLUMN nb_participants INT DEFAULT 0");
                    System.out.println("✅ Colonne nb_participants ajoutée");
                }
            }
            cols3.close();

        } catch (Exception e) {
            System.err.println("⚠️ Vérification colonnes : " + e.getMessage());
        }
    }

    public List<Evenement> getAll() {
        List<Evenement> liste = new ArrayList<>();
        if (connection == null) { System.err.println("⚠️ getAll() : connexion DB null"); return liste; }
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM evenement")) {
            while (rs.next()) {
                double prix = 0.0;
                int capacite = 0, nbPart = 0;
                try { prix = rs.getDouble("prix"); } catch (SQLException ignored) {}
                try { capacite = rs.getInt("capacite"); } catch (SQLException ignored) {}
                try { nbPart = rs.getInt("nb_participants"); } catch (SQLException ignored) {}
                liste.add(new Evenement(
                        rs.getInt("id"), rs.getString("nom"),
                        rs.getTimestamp("date_debut").toLocalDateTime(),
                        rs.getTimestamp("date_fin").toLocalDateTime(),
                        rs.getString("lieu"), rs.getString("statut"),
                        rs.getString("image"), rs.getString("description"),
                        rs.getString("coach_name"), rs.getString("objectifs"),
                        prix, capacite, nbPart
                ));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return liste;
    }

    /** Incrémente le nombre de participants d'un événement */
    public boolean incrementerParticipants(int evenementId) {
        String sql = "UPDATE evenement SET nb_participants = nb_participants + 1 WHERE id = ? AND (capacite = 0 OR nb_participants < capacite)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    // MÉTHODE DE JOINTURE
    public List<Sponsor> getSponsorsByEvenement(int evenementId) {
        List<Sponsor> sponsors = new ArrayList<>();
        String sql = "SELECT * FROM sponsor WHERE evenement_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, evenementId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sponsors.add(new Sponsor(
                            rs.getInt("id"), 
                            rs.getString("nom_partenaire"),
                            rs.getString("type"), 
                            rs.getString("description"),
                            rs.getString("statu"), 
                            rs.getString("logo"),
                            rs.getString("video_url"),
                            rs.getInt("evenement_id")
                    ));
                }
            }
        } catch (SQLException e) { System.err.println("Erreur SQL Jointure: " + e.getMessage()); }
        return sponsors;
    }

    public void ajouter(Evenement e) {
        String req = "INSERT INTO evenement (nom, date_debut, date_fin, lieu, statut, image, description, coach_name, objectifs, prix, capacite) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, e.getNom()); ps.setTimestamp(2, Timestamp.valueOf(e.getDate_debut()));
            ps.setTimestamp(3, Timestamp.valueOf(e.getDate_fin())); ps.setString(4, e.getLieu());
            ps.setString(5, e.getStatut()); ps.setString(6, e.getImage());
            ps.setString(7, e.getDescription()); ps.setString(8, e.getCoach_name());
            ps.setString(9, e.getObjectifs()); ps.setDouble(10, e.getPrix());
            ps.setInt(11, e.getCapacite());
            ps.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void modifier(Evenement e) {
        String req = "UPDATE evenement SET nom=?, date_debut=?, date_fin=?, lieu=?, statut=?, description=?, coach_name=?, objectifs=?, prix=?, capacite=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, e.getNom()); ps.setTimestamp(2, Timestamp.valueOf(e.getDate_debut()));
            ps.setTimestamp(3, Timestamp.valueOf(e.getDate_fin() != null ? e.getDate_fin() : e.getDate_debut().plusHours(2)));
            ps.setString(4, e.getLieu()); ps.setString(5, e.getStatut());
            ps.setString(6, e.getDescription()); ps.setString(7, e.getCoach_name());
            ps.setString(8, e.getObjectifs()); ps.setDouble(9, e.getPrix());
            ps.setInt(10, e.getCapacite()); ps.setInt(11, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    public void supprimer(int id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM evenement WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) { ex.printStackTrace(); }
    }
}