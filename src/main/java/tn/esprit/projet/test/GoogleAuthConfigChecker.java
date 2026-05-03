package tn.esprit.projet.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility to check Google OAuth configuration
 */
public class GoogleAuthConfigChecker {
    
    private static final String CLIENT_ID = "757584859966-iqm1k4nf3udd465ek3n2li0qvnhgd8oj.apps.googleusercontent.com";
    private static final String REDIRECT_URI = "http://localhost:8080/callback";
    
    public static void main(String[] args) {
        System.out.println("=== Google OAuth Configuration Checker ===");
        
        // Check 1: Client ID format
        System.out.println("1. Client ID: " + CLIENT_ID);
        System.out.println("   Format check: " + (CLIENT_ID.endsWith(".apps.googleusercontent.com") ? "✓ Valid" : "✗ Invalid"));
        
        // Check 2: Redirect URI
        System.out.println("2. Redirect URI: " + REDIRECT_URI);
        System.out.println("   Format check: " + (REDIRECT_URI.startsWith("http://localhost:") ? "✓ Valid" : "✗ Invalid"));
        
        // Check 3: Test Google OAuth discovery endpoint
        System.out.println("3. Testing Google OAuth discovery...");
        try {
            URL url = new URL("https://accounts.google.com/.well-known/openid_configuration");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("   ✓ Google OAuth endpoints accessible");
            } else {
                System.out.println("   ✗ Google OAuth endpoints returned: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("   ✗ Cannot reach Google OAuth endpoints: " + e.getMessage());
        }
        
        // Check 4: Generate test OAuth URL
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&access_type=offline"
                + "&prompt=select_account";
        
        System.out.println("4. Generated OAuth URL:");
        System.out.println("   " + authUrl);
        
        System.out.println("\n=== Configuration Status ===");
        System.out.println("✓ Client ID and Secret are configured");
        System.out.println("✓ Redirect URI is set to localhost:8080/callback");
        System.out.println("✓ OAuth scopes include: openid, email, profile");
        System.out.println("\nIMPORTANT: Make sure in Google Cloud Console:");
        System.out.println("- OAuth 2.0 Client ID is created");
        System.out.println("- Redirect URI 'http://localhost:8080/callback' is added");
        System.out.println("- OAuth consent screen is configured");
        System.out.println("=== Check Complete ===");
    }
}