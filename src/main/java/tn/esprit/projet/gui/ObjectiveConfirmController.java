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

public class ObjectiveConfirmController {

    @FXML private Label lblSummaryTitle, lblSummarySub;
    @FXML private Label lblSumCal, lblSumProtein, lblSumCarbs, lblSumFats, lblSumWater;
    @FXML private DatePicker startDatePicker;
    @FXML private CheckBox autoActivateCheck;

    private String goalType, goalLabel, planLevel, planLevelLabel;
    private int[] plan;

    public void setPlan(String goalType, String goalLabel, String planLevel, String planLevelLabel, int[] plan) {
        this.goalType = goalType; this.goalLabel = goalLabel;
        this.planLevel = planLevel; this.planLevelLabel = planLevelLabel;
        this.plan = plan;
        lblSummaryTitle.setText(goalLabel + " — " + planLevelLabel);
        lblSummarySub.setText("Your personalized 7-day nutrition plan");
        lblSumCal.setText(String.valueOf(plan[0]));
        lblSumProtein.setText(plan[1] + "g");
        lblSumCarbs.setText(plan[2] + "g");
        lblSumFats.setText(plan[3] + "g");
        lblSumWater.setText(plan[4] + "L");
        startDatePicker.setValue(LocalDate.now());
    }

    @FXML private void setToday()      { startDatePicker.setValue(LocalDate.now()); }
    @FXML private void setTomorrow()   { startDatePicker.setValue(LocalDate.now().plusDays(1)); }
    @FXML private void setNextMonday() { startDatePicker.setValue(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))); }
    @FXML private void setNextWeek()   { startDatePicker.setValue(LocalDate.now().plusWeeks(1)); }

    @FXML
    private void handleCreate() {
        if (startDatePicker.getValue() == null) return;

        NutritionObjective obj = new NutritionObjective();
        obj.setTitle(goalLabel + " — " + planLevelLabel);
        obj.setGoalType(goalType);
        obj.setPlanLevel(planLevel);
        obj.setTargetCalories(plan[0]);
        obj.setTargetProtein(plan[1]);
        obj.setTargetCarbs(plan[2]);
        obj.setTargetFats(plan[3]);
        obj.setTargetWater(plan[4]);
        obj.setPlannedStartDate(startDatePicker.getValue());
        obj.setAutoActivate(autoActivateCheck.isSelected());
        obj.setStatus("pending");

        try {
            new NutritionObjectiveService().save(obj);
            navigateTo("/fxml/objectives.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                "Could not save objective: " + e.getMessage(),
                javafx.scene.control.ButtonType.OK
            ).showAndWait();
        }
    }

    @FXML private void handleBack() { navigateTo("/fxml/objective_choose_plan.fxml"); }

    private void navigateTo(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentArea = (StackPane) startDatePicker.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
