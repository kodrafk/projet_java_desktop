package tn.esprit.projet.repository;

import tn.esprit.projet.models.ProgressPhoto;
import tn.esprit.projet.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository pour gérer les photos de progression
 */
public class ProgressPhotoRepository {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Récupérer toutes les photos d'un utilisateur
     */
    public List<ProgressPhoto> findByUserId(int userId) {
        List<ProgressPhoto> photos = new ArrayList<>();
        String sql = "SELECT * FROM progress_photo WHERE user_id=? ORDER BY taken_at DESC";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                photos.add(map(rs));
            }
        } catch (SQLException e) {
            // Table might not exist yet
            System.out.println("[ProgressPhotoRepository] Table progress_photo not found or error: " + e.getMessage());
        }
        return photos;
    }

    /**
     * Récupérer une photo par ID
     */
    public ProgressPhoto findById(int id) {
        String sql = "SELECT * FROM progress_photo WHERE id=?";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sauvegarder une nouvelle photo
     */
    public void save(ProgressPhoto photo) {
        String sql = "INSERT INTO progress_photo (user_id, filename, caption, weight, taken_at, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";
        
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, photo.getUserId());
            ps.setString(2, photo.getFilename());
            ps.setString(3, photo.getCaption());
            ps.setDouble(4, photo.getWeight());
            ps.setTimestamp(5, photo.getTakenAt() != null ? Timestamp.valueOf(photo.getTakenAt()) : null);
            ps.executeUpdate();
            
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                photo.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mettre à jour une photo
     */
    public void update(ProgressPhoto photo) {
        String sql = "UPDATE progress_photo SET caption=?, weight=?, taken_at=? WHERE id=?";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, photo.getCaption());
            ps.setDouble(2, photo.getWeight());
            ps.setTimestamp(3, photo.getTakenAt() != null ? Timestamp.valueOf(photo.getTakenAt()) : null);
            ps.setInt(4, photo.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Supprimer une photo
     */
    public void delete(int id) {
        String sql = "DELETE FROM progress_photo WHERE id=?";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compter les photos d'un utilisateur
     */
    public int countByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM progress_photo WHERE user_id=?";
        
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            // Table might not exist
        }
        return 0;
    }

    /**
     * Mapper ResultSet vers ProgressPhoto
     */
    private ProgressPhoto map(ResultSet rs) throws SQLException {
        ProgressPhoto photo = new ProgressPhoto();
        photo.setId(rs.getInt("id"));
        photo.setUserId(rs.getInt("user_id"));
        photo.setFilename(rs.getString("filename"));
        photo.setCaption(rs.getString("caption"));
        photo.setWeight(rs.getDouble("weight"));
        
        Timestamp takenAt = rs.getTimestamp("taken_at");
        if (takenAt != null) {
            photo.setTakenAt(takenAt.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            photo.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return photo;
    }
}
