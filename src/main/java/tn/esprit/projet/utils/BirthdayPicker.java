package tn.esprit.projet.utils;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A user-friendly birthday picker made of three ComboBoxes: Day / Month / Year.
 * Drop-in replacement for DatePicker in registration/edit forms.
 */
public class BirthdayPicker extends HBox {

    private final ComboBox<Integer> cbDay   = new ComboBox<>();
    private final ComboBox<String>  cbMonth = new ComboBox<>();
    private final ComboBox<Integer> cbYear  = new ComboBox<>();

    private static final String CB_STYLE =
            "-fx-background-color: #F0F7F0;" +
            "-fx-border-color: #C8E6C9;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;";

    public BirthdayPicker() {
        super(8); // spacing

        // ── Year ──────────────────────────────────────────────────────────────
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 18; y >= currentYear - 100; y--) years.add(y);
        cbYear.setItems(FXCollections.observableArrayList(years));
        cbYear.setPromptText("Year");
        cbYear.setPrefWidth(100);
        cbYear.setStyle(CB_STYLE);

        // ── Month ─────────────────────────────────────────────────────────────
        List<String> months = new ArrayList<>();
        for (Month m : Month.values())
            months.add(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        cbMonth.setItems(FXCollections.observableArrayList(months));
        cbMonth.setPromptText("Month");
        cbMonth.setPrefWidth(130);
        cbMonth.setStyle(CB_STYLE);

        // ── Day ───────────────────────────────────────────────────────────────
        cbDay.setPromptText("Day");
        cbDay.setPrefWidth(80);
        cbDay.setStyle(CB_STYLE);
        updateDays();

        // Rebuild days when month or year changes
        cbMonth.valueProperty().addListener((o, a, b) -> updateDays());
        cbYear.valueProperty().addListener((o, a, b)  -> updateDays());

        getChildren().addAll(cbDay, cbMonth, cbYear);
    }

    private void updateDays() {
        int selectedDay = cbDay.getValue() != null ? cbDay.getValue() : -1;

        int month = cbMonth.getValue() != null
                ? cbMonth.getItems().indexOf(cbMonth.getValue()) + 1 : 1;
        int year  = cbYear.getValue()  != null ? cbYear.getValue() : LocalDate.now().getYear();

        int maxDay = YearMonth.of(year, month).lengthOfMonth();
        List<Integer> days = new ArrayList<>();
        for (int d = 1; d <= maxDay; d++) days.add(d);
        cbDay.setItems(FXCollections.observableArrayList(days));

        if (selectedDay >= 1 && selectedDay <= maxDay) cbDay.setValue(selectedDay);
        else cbDay.setValue(null);
    }

    /** Returns the selected date, or null if incomplete. */
    public LocalDate getValue() {
        if (cbDay.getValue() == null || cbMonth.getValue() == null || cbYear.getValue() == null)
            return null;
        int month = cbMonth.getItems().indexOf(cbMonth.getValue()) + 1;
        return LocalDate.of(cbYear.getValue(), month, cbDay.getValue());
    }

    /** Pre-fill with an existing date. */
    public void setValue(LocalDate date) {
        if (date == null) return;
        cbYear.setValue(date.getYear());
        cbMonth.setValue(date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        cbDay.setValue(date.getDayOfMonth());
    }

    /** Highlight all three boxes red on error. */
    public void setError(boolean hasError) {
        String style = hasError
                ? CB_STYLE.replace("#F0F7F0", "#FFF5F5").replace("#C8E6C9", "#EF4444")
                : CB_STYLE;
        cbDay.setStyle(style);
        cbMonth.setStyle(style);
        cbYear.setStyle(style);
    }

    public ComboBox<Integer> getDayBox()   { return cbDay;   }
    public ComboBox<String>  getMonthBox() { return cbMonth; }
    public ComboBox<Integer> getYearBox()  { return cbYear;  }
}
