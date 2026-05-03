package tn.esprit.projet.services;

import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecetteFavorisService {

    private final Connection cnx;

    public RecetteFavorisService() {
        this.cnx = MyBDConnexion.getInstance().getCnx();
        ensureTableExists();
    }

    private void ensureTableExists() {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `recette_favoris` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `user_id` INT NOT NULL," +
                "  `recette_id` INT NOT NULL," +
                "  `added_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  UNIQUE KEY `user_recette` (`user_id`, `recette_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
        } catch (SQLException e) { System.err.println("RecetteFavorisService table: " + e.getMessage()); }
    }

    public void addFavorite(int userId, int recetteId) {
        try (PreparedStatement ps = cnx.prepareStatement("INSERT IGNORE INTO recette_favoris (user_id, recette_id, added_at) VALUES (?, ?, NOW())")) {
            ps.setInt(1, userId); ps.setInt(2, recetteId); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("RecetteFavoris.add: " + e.getMessage()); }
    }

    public void removeFavorite(int userId, int recetteId) {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM recette_favoris WHERE user_id = ? AND recette_id = ?")) {
            ps.setInt(1, userId); ps.setInt(2, recetteId); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("RecetteFavoris.remove: " + e.getMessage()); }
    }

    public boolean isFavorite(int userId, int recetteId) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM recette_favoris WHERE user_id = ? AND recette_id = ?")) {
            ps.setInt(1, userId); ps.setInt(2, recetteId);
            ResultSet rs = ps.executeQuery(); if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { System.err.println("RecetteFavoris.isFav: " + e.getMessage()); }
        return false;
    }

    public void toggleFavorite(int userId, int recetteId) {
        if (isFavorite(userId, recetteId)) removeFavorite(userId, recetteId);
        else addFavorite(userId, recetteId);
    }

    public List<Integer> getFavoriteIds(int userId) {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement("SELECT recette_id FROM recette_favoris WHERE user_id = ?")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("recette_id"));
        } catch (SQLException e) { System.err.println("RecetteFavoris.getIds: " + e.getMessage()); }
        return ids;
    }

    public int countFavorites(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM recette_favoris WHERE user_id = ?")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery(); if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("RecetteFavoris.count: " + e.getMessage()); }
        return 0;
    }
}
