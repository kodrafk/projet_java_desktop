package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.NutritionObjectiveService;
import tn.esprit.projet.services.DailyLogService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ObjectiveShowController {

    @FXML private Label lblHeaderTitle, lblHeaderSub;
    @FXML private Label lblTitle, lblDescription, lblGoalTag, lblStatus;
    @FXML private Label lblCalories, lblProtein, lblCarbs, lblFats, lblWater;
    @FXML private VBox progressSection;
    @FXML private Label lblDateRange, lblProgressPct;
    @FXML private ProgressBar progressBar;
    @FXML private Button btnActivate, btnViewLogs, btnPause, btnResume, btnEdit, btnDelete;
    @FXML private Label lblBlockedMsg;
    @FXML private HBox timelineContainer;
    @FXML private VBox timelineLocked;
    @FXML private Label lblPlannedDate;

    private NutritionObjective objective;
    private NutritionObjectiveService service;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter FMT_LONG = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        service = new NutritionObjectiveService();
    }

    public void setObjective(NutritionObjective obj) {
        this.objective = obj;
        populate();
    }

    private void populate() {
        lblHeaderTitle.setText(objective.getTitle());
        lblHeaderSub.setText(getStatusSubtitle());
        lblTitle.setText(objective.getTitle());
        lblDescription.setText(objective.getDescription() != null ? objective.getDescription() : "");
        lblGoalTag.setText(objective.getGoalLabel() + " / " + objective.getPlanLabel());
        lblStatus.setText(objective.getStatus().toUpperCase());

        lblCalories.setText(String.valueOf(objective.getTargetCalories()));
        lblProtein.setText((int) objective.getTargetProtein() + "g");
        lblCarbs.setText((int) objective.getTargetCarbs() + "g");
        lblFats.setText((int) objective.getTargetFats() + "g");
        lblWater.setText(objective.getTargetWater() + "L");

        // Progress
        if (objective.isActive() || objective.isPaused() || objective.isCompleted()) {
            progressSection.setVisible(true);
            progressSection.setManaged(true);
            if (objective.getStartDate() != null && objective.getEndDate() != null) {
                lblDateRange.setText(objective.getStartDate().format(FMT) + " — " + objective.getEndDate().format(FMT_LONG));
            }
            // Use log-based progress
            int completed = (int) new DailyLogService().getByObjectiveId(objective.getId())
                    .stream().filter(l -> l.isCompleted()).count();
            int pct = objective.getProgressPercentageFromLogs(completed);
            lblProgressPct.setText(pct + "% Complete" + (objective.isPaused() ? " (Paused)" : ""));
            progressBar.setProgress(pct / 100.0);
        }

        // Action buttons
        boolean anotherIsActive = service.getActive() != null
                && service.getActive().getId() != objective.getId();
        btnActivate.setVisible(objective.isPending() && !anotherIsActive);
        btnActivate.setManaged(objective.isPending() && !anotherIsActive);
        lblBlockedMsg.setVisible((objective.isPending() || objective.isPaused()) && anotherIsActive);
        lblBlockedMsg.setManaged((objective.isPending() || objective.isPaused()) && anotherIsActive);
        btnViewLogs.setVisible(objective.isActive() || objective.isPaused() || objective.isCompleted());
        btnViewLogs.setManaged(objective.isActive() || objective.isPaused() || objective.isCompleted());
        btnPause.setVisible(objective.isActive());
        btnPause.setManaged(objective.isActive());
        btnResume.setVisible(objective.isPaused() && !anotherIsActive);
        btnResume.setManaged(objective.isPaused() && !anotherIsActive);
        btnEdit.setVisible(!objective.isCompleted());
        btnEdit.setManaged(!objective.isCompleted());

        // Timeline
        timelineLocked.setVisible(false);
        timelineLocked.setManaged(false);
        timelineContainer.getChildren().clear();

        if (objective.isPending()) {
            timelineLocked.setVisible(true);
            timelineLocked.setManaged(true);
            if (objective.getPlannedStartDate() != null) {
                lblPlannedDate.setText("Planned: " + objective.getPlannedStartDate().format(FMT_LONG));
            }
        } else {
            buildTimeline();
        }
    }

    private String getStatusSubtitle() {
        return switch (objective.getStatus()) {
            case "active" -> "Your journey is underway. Track each day and finish strong.";
            case "paused" -> "Your objective is paused. Resume whenever you're ready.";
            case "pending" -> "Everything is set. Activate when you're ready to begin.";
            case "completed" -> "Challenge complete. Review your results.";
            default -> "";
        };
    }

    private void buildTimeline() {
        timelineContainer.getChildren().clear();
        if (objective.getStartDate() == null) return;
        for (int i = 0; i < 7; i++) {
            java.time.LocalDate day = objective.getStartDate().plusDays(i);
            boolean isToday = day.equals(java.time.LocalDate.now());
            boolean isPast = day.isBefore(java.time.LocalDate.now());

            VBox dayBox = new VBox(4);
            dayBox.setAlignment(javafx.geometry.Pos.CENTER);
            dayBox.setPrefWidth(70);
            String bg = isPast ? "#1F4D3A" : isToday ? "#2E7D5A" : "#F1F5F9";
            String fg = (isPast || isToday) ? "white" : "#64748B";
            dayBox.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10; -fx-padding: 10;");

            Label dayNum = new Label("Day " + (i + 1));
            dayNum.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + fg + ";");
            Label dayDate = new Label(day.format(FMT));
            dayDate.setStyle("-fx-font-size: 10px; -fx-text-fill: " + fg + ";");
            Label check = new Label(isPast ? "✓" : isToday ? "●" : "○");
            check.setStyle("-fx-font-size: 14px; -fx-text-fill: " + fg + ";");

            dayBox.getChildren().addAll(check, dayNum, dayDate);
            timelineContainer.getChildren().add(dayBox);
        }
    }

    @FXML private void handleBack() { navigateTo("/fxml/objectives.fxml"); }

    @FXML
    private void handleActivate() {
        // Block if another objective is already active
        NutritionObjective currentlyActive = service.getActive();
        if (currentlyActive != null && currentlyActive.getId() != objective.getId()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                "You already have an active objective: \"" + currentlyActive.getTitle() + "\".\n\nPause or delete it first before activating a new one.",
                ButtonType.OK);
            alert.setHeaderText("Only one active objective allowed");
            alert.showAndWait();
            return;
        }
        service.activate(objective);
        objective = service.getById(objective.getId());
        populate();
    }

    @FXML
    private void handlePause() {
        service.pause(objective);
        objective = service.getById(objective.getId());
        populate();
    }

    @FXML
    private void handleResume() {
        NutritionObjective currentlyActive = service.getActive();
        if (currentlyActive != null && currentlyActive.getId() != objective.getId()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                "You already have an active objective: \"" + currentlyActive.getTitle() + "\".\n\nPause it first before resuming this one.",
                ButtonType.OK);
            alert.setHeaderText("Only one active objective allowed");
            alert.showAndWait();
            return;
        }
        service.resume(objective);
        objective = service.getById(objective.getId());
        populate();
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/objective_edit.fxml"));
            Parent page = loader.load();
            ObjectiveEditController ctrl = loader.getController();
            ctrl.setObjective(objective);
            StackPane contentArea = (StackPane) lblTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleViewLogs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/daily_logs.fxml"));
            Parent page = loader.load();
            DailyLogsController ctrl = loader.getController();
            ctrl.setObjective(objective);
            StackPane contentArea = (StackPane) lblTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleDelete() {
        StackPane contentArea = (StackPane) btnDelete.getScene().lookup("#contentArea");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + objective.getTitle() + "\" permanently?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            service.delete(objective.getId());
            System.out.println("Deleted ID: " + objective.getId() + ", contentArea: " + contentArea);
            try {
                Parent page = FXMLLoader.load(getClass().getResource("/fxml/objectives.fxml"));
                if (contentArea != null) {
                    contentArea.getChildren().setAll(page);
                } else {
                    System.out.println("contentArea is NULL — scene lookup failed");
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void navigateTo(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentArea = (StackPane) lblTitle.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
