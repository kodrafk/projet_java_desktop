package tn.esprit.projet.services;

import tn.esprit.projet.models.Ingredient;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IngredientService implements CRUD<Ingredient> {

    private final Connection cnx;

    // ═══════════ CONSTRUCTEUR ═══════════
    public IngredientService() {
        this.cnx = MyBDConnexion.getInstance().getCnx();
    }

    // ═══════════ AJOUTER (CORRIGÉ) ═══════════
    @Override
    public void ajouter(Ingredient ingredient) {
        String query = "INSERT INTO ingredient (nom, nom_en, categorie, quantite, unite, date_peremption, notes, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ingredient.getNom());
            ps.setString(2, ingredient.getNomEn());
            ps.setString(3, ingredient.getCategorie());
            ps.setDouble(4, ingredient.getQuantite());
            ps.setString(5, ingredient.getUnite());

            // Gestion de la date nullable
            if (ingredient.getDatePeremption() != null) {
                ps.setDate(6, Date.valueOf(ingredient.getDatePeremption()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setString(7, ingredient.getNotes());
            ps.setString(8, ingredient.getImage());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // Récupérer l'ID généré automatiquement
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    ingredient.setId(generatedKeys.getInt(1));
                }
                System.out.println("✅ Ingredient ajouté avec succès : " + ingredient.getNom() + " (ID: " + ingredient.getId() + ")");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    // ═══════════ MODIFIER ═══════════
    @Override
    public void modifier(Ingredient ingredient) {
        String query = "UPDATE ingredient SET nom = ?, nom_en = ?, categorie = ?, quantite = ?, " +
                "unite = ?, date_peremption = ?, notes = ?, image = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, ingredient.getNom());
            ps.setString(2, ingredient.getNomEn());
            ps.setString(3, ingredient.getCategorie());
            ps.setDouble(4, ingredient.getQuantite());
            ps.setString(5, ingredient.getUnite());

            if (ingredient.getDatePeremption() != null) {
                ps.setDate(6, Date.valueOf(ingredient.getDatePeremption()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setString(7, ingredient.getNotes());
            ps.setString(8, ingredient.getImage());
            ps.setInt(9, ingredient.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Ingredient modifié avec succès : ID " + ingredient.getId());
            } else {
                System.out.println("⚠️ Aucun ingredient trouvé avec l'ID : " + ingredient.getId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    // ═══════════ SUPPRIMER ═══════════
    @Override
    public void supprimer(int id) {
        String query = "DELETE FROM ingredient WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Ingredient supprimé avec succès : ID " + id);
            } else {
                System.out.println("⚠️ Aucun ingredient trouvé avec l'ID : " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // ═══════════ GET BY ID ═══════════
    @Override
    public Ingredient getById(int id) {
        String query = "SELECT * FROM ingredient WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToIngredient(rs);
            } else {
                System.out.println("⚠️ Aucun ingredient trouvé avec l'ID : " + id);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération : " + e.getMessage());
        }

        return null;
    }

    // ═══════════ GET ALL ═══════════
    @Override
    public List<Ingredient> getAll() {
        List<Ingredient> ingredients = new ArrayList<>();
        if (cnx == null) {
            System.err.println("⚠️ No DB connection — returning empty ingredient list.");
            return ingredients;
        }
        String query = "SELECT * FROM ingredient ORDER BY id DESC";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ingredients.add(mapResultSetToIngredient(rs));
            }

            System.out.println("✅ " + ingredients.size() + " ingredient(s) récupéré(s)");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération : " + e.getMessage());
        }

        return ingredients;
    }

    // ═══════════ MÉTHODE UTILITAIRE : Mapper ResultSet → Ingredient ═══════════
    private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();

        ingredient.setId(rs.getInt("id"));
        ingredient.setNom(rs.getString("nom"));
        ingredient.setNomEn(rs.getString("nom_en"));
        ingredient.setCategorie(rs.getString("categorie"));
        ingredient.setQuantite(rs.getDouble("quantite"));
        ingredient.setUnite(rs.getString("unite"));

        // Gestion de la date nullable
        Date datePeremption = rs.getDate("date_peremption");
        if (datePeremption != null) {
            ingredient.setDatePeremption(datePeremption.toLocalDate());
        }

        ingredient.setNotes(rs.getString("notes"));
        ingredient.setImage(rs.getString("image"));

        return ingredient;
    }

    // ═══════════ MÉTHODE BONUS : Recherche par nom ═══════════
    public List<Ingredient> rechercherParNom(String nom) {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM ingredient WHERE nom LIKE ? ORDER BY nom";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, "%" + nom + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ingredients.add(mapResultSetToIngredient(rs));
            }

            System.out.println("🔍 " + ingredients.size() + " résultat(s) pour : " + nom);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche : " + e.getMessage());
        }

        return ingredients;
    }
}