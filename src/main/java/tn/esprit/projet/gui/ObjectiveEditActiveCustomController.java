package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.NutritionObjectiveService;

public class ObjectiveEditActiveCustomController {
    @FXML private TextField titleField, caloriesField, proteinField, carbsField, fatsField, waterField;
    @FXML private TextArea descriptionField;
    @FXML private Label lblError;

    private NutritionObjective objective;
    private NutritionObjectiveService service = new NutritionObjectiveService();

    public void setObjective(NutritionObjective obj) {
        this.objective = obj;
        titleField.setText(obj.getTitle());
        descriptionField.setText(obj.getDescription() != null ? obj.getDescription() : "");
        caloriesField.setText(String.valueOf(obj.getTargetCalories()));
        proteinField.setText(String.valueOf((int) obj.getTargetProtein()));
        carbsField.setText(String.valueOf((int) obj.getTargetCarbs()));
        fatsField.setText(String.valueOf((int) obj.getTargetFats()));
        waterField.setText(String.valueOf(obj.getTargetWater()));
    }

    @FXML private void handleSave() {
        try {
            objective.setTitle(titleField.getText());
            objective.setDescription(descriptionField.getText());
            objective.setTargetCalories(Integer.parseInt(caloriesField.getText().trim()));
            objective.setTargetProtein(Double.parseDouble(proteinField.getText().trim()));
            objective.setTargetCarbs(Double.parseDouble(carbsField.getText().trim()));
            objective.setTargetFats(Double.parseDouble(fatsField.getText().trim()));
            if (!waterField.getText().isBlank()) objective.setTargetWater(Double.parseDouble(waterField.getText().trim()));
            service.update(objective);
            navigateTo("/fxml/objectives.fxml");
        } catch (NumberFormatException e) {
            lblError.setText("Please enter valid numbers.");
            lblError.setVisible(true); lblError.setManaged(true);
        }
    }

    @FXML private void handleBack() { navigateTo("/fxml/objective_edit_active.fxml"); }

    private void navigateTo(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentArea = (StackPane) titleField.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
