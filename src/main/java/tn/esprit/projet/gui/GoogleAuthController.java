package tn.esprit.projet.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.esprit.projet.models.User;
import tn.esprit.projet.repository.UserRepository;
import tn.esprit.projet.utils.Nav;
import tn.esprit.projet.utils.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Google OAuth2 login via JavaFX WebView.
 *
 * Flow:
 * 1. Open Google OAuth consent screen in WebView
 * 2. Detect redirect to our redirect_uri containing ?code=...
 * 3. Exchange code for access_token via HTTP POST
 * 4. Fetch user info from Google API
 * 5. Find or create user in DB, then login
 *
 * SETUP: Replace CLIENT_ID and CLIENT_SECRET with your Google Cloud credentials.
 * In Google Cloud Console → APIs & Services → Credentials → OAuth 2.0 Client IDs
 * Add redirect URI: http://localhost:8080/callback
 */
public class GoogleAuthController {

    // ── Replace these with your real Google OAuth credentials ─────────────────
    private static final String CLIENT_ID     = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "YOUR_GOOGLE_CLIENT_SECRET";
    private static final String REDIRECT_URI  = "http://localhost:8080/callback";
    private static final String SCOPE         = "openid email profile";
    // ──────────────────────────────────────────────────────────────────────────

    @FXML private WebView           webView;
    @FXML private Label             statusLabel;
    @FXML private ProgressIndicator spinner;

    private final UserRepository repo = new UserRepository();

    /** Set to true when opened from register screen */
    public static boolean fromRegister = false;

    @FXML
    public void initialize() {
        if (CLIENT_ID.startsWith("YOUR_")) {
            // No credentials configured — show setup instructions
            showSetupInstructions();
            return;
        }

        WebEngine engine = webView.getEngine();

        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=code"
                + "&scope=" + SCOPE.replace(" ", "%20")
                + "&access_type=offline"
                + "&prompt=select_account";

        engine.load(authUrl);

        // Watch for redirect with auth code
        engine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl != null && newUrl.startsWith(REDIRECT_URI)) {
                engine.load("about:blank");
                if (statusLabel != null) statusLabel.setText("Authenticating...");
                if (spinner != null) spinner.setVisible(true);

                String code = extractParam(newUrl, "code");
                if (code != null) {
                    new Thread(() -> handleAuthCode(code)).start();
                } else {
                    Platform.runLater(() -> {
                        if (statusLabel != null) statusLabel.setText("Authentication cancelled.");
                    });
                }
            }
        });
    }

    private void handleAuthCode(String code) {
        try {
            // Exchange code for tokens
            String tokenJson = exchangeCodeForToken(code);
            String accessToken = extractJsonValue(tokenJson, "access_token");
            if (accessToken == null) throw new Exception("No access_token in response");

            // Get user info
            String userInfoJson = fetchUserInfo(accessToken);
            String googleId  = extractJsonValue(userInfoJson, "sub");
            String email     = extractJsonValue(userInfoJson, "email");
            String firstName = extractJsonValue(userInfoJson, "given_name");
            String lastName  = extractJsonValue(userInfoJson, "family_name");
            if (firstName == null) firstName = extractJsonValue(userInfoJson, "name");
            if (lastName  == null) lastName  = "";

            final String fEmail = email;
            final String fGoogleId = googleId;
            final String fFirst = firstName;
            final String fLast  = lastName;

            Platform.runLater(() -> processGoogleUser(fEmail, fGoogleId, fFirst, fLast));

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
                if (spinner != null) spinner.setVisible(false);
            });
        }
    }

    private void processGoogleUser(String email, String googleId, String firstName, String lastName) {
        if (spinner != null) spinner.setVisible(false);

        // Try find by googleId first, then by email
        User user = repo.findByGoogleId(googleId);
        if (user == null && email != null) user = repo.findByEmail(email);

        Stage stage = (Stage) webView.getScene().getWindow();

        if (fromRegister) {
            if (user != null) {
                if (statusLabel != null)
                    statusLabel.setText("This Gmail account is already registered. Please sign in instead.");
                return;
            }
            // Store Google data for CompleteProfile screen
            GoogleTempData.email     = email;
            GoogleTempData.googleId  = googleId;
            GoogleTempData.firstName = firstName;
            GoogleTempData.lastName  = lastName;
            stage.close();
            // Navigate to register with pre-filled data
            Nav.go((Stage) stage.getOwner(), "register.fxml", "NutriLife - Complete Profile");
        } else {
            // Login flow
            if (user == null) {
                if (statusLabel != null)
                    statusLabel.setText("This Gmail account is not registered yet. Please sign up.");
                return;
            }
            if (!user.isActive()) {
                if (statusLabel != null)
                    statusLabel.setText("Your account is deactivated.");
                return;
            }
            // Update googleId if not set
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                repo.update(user);
            }
            Session.login(user);
            stage.close();
            Stage owner = (Stage) stage.getOwner();
            if (user.isAdmin()) {
                Nav.go(owner, "admin_dashboard.fxml", "NutriLife - Admin", 1320, 780, true);
            } else {
                Nav.go(owner, "home.fxml", "NutriLife - Home", 1280, 760, true);
            }
        }
    }

    private void showSetupInstructions() {
        if (webView != null) webView.setVisible(false);
        if (statusLabel != null) {
            statusLabel.setText(
                "⚙️  Google OAuth not configured.\n\n" +
                "To enable Google login:\n" +
                "1. Go to console.cloud.google.com\n" +
                "2. Create OAuth 2.0 credentials\n" +
                "3. Add redirect URI: http://localhost:8080/callback\n" +
                "4. Set CLIENT_ID and CLIENT_SECRET in GoogleAuthController.java"
            );
            statusLabel.setWrapText(true);
            statusLabel.setStyle("-fx-font-size:13px;-fx-text-fill:#374151;-fx-padding:20;");
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) webView.getScene().getWindow()).close();
    }

    // ── HTTP helpers ───────────────────────────────────────────────────────────

    private String exchangeCodeForToken(String code) throws Exception {
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "code=" + code
                + "&client_id=" + CLIENT_ID
                + "&client_secret=" + CLIENT_SECRET
                + "&redirect_uri=" + REDIRECT_URI
                + "&grant_type=authorization_code";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }

    private String fetchUserInfo(String accessToken) throws Exception {
        URL url = new URL("https://www.googleapis.com/oauth2/v3/userinfo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        return readResponse(conn);
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private String extractParam(String url, String param) {
        try {
            String query = url.contains("?") ? url.substring(url.indexOf('?') + 1) : "";
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2 && kv[0].equals(param))
                    return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
        return null;
    }

    /** Minimal JSON value extractor — no external library needed */
    private String extractJsonValue(String json, String key) {
        if (json == null) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
        int end = start;
        if (json.charAt(colon + 1 + (colon + 1 < json.length() && json.charAt(colon + 1) == ' ' ? 1 : 0)) == '"') {
            // String value
            start = json.indexOf('"', colon + 1) + 1;
            end = json.indexOf('"', start);
        } else {
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        }
        if (start < 0 || end <= start) return null;
        return json.substring(start, end).trim();
    }

    /** Temp storage for Google data during registration */
    public static class GoogleTempData {
        public static String email, googleId, firstName, lastName;
    }
}
