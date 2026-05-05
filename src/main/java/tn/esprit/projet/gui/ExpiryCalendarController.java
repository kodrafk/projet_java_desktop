package tn.esprit.projet.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.projet.models.Ingredient;
import tn.esprit.projet.services.IngredientService;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class ExpiryCalendarController implements Initializable {

    @FXML private Label lblMonthYear;
    @FXML private GridPane gridDays;

    private IngredientService ingredientService;
    private List<Ingredient> ingredients;
    private YearMonth currentYearMonth;

    // Colors
    private static final String COLOR_NONE = "#E2E8F0";     // Neutral Gray
    private static final String COLOR_LOW = "#BBF7D0";      // Light Green (1)
    private static final String COLOR_MEDIUM = "#FDE68A";   // Amber (2-3)
    private static final String COLOR_HIGH = "#EF4444";     // Dark Red (4+)

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ingredientService = new IngredientService();
        currentYearMonth = YearMonth.now();
        refreshDataAndRender();
    }

    private void refreshDataAndRender() {
        ingredients = ingredientService.getAll();
        renderCalendar();
    }

    private void renderCalendar() {
        gridDays.getChildren().clear();
        lblMonthYear.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) 
                            + " " + currentYearMonth.getYear());

        // Add Day Names (Mon, Tue, etc.)
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(days[i]);
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748B; -fx-font-size: 11px;");
            gridDays.add(lbl, i, 0);
        }

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeekValue - 1;

        // Group ingredients by expiry date
        Map<LocalDate, List<Ingredient>> expiryMap = ingredients.stream()
                .filter(i -> i.getDatePeremption() != null)
                .collect(Collectors.groupingBy(Ingredient::getDatePeremption));

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            List<Ingredient> expiringItems = expiryMap.getOrDefault(date, new ArrayList<>());
            
            Node dayCell = createDayCell(day, expiringItems);
            gridDays.add(dayCell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private Node createDayCell(int day, List<Ingredient> expiringItems) {
        StackPane cell = new StackPane();
        cell.setPrefSize(60, 60);
        
        int count = expiringItems.size();
        String color;
        String textColor = "#475569";

        if (count == 0) color = COLOR_NONE;
        else if (count == 1) color = COLOR_LOW;
        else if (count <= 3) color = COLOR_MEDIUM;
        else {
            color = COLOR_HIGH;
            textColor = "white";
        }

        Rectangle rect = new Rectangle(50, 50);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        rect.setFill(Color.web(color));
        rect.setStroke(Color.web("#CBD5E1"));
        rect.setStrokeWidth(0.5);

        Label lblDay = new Label(String.valueOf(day));
        lblDay.setStyle("-fx-font-weight: bold; -fx-text-fill: " + textColor + "; -fx-font-size: 13px;");

        cell.getChildren().addAll(rect, lblDay);

        // Tooltip logic
        if (count > 0) {
            String list = expiringItems.stream()
                    .map(Ingredient::getNom)
                    .collect(Collectors.joining("\n• ", "Expiring today:\n• ", ""));
            
            Tooltip tt = new Tooltip(list);
            tt.setShowDelay(Duration.millis(100));
            tt.setStyle("-fx-font-size: 12px; -fx-background-color: #1E293B;");
            Tooltip.install(cell, tt);

            // Hover effects
            cell.setOnMouseEntered(e -> rect.setStrokeWidth(2));
            cell.setOnMouseExited(e -> rect.setStrokeWidth(0.5));
        }

        return cell;
    }

    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        renderCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        renderCalendar();
    }

    @FXML
    private void handleRefresh() {
        refreshDataAndRender();
    }

    @FXML
    private void handleClose() {
        ((Stage) lblMonthYear.getScene().getWindow()).close();
    }
}
