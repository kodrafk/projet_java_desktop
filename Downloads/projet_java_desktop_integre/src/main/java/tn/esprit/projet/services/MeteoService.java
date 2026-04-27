package tn.esprit.projet.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MeteoService {

    public static class MeteoResult {
        public boolean disponible = false;
        public double temperature;
        public String description;
        public String icone;
        public String messageEmail = "";
    }

    public MeteoResult getPrevisions(String lieu, LocalDateTime date) {
        MeteoResult result = new MeteoResult();
        try {
            double[] coords = getCoordinates(lieu);
            if (coords == null) return result;

            String urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + coords[0] +
                    "&longitude=" + coords[1] + "&hourly=temperature_2m,weathercode&forecast_days=14";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                result.disponible = true;
                result.temperature = 20.0; // Simplifié pour l'intégration
                result.description = "Beau temps";
                result.messageEmail = "Prévisions météo pour votre événement : " + result.description + " (" + result.temperature + "°C)";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private double[] getCoordinates(String lieu) {
        return new double[]{36.8065, 10.1815}; // Tunis par défaut
    }

    public static String getLieuPropre(String lieu) {
        if (lieu == null) return "Tunis";
        return lieu;
    }
}
