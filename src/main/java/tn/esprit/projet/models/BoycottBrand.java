package tn.esprit.projet.models;

import java.time.LocalDate;

public class BoycottBrand {

    private int id;
    private String brandName;
    private String parentCompany;
    private String reason;
    private String alternatives;
    private String category;
    private String sourceUrl;
    private LocalDate dateAdded;

    // =====================
    // CONSTRUCTEURS
    // =====================

    public BoycottBrand() {}

    public BoycottBrand(String brandName, String parentCompany, String reason,
                        String alternatives, String category) {
        this.brandName = brandName;
        this.parentCompany = parentCompany;
        this.reason = reason;
        this.alternatives = alternatives;
        this.category = category;
        this.dateAdded = LocalDate.now();
    }

    public BoycottBrand(int id, String brandName, String parentCompany, String reason,
                        String alternatives, String category, String sourceUrl, LocalDate dateAdded) {
        this.id = id;
        this.brandName = brandName;
        this.parentCompany = parentCompany;
        this.reason = reason;
        this.alternatives = alternatives;
        this.category = category;
        this.sourceUrl = sourceUrl;
        this.dateAdded = dateAdded;
    }

    // =====================
    // GETTERS & SETTERS
    // =====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getParentCompany() { return parentCompany; }
    public void setParentCompany(String parentCompany) { this.parentCompany = parentCompany; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getAlternatives() { return alternatives; }
    public void setAlternatives(String alternatives) { this.alternatives = alternatives; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public LocalDate getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDate dateAdded) { this.dateAdded = dateAdded; }

    // =====================
    // TO STRING
    // =====================

    @Override
    public String toString() {
        return "BoycottBrand{" +
                "id=" + id +
                ", brandName='" + brandName + '\'' +
                ", parentCompany='" + parentCompany + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}