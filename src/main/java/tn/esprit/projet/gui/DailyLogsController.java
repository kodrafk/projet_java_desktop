package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import tn.esprit.projet.models.DailyLog;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.DailyLogService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DailyLogsController {

    @FXML private Label lblHeaderTitle, lblHeaderSub;
    @FXML private Label lblProgress, lblDaysLogged, lblDaysLeft, lblAvgCal;
    @FXML private VBox dayCardsContainer;

    private NutritionObjective objective;
    private DailyLogService service;
    private List<DailyLog> logs;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEEE, MMM d");

    public void setObjective(NutritionObjective obj) {
        this.objective = obj;
        this.service = new DailyLogService();
        this.logs = service.getByObjectiveId(obj.getId());

        // If no logs exist but objective is active, create them now
        if (logs.isEmpty() && obj.isActive() && obj.getStartDate() != null) {
            service.createLogsForObjective(obj.getId(), obj.getStartDate());
            logs = service.getByObjectiveId(obj.getId());
        }

        System.out.println("Daily logs loaded: " + logs.size() + " for objective ID: " + obj.getId());
        populate();
    }

    private void populate() {
        lblHeaderTitle.setText("Daily Logs");
        lblHeaderSub.setText(objective.getTitle());

        int completed = (int) logs.stream().filter(DailyLog::isCompleted).count();
        int totalCal = logs.stream().filter(DailyLog::isCompleted).mapToInt(DailyLog::getCaloriesConsumed).sum();
        int avgCal = completed > 0 ? totalCal / completed : 0;

        lblProgress.setText(objective.getProgressPercentage() + "%");
        lblDaysLogged.setText(completed + "/7");
        lblDaysLeft.setText(String.valueOf(Math.max(0, 7 - completed)));
        lblAvgCal.setText(avgCal > 0 ? String.valueOf(avgCal) : "—");

        dayCardsContainer.getChildren().clear();

        if (logs.isEmpty()) {
            // Show a message if no logs
            javafx.scene.control.Label empty = new javafx.scene.control.Label(
                    "No daily logs found. Make sure the objective is active and has been activated properly.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B; -fx-wrap-text: true; " +
                    "-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 24; " +
                    "-fx-border-color: #E2E8F0; -fx-border-radius: 12;");
            dayCardsContainer.getChildren().add(empty);
            return;
        }

        for (DailyLog log : logs) {
            dayCardsContainer.getChildren().add(buildDayCard(log));
        }
    }

    private HBox buildDayCard(DailyLog log) {
        HBox card = new HBox(0);
        boolean isToday = log.isToday();
        boolean isFuture = log.isFuture();

        String accentColor = log.isCompleted() ? "#2E7D5A" : isToday ? "#eb7147" : "#E2E8F0";
        String opacity = isFuture && !log.isCompleted() ? "-fx-opacity: 0.6;" : "";

        // Accent bar
        javafx.scene.layout.Region accent = new javafx.scene.layout.Region();
        accent.setPrefWidth(4);
        accent.setStyle("-fx-background-color: " + accentColor + ";");

        VBox inner = new VBox(8);
        inner.setStyle("-fx-padding: 16 20; -fx-background-color: white;");
        HBox.setHgrow(inner, Priority.ALWAYS);

        // Top row: day badge + info + action
        HBox topRow = new HBox(14);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Day badge
        VBox badge = new VBox(2);
        badge.setAlignment(javafx.geometry.Pos.CENTER);
        badge.setPrefWidth(56);
        badge.setPrefHeight(56);
        badge.setStyle("-fx-background-color: " + (log.isCompleted() ? "#E8F5E9" : isToday ? "#FFF7ED" : "#F8FAFC") +
                "; -fx-background-radius: 10; -fx-border-color: " + accentColor + "; -fx-border-radius: 10;");
        Label dayNum = new Label(String.valueOf(log.getDayNumber()));
        dayNum.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label dayLbl = new Label("Day");
        dayLbl.setStyle("-fx-font-size: 9px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");
        badge.getChildren().addAll(dayNum, dayLbl);

        // Info
        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label dateLabel = new Label(log.getDate() != null ? log.getDate().format(FMT) : "Day " + log.getDayNumber());
        dateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        String tagText = isToday ? "● Today" : log.isCompleted() ? "✓ " + log.getLoggedMealsCount() + "/4 meals" : isFuture ? "Upcoming" : "Missed";
        String tagColor = isToday ? "#eb7147" : log.isCompleted() ? "#2E7D5A" : "#94A3B8";
        Label tag = new Label(tagText);
        tag.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + tagColor + ";");
        info.getChildren().addAll(dateLabel, tag);

        // Action button
        javafx.scene.control.Button btn;
        if (log.isCompleted()) {
            btn = new javafx.scene.control.Button("View →");
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2E7D5A; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #2E7D5A; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 14;");
        } else if (isToday || (!isFuture)) {
            btn = new javafx.scene.control.Button("Log Day →");
            btn.setStyle("-fx-background-color: #eb7147; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 14;");
        } else {
            btn = new javafx.scene.control.Button("Upcoming");
            btn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #94A3B8; -fx-background-radius: 8; -fx-padding: 8 14;");
            btn.setDisable(true);
        }
        btn.setOnAction(e -> openOverview(log));

        topRow.getChildren().addAll(badge, info, btn);

        // Macros row (if logged)
        if (log.isCompleted()) {
            HBox macros = new HBox(20);
            macros.getChildren().addAll(
                macroLabel("🔥", log.getCaloriesConsumed() + " kcal"),
                macroLabel("💪", (int)log.getProteinConsumed() + "g"),
                macroLabel("🌾", (int)log.getCarbsConsumed() + "g"),
                macroLabel("🥑", (int)log.getFatsConsumed() + "g")
            );
            inner.getChildren().addAll(topRow, macros);
        } else {
            inner.getChildren().add(topRow);
        }

        card.getChildren().addAll(accent, inner);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + (isToday ? "rgba(235,113,71,0.2)" : "#E2E8F0") +
                "; -fx-border-radius: 12; " + opacity);
        return card;
    }

    private Label macroLabel(String icon, String text) {
        Label lbl = new Label(icon + " " + text);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        return lbl;
    }

    private void openOverview(DailyLog log) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/daily_log_overview.fxml"));
            Parent page = loader.load();
            DailyLogOverviewController ctrl = loader.getController();
            ctrl.setData(objective, log);
            StackPane contentArea = (StackPane) dayCardsContainer.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/objective_show.fxml"));
            Parent page = loader.load();
            ObjectiveShowController ctrl = loader.getController();
            ctrl.setObjective(objective);
            StackPane contentArea = (StackPane) dayCardsContainer.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
