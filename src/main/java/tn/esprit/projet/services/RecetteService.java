package tn.esprit.projet.services;

import tn.esprit.projet.models.Recette;
import tn.esprit.projet.models.RecetteIngredient;
import tn.esprit.projet.utils.MyBDConnexion;
import tn.esprit.projet.utils.SessionManager;

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
        ensureTablesExist();
    }

    private void ensureTablesExist() {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `recette` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `nom` VARCHAR(255) NOT NULL," +
                "  `type` VARCHAR(50)," +
                "  `difficulte` VARCHAR(50)," +
                "  `temps_preparation` INT DEFAULT 0," +
                "  `portions` INT DEFAULT 4," +
                "  `description` TEXT," +
                "  `image` VARCHAR(500)," +
                "  `user_id` INT," +
                "  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "  `etapes` TEXT" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS `recette_ingredient` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `recette_id` INT NOT NULL," +
                "  `ingredient_id` INT NOT NULL," +
                "  `quantite` VARCHAR(100)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
        } catch (SQLException e) {
            System.err.println("RecetteService table setup: " + e.getMessage());
        }
    }

    @Override
    public void ajouter(Recette recette) {
        int userId = recette.getUserId() > 0 ? recette.getUserId() :
                     (SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 1);

        String sql = "INSERT INTO recette (nom, type, difficulte, temps_preparation, portions, description, image, user_id, created_at, etapes) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, recette.getNom()); pst.setString(2, recette.getType());
            pst.setString(3, recette.getDifficulte()); pst.setInt(4, recette.getTempsPreparation());
            pst.setInt(5, recette.getPortions()); pst.setString(6, recette.getDescription());
            pst.setString(7, recette.getImage()); pst.setInt(8, userId);
            pst.setTimestamp(9, Timestamp.valueOf(recette.getCreatedAt() != null ? recette.getCreatedAt() : LocalDateTime.now()));
            pst.setString(10, etapesToJson(recette.getEtapes()));
            pst.executeUpdate();
            ResultSet keys = pst.getGeneratedKeys();
            if (keys.next()) recette.setId(keys.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void modifier(Recette recette) {
        String sql = "UPDATE recette SET nom=?,type=?,difficulte=?,temps_preparation=?,portions=?,description=?,image=?,etapes=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, recette.getNom()); pst.setString(2, recette.getType());
            pst.setString(3, recette.getDifficulte()); pst.setInt(4, recette.getTempsPreparation());
            pst.setInt(5, recette.getPortions()); pst.setString(6, recette.getDescription());
            pst.setString(7, recette.getImage()); pst.setString(8, etapesToJson(recette.getEtapes()));
            pst.setInt(9, recette.getId()); pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void supprimer(int id) {
        try (PreparedStatement p1 = cnx.prepareStatement("DELETE FROM recette_ingredient WHERE recette_id=?");
             PreparedStatement p2 = cnx.prepareStatement("DELETE FROM recette WHERE id=?")) {
            p1.setInt(1, id); p1.executeUpdate();
            p2.setInt(1, id); p2.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public Recette getById(int id) {
        try (PreparedStatement pst = cnx.prepareStatement("SELECT * FROM recette WHERE id=?")) {
            pst.setInt(1, id); ResultSet rs = pst.executeQuery();
            if (rs.next()) { Recette r = map(rs); r.setRecetteIngredients(getIngredients(id)); return r; }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Recette> getAll() {
        List<Recette> list = new ArrayList<>();
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM recette ORDER BY created_at DESC")) {
            while (rs.next()) { Recette r = map(rs); r.setRecetteIngredients(getIngredients(r.getId())); list.add(r); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countTotal() {
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM recette")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countByUser(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM recette WHERE user_id=?")) {
            ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void addIngredientToRecette(int recetteId, int ingredientId, String quantite) {
        try (PreparedStatement pst = cnx.prepareStatement("INSERT INTO recette_ingredient (recette_id, ingredient_id, quantite) VALUES (?,?,?)")) {
            pst.setInt(1, recetteId); pst.setInt(2, ingredientId); pst.setString(3, quantite); pst.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private List<RecetteIngredient> getIngredients(int recetteId) {
        List<RecetteIngredient> list = new ArrayList<>();
        String sql = "SELECT ri.*, i.nom as ingredient_nom FROM recette_ingredient ri JOIN ingredient i ON ri.ingredient_id=i.id WHERE ri.recette_id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, recetteId); ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                RecetteIngredient ri = new RecetteIngredient();
                ri.setId(rs.getInt("id")); ri.setRecetteId(rs.getInt("recette_id"));
                ri.setIngredientId(rs.getInt("ingredient_id")); ri.setQuantite(rs.getString("quantite"));
                ri.setIngredientNom(rs.getString("ingredient_nom")); list.add(ri);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Recette map(ResultSet rs) throws SQLException {
        Recette r = new Recette();
        r.setId(rs.getInt("id")); r.setNom(rs.getString("nom")); r.setType(rs.getString("type"));
        r.setDifficulte(rs.getString("difficulte")); r.setTempsPreparation(rs.getInt("temps_preparation"));
        try { r.setPortions(rs.getInt("portions")); } catch (SQLException ignored) {}
        r.setDescription(rs.getString("description")); r.setImage(rs.getString("image"));
        r.setUserId(rs.getInt("user_id"));
        Timestamp ts = rs.getTimestamp("created_at");
        r.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        String etapesStr = rs.getString("etapes");
        List<String> etapes = new ArrayList<>();
        if (etapesStr != null && !etapesStr.isEmpty()) {
            if (etapesStr.startsWith("[")) {
                String content = etapesStr.substring(1, etapesStr.length() - 1);
                if (!content.isEmpty()) {
                    for (String p : content.split("\",\""))
                        etapes.add(p.replace("\"", "").trim());
                }
            } else { etapes.addAll(Arrays.asList(etapesStr.split("\\|\\|"))); }
        }
        r.setEtapes(etapes);
        return r;
    }

    private String etapesToJson(List<String> etapes) {
        if (etapes == null || etapes.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < etapes.size(); i++) {
            sb.append("\"").append(etapes.get(i).replace("\"", "\\\"")).append("\"");
            if (i < etapes.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }

    public int countByType(String type) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM recette WHERE type=?")) {
            ps.setString(1, type); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
