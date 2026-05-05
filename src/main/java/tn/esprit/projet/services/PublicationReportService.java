package tn.esprit.projet.services;

import tn.esprit.projet.models.PublicationReport;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les signalements de publications.
 */
public class PublicationReportService {

    private Connection cnx() {
        return MyBDConnexion.getInstance().getCnx();
    }

    /**
     * Ajoute un signalement.
     */
    public boolean report(PublicationReport r) {
        // Vérifier si l'utilisateur a déjà signalé cette publication
        if (hasAlreadyReported(r.getPublicationId(), r.getUserId())) {
            return false;
        }

        String sql = "INSERT INTO publication_report (publication_id, user_id, reason, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, r.getPublicationId());
            ps.setInt(2, r.getUserId());
            ps.setString(3, r.getReason());
            ps.setTimestamp(4, Timestamp.valueOf(r.getCreatedAt()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie si un utilisateur a déjà signalé une publication.
     */
    public boolean hasAlreadyReported(int publicationId, int userId) {
        String sql = "SELECT COUNT(*) FROM publication_report WHERE publication_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Compte le nombre de signalements pour une publication.
     */
    public int countReports(int publicationId) {
        String sql = "SELECT COUNT(*) FROM publication_report WHERE publication_id = ?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Liste les IDs des publications ayant atteint le seuil de signalement.
     */
    public List<Integer> getHighlyReportedPublications(int threshold) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT publication_id FROM publication_report GROUP BY publication_id HAVING COUNT(*) >= ?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("publication_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }

    /**
     * Supprime tous les signalements d'une publication (pour la 'republier').
     */
    public boolean deleteByPublicationId(int publicationId) {
        String sql = "DELETE FROM publication_report WHERE publication_id = ?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
