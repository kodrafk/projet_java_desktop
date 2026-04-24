package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.Map;

public class ObjectiveChoosePlanController {

    @FXML private Label lblGoalChosen;
    @FXML private HBox plansContainer;

    private String goalType;

    // calories, protein, carbs, fats, water
    private static final Map<String, int[][]> PLANS = Map.of(
        "lose_weight",  new int[][]{{1400,120,140,50,2},{1600,140,160,55,2},{1800,160,180,60,3}},
        "gain_weight",  new int[][]{{2200,160,280,70,3},{2600,180,320,80,3},{3000,200,380,90,4}},
        "maintain",     new int[][]{{1800,130,220,60,2},{2000,150,250,65,2},{2200,170,270,70,3}},
        "build_muscle", new int[][]{{2000,180,200,65,3},{2400,200,240,75,3},{2800,220,280,85,4}},
        "clean_eating", new int[][]{{1600,120,180,55,2},{1900,140,220,65,2},{2200,160,260,70,3}}
    );
    private static final String[] LEVELS = {"light", "moderate", "intense"};
    private static final String[] LEVEL_LABELS = {"Light", "Moderate", "Intense"};

    public void setGoal(String goalType, String goalLabel) {
        this.goalType = goalType;
        lblGoalChosen.setText("✓ " + goalLabel);
        buildPlans(goalLabel);
    }

    private void buildPlans(String goalLabel) {
        plansContainer.getChildren().clear();
        int[][] plans = PLANS.getOrDefault(goalType, PLANS.get("maintain"));
        for (int i = 0; i < 3; i++) {
            int[] p = plans[i];
            String level = LEVELS[i];
            String levelLabel = LEVEL_LABELS[i];
            boolean featured = i == 1;

            VBox card = new VBox(12);
            card.setPrefWidth(220);
            String border = featured ? "-fx-border-color: #2E7D5A; -fx-border-width: 2;" : "-fx-border-color: #E2E8F0;";
            card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 24; " +
                    border + " -fx-border-radius: 14; -fx-cursor: hand;");

            if (featured) {
                Label pop = new Label("★ Most Popular");
                pop.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px;");
                card.getChildren().add(pop);
            }

            Label name = new Label(levelLabel);
            name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            Label cal = new Label(p[0] + " cal/day");
            cal.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2E7D5A;");
            Label macros = new Label("P: " + p[1] + "g  C: " + p[2] + "g  F: " + p[3] + "g  W: " + p[4] + "L");
            macros.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
            Label btn = new Label("Choose This Plan →");
            btn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2E7D5A;");

            card.getChildren().addAll(name, cal, macros, btn);
            final int[] fp = p;
            card.setOnMouseClicked(e -> goToConfirm(goalLabel, level, levelLabel, fp));
            plansContainer.getChildren().add(card);
        }
        // Custom plan option
        VBox customCard = new VBox(12);
        customCard.setPrefWidth(220);
        customCard.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 24; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-cursor: hand;");
        javafx.scene.control.Label customName = new javafx.scene.control.Label("🛠 Custom");
        customName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        javafx.scene.control.Label customDesc = new javafx.scene.control.Label("Set your own calories, protein, carbs and fats manually");
        customDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-wrap-text: true;");
        javafx.scene.control.Label customBtn = new javafx.scene.control.Label("Build Custom Plan →");
        customBtn.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2E7D5A;");
        customCard.getChildren().addAll(customName, customDesc, customBtn);
        customCard.setOnMouseClicked(e -> {
            try {
                Parent page = FXMLLoader.load(getClass().getResource("/fxml/objective_custom.fxml"));
                StackPane contentArea = (StackPane) plansContainer.getScene().lookup("#contentArea");
                if (contentArea != null) contentArea.getChildren().setAll(page);
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        plansContainer.getChildren().add(customCard);
    }

    private void goToConfirm(String goalLabel, String level, String levelLabel, int[] plan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/objective_confirm.fxml"));
            Parent page = loader.load();
            ObjectiveConfirmController ctrl = loader.getController();
            ctrl.setPlan(goalType, goalLabel, level, levelLabel, plan);
            StackPane contentArea = (StackPane) plansContainer.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleBack() {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/fxml/objective_choose_goal.fxml"));
            StackPane contentArea = (StackPane) plansContainer.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
