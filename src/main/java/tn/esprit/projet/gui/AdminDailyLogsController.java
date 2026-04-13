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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminDailyLogsController {

    // Standard foods reference (mirrors DailyLogEditController.FOODS)
    private static final Object[][] FOODS = {
        {"Chicken Breast",165,31.0, 0.0, 3.6},{"Beef Steak",250,26.0, 0.0,15.0},
        {"Turkey",        189,29.0, 0.0, 7.0},{"Salmon",    208,20.0, 0.0,13.0},
        {"Tuna",          132,29.0, 0.0, 1.0},{"Shrimp",     99,24.0, 0.0, 0.3},
        {"Brown Rice",    216, 5.0,45.0, 1.8},{"Oats",      150, 5.0,27.0, 3.0},
        {"Pasta",         220, 8.0,43.0, 1.3},{"Whole Bread", 79, 4.0,15.0, 1.0},
        {"Broccoli",       55, 3.7,11.0, 0.6},{"Sweet Potato",103,2.3,24.0, 0.1},
        {"Spinach",        23, 2.9, 3.6, 0.4},{"Tomato",      18, 0.9, 3.9, 0.2},
        {"Banana",         89, 1.1,23.0, 0.3},{"Apple",       95, 0.5,25.0, 0.3},
        {"Avocado",       160, 2.0, 9.0,15.0},{"Blueberries", 57, 0.7,14.0, 0.3},
        {"Egg",            78, 6.0, 0.6, 5.0},{"Greek Yogurt",100,17.0, 6.0, 0.7},
        {"Milk",          149, 8.0,12.0, 8.0},{"Cheese",     113, 7.0, 0.4, 9.0},
        {"Almonds",       164, 6.0, 6.0,14.0},{"Peanut Butter",188,8.0,6.0,16.0},
        {"Walnuts",       185, 4.3, 4.0,18.0},
    };

    @FXML private Label lblObjectiveTitle;
    @FXML private VBox logsContainer;
    @FXML private VBox editPanel;

    // Edit fields
    @FXML private TextField editCalField, editProteinField, editCarbsField, editFatsField;
    @FXML private Label errCal, errProtein, errCarbs, errFats;

    private NutritionObjective objective;
    private DailyLogService service;
    private List<DailyLog> logs;
    private DailyLog editingLog;
    private StackPane contentArea;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE, MMM d");

    public void setObjective(NutritionObjective obj, StackPane contentArea) {
        this.objective = obj;
        this.contentArea = contentArea;
        this.service = new DailyLogService();
        lblObjectiveTitle.setText(obj.getTitle() + " — Daily Logs");
        reload();
    }

    private void reload() {
        logs = service.getByObjectiveId(objective.getId());
        // Create logs if missing and objective is active
        if (logs.isEmpty() && objective.isActive() && objective.getStartDate() != null) {
            service.createLogsForObjective(objective.getId(), objective.getStartDate());
            logs = service.getByObjectiveId(objective.getId());
        }
        render();
    }

    private void render() {
        logsContainer.getChildren().clear();

        if (logs.isEmpty()) {
            Label empty = new Label("No daily logs found for this objective.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-padding: 24;");
            logsContainer.getChildren().add(empty);
            return;
        }

        for (DailyLog log : logs) {
            logsContainer.getChildren().add(buildRow(log));
        }
    }

    private HBox buildRow(DailyLog log) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #F1F5F9 transparent; -fx-padding: 12 26;");

        Label day = new Label("Day " + log.getDayNumber());
        day.setPrefWidth(60);
        day.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Label date = new Label(log.getDate() != null ? log.getDate().format(FMT) : "—");
        date.setPrefWidth(120);
        date.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        String statusText = log.isCompleted() ? "✓ Logged" : log.isToday() ? "● Today" : log.isFuture() ? "Upcoming" : "Missed";
        String statusColor = log.isCompleted() ? "#2E7D5A" : log.isToday() ? "#eb7147" : "#94A3B8";
        Label status = new Label(statusText);
        status.setPrefWidth(100);
        status.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");

        Label cal = new Label(log.isCompleted() ? log.getCaloriesConsumed() + " kcal" : "—");
        cal.setPrefWidth(90);
        cal.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        Label protein = new Label(log.isCompleted() ? (int) log.getProteinConsumed() + "g" : "—");
        protein.setPrefWidth(80);
        protein.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        Label carbs = new Label(log.isCompleted() ? (int) log.getCarbsConsumed() + "g" : "—");
        carbs.setPrefWidth(80);
        carbs.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        Label fats = new Label(log.isCompleted() ? (int) log.getFatsConsumed() + "g" : "—");
        fats.setPrefWidth(80);
        fats.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnFoods = new Button("👁 Foods");
        btnFoods.setStyle("-fx-background-color: #F8FAFC; -fx-text-fill: #475569; -fx-background-radius: 7; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10; -fx-border-color: #E2E8F0; -fx-border-radius: 7;");
        btnFoods.setOnAction(e -> toggleFoodPanel(log));

        Button btnEdit = new Button("✏ Macros");
        btnEdit.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #15803D; -fx-background-radius: 7; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10;");
        btnEdit.setOnAction(e -> openEditPanel(log));

        Button btnDelete = new Button("🗑");
        btnDelete.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-background-radius: 7; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10;");
        btnDelete.setOnAction(e -> clearLog(log));

        HBox actions = new HBox(6, btnFoods, btnEdit, btnDelete);
        actions.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(day, date, status, cal, protein, carbs, fats, spacer, actions);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: transparent transparent #F1F5F9 transparent; -fx-padding: 12 26;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #F1F5F9 transparent; -fx-padding: 12 26;"));

        return row;
    }

    private void toggleFoodPanel(DailyLog log) {
        String panelId = "foods-" + log.getId();
        // Remove if already open (toggle)
        boolean removed = logsContainer.getChildren().removeIf(n -> panelId.equals(n.getUserData()));
        if (removed) return;

        // Find row index
        int idx = -1;
        for (int i = 0; i < logsContainer.getChildren().size(); i++) {
            Object ud = logsContainer.getChildren().get(i).getUserData();
            if (("row-" + log.getId()).equals(ud)) { idx = i; break; }
        }
        // fallback: find by day number position
        if (idx < 0) {
            for (int i = 0; i < logs.size(); i++) {
                if (logs.get(i).getId() == log.getId()) { idx = i; break; }
            }
        }

        VBox panel = buildFoodPanel(log, panelId);
        logsContainer.getChildren().add(idx + 1, panel);
    }

    private VBox buildFoodPanel(DailyLog log, String panelId) {
        VBox panel = new VBox(10);
        panel.setUserData(panelId);
        panel.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 16 26; -fx-border-width: 1;");

        // ── Header ──
        Label header = new Label("🍽 Foods logged — Day " + log.getDayNumber());
        header.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        panel.getChildren().add(header);

        // ── Food rows container (refreshable) ──
        VBox foodRows = new VBox(6);
        panel.getChildren().add(foodRows);
        refreshFoodRows(log, foodRows, panel, panelId);

        // ── Add food form ──
        VBox addForm = buildAddFoodForm(log, foodRows, panel, panelId);
        panel.getChildren().add(addForm);

        // ── Close ──
        Button closeBtn = new Button("✕ Close");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> logsContainer.getChildren().remove(panel));
        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.CENTER_RIGHT);
        panel.getChildren().add(closeRow);

        return panel;
    }

    private void refreshFoodRows(DailyLog log, VBox foodRows, VBox panel, String panelId) {
        foodRows.getChildren().clear();

        // Collect all foods
        List<Object[]> allFoods = new ArrayList<>();
        for (String mealType : DailyLog.MEAL_TYPES) {
            DailyLog.MealData meal = log.getMealData(mealType);
            if (meal != null && meal.logged && meal.foodNames != null) {
                for (String foodName : meal.foodNames) {
                    allFoods.add(new Object[]{foodName, mealType});
                }
            }
        }

        if (allFoods.isEmpty()) {
            Label none = new Label("No individual foods recorded yet. Use the form below to add foods.");
            none.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8; -fx-wrap-text: true;");
            foodRows.getChildren().add(none);
        } else {
            // Column headers
            HBox colHeader = new HBox(0);
            colHeader.setStyle("-fx-padding: 0 4;");
            Label hMeal = styledColHeader("Meal", 90);
            Label hName = styledColHeader("Food", 180);
            Label hCal  = styledColHeader("Cal", 70);
            Label hPro  = styledColHeader("Protein", 70);
            Label hCarb = styledColHeader("Carbs", 70);
            Label hFat  = styledColHeader("Fats", 70);
            Region hSp  = new Region(); HBox.setHgrow(hSp, Priority.ALWAYS);
            Label hAct  = styledColHeader("Actions", 80);
            colHeader.getChildren().addAll(hMeal, hName, hCal, hPro, hCarb, hFat, hSp, hAct);
            foodRows.getChildren().add(colHeader);

            for (Object[] food : allFoods) {
                String foodName = (String) food[0];
                String mealType = (String) food[1];
                boolean isCustom = foodName.endsWith(" (custom)");
                foodRows.getChildren().add(buildFoodRow(log, foodName, mealType, isCustom, foodRows, panel, panelId));
            }

            // Totals row
            HBox totals = new HBox(0);
            totals.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 8; -fx-padding: 8 12; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");
            Label tLbl = new Label("Totals");
            tLbl.setPrefWidth(270);
            tLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
            Label tCal  = boldTotal(log.getCaloriesConsumed() + " kcal", 70);
            Label tPro  = boldTotal((int)log.getProteinConsumed() + "g", 70);
            Label tCarb = boldTotal((int)log.getCarbsConsumed() + "g", 70);
            Label tFat  = boldTotal((int)log.getFatsConsumed() + "g", 70);
            totals.getChildren().addAll(tLbl, tCal, tPro, tCarb, tFat);
            foodRows.getChildren().add(totals);
        }
    }

    private HBox buildFoodRow(DailyLog log, String foodName, String mealType,
                               boolean isCustom, VBox foodRows, VBox panel, String panelId) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 8 12; -fx-border-color: #E2E8F0; -fx-border-radius: 8;");

        // Get per-food macros if available, otherwise divide meal total evenly
        DailyLog.MealData meal = log.getMealData(mealType);
        double[] perFood = (meal != null && meal.foodMacros != null && meal.foodMacros.containsKey(foodName))
                ? meal.foodMacros.get(foodName)
                : null;
        int foodCount = (meal != null && meal.foodNames != null && !meal.foodNames.isEmpty())
                ? meal.foodNames.size() : 1;
        int mealCal   = perFood != null ? (int) perFood[0] : (meal != null ? meal.calories / foodCount : 0);
        double mealPro  = perFood != null ? perFood[1] : (meal != null ? meal.protein  / foodCount : 0);
        double mealCarb = perFood != null ? perFood[2] : (meal != null ? meal.carbs    / foodCount : 0);
        double mealFat  = perFood != null ? perFood[3] : (meal != null ? meal.fats     / foodCount : 0);

        // Meal badge
        Label mealBadge = new Label(capitalize(mealType));
        mealBadge.setPrefWidth(90);
        mealBadge.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B; -fx-background-color: #F1F5F9; -fx-background-radius: 5; -fx-padding: 2 6;");

        // Name — label (view) + field (edit)
        Label nameLabel = new Label(foodName);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        TextField nameField = new TextField(foodName);
        nameField.setPrefWidth(160);
        nameField.setStyle("-fx-font-size: 12px; -fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 8;");
        nameField.setVisible(false); nameField.setManaged(false);
        VBox nameStack = new VBox(2, nameLabel, nameField);
        nameStack.setPrefWidth(180);

        // Macro display labels (always visible)
        Label calLbl  = macroDisplayLabel(mealCal + " kcal", 75);
        Label proLbl  = macroDisplayLabel((int)mealPro + "g", 65);
        Label carbLbl = macroDisplayLabel((int)mealCarb + "g", 65);
        Label fatLbl  = macroDisplayLabel((int)mealFat + "g", 65);

        // Macro edit fields (hidden until edit mode, only for custom)
        TextField calF  = editMacroField(String.valueOf(mealCal), 75);
        TextField proF  = editMacroField(String.valueOf((int)mealPro), 65);
        TextField carbF = editMacroField(String.valueOf((int)mealCarb), 65);
        TextField fatF  = editMacroField(String.valueOf((int)mealFat), 65);
        calF.setVisible(false);  calF.setManaged(false);
        proF.setVisible(false);  proF.setManaged(false);
        carbF.setVisible(false); carbF.setManaged(false);
        fatF.setVisible(false);  fatF.setManaged(false);

        // Inline error
        Label errLbl = new Label("");
        errLbl.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 10px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("✏");
        btnEdit.setStyle("-fx-background-color: #F0FDF4; -fx-text-fill: #15803D; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 4 8;");
        btnEdit.setOnAction(e -> {
            if ("✓".equals(btnEdit.getText())) {
                // Validate name
                String newName = nameField.getText().trim();
                if (newName.isEmpty()) {
                    errLbl.setText("⚠ Name required."); errLbl.setVisible(true); errLbl.setManaged(true); return;
                }
                if (isCustom) {
                    try {
                        int c  = Integer.parseInt(calF.getText().trim());
                        double p  = Double.parseDouble(proF.getText().trim());
                        double cb = Double.parseDouble(carbF.getText().trim());
                        double f  = Double.parseDouble(fatF.getText().trim());
                        if (c < 0 || c > 10000) { errLbl.setText("⚠ Cal: 0–10000"); errLbl.setVisible(true); errLbl.setManaged(true); return; }
                        if (p < 0 || p > 500)   { errLbl.setText("⚠ Protein: 0–500g"); errLbl.setVisible(true); errLbl.setManaged(true); return; }
                        if (cb < 0 || cb > 1000){ errLbl.setText("⚠ Carbs: 0–1000g"); errLbl.setVisible(true); errLbl.setManaged(true); return; }
                        if (f < 0 || f > 300)   { errLbl.setText("⚠ Fats: 0–300g"); errLbl.setVisible(true); errLbl.setManaged(true); return; }
                        if (meal != null) {
                            if (meal.foodMacros == null) meal.foodMacros = new java.util.HashMap<>();
                            meal.foodMacros.put(nameField.getText().trim(), new double[]{c, p, cb, f});
                            // Recalculate meal totals from all per-food macros
                            meal.calories = 0; meal.protein = 0; meal.carbs = 0; meal.fats = 0;
                            for (double[] mac : meal.foodMacros.values()) {
                                meal.calories += (int) mac[0];
                                meal.protein  += mac[1];
                                meal.carbs    += mac[2];
                                meal.fats     += mac[3];
                            }
                            // Also add macros for standard foods not in foodMacros
                            if (meal.foodNames != null) {
                                for (String fn : meal.foodNames) {
                                    if (!meal.foodMacros.containsKey(fn)) {
                                        for (Object[] stdFood : FOODS) {
                                            if (stdFood[0].equals(fn)) {
                                                meal.calories += (int) stdFood[1];
                                                meal.protein  += (double) stdFood[2];
                                                meal.carbs    += (double) stdFood[3];
                                                meal.fats     += (double) stdFood[4];
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        log.recalculateTotals();
                    } catch (NumberFormatException ex) {
                        errLbl.setText("⚠ Enter valid numbers."); errLbl.setVisible(true); errLbl.setManaged(true); return;
                    }
                }
                errLbl.setVisible(false); errLbl.setManaged(false);
                renameFoodInLog(log, mealType, foodName, newName);
                reload();
            } else {
                // Enter edit mode
                nameLabel.setVisible(false); nameLabel.setManaged(false);
                nameField.setVisible(true);  nameField.setManaged(true);
                if (isCustom) {
                    calLbl.setVisible(false);  calLbl.setManaged(false);
                    proLbl.setVisible(false);  proLbl.setManaged(false);
                    carbLbl.setVisible(false); carbLbl.setManaged(false);
                    fatLbl.setVisible(false);  fatLbl.setManaged(false);
                    calF.setVisible(true);  calF.setManaged(true);
                    proF.setVisible(true);  proF.setManaged(true);
                    carbF.setVisible(true); carbF.setManaged(true);
                    fatF.setVisible(true);  fatF.setManaged(true);
                }
                btnEdit.setText("✓");
            }
        });

        Button btnDel = new Button("🗑");
        btnDel.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #DC2626; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 4 8;");
        btnDel.setOnAction(e -> { removeFoodFromLog(log, mealType, foodName); reload(); });

        HBox actions = new HBox(4, btnEdit, btnDel);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox errStack = new VBox(errLbl);
        row.getChildren().addAll(mealBadge, nameStack, calLbl, calF, proLbl, proF, carbLbl, carbF, fatLbl, fatF, spacer, actions, errStack);
        return row;
    }

    private Label macroDisplayLabel(String text, double width) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        return l;
    }

    private TextField editMacroField(String val, double width) {
        TextField f = new TextField(val);
        f.setPrefWidth(width);
        f.setStyle("-fx-font-size: 12px; -fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4 6;");
        return f;
    }

    private VBox buildAddFoodForm(DailyLog log, VBox foodRows, VBox panel, String panelId) {
        VBox form = new VBox(8);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 14; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");

        Label addHeader = new Label("+ Add Food to Log");
        addHeader.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        HBox fields = new HBox(8);
        fields.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> mealCombo = new ComboBox<>();
        mealCombo.getItems().addAll("breakfast", "lunch", "dinner", "snacks");
        mealCombo.setValue("breakfast");
        mealCombo.setPrefWidth(110);
        mealCombo.setStyle("-fx-font-size: 12px;");

        TextField nameF = new TextField();
        nameF.setPromptText("Food name");
        nameF.setPrefWidth(160);
        nameF.setStyle("-fx-font-size: 12px; -fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 8;");

        TextField calF  = addMacroField("Cal", 65);
        TextField proF  = addMacroField("Protein g", 75);
        TextField carbF = addMacroField("Carbs g", 75);
        TextField fatF  = addMacroField("Fats g", 65);

        Label errLbl = new Label("");
        errLbl.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 11px;");
        errLbl.setVisible(false); errLbl.setManaged(false);

        Button btnAdd = new Button("+ Add");
        btnAdd.setStyle("-fx-background-color: #1F4D3A; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 7 14; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> {
            String name = nameF.getText().trim();
            if (name.isEmpty()) { showFormErr(errLbl, "Food name is required."); return; }
            int cal; double pro, carb, fat;
            try {
                cal  = Integer.parseInt(calF.getText().trim());
                pro  = Double.parseDouble(proF.getText().trim());
                carb = Double.parseDouble(carbF.getText().trim());
                fat  = Double.parseDouble(fatF.getText().trim());
            } catch (NumberFormatException ex) {
                showFormErr(errLbl, "Enter valid numbers for all macros.");
                return;
            }
            errLbl.setVisible(false); errLbl.setManaged(false);

            String meal = mealCombo.getValue();
            DailyLog.MealData mealData = log.getMealData(meal);
            if (mealData == null) mealData = new DailyLog.MealData();
            if (mealData.foodNames == null) mealData.foodNames = new ArrayList<>();
            mealData.foodNames.add(name);
            mealData.calories += cal;
            mealData.protein  += pro;
            mealData.carbs    += carb;
            mealData.fats     += fat;
            mealData.logged    = true;
            log.recalculateTotals();
            if (!log.isCompleted()) log.setCompleted(true);
            service.update(log);

            // Clear fields
            nameF.clear(); calF.clear(); proF.clear(); carbF.clear(); fatF.clear();
            reload();
        });

        fields.getChildren().addAll(mealCombo, nameF, calF, proF, carbF, fatF, btnAdd);
        form.getChildren().addAll(addHeader, fields, errLbl);
        return form;
    }

    // ── Helpers ──
    private Label styledColHeader(String text, double width) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #94A3B8;");
        return l;
    }

    private Label boldTotal(String text, double width) {
        Label l = new Label(text);
        l.setPrefWidth(width);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        return l;
    }

    private TextField addMacroField(String prompt, double width) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefWidth(width);
        f.setStyle("-fx-font-size: 12px; -fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 8;");
        return f;
    }

    private void showFormErr(Label lbl, String msg) {
        lbl.setText("⚠ " + msg);
        lbl.setVisible(true); lbl.setManaged(true);
    }

    private void renameFoodInLog(DailyLog log, String mealType, String oldName, String newName) {
        DailyLog.MealData meal = log.getMealData(mealType);
        if (meal == null || meal.foodNames == null) return;
        int idx = meal.foodNames.indexOf(oldName);
        if (idx >= 0) meal.foodNames.set(idx, newName);
        service.update(log);
    }

    private void removeFoodFromLog(DailyLog log, String mealType, String foodName) {
        DailyLog.MealData meal = log.getMealData(mealType);
        if (meal == null || meal.foodNames == null) return;
        meal.foodNames.remove(foodName);
        // If no foods left in this meal, mark as unlogged
        if (meal.foodNames.isEmpty()) {
            meal.logged = false;
            meal.calories = 0; meal.protein = 0; meal.carbs = 0; meal.fats = 0;
        }
        log.recalculateTotals();
        service.update(log);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void openEditPanel(DailyLog log) {
        this.editingLog = log;
        editCalField.setText(log.isCompleted() ? String.valueOf(log.getCaloriesConsumed()) : "");
        editProteinField.setText(log.isCompleted() ? String.valueOf((int) log.getProteinConsumed()) : "");
        editCarbsField.setText(log.isCompleted() ? String.valueOf((int) log.getCarbsConsumed()) : "");
        editFatsField.setText(log.isCompleted() ? String.valueOf((int) log.getFatsConsumed()) : "");
        clearAllErrors();
        editPanel.setVisible(true);
        editPanel.setManaged(true);
    }

    @FXML
    private void handleSaveEdit() {
        boolean valid = true;

        int cal = 0;
        try {
            cal = Integer.parseInt(editCalField.getText().trim());
            if (cal < 0 || cal > 10000) { showErr(errCal, editCalField, "0–10000 kcal"); valid = false; }
            else clearErr(errCal, editCalField);
        } catch (NumberFormatException e) { showErr(errCal, editCalField, "Enter a whole number."); valid = false; }

        double protein = 0;
        try {
            protein = Double.parseDouble(editProteinField.getText().trim());
            if (protein < 0 || protein > 500) { showErr(errProtein, editProteinField, "0–500 g"); valid = false; }
            else clearErr(errProtein, editProteinField);
        } catch (NumberFormatException e) { showErr(errProtein, editProteinField, "Enter a number."); valid = false; }

        double carbs = 0;
        try {
            carbs = Double.parseDouble(editCarbsField.getText().trim());
            if (carbs < 0 || carbs > 1000) { showErr(errCarbs, editCarbsField, "0–1000 g"); valid = false; }
            else clearErr(errCarbs, editCarbsField);
        } catch (NumberFormatException e) { showErr(errCarbs, editCarbsField, "Enter a number."); valid = false; }

        double fats = 0;
        try {
            fats = Double.parseDouble(editFatsField.getText().trim());
            if (fats < 0 || fats > 300) { showErr(errFats, editFatsField, "0–300 g"); valid = false; }
            else clearErr(errFats, editFatsField);
        } catch (NumberFormatException e) { showErr(errFats, editFatsField, "Enter a number."); valid = false; }

        if (!valid) return;

        editingLog.setCaloriesConsumed(cal);
        editingLog.setProteinConsumed(protein);
        editingLog.setCarbsConsumed(carbs);
        editingLog.setFatsConsumed(fats);
        editingLog.setCompleted(true);
        // Sync the meals JSON totals so they match the edited values
        for (String mealType : DailyLog.MEAL_TYPES) {
            DailyLog.MealData m = editingLog.getMealData(mealType);
            if (m != null && m.logged) {
                // Distribute proportionally — simplest: put all in the first logged meal
                m.calories = cal; m.protein = protein; m.carbs = carbs; m.fats = fats;
                break;
            }
        }
        service.update(editingLog);

        editPanel.setVisible(false);
        editPanel.setManaged(false);
        editingLog = null;
        reload();
    }

    @FXML
    private void handleCancelEdit() {
        editPanel.setVisible(false);
        editPanel.setManaged(false);
        editingLog = null;
        clearAllErrors();
    }

    private void clearLog(DailyLog log) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Clear all data for Day " + log.getDayNumber() + "?",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm Clear");
        if (alert.showAndWait().filter(b -> b == ButtonType.YES).isPresent()) {
            log.setCaloriesConsumed(0);
            log.setProteinConsumed(0);
            log.setCarbsConsumed(0);
            log.setFatsConsumed(0);
            log.setCompleted(false);
            service.update(log);
            reload();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_objectives.fxml"));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showErr(Label lbl, TextField field, String msg) {
        lbl.setText("⚠ " + msg); lbl.setVisible(true); lbl.setManaged(true);
        field.setStyle("-fx-background-color: #FFF5F5; -fx-border-color: #EF4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 10; -fx-font-size: 13px;");
    }

    private void clearErr(Label lbl, TextField field) {
        lbl.setText(""); lbl.setVisible(false); lbl.setManaged(false);
        field.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 0 10; -fx-font-size: 13px;");
    }

    private void clearAllErrors() {
        clearErr(errCal, editCalField); clearErr(errProtein, editProteinField);
        clearErr(errCarbs, editCarbsField); clearErr(errFats, editFatsField);
    }
}
