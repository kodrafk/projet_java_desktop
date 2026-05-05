package tn.esprit.projet.services;

import tn.esprit.projet.models.Recette;
import tn.esprit.projet.models.RecetteIngredient;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecetteService implements CRUD<Recette> {

    private final Connection cnx;
    private final IngredientService ingredientService;

    public RecetteService() {
        cnx = MyBDConnexion.getInstance().getCnx();
        ingredientService = new IngredientService();
    }

    @Override
    public void ajouter(Recette recette) {
        // Priority 1: use userId already set on the recette (set by the controller from SessionManager)
        // Priority 2: read SessionManager directly as fallback
        // Priority 3: hardcoded 1 as last resort
        int userId;
        if (recette.getUserId() > 0) {
            userId = recette.getUserId(); // Controller already set the correct connected user ID
        } else if (tn.esprit.projet.utils.SessionManager.getCurrentUser() != null) {
            userId = tn.esprit.projet.utils.SessionManager.getCurrentUser().getId();
            System.out.println("[RecetteService] Using session userId=" + userId);
        } else {
            userId = 1;
            System.err.println("[RecetteService] WARNING: No user in session and no userId on recette. Using fallback=1.");
        }
        System.out.println("[RecetteService] Creating recette with user_id=" + userId);

        // 2. Get the next ID manually to bypass the "Field 'id' doesn't have a default value" error
        int nextId = 1;
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(id) FROM recette")) {
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Warning: Could not fetch next ID, using 1. " + e.getMessage());
        }

        // 3. Perform the INSERT including the ID
        String sql = "INSERT INTO recette (id, nom, type, difficulte, temps_preparation, description, image, user_id, created_at, etapes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, nextId);
            pst.setString(2, recette.getNom());
            pst.setString(3, recette.getType());
            pst.setString(4, recette.getDifficulte());
            pst.setInt(5, recette.getTempsPreparation());
            pst.setString(6, recette.getDescription());
            pst.setString(7, recette.getImage());
            pst.setInt(8, userId);
            pst.setTimestamp(9, Timestamp.valueOf(recette.getCreatedAt() != null ? recette.getCreatedAt() : LocalDateTime.now()));
            
            // Format steps as JSON array
            String jsonEtapes = "[]";
            if (recette.getEtapes() != null && !recette.getEtapes().isEmpty()) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < recette.getEtapes().size(); i++) {
                    String step = recette.getEtapes().get(i).replace("\"", "\\\"");
                    sb.append("\"").append(step).append("\"");
                    if (i < recette.getEtapes().size() - 1) sb.append(",");
                }
                sb.append("]");
                jsonEtapes = sb.toString();
            }
            pst.setString(10, jsonEtapes);

            pst.executeUpdate();
            recette.setId(nextId);
            
            // 4. Add ingredients and update stock
            if (recette.getRecetteIngredients() != null) {
                for (RecetteIngredient ri : recette.getRecetteIngredients()) {
                    addIngredientToRecette(nextId, ri.getIngredientId(), ri.getQuantite());
                    
                    // Deduct from stock
                    double qtyToDeduct = extractQuantity(ri.getQuantite());
                    if (qtyToDeduct > 0) {
                        ingredientService.updateQuantite(ri.getIngredientId(), -qtyToDeduct);
                    }
                }
            }
            
            System.out.println("✅ Recette ajoutée avec succès et stock mis à jour (ID: " + nextId + "): " + recette.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout recette: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    private double extractQuantity(String quantiteStr) {
        if (quantiteStr == null || quantiteStr.trim().isEmpty()) return 0;
        try {
            // Extracts numbers from a string like "200g" or "2 kg"
            String numericPart = quantiteStr.replaceAll("[^0-9.]", "");
            if (numericPart.isEmpty()) return 0;
            double val = Double.parseDouble(numericPart);
            
            // If it contains "kg", multiply by 1000 to normalize to grams?
            // Actually, we don't know the unit in the ingredient table, but let's assume it matches.
            // A more robust way would be to check the unit of the ingredient.
            return val;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void modifier(Recette recette) {
        // 1. Get old ingredients to restore stock
        Recette oldRecette = getById(recette.getId());
        if (oldRecette != null && oldRecette.getRecetteIngredients() != null) {
            for (RecetteIngredient ri : oldRecette.getRecetteIngredients()) {
                double qtyToRestore = extractQuantity(ri.getQuantite());
                if (qtyToRestore > 0) {
                    ingredientService.updateQuantite(ri.getIngredientId(), qtyToRestore);
                }
            }
        }

        String sql = "UPDATE recette SET nom=?, type=?, difficulte=?, temps_preparation=?, description=?, image=?, etapes=? WHERE id=?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, recette.getNom());
            pst.setString(2, recette.getType());
            pst.setString(3, recette.getDifficulte());
            pst.setInt(4, recette.getTempsPreparation());
            pst.setString(5, recette.getDescription());
            pst.setString(6, recette.getImage());
            
            String jsonEtapes = "[]";
            if (recette.getEtapes() != null && !recette.getEtapes().isEmpty()) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < recette.getEtapes().size(); i++) {
                    String step = recette.getEtapes().get(i).replace("\"", "\\\"");
                    sb.append("\"").append(step).append("\"");
                    if (i < recette.getEtapes().size() - 1) sb.append(",");
                }
                sb.append("]");
                jsonEtapes = sb.toString();
            }
            pst.setString(7, jsonEtapes);
            pst.setInt(8, recette.getId());

            pst.executeUpdate();

            // 2. Update ingredients link: delete old ones and add new ones
            String deleteIngredientsSql = "DELETE FROM recette_ingredient WHERE recette_id = ?";
            try (PreparedStatement deletePst = cnx.prepareStatement(deleteIngredientsSql)) {
                deletePst.setInt(1, recette.getId());
                deletePst.executeUpdate();
            }

            // 3. Add new ingredients and deduct stock
            if (recette.getRecetteIngredients() != null) {
                for (RecetteIngredient ri : recette.getRecetteIngredients()) {
                    addIngredientToRecette(recette.getId(), ri.getIngredientId(), ri.getQuantite());
                    
                    double qtyToDeduct = extractQuantity(ri.getQuantite());
                    if (qtyToDeduct > 0) {
                        ingredientService.updateQuantite(ri.getIngredientId(), -qtyToDeduct);
                    }
                }
            }

            System.out.println("✅ Recette mise à jour et stock actualisé: " + recette.getNom());
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour recette: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String deleteLinksSql = "DELETE FROM recette_ingredient WHERE recette_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(deleteLinksSql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur suppression liens: " + e.getMessage());
        }

        String sql = "DELETE FROM recette WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Recette supprimée: ID " + id);
        } catch (SQLException e) {
            System.err.println("Erreur suppression recette: " + e.getMessage());
        }
    }

    @Override
    public Recette getById(int id) {
        String sql = "SELECT * FROM recette WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Recette recette = mapResultSetToRecette(rs);
                    recette.setRecetteIngredients(getIngredientsForRecette(id));
                    return recette;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture recette: " + e.getMessage());
        }

        return null;
    }

    @Override
    public List<Recette> getAll() {
        List<Recette> recettes = new ArrayList<>();
        String sql = "SELECT * FROM recette ORDER BY created_at DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Recette r = mapResultSetToRecette(rs);
                r.setRecetteIngredients(getIngredientsForRecette(r.getId()));
                recettes.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture recettes: " + e.getMessage());
        }

        return recettes;
    }

    public void add(Recette recette) { ajouter(recette); }
    public void update(Recette recette) { modifier(recette); }
    public void delete(int id) { supprimer(id); }

    public void addIngredientToRecette(int recetteId, int ingredientId, String quantite) {
        String sql = "INSERT INTO recette_ingredient (recette_id, ingredient_id, quantite) VALUES (?, ?, ?)";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, recetteId);
            pst.setInt(2, ingredientId);
            pst.setString(3, quantite);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur ajout ingrédient à recette: " + e.getMessage());
        }
    }

    public void removeIngredientFromRecette(int recetteId, int ingredientId) {
        String sql = "DELETE FROM recette_ingredient WHERE recette_id = ? AND ingredient_id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, recetteId);
            pst.setInt(2, ingredientId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur retrait ingrédient: " + e.getMessage());
        }
    }

    private List<RecetteIngredient> getIngredientsForRecette(int recetteId) {
        List<RecetteIngredient> ingredients = new ArrayList<>();
        String sql = "SELECT ri.*, i.nom as ingredient_nom FROM recette_ingredient ri " +
                "JOIN ingredient i ON ri.ingredient_id = i.id " +
                "WHERE ri.recette_id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, recetteId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    RecetteIngredient ri = new RecetteIngredient();
                    ri.setId(rs.getInt("id"));
                    ri.setRecetteId(rs.getInt("recette_id"));
                    ri.setIngredientId(rs.getInt("ingredient_id"));
                    ri.setQuantite(rs.getString("quantite"));
                    ri.setIngredientNom(rs.getString("ingredient_nom"));
                    ingredients.add(ri);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement ingrédients: " + e.getMessage());
        }

        return ingredients;
    }

    public List<Recette> search(String keyword, String type, String difficulte) {
        List<Recette> recettes = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM recette WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (nom LIKE ? OR description LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        if (type != null && !type.isEmpty() && !type.equals("Tous")) {
            sql.append(" AND type = ?");
            params.add(type);
        }

        if (difficulte != null && !difficulte.isEmpty() && !difficulte.equals("Tous")) {
            sql.append(" AND difficulte = ?");
            params.add(difficulte);
        }

        sql.append(" ORDER BY created_at DESC");

        try (PreparedStatement pst = cnx.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Recette r = mapResultSetToRecette(rs);
                    r.setRecetteIngredients(getIngredientsForRecette(r.getId()));
                    recettes.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche: " + e.getMessage());
        }

        return recettes;
    }

    public List<Recette> searchByNom(String keyword) { return search(keyword, null, null); }
    public List<Recette> getRecettesByType(String type) { return search(null, type, null); }
    public List<Recette> getRecettesByDifficulte(String difficulte) { return search(null, null, difficulte); }

    public List<Recette> getByUserId(int userId) {
        List<Recette> recettes = new ArrayList<>();
        String sql = "SELECT * FROM recette WHERE user_id = ? ORDER BY created_at DESC";

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Recette r = mapResultSetToRecette(rs);
                    r.setRecetteIngredients(getIngredientsForRecette(r.getId()));
                    recettes.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture recettes par user_id: " + e.getMessage());
        }

        return recettes;
    }

    public int countByType(String type) {
        String sql = "SELECT COUNT(*) FROM recette WHERE type = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, type);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur countByType: " + e.getMessage());
        }
        return 0;
    }

    public int countByTypeAndUserId(String type, int userId) {
        String sql = "SELECT COUNT(*) FROM recette WHERE type = ? AND user_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, type);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur countByTypeAndUserId: " + e.getMessage());
        }
        return 0;
    }

    public int countByDifficulte(String difficulte) {
        String sql = "SELECT COUNT(*) FROM recette WHERE difficulte = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, difficulte);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur countByDifficulte: " + e.getMessage());
        }
        return 0;
    }

    public int countByDifficulteAndUserId(String difficulte, int userId) {
        String sql = "SELECT COUNT(*) FROM recette WHERE difficulte = ? AND user_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, difficulte);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur countByDifficulteAndUserId: " + e.getMessage());
        }
        return 0;
    }

    public int countTotal() {
        String sql = "SELECT COUNT(*) FROM recette";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur countTotal: " + e.getMessage());
        }
        return 0;
    }

    public int countTotalByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM recette WHERE user_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur countTotalByUserId: " + e.getMessage());
        }
        return 0;
    }

    public int countTotalWithIngredients() {
        String sql = "SELECT COUNT(DISTINCT r.id) FROM recette r JOIN recette_ingredient ri ON r.id = ri.recette_id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur countTotalWithIngredients: " + e.getMessage());
        }
        return 0;
    }

    private Recette mapResultSetToRecette(ResultSet rs) throws SQLException {
        Recette r = new Recette();
        r.setId(rs.getInt("id"));
        r.setNom(rs.getString("nom"));
        r.setType(rs.getString("type"));
        r.setDifficulte(rs.getString("difficulte"));
        r.setTempsPreparation(rs.getInt("temps_preparation"));
        r.setDescription(rs.getString("description"));
        r.setImage(rs.getString("image"));
        r.setUserId(rs.getInt("user_id"));

        Timestamp ts = rs.getTimestamp("created_at");
        r.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());

        String etapesStr = rs.getString("etapes");
        List<String> etapesList = new ArrayList<>();
        if (etapesStr != null && !etapesStr.isEmpty()) {
            if (etapesStr.startsWith("[") && etapesStr.endsWith("]")) {
                // Handle JSON format ["a","b"]
                String content = etapesStr.substring(1, etapesStr.length() - 1);
                if (!content.isEmpty()) {
                    // Split by "," but be careful with quotes
                    String[] parts = content.split("\",\"");
                    for (String p : parts) {
                        etapesList.add(p.replace("\"", "").replace("\\\"", "\"").trim());
                    }
                }
            } else {
                // Handle old || format
                etapesList.addAll(Arrays.asList(etapesStr.split("\\|\\|")));
            }
        }
        r.setEtapes(etapesList);

        return r;
    }
    public int countByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM recette WHERE user_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("countByUser error: " + e.getMessage());
        }
        return 0;
    }
}
