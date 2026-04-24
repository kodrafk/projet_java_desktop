package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.*;

public class ObjectiveChooseGoalController {
    @FXML private GridPane goalsGrid;

    private static final String[][] GOALS = {
        {"lose_weight",  "Lose Weight",   "Burn fat and reduce body weight"},
        {"gain_weight",  "Gain Weight",   "Increase mass with healthy calories"},
        {"maintain",     "Maintain",      "Keep your current weight stable"},
        {"build_muscle", "Build Muscle",  "High protein for muscle growth"},
        {"clean_eating", "Clean Eating",  "Whole foods, minimal processing"},
        {"custom",       "Custom",        "Set your own calories, protein, carbs and fats manually"},
    };

    @FXML
    public void initialize() {
        int col = 0, row = 0;
        for (String[] g : GOALS) {
            String key = g[0], label = g[1], desc = g[2];
            VBox card = new VBox(8);
            card.setPrefWidth(200);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 24; " +
                    "-fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-cursor: hand;");
            javafx.scene.control.Label lbl = new javafx.scene.control.Label(label);
            lbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            javafx.scene.control.Label dsc = new javafx.scene.control.Label(desc);
            dsc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-wrap-text: true;");
            card.getChildren().addAll(lbl, dsc);
            card.setOnMouseClicked(e -> {
                if ("custom".equals(key)) {
                    try {
                        Parent page = FXMLLoader.load(getClass().getResource("/fxml/objective_custom.fxml"));
                        StackPane contentArea = (StackPane) goalsGrid.getScene().lookup("#contentArea");
                        if (contentArea != null) contentArea.getChildren().setAll(page);
                    } catch (Exception ex) { ex.printStackTrace(); }
                } else {
                    goToPlans(key, label);
                }
            });
            goalsGrid.add(card, col, row);
            col++;
            if (col > 2) { col = 0; row++; }
        }
    }

    private void goToPlans(String goalType, String goalLabel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/objective_choose_plan.fxml"));
            Parent page = loader.load();
            ObjectiveChoosePlanController ctrl = loader.getController();
            ctrl.setGoal(goalType, goalLabel);
            StackPane contentArea = (StackPane) goalsGrid.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleBack() {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/fxml/objectives.fxml"));
            StackPane contentArea = (StackPane) goalsGrid.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
