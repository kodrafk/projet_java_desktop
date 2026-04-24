package tn.esprit.projet.gui;

import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import tn.esprit.projet.models.NutritionObjective;
import tn.esprit.projet.services.NutritionAIService;
import tn.esprit.projet.services.NutritionObjectiveService;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ObjectiveCustomController implements Initializable {

    // ═══ FORM FIELDS ═══
    @FXML private TextField titleField, caloriesField, proteinField, carbsField, fatsField, waterField;
    @FXML private TextArea descriptionField;
    @FXML private DatePicker startDatePicker;
    @FXML private CheckBox autoActivateCheck;

    // ═══ ERROR LABELS ═══
    @FXML private Label errTitle, errCalories, errProtein, errCarbs, errFats, errWater, errDate;

    // ═══ AI COMPONENTS ═══
    @FXML private Button aiInlineBtn;
    @FXML private Pane aiOverlay;
    @FXML private StackPane aiFab;                          // StackPane in FXML
    @FXML private VBox aiTooltip, aiPanel, aiLoading, aiResult, aiError, aiPanelBody;
    @FXML private Button tooltipClose, aiPanelClose, aiGenerateBtn, aiApplyBtn, aiRefreshBtn, aiRetryBtn;
    @FXML private Label aiMessage, aiLoadingText, aiSugDesc, aiSugCal, aiSugProt, aiSugCarb, aiSugFat, aiSugWater, aiErrorMsg;
    @FXML private Label aiTooltipText, aiFabIcon, aiPanelTitle, aiPanelStatus;
    @FXML private ProgressBar aiProgressBar;
    @FXML private Circle aiFabCircle, aiStatusDot, fabGlowRing, fabPulseRing;
    @FXML private HBox aiPanelHeader, aiResultDescContainer;
    @FXML private StackPane aiPanelAvatar, aiMessageAvatar, aiErrorIconContainer;
    @FXML private VBox aiResultCalories, aiResultProtein, aiResultCarbs, aiResultFats, aiResultWater;

    // ═══ AI STATE ═══
    private boolean panelOpen = false;
    private boolean tooltipDismissed = false;
    private int variationCounter = 0;
    private NutritionAIService aiService;
    private NutritionAIService.NutritionSuggestion currentSuggestion;
    private WebView lottieWebView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        aiService = new NutritionAIService();

        if (startDatePicker != null) {
            startDatePicker.setValue(LocalDate.now());
        }

        if (aiFab != null) {
            aiFab.setPickOnBounds(true);
            aiFab.setOnMouseClicked(e -> handleAiFabClick());
        }

        setupFieldListeners();
        setupFabAnimation();
        applyAIStyles();
        showIntroTooltip();
        setupLottieWebView();

        // Scene-level click listener — fires regardless of what's on top
        // Detects clicks in the bottom-right corner where the robot is
        javafx.application.Platform.runLater(() -> {
            if (titleField != null && titleField.getScene() != null) {
                javafx.scene.Scene registeredScene = titleField.getScene();
                registeredScene.addEventFilter(
                    javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
                        // Guard: node may have been removed from the scene
                        if (titleField.getScene() == null) return;
                        javafx.scene.Scene scene = titleField.getScene();
                        double x = event.getSceneX();
                        double y = event.getSceneY();
                        double w = scene.getWidth();
                        double h = scene.getHeight();
                        // Robot is in bottom-right — click zone is last 220px x 220px
                        if (x > w - 220 && y > h - 220) {
                            System.out.println("SCENE CLICK in robot zone: " + x + "," + y);
                            handleAiFabClick();
                            event.consume();
                        }
                    });
            }
        });
    }

    // ═══ SETUP ═══

private void setupLottieWebView() {
    if (lottieWebView == null) return;

    lottieWebView.setStyle("-fx-background-color: transparent;");
    lottieWebView.getEngine().setJavaScriptEnabled(true);

    // Clip to circle — cuts the white square corners off
    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(100, 100, 100);
    lottieWebView.setClip(clip);

    // Force background via user stylesheet (most reliable approach)
    lottieWebView.getEngine().setUserStyleSheetLocation(
        "data:text/css,html,body{background-color:%23F6FBF7!important;margin:0;padding:0;}"
    );
    lottieWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, state) -> {
        if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
            try {
                lottieWebView.getEngine().executeScript(
                    "document.body.style.backgroundColor='#F6FBF7';" +
                    "document.documentElement.style.backgroundColor='#F6FBF7';"
                );
            } catch (Exception ignored) {}
        }
    });

    // This is the exact same HTML as your Twig file
    String html = """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                * { margin: 120; padding: 90; box-sizing: border-box; }
                html, body {
                    width: 160px;
                    height: 160px;
                    background: transparent !important;
                    overflow: hidden;
                }
            </style>
        </head>
        <body>
            <script
                src="https://unpkg.com/@dotlottie/player-component@latest/dist/dotlottie-player.mjs"
                type="module">
            </script>
            <dotlottie-player
                src="https://lottie.host/c4e2f923-e025-4461-9776-63d8d6231e12/7r2yRavvDt.lottie"
                background="transparent"
                speed="1"
                style="width: 160px; height: 160px;"
                loop
                autoplay>
            </dotlottie-player>
        </body>
        </html>
        """;

    lottieWebView.getEngine().loadContent(html);
}
    private void setupFieldListeners() {
        if (titleField != null) titleField.textProperty().addListener((o, old, n) -> { clearErr(errTitle, titleField); variationCounter = 0; });
        if (caloriesField != null) caloriesField.textProperty().addListener((o, old, n) -> clearErr(errCalories, caloriesField));
        if (proteinField != null) proteinField.textProperty().addListener((o, old, n) -> clearErr(errProtein, proteinField));
        if (carbsField != null) carbsField.textProperty().addListener((o, old, n) -> clearErr(errCarbs, carbsField));
        if (fatsField != null) fatsField.textProperty().addListener((o, old, n) -> clearErr(errFats, fatsField));
        if (waterField != null) waterField.textProperty().addListener((o, old, n) -> clearErr(errWater, waterField));
        if (startDatePicker != null) startDatePicker.valueProperty().addListener((o, old, n) -> clearErr(errDate, null));
    }

    private void setupFabAnimation() {
        if (aiFab == null) return;
        aiFab.setPickOnBounds(false);

        // setupLottieInFab() clears children and adds WebView — animations run after
        setupLottieInFab();

        // Hover scale on the whole FAB
        aiFab.setOnMouseEntered(e -> { ScaleTransition st = new ScaleTransition(Duration.millis(150), aiFab); st.setToX(1.08); st.setToY(1.08); st.play(); });
        aiFab.setOnMouseExited(e -> { ScaleTransition st = new ScaleTransition(Duration.millis(150), aiFab); st.setToX(1.0); st.setToY(1.0); st.play(); });
    }

    private void setupLottieInFab() {
        if (aiFab == null) return;

        aiFab.getChildren().clear();

        lottieWebView = new WebView();
        lottieWebView.setPrefSize(145, 145);
        lottieWebView.setMaxSize(145, 145);
        lottieWebView.setMinSize(145, 145);
        lottieWebView.setMouseTransparent(true);

        lottieWebView.setTranslateX(-70);
        lottieWebView.setTranslateY(-20);

        // offsetX: negative = move robot LEFT, positive = move RIGHT
        // offsetY: negative = move robot UP, positive = move DOWN
        // scale: bigger = zoom in more (1.5 = 150%)
        int offsetX = -1000;  // tweak this to center horizontally
        int offsetY = -40;  // tweak this to center vertically
        double scale = 1.4;

        String html = "<!DOCTYPE html><html><head><style>" +
            "* { margin:0; padding:0; }" +
            "html, body { width:300px; height:300px; overflow:hidden; background:#F6FBF7; }" +
            "#wrapper {" +
            "  width:300px; height:300px;" +
            "  transform: translate(" + offsetX + "px, " + offsetY + "px) scale(" + scale + ");" +
            "  transform-origin: center center;" +
            "}" +
            "#lottie { width:300px; height:300px; }" +
            "</style></head><body>" +
            "<div id='wrapper'><div id='lottie'></div></div>" +
            "<script src='https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.12.2/lottie.min.js'></script>" +
            "<script>" +
            "lottie.loadAnimation({" +
            "  container: document.getElementById('lottie')," +
            "  renderer: 'canvas', loop: true, autoplay: true," +
            "  path: 'https://assets2.lottiefiles.com/packages/lf20_myejiggj.json'" +
            "});" +
            "</script></body></html>";

        lottieWebView.getEngine().loadContent(html);
        aiFab.getChildren().add(lottieWebView);
    }

    // ═══ TOOLTIP ═══

    private void showIntroTooltip() {
        if (tooltipDismissed || aiTooltip == null) return;
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> {
            if (!tooltipDismissed && !panelOpen) {
                aiTooltip.setVisible(true);
                aiTooltip.setManaged(true);
                FadeTransition ft = new FadeTransition(Duration.millis(300), aiTooltip);
                ft.setFromValue(0); ft.setToValue(1); ft.play();
                PauseTransition autoDismiss = new PauseTransition(Duration.seconds(8));
                autoDismiss.setOnFinished(ev -> dismissTooltip());
                autoDismiss.play();
            }
        });
        delay.play();
    }

    private void dismissTooltip() {
        tooltipDismissed = true;
        if (aiTooltip == null) return;
        FadeTransition ft = new FadeTransition(Duration.millis(200), aiTooltip);
        ft.setFromValue(1); ft.setToValue(0);
        ft.setOnFinished(e -> { aiTooltip.setVisible(false); aiTooltip.setManaged(false); });
        ft.play();
    }

    @FXML private void handleTooltipClose() { dismissTooltip(); }

    // ═══ PANEL ═══

    private void openPanel() {
        panelOpen = true;
        dismissTooltip();
        if (aiOverlay != null) { aiOverlay.setVisible(true); aiOverlay.setManaged(true); }
        if (aiPanel != null) {
            aiPanel.setVisible(true); aiPanel.setManaged(true);
            FadeTransition ft = new FadeTransition(Duration.millis(350), aiPanel); ft.setFromValue(0); ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(350), aiPanel); tt.setFromY(20); tt.setToY(0);
            new ParallelTransition(ft, tt).play();
        }
    }

    private void closePanel() {
        panelOpen = false;
        if (aiPanel != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(250), aiPanel); ft.setFromValue(1); ft.setToValue(0);
            ft.setOnFinished(e -> { aiPanel.setVisible(false); aiPanel.setManaged(false); });
            ft.play();
        }
        if (aiOverlay != null) { aiOverlay.setVisible(false); aiOverlay.setManaged(false); }
    }

    @FXML private void handleAiFabClick() {
        System.out.println("FAB CLICKED — panelOpen=" + panelOpen + " aiPanel=" + aiPanel);
        dismissTooltip();
        if (panelOpen) closePanel(); else openPanel();
    }
    @FXML private void handleOverlayClick() { closePanel(); }
    @FXML private void handleAiPanelClose() { closePanel(); }
    @FXML private void handleAiInline() { if (!panelOpen) openPanel(); fetchSuggestion(); }

    // ═══ AI FETCH ═══

    @FXML private void handleAiGenerate() { fetchSuggestion(); }
    @FXML private void handleAiRefresh() { variationCounter++; fetchSuggestion(); }
    @FXML private void handleAiRetry() { fetchSuggestion(); }

    private void fetchSuggestion() {
        if (titleField == null) return;
        String title = titleField.getText().trim();
        if (title.length() < 3) {
            if (aiMessage != null) aiMessage.setText("⚠ Type at least 3 characters in the title first.");
            showState("idle");
            return;
        }

        showState("loading");
        if (aiMessage != null) aiMessage.setText("🤖 Analyzing your goal...");

        Task<NutritionAIService.NutritionSuggestion> task = aiService.suggestPlanAsync(title, variationCounter);

        if (aiProgressBar != null) aiProgressBar.progressProperty().bind(task.progressProperty());
        if (aiLoadingText != null) aiLoadingText.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> {
            NutritionAIService.NutritionSuggestion result = task.getValue();
            if (result == null || !result.isValid()) {
                if (aiErrorMsg != null) aiErrorMsg.setText(result != null ? result.getReason() : "No response from AI.");
                showState("error");
                return;
            }
            currentSuggestion = result;
            displayResult(result);
        });

        task.setOnFailed(e -> {
            if (aiErrorMsg != null) aiErrorMsg.setText("Could not get AI suggestions. Please try again.");
            showState("error");
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void displayResult(NutritionAIService.NutritionSuggestion r) {
        if (aiSugDesc != null) aiSugDesc.setText(r.getDescription());
        animateValue(aiSugCal, r.getCalories());
        animateValue(aiSugProt, (int) r.getProtein());
        animateValue(aiSugCarb, (int) r.getCarbs());
        animateValue(aiSugFat, (int) r.getFats());
        if (aiSugWater != null) aiSugWater.setText(String.valueOf(r.getWater()));
        if (aiApplyBtn != null) aiApplyBtn.setText("✓ Apply Values");
        if (aiMessage != null) aiMessage.setText("🌟 Here's what I recommend!");
        showState("result");
    }

    private void animateValue(Label label, int end) {
        if (label == null) return;
        final int[] cur = {0};
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            cur[0] += (int) Math.max(1, Math.ceil((end - cur[0]) * 0.15));
            if (cur[0] >= end) { label.setText(String.valueOf(end)); ((Timeline) e.getSource()).stop(); }
            else label.setText(String.valueOf(cur[0]));
        }));
        tl.setCycleCount(60);
        tl.play();
    }

    @FXML
    private void handleAiApply() {
        if (currentSuggestion == null) return;
        if (descriptionField != null) descriptionField.setText(currentSuggestion.getDescription());
        if (caloriesField != null) caloriesField.setText(String.valueOf(currentSuggestion.getCalories()));
        if (proteinField != null) proteinField.setText(String.valueOf((int) currentSuggestion.getProtein()));
        if (carbsField != null) carbsField.setText(String.valueOf((int) currentSuggestion.getCarbs()));
        if (fatsField != null) fatsField.setText(String.valueOf((int) currentSuggestion.getFats()));
        if (waterField != null) waterField.setText(String.valueOf(currentSuggestion.getWater()));
        clearErr(errCalories, caloriesField); clearErr(errProtein, proteinField);
        clearErr(errCarbs, carbsField); clearErr(errFats, fatsField); clearErr(errWater, waterField);
        if (aiApplyBtn != null) aiApplyBtn.setText("✓ Applied!");
        if (aiMessage != null) aiMessage.setText("✅ Targets applied! Adjust anything you'd like.");
        PauseTransition delay = new PauseTransition(Duration.seconds(1.2));
        delay.setOnFinished(e -> closePanel());
        delay.play();
    }

    private void showState(String state) {
        setVisible(aiGenerateBtn, "idle".equals(state));
        setVisible(aiLoading, "loading".equals(state));
        setVisible(aiResult, "result".equals(state));
        setVisible(aiError, "error".equals(state));
    }

    private void setVisible(javafx.scene.Node node, boolean v) {
        if (node != null) { node.setVisible(v); node.setManaged(v); }
    }

    // ═══ FORM HANDLERS ═══

    @FXML private void handleBack() { navigateTo("/fxml/objective_choose_goal.fxml"); }

    @FXML
    private void handleCreate() {
        boolean valid = true;
        String title = titleField != null ? titleField.getText().trim() : "";
        if (title.isEmpty()) { showErr(errTitle, titleField, "Title is required."); valid = false; }
        else if (title.length() < 3) { showErr(errTitle, titleField, "Title must be at least 3 characters."); valid = false; }
        else if (title.length() > 100) { showErr(errTitle, titleField, "Title must not exceed 100 characters."); valid = false; }

        int calories = 0;
        if (caloriesField != null) {
            String s = caloriesField.getText().trim();
            if (s.isEmpty()) { showErr(errCalories, caloriesField, "Calories is required."); valid = false; }
            else try { calories = Integer.parseInt(s); if (calories < 500 || calories > 5000) { showErr(errCalories, caloriesField, "Must be 500–5000 kcal."); valid = false; } }
            catch (NumberFormatException e) { showErr(errCalories, caloriesField, "Enter a whole number."); valid = false; }
        }

        double protein = 0;
        if (proteinField != null) {
            String s = proteinField.getText().trim();
            if (s.isEmpty()) { showErr(errProtein, proteinField, "Protein is required."); valid = false; }
            else try { protein = Double.parseDouble(s); if (protein < 10 || protein > 300) { showErr(errProtein, proteinField, "Must be 10–300 g."); valid = false; } }
            catch (NumberFormatException e) { showErr(errProtein, proteinField, "Enter a valid number."); valid = false; }
        }

        double carbs = 0;
        if (carbsField != null) {
            String s = carbsField.getText().trim();
            if (s.isEmpty()) { showErr(errCarbs, carbsField, "Carbs is required."); valid = false; }
            else try { carbs = Double.parseDouble(s); if (carbs < 20 || carbs > 500) { showErr(errCarbs, carbsField, "Must be 20–500 g."); valid = false; } }
            catch (NumberFormatException e) { showErr(errCarbs, carbsField, "Enter a valid number."); valid = false; }
        }

        double fats = 0;
        if (fatsField != null) {
            String s = fatsField.getText().trim();
            if (s.isEmpty()) { showErr(errFats, fatsField, "Fats is required."); valid = false; }
            else try { fats = Double.parseDouble(s); if (fats < 10 || fats > 200) { showErr(errFats, fatsField, "Must be 10–200 g."); valid = false; } }
            catch (NumberFormatException e) { showErr(errFats, fatsField, "Enter a valid number."); valid = false; }
        }

        double water = 0;
        if (waterField != null && !waterField.getText().trim().isEmpty()) {
            try { water = Double.parseDouble(waterField.getText().trim()); if (water < 0 || water > 10) { showErr(errWater, waterField, "Must be 0–10 L."); valid = false; } }
            catch (NumberFormatException e) { showErr(errWater, waterField, "Enter a valid number."); valid = false; }
        }

        LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : null;
        if (startDate == null) { showErr(errDate, null, "Please select a start date."); valid = false; }
        else if (startDate.isBefore(LocalDate.now())) { showErr(errDate, null, "Start date must be today or future."); valid = false; }

        if (!valid) return;

        NutritionObjective obj = new NutritionObjective();
        obj.setTitle(title);
        obj.setDescription(descriptionField != null ? descriptionField.getText().trim() : "");
        obj.setGoalType("custom");
        obj.setTargetCalories(calories);
        obj.setTargetProtein(protein);
        obj.setTargetCarbs(carbs);
        obj.setTargetFats(fats);
        obj.setTargetWater(water);
        obj.setPlannedStartDate(startDate);
        obj.setAutoActivate(autoActivateCheck != null && autoActivateCheck.isSelected());
        obj.setStatus("pending");
        new NutritionObjectiveService().save(obj);
        navigateTo("/fxml/objectives.fxml");
    }

    // ═══ HELPERS ═══

    private void showErr(Label lbl, TextField field, String msg) {
        if (lbl != null) { lbl.setText("⚠ " + msg); lbl.setVisible(true); lbl.setManaged(true); }
        if (field != null) field.setStyle("-fx-background-color: #FEF2F2; -fx-border-color: #EF4444; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 0 12; -fx-font-size: 13px;");
    }

    private void clearErr(Label lbl, TextField field) {
        if (lbl != null) { lbl.setText(""); lbl.setVisible(false); lbl.setManaged(false); }
        if (field != null) field.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 0 12; -fx-font-size: 13px;");
    }

    private void navigateTo(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentArea = (StackPane) titleField.getScene().lookup("#contentArea");
            if (contentArea != null) contentArea.getChildren().setAll(page);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ═══ PROGRAMMATIC STYLES ═══

    private void applyAIStyles() {
        style(aiOverlay, "-fx-background-color: rgba(0,0,0,0.3);");
        style(aiFab, "-fx-padding: 0 20 20 0;");
        style(aiTooltip, "-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12 16; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-max-width: 260; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        style(aiPanel, "-fx-background-color: white; -fx-background-radius: 16; -fx-border-color: #E2E8F0; -fx-border-radius: 16; -fx-max-width: 400; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 16, 0, 0, 4);");
        style(aiPanelHeader, "-fx-background-color: linear-gradient(to right, #f8faf9, #f0f4f2); -fx-background-radius: 16 16 0 0; -fx-padding: 16 20; -fx-border-color: transparent transparent #E2E8F0 transparent; -fx-border-width: 0 0 1 0;");
        style(aiPanelAvatar, "-fx-background-color: linear-gradient(to bottom right, #2d6a4f, #52b788); -fx-background-radius: 10;");
        style(aiMessageAvatar, "-fx-background-color: linear-gradient(to bottom right, #2d6a4f, #52b788); -fx-background-radius: 10;");
        style(aiMessage, "-fx-font-size: 13px; -fx-text-fill: #374151; -fx-background-color: #F8FAFC; -fx-background-radius: 4 16 16 16; -fx-padding: 14 18; -fx-border-color: #E2E8F0; -fx-border-radius: 4 16 16 16;");
        style(aiResultDescContainer, "-fx-background-color: #F8FAFC; -fx-background-radius: 10; -fx-padding: 14 16; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
        style(aiError, "-fx-background-color: #FEF2F2; -fx-background-radius: 10; -fx-padding: 20; -fx-border-color: #FCA5A5; -fx-border-radius: 10;");
        style(aiErrorIconContainer, "-fx-background-color: #FEF2F2; -fx-background-radius: 50%; -fx-border-color: #FCA5A5; -fx-border-radius: 50%;");
        style(aiErrorMsg, "-fx-font-size: 12px; -fx-text-fill: #DC2626; -fx-font-weight: 500;");
        style(aiLoadingText, "-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        if (aiProgressBar != null) aiProgressBar.setStyle("-fx-accent: #2d6a4f;");
        if (aiStatusDot != null) aiStatusDot.setStyle("-fx-fill: #4ade80;");

        styleResultItem(aiResultCalories); styleResultItem(aiResultProtein);
        styleResultItem(aiResultCarbs); styleResultItem(aiResultFats); styleResultItem(aiResultWater);

        styleBtn(aiGenerateBtn, "-fx-background-color: linear-gradient(to bottom right, #2d6a4f, #52b788); -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 14 20; -fx-cursor: hand;");
        styleBtn(aiApplyBtn, "-fx-background-color: linear-gradient(to bottom right, #2d6a4f, #52b788); -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 12 16; -fx-cursor: hand;");
        styleBtn(aiRefreshBtn, "-fx-background-color: white; -fx-text-fill: #374151; -fx-border-color: #E2E8F0; -fx-background-radius: 10; -fx-border-radius: 10; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 12 16; -fx-cursor: hand;");
        styleBtn(aiRetryBtn, "-fx-background-color: white; -fx-text-fill: #374151; -fx-border-color: #E2E8F0; -fx-background-radius: 10; -fx-border-radius: 10; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        styleBtn(tooltipClose, "-fx-background-color: #F3F4F6; -fx-text-fill: #6B7280; -fx-background-radius: 50%; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-cursor: hand;");
        styleBtn(aiPanelClose, "-fx-background-color: white; -fx-text-fill: #6B7280; -fx-border-color: #E2E8F0; -fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 6 10; -fx-cursor: hand;");
        styleBtn(aiInlineBtn, "-fx-background-color: #eb7147; -fx-text-fill: white; -fx-border-radius: 0 10 10 0; -fx-background-radius: 0 10 10 0; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 12 16; -fx-cursor: hand;");
    }

    private void style(javafx.scene.Node n, String css) { if (n != null) n.setStyle(css); }

    private void styleBtn(Button b, String css) {
        if (b == null) return;
        b.setStyle(css);
    }

    private void styleResultItem(VBox item) {
        if (item == null) return;
        String base = "-fx-background-color: #F8FAFC; -fx-background-radius: 10; -fx-padding: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 10;";
        item.setStyle(base);
        // Style child labels
        item.getChildren().forEach(n -> {
            if (n instanceof Label l) {
                String t = l.getText();
                if (t != null && (t.equals("CALORIES") || t.equals("PROTEIN") || t.equals("CARBS") || t.equals("FATS") || t.equals("WATER"))) {
                    l.setStyle("-fx-font-size: 9px; -fx-text-fill: #6B7280; -fx-font-weight: bold;");
                } else if (t != null && (t.equals("kcal") || t.equals("g") || t.equals("liters"))) {
                    l.setStyle("-fx-font-size: 9px; -fx-text-fill: #6B7280;");
                } else {
                    l.setStyle("-fx-font-size: 18px; -fx-text-fill: #1E293B; -fx-font-weight: bold;");
                }
            }
        });
    }
}
