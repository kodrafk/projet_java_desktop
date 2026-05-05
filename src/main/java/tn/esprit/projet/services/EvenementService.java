package tn.esprit.projet.services;

import tn.esprit.projet.models.Evenement;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements CRUD<Evenement> {
    private Connection cnx;

    public EvenementService() {
        cnx = MyBDConnexion.getInstance().getConnection();
        createTableIfNotExist();
    }

    private void createTableIfNotExist() {
        String req = "CREATE TABLE IF NOT EXISTS `evenement` (" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                "  `nom` varchar(255) NOT NULL," +
                "  `date_debut` datetime NOT NULL," +
                "  `date_fin` datetime NOT NULL," +
                "  `lieu` varchar(255) NOT NULL," +
                "  `statut` varchar(50) NOT NULL," +
                "  `image` varchar(255) DEFAULT NULL," +
                "  `description` text DEFAULT NULL," +
                "  `coach_name` varchar(255) DEFAULT NULL," +
                "  `objectifs` text DEFAULT NULL," +
                "  `prix` double DEFAULT 0," +
                "  `capacite` int(11) DEFAULT 0," +
                "  `nb_participants` int(11) DEFAULT 0," +
                "  PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        try {
            Statement st = cnx.createStatement();
            st.execute(req);
        } catch (SQLException e) {
            System.err.println("Erreur création table evenement: " + e.getMessage());
        }
    }

    @Override
    public void ajouter(Evenement evenement) {
        String req = "INSERT INTO evenement (nom, date_debut, date_fin, lieu, statut, image, description, coach_name, objectifs, prix, capacite, nb_participants) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, evenement.getNom());
            ps.setTimestamp(2, Timestamp.valueOf(evenement.getDate_debut()));
            ps.setTimestamp(3, Timestamp.valueOf(evenement.getDate_fin()));
            ps.setString(4, evenement.getLieu());
            ps.setString(5, evenement.getStatut());
            ps.setString(6, evenement.getImage());
            ps.setString(7, evenement.getDescription());
            ps.setString(8, evenement.getCoach_name());
            ps.setString(9, evenement.getObjectifs());
            ps.setDouble(10, evenement.getPrix());
            ps.setInt(11, evenement.getCapacite());
            ps.setInt(12, evenement.getNbParticipants());
            ps.executeUpdate();
            System.out.println("Événement ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Evenement evenement) {
        String req = "UPDATE evenement SET nom=?, date_debut=?, date_fin=?, lieu=?, statut=?, image=?, description=?, coach_name=?, objectifs=?, prix=?, capacite=?, nb_participants=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, evenement.getNom());
            ps.setTimestamp(2, Timestamp.valueOf(evenement.getDate_debut()));
            ps.setTimestamp(3, Timestamp.valueOf(evenement.getDate_fin()));
            ps.setString(4, evenement.getLieu());
            ps.setString(5, evenement.getStatut());
            ps.setString(6, evenement.getImage());
            ps.setString(7, evenement.getDescription());
            ps.setString(8, evenement.getCoach_name());
            ps.setString(9, evenement.getObjectifs());
            ps.setDouble(10, evenement.getPrix());
            ps.setInt(11, evenement.getCapacite());
            ps.setInt(12, evenement.getNbParticipants());
            ps.setInt(13, evenement.getId());
            ps.executeUpdate();
            System.out.println("Événement modifié avec succès !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM evenement WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Événement supprimé !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Evenement> getAll() {
        List<Evenement> list = new ArrayList<>();
        String req = "SELECT * FROM evenement";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                list.add(new Evenement(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getTimestamp("date_debut").toLocalDateTime(),
                        rs.getTimestamp("date_fin").toLocalDateTime(),
                        rs.getString("lieu"),
                        rs.getString("statut"),
                        rs.getString("image"),
                        rs.getString("description"),
                        rs.getString("coach_name"),
                        rs.getString("objectifs"),
                        rs.getDouble("prix"),
                        rs.getInt("capacite"),
                        rs.getInt("nb_participants")
                ));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    @Override
    public Evenement getById(int id) {
        String req = "SELECT * FROM evenement WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Evenement(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getTimestamp("date_debut").toLocalDateTime(),
                        rs.getTimestamp("date_fin").toLocalDateTime(),
                        rs.getString("lieu"),
                        rs.getString("statut"),
                        rs.getString("image"),
                        rs.getString("description"),
                        rs.getString("coach_name"),
                        rs.getString("objectifs"),
                        rs.getDouble("prix"),
                        rs.getInt("capacite"),
                        rs.getInt("nb_participants")
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public void incrementParticipants(int id) {
        String req = "UPDATE evenement SET nb_participants = nb_participants + 1 WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur incrément participants: " + e.getMessage());
        }
    }
}
