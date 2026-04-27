package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.models.UserGroup;
import tn.esprit.projet.repository.UserGroupRepository;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.ProfessionalWelcomeNotification;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Label     lblAdminName;
    @FXML private Label     lblAdminEmail;
    @FXML private Label     lblAdminAvatar;
    @FXML private Label     lblTotalUsers;
    @FXML private Label     lblActiveUsers;
    @FXML private Label     lblInactiveUsers;
    @FXML private Label     lblAdmins;
    // Groups widget
    @FXML private HBox      dashGroupCards;
    @FXML private Label     lblDashGroupCount;
    @FXML private Label     lblDashNoGroups;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnGroups;
    @FXML private Button btnStatistics;
    @FXML private Button btnAnomalyDetection;

    private final UserRepository      repo      = new UserRepository();
    private final UserGroupRepository groupRepo = new UserGroupRepository();
    private User currentAdmin;

    private static final String BTN_DEFAULT =
            "-fx-background-color:transparent;-fx-text-fill:#A8C4B8;-fx-font-size:12.5px;" +
            "-fx-background-radius:10;-fx-cursor:hand;-fx-alignment:CENTER_LEFT;-fx-padding:0 0 0 14;";
    private static final String BTN_ACTIVE =
            "-fx-background-color:#2E7D5A;-fx-text-fill:white;-fx-font-size:12.5px;-fx-font-weight:bold;" +
            "-fx-background-radius:10;-fx-cursor:hand;-fx-alignment:CENTER_LEFT;-fx-padding:0 0 0 14;";

    @FXML
    public void initialize() {
        if (!Session.isAdmin()) {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Nav.go(stage, "login.fxml", "NutriLife - Login");
            return;
        }
        
        currentAdmin = Session.getCurrentUser();
        
        if (lblAdminName != null && currentAdmin != null) {
            lblAdminName.setText("Hello, " + currentAdmin.getFirstName() + " 👋");
        }
        
        loadStats();
        loadDashboardGroups();
        if (btnDashboard != null) btnDashboard.setStyle(BTN_ACTIVE);
        
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            ProfessionalWelcomeNotification.show(stage, currentAdmin);
        });
    }

    private void loadStats() {
        set(lblTotalUsers,    String.valueOf(repo.countAll()));
        set(lblActiveUsers,   String.valueOf(repo.countActive()));
        set(lblInactiveUsers, String.valueOf(repo.countInactive()));
        set(lblAdmins,        String.valueOf(repo.countAdmins()));
    }

    @FXML private void handleDashboard() {
        activate(btnDashboard);
        loadStats();
        loadDashboardGroups();
    }

    @FXML private void handleUsers() {
        activate(btnUsers);
        loadPage("admin_user_list.fxml");
    }

    @FXML private void handleGroups() {
        activate(btnGroups);
        loadPage("admin_groups.fxml");
    }

    @FXML private void handleCreateGroupFromDash() {
        // Go to groups page — the create dialog will open automatically
        activate(btnGroups);
        loadPage("admin_groups.fxml");
    }

    // ── Groups widget ─────────────────────────────────────────────────────────

    private void loadDashboardGroups() {
        if (dashGroupCards == null || currentAdmin == null) return;
        groupRepo.ensureTablesExist();

        List<UserGroup> groups = groupRepo.findByAdmin(currentAdmin.getId());
        dashGroupCards.getChildren().clear();

        if (lblDashGroupCount != null)
            lblDashGroupCount.setText(groups.size() + " group" + (groups.size() != 1 ? "s" : ""));

        if (groups.isEmpty()) {
            if (lblDashNoGroups != null) {
                lblDashNoGroups.setVisible(true); lblDashNoGroups.setManaged(true);
                dashGroupCards.getChildren().add(lblDashNoGroups);
            }
            return;
        }

        if (lblDashNoGroups != null) { lblDashNoGroups.setVisible(false); lblDashNoGroups.setManaged(false); }

        // Show up to 6 group cards
        for (UserGroup g : groups.subList(0, Math.min(groups.size(), 6))) {
            dashGroupCards.getChildren().add(buildGroupCard(g));
        }
    }

    private VBox buildGroupCard(UserGroup g) {
        int memberCount = groupRepo.getMemberIds(g.getId()).size();

        VBox card = new VBox(10);
        card.setPrefWidth(160);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:" + g.getColor() + "44;" +
            "-fx-border-radius:12;" +
            "-fx-border-width:2;" +
            "-fx-cursor:hand;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");

        // Avatar circle
        StackPane avatar = new StackPane();
        avatar.setPrefSize(44, 44);
        avatar.setMaxSize(44, 44);
        avatar.setStyle("-fx-background-color:" + g.getColor() + ";-fx-background-radius:22;");
        Label initial = new Label(g.getName().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:white;");
        avatar.getChildren().add(initial);

        // Name
        Label name = new Label(g.getName());
        name.setWrapText(true);
        name.setMaxWidth(140);
        name.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#1E293B;");

        // Member count
        Label members = new Label("👤 " + memberCount + " member" + (memberCount != 1 ? "s" : ""));
        members.setStyle("-fx-font-size:11px;-fx-text-fill:#64748B;");

        // Open button
        Button open = new Button("Open →");
        open.setMaxWidth(Double.MAX_VALUE);
        open.setStyle(
            "-fx-background-color:" + g.getColor() + ";" +
            "-fx-text-fill:white;" +
            "-fx-font-size:11px;" +
            "-fx-font-weight:bold;" +
            "-fx-background-radius:7;" +
            "-fx-cursor:hand;" +
            "-fx-border-color:transparent;" +
            "-fx-padding:5 0;");

        open.setOnAction(e -> {
            activate(btnGroups);
            loadPage("admin_groups.fxml");
        });

        card.getChildren().addAll(avatar, name, members, open);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color:#F8FAFC;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:" + g.getColor() + ";" +
            "-fx-border-radius:12;" +
            "-fx-border-width:2;" +
            "-fx-cursor:hand;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.12),12,0,0,4);"));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:12;" +
            "-fx-border-color:" + g.getColor() + "44;" +
            "-fx-border-radius:12;" +
            "-fx-border-width:2;" +
            "-fx-cursor:hand;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);"));

        card.setOnMouseClicked(e -> {
            activate(btnGroups);
            loadPage("admin_groups.fxml");
        });

        return card;
    }

    @FXML private void handleStatistics() {
        activate(btnStatistics);
        loadPage("statistics.fxml");
    }

    @FXML private void handleAnomalyDetection() {
        activate(btnAnomalyDetection);
        loadPage("admin_anomaly_dashboard.fxml");
    }
    
    // Hidden handlers
    /*
    @FXML private void handlePersonalizedMessages() {
        activate(btnPersonalizedMessages);
        loadPage("admin_personalized_messages.fxml");
    }
    
    @FXML private void handleAlerts() {
        activate(btnAlerts);
        loadPage("admin_alerts.fxml");
    }
    */

    @FXML private void handleLogout() {
        Session.logout();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login", 1100, 720, false);
    }

    private void activate(Button active) {
        for (Button b : new Button[]{btnDashboard, btnUsers, btnGroups, btnStatistics, btnAnomalyDetection})
            if (b != null) b.setStyle(BTN_DEFAULT);
        if (active != null) active.setStyle(BTN_ACTIVE);
    }

    private void loadPage(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/fxml/" + fxml));
            contentArea.getChildren().setAll(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val); }
}
