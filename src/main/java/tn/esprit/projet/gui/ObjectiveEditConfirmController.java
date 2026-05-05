package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.NutritionObjectiveService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class ObjectiveEditConfirmController {
    @FXML private Label lblOldTitle, lblOldCal, lblOldProtein, lblOldCarbs, lblOldFats, lblOldWater;
    @FXML private Label lblNewTitle, lblNewCal, lblNewProtein, lblNewCarbs, lblNewFats, lblNewWater;
    @FXML private DatePicker startDatePicker;
    @FXML private CheckBox autoActivateCheck;

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
        startDatePicker.setValue(obj.getPlannedStartDate() != null ? obj.getPlannedStartDate() : LocalDate.now());
        autoActivateCheck.setSelected(obj.isAutoActivate());
    }

    @FXML private void setToday()      { startDatePicker.setValue(LocalDate.now()); }
    @FXML private void setTomorrow()   { startDatePicker.setValue(LocalDate.now().plusDays(1)); }
    @FXML private void setNextMonday() { startDatePicker.setValue(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))); }

    @FXML private void handleApply() {
        objective.setGoalType(newGoalType); objective.setPlanLevel(newPlanLevel);
        objective.setTitle(newTitle);
        objective.setTargetCalories(newPlan[0]); objective.setTargetProtein(newPlan[1]);
        objective.setTargetCarbs(newPlan[2]); objective.setTargetFats(newPlan[3]);
        objective.setTargetWater(newPlan[4]);
        objective.setPlannedStartDate(startDatePicker.getValue());
        objective.setAutoActivate(autoActivateCheck.isSelected());
        service.update(objective);
        navigateTo("/fxml/objectives.fxml");
    }

    @FXML private void handleBack() { navigateTo("/fxml/objective_edit.fxml"); }

    private void navigateTo(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentArea = (StackPane) startDatePicker.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
