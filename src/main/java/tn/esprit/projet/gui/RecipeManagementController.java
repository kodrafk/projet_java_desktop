package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import tn.esprit.projet.models.*;
import tn.esprit.projet.services.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;

import tn.esprit.projet.controllers.MealPlanTrackingController;
import javafx.scene.control.Alert;
public class RecipeManagementController implements Initializable {

    // ==================== FXML ELEMENTS ====================

    // Stats
    @FXML private Label lblTotalRecipes;
    @FXML private Label lblEntrees;
    @FXML private Label lblPlats;
    @FXML private Label lblDesserts;
    @FXML private Label lblBoissons;

    // Filters
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterType;
    @FXML private ComboBox<String> cmbFilterDifficulty;
    @FXML private ComboBox<String> cmbFilterTime;

    // Content
    @FXML private TableView<Recette> tableRecipes;
    @FXML private FlowPane recipesGrid;
    @FXML private HBox filterPillsContainer;
    @FXML private HBox paginationHBox;
    @FXML private Label lblPaginationInfo;
    @FXML private TableColumn<Recette, String> colNom;
    @FXML private TableColumn<Recette, String> colType;
    @FXML private TableColumn<Recette, String> colDifficulte;
    @FXML private TableColumn<Recette, Integer> colTemps;
    @FXML private TableColumn<Recette, String> colDescription;
    @FXML private TableColumn<Recette, Void> colActions;
    @FXML private Label lblTableInfo;
    @FXML private VBox emptyState;
    @FXML private VBox paginationContainer;

    // Modal Add/Edit
    @FXML private StackPane modalOverlay;
    @FXML private Label lblModalTitle;
    @FXML private Label lblGeneralError;
    @FXML private TextField txtNom;
    @FXML private Label lblNomError;
    @FXML private ComboBox<String> cmbType;
    @FXML private Label lblTypeError;
    @FXML private TextField txtImage;
    @FXML private Label lblImageError;
    @FXML private TextField txtTemps;
    @FXML private Label lblTempsError;
    @FXML private ComboBox<String> cmbDifficulte;
    @FXML private Label lblDifficulteError;
    @FXML private TextArea txtDescription;
    @FXML private Label lblDescriptionError;
    @FXML private VBox stepsContainer;
    @FXML private ComboBox<Ingredient> cmbIngredients;
    @FXML private VBox recipeIngredientsContainer;

    // Modal Details
    @FXML private StackPane detailsOverlay;
    @FXML private VBox detailsModal;
    @FXML private Label lblDetailsNom;
    @FXML private Label lblDetailsType;
    @FXML private Label lblDetailsTemps;
    @FXML private Label lblDetailsDifficulte;
    @FXML private Label txtDetailsDescription;
    @FXML private VBox detailsStepsContainer;

    // Toast
    @FXML private HBox toastNotification;
    @FXML private Label lblToastIcon;
    @FXML private Label lblToastMessage;
    // Recommendation Section
    @FXML private VBox recommendationSection;
    @FXML private VBox recommendationLoading;
    @FXML private VBox recommendationEmpty;
    @FXML private HBox recommendationHeader;
    @FXML private HBox recommendationCardsContainer;
    @FXML private Button btnRefreshRecommendations;

    // Data
    private RecetteService recetteService;
    private IngredientService ingredientService;
    private RecetteFavorisService favorisService;
    private int getCurrentUserId() {
        return tn.esprit.projet.utils.SessionManager.getCurrentUser() != null 
            ? tn.esprit.projet.utils.SessionManager.getCurrentUser().getId() 
            : 1;
    }
    private boolean showFavoritesOnly = false;
    private FilteredList<Recette> filteredRecipes;
    private Recette currentRecette = null;
    private boolean isEditMode = false;
    private List<TextField> stepFields = new ArrayList<>();
    private Map<Integer, RecetteIngredient> selectedIngredientsMap = new HashMap<>();
    private RecipeRecommendationService recommendationService;
    private int currentPage = 1;
    private int itemsPerPage = 6;
    private int totalPages = 1;

    // Types and difficulties
    private final String[] TYPES = {"entree", "main dish", "dessert", "drinks"};
    private final String[] TYPES_LABELS = {"Entree", "Main Dish", "Dessert", "Drinks"};
    private final String[] TYPES_EMOJIS = {"🥗", "🍲", "🍰", "🥤"};
    private final String[] DIFFICULTIES = {"easy", "medium", "hard"};
    private final String[] DIFFICULTIES_LABELS = {"Easy", "Medium", "Hard"};

    private String currentTypeFilter = "All";
    private String currentSortField = "nom";
    private boolean isAscending = true;

    // ==================== INITIALIZATION ====================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        recetteService = new RecetteService();
        ingredientService = new IngredientService();
        favorisService = new RecetteFavorisService();
        recommendationService = new RecipeRecommendationService();
        setupFormComboBoxes();
        setupTable();
        setupFilters();
        setupSearchListener();
        loadRecipes();
        updateStats();
        
        if (filterPillsContainer != null) {
            renderTypePills();
        }

        // Initialize Ingredients ComboBox
        if (cmbIngredients != null) {
            List<Ingredient> ingredients = ingredientService.getAll();
            cmbIngredients.setItems(FXCollections.observableArrayList(ingredients));
            
            // Custom cell for ComboBox display
            cmbIngredients.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Ingredient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getNom());
                }
            });
            cmbIngredients.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Ingredient item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getNom());
                }
            });

            cmbIngredients.setOnAction(e -> {
                Ingredient selected = cmbIngredients.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    addIngredientToRecipe(selected, "");
                    cmbIngredients.getSelectionModel().clearSelection();
                }
            });
        }
        loadRecommendations();
    }
    private void setupFormComboBoxes() {
        if (cmbType != null) {
            cmbType.setItems(FXCollections.observableArrayList(TYPES_LABELS));
        }

        if (cmbDifficulte != null) {
            cmbDifficulte.setItems(FXCollections.observableArrayList(DIFFICULTIES_LABELS));
        }
    }
    private void renderTypePills() {
        filterPillsContainer.getChildren().clear();

        // "ALL" Pill
        Button allPill = createPill("🍽️ ALL", "All".equals(currentTypeFilter) && !showFavoritesOnly);
        allPill.setOnAction(e -> {
            currentTypeFilter = "All";
            showFavoritesOnly = false;
            currentPage = 1;
            renderTypePills();
            applyFilters();
        });
        filterPillsContainer.getChildren().add(allPill);

        // Type Pills
        for (int i = 0; i < TYPES_LABELS.length; i++) {
            String label = TYPES_LABELS[i];
            String emoji = TYPES_EMOJIS[i];
            Button pill = createPill(emoji + " " + label.toUpperCase(),
                    label.equals(currentTypeFilter) && !showFavoritesOnly);
            pill.setOnAction(e -> {
                currentTypeFilter = label;
                showFavoritesOnly = false;
                currentPage = 1;
                renderTypePills();
                applyFilters();
            });
            filterPillsContainer.getChildren().add(pill);
        }

        // Favorites Pill
        int favCount = favorisService.countFavorites(getCurrentUserId());
        Button favPill = createPill("❤️ FAVORITES (" + favCount + ")", showFavoritesOnly);
        favPill.setOnAction(e -> {
            showFavoritesOnly = true;
            currentTypeFilter = "All";
            currentPage = 1;
            renderTypePills();
            applyFilters();
        });
        filterPillsContainer.getChildren().add(favPill);
    }

    private Button createPill(String text, boolean isActive) {
        Button pill = new Button(text);
        String baseStyle = "-fx-background-radius: 25; -fx-padding: 10 25; -fx-font-size: 13px; -fx-font-weight: 900; -fx-cursor: hand; -fx-transition: all 0.2s; ";
        if (isActive) {
            pill.setStyle(baseStyle + "-fx-background-color: #F59E0B; -fx-text-fill: white;");
            pill.setEffect(new DropShadow(15, 0, 5, Color.web("#F59E0B4D")));
        } else {
            pill.setStyle(baseStyle + "-fx-background-color: white; -fx-text-fill: #64748B; -fx-border-color: #E2E8F0; -fx-border-radius: 25;");
        }
        return pill;
    }

    private void renderRecipesGrid() {
        if (recipesGrid == null) return;
        
        recipesGrid.getChildren().clear();
        
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredRecipes.size());
        
        for (int i = start; i < end; i++) {
            Recette recette = filteredRecipes.get(i);
            VBox card = createRecipeCard(recette);
            recipesGrid.getChildren().add(card);
        }
        
        renderPagination();
    }

    private VBox createRecipeCard(Recette recette) {
        VBox card = new VBox(0);
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-color: #E2E8F0; -fx-border-radius: 20;");
        card.setEffect(new DropShadow(15, 0, 5, Color.web("#0000000D")));
        card.setCursor(javafx.scene.Cursor.HAND);

        // Image Section
        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(200);
        imageArea.setStyle("-fx-background-radius: 20 20 0 0; -fx-background-color: #F8FAFC;");
        
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        String imageUrl = (recette.getImage() == null || recette.getImage().isEmpty()) 
                ? "https://images.unsplash.com/photo-1495521821757-a1efb6729352?w=600" 
                : recette.getImage();
        
        // Optimized image loading
        javafx.scene.image.Image img = new javafx.scene.image.Image(imageUrl, 640, 400, true, true, true);
        imageView.setImage(img);
        imageView.setFitWidth(320);
        imageView.setFitHeight(200);
        
        // Clip image to match card radius
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(320, 200);
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        imageArea.setClip(clip);
        
        // Badge Type with colors from image
        String badgeColor = switch(recette.getType().toLowerCase()) {
            case "entree" -> "#10B981"; // Green
            case "dessert" -> "#F43F5E"; // Pink/Red
            case "plat", "main dish" -> "#65A30D"; // Olive Green
            default -> "#F59E0B"; // Orange
        };
        
        // Badges for Type and Favorite
        Label typeBadge = new Label(recette.getTypeLabel().toUpperCase());
        typeBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 900;");
        StackPane.setAlignment(typeBadge, Pos.TOP_LEFT);
        StackPane.setMargin(typeBadge, new Insets(15));

        boolean isFav = favorisService.isFavorite(getCurrentUserId(), recette.getId());
        Button btnFav = new Button(isFav ? "❤" : "♡");
        btnFav.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95);" +
                        "-fx-text-fill: " + (isFav ? "#F43F5E" : "#94A3B8") + ";" +
                        "-fx-min-width: 38; -fx-min-height: 38;" +
                        "-fx-background-radius: 19; -fx-cursor: hand;" +
                        "-fx-font-size: 20px; -fx-font-weight: bold;"
        );
        // Hover Actions Overlay
        HBox actionsOverlay = new HBox(10);
        actionsOverlay.setAlignment(Pos.CENTER);
        actionsOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 20 20 0 0;");
        actionsOverlay.setOpacity(0);
        btnFav.setOnAction(e -> {
            favorisService.toggleFavorite(getCurrentUserId(), recette.getId());
            boolean nowFav = favorisService.isFavorite(getCurrentUserId(), recette.getId());
            btnFav.setText(nowFav ? "❤" : "♡");
            btnFav.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.95);" +
                            "-fx-text-fill: " + (nowFav ? "#F43F5E" : "#94A3B8") + ";" +
                            "-fx-min-width: 38; -fx-min-height: 38;" +
                            "-fx-background-radius: 19; -fx-cursor: hand;" +
                            "-fx-font-size: 20px; -fx-font-weight: bold;"
            );
            renderTypePills();
            e.consume();
        });

        btnFav.setOnMouseEntered(e -> actionsOverlay.setOpacity(0));
        StackPane.setAlignment(btnFav, Pos.TOP_RIGHT);
        StackPane.setMargin(btnFav, new Insets(15));


        
        Button btnEdit = new Button("✏");
        btnEdit.setStyle("-fx-background-color: white; -fx-text-fill: #1E293B; -fx-font-size: 14px; -fx-min-width: 36; -fx-min-height: 36; -fx-background-radius: 10; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> handleEdit(recette));
        
        Button btnDel = new Button("🗑");
        btnDel.setStyle("-fx-background-color: white; -fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-min-width: 36; -fx-min-height: 36; -fx-background-radius: 10; -fx-cursor: hand;");
        btnDel.setOnAction(e -> handleDelete(recette));
        
        actionsOverlay.getChildren().addAll(btnEdit, btnDel);
        
        imageArea.setOnMouseEntered(e -> actionsOverlay.setOpacity(1));
        imageArea.setOnMouseExited(e -> actionsOverlay.setOpacity(0));

        imageArea.getChildren().addAll(imageView, actionsOverlay, typeBadge, btnFav);

        // Content Section
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        
        Label title = new Label(recette.getNom());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1E293B;");
        title.setWrapText(true);
        
        Label desc = new Label(recette.getDescription() != null ? recette.getDescription() : "Delicious recipe to discover...");
        desc.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B; -fx-line-spacing: 2;");
        desc.setWrapText(true);
        desc.setPrefHeight(45);
        
        HBox meta = new HBox(20);
        meta.setAlignment(Pos.CENTER_LEFT);
        
        Label time = new Label("⏱ " + recette.getTempsPreparation() + " min");
        time.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: bold;");
        
        Label diff = new Label("📊 " + recette.getDifficultyLabel());
        diff.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        meta.getChildren().addAll(time, diff, spacer);
        
        Button btnView = new Button("See recipe →");
        btnView.setStyle("-fx-background-color: transparent; -fx-text-fill: #1F4D3A; -fx-font-weight: 900; -fx-padding: 5 0; -fx-cursor: hand; -fx-font-size: 14px;");
        btnView.setOnAction(e -> handleViewDetails(recette));

        content.getChildren().addAll(title, desc, meta, btnView);
        card.getChildren().addAll(imageArea, content);
        
        return card;
    }

    private void renderPagination() {
        if (paginationHBox == null) return;
        
        paginationHBox.getChildren().clear();
        totalPages = (int) Math.ceil((double) filteredRecipes.size() / itemsPerPage);
        if (totalPages <= 1) {
            if (paginationContainer != null) {
                paginationContainer.setVisible(false);
                paginationContainer.setManaged(false);
            }
            return;
        }
        
        if (paginationContainer != null) {
            paginationContainer.setVisible(true);
            paginationContainer.setManaged(true);
        }
        lblPaginationInfo.setText("Page " + currentPage + " sur " + totalPages);

        // Previous
        Button prev = createPaginationButton("<", currentPage > 1);
        prev.setOnAction(e -> {
            currentPage--;
            renderRecipesGrid();
        });
        paginationHBox.getChildren().add(prev);

        // Pages
        for (int i = 1; i <= totalPages; i++) {
            if (i == 1 || i == totalPages || (i >= currentPage - 1 && i <= currentPage + 1)) {
                Button pBtn = createPaginationButton(String.valueOf(i), true);
                if (i == currentPage) {
                    pBtn.setStyle("-fx-background-color: #1F4D3A; -fx-text-fill: white; -fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; -fx-font-weight: bold;");
                }
                int finalI = i;
                pBtn.setOnAction(e -> {
                    currentPage = finalI;
                    renderRecipesGrid();
                });
                paginationHBox.getChildren().add(pBtn);
            } else if (i == currentPage - 2 || i == currentPage + 2) {
                Label dots = new Label("...");
                dots.setStyle("-fx-text-fill: #94A3B8; -fx-font-weight: bold;");
                paginationHBox.getChildren().add(dots);
            }
        }

        // Next
        Button next = createPaginationButton(">", currentPage < totalPages);
        next.setOnAction(e -> {
            currentPage++;
            renderRecipesGrid();
        });
        paginationHBox.getChildren().add(next);
    }

    private Button createPaginationButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefSize(40, 40);
        String style = "-fx-background-radius: 20; -fx-font-weight: bold; -fx-cursor: hand; ";
        if (active) {
            btn.setStyle(style + "-fx-background-color: white; -fx-text-fill: #1F4D3A; -fx-border-color: #E2E8F0; -fx-border-radius: 20;");
        } else {
            btn.setStyle(style + "-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-border-color: #F1F5F9; -fx-border-radius: 20;");
            btn.setDisable(true);
        }
        return btn;
    }

    private void setupTable() {
        if (tableRecipes == null || colNom == null) return;
        
        colNom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTypeLabel()));
        colDifficulte.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDifficultyLabel()));
        colTemps.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tempsPreparation"));
        colDescription.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));

        // Custom cell for Actions
        if (colActions != null) {
            colActions.setCellFactory(column -> new TableCell<>() {
                private final Button btnView = new Button("👁");
                private final Button btnEdit = new Button("✏");
                private final Button btnDelete = new Button("🗑");
                private final HBox actions = new HBox(8, btnView, btnEdit, btnDelete);

                {
                    actions.setAlignment(Pos.CENTER);
                    btnView.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #4F46E5; -fx-cursor: hand; -fx-background-radius: 5;");
                    btnEdit.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706; -fx-cursor: hand; -fx-background-radius: 5;");
                    btnDelete.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-cursor: hand; -fx-background-radius: 5;");

                    btnView.setOnAction(e -> handleViewDetails(getTableView().getItems().get(getIndex())));
                    btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                    btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(actions);
                    }
                }
            });
        }
    }

    private void setupFilters() {
        if (cmbFilterType != null) {
            cmbFilterType.setItems(FXCollections.observableArrayList(
                    "All", "Entree", "Main Dish", "Dessert", "Drinks"
            ));
            cmbFilterType.setValue("All");
            cmbFilterType.setOnAction(e -> filterRecipes());
        }

        if (cmbFilterDifficulty != null) {
            cmbFilterDifficulty.setItems(FXCollections.observableArrayList(
                    "All", "Easy", "Medium", "Hard"
            ));
            cmbFilterDifficulty.setValue("All");
            cmbFilterDifficulty.setOnAction(e -> filterRecipes());
        }

        if (cmbFilterTime != null) {
            cmbFilterTime.setItems(FXCollections.observableArrayList(
                    "All", "< 15 min", "< 30 min", "< 1 hour", "> 1 hour"
            ));
            cmbFilterTime.setValue("All");
            cmbFilterTime.setOnAction(e -> filterRecipes());
        }
    }

    private void setupSearchListener() {
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, old, newVal) -> {
                loadRecipes();
                applyFilters();
            });
        }
    }

    // ==================== LOAD DATA ====================

    private void loadRecipes() {
        int currentUserId = getCurrentUserId();
        List<Recette> allRecipes;

        // Si admin → charger toutes les recettes
        if (tn.esprit.projet.utils.SessionManager.getCurrentUser() != null
                && tn.esprit.projet.utils.SessionManager.getCurrentUser().isAdmin()) {
            allRecipes = recetteService.getAll();
            System.out.println("➡️ Admin mode → toutes les recettes: " + allRecipes.size());
        } else {
            allRecipes = recetteService.getByUserId(currentUserId);
            System.out.println("➡️ User mode → recettes user " + currentUserId + ": " + allRecipes.size());
        }

        filteredRecipes = new FilteredList<>(
                FXCollections.observableArrayList(allRecipes), p -> true);
        applyFilters();
    }

    private void applyFilters() {
        if (txtSearch == null || filteredRecipes == null) return;
        
        String searchText = txtSearch.getText().toLowerCase();
        String typeLabel = (cmbFilterType != null) ? cmbFilterType.getValue() : currentTypeFilter;
        String difficultyLabel = (cmbFilterDifficulty != null) ? cmbFilterDifficulty.getValue() : "All";
        String timeFilter = (cmbFilterTime != null) ? cmbFilterTime.getValue() : "All";

        List<Integer> favoriteIds = showFavoritesOnly
                ? favorisService.getFavoriteIds(getCurrentUserId())
                : null;

        filteredRecipes.setPredicate(recette -> {
            // Favorites filter
            if (showFavoritesOnly) {
                if (!favoriteIds.contains(recette.getId())) return false;
            }

            // Search filter
            if (searchText != null && !searchText.isEmpty()) {
                if (!recette.getNom().toLowerCase().contains(searchText) &&
                        (recette.getDescription() == null || !recette.getDescription().toLowerCase().contains(searchText))) {
                    return false;
                }
            }

            // Type filter
            if (!showFavoritesOnly && typeLabel != null && !typeLabel.equals("All")) {
                String typeValue = getTypeValue(typeLabel);
                if (typeValue != null && !recette.getType().equals(typeValue)) {
                    return false;
                }
            }

            // Difficulty filter
            if (difficultyLabel != null && !difficultyLabel.equals("All")) {
                String difficultyValue = getDifficultyValue(difficultyLabel);
                if (difficultyValue != null && !recette.getDifficulte().equals(difficultyValue)) {
                    return false;
                }
            }

            // Time filter
            if (timeFilter != null && !timeFilter.equals("All")) {
                int temps = recette.getTempsPreparation();
                switch (timeFilter) {
                    case "< 15 min":
                        if (temps >= 15) return false;
                        break;
                    case "< 30 min":
                        if (temps >= 30) return false;
                        break;
                    case "< 1 hour":
                        if (temps >= 60) return false;
                        break;
                    case "> 1 hour":
                        if (temps <= 60) return false;
                        break;
                }
            }

            return true;
        });

        // Update TableView if exists
        if (tableRecipes != null) {
            tableRecipes.setItems(filteredRecipes);
        }
        
        // Render Grid if exists
        if (recipesGrid != null) {
            renderRecipesGrid();
        }

        if (lblTableInfo != null) {
            lblTableInfo.setText(filteredRecipes.size() + " recipe(s) found");
        }

        if (emptyState != null) {
            if (filteredRecipes.isEmpty()) {
                emptyState.setVisible(true);
                emptyState.setManaged(true);
                if (tableRecipes != null) tableRecipes.setVisible(false);
                if (recipesGrid != null) recipesGrid.setVisible(false);
            } else {
                emptyState.setVisible(false);
                emptyState.setManaged(false);
                if (tableRecipes != null) tableRecipes.setVisible(true);
                if (recipesGrid != null) recipesGrid.setVisible(true);
            }
        }
    }

    private void filterRecipes() {
        applyFilters();
    }

    // ==================== HELPER METHODS ====================

    private String getTypeValue(String label) {
        if (label == null || label.equals("All")) return null;
        return switch (label) {
            case "Entree" -> "entree";
            case "Main Dish" -> "plat";
            case "Dessert" -> "dessert";
            case "Drinks" -> "boisson";
            default -> null;
        };
    }

    private String getDifficultyValue(String label) {
        if (label == null || label.equals("All")) return null;
        return switch (label) {
            case "Easy" -> "facile";
            case "Medium" -> "moyen";
            case "Hard" -> "difficile";
            default -> null;
        };
    }

    // ==================== STATS ====================

    private void updateStats() {
        int currentUserId = getCurrentUserId();
        if (lblTotalRecipes != null) lblTotalRecipes.setText(String.valueOf(recetteService.countTotalByUserId(currentUserId)));
        if (lblEntrees != null) lblEntrees.setText(String.valueOf(recetteService.countByTypeAndUserId("entree", currentUserId)));
        if (lblPlats != null) lblPlats.setText(String.valueOf(recetteService.countByTypeAndUserId("main dish", currentUserId)));
        if (lblDesserts != null) lblDesserts.setText(String.valueOf(recetteService.countByTypeAndUserId("dessert", currentUserId)));
        if (lblBoissons != null) lblBoissons.setText(String.valueOf(recetteService.countByTypeAndUserId("drinks", currentUserId)));
    }

    // ==================== HANDLERS ====================

    @FXML
    public void handleShowStatistics(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics_dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Recipe Statistics Dashboard");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            
            // Optional: make it responsive/resizable
            stage.setMinWidth(1000);
            stage.setMinHeight(750);
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Using existing toast mechanism if possible
            if (lblToastMessage != null) {
                lblToastMessage.setText("Error loading statistics");
                if (toastNotification != null) toastNotification.setVisible(true);
            }
        }
    }

    @FXML
    public void handleAddNew(javafx.event.ActionEvent event) {
        isEditMode = false;
        currentRecette = null;
        if (lblModalTitle != null) lblModalTitle.setText("✨ New Recipe");
        clearForm();
        addStepField();
        showModal();
    }

    @FXML
    public void handleRefresh(javafx.event.ActionEvent event) {
        txtSearch.clear();
        if (cmbFilterType != null) cmbFilterType.setValue("All");
        if (cmbFilterDifficulty != null) cmbFilterDifficulty.setValue("All");
        if (cmbFilterTime != null) cmbFilterTime.setValue("All");
        
        currentTypeFilter = "All";
        currentPage = 1;
        
        if (filterPillsContainer != null) {
            renderTypePills();
        }
        
        loadRecipes();
        updateStats();
        loadRecommendations();
    }

    @FXML
    public void handleEdit(Recette recette) {
        if (recette == null) return;
        isEditMode = true;
        currentRecette = recette;
        if (lblModalTitle != null) lblModalTitle.setText("✏ Edit Recipe");
        fillForm(recette);
        showModal();
    }

    private void handleDelete(Recette recette) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Recipe");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to delete this recipe?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                recetteService.supprimer(recette.getId());
                loadRecipes();
                updateStats();
            }
        });
    }

    @FXML
    public void handleCloseDetails(javafx.event.ActionEvent event) {
        if (detailsOverlay != null) {
            detailsOverlay.setVisible(false);
            detailsOverlay.setManaged(false);
        }
    }

    private void handleViewDetails(Recette recette) {
        if (detailsOverlay == null) return;
        
        lblDetailsNom.setText(recette.getNom());
        lblDetailsType.setText(recette.getTypeLabel().toUpperCase());
        lblDetailsTemps.setText("⏱ " + recette.getTempsPreparation() + " min");
        lblDetailsDifficulte.setText("📊 " + recette.getDifficultyLabel());
        txtDetailsDescription.setText(recette.getDescription() != null ? recette.getDescription() : "No description available.");
        
        // Color for type badge
        String typeColor = switch(recette.getType().toLowerCase()) {
            case "entree" -> "#10B981";
            case "dessert" -> "#F43F5E";
            case "plat", "main dish" -> "#65A30D";
            default -> "#F59E0B";
        };
        lblDetailsType.setStyle("-fx-background-color: " + typeColor + "; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 10; -fx-font-weight: bold;");

        detailsStepsContainer.getChildren().clear();
        if (recette.getEtapes() != null) {
            int i = 1;
            for (String step : recette.getEtapes()) {
                HBox stepRow = new HBox(15);
                stepRow.setAlignment(Pos.TOP_LEFT);
                
                Label num = new Label(String.valueOf(i++));
                num.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-min-width: 24; -fx-min-height: 24; -fx-background-radius: 12; -fx-alignment: center; -fx-font-weight: bold;");
                
                Label text = new Label(step);
                text.setWrapText(true);
                text.setStyle("-fx-text-fill: #475569;");
                
                stepRow.getChildren().addAll(num, text);
                detailsStepsContainer.getChildren().add(stepRow);
            }
        }

        detailsOverlay.setVisible(true);
        detailsOverlay.setManaged(true);
        detailsOverlay.toFront();
    }

    @FXML
    public void handleCloseModal(javafx.event.ActionEvent event) {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        resetErrorLabels();
    }

    @FXML
    public void handleSave(javafx.event.ActionEvent event) {
        handleSaveAction();
    }

    private void handleSaveAction() {
        if (!validateForm()) {
            if (lblGeneralError != null) {
                lblGeneralError.setVisible(true);
                lblGeneralError.setManaged(true);
            }
            return;
        }

        Recette recette = isEditMode ? currentRecette : new Recette();

        recette.setNom(txtNom.getText().trim());
        
        // Match indexes with TYPES and DIFFICULTIES arrays
        int typeIdx = cmbType.getSelectionModel().getSelectedIndex();
        if (typeIdx >= 0) {
            recette.setType(TYPES[typeIdx]);
        }
        
        int diffIdx = cmbDifficulte.getSelectionModel().getSelectedIndex();
        if (diffIdx >= 0) {
            recette.setDifficulte(DIFFICULTIES[diffIdx]);
        }
        
        recette.setTempsPreparation(Integer.parseInt(txtTemps.getText().trim()));
        recette.setDescription(txtDescription.getText().trim());
        recette.setImage(txtImage.getText().trim());
        // === DIAGNOSTIC LOG ===
        tn.esprit.projet.models.User sessionUser = tn.esprit.projet.utils.SessionManager.getCurrentUser();
        System.out.println("[SAVE RECETTE] SessionManager.getCurrentUser() = " + 
            (sessionUser != null ? "id=" + sessionUser.getId() + " email=" + sessionUser.getEmail() : "NULL"));
        int resolvedUserId = getCurrentUserId();
        System.out.println("[SAVE RECETTE] resolvedUserId = " + resolvedUserId);
        recette.setUserId(resolvedUserId); // Use logged in user ID

        // Retrieve steps
        List<String> etapes = new ArrayList<>();
        for (TextField tf : stepFields) {
            if (!tf.getText().trim().isEmpty()) {
                etapes.add(tf.getText().trim());
            }
        }
        recette.setEtapes(etapes);

        // Retrieve Ingredients
        recette.setRecetteIngredients(new ArrayList<>(selectedIngredientsMap.values()));

        try {
            if (isEditMode) {
                recetteService.modifier(recette);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Recipe updated successfully!");
                alert.showAndWait();
            } else {
                recette.setCreatedAt(java.time.LocalDateTime.now());
                recetteService.ajouter(recette);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Recipe added successfully!");
                alert.showAndWait();
            }

            handleCloseModal(null);
            loadRecipes();
            updateStats();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database operation failed");
            alert.setContentText("The recipe could not be saved: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private void resetErrorLabels() {
        if (lblGeneralError != null) {
            lblGeneralError.setVisible(false);
            lblGeneralError.setManaged(false);
        }
        if (lblNomError != null) {
            lblNomError.setVisible(false);
            lblNomError.setManaged(false);
        }
        if (lblTypeError != null) {
            lblTypeError.setVisible(false);
            lblTypeError.setManaged(false);
        }
        if (lblImageError != null) {
            lblImageError.setVisible(false);
            lblImageError.setManaged(false);
        }
        if (lblTempsError != null) {
            lblTempsError.setVisible(false);
            lblTempsError.setManaged(false);
        }
        if (lblDifficulteError != null) {
            lblDifficulteError.setVisible(false);
            lblDifficulteError.setManaged(false);
        }
        if (lblDescriptionError != null) {
            lblDescriptionError.setVisible(false);
            lblDescriptionError.setManaged(false);
        }
    }

    private void showError(Label label, String message) {
        if (label == null) return;
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private boolean validateForm() {
        resetErrorLabels();
        boolean isValid = true;

        // Nom Validation: min 2, max 150, regex ^[a-zA-ZÀ-ÿ \-']+$
        String nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            showError(lblNomError, "The recipe name is required.");
            isValid = false;
        } else if (nom.length() < 2) {
            showError(lblNomError, "The name must contain at least 2 characters.");
            isValid = false;
        } else if (nom.length() > 150) {
            showError(lblNomError, "The name cannot exceed 150 characters.");
            isValid = false;
        } else if (!nom.matches("^[a-zA-ZÀ-ÿ \\-']+$")) {
            showError(lblNomError, "The name can only contain letters, spaces, apostrophes and hyphens.");
            isValid = false;
        }

        // Type Validation
        if (cmbType.getSelectionModel().getSelectedIndex() < 0) {
            showError(lblTypeError, "The dish type is required.");
            isValid = false;
        }

        // Difficulty Validation
        if (cmbDifficulte.getSelectionModel().getSelectedIndex() < 0) {
            showError(lblDifficulteError, "The difficulty level is required.");
            isValid = false;
        }

        // Temps Validation: Positive, <= 1440 min
        String tempsStr = txtTemps.getText().trim();
        if (tempsStr.isEmpty()) {
            showError(lblTempsError, "The preparation time is required.");
            isValid = false;
        } else {
            try {
                int t = Integer.parseInt(tempsStr);
                if (t <= 0) {
                    showError(lblTempsError, "The time must be greater than 0.");
                    isValid = false;
                } else if (t > 1440) {
                    showError(lblTempsError, "The time cannot exceed 24h (1440 min).");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showError(lblTempsError, "The time must be a number.");
                isValid = false;
            }
        }

        // Description Validation: max 1000
        String desc = txtDescription.getText().trim();
        if (desc.length() > 1000) {
            showError(lblDescriptionError, "The description cannot exceed 1000 characters.");
            isValid = false;
        }

        // Image Validation: URL regex, max 255
        String image = txtImage.getText().trim();
        if (!image.isEmpty()) {
            if (image.length() > 255) {
                showError(lblImageError, "The URL cannot exceed 255 characters.");
                isValid = false;
            } else if (!image.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$")) {
                showError(lblImageError, "The image URL is not valid.");
                isValid = false;
            }
        }

        // Ingredients Validation: check stock
        if (recipeIngredientsContainer != null) {
            for (javafx.scene.Node node : recipeIngredientsContainer.getChildren()) {
                if (node instanceof VBox rowContainer) {
                    Label errorLabel = (Label) rowContainer.getUserData();
                    HBox row = (HBox) rowContainer.getChildren().get(0);
                    TextField qtyField = (TextField) row.getChildren().get(1);
                    String qtyStr = qtyField.getText().trim();
                    
                    // Find which ingredient this is
                    Label nameLabel = (Label) row.getChildren().get(0);
                    String name = nameLabel.getText().replace("🥕 ", "");
                    
                    if (qtyStr.isEmpty()) {
                        showError(errorLabel, "Quantity is required.");
                        isValid = false;
                    } else {
                        double needed = extractQuantity(qtyStr);
                        if (needed <= 0) {
                            showError(errorLabel, "Invalid quantity.");
                            isValid = false;
                        } else {
                            // Fetch current stock from DB to be sure
                            // We need the ID. Let's find it from selectedIngredientsMap by name or store it in UserData
                            // Actually, let's store the ID in the rowContainer's properties or a custom object
                            Optional<RecetteIngredient> riOpt = selectedIngredientsMap.values().stream()
                                    .filter(ri -> ri.getIngredientNom().equals(name))
                                    .findFirst();
                            
                            if (riOpt.isPresent()) {
                                Ingredient ing = ingredientService.getById(riOpt.get().getIngredientId());
                                if (ing != null && ing.getQuantite() < needed) {
                                    showError(errorLabel, "Insufficient stock! (Available: " + ing.getQuantite() + " " + (ing.getUnite() != null ? ing.getUnite() : "") + ")");
                                    isValid = false;
                                }
                            }
                        }
                    }
                }
            }
        }

        // Steps Validation: not empty
        if (stepsContainer != null) {
            for (javafx.scene.Node node : stepsContainer.getChildren()) {
                if (node instanceof VBox stepContainer) {
                    Label errorLabel = (Label) stepContainer.getUserData();
                    HBox row = (HBox) stepContainer.getChildren().get(0);
                    TextField stepField = (TextField) row.getChildren().get(1);
                    
                    if (stepField.getText().trim().isEmpty()) {
                        showError(errorLabel, "Step description cannot be empty.");
                        isValid = false;
                    }
                }
            }
        }

        return isValid;
    }

    private double extractQuantity(String quantiteStr) {
        if (quantiteStr == null || quantiteStr.trim().isEmpty()) return 0;
        try {
            String numericPart = quantiteStr.replaceAll("[^0-9.]", "");
            if (numericPart.isEmpty()) return 0;
            return Double.parseDouble(numericPart);
        } catch (Exception e) {
            return 0;
        }
    }

    @FXML
    public void handleAddStep(javafx.event.ActionEvent event) {
        addStepField();
    }

    @FXML
    private void handleExport() {
        showToast("📥", "Export in progress...", "#3B82F6");
    }

    // ==================== FORM HELPERS ====================

    private void showModal() {
        if (modalOverlay != null) {
            modalOverlay.setVisible(true);
            modalOverlay.setManaged(true);
            modalOverlay.toFront(); // Ensure it's on top
        }
    }

    private void clearForm() {
        if (txtNom != null) txtNom.clear();
        if (cmbType != null) cmbType.setValue(null);
        if (txtImage != null) txtImage.clear();
        if (txtTemps != null) txtTemps.clear();
        if (cmbDifficulte != null) cmbDifficulte.setValue(null);
        if (txtDescription != null) txtDescription.clear();
        if (stepsContainer != null) stepsContainer.getChildren().clear();
        stepFields.clear();
        if (recipeIngredientsContainer != null) recipeIngredientsContainer.getChildren().clear();
        selectedIngredientsMap.clear();
        resetErrorLabels();
    }

    private void fillForm(Recette recette) {
        if (txtNom != null) txtNom.setText(recette.getNom());

        if (cmbType != null) {
            int typeIndex = java.util.Arrays.asList(TYPES).indexOf(recette.getType());
            if (typeIndex >= 0) {
                cmbType.setValue(TYPES_LABELS[typeIndex]);
            }
        }

        if (txtImage != null) txtImage.setText(recette.getImage() != null ? recette.getImage() : "");
        if (txtTemps != null) txtTemps.setText(String.valueOf(recette.getTempsPreparation()));

        if (cmbDifficulte != null) {
            int diffIndex = java.util.Arrays.asList(DIFFICULTIES).indexOf(recette.getDifficulte());
            if (diffIndex >= 0) {
                cmbDifficulte.setValue(DIFFICULTIES_LABELS[diffIndex]);
            }
        }

        if (txtDescription != null) txtDescription.setText(recette.getDescription() != null ? recette.getDescription() : "");

        // Ingredients
        if (recipeIngredientsContainer != null) {
            recipeIngredientsContainer.getChildren().clear();
            selectedIngredientsMap.clear();
            if (recette.getRecetteIngredients() != null) {
                for (RecetteIngredient ri : recette.getRecetteIngredients()) {
                    Ingredient ing = ingredientService.getById(ri.getIngredientId());
                    if (ing != null) {
                        addIngredientToRecipe(ing, ri.getQuantite());
                    }
                }
            }
        }

        // Steps
        if (stepsContainer != null) {
            stepsContainer.getChildren().clear();
            stepFields.clear();
            if (recette.getEtapes() != null && !recette.getEtapes().isEmpty()) {
                for (String etape : recette.getEtapes()) {
                    addStepField(etape);
                }
            } else {
                addStepField();
            }
        }
        resetErrorLabels();
    }

    private void addIngredientToRecipe(Ingredient ingredient, String quantite) {
        if (selectedIngredientsMap.containsKey(ingredient.getId())) return;

        RecetteIngredient ri = new RecetteIngredient();
        ri.setIngredientId(ingredient.getId());
        ri.setIngredientNom(ingredient.getNom());
        ri.setQuantite(quantite);
        selectedIngredientsMap.put(ingredient.getId(), ri);

        VBox rowContainer = new VBox(5);
        
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10 15; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");

        Label nameLabel = new Label("🥕 " + ingredient.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-font-size: 14px;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        TextField qtyField = new TextField(quantite);
        qtyField.setPromptText("Quantity (e.g. 200g)");
        qtyField.setPrefWidth(150);
        qtyField.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 11px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        qtyField.textProperty().addListener((obs, old, newVal) -> {
            ri.setQuantite(newVal);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });

        Button btnRemove = new Button("✕");
        btnRemove.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 16px; -fx-cursor: hand;");
        btnRemove.setOnAction(e -> {
            recipeIngredientsContainer.getChildren().remove(rowContainer);
            selectedIngredientsMap.remove(ingredient.getId());
        });

        row.getChildren().addAll(nameLabel, qtyField, btnRemove);
        rowContainer.getChildren().addAll(row, errorLabel);
        
        // Store error label in the RecetteIngredient or a map? 
        // Let's use a dynamic property or a Map for validation later.
        rowContainer.setUserData(errorLabel); // Useful for validation
        
        recipeIngredientsContainer.getChildren().add(rowContainer);
    }

    private void addStepField() {
        addStepField("");
    }

    private void addStepField(String value) {
        if (stepsContainer == null) return;
        
        int stepNum = stepFields.size() + 1;

        VBox stepContainer = new VBox(5);
        HBox stepRow = new HBox(12);
        stepRow.setAlignment(Pos.CENTER_LEFT);

        Label numLabel = new Label(String.valueOf(stepNum));
        numLabel.setStyle(
                "-fx-background-color: #F59E0B; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: 900; " +
                        "-fx-font-size: 13px; " +
                        "-fx-min-width: 32; " +
                        "-fx-min-height: 32; " +
                        "-fx-max-width: 32; " +
                        "-fx-max-height: 32; " +
                        "-fx-alignment: center; " +
                        "-fx-background-radius: 16;"
        );
        numLabel.setAlignment(Pos.CENTER);

        TextField stepField = new TextField(value);
        stepField.setPromptText("Describe step " + stepNum + "...");
        stepField.setPrefHeight(48);
        HBox.setHgrow(stepField, Priority.ALWAYS);
        stepField.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-padding: 12 18; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #E2E8F0; " +
                        "-fx-border-radius: 12; " +
                        "-fx-background-color: #F8FAFC;"
        );
        stepFields.add(stepField);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 11px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        stepField.textProperty().addListener((obs, old, newVal) -> {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });

        Button btnRemove = new Button("✕");
        btnRemove.setStyle(
                "-fx-background-color: #FEE2E2; " +
                        "-fx-text-fill: #DC2626; " +
                        "-fx-font-size: 14px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-min-width: 40; " +
                        "-fx-min-height: 40; " +
                        "-fx-cursor: hand;"
        );
        btnRemove.setOnAction(e -> {
            stepsContainer.getChildren().remove(stepContainer);
            stepFields.remove(stepField);
            updateStepNumbers();
        });

        stepRow.getChildren().addAll(numLabel, stepField, btnRemove);
        stepContainer.getChildren().addAll(stepRow, errorLabel);
        stepContainer.setUserData(errorLabel);
        
        stepsContainer.getChildren().add(stepContainer);
    }

    private void updateStepNumbers() {
        for (int i = 0; i < stepsContainer.getChildren().size(); i++) {
            VBox container = (VBox) stepsContainer.getChildren().get(i);
            HBox row = (HBox) container.getChildren().get(0);
            Label numLabel = (Label) row.getChildren().get(0);
            numLabel.setText(String.valueOf(i + 1));
            
            TextField field = (TextField) row.getChildren().get(1);
            field.setPromptText("Describe step " + (i + 1) + "...");
        }
    }

    // ==================== TOAST ====================

    private void showToast(String icon, String message, String color) {
        if (lblToastIcon != null) {
            lblToastIcon.setText(icon);
            lblToastIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: " + color + ";");
        }
        if (lblToastMessage != null) {
            lblToastMessage.setText(message);
        }

        if (toastNotification != null) {
            toastNotification.setVisible(true);
            toastNotification.setManaged(true);

            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        toastNotification.setVisible(false);
                        toastNotification.setManaged(false);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }


    @FXML
    private void handleOpenHistorique() {
        try {
            // Vérifier s'il existe un plan actif
            MealPlanService planService = new MealPlanService();
            MealPlan planActif = planService.getPlanActif(1); // userId

            if (planActif == null) {
                // Pas de plan → afficher alerte
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No meal plan");
                alert.setHeaderText(null);
                alert.setContentText("You haven't generated any meal plan yet.\nUse Smart Meal Planner first!");
                alert.showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/meal_plan_tracking.fxml"));
            Parent root = loader.load();

            MealPlanTrackingController ctrl = loader.getController();
            ctrl.setPlanId(planActif.getId());

            Stage stage = new Stage();
            stage.setTitle("Meal Plan Tracking");
            stage.setScene(new Scene(root, 900, 700));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ handleOpenHistorique : " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ─── Ouvrir Meal Planner ──────────────────────────
    @FXML
    private void handleOpenMealPlanner() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/meal_planner_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Smart Meal Planner");
            stage.setScene(new Scene(root, 900, 700));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ handleOpenMealPlanner : " + e.getMessage());
        }
    }

    // ─── Ouvrir AI Generator ──────────────────────────
    @FXML
    private void handleOpenAIGenerator() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/ai_recipe_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("AI Recipe Chef");
            stage.setScene(new Scene(root, 800, 700));
            stage.initModality(Modality.APPLICATION_MODAL);

            // ✅ FIX : Rafraîchir tableau quand fenêtre AI se ferme
            stage.setOnHidden(e -> {
                handleRefresh(null);
                System.out.println("✅ Tableau rafraîchi après fermeture AI");
            });

            stage.show();

        } catch (IOException e) {
            System.err.println("❌ handleOpenAIGenerator : " + e.getMessage());
        }
    }

    // ==================== RECOMMENDATION SYSTEM ====================

    private void loadRecommendations() {
        if (recommendationSection == null) return;

        // Afficher la section
        recommendationSection.setVisible(true);
        recommendationSection.setManaged(true);

        // Afficher l'état loading
        showRecommendationLoading();

        // Calcul en arrière-plan
        Thread thread = new Thread(() -> {
            try {
                // Calcul IA (peut prendre un peu de temps)
                List<RecipeRecommendation> recommendations =
                        recommendationService.getRecommendations(getCurrentUserId(), 5);

                // Retour sur le thread JavaFX
                Platform.runLater(() -> {
                    if (recommendations.isEmpty()) {
                        showRecommendationEmpty();
                    } else {
                        showRecommendationResults(recommendations);
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ Erreur chargement recommandations: " + e.getMessage());
                Platform.runLater(this::showRecommendationEmpty);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    private void showRecommendationLoading() {
        if (recommendationLoading != null) {
            recommendationLoading.setVisible(true);
            recommendationLoading.setManaged(true);
        }
        if (recommendationEmpty != null) {
            recommendationEmpty.setVisible(false);
            recommendationEmpty.setManaged(false);
        }
        if (recommendationHeader != null) {
            recommendationHeader.setVisible(false);
            recommendationHeader.setManaged(false);
        }
        if (recommendationCardsContainer != null) {
            recommendationCardsContainer.setVisible(false);
            recommendationCardsContainer.setManaged(false);
        }
    }


    private void showRecommendationEmpty() {
        if (recommendationLoading != null) {
            recommendationLoading.setVisible(false);
            recommendationLoading.setManaged(false);
        }
        if (recommendationEmpty != null) {
            recommendationEmpty.setVisible(true);
            recommendationEmpty.setManaged(true);
        }
        if (recommendationHeader != null) {
            recommendationHeader.setVisible(false);
            recommendationHeader.setManaged(false);
        }
        if (recommendationCardsContainer != null) {
            recommendationCardsContainer.setVisible(false);
            recommendationCardsContainer.setManaged(false);
        }
    }


    private void showRecommendationResults(List<RecipeRecommendation> recommendations) {
        if (recommendationLoading != null) {
            recommendationLoading.setVisible(false);
            recommendationLoading.setManaged(false);
        }
        if (recommendationEmpty != null) {
            recommendationEmpty.setVisible(false);
            recommendationEmpty.setManaged(false);
        }
        if (recommendationHeader != null) {
            recommendationHeader.setVisible(true);
            recommendationHeader.setManaged(true);
        }
        if (recommendationCardsContainer != null) {
            recommendationCardsContainer.setVisible(true);
            recommendationCardsContainer.setManaged(true);
            recommendationCardsContainer.getChildren().clear();

            for (RecipeRecommendation reco : recommendations) {
                VBox card = createRecommendationCard(reco);
                recommendationCardsContainer.getChildren().add(card);
            }
        }
    }



    private VBox createRecommendationCard(RecipeRecommendation reco) {
        Recette recette = reco.getRecette();

        // ── Carte principale ─────────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setPrefWidth(260);
        card.setMaxWidth(260);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #E2E8F0;" +
                        "-fx-border-radius: 16;"
        );
        card.setEffect(new DropShadow(10, 0, 4, Color.web("#00000015")));

        // ── Image + Badges ────────────────────────────────────────────────────
        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(160);
        imageArea.setStyle("-fx-background-radius: 16 16 0 0; -fx-background-color: #F8FAFC;");

        // Image
        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        String imageUrl = (recette.getImage() == null || recette.getImage().isEmpty())
                ? "https://images.unsplash.com/photo-1495521821757-a1efb6729352?w=400"
                : recette.getImage();

        javafx.scene.image.Image img = new javafx.scene.image.Image(
                imageUrl, 520, 320, true, true, true
        );
        imageView.setImage(img);
        imageView.setFitWidth(260);
        imageView.setFitHeight(160);

        // Clip arrondi image
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(260, 160);
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        imageArea.setClip(clip);

        // Badge Score (coin haut gauche)
        Label scoreBadge = new Label(reco.getBadgeEmoji() + " " + reco.getMatchPercent() + "% Match");
        scoreBadge.setStyle(
                "-fx-background-color: " + reco.getBadgeBackgroundColor() + ";" +
                        "-fx-text-fill: " + reco.getBadgeColor() + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 8;"
        );
        StackPane.setAlignment(scoreBadge, Pos.TOP_LEFT);
        StackPane.setMargin(scoreBadge, new Insets(10));

        // Badge AI Pick (coin haut droit)
        Label aiBadge = new Label("🤖 AI Pick");
        aiBadge.setStyle(
                "-fx-background-color: #1F4D3A;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 8;"
        );
        StackPane.setAlignment(aiBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(aiBadge, new Insets(10));

        imageArea.getChildren().addAll(imageView, scoreBadge, aiBadge);


        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // Titre recette
        Label title = new Label(recette.getNom());
        title.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-text-fill: #1E293B;"
        );
        title.setWrapText(true);

        // Métadonnées (type + temps)
        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label typeLbl = new Label(recette.getTypeIcon() + " " + recette.getTypeLabel());
        typeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: #CBD5E1;");

        Label timeLbl = new Label("⏱ " + recette.getTempsPreparation() + " min");
        timeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        meta.getChildren().addAll(typeLbl, dot, timeLbl);

        // Difficulté
        Label diffLbl = new Label(recette.getDifficultyIcon() + " " + recette.getDifficultyLabel());
        diffLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E2E8F0;");

        // Statut ingrédients
        Label ingredientStatus = new Label(reco.getIngredientStatusText());
        ingredientStatus.setWrapText(true);
        ingredientStatus.setStyle(
                "-fx-background-color: " + reco.getIngredientStatusBackground() + ";" +
                        "-fx-text-fill: " + reco.getIngredientStatusColor() + ";" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 6 10;" +
                        "-fx-background-radius: 6;"
        );

        // Bouton See Recipe
        Button btnSee = new Button("See Recipe →");
        btnSee.setMaxWidth(Double.MAX_VALUE);
        btnSee.setPrefHeight(40);
        btnSee.setStyle(
                "-fx-background-color: #1F4D3A;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        );
        btnSee.setOnAction(e -> handleViewDetails(recette));

        btnSee.setOnMouseEntered(e -> btnSee.setStyle(
                "-fx-background-color: #2E7D5A;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        ));
        btnSee.setOnMouseExited(e -> btnSee.setStyle(
                "-fx-background-color: #1F4D3A;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 900;" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-radius: 10;" +
                        "-fx-cursor: hand;"
        ));

        content.getChildren().addAll(title, meta, diffLbl, sep, ingredientStatus, btnSee);
        card.getChildren().addAll(imageArea, content);

        return card;
    }


    @FXML
    private void handleRefreshRecommendations(javafx.event.ActionEvent event) {
        loadRecommendations();
    }

    @FXML
    private void handleBrowseRecipesForReco(javafx.event.ActionEvent event) {
        // Reset tous les filtres pour montrer toutes les recettes
        if (txtSearch != null) txtSearch.clear();
        currentTypeFilter = "All";
        showFavoritesOnly = false;
        currentPage = 1;
        if (filterPillsContainer != null) renderTypePills();
        applyFilters();
    }

}