package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatisticsController {

    // KPI labels
    @FXML private Label kpiTotal;
    @FXML private Label kpiActive;
    @FXML private Label kpiInactive;
    @FXML private Label kpiAdmins;
    @FXML private Label kpiNewMonth;

    // Bar chart
    @FXML private StackPane barUser;
    @FXML private StackPane barAdmin;
    @FXML private StackPane barActive;
    @FXML private StackPane barInactive;
    @FXML private Label     lblBarUser;
    @FXML private Label     lblBarAdmin;
    @FXML private Label     lblBarActive;
    @FXML private Label     lblBarInactive;

    // Recent list
    @FXML private VBox recentList;

    // Progress bar
    @FXML private StackPane progressActive;
    @FXML private StackPane progressInactive;
    @FXML private Label     lblProgressActive;
    @FXML private Label     lblProgressInactive;
    @FXML private Label     lblLegendActive;
    @FXML private Label     lblLegendInactive;

    private final UserDAO dao = new UserDAO();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        int total    = dao.countAll();
        int active   = dao.countActive();
        int inactive = total - active;
        int admins   = dao.countByRole("ROLE_ADMIN");
        int users    = dao.countByRole("ROLE_USER");
        int newMonth = dao.countRegisteredThisMonth();

        // KPIs
        set(kpiTotal,    String.valueOf(total));
        set(kpiActive,   String.valueOf(active));
        set(kpiInactive, String.valueOf(inactive));
        set(kpiAdmins,   String.valueOf(admins));
        set(kpiNewMonth, String.valueOf(newMonth));

        // Bar chart — max bar width ~300px
        double max = Math.max(total, 1);
        double maxW = 300.0;

        setBar(barUser,     lblBarUser,     users,    max, maxW);
        setBar(barAdmin,    lblBarAdmin,    admins,   max, maxW);
        setBar(barActive,   lblBarActive,   active,   max, maxW);
        setBar(barInactive, lblBarInactive, inactive, max, maxW);

        // Progress bar (active vs inactive)
        if (total > 0) {
            double activePct   = (active   * 100.0) / total;
            double inactivePct = (inactive * 100.0) / total;
            progressActive.setPrefWidth(activePct   * 8); // scale to ~800px total
            progressInactive.setPrefWidth(inactivePct * 8);
            set(lblProgressActive,   String.format("%.0f%%", activePct));
            set(lblProgressInactive, String.format("%.0f%%", inactivePct));
            set(lblLegendActive,   String.format("Active (%.0f%%)", activePct));
            set(lblLegendInactive, String.format("Inactive (%.0f%%)", inactivePct));
        }

        // Recent registrations
        List<User> all = dao.findAll(); // already ordered DESC
        int limit = Math.min(5, all.size());
        for (int i = 0; i < limit; i++) {
            User u = all.get(i);
            recentList.getChildren().add(buildRecentRow(u));
        }
    }

    private HBox buildRecentRow(User u) {
        // Avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(34, 34);
        avatar.setStyle("-fx-background-color: #2E7D5A; -fx-background-radius: 17;");
        Label init = new Label(u.getFirstName() != null && !u.getFirstName().isEmpty()
                ? String.valueOf(u.getFirstName().charAt(0)).toUpperCase() : "?");
        init.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        avatar.getChildren().add(init);

        if (u.getPhotoFilename() != null && !u.getPhotoFilename().isBlank()) {
            File f = new File("uploads/profiles/" + u.getPhotoFilename());
            if (f.exists()) {
                ImageView iv = new ImageView(new Image(f.toURI().toString()));
                iv.setFitWidth(34); iv.setFitHeight(34); iv.setPreserveRatio(true);
                avatar.getChildren().add(iv);
                init.setVisible(false);
            }
        }

        // Info
        VBox info = new VBox(2);
        Label name = new Label(u.getFullName());
        name.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        Label email = new Label(u.getEmail() != null ? u.getEmail() : "");
        email.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");
        info.getChildren().addAll(name, email);

        // Date
        Label date = new Label(u.getCreatedAt() != null ? u.getCreatedAt().format(FMT) : "");
        date.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

        // Role badge
        boolean isAdmin = u.getRoles() != null && u.getRoles().contains("ROLE_ADMIN");
        Label role = new Label(isAdmin ? "ADMIN" : "USER");
        role.setStyle("-fx-background-color: " + (isAdmin ? "#EDE9FE" : "#DCFCE7") + "; " +
                "-fx-text-fill: " + (isAdmin ? "#7C3AED" : "#166534") + "; " +
                "-fx-background-radius: 5; -fx-padding: 2 8; -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox row = new HBox(10, avatar, info);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        HBox full = new HBox(10, row, date, role);
        full.setAlignment(Pos.CENTER_LEFT);
        full.setStyle("-fx-background-color: #F8FAFB; -fx-background-radius: 8; -fx-padding: 8 12;");
        HBox.setHgrow(row, javafx.scene.layout.Priority.ALWAYS);
        return full;
    }

    private void setBar(StackPane bar, Label lbl, int value, double max, double maxW) {
        if (bar != null) bar.setPrefWidth(Math.max(4, (value / max) * maxW));
        set(lbl, String.valueOf(value));
    }

    private void set(Label lbl, String val) { if (lbl != null) lbl.setText(val); }
}
