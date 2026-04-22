package tn.esprit.projet.services;

import tn.esprit.projet.models.AIRecipeResult;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;

public class AIRecipeSaveService {

    private final Connection cnx;

    public AIRecipeSaveService() {
        cnx = MyBDConnexion.getInstance().getCnx();
    }

    // ═════════════════════════════════════════════════
    // SAUVEGARDER recette générée par AI
    // ═════════════════════════════════════════════════
    public int sauvegarder(AIRecipeResult result, int userId) {
        int recetteId = -1;

        try {
            cnx.setAutoCommit(false);

            // 1. INSERT recette
            recetteId = insertRecette(result, userId);
            if (recetteId == -1) throw new Exception("Échec INSERT recette");

            // 2. INSERT recette_info_plus
            insertRecetteInfoPlus(result, recetteId);

            cnx.commit();
            System.out.println("✅ Recette AI sauvegardée → id=" + recetteId);

        } catch (Exception e) {
            System.err.println("❌ sauvegarder : " + e.getMessage());
            try { cnx.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            try { cnx.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }

        return recetteId;
    }

    // ─── INSERT recette ───────────────────────────────
    private int insertRecette(AIRecipeResult result, int userId) throws SQLException {
        String sql =
                "INSERT INTO recette " +
                        "(nom, type, difficulte, temps_preparation, " +
                        " description, image, etapes, user_id, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement ps = cnx.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, result.getNom());
            ps.setString(2, result.getType());
            ps.setString(3, result.getDifficulte());
            ps.setInt   (4, result.getTempsPreparation());
            ps.setString(5, result.getDescription());
            ps.setString(6, result.getImageUrl());
            ps.setString(7, result.getStepsAsJson());
            ps.setInt   (8, userId);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    // ─── INSERT recette_info_plus ─────────────────────
    private void insertRecetteInfoPlus(AIRecipeResult result,
                                       int recetteId) throws SQLException {

        // Déterminer moment_repas selon type
        String momentRepas;
        switch (result.getType().toLowerCase()) {
            case "dessert", "drinks" -> momentRepas = "PETIT_DEJEUNER";
            case "entrée", "entree"  -> momentRepas = "DEJEUNER";
            default                  -> momentRepas = "DINER";
        }

        String sql =
                "INSERT INTO recette_info_plus " +
                        "(recette_id, calories, proteines, lipides, glucides, " +
                        " moment_repas, is_vegetarien, is_vegan, is_halal, " +
                        " contains_gluten, contains_lactose, contains_nuts, contains_eggs) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt   (1,  recetteId);
            ps.setInt   (2,  result.getCalories());
            ps.setFloat (3,  result.getProteines());
            ps.setFloat (4,  result.getLipides());
            ps.setFloat (5,  result.getGlucides());
            ps.setString(6,  momentRepas);
            ps.setInt   (7,  result.isVegetarian()      ? 1 : 0);
            ps.setInt   (8,  result.isVegan()            ? 1 : 0);
            ps.setInt   (9,  result.isHalal()            ? 1 : 0);
            ps.setInt   (10, result.isContainsGluten()   ? 1 : 0);
            ps.setInt   (11, result.isContainsLactose()  ? 1 : 0);
            ps.setInt   (12, result.isContainsNuts()     ? 1 : 0);
            ps.setInt   (13, result.isContainsEggs()     ? 1 : 0);
            ps.executeUpdate();
        }
    }
}