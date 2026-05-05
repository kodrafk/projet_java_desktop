package tn.esprit.projet.models;

import java.time.LocalDate;

public class CourseItem {

    // ═══════════ ATTRIBUTES ═══════════
    private int id;
    private String ingredientName;
    private double quantity;
    private String unit;
    private LocalDate dateAdded;
    private boolean purchased;

    // ═══════════ CONSTRUCTORS ═══════════

    /** Empty constructor */
    public CourseItem() {
    }

    /** Constructor WITHOUT id (for creation) */
    public CourseItem(String ingredientName, double quantity, String unit, LocalDate dateAdded) {
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.dateAdded = dateAdded;
        this.purchased = false;
    }

    /** Constructor WITH id (for display/update) */
    public CourseItem(int id, String ingredientName, double quantity,
                      String unit, LocalDate dateAdded, boolean purchased) {
        this.id = id;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.dateAdded = dateAdded;
        this.purchased = purchased;
    }

    // ═══════════ GETTERS / SETTERS ═══════════

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDate getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDate dateAdded) { this.dateAdded = dateAdded; }

    public boolean isPurchased() { return purchased; }
    public void setPurchased(boolean purchased) { this.purchased = purchased; }

    // ═══════════ UTILITY METHODS ═══════════

    @Override
    public String toString() {
        return "CourseItem{" +
                "id=" + id +
                ", ingredientName='" + ingredientName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", dateAdded=" + dateAdded +
                ", purchased=" + purchased +
                '}';
    }
}