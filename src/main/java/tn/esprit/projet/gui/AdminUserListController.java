package tn.esprit.projet.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.AlertUtil;
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminUserListController {

    @FXML private TextField                 searchField;
    @FXML private TableView<User>           userTable;
    @FXML private TableColumn<User, String> colId;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, String> colCreatedAt;
    @FXML private TableColumn<User, Void>   colActions;
    @FXML private Label                     lblCount;
    @FXML private Button                    btnAdminProfile;

    private final UserRepository repo = new UserRepository();
    private final ObservableList<User> data = FXCollections.observableArrayList();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Sort state
    private String sortCol = "id";
    private String sortDir = "DESC";

    @FXML
    public void initialize() {
        setupColumns();
        userTable.setItems(data);
        loadAll();
        
        // Setup admin profile button tooltip
        if (btnAdminProfile != null) {
            Tooltip tooltip = new Tooltip("View/Edit Admin Profile");
            Tooltip.install(btnAdminProfile, tooltip);
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

    private void setupColumns() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
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
            private final Button btnView     = btn("View",     "#06B6D4");
            private final Button btnBadges   = btn("Badges",   "#7C3AED");
            private final Button btnMessage  = btn("Message",  "#F59E0B");
            private final Button btnProgress = btn("Progress", "#10B981");
            private final Button btnGallery  = btn("Gallery",  "#EC4899");
            private final Button btnFaceId   = btn("FaceID",   "#0F2820");
            private final Button btnEdit     = btn("Edit",     "#3B82F6");
            private final Button btnToggle   = btn("Toggle",   "#8B5CF6");
            private final Button btnDelete   = btn("Delete",   "#EF4444");
            private final HBox   box         = new HBox(4, btnView, btnBadges, btnMessage, btnProgress, btnGallery, btnFaceId, btnEdit, btnToggle, btnDelete);
            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnView.setOnAction(e     -> handleView(getTableRow().getItem()));
                btnBadges.setOnAction(e   -> handleViewBadges(getTableRow().getItem()));
                btnMessage.setOnAction(e  -> handleSendMessage(getTableRow().getItem()));
                btnProgress.setOnAction(e -> handleViewProgress(getTableRow().getItem()));
                btnGallery.setOnAction(e  -> handleViewGallery(getTableRow().getItem()));
                btnFaceId.setOnAction(e   -> handleFaceId(getTableRow().getItem()));
                btnEdit.setOnAction(e     -> handleEdit(getTableRow().getItem()));
                btnToggle.setOnAction(e   -> handleToggle(getTableRow().getItem()));
                btnDelete.setOnAction(e   -> handleDelete(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + "22;-fx-text-fill:" + color +
                ";-fx-font-size:11px;-fx-background-radius:6;-fx-padding:3 8;-fx-cursor:hand;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:" + color +
                ";-fx-text-fill:white;-fx-font-size:11px;-fx-background-radius:6;-fx-padding:3 8;-fx-cursor:hand;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color:" + color + "22;-fx-text-fill:" + color +
                ";-fx-font-size:11px;-fx-background-radius:6;-fx-padding:3 8;-fx-cursor:hand;"));
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
