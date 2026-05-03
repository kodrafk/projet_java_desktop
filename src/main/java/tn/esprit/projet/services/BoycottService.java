package tn.esprit.projet.services;

import tn.esprit.projet.models.BoycottBrand;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BoycottService {

    private Connection cnx;

    public BoycottService() {
        cnx = MyBDConnexion.getInstance().getCnx();
        ensureTableExists();
    }

    private void ensureTableExists() {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `boycott_brands` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `brand_name` VARCHAR(255) NOT NULL," +
                "  `parent_company` VARCHAR(255)," +
                "  `reason` TEXT," +
                "  `alternatives` TEXT," +
                "  `category` VARCHAR(100)," +
                "  `source_url` VARCHAR(500)," +
                "  `date_added` DATE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
        } catch (SQLException e) {
            System.err.println("BoycottService table setup: " + e.getMessage());
        }
    }

    private BoycottBrand mapRow(ResultSet rs) throws SQLException {
        BoycottBrand b = new BoycottBrand();
        b.setId(rs.getInt("id"));
        b.setBrandName(rs.getString("brand_name"));
        b.setParentCompany(rs.getString("parent_company"));
        b.setReason(rs.getString("reason"));
        b.setAlternatives(rs.getString("alternatives"));
        b.setCategory(rs.getString("category"));
        return b;
    }

    public BoycottBrand checkBrand(String brandsFromApi) {
        if (brandsFromApi == null || brandsFromApi.isBlank()) return null;
        String[] brands = brandsFromApi.split(",");
        String sql = "SELECT * FROM boycott_brands WHERE LOWER(brand_name) LIKE ? OR LOWER(parent_company) LIKE ? LIMIT 1";
        try {
            for (String brand : brands) {
                String search = "%" + brand.trim().toLowerCase() + "%";
                PreparedStatement ps = cnx.prepareStatement(sql);
                ps.setString(1, search); ps.setString(2, search);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("BoycottService.checkBrand: " + e.getMessage()); }
        return null;
    }

    public List<BoycottBrand> getAll() {
        List<BoycottBrand> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement("SELECT * FROM boycott_brands ORDER BY category, brand_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("BoycottService.getAll: " + e.getMessage()); }
        return list;
    }

    public List<BoycottBrand> searchByName(String keyword) {
        List<BoycottBrand> list = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return getAll();
        String sql = "SELECT * FROM boycott_brands WHERE LOWER(brand_name) LIKE ? OR LOWER(parent_company) LIKE ? OR LOWER(category) LIKE ? ORDER BY brand_name";
        try {
            String search = "%" + keyword.trim().toLowerCase() + "%";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, search); ps.setString(2, search); ps.setString(3, search);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("BoycottService.searchByName: " + e.getMessage()); }
        return list;
    }

    public int getTotalBoycottedBrands() {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM boycott_brands");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("BoycottService.count: " + e.getMessage()); }
        return 0;
    }
}
