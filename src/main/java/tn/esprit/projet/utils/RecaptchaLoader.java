package tn.esprit.projet.utils;

import javafx.scene.control.Label;
import javafx.scene.web.WebView;

/**
 * Utility to load reCAPTCHA widget into a JavaFX WebView.
 * Injects the real site key from config.properties at runtime.
 */
public class RecaptchaLoader {

    /**
     * Load reCAPTCHA into the given WebView.
     * Injects the site key dynamically so it's never hardcoded in HTML.
     */
    public static void load(WebView webView, Label statusLabel) {
        if (webView == null) return;

        String siteKey = AppConfig.recaptchaSiteKey();
        if (siteKey == null || siteKey.isBlank() || siteKey.startsWith("YOUR_")) {
            System.err.println("[reCAPTCHA] Site key not configured in config.properties");
            return;
        }

        try {
            // Start embedded HTTP server (required — Google reCAPTCHA refuses file:// URLs)
            RecaptchaServer.start();

            // Load from localhost HTTP server
            String url = RecaptchaServer.getUrl();
            webView.getEngine().load(url);

            webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, state) -> {
                if (state == javafx.concurrent.Worker.State.SUCCEEDED) {
                    System.out.println("[reCAPTCHA] ✅ Widget ready at " + url);
                } else if (state == javafx.concurrent.Worker.State.FAILED) {
                    System.err.println("[reCAPTCHA] ❌ Failed to load widget");
                }
            });

            System.out.println("[reCAPTCHA] Loading from: " + url);

        } catch (Exception e) {
            System.err.println("[reCAPTCHA] Load error: " + e.getMessage());
        }
    }

    /**
     * Get the current token from the WebView.
     * Returns null if not yet verified.
     */
    public static String getToken(WebView webView) {
        if (webView == null) return null;
        try {
            Object result = webView.getEngine().executeScript("getToken()");
            return result != null && !result.toString().equals("null") ? result.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reset the reCAPTCHA widget (after failed attempt).
     */
    public static void reset(WebView webView) {
        if (webView == null) return;
        try {
            webView.getEngine().executeScript("reset()");
        } catch (Exception ignored) {}
    }
}
