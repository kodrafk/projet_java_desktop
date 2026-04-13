package tn.esprit.projet.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.dao.UserDAO;
import tn.esprit.projet.models.User;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.SessionManager;
import tn.esprit.projet.utils.Toast;

import java.io.File;

public class UserListController {

    @FXML private TableView<User>           tableUsers;
    @FXML private TableColumn<User, String> colProfile;
    @FXML private TableColumn<User, String> colRoles;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void>   colActions;
    @FXML private TextField                 searchField;
    @FXML private Label                     lblCount;

    private final UserDAO dao = new UserDAO();
    private final ObservableList<User> masterList = FXCollections.observableArrayList();
    private FilteredList<User> filtered;

    @FXML
    public void initialize() {
        setupColumns();
        loadData();

        filtered = new FilteredList<>(masterList, u -> true);
        SortedList<User> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tableUsers.comparatorProperty());
        tableUsers.setItems(sorted);

        // Live search on type
        searchField.textProperty().addListener((obs, old, val) -> applyFilter(val));

        // Row style: inactive = light red
        tableUsers.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(User u, boolean empty) {
                super.updateItem(u, empty);
                setStyle((!empty && u != null && !u.isActive())
                        ? "-fx-background-color: #FFF5F5;"
                        : "");
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
            private final Button btnView   = iconBtn("👁",  "#06B6D4", "View");
            private final Button btnEdit   = iconBtn("✏",  "#10B981", "Edit");
            private final Button btnToggle = iconBtn("⚙",  "#8B5CF6", "Toggle");
            private final Button btnDelete = iconBtn("🗑", "#EF4444", "Delete");
            private final HBox   box       = new HBox(4, btnStats, btnPhoto, btnPwd, btnView, btnEdit, btnToggle, btnDelete);
            {
                box.setAlignment(Pos.CENTER_LEFT);
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
