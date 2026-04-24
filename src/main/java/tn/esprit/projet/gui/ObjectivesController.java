package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.DailyLogService;
import tn.esprit.projet.services.NutritionObjectiveService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectivesController {

    @FXML private VBox activeBanner;
    @FXML private Label activeBannerTitle, activeBannerMeta, activeBannerProgress;
    @FXML private ProgressBar activeBannerBar;
    @FXML private Button btnViewLogs, btnNewObjective;
    @FXML private Label lblActiveCount, lblPausedCount, lblPendingCount, lblDoneCount, lblCount;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private VBox objectivesContainer, emptyState;

    private NutritionObjectiveService service;
    private List<NutritionObjective> allObjectives;
    private NutritionObjective activeObjective;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d");

    @FXML
    public void initialize() {
        service = new NutritionObjectiveService();

        sortCombo.getItems().addAll("Default", "Newest First", "Oldest First", "Status");
        sortCombo.setValue("Default");
        sortCombo.setOnAction(e -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        loadData();
    }

    private void loadData() {
        allObjectives = service.getAll();
        activeObjective = allObjectives.stream().filter(NutritionObjective::isActive).findFirst().orElse(null);

        updateStats();
        updateActiveBanner();
        applyFilter();
    }

    private void updateStats() {
        lblActiveCount.setText(String.valueOf(allObjectives.stream().filter(NutritionObjective::isActive).count()));
        lblPausedCount.setText(String.valueOf(allObjectives.stream().filter(NutritionObjective::isPaused).count()));
        lblPendingCount.setText(String.valueOf(allObjectives.stream().filter(NutritionObjective::isPending).count()));
        lblDoneCount.setText(String.valueOf(allObjectives.stream().filter(NutritionObjective::isCompleted).count()));
    }

    private void updateActiveBanner() {
        if (activeObjective != null) {
            activeBanner.setVisible(true);
            activeBanner.setManaged(true);
            activeBannerTitle.setText(activeObjective.getTitle());
            String meta = "";
            if (activeObjective.getStartDate() != null && activeObjective.getEndDate() != null) {
                meta = activeObjective.getStartDate().format(FMT) + " — " + activeObjective.getEndDate().format(FMT)
                        + "  ·  " + activeObjective.getDaysRemaining() + " days left";
            }
            activeBannerMeta.setText(meta);
            // Use log-based progress
            DailyLogService dlService = new DailyLogService();
            int completed = (int) dlService.getByObjectiveId(activeObjective.getId())
                    .stream().filter(l -> l.isCompleted()).count();
            int pct = activeObjective.getProgressPercentageFromLogs(completed);
            activeBannerProgress.setText(pct + "%");
            activeBannerBar.setProgress(pct / 100.0);
        } else {
            activeBanner.setVisible(false);
            activeBanner.setManaged(false);
        }
    }

    private void applyFilter() {
        String query = searchField.getText().toLowerCase().trim();
        String sort = sortCombo.getValue();

        List<NutritionObjective> filtered = allObjectives.stream()
                .filter(o -> query.isEmpty()
                        || o.getTitle().toLowerCase().contains(query)
                        || (o.getDescription() != null && o.getDescription().toLowerCase().contains(query)))
                .collect(Collectors.toList());

        if ("Newest First".equals(sort)) {
            filtered.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        } else if ("Oldest First".equals(sort)) {
            filtered.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        } else if ("Status".equals(sort)) {
            filtered.sort((a, b) -> statusOrder(a) - statusOrder(b));
        }

        lblCount.setText(filtered.size() + " of " + allObjectives.size() + " objectives");
        renderCards(filtered);
    }

    private int statusOrder(NutritionObjective o) {
        return switch (o.getStatus()) {
            case "active" -> 1;
            case "pending" -> 2;
            case "paused" -> 3;
            default -> 4;
        };
    }

    private void renderCards(List<NutritionObjective> list) {
        objectivesContainer.getChildren().clear();

        if (list.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            return;
        }
        emptyState.setVisible(false);
        emptyState.setManaged(false);

        for (NutritionObjective obj : list) {
            objectivesContainer.getChildren().add(buildCard(obj));
        }
    }

    private VBox buildCard(NutritionObjective obj) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 20; " +
                "-fx-border-color: #E2E8F0; -fx-border-radius: 14; -fx-cursor: hand;");

        // Status color
        String statusColor = switch (obj.getStatus()) {
            case "active" -> "#2E7D5A";
            case "paused" -> "#f0ad4e";
            case "completed" -> "#1F4D3A";
            default -> "#64748B";
        };

        HBox topRow = new HBox(12);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statusBadge = new Label(obj.getStatus().toUpperCase());
        statusBadge.setStyle("-fx-background-color: " + statusColor + "22; -fx-text-fill: " + statusColor +
                "; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label title = new Label(obj.getTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button detailsBtn = new Button("Details →");
        detailsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #2E7D5A; -fx-font-size: 12px; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> openShow(obj));

        topRow.getChildren().addAll(statusBadge, title, spacer, detailsBtn);

        // Macros row
        HBox macros = new HBox(16);
        macros.getChildren().addAll(
                macroLabel("🔥", obj.getTargetCalories() + " kcal"),
                macroLabel("💪", (int) obj.getTargetProtein() + "g protein"),
                macroLabel("🌾", (int) obj.getTargetCarbs() + "g carbs"),
                macroLabel("🥑", (int) obj.getTargetFats() + "g fats")
        );

        card.getChildren().addAll(topRow, macros);

        // Progress bar for active/paused/completed
        if (obj.isActive() || obj.isPaused() || obj.isCompleted()) {
            ProgressBar pb = new ProgressBar(obj.getProgressPercentage() / 100.0);
            pb.setPrefWidth(Double.MAX_VALUE);
            pb.setStyle("-fx-accent: " + statusColor + ";");
            Label pctLabel = new Label(obj.getProgressPercentage() + "%");
            pctLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
            card.getChildren().addAll(pb, pctLabel);
        }

        card.setOnMouseClicked(e -> openShow(obj));
        return card;
    }

    private Label macroLabel(String icon, String text) {
        Label lbl = new Label(icon + " " + text);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        return lbl;
    }

    private void openShow(NutritionObjective obj) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/objective_show.fxml"));
            Parent page = loader.load();
            ObjectiveShowController ctrl = loader.getController();
            ctrl.setObjective(obj);
            // Navigate via parent StackPane
            StackPane contentArea = (StackPane) objectivesContainer.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().setAll(page);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewObjective() {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/fxml/objective_choose_goal.fxml"));
            StackPane contentArea = (StackPane) objectivesContainer.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewLogs() {
        if (activeObjective == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/daily_logs.fxml"));
            Parent page = loader.load();
            DailyLogsController ctrl = loader.getController();
            ctrl.setObjective(activeObjective);
            StackPane contentArea = (StackPane) objectivesContainer.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
