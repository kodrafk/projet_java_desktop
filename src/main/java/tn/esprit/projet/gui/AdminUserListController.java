package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.services.EmailSignalService;
import tn.esprit.projet.services.InactiveUserNotification;
import tn.esprit.projet.services.NutritionObjectiveService;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AdminUserListController {

    @FXML private TextField                 searchField;
    @FXML private TableView<User>           userTable;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, String> colCreatedAt;
    @FXML private TableColumn<User, Void>   colActions;
    @FXML private Label                     lblCount;
    @FXML private Button                    btnAdminProfile;
    @FXML private Button                    btnBell;
    @FXML private Label                     lblBellBadge;

    private final UserRepository            repo          = new UserRepository();
    private final NutritionObjectiveService objService    = new NutritionObjectiveService();
    private final EmailSignalService        emailService  = new EmailSignalService();
    private final ObservableList<User>      data          = FXCollections.observableArrayList();
    private List<InactiveUserNotification>  notifications = List.of();
    private Popup                           bellPopup;
    private static final DateTimeFormatter  FMT           = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Reference to parent AdminLayoutController for navigation
    private AdminLayoutController parentController;

    public void setParentController(AdminLayoutController parent) {
        this.parentController = parent;
    }

    // Sort state
    private String sortCol = "id";
    private String sortDir = "DESC";

    @FXML
    public void initialize() {
        setupColumns();
        userTable.setItems(data);
        loadAll();
        loadNotifications();
        
        // Setup admin profile button tooltip
        if (btnAdminProfile != null) {
            Tooltip tooltip = new Tooltip("View/Edit Admin Profile");
            Tooltip.install(btnAdminProfile, tooltip);
        }

        // Bell button
        if (btnBell != null) {
            btnBell.setOnAction(e -> toggleBellPopup());
        }

        // Live search on each keystroke
        searchField.textProperty().addListener((o, a, b) -> {
            if (b == null || b.isBlank()) loadAll();
            else {
                List<User> results = repo.searchByEmailOrName(b.trim());
                data.setAll(results);
                updateCount();
            }
        });

        // Sort on column header click
        userTable.setOnSort(e -> {
            if (!userTable.getSortOrder().isEmpty()) {
                TableColumn<User, ?> col = userTable.getSortOrder().get(0);
                sortDir = col.getSortType() == TableColumn.SortType.ASCENDING ? "ASC" : "DESC";
                sortCol = colToField(col.getId());
                List<User> sorted = repo.findAllSortedBy(sortCol, sortDir);
                data.setAll(sorted);
                updateCount();
            }
        });

        // Inactive rows in light red
        userTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setStyle((!empty && u != null && !u.isActive()) ? "-fx-background-color:#FFF5F5;" : "");
            }
        });
    }

    // ══════════════════════════════════════════════════════
    // BELL / NOTIFICATION PANEL
    // ══════════════════════════════════════════════════════

    private void loadNotifications() {
        notifications = objService.getInactiveUserNotifications();
        int count = notifications.size();
        if (lblBellBadge != null) {
            lblBellBadge.setText(String.valueOf(count));
            lblBellBadge.setVisible(true);
            lblBellBadge.setStyle(count == 0
                ? "-fx-background-color:#9ca3af;-fx-text-fill:white;-fx-background-radius:10;-fx-font-size:9px;-fx-font-weight:bold;-fx-padding:1 5;"
                : "-fx-background-color:#dc2626;-fx-text-fill:white;-fx-background-radius:10;-fx-font-size:9px;-fx-font-weight:bold;-fx-padding:1 5;");
        }
        if (btnBell != null) {
            btnBell.setStyle(count > 0
                ? "-fx-background-color:#fef3c7;-fx-border-color:#fbbf24;-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;-fx-padding:6 10;-fx-font-size:14px;"
                : "-fx-background-color:white;-fx-border-color:#e5e7eb;-fx-border-radius:10;-fx-background-radius:10;-fx-cursor:hand;-fx-padding:6 10;-fx-font-size:14px;");
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
        javafx.geometry.Bounds bounds = btnBell.localToScreen(btnBell.getBoundsInLocal());
        bellPopup.show(btnBell.getScene().getWindow(),
                bounds.getMaxX() - 420,
                bounds.getMaxY() + 8);
    }

    private VBox buildBellDropdown() {
        VBox dropdown = new VBox(0);
        dropdown.setStyle("-fx-background-color:white;-fx-border-color:#e5e7eb;" +
                "-fx-border-radius:14;-fx-background-radius:14;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),24,0,0,6);");
        dropdown.setPrefWidth(420);
        dropdown.setMaxHeight(520);

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 12, 20));
        header.setStyle("-fx-border-color:transparent transparent #f3f4f6 transparent;");
        Label title = new Label("🔔 Inactive Users");
        title.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");
        Label countBadge = new Label(String.valueOf(notifications.size()));
        countBadge.setStyle(notifications.isEmpty()
            ? "-fx-background-color:#f3f4f6;-fx-text-fill:#9ca3af;-fx-background-radius:10;-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:2 8;"
            : "-fx-background-color:#fee2e2;-fx-text-fill:#dc2626;-fx-background-radius:10;-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:2 8;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color:#f3f4f6;-fx-text-fill:#6b7280;-fx-background-radius:8;-fx-font-size:11px;-fx-cursor:hand;-fx-padding:3 8;");
        closeBtn.setOnAction(e -> { if (bellPopup != null) bellPopup.hide(); });
        header.getChildren().addAll(title, countBadge, spacer, closeBtn);

        // List
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;-fx-border-color:transparent;");
        scroll.setPrefHeight(Math.min(notifications.size() * 80.0 + 10, 380));

        VBox list = new VBox(0);
        list.setStyle("-fx-background-color:white;");

        if (notifications.isEmpty()) {
            VBox empty = new VBox(6);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(36, 20, 36, 20));
            Label icon = new Label("✅"); icon.setStyle("-fx-font-size:28px;");
            Label msg  = new Label("All good!"); msg.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#6b7280;");
            Label sub  = new Label("Every user has an active objective"); sub.setStyle("-fx-font-size:11px;-fx-text-fill:#9ca3af;");
            empty.getChildren().addAll(icon, msg, sub);
            list.getChildren().add(empty);
        } else {
            for (int i = 0; i < notifications.size(); i++) {
                list.getChildren().add(buildNotifRow(notifications.get(i), i > 0));
            }
        }
        scroll.setContent(list);
        dropdown.getChildren().addAll(header, scroll);

        // Footer
        if (!notifications.isEmpty()) {
            HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER_LEFT);
            footer.setPadding(new Insets(8, 20, 8, 20));
            footer.setStyle("-fx-background-color:#fafbfc;-fx-border-color:#f3f4f6 transparent transparent transparent;-fx-background-radius:0 0 14 14;");
            Label fl = new Label(notifications.size() + " inactive user" + (notifications.size() != 1 ? "s" : ""));
            fl.setStyle("-fx-font-size:10px;-fx-text-fill:#9ca3af;");
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
        String timerBg  = switch (severity) { case "danger" -> "#fee2e2"; case "warning" -> "#fef3c7"; default -> "#f3f4f6"; };
        String timerFg  = switch (severity) { case "danger" -> "#dc2626"; case "warning" -> "#92400e"; default -> "#6b7280"; };
        Color  circleColor = switch (severity) { case "danger" -> Color.web("#dc2626"); case "warning" -> Color.web("#d97706"); default -> Color.web("#6b7280"); };

        String timerLabel;
        if      (totalDays >= 365) timerLabel = (totalDays / 365) + "y " + ((totalDays % 365) / 30) + "mo";
        else if (totalDays >= 30)  timerLabel = (totalDays / 30) + " mo " + (totalDays % 30) + "d";
        else if (totalDays >= 1)   timerLabel = totalDays + " day" + (totalDays != 1 ? "s" : "");
        else                       timerLabel = "today";

        // Avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(38, 38); avatar.setMinSize(38, 38);
        Circle circle = new Circle(19, circleColor);
        Label initial = new Label(u.getEmail() != null ? String.valueOf(u.getEmail().charAt(0)).toUpperCase() : "?");
        initial.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:14px;");
        avatar.getChildren().addAll(circle, initial);

        // Info
        String displayName = (u.getFirstName() != null && !u.getFirstName().isBlank())
            ? u.getFirstName() + " " + (u.getLastName() != null ? u.getLastName() : "")
            : u.getEmail();
        Label nameLabel   = new Label(displayName.trim());
        nameLabel.setStyle("-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#1a1a2e;");
        Label emailLabel  = new Label(u.getEmail());
        emailLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#9ca3af;");
        String lastObjText = notif.lastObjectiveTitle() != null
            ? "📋 Last: «" + (notif.lastObjectiveTitle().length() > 22
                ? notif.lastObjectiveTitle().substring(0, 22) + "…" : notif.lastObjectiveTitle()) + "»"
            : "⚠️ No objective ever created";
        Label detailLabel = new Label(lastObjText);
        detailLabel.setStyle("-fx-font-size:10px;-fx-text-fill:#6b7280;");
        VBox info = new VBox(2, nameLabel, emailLabel, detailLabel);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Timer
        Label timer = new Label("⏱ " + timerLabel);
        timer.setStyle("-fx-background-color:" + timerBg + ";-fx-text-fill:" + timerFg +
                ";-fx-background-radius:6;-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:3 8;");
        String sinceStr = since != null ? since.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
        Label agoLabel = new Label("since " + sinceStr);
        agoLabel.setStyle("-fx-font-size:9px;-fx-text-fill:#b0b5bf;");
        VBox timerBox = new VBox(3, timer, agoLabel);
        timerBox.setAlignment(Pos.CENTER_RIGHT);

        // Signal button
        Button signalBtn = new Button("✉ Signal");
        String baseStyle  = "-fx-background-color:white;-fx-border-color:#e5e7eb;-fx-border-radius:6;-fx-background-radius:6;-fx-text-fill:#6b7280;-fx-font-size:10px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:4 10;";
        String hoverStyle = "-fx-background-color:#fef3c7;-fx-border-color:#f59e0b;-fx-border-radius:6;-fx-background-radius:6;-fx-text-fill:#92400e;-fx-font-size:10px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:4 10;";
        signalBtn.setStyle(baseStyle);
        signalBtn.setOnMouseEntered(e -> signalBtn.setStyle(hoverStyle));
        signalBtn.setOnMouseExited(e  -> signalBtn.setStyle(baseStyle));
        signalBtn.setOnAction(e -> handleSignal(notif, signalBtn));

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 16));
        if (addTopBorder) row.setStyle("-fx-border-color:#f3f4f6 transparent transparent transparent;");
        row.getChildren().addAll(avatar, info, timerBox, signalBtn);
        return row;
    }

    private void handleSignal(InactiveUserNotification notif, Button btn) {
        User u = notif.user();
        String name = (u.getFirstName() != null && !u.getFirstName().isBlank())
            ? u.getFirstName() + " " + (u.getLastName() != null ? u.getLastName() : "")
            : u.getEmail();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Send email reminder to " + name.trim() + "?\n\nAn alert email will be sent to the user.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("📧 Send Signal");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                btn.setText("📲 Sending…");
                btn.setDisable(true);
                new Thread(() -> {
                    boolean ok = emailService.sendSignal(name.trim(), u.getEmail(),
                            notif.lastObjectiveTitle(), notif.inactiveSince());
                    Platform.runLater(() -> {
                        Stage owner = (Stage) userTable.getScene().getWindow();
                        if (ok) {
                            Toasts.show(owner, "Signal sent to " + name.trim(), Toasts.Type.SUCCESS);
                            btn.setText("✅ Sent");
                        } else {
                            Toasts.show(owner, "Failed to send signal. Check SMTP config.", Toasts.Type.ERROR);
                            btn.setText("✉ Signal");
                            btn.setDisable(false);
                        }
                    });
                }).start();
            }
        });
    }

    private void setupColumns() {
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colFullName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));

        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Inactive"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.setStyle("-fx-background-color:" + ("Active".equals(s) ? "#DCFCE7" : "#FEE2E2") +
                        ";-fx-text-fill:" + ("Active".equals(s) ? "#166534" : "#991B1B") +
                        ";-fx-background-radius:6;-fx-padding:2 10;-fx-font-size:11px;-fx-font-weight:bold;");
                setGraphic(badge);
            }
        });

        colCreatedAt.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(FMT) : "—"));

        colActions.setCellFactory(col -> new TableCell<>() {

            // ── Row 1 : View · Edit · Toggle · Delete ──────────────────────
            private final Button btnView   = actionBtn("👁 View",      "#0EA5E9", "#E0F2FE");
            private final Button btnEdit   = actionBtn("✏️ Edit",      "#3B82F6", "#DBEAFE");
            private final Button btnToggle = actionBtn("⏸ Toggle",    "#8B5CF6", "#EDE9FE");
            private final Button btnDelete = actionBtn("🗑 Delete",    "#EF4444", "#FEE2E2");

            // ── Row 2 : Badges · Message · Progress · Gallery · FaceID · Objectives ──
            private final Button btnBadges     = actionBtn("🏆 Badges",     "#F59E0B", "#FEF3C7");
            private final Button btnMessage    = actionBtn("💬 Message",    "#10B981", "#D1FAE5");
            private final Button btnProgress   = actionBtn("📈 Progress",   "#06B6D4", "#CFFAFE");
            private final Button btnGallery    = actionBtn("🖼 Gallery",    "#EC4899", "#FCE7F3");
            private final Button btnFaceId     = actionBtn("🎭 Face ID",    "#1E293B", "#E2E8F0");
            private final Button btnObjectives = actionBtn("🎯 Objectives", "#2E7D32", "#DCFCE7");

            private final HBox row1 = new HBox(5, btnView, btnEdit, btnToggle, btnDelete);
            private final HBox row2 = new HBox(5, btnBadges, btnMessage, btnProgress, btnGallery, btnFaceId, btnObjectives);
            private final VBox box  = new VBox(4, row1, row2);

            {
                row1.setAlignment(Pos.CENTER_LEFT);
                row2.setAlignment(Pos.CENTER_LEFT);
                box.setStyle("-fx-padding: 4 2;");

                btnView.setOnAction(e       -> handleView(getTableRow().getItem()));
                btnEdit.setOnAction(e       -> handleEdit(getTableRow().getItem()));
                btnToggle.setOnAction(e     -> handleToggle(getTableRow().getItem()));
                btnDelete.setOnAction(e     -> handleDelete(getTableRow().getItem()));
                btnBadges.setOnAction(e     -> handleViewBadges(getTableRow().getItem()));
                btnMessage.setOnAction(e    -> handleSendMessage(getTableRow().getItem()));
                btnProgress.setOnAction(e   -> handleViewProgress(getTableRow().getItem()));
                btnGallery.setOnAction(e    -> handleViewGallery(getTableRow().getItem()));
                btnFaceId.setOnAction(e     -> handleFaceId(getTableRow().getItem()));
                btnObjectives.setOnAction(e -> handleViewObjectives(getTableRow().getItem()));
            }

            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                // Update Toggle label based on user state
                User u = getTableRow().getItem();
                if (u != null) {
                    btnToggle.setText(u.isActive() ? "⏸ Disable" : "▶ Enable");
                    String toggleBg  = u.isActive() ? "#FEF3C7" : "#D1FAE5";
                    String toggleClr = u.isActive() ? "#92400E" : "#065F46";
                    btnToggle.setStyle(btnToggle.getStyle()
                        .replaceAll("-fx-background-color:[^;]+", "-fx-background-color:" + toggleBg)
                        .replaceAll("-fx-text-fill:[^;]+",        "-fx-text-fill:" + toggleClr));
                }
                setGraphic(box);
            }
        });
    }

    private Button actionBtn(String text, String textColor, String bgColor) {
        Button b = new Button(text);
        String base = "-fx-background-color:" + bgColor + ";" +
                      "-fx-text-fill:" + textColor + ";" +
                      "-fx-font-size:11px;" +
                      "-fx-font-weight:bold;" +
                      "-fx-background-radius:6;" +
                      "-fx-padding:4 10;" +
                      "-fx-cursor:hand;" +
                      "-fx-border-color:transparent;";
        String hover = "-fx-background-color:" + textColor + ";" +
                       "-fx-text-fill:white;" +
                       "-fx-font-size:11px;" +
                       "-fx-font-weight:bold;" +
                       "-fx-background-radius:6;" +
                       "-fx-padding:4 10;" +
                       "-fx-cursor:hand;" +
                       "-fx-border-color:transparent;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e  -> b.setStyle(base));
        return b;
    }

    private void loadAll() {
        List<User> users = null;

        // Attempt 1: UserRepository (uses validated connection)
        try {
            users = repo.findAllSortedBy(sortCol, sortDir);
        } catch (Exception e) {
            System.err.println("[UserManagement] Attempt 1 failed: " + e.getMessage());
        }

        // Attempt 2: fallback via UserDAO if repo returned empty or threw
        if (users == null || users.isEmpty()) {
            try {
                users = new tn.esprit.projet.dao.UserDAO().findAll();
                System.out.println("[UserManagement] Fallback UserDAO → " + (users != null ? users.size() : 0) + " users");
            } catch (Exception e) {
                System.err.println("[UserManagement] Attempt 2 failed: " + e.getMessage());
            }
        }

        // Attempt 3: direct JDBC as last resort
        if (users == null || users.isEmpty()) {
            try {
                users = loadUsersDirectJdbc();
                System.out.println("[UserManagement] Direct JDBC → " + (users != null ? users.size() : 0) + " users");
            } catch (Exception e) {
                System.err.println("[UserManagement] Attempt 3 failed: " + e.getMessage());
                users = new java.util.ArrayList<>();
            }
        }

        System.out.println("[UserManagement] Final user count: " + users.size());
        data.setAll(users);
        updateCount();
    }

    /** Last-resort: open a fresh JDBC connection and query directly */
    private List<User> loadUsersDirectJdbc() throws Exception {
        List<User> list = new java.util.ArrayList<>();
        String url = tn.esprit.projet.utils.DatabaseConfig.getUrl();
        try (java.sql.Connection c = java.sql.DriverManager.getConnection(
                url,
                tn.esprit.projet.utils.DatabaseConfig.USER,
                tn.esprit.projet.utils.DatabaseConfig.PASSWORD);
             java.sql.PreparedStatement ps = c.prepareStatement("SELECT * FROM user ORDER BY id DESC");
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tn.esprit.projet.models.User u = new tn.esprit.projet.models.User();
                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setFirstName(rs.getString("first_name"));
                u.setLastName(rs.getString("last_name"));
                String role = rs.getString("roles");
                if (role != null && role.startsWith("[")) role = role.replaceAll("[\\[\\]\"\\s]", "");
                u.setRole(role != null ? role : "ROLE_USER");
                u.setActive(rs.getBoolean("is_active"));
                java.sql.Timestamp ca = rs.getTimestamp("created_at");
                if (ca != null) u.setCreatedAt(ca.toLocalDateTime());
                u.setPhotoFilename(rs.getString("photo_filename"));
                u.setWeight(rs.getDouble("weight"));
                u.setHeight(rs.getDouble("height"));
                try { u.setPhone(rs.getString("phone")); } catch (Exception ignored) {}
                list.add(u);
            }
        }
        return list;
    }

    private void updateCount() {
        if (lblCount != null) lblCount.setText(data.size() + " user(s)");
    }

    @FXML private void handleAddUser() { openForm(null); }
    
    @FXML
    private void handleAdminProfile() {
        User currentAdmin = Session.getCurrentUser();
        if (currentAdmin == null) {
            AlertUtil.show(AlertUtil.Type.ERROR, "Error", "No admin session found. Please log in again.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user_edit.fxml"));
            Parent root = loader.load();
            AdminUserEditController ctrl = loader.getController();
            ctrl.setUser(currentAdmin);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("My Profile — " + currentAdmin.getFullName());
            stage.setScene(new Scene(root, 620, 680));
            stage.setResizable(false);
            stage.showAndWait();
            loadAll();
        } catch (Exception e) { 
            e.printStackTrace();
            AlertUtil.show(AlertUtil.Type.ERROR, "Error", "Could not open profile editor: " + e.getMessage());
        }
    }

    private void handleView(User u) {
        if (u == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user_show.fxml"));
            Parent root = loader.load();
            AdminUserShowController ctrl = loader.getController();
            ctrl.setUser(u);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("User — " + u.getFullName());
            stage.setScene(new Scene(root, 560, 580));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleViewBadges(User u) {
        if (u == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user_badges.fxml"));
            Parent root = loader.load();
            AdminUserBadgesController ctrl = loader.getController();
            ctrl.setUser(u);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Badges — " + u.getFullName());
            stage.setScene(new Scene(root, 900, 700));
            stage.setResizable(true);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleSendMessage(User u) {
        if (u == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user_messages.fxml"));
            Parent root = loader.load();
            AdminUserMessagesController ctrl = loader.getController();
            ctrl.setUser(u);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Messages — " + u.getFullName());
            stage.setScene(new Scene(root, 800, 650));
            stage.setResizable(true);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleViewProgress(User u) {
        if (u == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user_progress.fxml"));
            Parent root = loader.load();
            AdminUserProgressController ctrl = loader.getController();
            ctrl.setUser(u);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Progress — " + u.getFullName());
            stage.setScene(new Scene(root, 900, 700));
            stage.setResizable(true);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleViewGallery(User u) {
        if (u == null) return;
        try {
            // Always fetch fresh from DB so gallery_access_enabled is current
            User fresh = new tn.esprit.projet.repository.UserRepository().findById(u.getId());
            if (fresh == null) fresh = u;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_user_gallery.fxml"));
            Parent root = loader.load();
            AdminUserGalleryController ctrl = loader.getController();
            ctrl.setUser(fresh);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Gallery — " + fresh.getFullName());
            stage.setScene(new Scene(root, 1000, 700));
            stage.setResizable(true);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleFaceId(User u) {
        if (u == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_face_id.fxml"));
            Parent root = loader.load();
            AdminFaceIdController ctrl = loader.getController();
            ctrl.setTargetUser(u);
            ctrl.setOnChanged(this::loadAll);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(userTable.getScene().getWindow());
            stage.setTitle("Face ID — " + u.getFullName());
            stage.setScene(new Scene(root, 480, 420));
            stage.setResizable(false);
            stage.showAndWait();
            loadAll();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleViewObjectives(User u) {
        if (u == null) return;
        // Navigate to the Objectives page in the admin sidebar
        if (parentController != null) {
            parentController.handleObjectivesForUser(u);
        } else {
            // Fallback: find AdminLayoutController from scene
            try {
                javafx.scene.Node node = userTable;
                while (node.getParent() != null) node = node.getParent();
                // The root is the BorderPane of admin_layout.fxml
                // Get its controller via lookup is not possible directly,
                // so we use the contentArea's parent chain
                javafx.scene.layout.StackPane contentArea =
                    (javafx.scene.layout.StackPane) userTable.getScene().lookup("#contentArea");
                if (contentArea != null) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_objectives.fxml"));
                    Parent page = loader.load();
                    AdminObjectivesController ctrl = loader.getController();
                    ctrl.setUserFilter(u);
                    contentArea.getChildren().setAll(page);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleEdit(User u) { if (u != null) openForm(u); }

    private void handleToggle(User u) {
        if (u == null) return;
        boolean newState = !u.isActive();
        String msg = (newState ? "Activate" : "Deactivate") + " " + u.getFullName() + "?";
        boolean confirmed = AlertUtil.confirm(newState ? "Activate User" : "Deactivate User", msg);
        if (confirmed) {
            repo.setActive(u.getId(), newState);
            loadAll();
            Stage owner = (Stage) userTable.getScene().getWindow();
            Toasts.show(owner, "User has been " + (newState ? "activated" : "deactivated") + " successfully!",
                    newState ? Toasts.Type.SUCCESS : Toasts.Type.INFO);
        }
    }

    private void handleDelete(User u) {
        if (u == null) return;
        if (Session.getCurrentUser() != null && Session.getCurrentUser().getId() == u.getId()) {
            AlertUtil.show(AlertUtil.Type.ERROR, "Not Allowed", "You cannot delete your own account!");
            return;
        }
        boolean confirmed = AlertUtil.confirm("Delete User",
                "Are you sure you want to delete " + u.getFullName() + "? This action cannot be undone.");
        if (confirmed) {
            repo.delete(u.getId());
            loadAll();
            Stage owner = (Stage) userTable.getScene().getWindow();
            Toasts.show(owner, "User deleted successfully!", Toasts.Type.SUCCESS);
        }
    }

    private void openForm(User user) {
        try {
            String fxml = user == null ? "admin_user_new.fxml" : "admin_user_edit.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
            Parent root = loader.load();
            if (user != null) {
                AdminUserEditController ctrl = loader.getController();
                ctrl.setUser(user);
            }
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(user == null ? "Create New User" : "Edit — " + user.getFullName());
            stage.setScene(new Scene(root, 620, 680));
            stage.setResizable(false);
            stage.showAndWait();
            loadAll();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String colToField(String colId) {
        return switch (colId != null ? colId : "") {
            case "colEmail"     -> "email";
            case "colFullName"  -> "first_name";
            case "colRole"      -> "roles";
            case "colStatus"    -> "is_active";
            case "colCreatedAt" -> "created_at";
            default             -> "id";
        };
    }
}
