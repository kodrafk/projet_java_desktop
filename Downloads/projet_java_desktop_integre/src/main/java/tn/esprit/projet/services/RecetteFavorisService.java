package tn.esprit.projet.services;

import tn.esprit.projet.models.Recette;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecetteFavorisService {

    private final Connection cnx;
    private final RecetteService recetteService;

    public RecetteFavorisService() {
        this.cnx = MyBDConnexion.getInstance().getCnx();
        this.recetteService = new RecetteService();
    }

    public void addFavorite(int userId, int recetteId) {
        String query = "INSERT INTO recette_favoris (user_id, recette_id, added_at) VALUES (?, ?, NOW())";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, recetteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error adding favorite: " + e.getMessage());
        }
    }

    public void removeFavorite(int userId, int recetteId) {
        String query = "DELETE FROM recette_favoris WHERE user_id = ? AND recette_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, recetteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Error removing favorite: " + e.getMessage());
        }
    }

    public boolean isFavorite(int userId, int recetteId) {
        String query = "SELECT COUNT(*) FROM recette_favoris WHERE user_id = ? AND recette_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, recetteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking favorite: " + e.getMessage());
        }
        return false;
    }

    public void toggleFavorite(int userId, int recetteId) {
        if (isFavorite(userId, recetteId)) {
            removeFavorite(userId, recetteId);
        } else {
            addFavorite(userId, recetteId);
        }
    }

    public List<Integer> getFavoriteIds(int userId) {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT recette_id FROM recette_favoris WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("recette_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching favorites: " + e.getMessage());
        }
        return ids;
    }

    public int countFavorites(int userId) {
        String query = "SELECT COUNT(*) FROM recette_favoris WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error counting favorites: " + e.getMessage());
        }
        return 0;
    }
}