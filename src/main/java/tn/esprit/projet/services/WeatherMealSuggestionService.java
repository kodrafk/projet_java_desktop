package tn.esprit.projet.services;

import tn.esprit.projet.models.Recette;

import java.time.LocalDate;
import java.util.List;

public class WeatherMealSuggestionService {

    private final WeatherService weatherService;
    private final RecetteService recetteService;

    public WeatherMealSuggestionService() {
        this.weatherService = new WeatherService();
        this.recetteService = new RecetteService();
    }

    /**
     * Méthode principale : retourne une recette suggérée selon la météo actuelle
     */
    public Suggestion getWeatherBasedSuggestion() {
        WeatherService.WeatherInfo weather = weatherService.getWeatherForDate(
                weatherService.getDefaultCity(), LocalDate.now()
        );
        String condition = resolveCondition(weather);
        String recipeType = getRecipeTypeByCondition(condition);
        Recette recipe = getRandomRecipeByType(recipeType);
        return new Suggestion(weather, condition, recipe);
    }

    /**
     * Détermine la condition simplifiée à partir de la température et description
     */
    private String resolveCondition(WeatherService.WeatherInfo weather) {
        double temp = weather.getTemperature();
        String desc = weather.getDescription().toLowerCase();

        if (desc.contains("snow") || desc.contains("neige")) return "snowy";
        if (desc.contains("rain") || desc.contains("pluie") || desc.contains("drizzle")) return "rainy";
        if (temp <= 10) return "cold";
        if (temp >= 28) return "hot";
        return "mild";
    }

    /**
     * Logique de mapping condition → type de recette
     */
    private String getRecipeTypeByCondition(String condition) {
        switch (condition) {
            case "cold":   return "soup";       // Soupe, plat chaud
            case "rainy":  return "main dish";  // Comfort food
            case "hot":    return "entree";     // Salade, plat frais
            case "snowy":  return "main dish";  // Plat très chaud
            case "mild":
            default:       return "entree";     // Léger, healthy
        }
    }

    /**
     * Récupère une recette aléatoire du type donné
     */
    private Recette getRandomRecipeByType(String type) {
        List<Recette> recipes = recetteService.getRecettesByType(type);

        if (recipes.isEmpty()) {
            recipes = recetteService.getAll();
        }

        if (recipes.isEmpty()) {
            return null;
        }

        int randomIndex = (int) (Math.random() * recipes.size());
        return recipes.get(randomIndex);
    }

    // ═══════════════════════════════════
    // CLASSE EMBARQUÉE POUR RETOUR
    // ═══════════════════════════════════

    public static class Suggestion {
        private final WeatherService.WeatherInfo weather;
        private final String condition;
        private final Recette recipe;

        public Suggestion(WeatherService.WeatherInfo weather, String condition, Recette recipe) {
            this.weather = weather;
            this.condition = condition;
            this.recipe = recipe;
        }

        public WeatherService.WeatherInfo getWeather() { return weather; }
        public String getCondition() { return condition; }
        public Recette getRecipe() { return recipe; }
        public boolean hasRecipe() { return recipe != null; }
    }
}
