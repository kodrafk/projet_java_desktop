package tn.esprit.projet.services;

import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorService implements CRUD<Sponsor> {
    private Connection cnx;

    public SponsorService() {
        cnx = MyBDConnexion.getInstance().getConnection();
        createTableIfNotExist();
    }

    private void createTableIfNotExist() {
        String req = "CREATE TABLE IF NOT EXISTS `sponsor` (" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                "  `nom_partenaire` varchar(255) NOT NULL," +
                "  `type` varchar(255) NOT NULL," +
                "  `description` text DEFAULT NULL," +
                "  `statu` varchar(50) NOT NULL," +
                "  `logo` varchar(255) DEFAULT NULL," +
                "  `video_url` varchar(255) DEFAULT NULL," +
                "  `evenement_id` int(11) DEFAULT NULL," +
                "  PRIMARY KEY (`id`)," +
                "  KEY `evenement_id` (`evenement_id`)," +
                "  CONSTRAINT `sponsor_ibfk_1` FOREIGN KEY (`evenement_id`) REFERENCES `evenement` (`id`) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        try {
            Statement st = cnx.createStatement();
            st.execute(req);
        } catch (SQLException e) {
            System.err.println("Erreur création table sponsor: " + e.getMessage());
        }
    }

    @Override
    public void ajouter(Sponsor sponsor) {
        String req = "INSERT INTO sponsor (nom_partenaire, type, description, statu, logo, video_url, evenement_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, sponsor.getNom_partenaire());
            ps.setString(2, sponsor.getType());
            ps.setString(3, sponsor.getDescription());
            ps.setString(4, sponsor.getStatu());
            ps.setString(5, sponsor.getLogo());
            ps.setString(6, sponsor.getVideo_url());
            if (sponsor.getEvenement_id() > 0) {
                ps.setInt(7, sponsor.getEvenement_id());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.executeUpdate();
            System.out.println("Sponsor ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Sponsor sponsor) {
        String req = "UPDATE sponsor SET nom_partenaire=?, type=?, description=?, statu=?, logo=?, video_url=?, evenement_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, sponsor.getNom_partenaire());
            ps.setString(2, sponsor.getType());
            ps.setString(3, sponsor.getDescription());
            ps.setString(4, sponsor.getStatu());
            ps.setString(5, sponsor.getLogo());
            ps.setString(6, sponsor.getVideo_url());
            if (sponsor.getEvenement_id() > 0) {
                ps.setInt(7, sponsor.getEvenement_id());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8, sponsor.getId());
            ps.executeUpdate();
            System.out.println("Sponsor modifié avec succès !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM sponsor WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Sponsor supprimé !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Sponsor> getAll() {
        List<Sponsor> list = new ArrayList<>();
        String req = "SELECT * FROM sponsor";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                list.add(new Sponsor(
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
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    @Override
    public Sponsor getById(int id) {
        String req = "SELECT * FROM sponsor WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Sponsor(
                        rs.getInt("id"),
                        rs.getString("nom_partenaire"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("statu"),
                        rs.getString("logo"),
                        rs.getString("video_url"),
                        rs.getInt("evenement_id")
                );
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public List<Sponsor> getByEvenementId(int evenementId) {
        List<Sponsor> list = new ArrayList<>();
        String req = "SELECT * FROM sponsor WHERE evenement_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, evenementId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Sponsor(
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
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }
}
