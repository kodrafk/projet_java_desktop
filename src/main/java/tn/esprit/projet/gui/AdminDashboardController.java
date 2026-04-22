package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Session;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Label     lblAdminName;
    @FXML private Label     lblAdminEmail;
    @FXML private Label     lblAdminAvatar;
    @FXML private Label     lblTotalUsers;
    @FXML private Label     lblActiveUsers;
    @FXML private Label     lblInactiveUsers;
    @FXML private Label     lblAdmins;

    @FXML private Button btnDashboard;
    @FXML private Button btnUsers;
    @FXML private Button btnStatistics;

    private final UserRepository repo = new UserRepository();

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
        User admin = Session.getCurrentUser();
        if (admin != null) {
            set(lblAdminName, "Hello, " + admin.getFirstName() + " 👋");
            set(lblAdminEmail, admin.getEmail());
            if (lblAdminAvatar != null && admin.getFirstName() != null)
                lblAdminAvatar.setText(String.valueOf(admin.getFirstName().charAt(0)).toUpperCase());
        }
        loadStats();
        if (btnDashboard != null) btnDashboard.setStyle(BTN_ACTIVE);
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
    }

    @FXML private void handleUsers() {
        activate(btnUsers);
        loadPage("admin_user_list.fxml");
    }

    @FXML private void handleStatistics() {
        activate(btnStatistics);
        loadPage("statistics.fxml");
    }

    @FXML private void handleLogout() {
        Session.logout();
        Stage stage = (Stage) contentArea.getScene().getWindow();
        Nav.go(stage, "login.fxml", "NutriLife - Login", 1100, 720, false);
    }

    private void activate(Button active) {
        for (Button b : new Button[]{btnDashboard, btnUsers, btnStatistics})
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
