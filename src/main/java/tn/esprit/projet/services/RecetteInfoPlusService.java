package tn.esprit.projet.services;

import tn.esprit.projet.models.RecetteInfoPlus;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecetteInfoPlusService {

    private final Connection cnx;

    public RecetteInfoPlusService() {
        cnx = MyBDConnexion.getInstance().getCnx();
    }

    // ─── Mapper ResultSet → RecetteInfoPlus ───────────────────
    private RecetteInfoPlus mapResultSet(ResultSet rs) throws SQLException {
        RecetteInfoPlus rip = new RecetteInfoPlus();
        rip.setRecetteId      (rs.getInt    ("recette_id"));
        rip.setCalories       (rs.getInt    ("calories"));
        rip.setProteines      (rs.getFloat  ("proteines"));
        rip.setLipides        (rs.getFloat  ("lipides"));
        rip.setGlucides       (rs.getFloat  ("glucides"));
        rip.setMomentRepas    (RecetteInfoPlus.MomentRepas
                .valueOf(rs.getString("moment_repas")));
        rip.setVegetarien     (rs.getInt("is_vegetarien")    == 1);
        rip.setVegan          (rs.getInt("is_vegan")         == 1);
        rip.setHalal          (rs.getInt("is_halal")         == 1);
        rip.setContainsGluten (rs.getInt("contains_gluten")  == 1);
        rip.setContainsLactose(rs.getInt("contains_lactose") == 1);
        rip.setContainsNuts   (rs.getInt("contains_nuts")    == 1);
        rip.setContainsEggs   (rs.getInt("contains_eggs")    == 1);
        return rip;
    }

    // ─── GET par recette_id ───────────────────────────────────
    public RecetteInfoPlus getByRecetteId(int recetteId) {
        String sql = "SELECT * FROM recette_info_plus WHERE recette_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, recetteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ getByRecetteId : " + e.getMessage());
        }
        return null;
    }

    // ─── GET par moment repas ─────────────────────────────────
    public List<RecetteInfoPlus> getByMoment(RecetteInfoPlus.MomentRepas moment) {
        List<RecetteInfoPlus> liste = new ArrayList<>();
        String sql = "SELECT * FROM recette_info_plus WHERE moment_repas = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, moment.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ getByMoment : " + e.getMessage());
        }
        return liste;
    }

    // ─── GET ALL ──────────────────────────────────────────────
    public List<RecetteInfoPlus> getAll() {
        List<RecetteInfoPlus> liste = new ArrayList<>();
        String sql = "SELECT * FROM recette_info_plus";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ getAll : " + e.getMessage());
        }
        return liste;
    }

    // ─── AJOUTER ──────────────────────────────────────────────
    public void ajouter(RecetteInfoPlus rip) {
        String sql = """
                INSERT INTO recette_info_plus
                (recette_id, calories, proteines, lipides, glucides,
                 moment_repas, is_vegetarien, is_vegan, is_halal,
                 contains_gluten, contains_lactose, contains_nuts, contains_eggs)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt   (1,  rip.getRecetteId());
            ps.setInt   (2,  rip.getCalories());
            ps.setFloat (3,  rip.getProteines());
            ps.setFloat (4,  rip.getLipides());
            ps.setFloat (5,  rip.getGlucides());
            ps.setString(6,  rip.getMomentRepas().name());
            ps.setInt   (7,  rip.isVegetarien()     ? 1 : 0);
            ps.setInt   (8,  rip.isVegan()           ? 1 : 0);
            ps.setInt   (9,  rip.isHalal()           ? 1 : 0);
            ps.setInt   (10, rip.isContainsGluten()  ? 1 : 0);
            ps.setInt   (11, rip.isContainsLactose() ? 1 : 0);
            ps.setInt   (12, rip.isContainsNuts()    ? 1 : 0);
            ps.setInt   (13, rip.isContainsEggs()    ? 1 : 0);
            ps.executeUpdate();
            System.out.println("✅ RecetteInfoPlus ajoutée pour recette_id=" + rip.getRecetteId());
        } catch (SQLException e) {
            System.err.println("❌ ajouter : " + e.getMessage());
        }
    }
}