package tn.esprit.projet.services;

import tn.esprit.projet.models.Sponsor;
import tn.esprit.projet.utils.MyBDConnexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorService {
    private Connection connection;

    public SponsorService() {
        this.connection = MyBDConnexion.getInstance().getConnection();
    }

    public void ajouter(Sponsor s) {
        String req = "INSERT INTO sponsor (nom_partenaire, type, description, statu, logo, video_url, evenement_id) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, s.getNom_partenaire());
            ps.setString(2, s.getType());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getStatu());
            ps.setString(5, s.getLogo());
            ps.setString(6, s.getVideo_url());
            ps.setInt(7, s.getEvenement_id());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void supprimer(int id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM sponsor WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<Sponsor> getAll() {
        List<Sponsor> liste = new ArrayList<>();
        String req = "SELECT * FROM sponsor ORDER BY id DESC";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new Sponsor(
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }

    public List<Sponsor> getSponsorsByEvenement(int evenementId) {
        List<Sponsor> liste = new ArrayList<>();
        String req = "SELECT * FROM sponsor WHERE evenement_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, evenementId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new Sponsor(
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return liste;
    }
}