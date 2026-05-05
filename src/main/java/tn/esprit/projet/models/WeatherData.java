package tn.esprit.projet.models;

public class WeatherData {

    private String city;           // "Tunis"
    private double temperature;    // 12.5
    private String description;    // "cloudy", "clear sky", "rain"...
    private String icon;           // emoji affiché dans l'UI
    private String condition;      // catégorie simplifiée : "cold", "hot", "rainy", "snowy", "mild"

    public WeatherData() {}

    public WeatherData(String city, double temperature, String description, String icon, String condition) {
        this.city = city;
        this.temperature = temperature;
        this.description = description;
        this.icon = icon;
        this.condition = condition;
    }

    // ═══════════════════════════════════
    // GETTERS & SETTERS
    // ═══════════════════════════════════

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    // ═══════════════════════════════════
    // UTILITAIRE
    // ═══════════════════════════════════

    public String getFormattedTemperature() {
        return String.format("%.1f°C", temperature);
    }

    @Override
    public String toString() {
        return icon + " " + city + " — " + getFormattedTemperature() + " — " + description;
    }
}
