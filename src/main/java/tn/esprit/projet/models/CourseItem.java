package tn.esprit.projet.models;

import java.time.LocalDate;

public class CourseItem {
    private int id;
    private String ingredientName;
    private double quantity;
    private String unit;
    private LocalDate dateAdded;
    private boolean purchased;

    public CourseItem() {}

    public CourseItem(String ingredientName, double quantity, String unit, LocalDate dateAdded) {
        this.ingredientName = ingredientName; this.quantity = quantity;
        this.unit = unit; this.dateAdded = dateAdded; this.purchased = false;
    }

    public CourseItem(int id, String ingredientName, double quantity, String unit, LocalDate dateAdded, boolean purchased) {
        this.id = id; this.ingredientName = ingredientName; this.quantity = quantity;
        this.unit = unit; this.dateAdded = dateAdded; this.purchased = purchased;
    }

    public int getId() { return id; } public void setId(int id) { this.id = id; }
    public String getIngredientName() { return ingredientName; } public void setIngredientName(String n) { this.ingredientName = n; }
    public double getQuantity() { return quantity; } public void setQuantity(double q) { this.quantity = q; }
    public String getUnit() { return unit; } public void setUnit(String u) { this.unit = u; }
    public LocalDate getDateAdded() { return dateAdded; } public void setDateAdded(LocalDate d) { this.dateAdded = d; }
    public boolean isPurchased() { return purchased; } public void setPurchased(boolean p) { this.purchased = p; }
}
