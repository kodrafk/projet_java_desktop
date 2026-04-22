package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.services.EmailSignalService;
import tn.esprit.projet.services.InactiveUserNotification;
import tn.esprit.projet.services.NutritionObjectiveService;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.Toast;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class UserListController {

    @FXML private TableView<User>           tableUsers;
    @FXML private TableColumn<User, String> colProfile;
    @FXML private TableColumn<User, String> colRoles;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void>   colActions;
    @FXML private TextField                 searchField;
    @FXML private Label                     lblCount;
    @FXML private Button                    btnBell;
    @FXML private Label                     lblBellBadge;

    private final UserDAO dao = new UserDAO();
    private final NutritionObjectiveService objService = new NutritionObjectiveService();
    private final EmailSignalService emailService = new EmailSignalService();
    private final ObservableList<User> masterList = FXCollections.observableArrayList();
    private FilteredList<User> filtered;
    private List<InactiveUserNotification> notifications = List.of();
    private Popup bellPopup;

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
        loadNotifications();

        filtered = new FilteredList<>(masterList, u -> true);
        SortedList<User> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableUsers.comparatorProperty());
        tableUsers.setItems(sorted);

        searchField.textProperty().addListener((obs, old, val) -> applyFilter(val));

        tableUsers.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setStyle((!empty && u != null && !u.isActive())
                        ? "-fx-background-color: #FFF5F5;" : "");
            }
        });

        if (btnBell != null) {
            btnBell.setOnAction(e -> toggleBellPopup());
        }
    }

    private void loadNotifications() {
        notifications = objService.getInactiveUserNotifications();
        int count = notifications.size();
        if (lblBellBadge != null) {
            lblBellBadge.setText(String.valueOf(count));
            lblBellBadge.setVisible(true);
            if (count == 0) {
                lblBellBadge.setStyle("-fx-background-color: #9ca3af; -fx-text-fill: white; " +
                        "-fx-background-radius: 10; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 1 5;");
            } else {
                lblBellBadge.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; " +
                        "-fx-background-radius: 10; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 1 5;");
            }
        }
        if (btnBell != null) {
            if (count > 0) {
                btnBell.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #fbbf24; " +
                        "-fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 6 10;");
            } else {
                btnBell.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                        "-fx-border-radius: 10; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 6 10;");
            }
        }
    }

    private void toggleBellPopup() {
        if (bellPopup != null && bellPopup.isShowing()) {
            bellPopup.hide();
            bellPopup = null;
            return;
        }
        bellPopup = new Popup();
        bellPopup.setAutoHide(true);
        bellPopup.getContent().add(buildBellDropdown());

        // Position below the bell button
        javafx.geometry.Bounds bounds = btnBell.localToScreen(btnBell.getBoundsInLocal());
        bellPopup.show(btnBell.getScene().getWindow(),
                bounds.getMaxX() - 420,
                bounds.getMaxY() + 8);
    }

    private VBox buildBellDropdown() {
        VBox dropdown = new VBox(0);
        dropdown.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 14; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 24, 0, 0, 6);");
        dropdown.setPrefWidth(420);
        dropdown.setMaxHeight(520);

        // ── Header ──
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 12, 20));
        header.setStyle("-fx-border-color: transparent transparent #f3f4f6 transparent;");
        Label title = new Label("🔔 Inactive Users");
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label countBadge = new Label(String.valueOf(notifications.size()));
        countBadge.setStyle(notifications.isEmpty()
                ? "-fx-background-color: #f3f4f6; -fx-text-fill: #9ca3af; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8;"
                : "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 8;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; " +
                "-fx-background-radius: 8; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 3 8;");
        closeBtn.setOnAction(e -> { if (bellPopup != null) bellPopup.hide(); });
        header.getChildren().addAll(title, countBadge, spacer, closeBtn);

        // ── List ──
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white; -fx-border-color: transparent;");
        scroll.setPrefHeight(Math.min(notifications.size() * 80.0 + 10, 380));

        VBox list = new VBox(0);
        list.setStyle("-fx-background-color: white;");

        if (notifications.isEmpty()) {
            VBox empty = new VBox(6);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(36, 20, 36, 20));
            Label icon = new Label("✅"); icon.setStyle("-fx-font-size: 28px;");
            Label msg  = new Label("All good!"); msg.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");
            Label sub  = new Label("Every user has an active objective"); sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
            empty.getChildren().addAll(icon, msg, sub);
            list.getChildren().add(empty);
        } else {
            for (int i = 0; i < notifications.size(); i++) {
                list.getChildren().add(buildNotifRow(notifications.get(i), i > 0));
            }
        }
        scroll.setContent(list);

        dropdown.getChildren().addAll(header, scroll);

        // ── Footer ──
        if (!notifications.isEmpty()) {
            HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER_LEFT);
            footer.setPadding(new Insets(8, 20, 8, 20));
            footer.setStyle("-fx-background-color: #fafbfc; -fx-border-color: #f3f4f6 transparent transparent transparent; -fx-background-radius: 0 0 14 14;");
            Label fl = new Label(notifications.size() + " inactive user" + (notifications.size() != 1 ? "s" : ""));
            fl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
            footer.getChildren().add(fl);
            dropdown.getChildren().add(footer);
        }

        return dropdown;
    }

    private HBox buildNotifRow(InactiveUserNotification notif, boolean addTopBorder) {
        User u = notif.user();
        LocalDate since = notif.inactiveSince();
        long totalDays = since != null ? ChronoUnit.DAYS.between(since, LocalDate.now()) : 0;

        String severity = totalDays >= 30 ? "danger" : totalDays >= 7 ? "warning" : "mild";
        String avatarColor = switch (severity) {
            case "danger"  -> "linear-gradient(#dc2626, #ef4444)";
            case "warning" -> "linear-gradient(#d97706, #f59e0b)";
            default        -> "linear-gradient(#6b7280, #9ca3af)";
        };
        String timerBg = switch (severity) {
            case "danger"  -> "#fee2e2"; case "warning" -> "#fef3c7"; default -> "#f3f4f6";
        };
        String timerFg = switch (severity) {
            case "danger"  -> "#dc2626"; case "warning" -> "#92400e"; default -> "#6b7280";
        };

        String timerLabel;
        if (totalDays >= 365)     timerLabel = (totalDays / 365) + "y " + ((totalDays % 365) / 30) + "mo";
        else if (totalDays >= 30) timerLabel = (totalDays / 30) + " mo " + (totalDays % 30) + "d";
        else if (totalDays >= 1)  timerLabel = totalDays + " day" + (totalDays != 1 ? "s" : "");
        else                      timerLabel = "today";

        // Avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(38, 38);
        avatar.setMinSize(38, 38);
        avatar.setStyle("-fx-background-radius: 19;");
        Circle circle = new Circle(19, Color.web(severity.equals("danger") ? "#dc2626"
                : severity.equals("warning") ? "#d97706" : "#6b7280"));
        Label initial = new Label(u.getEmail() != null ? String.valueOf(u.getEmail().charAt(0)).toUpperCase() : "?");
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        avatar.getChildren().addAll(circle, initial);

        // Info
        Label nameLabel = new Label(u.getFullName().isBlank() ? u.getEmail() : u.getFullName());
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label emailLabel = new Label(u.getEmail());
        emailLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        String lastObjText = notif.lastObjectiveTitle() != null
                ? "📋 Last: «" + (notif.lastObjectiveTitle().length() > 22
                    ? notif.lastObjectiveTitle().substring(0, 22) + "…"
                    : notif.lastObjectiveTitle()) + "»"
                : "⚠️ No objective ever created";
        Label detailLabel = new Label(lastObjText);
        detailLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6b7280;");
        VBox info = new VBox(2, nameLabel, emailLabel, detailLabel);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Timer
        Label timer = new Label("⏱ " + timerLabel);
        timer.setStyle("-fx-background-color: " + timerBg + "; -fx-text-fill: " + timerFg +
                "; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8;");
        String sinceStr = since != null ? since.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        Label agoLabel = new Label("since " + sinceStr);
        agoLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #b0b5bf;");
        VBox timerBox = new VBox(3, timer, agoLabel);
        timerBox.setAlignment(Pos.CENTER_RIGHT);

        // Signal button
        Button signalBtn = new Button("✉ Signal");
        signalBtn.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-text-fill: #6b7280; -fx-font-size: 10px; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10;");
        signalBtn.setOnMouseEntered(e -> signalBtn.setStyle("-fx-background-color: #fef3c7; -fx-border-color: #f59e0b; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #92400e; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10;"));
        signalBtn.setOnMouseExited(e -> signalBtn.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #6b7280; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10;"));
        signalBtn.setOnAction(e -> handleSignal(notif, signalBtn));

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 16));
        if (addTopBorder)
            row.setStyle("-fx-border-color: #f3f4f6 transparent transparent transparent;");
        row.getChildren().addAll(avatar, info, timerBox, signalBtn);
        return row;
    }

    private void handleSignal(InactiveUserNotification notif, Button btn) {
        User u = notif.user();
        String name = u.getFullName().isBlank() ? u.getEmail() : u.getFullName();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Send email signal about " + name + "?\n\nAn alert email will be sent to the admin.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("📧 Send Signal");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                btn.setText("📲 Sending…");
                btn.setDisable(true);
                new Thread(() -> {
                    boolean ok = emailService.sendSignal(name, u.getEmail(),
                            notif.lastObjectiveTitle(), notif.inactiveSince());
                    Platform.runLater(() -> {
                        Stage owner = (Stage) tableUsers.getScene().getWindow();
                        if (ok) {
                            Toast.show(owner, "Signal sent for " + name, Toast.Type.SUCCESS);
                            btn.setText("✅ Sent");
                        } else {
                            Toast.show(owner, "Failed to send signal. Check SMTP config.", Toast.Type.ERROR);
                            btn.setText("✉ Signal");
                            btn.setDisable(false);
                        }
                    });
                }).start();
            }
        });
    }

    @FXML
    private void handleSearch() {
        applyFilter(searchField.getText());
    }

    private void applyFilter(String val) {
        if (filtered == null) return;
        filtered.setPredicate(u -> {
            if (val == null || val.isBlank()) return true;
            String lower = val.toLowerCase();
            return (u.getEmail()     != null && u.getEmail().toLowerCase().contains(lower))
                || (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(lower))
                || (u.getLastName()  != null && u.getLastName().toLowerCase().contains(lower));
        });
        lblCount.setText(filtered.size() + " user(s)");
    }

    private void setupColumns() {

        // ── Profile column: avatar circle + name + email ──────────────────────
        colProfile.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colProfile.setCellFactory(col -> new TableCell<>() {
            private final StackPane avatar  = new StackPane();
            private final Label     initial = new Label();
            private final ImageView iv      = new ImageView();
            private final Label     name    = new Label();
            private final Label     email   = new Label();
            private final VBox      info    = new VBox(2, name, email);
            private final HBox      row     = new HBox(12, avatar, info);
            {
                avatar.setPrefSize(38, 38);
                avatar.setStyle("-fx-background-color: #2E7D5A; -fx-background-radius: 19;");
                initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                iv.setFitWidth(38); iv.setFitHeight(38); iv.setPreserveRatio(true);
                avatar.getChildren().addAll(initial, iv);
                name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
                email.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
                info.setAlignment(Pos.CENTER_LEFT);
                row.setAlignment(Pos.CENTER_LEFT);
            }
            @Override protected void updateItem(String e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                User u = getTableRow().getItem();
                String fn = u.getFirstName() != null && !u.getFirstName().isEmpty()
                        ? String.valueOf(u.getFirstName().charAt(0)).toUpperCase() : "?";
                initial.setText(fn);
                iv.setVisible(false); initial.setVisible(true);
                if (u.getPhotoFilename() != null && !u.getPhotoFilename().isBlank()) {
                    File f = new File("uploads/profiles/" + u.getPhotoFilename());
                    if (f.exists()) {
                        iv.setImage(new Image(f.toURI().toString()));
                        iv.setVisible(true); initial.setVisible(false);
                    }
                }
                name.setText(u.getFullName());
                email.setText(u.getEmail() != null ? u.getEmail() : "");
                setGraphic(row);
            }
        });

        // ── Role column ───────────────────────────────────────────────────────
        colRoles.setCellValueFactory(c -> {
            String r = c.getValue().getRoles();
            if (r != null && r.startsWith("[")) r = r.replaceAll("[\\[\\]\"\\s]", "");
            return new SimpleStringProperty(r != null ? r : "ROLE_USER");
        });
        colRoles.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setGraphic(null); return; }
                boolean isAdmin = role.contains("ROLE_ADMIN");
                Label badge = new Label("● " + (isAdmin ? "ADMIN" : "USER"));
                badge.setStyle("-fx-background-color: " + (isAdmin ? "#EFF6FF" : "#F0FDF4") + "; " +
                        "-fx-text-fill: " + (isAdmin ? "#1D4ED8" : "#166534") + "; " +
                        "-fx-background-radius: 6; -fx-padding: 3 10; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(badge); setAlignment(Pos.CENTER_LEFT);
            }
        });

        // ── Status column ─────────────────────────────────────────────────────
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "ACTIVE" : "INACTIVE"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                boolean active = "ACTIVE".equals(s);
                Label badge = new Label(s);
                badge.setStyle("-fx-background-color: " + (active ? "#DCFCE7" : "#FEE2E2") + "; " +
                        "-fx-text-fill: " + (active ? "#166534" : "#991B1B") + "; " +
                        "-fx-background-radius: 6; -fx-padding: 3 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                setGraphic(badge); setAlignment(Pos.CENTER_LEFT);
            }
        });

        // ── Actions column: icon buttons matching the design ──────────────────
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnStats  = iconBtn("📊", "#F59E0B", "Stats");
            private final Button btnPhoto  = iconBtn("🖼", "#EC4899", "Photo");
            private final Button btnPwd    = iconBtn("👤", "#6366F1", "Password");
            private final Button btnObjs   = iconBtn("🎯", "#2E7D5A", "Objectives");
            private final Button btnView   = iconBtn("👁",  "#06B6D4", "View");
            private final Button btnEdit   = iconBtn("✏",  "#10B981", "Edit");
            private final Button btnToggle = iconBtn("⚙",  "#8B5CF6", "Toggle");
            private final Button btnDelete = iconBtn("🗑", "#EF4444", "Delete");
            private final HBox   box       = new HBox(4, btnStats, btnPhoto, btnPwd, btnObjs, btnView, btnEdit, btnToggle, btnDelete);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnObjs.setOnAction(e   -> handleViewObjectives(getTableRow().getItem()));
                btnView.setOnAction(e   -> handleView(getTableRow().getItem()));
                btnEdit.setOnAction(e   -> handleEdit(getTableRow().getItem()));
                btnToggle.setOnAction(e -> handleToggle(getTableRow().getItem()));
                btnDelete.setOnAction(e -> handleDelete(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private Button iconBtn(String icon, String color, String tip) {
        Button b = new Button(icon);
        b.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color +
                "; -fx-font-size: 13px; -fx-background-radius: 7; -fx-padding: 4 7; -fx-cursor: hand;" +
                "-fx-border-color: transparent;");
        Tooltip.install(b, new Tooltip(tip));
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: " + color +
                "; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 7; -fx-padding: 4 7; -fx-cursor: hand;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color +
                "; -fx-font-size: 13px; -fx-background-radius: 7; -fx-padding: 4 7; -fx-cursor: hand;" +
                "-fx-border-color: transparent;"));
        return b;
    }

    private void loadData() {
        masterList.setAll(dao.findAll());
        lblCount.setText(masterList.size() + " user(s)");
    }

    @FXML private void handleAddUser() { openForm(null); }

    private void handleViewObjectives(User u) {
        if (u == null) return;
        try {
            // Load admin objectives page filtered for this user
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_objectives.fxml"));
            Parent root = loader.load();
            AdminObjectivesController ctrl = loader.getController();
            ctrl.setUserFilter(u);
            StackPane contentArea = (StackPane) tableUsers.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(root);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleView(User u) {
        if (u == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_view.fxml"));
            Parent root = loader.load();
            UserViewController ctrl = loader.getController();
            ctrl.setUser(u);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("User Details — " + u.getFullName());
            stage.setScene(new Scene(root, 500, 560));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleEdit(User u) { if (u != null) openForm(u); }

    private void handleToggle(User u) {
        if (u == null) return;
        User me = SessionManager.getCurrentUser();
        if (me != null && me.getId() == u.getId()) {
            AlertUtil.show(AlertUtil.Type.WARNING, "Not Allowed", "You cannot toggle your own account.");
            return;
        }
        boolean newState = !u.isActive();
        String msg = "Are you sure you want to " + (newState ? "activate" : "deactivate") + " " + u.getFullName() + "?";
        if (!AlertUtil.confirm((newState ? "Activate" : "Deactivate") + " User", msg)) return;
        if (dao.toggleActive(u.getId(), newState)) {
            loadData();
            Stage owner = (Stage) tableUsers.getScene().getWindow();
            Toast.show(owner, u.getFullName() + " is now " + (newState ? "Active" : "Inactive"),
                    newState ? Toast.Type.SUCCESS : Toast.Type.INFO);
        }
    }

    private void handleDelete(User u) {
        if (u == null) return;
        User me = SessionManager.getCurrentUser();
        if (me != null && me.getId() == u.getId()) {
            AlertUtil.show(AlertUtil.Type.WARNING, "Not Allowed", "You cannot delete your own admin account.");
            return;
        }
        if (!AlertUtil.confirm("Delete User",
                "Delete " + u.getFullName() + "?\n\nThis action cannot be undone. The user will be permanently deleted.")) return;
        if (dao.delete(u.getId())) {
            loadData();
            Stage owner = (Stage) tableUsers.getScene().getWindow();
            Toast.show(owner, "User deleted successfully.", Toast.Type.SUCCESS);
        } else {
            AlertUtil.show(AlertUtil.Type.ERROR, "Error", "Failed to delete user. Please try again.");
        }
    }

    private void openForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user_form.fxml"));
            Parent root = loader.load();
            UserFormController ctrl = loader.getController();
            ctrl.setUser(user);
            ctrl.setOnSaved(() -> {
                loadData();
                Stage owner = (Stage) tableUsers.getScene().getWindow();
                Toast.show(owner, user == null ? "User created." : "User updated.", Toast.Type.SUCCESS);
            });
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(user == null ? "Create New User" : "Edit — " + user.getFullName());
            stage.setScene(new Scene(root, 620, 680));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void warn(String msg) {
        AlertUtil.show(AlertUtil.Type.WARNING, "Not Allowed", msg);
    }
}
