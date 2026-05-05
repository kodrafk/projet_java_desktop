package tn.esprit.projet.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Verifies reCAPTCHA v2 tokens with Google's API.
 * Uses Google's official test keys by default (always pass in dev).
 * Replace config.properties keys with real ones for production.
 */
public class RecaptchaService {

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    // Google's official test secret key — always returns success
    private static final String TEST_SECRET = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe";

    /**
     * Verify a reCAPTCHA token against Google's API.
     * @param token the g-recaptcha-response token from the widget
     * @return true if valid
     */
    public boolean verify(String token) {
        if (token == null || token.isBlank()) return false;

        // Get secret key from config (falls back to test key)
        String secretKey = tn.esprit.projet.utils.AppConfig.recaptchaSecretKey();
        if (secretKey == null || secretKey.isBlank() || secretKey.startsWith("YOUR_")) {
            secretKey = TEST_SECRET;
        }

        // If using the test secret key, skip network call — always valid
        if (TEST_SECRET.equals(secretKey)) {
            System.out.println("[reCAPTCHA] ✅ Test key — verification passed.");
            return true;
        }

        // Real key — verify with Google
        try {
            URL url = new URL(VERIFY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "secret=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8)
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
            return json.contains("\"success\": true") || json.contains("\"success\":true");

        } catch (Exception e) {
            System.err.println("[reCAPTCHA] Verification error: " + e.getMessage());
            return true; // fail open on network error
        }
    }

    public boolean hasToken(String token) {
        return token != null && !token.isBlank();
    }
}
