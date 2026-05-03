package tn.esprit.projet.services;

import tn.esprit.projet.models.MealPlan;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;

public class MealPlanService {

    private final Connection cnx;

    public MealPlanService() {
        this.cnx = MyBDConnexion.getInstance().getCnx();
    }

    public MealPlan getPlanActif(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement(
                "SELECT * FROM meal_plan WHERE user_id = ? AND is_active = 1 ORDER BY id DESC LIMIT 1")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                MealPlan plan = new MealPlan();
                plan.setId(rs.getInt("id"));
                plan.setUserId(rs.getInt("user_id"));
                plan.setActive(true);
                return plan;
            }
        } catch (SQLException e) {
            System.err.println("MealPlanService.getPlanActif: " + e.getMessage());
        }
        return null;
    }
}
