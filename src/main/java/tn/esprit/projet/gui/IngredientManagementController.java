package tn.esprit.projet.gui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import tn.esprit.projet.models.Ingredient;
import tn.esprit.projet.services.IngredientService;

import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class IngredientManagementController implements Initializable {

    // Stats
    @FXML private Label lblTotalCount;
    @FXML private Label lblExpiringSoon;
    @FXML private Label lblExpired;
    @FXML private Label lblCategoriesCount;
    @FXML private Label lblLowStock;
    @FXML private Label lblOutOfStock;
    @FXML private Label lblTableInfo;

    // Search & Filters
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterCategory;
    @FXML private ComboBox<String> cmbSortBy;

    // Table
    @FXML private TableView<Ingredient> tableIngredients;
    @FXML private TableColumn<Ingredient, Boolean> colSelect;
    @FXML private TableColumn<Ingredient, String> colNom;
    @FXML private TableColumn<Ingredient, String> colCategorie;
    @FXML private TableColumn<Ingredient, String> colQuantite;
    @FXML private TableColumn<Ingredient, LocalDate> colDatePeremption;
    @FXML private TableColumn<Ingredient, String> colStatus;
    @FXML private TableColumn<Ingredient, String> colNotes;
    @FXML private TableColumn<Ingredient, Void> colActions;

    // Modal
    @FXML private StackPane formOverlay;
    @FXML private Label lblFormTitle;
    @FXML private Label lblError;

    // Required fields
    @FXML private TextField txtNom;
    @FXML private Label lblNomError;
    @FXML private ComboBox<String> cmbCategorie;
    @FXML private Label lblCategorieError;
    @FXML private TextField txtQuantite;
    @FXML private Label lblQuantiteError;

    // Optional fields
    @FXML private ComboBox<String> cmbUnite;
    @FXML private DatePicker datePeremption;
    @FXML private Label lblDateError;
    @FXML private TextField txtImage;
    @FXML private TextArea txtNotes;

    @FXML private Button btnSaveIngredient;

    private IngredientService ingredientService;
    private FilteredList<Ingredient> filteredIngredients;
    private Ingredient currentIngredient;

    // Name: letters, spaces, apostrophes, hyphens only
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} '\\-]+$");

    private static final List<String> CATEGORIES = Arrays.asList(
            "Vegetables", "Fruits", "Meats & Fish",
            "Dairy Products", "Grocery Store",
            "Frozen", "Drinks", "Condiments"
    );

    private static final List<String> UNITS = Arrays.asList(
            "g", "kg", "ml", "l", "pcs"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ingredientService = new IngredientService();
        setupComboBoxes();
        setupTableColumns();
        loadTableData();
        updateStats();
        setupSearchAndFilters();
    }

    private void setupComboBoxes() {
        if (cmbFilterCategory != null) {
            // Strip emojis for the filter display to avoid rendering issues
            cmbFilterCategory.setItems(FXCollections.observableArrayList(CATEGORIES));
            cmbFilterCategory.getItems().add(0, "All");
            cmbFilterCategory.setValue("All");
            cmbFilterCategory.setOnAction(e -> applyFilters());
        }
        if (cmbSortBy != null) {
            cmbSortBy.setItems(FXCollections.observableArrayList(
                    "Name (A-Z)", "Name (Z-A)", "Expiry Date", "Quantity"
            ));
            cmbSortBy.setValue("Name (A-Z)");
            cmbSortBy.setOnAction(e -> applyFilters());
        }
        if (cmbCategorie != null) {
            cmbCategorie.setItems(FXCollections.observableArrayList(CATEGORIES));
        }
        if (cmbUnite != null) {
            cmbUnite.setItems(FXCollections.observableArrayList(UNITS));
        }
        // Disable past dates in the DatePicker
        if (datePeremption != null) {
            datePeremption.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(LocalDate.now()));
                }
            });
        }
    }

    private String getCategoryName(String category) {
        return category;
    }
    private void setupTableColumns() {
        if (tableIngredients == null) return;
        
        // Selection column (if present)
        if (colSelect != null) {
            colSelect.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
            colSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colSelect));
            colSelect.setEditable(true);
            tableIngredients.setEditable(true);
        }

        // Name column
        if (colNom != null) {
            colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
            colNom.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
                    }
                }
            });
        }

        // Category column
        if (colCategorie != null) {
            colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
            colCategorie.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                        setStyle("-fx-text-fill: #475569;");
                    }
                }
            });
        }

        if (colQuantite != null) {
            colQuantite.setCellValueFactory(cellData -> {
                Ingredient i = cellData.getValue();
                String unit = i.getUnite() != null ? " " + i.getUnite() : "";
                return new SimpleStringProperty(i.getQuantite() + unit);
            });
        }

        if (colDatePeremption != null) {
            colDatePeremption.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("—");
                        setStyle("-fx-text-fill: #94A3B8;");
                    } else {
                        setText(item.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        setStyle("-fx-text-fill: #475569;");
                    }
                }
            });
            colDatePeremption.setCellValueFactory(new PropertyValueFactory<>("datePeremption"));
        }

        if (colStatus != null) {
            colStatus.setCellValueFactory(this::getStatusCell);
            colStatus.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        Label label = new Label(item);
                        String style = "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 6; ";
                        if (item.contains("Expiré") || item.contains("Expired")) {
                            style += "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
                        } else if (item.contains("restant") || item.contains("Soon")) {
                            style += "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;";
                        } else {
                            style += "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;";
                        }
                        label.setStyle(style);
                        setGraphic(label);
                    }
                }
            });
        }

        if (colNotes != null) {
            colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        }

        if (colActions != null) {
            colActions.setCellFactory(param -> new TableCell<>() {
                private final Button editBtn = new Button("✏");
                private final Button deleteBtn = new Button("🗑");
                private final HBox buttons = new HBox(8, editBtn, deleteBtn);
                {
                    buttons.setAlignment(javafx.geometry.Pos.CENTER);
                    editBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-size: 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                    deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-size: 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                    editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                    deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttons);
                    }
                }
            });
        }
    }

    private SimpleStringProperty getStatusCell(TableColumn.CellDataFeatures<Ingredient, String> cellData) {
        Ingredient i = cellData.getValue();
        if (i.getDatePeremption() == null) return new SimpleStringProperty("— Aucune date");
        
        LocalDate today = LocalDate.now();
        if (i.getDatePeremption().isBefore(today)) return new SimpleStringProperty("❌ Expiré");
        
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, i.getDatePeremption());
        if (daysRemaining <= 3) return new SimpleStringProperty("⚠️ " + daysRemaining + "j restant(s)");
        
        return new SimpleStringProperty("✅ " + daysRemaining + " jours");
    }

    private void loadTableData() {
        List<Ingredient> all = ingredientService.getAll();
        filteredIngredients = new FilteredList<>(FXCollections.observableArrayList(all), p -> true);
        applyFilters();
    }

    private void setupSearchAndFilters() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, old, newVal) -> applyFilters());
        }
    }

    @FXML
    public void applyFilters(ActionEvent event) {
        applyFilters();
    }

    private void applyFilters() {
        if (filteredIngredients == null) return;

        String searchText = txtSearch.getText() != null ? txtSearch.getText().toLowerCase() : "";
        String selectedCategory = cmbFilterCategory.getValue();
        String selectedSort = cmbSortBy.getValue();

        filteredIngredients.setPredicate(ingredient -> {
            // Search filter
            if (!searchText.isEmpty()) {
                boolean matchName = ingredient.getNom() != null && ingredient.getNom().toLowerCase().contains(searchText);
                if (!matchName) return false;
            }
            // Category filter
            if (selectedCategory != null && !selectedCategory.equals("All")) {
                String ingredientCat = ingredient.getCategorie() != null ? ingredient.getCategorie().toLowerCase() : "";
                if (!ingredientCat.contains(selectedCategory.toLowerCase())) return false;
            }
            return true;
        });

        SortedList<Ingredient> sortedList = new SortedList<>(filteredIngredients);
        if (selectedSort != null) {
            switch (selectedSort) {
                case "Name (A-Z)" -> sortedList.setComparator(Comparator.comparing(Ingredient::getNom, Comparator.nullsLast(Comparator.naturalOrder())));
                case "Name (Z-A)" -> sortedList.setComparator(Comparator.comparing(Ingredient::getNom, Comparator.nullsLast(Comparator.reverseOrder())));
                case "Expiry Date" -> sortedList.setComparator(Comparator.comparing(Ingredient::getDatePeremption, Comparator.nullsLast(Comparator.naturalOrder())));
                case "Quantity" -> sortedList.setComparator(Comparator.comparingDouble(Ingredient::getQuantite));
                default -> sortedList.setComparator(Comparator.comparing(Ingredient::getNom, Comparator.nullsLast(Comparator.naturalOrder())));
            }
        }

        tableIngredients.setItems(sortedList);
        lblTableInfo.setText("Showing " + sortedList.size() + " ingredient(s)");
    }

    private void updateStats() {
        List<Ingredient> all = ingredientService.getAll();
        LocalDate today = LocalDate.now();

        lblTotalCount.setText(String.valueOf(all.size()));
        lblExpiringSoon.setText(String.valueOf(all.stream()
                .filter(i -> i.getDatePeremption() != null)
                .filter(i -> !i.getDatePeremption().isBefore(today) && i.getDatePeremption().isBefore(today.plusDays(4)))
                .count()));
        lblExpired.setText(String.valueOf(all.stream()
                .filter(i -> i.getDatePeremption() != null && i.getDatePeremption().isBefore(today))
                .count()));
        lblCategoriesCount.setText(String.valueOf(all.stream()
                .map(i -> i.getCategorie() != null ? i.getCategorie().toLowerCase() : "")
                .distinct().count()));
        
        if (lblOutOfStock != null) {
            lblOutOfStock.setText(String.valueOf(all.stream()
                    .filter(i -> i.getQuantite() <= 0)
                    .count()));
        }
        
        if (lblLowStock != null) {
            lblLowStock.setText(String.valueOf(all.stream()
                    .filter(i -> i.getQuantite() > 0 && isLowStock(i))
                    .count()));
        }
    }

    private boolean isLowStock(Ingredient i) {
        if (i.getUnite() == null) return i.getQuantite() <= 3;
        String unit = i.getUnite().toLowerCase();
        if (unit.equals("pcs")) return i.getQuantite() <= 3;
        if (unit.equals("g") || unit.equals("ml")) return i.getQuantite() <= 100;
        if (unit.equals("kg") || unit.equals("l")) return i.getQuantite() <= 0.5;
        return i.getQuantite() <= 3;
    }

    @FXML
    public void handleAddNew(ActionEvent event) {
        currentIngredient = null;
        lblFormTitle.setText("Add Ingredient");
        clearForm();
        showModal();
    }

    private void handleEdit(Ingredient ingredient) {
        currentIngredient = ingredient;
        lblFormTitle.setText("Edit Ingredient");
        fillForm(ingredient);
        showModal();
    }

    private void handleDelete(Ingredient ingredient) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete ingredient?");
        alert.setContentText("Are you sure you want to delete \"" + ingredient.getNom() + "\"?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ingredientService.supprimer(ingredient.getId());
                loadTableData();
                updateStats();
                showInfoDialog("Ingredient deleted successfully.");
            }
        });
    }

    @FXML
    public void handleCloseForm(ActionEvent event) {
        if (formOverlay != null) {
            formOverlay.setVisible(false);
            formOverlay.setManaged(false);
        }
    }

    @FXML
    public void handleSaveIngredient(ActionEvent event) {
        clearFieldErrors();
        if (!validateForm()) return;

        Ingredient ingredient = (currentIngredient != null) ? currentIngredient : new Ingredient();
        ingredient.setNom(txtNom.getText().trim());
        ingredient.setNomEn(null);
        ingredient.setCategorie(cmbCategorie.getValue());
        ingredient.setQuantite(Double.parseDouble(txtQuantite.getText().trim()));
        ingredient.setUnite(cmbUnite.getValue());
        ingredient.setDatePeremption(datePeremption.getValue());
        ingredient.setNotes(txtNotes.getText().trim());
        ingredient.setImage(txtImage.getText().trim());

        if (currentIngredient != null) {
            ingredientService.modifier(ingredient);
            handleCloseForm(null);
            loadTableData();
            updateStats();
            showInfoDialog("Ingredient updated successfully.");
        } else {
            ingredientService.ajouter(ingredient);
            handleCloseForm(null);
            loadTableData();
            updateStats();
            showInfoDialog("Ingredient added successfully.");
        }
    }

    /** Returns true if all fields are valid, shows inline errors otherwise. */
    private boolean validateForm() {
        boolean valid = true;

        // Name: required, letters/spaces/apostrophes/hyphens only
        String nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            showFieldError(lblNomError, txtNom, "Name is required.");
            valid = false;
        } else if (!NAME_PATTERN.matcher(nom).matches()) {
            showFieldError(lblNomError, txtNom, "Only letters, spaces, apostrophes and hyphens allowed.");
            valid = false;
        }

        // Category: required
        if (cmbCategorie.getValue() == null) {
            showFieldError(lblCategorieError, null, "Category is required.");
            valid = false;
        }

        // Quantity: required, positive number
        String qty = txtQuantite.getText().trim();
        if (qty.isEmpty()) {
            showFieldError(lblQuantiteError, txtQuantite, "Quantity is required.");
            valid = false;
        } else {
            try {
                double q = Double.parseDouble(qty);
                if (q <= 0) {
                    showFieldError(lblQuantiteError, txtQuantite, "Quantity must be greater than 0.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showFieldError(lblQuantiteError, txtQuantite, "Quantity must be a valid number.");
                valid = false;
            }
        }

        // Expiry date: optional — past dates are already blocked by dayCellFactory
        // No additional validation needed here

        return valid;
    }

    private void showFieldError(Label errorLabel, TextField field, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        if (field != null) {
            field.setStyle(field.getStyle() + "-fx-border-color: #DC2626;");
        }
    }

    private void clearFieldErrors() {
        clearError(lblNomError, txtNom);
        clearError(lblCategorieError, null);
        clearError(lblQuantiteError, txtQuantite);
        clearError(lblDateError, null);
        if (lblError != null) { lblError.setVisible(false); lblError.setManaged(false); }
    }

    private void clearError(Label lbl, TextField field) {
        if (lbl != null) { lbl.setVisible(false); lbl.setManaged(false); lbl.setText(""); }
        if (field != null) {
            field.setStyle("-fx-font-size: 13px; -fx-padding: 10 14; -fx-background-radius: 10; -fx-border-color: #D1D5DB; -fx-border-radius: 10; -fx-background-color: #FAFAFA;");
        }
    }

    @FXML
    public void handleShowCalendar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/expiry_calendar.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Ingredient Expiry Calendar");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            
            // Set minimal size for the small modal
            stage.setResizable(false);
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading expiry calendar: " + e.getMessage());
        }
    }

    @FXML
    public void handleRefresh(ActionEvent event) {
        txtSearch.clear();
        if (cmbFilterCategory != null) cmbFilterCategory.setValue("All");
        if (cmbSortBy != null) cmbSortBy.setValue("Name (A-Z)");
        loadTableData();
        updateStats();
    }

    private void showInfoDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleExport() {
        showInfoDialog("Export in progress...");
    }

    private void showModal() {
        if (formOverlay != null) {
            formOverlay.setVisible(true);
            formOverlay.setManaged(true);
        }
    }

    private void clearForm() {
        txtNom.clear();
        cmbCategorie.setValue(null);
        txtQuantite.clear();
        cmbUnite.setValue(null);
        datePeremption.setValue(null);
        txtImage.clear();
        txtNotes.clear();
        clearFieldErrors();
    }

    private void fillForm(Ingredient ingredient) {
        txtNom.setText(ingredient.getNom() != null ? ingredient.getNom() : "");
        cmbCategorie.setValue(ingredient.getCategorie());
        txtQuantite.setText(String.valueOf(ingredient.getQuantite()));
        cmbUnite.setValue(ingredient.getUnite());
        datePeremption.setValue(ingredient.getDatePeremption());
        txtImage.setText(ingredient.getImage() != null ? ingredient.getImage() : "");
        txtNotes.setText(ingredient.getNotes() != null ? ingredient.getNotes() : "");
        clearFieldErrors();
    }

}
