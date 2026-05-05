package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.projet.models.DailyLog;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.DailyLogService;

import java.time.format.DateTimeFormatter;

public class DailyLogOverviewController {

    @FXML private Label lblDayBadge, lblHeaderTitle, lblHeaderSub;
    @FXML private Label lblMealsCount;
    @FXML private Label lblTotalCal, lblTargetCal;
    @FXML private Label lblTotalProtein, lblTargetProtein;
    @FXML private Label lblTotalCarbs, lblTargetCarbs;
    @FXML private Label lblTotalFats, lblTargetFats;
    @FXML private ProgressBar calorieProgress;
    @FXML private GridPane mealsGrid;
    @FXML private Button btnMoodGreat, btnMoodGood, btnMoodOkay, btnMoodLow;
    @FXML private TextArea notesField;

    private NutritionObjective objective;
    private DailyLog log;
    private DailyLogService service;
    private String selectedMood;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");

    public void setData(NutritionObjective obj, DailyLog dailyLog) {
        this.objective = obj;
        this.log = dailyLog;
        this.service = new DailyLogService();
        this.selectedMood = dailyLog.getMood();
        populate();
    }

    private void populate() {
        lblDayBadge.setText("Day " + log.getDayNumber());
        lblHeaderTitle.setText(log.getDate() != null ? log.getDate().format(FMT) : "Day " + log.getDayNumber());
        lblHeaderSub.setText(objective.getTitle());

        lblMealsCount.setText(log.getLoggedMealsCount() + "/4 meals");
        lblTotalCal.setText(String.valueOf(log.getCaloriesConsumed()));
        lblTargetCal.setText("/ " + objective.getTargetCalories() + " kcal");
        lblTotalProtein.setText((int)log.getProteinConsumed() + "g");
        lblTargetProtein.setText("/ " + (int)objective.getTargetProtein() + "g");
        lblTotalCarbs.setText((int)log.getCarbsConsumed() + "g");
        lblTargetCarbs.setText("/ " + (int)objective.getTargetCarbs() + "g");
        lblTotalFats.setText((int)log.getFatsConsumed() + "g");
        lblTargetFats.setText("/ " + (int)objective.getTargetFats() + "g");

        double progress = objective.getTargetCalories() > 0
                ? Math.min(1.0, log.getCaloriesConsumed() / (double) objective.getTargetCalories())
                : 0;
        calorieProgress.setProgress(progress);

        if (log.getNotes() != null) notesField.setText(log.getNotes());
        buildMealCards();
        updateMoodButtons();
    }

    private void buildMealCards() {
        mealsGrid.getChildren().clear();
        String[] types  = DailyLog.MEAL_TYPES;
        String[] labels = DailyLog.MEAL_LABELS;
        String[] emojis = DailyLog.MEAL_EMOJIS;
        String[] colors = {"#F7A325", "#eb7147", "#2E7D5A", "#A78BFA"};

        int col = 0, row = 0;
        for (int i = 0; i < 4; i++) {
            final String mealType = types[i];
            boolean logged = log.isMealLogged(mealType);

            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                    "-fx-border-color: " + (logged ? "rgba(46,125,90,0.25)" : "#E2E8F0") +
                    "; -fx-border-radius: 14; -fx-padding: 0;");

            // Accent top bar
            javafx.scene.layout.Region bar = new javafx.scene.layout.Region();
            bar.setPrefHeight(4);
            bar.setStyle("-fx-background-color: " + colors[i] + "; -fx-background-radius: 14 14 0 0;");

            VBox body = new VBox(10);
            body.setStyle("-fx-padding: 16;");

            HBox topRow = new HBox(12);
            topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label emoji = new Label(emojis[i]);
            emoji.setStyle("-fx-font-size: 28px;");
            VBox info = new VBox(3);
            Label name = new Label(labels[i]);
            name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            String statusText = logged ? "✓ Logged" : "Not logged";
            String statusColor = logged ? "#2E7D5A" : "#94A3B8";
            Label status = new Label(statusText);
            status.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
            info.getChildren().addAll(name, status);
            topRow.getChildren().addAll(emoji, info);

            if (logged) {
                Label cal = new Label("🔥 " + log.getMealCalories(mealType) + " kcal");
                cal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #eb7147;");
                body.getChildren().addAll(topRow, cal);
            } else {
                body.getChildren().add(topRow);
            }

            Button btn = new Button(logged ? "✏ Edit" : "➕ Log " + labels[i]);
            btn.setPrefWidth(Double.MAX_VALUE);
            btn.setStyle(logged
                    ? "-fx-background-color: transparent; -fx-text-fill: #2E7D5A; -fx-border-color: #2E7D5A; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10;"
                    : "-fx-background-color: #eb7147; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10;");
            btn.setOnAction(e -> openMealEdit(mealType));
            body.getChildren().add(btn);

            card.getChildren().addAll(bar, body);
            mealsGrid.add(card, col, row);
            col++;
            if (col > 1) { col = 0; row++; }
        }
    }

    private void openMealEdit(String mealType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/daily_log_edit.fxml"));
            Parent page = loader.load();
            DailyLogEditController ctrl = loader.getController();
            ctrl.setData(objective, log, mealType);
            StackPane contentArea = (StackPane) mealsGrid.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateMoodButtons() {
        String active = "-fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 10 16;";
        btnMoodGreat.setStyle("-fx-background-color: " + ("great".equals(selectedMood) ? "#2E7D5A; -fx-text-fill: white;" : "#F0FDF4; -fx-text-fill: #2E7D5A;") + active);
        btnMoodGood.setStyle("-fx-background-color: "  + ("good".equals(selectedMood)  ? "#2E7D5A; -fx-text-fill: white;" : "#F0FDF4; -fx-text-fill: #2E7D5A;") + active);
        btnMoodOkay.setStyle("-fx-background-color: "  + ("okay".equals(selectedMood)  ? "#D97706; -fx-text-fill: white;" : "#FFFBEB; -fx-text-fill: #D97706;") + active);
        btnMoodLow.setStyle("-fx-background-color: "   + ("low".equals(selectedMood)   ? "#eb7147; -fx-text-fill: white;" : "#FFF7ED; -fx-text-fill: #eb7147;") + active);
    }

    @FXML private void setMoodGreat() { selectedMood = "great"; updateMoodButtons(); }
    @FXML private void setMoodGood()  { selectedMood = "good";  updateMoodButtons(); }
    @FXML private void setMoodOkay()  { selectedMood = "okay";  updateMoodButtons(); }
    @FXML private void setMoodLow()   { selectedMood = "low";   updateMoodButtons(); }

    @FXML
    private void handleSaveNotes() {
        log.setMood(selectedMood);
        String notes = notesField.getText();
        if (notes != null && notes.length() > 1000) notes = notes.substring(0, 1000);
        log.setNotes(notes);
        service.update(log);
    }

    @FXML private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/daily_logs.fxml"));
            Parent page = loader.load();
            DailyLogsController ctrl = loader.getController();
            ctrl.setObjective(objective);
            StackPane contentArea = (StackPane) mealsGrid.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
