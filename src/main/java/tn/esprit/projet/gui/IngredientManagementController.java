package tn.esprit.projet.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.concurrent.Task;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.projet.models.CourseItem;
import tn.esprit.projet.models.Ingredient;
import tn.esprit.projet.services.CourseService;
import tn.esprit.projet.services.IngredientService;
import tn.esprit.projet.services.SubstitutionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class IngredientManagementController implements Initializable {

    // ═══════════════════════════════════
    // FXML VARIABLES
    // ═══════════════════════════════════

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
    @FXML private TableColumn<Ingredient, String> colImage;
    @FXML private TableColumn<Ingredient, Boolean> colSelect;
    @FXML private TableColumn<Ingredient, String> colNom;
    @FXML private TableColumn<Ingredient, String> colCategorie;
    @FXML private TableColumn<Ingredient, String> colQuantite;
    @FXML private TableColumn<Ingredient, LocalDate> colDatePeremption;
    @FXML private TableColumn<Ingredient, String> colStatus;
    @FXML private TableColumn<Ingredient, String> colNotes;
    @FXML private TableColumn<Ingredient, Void> colActions;

    // Add/Edit Modal
    @FXML private StackPane formOverlay;
    @FXML private Label lblFormTitle;
    @FXML private Label lblError;
    @FXML private TextField txtNom;
    @FXML private Label lblNomError;
    @FXML private ComboBox<String> cmbCategorie;
    @FXML private Label lblCategorieError;
    @FXML private TextField txtQuantite;
    @FXML private Label lblQuantiteError;
    @FXML private ComboBox<String> cmbUnite;
    @FXML private DatePicker datePeremption;
    @FXML private Label lblDateError;
    @FXML private TextField txtImage;
    @FXML private TextArea txtNotes;
    @FXML private Button btnSaveIngredient;

    // AI Substitution
    @FXML private ComboBox<Ingredient> cmbOutOfStock;
    @FXML private Button btnFindSubstitutes;
    @FXML private HBox loadingBox;
    @FXML private VBox substitutionResults;

    // Shopping List Modal
    @FXML private StackPane shoppingListOverlay;
    @FXML private VBox shoppingListContainer;
    @FXML private HBox emptyShoppingState;
    @FXML private Label lblShoppingCount;

    // ═══════════════════════════════════
    // SERVICES & VARIABLES
    // ═══════════════════════════════════

    private IngredientService ingredientService;
    private SubstitutionService substitutionService;
    private CourseService courseService;
    private FilteredList<Ingredient> filteredIngredients;
    private Ingredient currentIngredient;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} '\\-]+$");

    private static final List<String> CATEGORIES = Arrays.asList(
            "Vegetables", "Fruits", "Meats & Fish",
            "Dairy Products", "Grocery Store",
            "Frozen", "Drinks", "Condiments"
    );

    private static final List<String> UNITS = Arrays.asList(
            "g", "kg", "ml", "l", "pcs"
    );

    // ═══════════════════════════════════
    // INITIALIZE
    // ═══════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ingredientService = new IngredientService();
        substitutionService = new SubstitutionService();
        courseService = new CourseService();

        setupComboBoxes();
        setupTableColumns();
        loadTableData();
        updateStats();
        setupSearchAndFilters();
        if (cmbOutOfStock != null) {
            loadOutOfStockComboBox();
        }
    }

    // ═══════════════════════════════════
    // COMBOBOXES SETUP
    // ═══════════════════════════════════

    private void setupComboBoxes() {
        if (cmbFilterCategory != null) {
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

    // ═══════════════════════════════════
    // TABLE COLUMNS SETUP
    // ═══════════════════════════════════

    private void setupTableColumns() {
        if (tableIngredients == null) return;

        // Image column
        if (colImage != null) {
            colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
            colImage.setCellFactory(column -> new TableCell<>() {
                private final ImageView imageView = new ImageView();
                {
                    imageView.setFitWidth(40);
                    imageView.setFitHeight(40);
                    imageView.setPreserveRatio(true);
                }

                @Override
                protected void updateItem(String imageUrl, boolean empty) {
                    super.updateItem(imageUrl, empty);
                    if (empty || imageUrl == null || imageUrl.isBlank()) {
                        setGraphic(null);
                    } else {
                        try {
                            Image img = new Image(imageUrl, 40, 40, true, true, true);
                            imageView.setImage(img);
                            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(40, 40);
                            clip.setArcWidth(10);
                            clip.setArcHeight(10);
                            imageView.setClip(clip);
                            setGraphic(imageView);
                        } catch (Exception e) {
                            setGraphic(null);
                        }
                    }
                    setAlignment(Pos.CENTER);
                }
            });
        }

        // Select column
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

        // Quantity column
        if (colQuantite != null) {
            colQuantite.setCellValueFactory(cellData -> {
                Ingredient i = cellData.getValue();
                String unit = i.getUnite() != null ? " " + i.getUnite() : "";
                return new SimpleStringProperty(i.getQuantite() + unit);
            });
        }

        // Expiry date column
        if (colDatePeremption != null) {
            colDatePeremption.setCellValueFactory(new PropertyValueFactory<>("datePeremption"));
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
        }

        // Status column
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
                        if (item.contains("Expired")) {
                            style += "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
                        } else if (item.contains("Soon")) {
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

        // Notes column
        if (colNotes != null) {
            colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        }

        // Actions column
        if (colActions != null) {
            colActions.setCellFactory(param -> new TableCell<>() {
                private final Button editBtn = new Button("✏");
                private final Button deleteBtn = new Button("🗑");
                private final HBox buttons = new HBox(8, editBtn, deleteBtn);
                {
                    buttons.setAlignment(Pos.CENTER);
                    editBtn.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-size: 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                    deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-size: 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                    editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                    deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : buttons);
                }
            });
        }
    }

    private SimpleStringProperty getStatusCell(TableColumn.CellDataFeatures<Ingredient, String> cellData) {
        Ingredient i = cellData.getValue();
        if (i.getDatePeremption() == null) return new SimpleStringProperty("— No date");

        LocalDate today = LocalDate.now();
        if (i.getDatePeremption().isBefore(today)) return new SimpleStringProperty("❌ Expired");

        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, i.getDatePeremption());
        if (daysRemaining <= 3) return new SimpleStringProperty("⚠️ " + daysRemaining + "d Soon");

        return new SimpleStringProperty("✅ " + daysRemaining + " days");
    }

    // ═══════════════════════════════════
    // TABLE DATA & FILTERS
    // ═══════════════════════════════════

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
            if (!searchText.isEmpty()) {
                boolean matchName = ingredient.getNom() != null && ingredient.getNom().toLowerCase().contains(searchText);
                if (!matchName) return false;
            }
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

    // ═══════════════════════════════════
    // STATS
    // ═══════════════════════════════════

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

    // ═══════════════════════════════════
    // ADD / EDIT / DELETE
    // ═══════════════════════════════════

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
                loadOutOfStockComboBox();
                showToast("✅ Ingredient deleted successfully.");
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
        } else {
            ingredientService.ajouter(ingredient);
        }

        handleCloseForm(null);
        loadTableData();
        updateStats();
        loadOutOfStockComboBox();
        showToast("✅ Ingredient saved successfully.");
    }

    // ═══════════════════════════════════
    // FORM VALIDATION
    // ═══════════════════════════════════

    private boolean validateForm() {
        boolean valid = true;

        String nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            showFieldError(lblNomError, txtNom, "Name is required.");
            valid = false;
        } else if (!NAME_PATTERN.matcher(nom).matches()) {
            showFieldError(lblNomError, txtNom, "Only letters, spaces, apostrophes and hyphens allowed.");
            valid = false;
        }

        if (cmbCategorie.getValue() == null) {
            showFieldError(lblCategorieError, null, "Category is required.");
            valid = false;
        }

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
            field.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 12;");
        }
    }

    // ═══════════════════════════════════
    // FORM HELPERS
    // ═══════════════════════════════════

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

    // ═══════════════════════════════════
    // AI SUBSTITUTION
    // ═══════════════════════════════════

    private void loadOutOfStockComboBox() {
        if (cmbOutOfStock == null) return;
        List<Ingredient> outOfStock = substitutionService.getOutOfStockIngredients();
        cmbOutOfStock.setItems(FXCollections.observableArrayList(outOfStock));

        cmbOutOfStock.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("🔴 " + item.getNom() + " (0 " + (item.getUnite() != null ? item.getUnite() : "units") + ")");
                }
            }
        });

        cmbOutOfStock.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select out-of-stock ingredient...");
                } else {
                    setText("🔴 " + item.getNom());
                }
            }
        });
    }

    @FXML
    public void handleFindSubstitutes(ActionEvent event) {
        Ingredient selected = cmbOutOfStock.getValue();

        if (selected == null) {
            showToast("⚠️ Please select an out-of-stock ingredient first!");
            return;
        }

        substitutionResults.getChildren().clear();

        loadingBox.setVisible(true);
        loadingBox.setManaged(true);
        btnFindSubstitutes.setDisable(true);

        Task<List<SubstitutionService.Substitution>> task = new Task<>() {
            @Override
            protected List<SubstitutionService.Substitution> call() {
                return substitutionService.findSubstitutes(selected);
            }
        };

        task.setOnSucceeded(e -> {
            List<SubstitutionService.Substitution> substitutes = task.getValue();
            loadingBox.setVisible(false);
            loadingBox.setManaged(false);
            btnFindSubstitutes.setDisable(false);

            if (substitutes.isEmpty()) {
                showNoSubstituteFound(selected);
            } else {
                showSubstitutesFound(selected, substitutes);
            }
        });

        task.setOnFailed(e -> {
            loadingBox.setVisible(false);
            loadingBox.setManaged(false);
            btnFindSubstitutes.setDisable(false);
            String errMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
            System.err.println("❌ [TASK FAILED] " + errMsg);
            showToast("❌ AI Error: " + (errMsg != null && errMsg.length() > 80 ? errMsg.substring(0, 80) + "…" : errMsg));
        });

        new Thread(task).start();
    }

    private void showSubstitutesFound(Ingredient outOfStock, List<SubstitutionService.Substitution> substitutes) {
        substitutionResults.getChildren().clear();

        Label headerLabel = new Label("✅ " + substitutes.size() + " substitute(s) found:");
        headerLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #16A34A; -fx-padding: 5 0;");
        substitutionResults.getChildren().add(headerLabel);

        VBox listContainer = new VBox(6);
        listContainer.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-padding: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        for (SubstitutionService.Substitution sub : substitutes) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 4 8; -fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #F1F5F9; -fx-border-radius: 6;");

            Label icon = new Label("🌿");
            icon.setStyle("-fx-font-size: 14px;");

            Label text = new Label(sub.getRawText());
            text.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");
            text.setWrapText(true);
            HBox.setHgrow(text, Priority.ALWAYS);

            Button btnApply = new Button("Use this substitute");
            btnApply.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; " +
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 3 10; -fx-cursor: hand;");
            btnApply.setOnAction(e -> handleApplySubstitution(sub, outOfStock));

            row.getChildren().addAll(icon, text, btnApply);
            listContainer.getChildren().add(row);
        }
        substitutionResults.getChildren().add(listContainer);
    }

    private void handleApplySubstitution(SubstitutionService.Substitution sub, Ingredient outOfStock) {
        // ✅ FIX: Use flexible name matching (contains + case-insensitive)
        // so slight AI name variations (e.g. "Olive oil" vs "Olive Oil") still match.
        String aiName = sub.getSubstituteName().trim().toLowerCase();
        Optional<Ingredient> subIngOpt = ingredientService.getAll().stream()
                .filter(i -> {
                    String dbName = i.getNom() != null ? i.getNom().trim().toLowerCase() : "";
                    return dbName.equals(aiName) || dbName.contains(aiName) || aiName.contains(dbName);
                })
                .filter(i -> i.getQuantite() > 0)
                .findFirst();

        if (subIngOpt.isEmpty()) {
            showToast("❌ Could not find \"" + sub.getSubstituteName() + "\" in stock.");
            return;
        }

        Ingredient subIngredient = subIngOpt.get();

        TextInputDialog dialog = new TextInputDialog("100");
        dialog.setTitle("Apply Substitution");
        dialog.setHeaderText("Substitute " + sub.getSubstituteName() + " for " + outOfStock.getNom());
        dialog.setContentText("How much " + outOfStock.getNom() + " (" + (outOfStock.getUnite() != null ? outOfStock.getUnite() : "units") + ") do you need?");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double neededOriginal = Double.parseDouble(input);
                double neededSub = (neededOriginal / sub.getOriginalReferenceQuantity()) * sub.getRatio();

                if (neededSub > subIngredient.getQuantite()) {
                    showToast("⚠️ Not enough " + subIngredient.getNom() + " (Stock: " + subIngredient.getQuantite() + ")");
                    return;
                }

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm");
                confirm.setHeaderText("Deduct " + String.format("%.2f", neededSub) + " " + sub.getSubstituteUnit() + " of " + subIngredient.getNom() + "?");
                if (confirm.showAndWait().get() == ButtonType.OK) {
                    ingredientService.updateQuantite(subIngredient.getId(), -neededSub);
                    showToast("✅ Substitution applied!");
                    substitutionResults.getChildren().clear();
                    loadTableData();
                    updateStats();
                    loadOutOfStockComboBox();
                }
            } catch (Exception e) {
                showToast("❌ Invalid input.");
            }
        });
    }




    private void showNoSubstituteFound(Ingredient outOfStock) {
        substitutionResults.getChildren().clear();

        VBox noSubstituteBox = new VBox();
        noSubstituteBox.setSpacing(14);
        noSubstituteBox.setAlignment(Pos.CENTER);
        noSubstituteBox.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 12; " +
                "-fx-border-color: #CBD5E1; -fx-border-radius: 12; -fx-padding: 24;");

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 36px;");

        Label message = new Label("No substitute found in your stock for \"" + outOfStock.getNom() + "\"");
        message.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        message.setWrapText(true);

        Label subMessage = new Label("Would you like to add it to your shopping list?");
        subMessage.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Button btnAddToCart = new Button("🛒 Add \"" + outOfStock.getNom() + "\" to Shopping List");
        btnAddToCart.setStyle("-fx-background-color: #1F4D3A; " +
                "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 12 24;");
        btnAddToCart.setOnAction(e -> handleAddToShoppingList(outOfStock));

        noSubstituteBox.getChildren().addAll(icon, message, subMessage, btnAddToCart);
        substitutionResults.getChildren().add(noSubstituteBox);
    }

    // ═══════════════════════════════════
    // SHOPPING LIST
    // ═══════════════════════════════════

    private void handleAddToShoppingList(Ingredient ingredient) {
        if (courseService.itemAlreadyExists(ingredient.getNom())) {
            showToast("⚠️ \"" + ingredient.getNom() + "\" is already in your shopping list!");
            return;
        }

        CourseItem item = new CourseItem(
                ingredient.getNom(),
                1,
                ingredient.getUnite() != null ? ingredient.getUnite() : "pcs",
                LocalDate.now()
        );

        courseService.addItem(item);
        substitutionResults.getChildren().clear();
        loadOutOfStockComboBox();

        // ★ Juste un toast, pas de dialog
        showToast("✅ \"" + ingredient.getNom() + "\" added to shopping list!");
    }
    @FXML
    public void handleShowShoppingList(ActionEvent event) {
        if (shoppingListOverlay != null) {
            shoppingListOverlay.setVisible(true);
            shoppingListOverlay.setManaged(true);
            loadShoppingListModal();
        }
    }

    @FXML
    public void handleCloseShoppingList(ActionEvent event) {
        if (shoppingListOverlay != null) {
            shoppingListOverlay.setVisible(false);
            shoppingListOverlay.setManaged(false);
        }
    }

    private void loadShoppingListModal() {
        shoppingListContainer.getChildren().clear();

        List<CourseItem> allItems = courseService.getAllItems();
        List<CourseItem> pendingItems = allItems.stream().filter(i -> !i.isPurchased()).toList();
        List<CourseItem> purchasedItems = allItems.stream().filter(CourseItem::isPurchased).toList();

        lblShoppingCount.setText(pendingItems.size() + " ingredient(s) to buy");

        if (allItems.isEmpty()) {
            emptyShoppingState.setVisible(true);
            emptyShoppingState.setManaged(true);
            shoppingListContainer.getChildren().add(emptyShoppingState);
            return;
        }

        emptyShoppingState.setVisible(false);
        emptyShoppingState.setManaged(false);

        // Pending section
        if (!pendingItems.isEmpty()) {
            Label pendingHeader = new Label("📋 Pending (" + pendingItems.size() + ")");
            pendingHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-padding: 8 0 4 0;");
            shoppingListContainer.getChildren().add(pendingHeader);

            for (CourseItem item : pendingItems) {
                shoppingListContainer.getChildren().add(buildShoppingItemCard(item));
            }
        }

        // Purchased section
        if (!purchasedItems.isEmpty()) {
            Label purchasedHeader = new Label("✅ Purchased (" + purchasedItems.size() + ")");
            purchasedHeader.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #64748B; -fx-padding: 14 0 4 0;");
            shoppingListContainer.getChildren().add(purchasedHeader);

            for (CourseItem item : purchasedItems) {
                shoppingListContainer.getChildren().add(buildShoppingItemCard(item));
            }
        }
    }

    private void loadShoppingList() {
        if (shoppingListOverlay != null && shoppingListOverlay.isVisible()) {
            loadShoppingListModal();
        }
    }

    private HBox buildShoppingItemCard(CourseItem item) {
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(14);

        String cardStyle = item.isPurchased()
                ? "-fx-background-color: #F8FAFC; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-padding: 12 16; -fx-opacity: 0.6;"
                : "-fx-background-color: #F0FDF9; -fx-background-radius: 10; -fx-border-color: #A7F3D0; -fx-border-radius: 10; -fx-padding: 12 16;";
        card.setStyle(cardStyle);

        // Checkbox
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(item.isPurchased());
        checkBox.setStyle("-fx-cursor: hand;");
        checkBox.setOnAction(e -> {
            courseService.markAsPurchased(item.getId());
            loadShoppingListModal();
        });

        // Item info
        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label((item.isPurchased() ? "✓ " : "🛒 ") + item.getIngredientName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: "
                + (item.isPurchased() ? "#94A3B8;" : "#1E293B;"));

        Label detailLabel = new Label("Added: " + item.getDateAdded().toString()
                + (item.getUnit() != null ? " · Unit: " + item.getUnit() : ""));
        detailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        info.getChildren().addAll(nameLabel, detailLabel);

        // Status badge
        Label badge = new Label(item.isPurchased() ? "Purchased ✓" : "Pending");
        badge.setStyle(item.isPurchased()
                ? "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10;"
                : "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10;");

        // Delete button
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                "-fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 13px;");
        deleteBtn.setOnAction(e -> {
            courseService.deleteItem(item.getId());
            loadShoppingListModal();
            showToast("🗑 Item removed from shopping list");
        });

        card.getChildren().addAll(checkBox, info, badge, deleteBtn);
        return card;
    }

    @FXML
    public void handleAddManualItem(ActionEvent event) {
        // Create custom dialog
        Dialog<CourseItem> dialog = new Dialog<>();
        dialog.setTitle("Add Item to Shopping List");
        dialog.setHeaderText("Enter item details");

        // Buttons
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Butter");

        TextField quantityField = new TextField();
        quantityField.setPromptText("e.g. 500");

        ComboBox<String> unitCombo = new ComboBox<>();
        unitCombo.setItems(FXCollections.observableArrayList("g", "kg", "ml", "l", "pcs"));
        unitCombo.setValue("pcs");

        grid.add(new Label("Ingredient name *"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Quantity *"), 0, 1);
        grid.add(quantityField, 1, 1);
        grid.add(new Label("Unit"), 0, 2);
        grid.add(unitCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String name = nameField.getText().trim();
                String qtyStr = quantityField.getText().trim();
                String unit = unitCombo.getValue();

                if (name.isEmpty() || qtyStr.isEmpty()) return null;

                try {
                    double qty = Double.parseDouble(qtyStr);
                    return new CourseItem(name, qty, unit, LocalDate.now());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            if (courseService.itemAlreadyExists(item.getIngredientName())) {
                showToast("⚠️ \"" + item.getIngredientName() + "\" already in list!");
                return;
            }
            courseService.addItem(item);
            loadShoppingListModal();
            showToast("✅ \"" + item.getIngredientName() + "\" added!");
        });
    }

    @FXML
    public void handleMarkAllPurchased(ActionEvent event) {
        List<CourseItem> pending = courseService.getPendingItems();
        for (CourseItem item : pending) {
            courseService.markAsPurchased(item.getId());
        }
        loadShoppingListModal();
        showToast("✅ All items marked as purchased!");
    }

    @FXML
    public void handleClearPurchased(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Purchased Items");
        alert.setHeaderText("Remove all purchased items?");
        alert.setContentText("This will permanently delete all items marked as purchased.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                courseService.clearPurchasedItems();
                loadShoppingListModal();
                showToast("🧹 Purchased items cleared!");
            }
        });
    }

    @FXML
    public void handleExportShoppingList(ActionEvent event) {
        List<CourseItem> items = courseService.getAllItems();
        if (items.isEmpty()) {
            showToast("⚠️ Shopping list is empty, nothing to export!");
            return;
        }

        // Choisir où sauvegarder
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Shopping List");
        fileChooser.setInitialFileName("shopping_list.pdf");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        Stage stage = (Stage) shoppingListOverlay.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);

        if (file == null) return;

        try {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
            document.open();

            // Title
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 20,
                    com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "Shopping List", titleFont);
            title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Date
            com.itextpdf.text.Font dateFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                    com.itextpdf.text.Font.ITALIC);
            com.itextpdf.text.Paragraph date = new com.itextpdf.text.Paragraph(
                    "Generated on: " + LocalDate.now().toString(), dateFont);
            date.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // Separator
            document.add(new com.itextpdf.text.Chunk(
                    com.itextpdf.text.Chunk.NEWLINE));

            // Pending items
            List<CourseItem> pending = items.stream()
                    .filter(i -> !i.isPurchased()).toList();
            List<CourseItem> purchased = items.stream()
                    .filter(CourseItem::isPurchased).toList();

            com.itextpdf.text.Font sectionFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 13,
                    com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font itemFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11);

            if (!pending.isEmpty()) {
                document.add(new com.itextpdf.text.Paragraph(
                        "To Buy (" + pending.size() + ")", sectionFont));
                document.add(new com.itextpdf.text.Chunk(
                        com.itextpdf.text.Chunk.NEWLINE));

                for (CourseItem item : pending) {
                    String line = "[ ]  " + item.getIngredientName()
                            + "  —  " + item.getQuantity()
                            + " " + (item.getUnit() != null ? item.getUnit() : "");
                    document.add(new com.itextpdf.text.Paragraph(line, itemFont));
                }
            }

            if (!purchased.isEmpty()) {
                document.add(new com.itextpdf.text.Chunk(
                        com.itextpdf.text.Chunk.NEWLINE));
                document.add(new com.itextpdf.text.Paragraph(
                        "Purchased (" + purchased.size() + ")", sectionFont));
                document.add(new com.itextpdf.text.Chunk(
                        com.itextpdf.text.Chunk.NEWLINE));

                for (CourseItem item : purchased) {
                    String line = "[x]  " + item.getIngredientName()
                            + "  —  " + item.getQuantity()
                            + " " + (item.getUnit() != null ? item.getUnit() : "");
                    document.add(new com.itextpdf.text.Paragraph(line, itemFont));
                }
            }

            document.close();
            showToast("✅ PDF exported successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showToast("❌ Error exporting PDF: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════
    // MISC HANDLERS
    // ═══════════════════════════════════

    @FXML
    public void handleRefresh(ActionEvent event) {
        txtSearch.clear();
        if (cmbFilterCategory != null) cmbFilterCategory.setValue("All");
        if (cmbSortBy != null) cmbSortBy.setValue("Name (A-Z)");
        loadTableData();
        updateStats();
        loadOutOfStockComboBox();
    }

    @FXML
    private void handleExport() {
        showToast("📥 Export in progress...");
    }

    // ═══════════════════════════════════
    // TOAST NOTIFICATION
    // ═══════════════════════════════════

    private void showToast(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String message) {
        showToast(message);
    }

    @FXML
    private void openScanner() {
        try {
            var url = getClass().getResource("/fxml/scanner_view.fxml");
            if (url == null) {
                showToast("❌ Error: scanner_view.fxml not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage scannerStage = new Stage();
            scannerStage.setTitle("📷 Product Scanner & Analysis");
            scannerStage.setScene(new Scene(root, 1000, 650));
            scannerStage.setResizable(false);

            ScannerController controller = loader.getController();
            scannerStage.setOnCloseRequest(e -> controller.cleanup());

            scannerStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("❌ Error opening scanner: " + e.getMessage());
        }
    }
    @FXML
    public void handleShowCalendar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/expiry_calendar.fxml"));
            //charger contenu depuis fxml
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ingredient Expiry Calendar");
            stage.initModality(Modality.APPLICATION_MODAL);
            //mettre contenu dans stage
            stage.setScene(new Scene(root));

            stage.setResizable(false);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading expiry calendar: " + e.getMessage());
        }
    }
}
