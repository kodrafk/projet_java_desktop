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

public class ObjectiveEditSettingsController {
    @FXML private Label lblPlanTag;
    @FXML private DatePicker startDatePicker;
    @FXML private CheckBox autoActivateCheck;
    @FXML private Label errDate;

    private NutritionObjective objective;
    private NutritionObjectiveService service = new NutritionObjectiveService();

    public void setObjective(NutritionObjective obj) {
        this.objective = obj;
        lblPlanTag.setText(obj.getGoalLabel() + " · " + obj.getPlanLabel() + " · " + obj.getTargetCalories() + " cal/day");
        startDatePicker.setValue(obj.getPlannedStartDate() != null ? obj.getPlannedStartDate() : LocalDate.now());
        autoActivateCheck.setSelected(obj.isAutoActivate());
        startDatePicker.valueProperty().addListener((o, old, n) -> {
            errDate.setVisible(false); errDate.setManaged(false);
        });
    }

    @FXML private void setToday()      { startDatePicker.setValue(LocalDate.now()); }
    @FXML private void setTomorrow()   { startDatePicker.setValue(LocalDate.now().plusDays(1)); }
    @FXML private void setNextMonday() { startDatePicker.setValue(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))); }
    @FXML private void setNextWeek()   { startDatePicker.setValue(LocalDate.now().plusWeeks(1)); }

    @FXML private void handleSave() {
        LocalDate date = startDatePicker.getValue();
        if (date == null) {
            errDate.setText("⚠ Please select a start date.");
            errDate.setVisible(true); errDate.setManaged(true); return;
        }
        if (date.isBefore(LocalDate.now())) {
            errDate.setText("⚠ Start date must be today or in the future.");
            errDate.setVisible(true); errDate.setManaged(true); return;
        }
        errDate.setVisible(false); errDate.setManaged(false);
        objective.setPlannedStartDate(date);
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
