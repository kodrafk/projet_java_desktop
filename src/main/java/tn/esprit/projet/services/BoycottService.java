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
    }

    // ===========================
    // MAPPER ResultSet → BoycottBrand
    // ===========================
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

    // ===========================
    // CHECK BRAND (Scanner)
    // ===========================
    public BoycottBrand checkBrand(String brandsFromApi) {
        if (brandsFromApi == null || brandsFromApi.isBlank()) return null;

        String[] brands = brandsFromApi.split(",");
        String sql = "SELECT * FROM boycott_brands " +
                "WHERE LOWER(brand_name) LIKE ? " +
                "OR LOWER(parent_company) LIKE ? LIMIT 1";

        try {
            for (String brand : brands) {
                String search = "%" + brand.trim().toLowerCase() + "%";
                PreparedStatement ps = cnx.prepareStatement(sql);
                ps.setString(1, search);
                ps.setString(2, search);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("BoycottService.checkBrand error: " + e.getMessage());
        }
        return null;
    }

    // ===========================
    // GET ALL BOYCOTTED BRANDS
    // ===========================
    public List<BoycottBrand> getAll() {
        List<BoycottBrand> list = new ArrayList<>();
        String sql = "SELECT * FROM boycott_brands ORDER BY category, brand_name";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BoycottService.getAll error: " + e.getMessage());
        }
        return list;
    }

    // ===========================
    // SEARCH BY NAME
    // ===========================
    public List<BoycottBrand> searchByName(String keyword) {
        List<BoycottBrand> list = new ArrayList<>();
        if (keyword == null || keyword.isBlank()) return getAll();

        String sql = "SELECT * FROM boycott_brands " +
                "WHERE LOWER(brand_name) LIKE ? " +
                "OR LOWER(parent_company) LIKE ? " +
                "OR LOWER(category) LIKE ? " +
                "ORDER BY brand_name";
        try {
            String search = "%" + keyword.trim().toLowerCase() + "%";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BoycottService.searchByName error: " + e.getMessage());
        }
        return list;
    }

    // ===========================
    // GET BY CATEGORY
    // ===========================
    public List<BoycottBrand> getByCategory(String category) {
        List<BoycottBrand> list = new ArrayList<>();
        String sql = "SELECT * FROM boycott_brands " +
                "WHERE LOWER(category) = ? " +
                "ORDER BY brand_name";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, category.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("BoycottService.getByCategory error: " + e.getMessage());
        }
        return list;
    }

    // ===========================
    // COUNT TOTAL
    // ===========================
    public int getTotalBoycottedBrands() {
        String sql = "SELECT COUNT(*) FROM boycott_brands";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("BoycottService.getTotalBoycottedBrands error: " + e.getMessage());
        }
        return 0;
    }
}