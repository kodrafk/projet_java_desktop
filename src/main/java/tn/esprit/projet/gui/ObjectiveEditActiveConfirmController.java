package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.NutritionObjectiveService;

public class ObjectiveEditActiveConfirmController {
    @FXML private Label lblOldTitle, lblOldCal, lblOldProtein, lblOldCarbs, lblOldFats, lblOldWater;
    @FXML private Label lblNewTitle, lblNewCal, lblNewProtein, lblNewCarbs, lblNewFats, lblNewWater;
    @FXML private HBox timelineContainer;

    private NutritionObjective objective;
    private int[] newPlan;
    private String newGoalType, newPlanLevel, newTitle;
    private NutritionObjectiveService service = new NutritionObjectiveService();

    public void setData(NutritionObjective obj, String goalType, String planLevel, String title, int[] plan) {
        this.objective = obj; this.newGoalType = goalType; this.newPlanLevel = planLevel;
        this.newTitle = title; this.newPlan = plan;
        lblOldTitle.setText(obj.getTitle());
        lblOldCal.setText(obj.getTargetCalories() + " kcal");
        lblOldProtein.setText((int)obj.getTargetProtein() + "g");
        lblOldCarbs.setText((int)obj.getTargetCarbs() + "g");
        lblOldFats.setText((int)obj.getTargetFats() + "g");
        lblOldWater.setText(obj.getTargetWater() + "L");
        lblNewTitle.setText(title);
        lblNewCal.setText(plan[0] + " kcal");
        lblNewProtein.setText(plan[1] + "g");
        lblNewCarbs.setText(plan[2] + "g");
        lblNewFats.setText(plan[3] + "g");
        lblNewWater.setText(plan[4] + "L");
    }

    @FXML private void handleApply() {
        objective.setGoalType(newGoalType); objective.setPlanLevel(newPlanLevel);
        objective.setTitle(newTitle);
        objective.setTargetCalories(newPlan[0]); objective.setTargetProtein(newPlan[1]);
        objective.setTargetCarbs(newPlan[2]); objective.setTargetFats(newPlan[3]);
        objective.setTargetWater(newPlan[4]);
        service.update(objective);
        navigateTo("/fxml/objectives.fxml");
    }

    @FXML private void handleBack() { navigateTo("/fxml/objective_edit_active.fxml"); }

    private void navigateTo(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentArea = (StackPane) lblOldTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
