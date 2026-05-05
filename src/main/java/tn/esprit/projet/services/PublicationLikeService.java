package tn.esprit.projet.services;

import tn.esprit.projet.models.PublicationLike;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;

public class PublicationLikeService {

    private Connection cnx() {
        return MyBDConnexion.getInstance().getCnx();
    }

    public boolean addInteraction(PublicationLike like) {
        // First remove any existing interaction for this user and publication
        removeInteraction(like.getPublicationId(), like.getUserId());

        String sql = "INSERT INTO publication_like (created_at, publication_id, user_id, is_like) VALUES (NOW(), ?, ?, ?)";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, like.getPublicationId());
            ps.setInt(2, like.getUserId());
            ps.setBoolean(3, like.isLike());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PublicationLikeService.addInteraction] " + e.getMessage());
        }
        return false;
    }

    public boolean removeInteraction(int publicationId, int userId) {
        String sql = "DELETE FROM publication_like WHERE publication_id=? AND user_id=?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PublicationLikeService.removeInteraction] " + e.getMessage());
        }
        return false;
    }

    public int countLikes(int publicationId) {
        String sql = "SELECT COUNT(*) FROM publication_like WHERE publication_id=? AND is_like=1";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[PublicationLikeService.countLikes] " + e.getMessage());
        }
        return 0;
    }

    public int countDislikes(int publicationId) {
        String sql = "SELECT COUNT(*) FROM publication_like WHERE publication_id=? AND is_like=0";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[PublicationLikeService.countDislikes] " + e.getMessage());
        }
        return 0;
    }

    // Returns 1 for like, 0 for dislike, -1 for no interaction
    public int getUserInteraction(int publicationId, int userId) {
        String sql = "SELECT is_like FROM publication_like WHERE publication_id=? AND user_id=?";
        try (PreparedStatement ps = cnx().prepareStatement(sql)) {
            ps.setInt(1, publicationId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_like") ? 1 : 0;
            }
        } catch (SQLException e) {
            System.err.println("[PublicationLikeService.getUserInteraction] " + e.getMessage());
        }
        return -1;
    }
}
