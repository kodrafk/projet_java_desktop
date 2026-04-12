package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.projet.models.DailyLog;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.DailyLogService;
import tn.esprit.projet.services.NutritionObjectiveService;

import java.util.ArrayList;
import java.util.List;

public class DailyLogEditController {

    @FXML private Label lblMealType, lblHeaderTitle, lblHeaderSub;
    @FXML private Label trackerCal, trackerCalTarget, trackerProtein, trackerProteinTarget;
    @FXML private Label trackerCarbs, trackerCarbsTarget, trackerFats, trackerFatsTarget;
    @FXML private ProgressBar trackerProgress;
    @FXML private TextField searchField;
    @FXML private HBox categoryTabs;
    @FXML private FlowPane foodGrid;
    @FXML private TextField customNameField, customCalField, customProteinField, customCarbsField, customFatsField;
    @FXML private Label customNameError, customCalError, customProteinError, customCarbsError, customFatsError;
    @FXML private Button btnAddCustom;
    @FXML private VBox selectedFoodsList;
    @FXML private Label selectedCount;

    private NutritionObjective objective;
    private DailyLog log;
    private String mealType;
    private DailyLogService service;
    private String currentCategory = "All";
    private Object[] editingCustomFood = null;

    private static final Object[][] FOODS = {
        {"Chicken Breast", "Meat",       165, 31.0,  0.0,  3.6},
        {"Beef Steak",     "Meat",       250, 26.0,  0.0, 15.0},
        {"Turkey",         "Meat",       189, 29.0,  0.0,  7.0},
        {"Salmon",         "Fish",       208, 20.0,  0.0, 13.0},
        {"Tuna",           "Fish",       132, 29.0,  0.0,  1.0},
        {"Shrimp",         "Fish",        99, 24.0,  0.0,  0.3},
        {"Brown Rice",     "Grains",     216,  5.0, 45.0,  1.8},
        {"Oats",           "Grains",     150,  5.0, 27.0,  3.0},
        {"Pasta",          "Grains",     220,  8.0, 43.0,  1.3},
        {"Whole Bread",    "Grains",      79,  4.0, 15.0,  1.0},
        {"Broccoli",       "Vegetables",  55,  3.7, 11.0,  0.6},
        {"Sweet Potato",   "Vegetables", 103,  2.3, 24.0,  0.1},
        {"Spinach",        "Vegetables",  23,  2.9,  3.6,  0.4},
        {"Tomato",         "Vegetables",  18,  0.9,  3.9,  0.2},
        {"Banana",         "Fruits",      89,  1.1, 23.0,  0.3},
        {"Apple",          "Fruits",      95,  0.5, 25.0,  0.3},
        {"Avocado",        "Fruits",     160,  2.0,  9.0, 15.0},
        {"Blueberries",    "Fruits",      57,  0.7, 14.0,  0.3},
        {"Egg",            "Protein",     78,  6.0,  0.6,  5.0},
        {"Greek Yogurt",   "Dairy",      100, 17.0,  6.0,  0.7},
        {"Milk",           "Dairy",      149,  8.0, 12.0,  8.0},
        {"Cheese",         "Dairy",      113,  7.0,  0.4,  9.0},
        {"Almonds",        "Nuts",       164,  6.0,  6.0, 14.0},
        {"Peanut Butter",  "Nuts",       188,  8.0,  6.0, 16.0},
        {"Walnuts",        "Nuts",       185,  4.3,  4.0, 18.0},
    };

    private final List<Object[]> selectedFoods = new ArrayList<>();

    // ═══ INIT ═══
    public void setData(NutritionObjective obj, DailyLog dailyLog, String mealType) {
        this.objective = obj;
        this.log = dailyLog;
        this.mealType = mealType;
        this.service = new DailyLogService();

        String mealLabel = getMealLabel(mealType);
        lblMealType.setText(mealLabel.toUpperCase());
        lblHeaderTitle.setText("Pick Your " + mealLabel);
        lblHeaderSub.setText("Day " + log.getDayNumber() + " — " + objective.getTitle());

        trackerCalTarget.setText("/ " + objective.getTargetCalories());
        trackerProteinTarget.setText("/ " + (int) objective.getTargetProtein() + "g");
        trackerCarbsTarget.setText("/ " + (int) objective.getTargetCarbs() + "g");
        trackerFatsTarget.setText("/ " + (int) objective.getTargetFats() + "g");

        // Load previously saved foods
        if (dailyLog.isMealLogged(mealType)) {
            DailyLog.MealData existing = dailyLog.getMealData(mealType);
            if (existing != null && existing.logged) {
                if (existing.foodNames != null && !existing.foodNames.isEmpty()) {
                    for (String foodName : existing.foodNames) {
                        boolean found = false;
                        for (Object[] food : FOODS) {
                            if (food[0].equals(foodName)) {
                                selectedFoods.add(food);
                                found = true;
                                break;
                            }
                        }
                        if (!found && existing.calories > 0) {
                            int perFood = existing.calories / Math.max(1, existing.foodNames.size());
                            double perPro = existing.protein / Math.max(1, existing.foodNames.size());
                            double perCarb = existing.carbs / Math.max(1, existing.foodNames.size());
                            double perFat = existing.fats / Math.max(1, existing.foodNames.size());
                            selectedFoods.add(new Object[]{foodName, "Custom", perFood, perPro, perCarb, perFat});
                        }
                    }
                } else if (existing.calories > 0) {
                    selectedFoods.add(new Object[]{mealLabel, "Saved",
                        existing.calories, existing.protein, existing.carbs, existing.fats});
                }
            }
        }

        buildCategoryTabs("All");
        buildFoodGrid("All", "");
        updateSidebar();
        updateTracker();
        searchField.textProperty().addListener((obs, o, n) -> buildFoodGrid(currentCategory, n));
    }

    // ═══ CATEGORY TABS ═══
    private void buildCategoryTabs(String active) {
        currentCategory = active;
        categoryTabs.getChildren().clear();
        for (String cat : new String[]{"All","Meat","Fish","Grains","Vegetables","Fruits","Dairy","Nuts","Protein"}) {
            Button tab = new Button(cat);
            boolean isActive = cat.equals(active);
            tab.setStyle(isActive
                ? "-fx-background-color: #eb7147; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 7 16;"
                : "-fx-background-color: white; -fx-text-fill: #374151; -fx-border-color: #E2E8F0; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 7 16;");
            tab.setOnAction(e -> { buildCategoryTabs(cat); buildFoodGrid(cat, searchField.getText()); });
            categoryTabs.getChildren().add(tab);
        }
    }

    // ═══ FOOD GRID ═══
    private void buildFoodGrid(String category, String search) {
        foodGrid.getChildren().clear();
        String q = search == null ? "" : search.toLowerCase().trim();

        for (Object[] food : FOODS) {
            String name = (String) food[0];
            String cat  = (String) food[1];
            if (!category.equals("All") && !cat.equals(category)) continue;
            if (!q.isEmpty() && !name.toLowerCase().contains(q)) continue;

            int cal        = (int) food[2];
            double protein = (double) food[3];
            double carbs   = (double) food[4];
            double fats    = (double) food[5];
            boolean isSelected = selectedFoods.stream().anyMatch(f -> f[0].equals(name));

            VBox card = new VBox(8);
            card.setPrefWidth(195);
            String border = isSelected ? "-fx-border-color: #2E7D5A; -fx-border-width: 2;" : "-fx-border-color: #E2E8F0;";
            card.setStyle("-fx-background-color: " + (isSelected ? "rgba(46,125,90,0.05)" : "#F8FAFC")
                + "; -fx-background-radius: 12; -fx-padding: 14; " + border + " -fx-border-radius: 12; -fx-cursor: hand;");

            HBox nameRow = new HBox(8);
            nameRow.setAlignment(Pos.CENTER_LEFT);
            Label check = new Label(isSelected ? "✓" : "○");
            check.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (isSelected ? "#2E7D5A" : "#CBD5E1") + ";");
            Label nameLbl = new Label(name);
            nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            nameRow.getChildren().addAll(check, nameLbl);

            Label macros = new Label("🔥 " + cal + "  💪 " + (int)protein + "g  🌾 " + (int)carbs + "g  🥑 " + (int)fats + "g");
            macros.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
            card.getChildren().addAll(nameRow, macros);

            card.setOnMouseClicked(e -> {
                if (isSelected) selectedFoods.removeIf(f -> f[0].equals(name));
                else selectedFoods.add(new Object[]{name, cat, cal, protein, carbs, fats});
                buildFoodGrid(currentCategory, searchField.getText());
                updateSidebar();
                updateTracker();
            });
            foodGrid.getChildren().add(card);
        }

        if (foodGrid.getChildren().isEmpty()) {
            Label empty = new Label("No foods found" + (q.isEmpty() ? "" : " for \"" + q + "\""));
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-padding: 20;");
            foodGrid.getChildren().add(empty);
        }
    }

    // ═══ SIDEBAR ═══
    private void updateSidebar() {
        selectedFoodsList.getChildren().clear();
        selectedCount.setText(String.valueOf(selectedFoods.size()));

        if (selectedFoods.isEmpty()) {
            Label empty = new Label("No foods selected yet.\nClick a food card to add it.");
            empty.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8; -fx-text-alignment: center; -fx-wrap-text: true;");
            empty.setAlignment(Pos.CENTER);
            selectedFoodsList.getChildren().add(empty);
            return;
        }

        for (Object[] food : new ArrayList<>(selectedFoods)) {
            String name    = (String) food[0];
            int cal        = (int) food[2];
            double pro     = (double) food[3];
            double carb    = (double) food[4];
            double fat     = (double) food[5];
            boolean isCustom = "Custom".equals(food[1]);

            HBox item = new HBox(6);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 10 10; "
                    + "-fx-border-color: " + (isCustom ? "rgba(235,113,71,0.3)" : "#E2E8F0") + "; -fx-border-radius: 10;");

            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);
            Label nameLbl = new Label(name);
            nameLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            Label macros = new Label("🔥 " + cal + "  💪 " + (int) pro + "g  🌾 " + (int) carb + "g  🥑 " + (int) fat + "g");
            macros.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");
            info.getChildren().addAll(nameLbl, macros);
            item.getChildren().add(info);

            if (isCustom) {
                Button edit = new Button("✏");
                edit.setStyle("-fx-background-color: rgba(235,113,71,0.1); -fx-text-fill: #eb7147; "
                        + "-fx-background-radius: 50%; -fx-cursor: hand; -fx-padding: 4 7; -fx-font-size: 11px;");
                edit.setOnAction(e -> populateCustomFormForEdit(food));
                item.getChildren().add(edit);
            }

            Button remove = new Button("✕");
            remove.setStyle("-fx-background-color: rgba(220,53,69,0.1); -fx-text-fill: #DC2626; "
                    + "-fx-background-radius: 50%; -fx-cursor: hand; -fx-padding: 4 7; -fx-font-weight: bold; -fx-font-size: 11px;");
            remove.setOnAction(e -> {
                selectedFoods.removeIf(f -> f[0].equals(name));
                buildFoodGrid(currentCategory, searchField.getText());
                updateSidebar();
                updateTracker();
            });
            item.getChildren().add(remove);

            selectedFoodsList.getChildren().add(item);
        }
    }

    // ═══ LIVE TRACKER ═══
    private void updateTracker() {
        int totalCal    = selectedFoods.stream().mapToInt(f -> (int) f[2]).sum();
        double totalPro = selectedFoods.stream().mapToDouble(f -> (double) f[3]).sum();
        double totalCarb = selectedFoods.stream().mapToDouble(f -> (double) f[4]).sum();
        double totalFat  = selectedFoods.stream().mapToDouble(f -> (double) f[5]).sum();

        trackerCal.setText(String.valueOf(totalCal));
        trackerProtein.setText((int) totalPro + "g");
        trackerCarbs.setText((int) totalCarb + "g");
        trackerFats.setText((int) totalFat + "g");

        boolean over = objective.getTargetCalories() > 0 && totalCal > objective.getTargetCalories();
        trackerCal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + (over ? "#DC2626" : "#eb7147") + ";");

        double progress = objective.getTargetCalories() > 0
                ? Math.min(1.0, totalCal / (double) objective.getTargetCalories()) : 0;
        trackerProgress.setProgress(progress);
        trackerProgress.setStyle(over ? "-fx-accent: #DC2626;" : "-fx-accent: #eb7147;");
    }

    // ═══ CUSTOM FOOD WITH VALIDATION ═══
    private void populateCustomFormForEdit(Object[] food) {
        editingCustomFood = food;
        customNameField.setText((String) food[0]);
        customCalField.setText(String.valueOf((int) food[2]));
        customProteinField.setText(String.valueOf((int)(double) food[3]));
        customCarbsField.setText(String.valueOf((int)(double) food[4]));
        customFatsField.setText(String.valueOf((int)(double) food[5]));
        btnAddCustom.setText("✓ Update Custom Food");
        btnAddCustom.setStyle("-fx-background-color: #2E7D5A; -fx-text-fill: white; "
                + "-fx-background-radius: 10; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
    }
    @FXML
    private void handleAddCustomFood() {
        boolean valid = true;

        // Clear previous errors
        clearError(customNameError);
        clearError(customCalError);
        clearError(customProteinError);
        clearError(customCarbsError);
        clearError(customFatsError);

        String name = customNameField.getText().trim();
        String calStr     = customCalField.getText().trim();
        String proteinStr = customProteinField.getText().trim();
        String carbsStr   = customCarbsField.getText().trim();
        String fatsStr    = customFatsField.getText().trim();

        // Name validation
        if (name.isEmpty()) {
            showError(customNameError, "Food name is required.");
            valid = false;
        } else if (name.length() < 2) {
            showError(customNameError, "Name must be at least 2 characters.");
            valid = false;
        } else if (name.length() > 100) {
            showError(customNameError, "Name must not exceed 100 characters.");
            valid = false;
        }

        // Calories validation
        int cal = 0;
        if (calStr.isEmpty()) {
            showError(customCalError, "Calories is required.");
            valid = false;
        } else {
            try {
                cal = Integer.parseInt(calStr);
                if (cal < 1 || cal > 2000) {
                    showError(customCalError, "Must be between 1 and 2000 kcal.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showError(customCalError, "Enter a valid whole number.");
                valid = false;
            }
        }

        // Protein validation (required)
        double protein = 0;
        if (proteinStr.isEmpty()) {
            showError(customProteinError, "Protein is required.");
            valid = false;
        } else {
            try {
                protein = Double.parseDouble(proteinStr);
                if (protein < 0 || protein > 300) {
                    showError(customProteinError, "Must be between 0 and 300g.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showError(customProteinError, "Enter a valid number.");
                valid = false;
            }
        }

        // Carbs validation (required)
        double carbs = 0;
        if (carbsStr.isEmpty()) {
            showError(customCarbsError, "Carbs is required.");
            valid = false;
        } else {
            try {
                carbs = Double.parseDouble(carbsStr);
                if (carbs < 0 || carbs > 500) {
                    showError(customCarbsError, "Must be between 0 and 500g.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showError(customCarbsError, "Enter a valid number.");
                valid = false;
            }
        }

        // Fats validation (required)
        double fats = 0;
        if (fatsStr.isEmpty()) {
            showError(customFatsError, "Fats is required.");
            valid = false;
        } else {
            try {
                fats = Double.parseDouble(fatsStr);
                if (fats < 0 || fats > 150) {
                    showError(customFatsError, "Must be between 0 and 150g.");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showError(customFatsError, "Enter a valid number.");
                valid = false;
            }
        }

        if (!valid) return;

        // If editing an existing custom food, remove it first
        if (editingCustomFood != null) {
            final Object[] toRemove = editingCustomFood;
            selectedFoods.removeIf(f -> f == toRemove);
            editingCustomFood = null;
            btnAddCustom.setText("+ Add Custom Food");
            btnAddCustom.setStyle("-fx-background-color: #eb7147; -fx-text-fill: white; "
                    + "-fx-background-radius: 10; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        }

        // Add to selected — strip existing (custom) suffix to avoid doubling
        String baseName = name.endsWith(" (custom)") ? name.substring(0, name.length() - 9).trim() : name;
        selectedFoods.add(new Object[]{baseName + " (custom)", "Custom", cal, protein, carbs, fats});

        // Clear fields
        customNameField.clear();
        customCalField.clear();
        customProteinField.clear();
        customCarbsField.clear();
        customFatsField.clear();

        updateSidebar();
        updateTracker();
    }

    private void showError(Label lbl, String msg) {
        if (lbl != null) {
            lbl.setText("⚠ " + msg);
            lbl.setVisible(true);
            lbl.setManaged(true);
        }
    }

    private void clearError(Label lbl) {
        if (lbl != null) {
            lbl.setText("");
            lbl.setVisible(false);
            lbl.setManaged(false);
        }
    }

    // ═══ SAVE MEAL ═══
    @FXML
    private void handleSaveMeal() {
        if (selectedFoods.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select at least one food.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        int totalCal    = selectedFoods.stream().mapToInt(f -> (int) f[2]).sum();
        double protein  = selectedFoods.stream().mapToDouble(f -> (double) f[3]).sum();
        double carbs    = selectedFoods.stream().mapToDouble(f -> (double) f[4]).sum();
        double fats     = selectedFoods.stream().mapToDouble(f -> (double) f[5]).sum();
        List<String> foodNames = selectedFoods.stream()
                .map(f -> (String) f[0])
                .collect(java.util.stream.Collectors.toList());

        log.setMealMacros(mealType, totalCal, protein, carbs, fats, foodNames);
        service.update(log);

        // Check if all 7 logs completed
        List<DailyLog> allLogs = service.getByObjectiveId(objective.getId());
        long completedCount = allLogs.stream().filter(DailyLog::isCompleted).count();
        if (completedCount >= 7) {
            objective.setStatus("completed");
            new NutritionObjectiveService().update(objective);
        }

        navigateToOverview();
    }

    @FXML
    private void handleBack() { navigateToOverview(); }

    private void navigateToOverview() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/daily_log_overview.fxml"));
            Parent page = loader.load();
            DailyLogOverviewController ctrl = loader.getController();
            DailyLog updated = service.getById(log.getId());
            ctrl.setData(objective, updated != null ? updated : log);
            StackPane contentArea = (StackPane) foodGrid.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getMealLabel(String type) {
        return switch (type) {
            case "breakfast" -> "Breakfast";
            case "lunch"     -> "Lunch";
            case "dinner"    -> "Dinner";
            case "snacks"    -> "Snacks";
            default          -> "Meal";
        };
    }
}
