package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.models.User;
import tn.esprit.projet.services.NutritionObjectiveService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminObjectivesController {

    @FXML private VBox objectivesContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Label lblCount;

    private NutritionObjectiveService service;
    private List<NutritionObjective> all;
    private User filteredUser = null; // when set, show only this user's objectives
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    public void initialize() {
        service = new NutritionObjectiveService();
        all = service.getAll();

        sortCombo.getItems().addAll(
            "All", "Active", "Pending", "Paused", "Completed",
            "Newest First", "Oldest First", "Calories ↑", "Calories ↓"
        );
        sortCombo.setValue("All");
        sortCombo.setOnAction(e -> render(filter(searchField.getText())));
        searchField.textProperty().addListener((o, old, n) -> render(filter(n)));
        render(all);
    }

    /** Called from UserListController to show only one user's objectives */
    public void setUserFilter(User user) {
        this.filteredUser = user;
        // Reload filtered by this user
        all = service.getAllForAdmin().stream()
            .filter(o -> {
                // We need to get user_id from DB — use a service method
                return true; // will filter below
            })
            .collect(java.util.stream.Collectors.toList());
        // Actually filter by user_id via a dedicated query
        all = service.getAllByUserId(user.getId());
        lblCount.setText("Objectives for: " + user.getFullName() + " (" + all.size() + ")");
        render(all);
    }

    private List<NutritionObjective> filter(String q) {
        String lq = q == null ? "" : q.toLowerCase().trim();
        String sort = sortCombo.getValue();

        List<NutritionObjective> result = all.stream()
                .filter(o -> lq.isEmpty()
                        || o.getTitle().toLowerCase().contains(lq)
                        || o.getStatus().toLowerCase().contains(lq))
                .collect(Collectors.toList());

        // Status filter
        switch (sort == null ? "All" : sort) {
            case "Active"    -> result = result.stream().filter(NutritionObjective::isActive).collect(Collectors.toList());
            case "Pending"   -> result = result.stream().filter(NutritionObjective::isPending).collect(Collectors.toList());
            case "Paused"    -> result = result.stream().filter(NutritionObjective::isPaused).collect(Collectors.toList());
            case "Completed" -> result = result.stream().filter(NutritionObjective::isCompleted).collect(Collectors.toList());
            case "Newest First" -> result.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            });
            case "Oldest First" -> result.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            });
            case "Calories ↑" -> result.sort((a, b) -> a.getTargetCalories() - b.getTargetCalories());
            case "Calories ↓" -> result.sort((a, b) -> b.getTargetCalories() - a.getTargetCalories());
        }

        return result;
    }

    private void render(List<NutritionObjective> list) {
        objectivesContainer.getChildren().clear();
        lblCount.setText(list.size() + " objective(s)");

        if (list.isEmpty()) {
            Label empty = new Label("No objectives found.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-padding: 24;");
            objectivesContainer.getChildren().add(empty);
            return;
        }

        for (NutritionObjective obj : list) {
            objectivesContainer.getChildren().add(buildRow(obj));
        }
    }

    private HBox buildRow(NutritionObjective obj) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #F1F5F9 transparent; -fx-padding: 12 26;");

        // Status color
        String statusColor = switch (obj.getStatus()) {
            case "active"    -> "#2E7D5A";
            case "paused"    -> "#D97706";
            case "completed" -> "#1F4D3A";
            default          -> "#64748B";
        };

        Label title = new Label(obj.getTitle());
        title.setPrefWidth(260);
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Label status = new Label(obj.getStatus().toUpperCase());
        status.setPrefWidth(100);
        status.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + statusColor +
                "; -fx-background-color: " + statusColor + "22; -fx-background-radius: 6; -fx-padding: 3 8;");

        Label goal = new Label(obj.getGoalLabel());
        goal.setPrefWidth(120);
        goal.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        Label cal = new Label(obj.getTargetCalories() + " kcal");
        cal.setPrefWidth(90);
        cal.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        Label startDate = new Label(obj.getStartDate() != null ? obj.getStartDate().format(FMT)
                : obj.getPlannedStartDate() != null ? obj.getPlannedStartDate().format(FMT) : "—");
        startDate.setPrefWidth(110);
        startDate.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action buttons
        Button btnView = new Button("👁 View");
        btnView.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #475569; -fx-background-radius: 7; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10; -fx-border-color: #E2E8F0; -fx-border-radius: 7;");
        btnView.setOnAction(e -> openView(obj));

        Button btnLogs = new Button("📋 Logs");
        btnLogs.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-background-radius: 7; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10;");
        btnLogs.setOnAction(e -> openLogs(obj));

        Button btnEdit = new Button("✏ Edit");
        btnEdit.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #15803D; -fx-background-radius: 7; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10;");
        btnEdit.setOnAction(e -> openEdit(obj));

        Button btnDelete = new Button("🗑");
        btnDelete.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-background-radius: 7; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10;");
        btnDelete.setOnAction(e -> deleteObjective(obj));

        HBox actions = new HBox(6, btnView, btnLogs, btnEdit, btnDelete);
        actions.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(title, status, goal, cal, startDate, spacer, actions);

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: transparent transparent #F1F5F9 transparent; -fx-padding: 12 26;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #F1F5F9 transparent; -fx-padding: 12 26;"));

        return row;
    }

    private void openView(NutritionObjective obj) {
        String panelId = "detail-" + obj.getId();

        // If panel already exists → remove it (toggle off)
        boolean removed = objectivesContainer.getChildren().removeIf(
                n -> panelId.equals(n.getUserData()));
        if (removed) return;

        // Find the row index for this obj in the current list
        List<NutritionObjective> current = filter(searchField.getText());
        int idx = -1;
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getId() == obj.getId()) { idx = i; break; }
        }
        if (idx < 0) return;

        // Each row occupies one slot; panels already inserted shift indices,
        // so count actual children up to the target row
        int insertAt = 0;
        int rowsSeen = 0;
        for (int i = 0; i < objectivesContainer.getChildren().size(); i++) {
            Object ud = objectivesContainer.getChildren().get(i).getUserData();
            if (ud == null) rowsSeen++; // rows have no userData, panels do
            if (rowsSeen - 1 == idx) { insertAt = i + 1; break; }
        }

        // Build detail panel
        VBox detail = new VBox(12);
        detail.setUserData(panelId);
        detail.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 18 26; -fx-border-width: 1;");

        Label title = new Label(obj.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label desc = new Label(obj.getDescription() != null && !obj.getDescription().isBlank()
                ? obj.getDescription() : "No description.");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B; -fx-wrap-text: true;");
        desc.setMaxWidth(700);

        HBox macros = new HBox(12);
        macros.getChildren().addAll(
            macroChip("🔥 Calories", obj.getTargetCalories() + " kcal"),
            macroChip("💪 Protein",  (int)obj.getTargetProtein() + "g"),
            macroChip("🌾 Carbs",    (int)obj.getTargetCarbs() + "g"),
            macroChip("🥑 Fats",     (int)obj.getTargetFats() + "g"),
            macroChip("💧 Water",    obj.getTargetWater() + "L")
        );

        HBox dates = new HBox(12);
        String startStr = obj.getStartDate() != null ? obj.getStartDate().format(FMT)
                : obj.getPlannedStartDate() != null ? obj.getPlannedStartDate().format(FMT) : "—";
        String endStr = obj.getEndDate() != null ? obj.getEndDate().format(FMT) : "—";
        dates.getChildren().addAll(
            macroChip("📅 Start", startStr),
            macroChip("🏁 End", endStr),
            macroChip("📊 Progress", obj.getProgressPercentage() + "%"),
            macroChip("🎯 Goal", obj.getGoalLabel()),
            macroChip("⚡ Plan", obj.getPlanLabel())
        );

        Button closeBtn = new Button("✕ Close");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> objectivesContainer.getChildren().remove(detail));
        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.CENTER_RIGHT);

        detail.getChildren().addAll(title, desc, macros, dates, closeRow);
        objectivesContainer.getChildren().add(insertAt, detail);
    }

    private VBox macroChip(String label, String value) {
        VBox chip = new VBox(2);
        chip.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8 14; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        chip.getChildren().addAll(lbl, val);
        return chip;
    }

    private void openLogs(NutritionObjective obj) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_daily_logs.fxml"));
            Parent page = loader.load();
            AdminDailyLogsController ctrl = loader.getController();
            ctrl.setObjective(obj, getContentArea());
            getContentArea().getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openEdit(NutritionObjective obj) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/objective_edit_custom.fxml"));
            Parent page = loader.load();
            ObjectiveEditCustomController ctrl = loader.getController();
            ctrl.setObjective(obj);
            ctrl.setBackTarget("/fxml/admin_objectives.fxml");
            getContentArea().getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteObjective(NutritionObjective obj) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + obj.getTitle() + "\" and all its daily logs?",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm Delete");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            service.delete(obj.getId());
            all = service.getAll();
            render(filter(searchField.getText()));
        }
    }

    private StackPane getContentArea() {
        return (StackPane) objectivesContainer.getScene().lookup("#contentArea");
    }
}
