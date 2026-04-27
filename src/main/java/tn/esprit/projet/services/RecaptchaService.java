package tn.esprit.projet.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * RecaptchaService — verifies reCAPTCHA v2 tokens with Google's API.
 * Uses the Google test secret key (always passes in dev).
 * Replace with real keys for production.
 */
public class RecaptchaService {

    // Google test keys — always valid in dev
    // Replace with real keys from https://www.google.com/recaptcha/admin
    private static final String SECRET_KEY = tn.esprit.projet.utils.AppConfig.recaptchaSecretKey();
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    /**
     * Verify a reCAPTCHA token.
     * @param token the g-recaptcha-response token from the widget
     * @return true if valid, false otherwise
     */
    public boolean verify(String token) {
        if (token == null || token.isBlank()) return false;

        try {
            URL url = new URL(VERIFY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "secret=" + URLEncoder.encode(SECRET_KEY, StandardCharsets.UTF_8)
                        + "&response=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) response.append(line);
            }

            String json = response.toString();
            System.out.println("[reCAPTCHA] Response: " + json);

            // Parse "success": true/false from JSON
            return json.contains("\"success\": true") || json.contains("\"success\":true");

        } catch (Exception e) {
            System.err.println("[reCAPTCHA] Verification error: " + e.getMessage());
            // Fail open — if network error, allow login
            return true;
        }
    }

    /**
     * Check if a token is non-empty (basic check before network call).
     */
    public boolean hasToken(String token) {
        return token != null && !token.isBlank();
    }
}
