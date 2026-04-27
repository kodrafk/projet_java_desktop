package tn.esprit.projet.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class WeatherService {
    
    private String apiKey;
    private String defaultCity;
    private String language;
    private String units;
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String CONFIG_FILE = "weather.properties";
    
    public WeatherService() {
        loadConfiguration();
    }
    
    /**
     * Charge la configuration depuis le fichier weather.properties
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
                apiKey = props.getProperty("api.key", "votre_cle_api_openweathermap");
                defaultCity = props.getProperty("default.city", "Tunis");
                language = props.getProperty("language", "fr");
                units = props.getProperty("units", "metric");
            } else {
                // Fichier non trouvé, essayer de le lire depuis le répertoire racine
                try (FileInputStream fileInput = new FileInputStream(CONFIG_FILE)) {
                    props.load(fileInput);
                    apiKey = props.getProperty("api.key", "votre_cle_api_openweathermap");
                    defaultCity = props.getProperty("default.city", "Tunis");
                    language = props.getProperty("language", "fr");
                    units = props.getProperty("units", "metric");
                } catch (IOException e) {
                    // Utiliser les valeurs par défaut
                    apiKey = "votre_cle_api_openweathermap";
                    defaultCity = "Tunis";
                    language = "fr";
                    units = "metric";
                    System.err.println("Fichier weather.properties non trouvé, utilisation des valeurs par défaut");
                }
            }
        } catch (IOException e) {
            // Utiliser les valeurs par défaut
            apiKey = "votre_cle_api_openweathermap";
            defaultCity = "Tunis";
            language = "fr";
            units = "metric";
            System.err.println("Erreur lors du chargement de la configuration météo: " + e.getMessage());
        }
    }
    
    /**
     * Récupère les informations météo pour une ville et une date données
     * @param ville Nom de la ville
     * @param date Date pour laquelle récupérer la météo
     * @return Objet WeatherInfo contenant les données météo
     */
    public WeatherInfo getWeatherForDate(String ville, LocalDate date) {
        try {
            String urlString = String.format("%s?q=%s&appid=%s&units=%s&lang=%s", 
                API_URL, ville, apiKey, units, language);
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
                return parseWeatherData(response.toString(), date);
            } else {
                System.err.println("Erreur API météo: " + responseCode);
                return getDefaultWeather();
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de la météo: " + e.getMessage());
            return getDefaultWeather();
        }
    }
    
    /**
     * Parse les données JSON de l'API météo
     */
    private WeatherInfo parseWeatherData(String jsonData, LocalDate targetDate) {
        try {
            JsonObject root = JsonParser.parseString(jsonData).getAsJsonObject();
            var list = root.getAsJsonArray("list");
            
            // Chercher la prévision la plus proche de la date cible
            for (int i = 0; i < list.size(); i++) {
                JsonObject forecast = list.get(i).getAsJsonObject();
                String dateStr = forecast.get("dt_txt").getAsString();
                LocalDate forecastDate = LocalDate.parse(dateStr.substring(0, 10));
                
                if (forecastDate.equals(targetDate)) {
                    JsonObject main = forecast.getAsJsonObject("main");
                    JsonObject weather = forecast.getAsJsonArray("weather").get(0).getAsJsonObject();
                    
                    double temp = main.get("temp").getAsDouble();
                    String description = weather.get("description").getAsString();
                    String icon = weather.get("icon").getAsString();
                    
                    return new WeatherInfo(temp, description, icon);
                }
            }
            
            return getDefaultWeather();
            
        } catch (Exception e) {
            System.err.println("Erreur parsing météo: " + e.getMessage());
            return getDefaultWeather();
        }
    }
    
    /**
     * Retourne une météo par défaut en cas d'erreur
     */
    private WeatherInfo getDefaultWeather() {
        return new WeatherInfo(20.0, "Données météo non disponibles", "01d");
    }
    
    /**
     * Retourne la ville par défaut configurée
     */
    public String getDefaultCity() {
        return defaultCity;
    }
    
    /**
     * Vérifie si l'API est configurée
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.equals("votre_cle_api_openweathermap");
    }
    
    /**
     * Classe interne pour stocker les informations météo
     */
    public static class WeatherInfo {
        private final double temperature;
        private final String description;
        private final String iconCode;
        
        public WeatherInfo(double temperature, String description, String iconCode) {
            this.temperature = temperature;
            this.description = description;
            this.iconCode = iconCode;
        }
        
        public double getTemperature() {
            return temperature;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getIconCode() {
            return iconCode;
        }
        
        public String getWeatherEmoji() {
            // Convertir le code icon en emoji
            switch (iconCode.substring(0, 2)) {
                case "01": return "☀️";  // Ciel dégagé
                case "02": return "⛅";  // Peu nuageux
                case "03": return "☁️";  // Nuageux
                case "04": return "☁️";  // Très nuageux
                case "09": return "🌧️"; // Pluie
                case "10": return "🌦️"; // Pluie légère
                case "11": return "⛈️";  // Orage
                case "13": return "❄️";  // Neige
                case "50": return "🌫️"; // Brouillard
                default: return "🌤️";
            }
        }
        
        public String getFormattedTemp() {
            return String.format("%.1f°C", temperature);
        }
        
        @Override
        public String toString() {
            return String.format("%s %s - %s", getWeatherEmoji(), getFormattedTemp(), description);
        }
    }
}
