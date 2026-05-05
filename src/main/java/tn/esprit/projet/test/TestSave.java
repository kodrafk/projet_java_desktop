package tn.esprit.projet.test;

import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.NutritionObjectiveService;
import tn.esprit.projet.utils.MyBDConnexion;

import java.sql.*;
import java.time.LocalDate;

public class TestSave {
    public static void main(String[] args) throws Exception {
        Connection cnx = MyBDConnexion.getInstance().getCnx();

        // Check user table
        System.out.println("=== Users ===");
        try (ResultSet rs = cnx.createStatement().executeQuery("SELECT id FROM user LIMIT 3")) {
            while (rs.next()) System.out.println("user id=" + rs.getInt(1));
        } catch (Exception e) { System.out.println("user error: " + e.getMessage()); }

        // Try saving an objective
        NutritionObjective obj = new NutritionObjective();
        obj.setTitle("Test Objective");
        obj.setGoalType("maintain");
        obj.setPlanLevel("moderate");
        obj.setTargetCalories(2000);
        obj.setTargetProtein(150);
        obj.setTargetCarbs(250);
        obj.setTargetFats(65);
        obj.setTargetWater(2.5);
        obj.setPlannedStartDate(LocalDate.now());

        new NutritionObjectiveService().save(obj);
        System.out.println("Result ID: " + obj.getId());
    }
}
