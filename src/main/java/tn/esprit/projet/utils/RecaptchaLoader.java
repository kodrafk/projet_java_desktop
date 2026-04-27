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

        // Build HTML inline with the real site key injected
        String html = "<!DOCTYPE html><html><head>"
            + "<meta charset='UTF-8'>"
            + "<script src='https://www.google.com/recaptcha/api.js' async defer></script>"
            + "<style>"
            + "* { margin:0; padding:0; box-sizing:border-box; }"
            + "body { display:flex; justify-content:center; align-items:center;"
            + "       height:100vh; background:transparent; overflow:hidden; }"
            + ".wrap { transform:scale(0.88); transform-origin:center; }"
            + "</style></head><body>"
            + "<div class='wrap'>"
            + "<div class='g-recaptcha'"
            + "     data-sitekey='" + siteKey + "'"
            + "     data-callback='onSuccess'"
            + "     data-expired-callback='onExpired'"
            + "     data-error-callback='onError'>"
            + "</div></div>"
            + "<script>"
            + "window.recaptchaToken = null;"
            + "function onSuccess(t) { window.recaptchaToken = t; console.log('reCAPTCHA OK'); }"
            + "function onExpired()  { window.recaptchaToken = null; }"
            + "function onError()    { window.recaptchaToken = null; }"
            + "function getToken()   { return window.recaptchaToken; }"
            + "function reset()      { if(typeof grecaptcha!='undefined') { grecaptcha.reset(); window.recaptchaToken=null; } }"
            + "</script></body></html>";

        webView.getEngine().loadContent(html);

        System.out.println("[reCAPTCHA] Widget loaded with site key: " + siteKey.substring(0, 10) + "...");
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
