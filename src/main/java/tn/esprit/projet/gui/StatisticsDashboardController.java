package tn.esprit.projet.gui;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.Recette;
import tn.esprit.projet.services.RecetteService;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsDashboardController implements Initializable {

    @FXML private HBox headerPane;
    @FXML private Label lblTitle;
    @FXML private Label lblSubtitle;
    @FXML private ComboBox<String> comboCriteria;
    @FXML private VBox contentArea;

    private RecetteService recetteService;
    private List<Recette> recipes;
    
    // Exact colors from the provided images
    private static final String COLOR_BLUE_BUBBLE = "#4299E1"; // Light Blue for fast
    private static final String COLOR_AMBER_BUBBLE = "#D69E2E"; // Amber for long
    
    private static final String[] DIFFICULTY_3_COLORS = {
        "#10B981", // Easy: Green
        "#F59E0B", // Medium: Amber
        "#EF4444"  // Hard: Red
    };

    private static final String LOLLIPOP_TOP = "#059669"; // Dark Green
    private static final String LOLLIPOP_MID = "#3B82F6"; // Blue
    private static final String LOLLIPOP_LOW = "#71717A"; // Gray

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        recetteService = new RecetteService();
        recipes = recetteService.getAll();
        
        comboCriteria.setItems(FXCollections.observableArrayList(
            "Duration × Difficulty",
            "Difficulty Distribution",
            "Popularity Ranking"
        ));

        comboCriteria.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateView(newVal);
            }
        });

        // Set default selection
        comboCriteria.getSelectionModel().select(0);
    }

    private void updateView(String criterion) {
        contentArea.getChildren().clear();
        headerPane.setStyle("-fx-background-color: #1A5C40; -fx-padding: 0 25; -fx-background-radius: 15 15 0 0;");

        switch (criterion) {
            case "Duration × Difficulty":
                setupBubbleChart();
                break;
            case "Difficulty Distribution":
                setupPyramidChart();
                break;
            case "Popularity Ranking":
                setupLollipopChart();
                break;
        }
    }

    // ══════════════════ CRITERIA 1: BUBBLE CHART (FIXED DISTORTION) ══════════════════
    private void setupBubbleChart() {
        lblTitle.setText("Duration × Difficulty");
        lblSubtitle.setText("Circle size = frequency of use");

        VBox chartContainer = createChartWrapper();
        
        NumberAxis xAxis = new NumberAxis(0, 140, 20);
        xAxis.setLabel("Duration (min)");
        NumberAxis yAxis = new NumberAxis(0, 6, 1);
        yAxis.setLabel("Difficulty");

        // Use ScatterChart instead of BubbleChart to avoid vertical distortion (ellipses)
        ScatterChart<Number, Number> sc = new ScatterChart<>(xAxis, yAxis);
        sc.setLegendVisible(false);
        sc.setPrefHeight(300);
        sc.setAnimated(true);

        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        for (Recette r : recipes) {
            int diff = mapDifficultyToNumeric(r.getDifficulte());
            int freq = getMockFrequency(r.getId());
            
            XYChart.Data<Number, Number> data = new XYChart.Data<>(r.getTempsPreparation(), diff);
            
            // Custom symbol to ensure perfect circles
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    // Force the node to be a StackPane with a Circle inside or just a styled Circle
                    StackPane container = (StackPane) newNode;
                    container.getChildren().clear();
                    
                    // Calculate radius based on frequency (Impressive sizing)
                    double radius = 5 + (freq * 1.5); 
                    Circle bubble = new Circle(radius);
                    
                    boolean isFast = r.getTempsPreparation() <= 45;
                    String color = isFast ? COLOR_BLUE_BUBBLE : COLOR_AMBER_BUBBLE;
                    
                    bubble.setFill(Color.web(color, 0.7));
                    bubble.setStroke(Color.WHITE);
                    bubble.setStrokeWidth(1.5);
                    
                    container.getChildren().add(bubble);
                    container.setStyle("-fx-background-color: transparent;"); // Remove default symbol background

                    // Tooltip
                    Tooltip tt = new Tooltip(r.getNom() + "\nDuration: " + r.getTempsPreparation() + "min\nFrequency: " + freq + "×");
                    Tooltip.install(bubble, tt);
                    
                    // Hover effect
                    bubble.setOnMouseEntered(e -> {
                        bubble.setOpacity(1.0);
                        ScaleTransition st = new ScaleTransition(Duration.millis(150), bubble);
                        st.setToX(1.2); st.setToY(1.2); st.play();
                    });
                    bubble.setOnMouseExited(e -> {
                        bubble.setOpacity(0.7);
                        ScaleTransition st = new ScaleTransition(Duration.millis(150), bubble);
                        st.setToX(1.0); st.setToY(1.0); st.play();
                    });
                }
            });

            series.getData().add(data);
        }

        sc.getData().add(series);
        
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(5, 0, 0, 0));
        legend.getChildren().addAll(
            createLegendItem(COLOR_BLUE_BUBBLE, "Fast ≤45min"),
            createLegendItem(COLOR_AMBER_BUBBLE, "Long >45min"),
            createLegendItem("#CBD5E1", "Size = usage")
        );

        chartContainer.getChildren().addAll(sc, legend);
        contentArea.getChildren().add(chartContainer);
    }

    // ══════════════════ CRITERIA 2: PYRAMID CHART (SMALL SCALE) ══════════════════
    private void setupPyramidChart() {
        lblTitle.setText("Difficulty Distribution");
        lblSubtitle.setText("Comparative analysis by difficulty level");

        VBox chartContainer = createChartWrapper();
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);

        BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setLegendVisible(false);
        bc.setPrefHeight(280);
        bc.setAnimated(true);
        bc.setCategoryGap(35);

        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("Easy", 0);
        counts.put("Medium", 0);
        counts.put("Hard", 0);

        for (Recette r : recipes) {
            String diff = r.getDifficulte().toLowerCase();
            if (diff.contains("easy") || diff.contains("facile")) counts.put("Easy", counts.get("Easy") + 1);
            else if (diff.contains("medium") || diff.contains("moyen")) counts.put("Medium", counts.get("Medium") + 1);
            else if (diff.contains("hard") || diff.contains("difficile")) counts.put("Hard", counts.get("Hard") + 1);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int i = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
            
            final int index = i++;
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + DIFFICULTY_3_COLORS[index] + "; -fx-background-radius: 8 8 0 0;");
                    
                    Tooltip tt = new Tooltip(entry.getKey() + ": " + entry.getValue() + " recipes");
                    Tooltip.install(newNode, tt);

                    newNode.setOnMouseEntered(e -> newNode.setOpacity(0.8));
                    newNode.setOnMouseExited(e -> newNode.setOpacity(1.0));
                }
            });
        }

        bc.getData().add(series);

        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(5, 0, 0, 0));
        String[] diffs = {"Easy", "Medium", "Hard"};
        for (int j = 0; j < 3; j++) {
            legend.getChildren().add(createLegendItem(DIFFICULTY_3_COLORS[j], diffs[j]));
        }

        chartContainer.getChildren().addAll(bc, legend);
        contentArea.getChildren().add(chartContainer);
    }

    // ══════════════════ CRITERIA 3: LOLLIPOP CHART (COMPACT) ══════════════════
    private void setupLollipopChart() {
        lblTitle.setText("Popularity Ranking");
        lblSubtitle.setText("Recipe usage frequency (Top 5)");

        VBox chartContainer = createChartWrapper();
        chartContainer.setPadding(new Insets(10, 30, 10, 30));
        
        VBox rankingList = new VBox(10);
        rankingList.setAlignment(Pos.TOP_CENTER);

        List<Recette> sorted = recipes.stream()
                .sorted((r1, r2) -> Integer.compare(getMockFrequency(r2.getId()), getMockFrequency(r1.getId())))
                .limit(5)
                .collect(Collectors.toList());

        int rank = 1;
        for (Recette r : sorted) {
            int freq = getMockFrequency(r.getId());
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label lblRank = new Label("#" + rank);
            lblRank.setMinWidth(30);
            lblRank.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");

            Label lblName = new Label(r.getNom());
            lblName.setMinWidth(120);
            lblName.setStyle("-fx-font-size: 12px; -fx-text-fill: #1E293B; -fx-font-weight: bold;");

            StackPane barArea = new StackPane();
            HBox.setHgrow(barArea, Priority.ALWAYS);
            barArea.setAlignment(Pos.CENTER_LEFT);

            String color = rank == 1 ? LOLLIPOP_TOP : (rank <= 3 ? "#065F46" : LOLLIPOP_MID);
            double width = (freq / 15.0) * 280;
            
            Line line = new Line(0, 0, width, 0);
            line.setStroke(Color.web(color));
            line.setStrokeWidth(2.5);

            Circle circle = new Circle(7);
            circle.setFill(Color.web(color));
            circle.setTranslateX(width);
            
            circle.setOnMouseEntered(e -> circle.setScaleX(1.3));
            circle.setOnMouseExited(e -> circle.setScaleX(1.0));

            Label lblVal = new Label(freq + "×");
            lblVal.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-font-size: 11px;");
            lblVal.setTranslateX(width + 15);

            barArea.getChildren().addAll(line, circle, lblVal);
            row.getChildren().addAll(lblRank, lblName, barArea);
            rankingList.getChildren().add(row);
            rank++;
        }

        chartContainer.getChildren().add(rankingList);
        contentArea.getChildren().add(chartContainer);
    }

    // ══════════════════ UI HELPERS ══════════════════

    private VBox createChartWrapper() {
        VBox wrapper = new VBox(3);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 10;");
        wrapper.setPrefWidth(600);
        return wrapper;
    }

    private HBox createLegendItem(String color, String text) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(8, 8);
        rect.setFill(Color.web(color));
        rect.setArcHeight(2); rect.setArcWidth(2);
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B; -fx-font-weight: bold;");
        item.getChildren().addAll(rect, lbl);
        return item;
    }

    private int mapDifficultyToNumeric(String diff) {
        if (diff == null) return 1;
        String d = diff.toLowerCase();
        if (d.contains("easy") || d.contains("facile")) return 1;
        if (d.contains("medium") || d.contains("moyen")) return 3;
        if (d.contains("hard") || d.contains("difficile")) return 5;
        return 2;
    }

    private int getMockFrequency(int id) {
        return (id * 7) % 12 + 3;
    }

    @FXML
    private void handleClose() {
        ((Stage) comboCriteria.getScene().getWindow()).close();
    }
}
