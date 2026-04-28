import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Quick test to verify Gemini API key is working
 * Compile: javac test_gemini_api.java
 * Run: java test_gemini_api
 */
public class test_gemini_api {
    
    private static final String API_KEY = "AIzaSyBxO-t07gZhkNmm6x9XwYBNAvH5ix__c5E";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    
    public static void main(String[] args) {
        System.out.println("=== Testing Gemini API ===");
        System.out.println("API Key: " + API_KEY.substring(0, 10) + "...");
        System.out.println("API URL: " + API_URL);
        System.out.println();
        
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            String jsonInput = "{\"contents\":[{\"parts\":[{\"text\":\"Say hello\"}]}]}";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("X-goog-api-key", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                    .build();
            
            System.out.println("Sending test request...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            System.out.println();
            
            if (response.statusCode() == 200) {
                System.out.println("✅ SUCCESS! API key is working correctly.");
                System.out.println("Your AI features should work now.");
            } else if (response.statusCode() == 400) {
                System.out.println("❌ ERROR: Invalid API key or malformed request");
                System.out.println("Solution: Get a new API key from https://aistudio.google.com/app/apikey");
            } else if (response.statusCode() == 429) {
                System.out.println("⚠️ WARNING: Quota exceeded (20 requests/day limit)");
                System.out.println("Solution: Wait until tomorrow or upgrade your plan");
            } else if (response.statusCode() == 403) {
                System.out.println("❌ ERROR: API key doesn't have permission");
                System.out.println("Solution: Enable Gemini API in Google Cloud Console");
            } else {
                System.out.println("❌ ERROR: Unexpected response code");
            }
            
        } catch (Exception e) {
            System.out.println("❌ EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
