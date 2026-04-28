import tn.esprit.projet.utils.BadWordAPI;

public class TestBadWordAPI {
    public static void main(String[] args) {
        System.out.println("=== Testing BadWord API ===");
        
        // Test the API
        BadWordAPI.testAPI();
        
        System.out.println("\n=== Manual Tests ===");
        
        // Manual tests
        testText("Hello world, this is a clean message");
        testText("This message contains fuck word");
        testText("You are stupid");
        testText("Great service, thank you!");
        testText("This is shit quality");
        
        System.out.println("\n=== Test Complete ===");
    }
    
    private static void testText(String text) {
        boolean hasProfanity = BadWordAPI.hasProfanity(text);
        System.out.println("\"" + text + "\" -> " + (hasProfanity ? "❌ BLOCKED" : "✅ ALLOWED"));
    }
}