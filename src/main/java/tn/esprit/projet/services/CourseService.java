package tn.esprit.projet.services;

import tn.esprit.projet.models.CourseItem;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CourseService {

    private final Connection cnx;

    public CourseService() {
        this.cnx = MyBDConnexion.getInstance().getCnx();
        ensureTableExists();
    }

    private void ensureTableExists() {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `liste_courses` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `nom_ingredient` VARCHAR(255) NOT NULL," +
                "  `quantite` DOUBLE DEFAULT 1," +
                "  `unite` VARCHAR(50)," +
                "  `date_ajout` DATE," +
                "  `est_achete` TINYINT(1) DEFAULT 0" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
        } catch (SQLException e) { System.err.println("CourseService table: " + e.getMessage()); }
    }

    public void addItem(CourseItem item) {
        String query = "INSERT INTO liste_courses (nom_ingredient, quantite, unite, date_ajout, est_achete) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getIngredientName()); ps.setDouble(2, item.getQuantity());
            ps.setString(3, item.getUnit()); ps.setDate(4, Date.valueOf(item.getDateAdded())); ps.setBoolean(5, item.isPurchased());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) item.setId(keys.getInt(1));
        } catch (SQLException e) { System.err.println("CourseService.addItem: " + e.getMessage()); }
    }

    public List<CourseItem> getAllItems() {
        List<CourseItem> items = new ArrayList<>();
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM liste_courses ORDER BY est_achete ASC, date_ajout DESC")) {
            while (rs.next()) items.add(map(rs));
        } catch (SQLException e) { System.err.println("CourseService.getAll: " + e.getMessage()); }
        return items;
    }

    public List<CourseItem> getPendingItems() {
        List<CourseItem> items = new ArrayList<>();
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM liste_courses WHERE est_achete = FALSE ORDER BY date_ajout DESC")) {
            while (rs.next()) items.add(map(rs));
        } catch (SQLException e) { System.err.println("CourseService.getPending: " + e.getMessage()); }
        return items;
    }

    public void markAsPurchased(int id) {
        try (PreparedStatement ps = cnx.prepareStatement("UPDATE liste_courses SET est_achete = TRUE WHERE id = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("CourseService.markPurchased: " + e.getMessage()); }
    }

    public void deleteItem(int id) {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM liste_courses WHERE id = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("CourseService.delete: " + e.getMessage()); }
    }

    public boolean itemAlreadyExists(String ingredientName) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM liste_courses WHERE nom_ingredient = ? AND est_achete = FALSE")) {
            ps.setString(1, ingredientName); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { System.err.println("CourseService.exists: " + e.getMessage()); }
        return false;
    }

    public void clearPurchasedItems() {
        try (Statement stmt = cnx.createStatement()) {
            stmt.executeUpdate("DELETE FROM liste_courses WHERE est_achete = TRUE");
        } catch (SQLException e) { System.err.println("CourseService.clear: " + e.getMessage()); }
    }

    private CourseItem map(ResultSet rs) throws SQLException {
        CourseItem item = new CourseItem();
        item.setId(rs.getInt("id")); item.setIngredientName(rs.getString("nom_ingredient"));
        item.setQuantity(rs.getDouble("quantite")); item.setUnit(rs.getString("unite"));
        Date d = rs.getDate("date_ajout"); if (d != null) item.setDateAdded(d.toLocalDate());
        item.setPurchased(rs.getBoolean("est_achete"));
        return item;
    }
}
