package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.NutritionObjectiveService;

public class ObjectiveEditCustomController {

    @FXML private TextField titleField, caloriesField, proteinField, carbsField, fatsField, waterField;
    @FXML private TextArea descriptionField;

    // Inline error labels
    @FXML private Label errTitle, errCalories, errProtein, errCarbs, errFats, errWater;

    private NutritionObjective objective;
    private final NutritionObjectiveService service = new NutritionObjectiveService();

    public void setObjective(NutritionObjective obj) {
        this.objective = obj;
        titleField.setText(obj.getTitle());
        descriptionField.setText(obj.getDescription() != null ? obj.getDescription() : "");
        caloriesField.setText(String.valueOf(obj.getTargetCalories()));
        proteinField.setText(String.valueOf((int) obj.getTargetProtein()));
        carbsField.setText(String.valueOf((int) obj.getTargetCarbs()));
        fatsField.setText(String.valueOf((int) obj.getTargetFats()));
        waterField.setText(obj.getTargetWater() > 0 ? String.valueOf(obj.getTargetWater()) : "");
    }

    @FXML
    public void initialize() {
        // Live clear on type
        titleField.textProperty().addListener((o, old, n)    -> clearErr(errTitle, titleField));
        caloriesField.textProperty().addListener((o, old, n) -> clearErr(errCalories, caloriesField));
        proteinField.textProperty().addListener((o, old, n)  -> clearErr(errProtein, proteinField));
        carbsField.textProperty().addListener((o, old, n)    -> clearErr(errCarbs, carbsField));
        fatsField.textProperty().addListener((o, old, n)     -> clearErr(errFats, fatsField));
        waterField.textProperty().addListener((o, old, n)    -> clearErr(errWater, waterField));
    }

    @FXML
    private void handleSave() {
        boolean valid = true;

        // Title
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showErr(errTitle, titleField, "Title is required.");
            valid = false;
        } else if (title.length() < 3) {
            showErr(errTitle, titleField, "Title must be at least 3 characters.");
            valid = false;
        } else if (title.length() > 100) {
            showErr(errTitle, titleField, "Title must not exceed 100 characters.");
            valid = false;
        }

        // Calories
        int calories = 0;
        String calStr = caloriesField.getText().trim();
        if (calStr.isEmpty()) {
            showErr(errCalories, caloriesField, "Calories is required.");
            valid = false;
        } else {
            try {
                calories = Integer.parseInt(calStr);
                if (calories < 500 || calories > 5000) {
                    showErr(errCalories, caloriesField, "Must be between 500 and 5000 kcal.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showErr(errCalories, caloriesField, "Enter a whole number (e.g. 2000).");
                valid = false;
            }
        }

        // Protein
        double protein = 0;
        String proStr = proteinField.getText().trim();
        if (proStr.isEmpty()) {
            showErr(errProtein, proteinField, "Protein is required.");
            valid = false;
        } else {
            try {
                protein = Double.parseDouble(proStr);
                if (protein < 10 || protein > 300) {
                    showErr(errProtein, proteinField, "Must be between 10 and 300 g.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showErr(errProtein, proteinField, "Enter a valid number (e.g. 150).");
                valid = false;
            }
        }

        // Carbs
        double carbs = 0;
        String carbStr = carbsField.getText().trim();
        if (carbStr.isEmpty()) {
            showErr(errCarbs, carbsField, "Carbs is required.");
            valid = false;
        } else {
            try {
                carbs = Double.parseDouble(carbStr);
                if (carbs < 20 || carbs > 500) {
                    showErr(errCarbs, carbsField, "Must be between 20 and 500 g.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showErr(errCarbs, carbsField, "Enter a valid number (e.g. 200).");
                valid = false;
            }
        }

        // Fats
        double fats = 0;
        String fatStr = fatsField.getText().trim();
        if (fatStr.isEmpty()) {
            showErr(errFats, fatsField, "Fats is required.");
            valid = false;
        } else {
            try {
                fats = Double.parseDouble(fatStr);
                if (fats < 10 || fats > 200) {
                    showErr(errFats, fatsField, "Must be between 10 and 200 g.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showErr(errFats, fatsField, "Enter a valid number (e.g. 65).");
                valid = false;
            }
        }

        // Water (optional)
        double water = 0;
        String waterStr = waterField.getText().trim();
        if (!waterStr.isEmpty()) {
            try {
                water = Double.parseDouble(waterStr);
                if (water < 0 || water > 10) {
                    showErr(errWater, waterField, "Must be between 0 and 10 L.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showErr(errWater, waterField, "Enter a valid number (e.g. 2.5).");
                valid = false;
            }
        }

        if (!valid) return;

        objective.setTitle(title);
        objective.setDescription(descriptionField.getText().trim());
        objective.setTargetCalories(calories);
        objective.setTargetProtein(protein);
        objective.setTargetCarbs(carbs);
        objective.setTargetFats(fats);
        objective.setTargetWater(water);
        service.update(objective);
        navigateTo("/fxml/objectives.fxml");
    }

    @FXML private void handleBack() { navigateTo("/fxml/objective_edit.fxml"); }

    private void showErr(Label lbl, TextField field, String msg) {
        lbl.setText("⚠ " + msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
        if (field != null)
            field.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #EF4444; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 0 12; -fx-font-size: 13px;");
    }

    private void clearErr(Label lbl, TextField field) {
        lbl.setText("");
        lbl.setVisible(false);
        lbl.setManaged(false);
        if (field != null)
            field.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 0 12; -fx-font-size: 13px;");
    }

    private void navigateTo(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentArea = (StackPane) titleField.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
