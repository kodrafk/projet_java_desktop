package tn.esprit.projet.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyLog {

    public static final String[] MEAL_TYPES  = {"breakfast", "lunch", "dinner", "snacks"};
    public static final String[] MEAL_LABELS = {"Breakfast", "Lunch", "Dinner", "Snacks"};
    public static final String[] MEAL_EMOJIS = {"🌅", "☀️", "🌙", "🍿"};
    public static final String[] MEAL_COLORS = {"#f7a325", "#eb7147", "#52b788", "#a78bfa"};

    private int id;
    private int nutritionObjectiveId;
    private int dayNumber;
    private LocalDate date;
    private boolean completed;

    private int caloriesConsumed;
    private double proteinConsumed;
    private double carbsConsumed;
    private double fatsConsumed;
    private double waterConsumed;

    private String mood;
    private String notes;

    // Per-meal data stored as a map (mirrors the Symfony `meals` JSON column)
    // key = "breakfast"/"lunch"/"dinner"/"snacks"
    // value = {calories, protein, carbs, fats, logged}
    private final Map<String, MealData> meals = new HashMap<>();

    public DailyLog() {
        for (String type : MEAL_TYPES) {
            meals.put(type, new MealData());
        }
    }

    // ── Inner class for meal data ──
    public static class MealData {
        public int calories;
        public double protein;
        public double carbs;
        public double fats;
        public boolean logged;
        public List<String> foodNames = new java.util.ArrayList<>(); // individual food names
    }

    // ── Meal helpers ──
    public MealData getMealData(String mealType) {
        return meals.get(mealType);
    }

    public boolean isMealLogged(String mealType) {
        MealData m = meals.get(mealType);
        return m != null && m.logged;
    }

    public void setMealLogged(String mealType, boolean logged) {
        meals.computeIfAbsent(mealType, k -> new MealData()).logged = logged;
    }

    public int getMealCalories(String mealType) {
        MealData m = meals.get(mealType);
        return m != null ? m.calories : 0;
    }

    public void setMealMacros(String mealType, int cal, double protein, double carbs, double fats) {
        setMealMacros(mealType, cal, protein, carbs, fats, new java.util.ArrayList<>());
    }

    public void setMealMacros(String mealType, int cal, double protein, double carbs, double fats,
                               java.util.List<String> foodNames) {
        MealData m = meals.computeIfAbsent(mealType, k -> new MealData());
        m.calories   = cal;
        m.protein    = protein;
        m.carbs      = carbs;
        m.fats       = fats;
        m.logged     = true;
        m.foodNames  = foodNames != null ? foodNames : new java.util.ArrayList<>();
        recalculateTotals();
    }

    public void recalculateTotals() {
        caloriesConsumed = 0;
        proteinConsumed  = 0;
        carbsConsumed    = 0;
        fatsConsumed     = 0;
        for (MealData m : meals.values()) {
            if (m.logged) {
                caloriesConsumed += m.calories;
                proteinConsumed  += m.protein;
                carbsConsumed    += m.carbs;
                fatsConsumed     += m.fats;
            }
        }
        completed = meals.values().stream().anyMatch(m -> m.logged);
    }

    public int getLoggedMealsCount() {
        return (int) meals.values().stream().filter(m -> m.logged).count();
    }

    // ── JSON serialization for the `meals` column ──
    public String getMealsJson() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (String type : MEAL_TYPES) {
            MealData m = meals.getOrDefault(type, new MealData());
            if (!first) sb.append(",");
            sb.append("\"").append(type).append("\":{")
              .append("\"calories\":").append(m.calories).append(",")
              .append("\"protein\":").append(m.protein).append(",")
              .append("\"carbs\":").append(m.carbs).append(",")
              .append("\"fats\":").append(m.fats).append(",")
              .append("\"logged\":").append(m.logged).append(",")
              .append("\"foodNames\":[");
            if (m.foodNames != null) {
                for (int i = 0; i < m.foodNames.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(m.foodNames.get(i).replace("\"", "'")).append("\"");
                }
            }
            sb.append("],\"foods\":[],\"customFoods\":[]")
              .append("}");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /** Parse the meals JSON string from DB back into the meals map */
    public void parseMealsJson(String json) {
        if (json == null || json.isBlank()) return;
        for (String type : MEAL_TYPES) {
            MealData m = meals.computeIfAbsent(type, k -> new MealData());
            m.calories = extractInt(json, type, "calories");
            m.protein  = extractDouble(json, type, "protein");
            m.carbs    = extractDouble(json, type, "carbs");
            m.fats     = extractDouble(json, type, "fats");
            m.logged   = extractBool(json, type, "logged");
            m.foodNames = extractStringArray(json, type, "foodNames");
        }
        recalculateTotals();
    }

    private int extractInt(String json, String meal, String key) {
        try {
            String pattern = "\"" + meal + "\":{";
            int start = json.indexOf(pattern);
            if (start < 0) return 0;
            String sub = json.substring(start + pattern.length());
            String kp = "\"" + key + "\":";
            int ki = sub.indexOf(kp);
            if (ki < 0) return 0;
            String rest = sub.substring(ki + kp.length());
            int end = rest.indexOf(',');
            if (end < 0) end = rest.indexOf('}');
            if (end < 0) return 0;
            return (int) Double.parseDouble(rest.substring(0, end).trim());
        } catch (Exception e) { return 0; }
    }

    private double extractDouble(String json, String meal, String key) {
        try {
            String pattern = "\"" + meal + "\":{";
            int start = json.indexOf(pattern);
            if (start < 0) return 0;
            String sub = json.substring(start + pattern.length());
            String kp = "\"" + key + "\":";
            int ki = sub.indexOf(kp);
            if (ki < 0) return 0;
            String rest = sub.substring(ki + kp.length());
            int end = rest.indexOf(',');
            if (end < 0) end = rest.indexOf('}');
            if (end < 0) return 0;
            return Double.parseDouble(rest.substring(0, end).trim());
        } catch (Exception e) { return 0; }
    }

    private java.util.List<String> extractStringArray(String json, String meal, String key) {
        java.util.List<String> result = new java.util.ArrayList<>();
        try {
            String pattern = "\"" + meal + "\":{";
            int start = json.indexOf(pattern);
            if (start < 0) return result;
            String sub = json.substring(start + pattern.length());
            String kp = "\"" + key + "\":[";
            int ki = sub.indexOf(kp);
            if (ki < 0) return result;
            int arrStart = ki + kp.length();
            int arrEnd = sub.indexOf("]", arrStart);
            if (arrEnd < 0) return result;
            String arr = sub.substring(arrStart, arrEnd).trim();
            if (arr.isEmpty()) return result;
            for (String item : arr.split(",")) {
                String s = item.trim().replaceAll("^\"|\"$", "");
                if (!s.isEmpty()) result.add(s);
            }
        } catch (Exception ignored) {}
        return result;
    }

    private boolean extractBool(String json, String meal, String key) {
        try {
            String pattern = "\"" + meal + "\":{";
            int start = json.indexOf(pattern);
            if (start < 0) return false;
            String sub = json.substring(start + pattern.length());
            String kp = "\"" + key + "\":";
            int ki = sub.indexOf(kp);
            if (ki < 0) return false;
            String rest = sub.substring(ki + kp.length());
            int end = rest.indexOf(',');
            if (end < 0) end = rest.indexOf('}');
            if (end < 0) return false;
            return rest.substring(0, end).trim().equals("true");
        } catch (Exception e) { return false; }
    }

    // ── Status helpers ──
    public boolean isToday()  { return date != null && date.equals(LocalDate.now()); }
    public boolean isFuture() { return date != null && date.isAfter(LocalDate.now()); }
    public boolean isPast()   { return date != null && date.isBefore(LocalDate.now()); }

    // ── Getters & Setters ──
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNutritionObjectiveId() { return nutritionObjectiveId; }
    public void setNutritionObjectiveId(int id) { this.nutritionObjectiveId = id; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getCaloriesConsumed() { return caloriesConsumed; }
    public void setCaloriesConsumed(int v) { this.caloriesConsumed = v; }

    public double getProteinConsumed() { return proteinConsumed; }
    public void setProteinConsumed(double v) { this.proteinConsumed = v; }

    public double getCarbsConsumed() { return carbsConsumed; }
    public void setCarbsConsumed(double v) { this.carbsConsumed = v; }

    public double getFatsConsumed() { return fatsConsumed; }
    public void setFatsConsumed(double v) { this.fatsConsumed = v; }

    public double getWaterConsumed() { return waterConsumed; }
    public void setWaterConsumed(double v) { this.waterConsumed = v; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
