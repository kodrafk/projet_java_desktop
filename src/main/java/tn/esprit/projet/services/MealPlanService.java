package tn.esprit.projet.services;

import tn.esprit.projet.models.MealPlan;
import tn.esprit.projet.models.MealPlanItem;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MealPlanService {

    private final Connection cnx;

    public MealPlanService() {
        cnx = MyBDConnexion.getInstance().getCnx();
    }

    public int sauvegarderPlan(MealPlan plan, List<MealPlanItem> items) {
        int planId = -1;

        String sqlPlan =
                "INSERT INTO meal_plan " +
                        "(user_id, date_creation, objectif, regime, " +
                        " allergie_lactose, allergie_gluten, allergie_nuts, allergie_eggs) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(
                sqlPlan, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt      (1, plan.getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(
                    plan.getDateCreation() != null
                            ? plan.getDateCreation()
                            : LocalDateTime.now()));
            ps.setString   (3, plan.getObjectif().name());
            ps.setString   (4, plan.getRegime().name());
            ps.setInt      (5, plan.isAllergieLactose() ? 1 : 0);
            ps.setInt      (6, plan.isAllergieGluten()  ? 1 : 0);
            ps.setInt      (7, plan.isAllergieNuts()    ? 1 : 0);
            ps.setInt      (8, plan.isAllergieEggs()    ? 1 : 0);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                planId = rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ sauvegarderPlan : " + e.getMessage());
            return -1;
        }

        if (planId != -1) {
            sauvegarderItems(planId, items);
        }
        return planId;
    }

    private void sauvegarderItems(int planId, List<MealPlanItem> items) {
        String sqlItem =
                "INSERT INTO meal_plan_item " +
                        "(meal_plan_id, jour_nom, moment_repas, recette_id, urgence_niveau, is_eaten) " +
                        "VALUES (?, ?, ?, ?, ?, 0)";

        try (PreparedStatement ps = cnx.prepareStatement(sqlItem)) {
            for (MealPlanItem item : items) {
                ps.setInt   (1, planId);
                ps.setString(2, item.getJourNom().name());
                ps.setString(3, item.getMomentRepas().name());
                ps.setInt   (4, item.getRecetteId());
                ps.setString(5, item.getUrgenceNiveau().name());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("❌ sauvegarderItems : " + e.getMessage());
        }
    }

    public MealPlan getPlanActif(int userId) {
        String sql =
                "SELECT * FROM meal_plan " +
                        "WHERE user_id = ? " +
                        "ORDER BY date_creation DESC LIMIT 1";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                MealPlan plan = new MealPlan();
                plan.setId              (rs.getInt("id"));
                plan.setUserId          (rs.getInt("user_id"));
                plan.setDateCreation    (rs.getTimestamp("date_creation").toLocalDateTime());
                plan.setObjectif        (MealPlan.Objectif.valueOf(rs.getString("objectif")));
                plan.setRegime          (MealPlan.Regime.valueOf(rs.getString("regime")));
                plan.setAllergieLactose (rs.getInt("allergie_lactose") == 1);
                plan.setAllergieGluten  (rs.getInt("allergie_gluten")  == 1);
                plan.setAllergieNuts    (rs.getInt("allergie_nuts")    == 1);
                plan.setAllergieEggs    (rs.getInt("allergie_eggs")    == 1);

                List<MealPlanItem> items = getItemsByPlanId(plan.getId());
                plan.setItems(items);
                return plan;
            }
        } catch (SQLException e) {
            System.err.println("❌ getPlanActif : " + e.getMessage());
        }
        return null;
    }

    // ✅ CORRIGÉ : inclut image de la recette
    public List<MealPlanItem> getItemsByPlanId(int planId) {
        List<MealPlanItem> items = new ArrayList<>();

        String sql =
                "SELECT mpi.*, r.nom AS recette_nom, r.image AS recette_image, rip.calories " +
                        "FROM meal_plan_item mpi " +
                        "JOIN recette r ON mpi.recette_id = r.id " +
                        "JOIN recette_info_plus rip ON mpi.recette_id = rip.recette_id " +
                        "WHERE mpi.meal_plan_id = ? " +
                        "ORDER BY " +
                        "FIELD(mpi.jour_nom, 'Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi','Dimanche'), " +
                        "FIELD(mpi.moment_repas, 'PETIT_DEJEUNER','DEJEUNER','DINER')";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MealPlanItem item = new MealPlanItem();
                item.setId             (rs.getInt    ("id"));
                item.setMealPlanId     (rs.getInt    ("meal_plan_id"));
                item.setJourNom        (MealPlanItem.JourNom.valueOf(rs.getString("jour_nom")));
                item.setMomentRepas    (MealPlanItem.MomentRepas.valueOf(rs.getString("moment_repas")));
                item.setRecetteId      (rs.getInt    ("recette_id"));
                item.setRecetteNom     (rs.getString ("recette_nom"));
                item.setRecetteCalories(rs.getInt    ("calories"));
                item.setRecetteImage   (rs.getString ("recette_image"));  // 🆕
                item.setUrgenceNiveau  (MealPlanItem.UrgenceNiveau.valueOf(rs.getString("urgence_niveau")));
                item.setEaten          (rs.getInt    ("is_eaten") == 1);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("❌ getItemsByPlanId : " + e.getMessage());
        }
        return items;
    }

    public void updateStatutRepas(int itemId, boolean isEaten) {
        String sql = "UPDATE meal_plan_item SET is_eaten = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, isEaten ? 1 : 0);
            ps.setInt(2, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ updateStatutRepas : " + e.getMessage());
        }
    }

    public int getNbRepasEffectues(int planId) {
        String sql = "SELECT COUNT(*) FROM meal_plan_item WHERE meal_plan_id = ? AND is_eaten = 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ getNbRepasEffectues : " + e.getMessage());
        }
        return 0;
    }

    public int getNbIngredientsUrgentsSauves(int planId) {
        String sql = "SELECT COUNT(*) FROM meal_plan_item WHERE meal_plan_id = ? AND urgence_niveau = 'URGENT' AND is_eaten = 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, planId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ getNbIngredientsUrgentsSauves : " + e.getMessage());
        }
        return 0;
    }
}