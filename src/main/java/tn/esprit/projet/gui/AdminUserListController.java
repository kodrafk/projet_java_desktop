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
import tn.esprit.projet.utils.Session;
import tn.esprit.projet.utils.Toasts;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
            private final Button btnView   = btn("View",          "#06B6D4");
            private final Button btnEdit   = btn("Edit",          "#10B981");
            private final Button btnToggle = btn("Toggle Active", "#8B5CF6");
            private final Button btnDelete = btn("Delete",        "#EF4444");
            private final HBox   box       = new HBox(6, btnView, btnEdit, btnToggle, btnDelete);
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
        data.setAll(repo.findAllSortedBy(sortCol, sortDir));
        updateCount();
    }

    private void updateCount() {
        if (lblCount != null) lblCount.setText(data.size() + " user(s)");
    }

    @FXML private void handleAddUser() { openForm(null); }

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

    private void handleEdit(User u) { if (u != null) openForm(u); }

    private void handleToggle(User u) {
        if (u == null) return;
        boolean newState = !u.isActive();
        String msg = (newState ? "Activate" : "Deactivate") + " this user?";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(newState ? "Activate User" : "Deactivate User");
        alert.setContentText(msg);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
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
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Not Allowed");
            err.setContentText("You cannot delete your own account!");
            err.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setContentText("Are you sure you want to delete " + u.getFullName() + "? This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
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
