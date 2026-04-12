package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import tn.esprit.projet.models.NutritionObjective;

public class ObjectiveEditController {

    @FXML private Label lblHeaderTitle, lblCurrentTitle, lblCurrentCal, lblCurrentProtein, lblCurrentCarbs, lblCurrentFats;
    @FXML private VBox goalsSection, plansSection;
    @FXML private GridPane goalsGrid;
    @FXML private HBox plansContainer;
    @FXML private Label lblSelectedGoal;

    private NutritionObjective objective;

    private static final String[][] GOALS = {
        {"lose_weight",  "Lose Weight",   "Burn fat and reduce body weight"},
        {"gain_weight",  "Gain Weight",   "Increase mass with healthy calories"},
        {"maintain",     "Maintain",      "Keep your current weight stable"},
        {"build_muscle", "Build Muscle",  "High protein for muscle growth"},
        {"clean_eating", "Clean Eating",  "Whole foods, minimal processing"},
        {"custom",       "Custom",        "Set your own macros manually"},
    };

    // calories, protein, carbs, fats, water
    private static final java.util.Map<String, int[][]> PLANS = java.util.Map.of(
        "lose_weight",  new int[][]{{1400,120,140,50,2},{1600,140,160,55,2},{1800,160,180,60,3}},
        "gain_weight",  new int[][]{{2200,160,280,70,3},{2600,180,320,80,3},{3000,200,380,90,4}},
        "maintain",     new int[][]{{1800,130,220,60,2},{2000,150,250,65,2},{2200,170,270,70,3}},
        "build_muscle", new int[][]{{2000,180,200,65,3},{2400,200,240,75,3},{2800,220,280,85,4}},
        "clean_eating", new int[][]{{1600,120,180,55,2},{1900,140,220,65,2},{2200,160,260,70,3}}
    );
    private static final String[] LEVELS = {"light", "moderate", "intense"};
    private static final String[] LEVEL_LABELS = {"Light", "Moderate", "Intense"};

    public void setObjective(NutritionObjective obj) {
        this.objective = obj;
        lblHeaderTitle.setText(obj.getTitle());
        lblCurrentTitle.setText(obj.getTitle());
        lblCurrentCal.setText("🔥 " + obj.getTargetCalories() + " kcal");
        lblCurrentProtein.setText("💪 " + (int) obj.getTargetProtein() + "g");
        lblCurrentCarbs.setText("🌾 " + (int) obj.getTargetCarbs() + "g");
        lblCurrentFats.setText("🥑 " + (int) obj.getTargetFats() + "g");
    }

    @FXML
    private void handleChangeGoal() {
        goalsSection.setVisible(true);
        goalsSection.setManaged(true);
        buildGoalsGrid();
    }

    private void buildGoalsGrid() {
        goalsGrid.getChildren().clear();
        int col = 0, row = 0;
        for (String[] g : GOALS) {
            String key = g[0], label = g[1], desc = g[2];
            boolean isCurrent = key.equals(objective.getGoalType());

            VBox card = new VBox(8);
            card.setPrefWidth(190);
            String border = isCurrent ? "-fx-border-color: #2E7D5A; -fx-border-width: 2;" : "-fx-border-color: #E2E8F0;";
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 18; "
                    + border + " -fx-border-radius: 12; -fx-cursor: hand;");

            if (isCurrent) {
                Label cur = new Label("✓ Current");
                cur.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 10px;");
                card.getChildren().add(cur);
            }

            Label lbl = new Label(label);
            lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            Label dsc = new Label(desc);
            dsc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B; -fx-wrap-text: true;");
            card.getChildren().addAll(lbl, dsc);

            card.setOnMouseClicked(e -> {
                if ("custom".equals(key)) {
                    navigateWithObjective("/fxml/objective_edit_custom.fxml");
                } else {
                    showPlans(key, label);
                }
            });

            goalsGrid.add(card, col, row);
            col++;
            if (col > 2) { col = 0; row++; }
        }
    }

    private void showPlans(String goalKey, String goalLabel) {
        plansSection.setVisible(true);
        plansSection.setManaged(true);
        lblSelectedGoal.setText(goalLabel + " — Choose intensity");
        plansContainer.getChildren().clear();

        int[][] plans = PLANS.getOrDefault(goalKey, PLANS.get("maintain"));
        for (int i = 0; i < 3; i++) {
            int[] p = plans[i];
            String level = LEVELS[i];
            String levelLabel = LEVEL_LABELS[i];
            boolean featured = i == 1;

            VBox card = new VBox(10);
            card.setPrefWidth(200);
            String border = featured ? "-fx-border-color: #2E7D5A; -fx-border-width: 2;" : "-fx-border-color: #E2E8F0;";
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20; "
                    + border + " -fx-border-radius: 12; -fx-cursor: hand;");

            if (featured) {
                Label pop = new Label("★ Most Popular");
                pop.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 3 8; -fx-font-size: 10px;");
                card.getChildren().add(pop);
            }

            Label name = new Label(levelLabel);
            name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            Label cal = new Label(p[0] + " cal/day");
            cal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2E7D5A;");
            Label macros = new Label("P: " + p[1] + "g  C: " + p[2] + "g  F: " + p[3] + "g");
            macros.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
            Label btn = new Label("Apply This Plan →");
            btn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2E7D5A;");

            card.getChildren().addAll(name, cal, macros, btn);

            final int[] fp = p;
            card.setOnMouseClicked(e -> goToConfirm(goalKey, goalLabel, level, levelLabel, fp));
            plansContainer.getChildren().add(card);
        }
    }

    private void goToConfirm(String goalKey, String goalLabel, String level, String levelLabel, int[] plan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/objective_edit_confirm.fxml"));
            Parent page = loader.load();
            ObjectiveEditConfirmController ctrl = loader.getController();
            ctrl.setData(objective, goalKey, level, goalLabel + " — " + levelLabel, plan);
            StackPane contentArea = (StackPane) lblHeaderTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleChangeSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/objective_edit_settings.fxml"));
            Parent page = loader.load();
            ObjectiveEditSettingsController ctrl = loader.getController();
            ctrl.setObjective(objective);
            StackPane contentArea = (StackPane) lblHeaderTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleCustomPlan() { navigateWithObjective("/fxml/objective_edit_custom.fxml"); }

    @FXML private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/objective_show.fxml"));
            Parent page = loader.load();
            ObjectiveShowController ctrl = loader.getController();
            ctrl.setObjective(objective);
            StackPane contentArea = (StackPane) lblHeaderTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void navigateWithObjective(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent page = loader.load();
            ObjectiveEditCustomController ctrl = loader.getController();
            ctrl.setObjective(objective);
            StackPane contentArea = (StackPane) lblHeaderTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
