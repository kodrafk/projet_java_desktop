import tn.esprit.projet.utils.BadWordAPI;

public class TestBadWordNow {
    public static void main(String[] args) {
        System.out.println("=== Test BadWord API ===");
        
        // Test simple
        String[] tests = {
            "Hello world",
            "This is shit",
            "You are stupid", 
            "Great service",
            "This contains fuck"
        };
        
        for (String text : tests) {
            try {
                boolean result = BadWordAPI.hasProfanity(text);
                System.out.println("\"" + text + "\" -> " + (result ? "❌ BLOCKED" : "✅ ALLOWED"));
            } catch (Exception e) {
                System.out.println("ERROR testing \"" + text + "\": " + e.getMessage());
            }
        }
        
        System.out.println("\n=== Test terminé ===");
    }
}