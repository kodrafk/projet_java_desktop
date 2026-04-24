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
    }

    // ═══════════ ADD ═══════════

    public void addItem(CourseItem item) {
        String query = "INSERT INTO liste_courses (nom_ingredient, quantite, unite, date_ajout, est_achete) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getIngredientName());
            ps.setDouble(2, item.getQuantity());
            ps.setString(3, item.getUnit());
            ps.setDate(4, Date.valueOf(item.getDateAdded()));
            ps.setBoolean(5, item.isPurchased());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("✅ Item added to shopping list: " + item.getIngredientName());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error adding item: " + e.getMessage());
        }
    }

    // ═══════════ GET ALL ═══════════

    public List<CourseItem> getAllItems() {
        List<CourseItem> items = new ArrayList<>();
        String query = "SELECT * FROM liste_courses ORDER BY est_achete ASC, date_ajout DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                items.add(mapResultSetToCourseItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching items: " + e.getMessage());
        }

        return items;
    }

    // ═══════════ GET PENDING ONLY ═══════════

    public List<CourseItem> getPendingItems() {
        List<CourseItem> items = new ArrayList<>();
        String query = "SELECT * FROM liste_courses WHERE est_achete = FALSE ORDER BY date_ajout DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                items.add(mapResultSetToCourseItem(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching pending items: " + e.getMessage());
        }

        return items;
    }

    // ═══════════ MARK AS PURCHASED ═══════════

    public void markAsPurchased(int id) {
        String query = "UPDATE liste_courses SET est_achete = TRUE WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Item marked as purchased: ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error marking item as purchased: " + e.getMessage());
        }
    }

    // ═══════════ DELETE ═══════════

    public void deleteItem(int id) {
        String query = "DELETE FROM liste_courses WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Item deleted from shopping list: ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting item: " + e.getMessage());
        }
    }

    // ═══════════ CHECK IF ALREADY EXISTS ═══════════

    public boolean itemAlreadyExists(String ingredientName) {
        String query = "SELECT COUNT(*) FROM liste_courses WHERE nom_ingredient = ? AND est_achete = FALSE";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, ingredientName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking item existence: " + e.getMessage());
        }

        return false;
    }

    // ═══════════ DELETE ALL PURCHASED ═══════════

    public void clearPurchasedItems() {
        String query = "DELETE FROM liste_courses WHERE est_achete = TRUE";

        try (Statement stmt = cnx.createStatement()) {
            int rowsAffected = stmt.executeUpdate(query);
            System.out.println("✅ Cleared " + rowsAffected + " purchased item(s)");
        } catch (SQLException e) {
            System.err.println("❌ Error clearing purchased items: " + e.getMessage());
        }
    }

    // ═══════════ MAPPING ═══════════

    private CourseItem mapResultSetToCourseItem(ResultSet rs) throws SQLException {
        CourseItem item = new CourseItem();
        item.setId(rs.getInt("id"));
        item.setIngredientName(rs.getString("nom_ingredient"));
        item.setQuantity(rs.getDouble("quantite"));
        item.setUnit(rs.getString("unite"));

        Date dateAdded = rs.getDate("date_ajout");
        if (dateAdded != null) {
            item.setDateAdded(dateAdded.toLocalDate());
        }

        item.setPurchased(rs.getBoolean("est_achete"));
        return item;
    }
}