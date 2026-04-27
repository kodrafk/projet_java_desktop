package tn.esprit.projet.utils;

import java.io.InputStream;
import java.util.Properties;

/**
 * Loads app configuration from config.properties.
 * Keys are never hardcoded in source files.
 */
public class AppConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
                System.out.println("[Config] ✅ config.properties loaded");
            } else {
                System.err.println("[Config] ❌ config.properties not found");
            }
        } catch (Exception e) {
            System.err.println("[Config] Error: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return props.getProperty(key, "");
    }

    public static String googleClientId()     { return get("google.client.id"); }
    public static String googleClientSecret() { return get("google.client.secret"); }
    public static String recaptchaSiteKey()   { return get("recaptcha.site.key"); }
    public static String recaptchaSecretKey() { return get("recaptcha.secret.key"); }
}
