import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class TestGemini {
    public static void main(String[] args) throws Exception {
        String API_KEY = "AIzaSyDrPPwepFzZEC-km4L6wcV1BPdFV1pDg5Q";
        String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
        String body = "{\"contents\":[{\"parts\":[{\"text\":\"Hello\"}]}],\"generationConfig\":{\"temperature\":0.75,\"maxOutputTokens\":2000}}";
        
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
            
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + resp.statusCode());
        System.out.println("Body: " + resp.body());
    }
}
