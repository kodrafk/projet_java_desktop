package tn.esprit.projet.services;

import tn.esprit.projet.models.EmotionAnalysis;
import tn.esprit.projet.utils.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EmotionAnalysisService {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static EmotionAnalysis analyzeComplaintEmotion(String complaintTitle, String complaintDescription) {
        if (complaintTitle == null || complaintTitle.isEmpty() || 
            complaintDescription == null || complaintDescription.isEmpty()) {
            return new EmotionAnalysis("NEUTRAL", 0, 1, "No emotion detected");
        }

        String response = GeminiService.analyzeEmotion(complaintTitle, complaintDescription);
        
        if (response == null || response.isEmpty()) {
            return new EmotionAnalysis("NEUTRAL", 0, 1, "Analysis failed");
        }

        return parseEmotionResponse(response);
    }

    private static EmotionAnalysis parseEmotionResponse(String jsonResponse) {
        try {
            String cleanedResponse = jsonResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode node = mapper.readTree(cleanedResponse);

            String emotion = node.has("emotion") ? node.get("emotion").asText() : "NEUTRAL";
            double score = node.has("score") ? node.get("score").asDouble() : 0;
            int urgency = node.has("urgency") ? node.get("urgency").asInt() : 1;

            urgency = Math.max(1, Math.min(5, urgency));

            String recommendation = getRecommendation(emotion, urgency, score);

            return new EmotionAnalysis(emotion, score, urgency, recommendation);

        } catch (Exception e) {
            System.err.println("Error parsing emotion response: " + e.getMessage());
            return new EmotionAnalysis("NEUTRAL", 0, 1, "Parsing error");
        }
    }

    private static String getRecommendation(String emotion, int urgency, double score) {
        if (urgency >= 4) {
            return "🔴 URGENT - Assign to senior support staff immediately";
        } else if (urgency == 3) {
            return "🟠 HIGH - Prioritize for response within 2 hours";
        } else if (urgency == 2) {
            return "🟡 MEDIUM - Standard response within 24 hours";
        } else {
            return "🟢 LOW - Can be handled in regular queue";
        }
    }

    public static String getUrgencyColor(int urgencyLevel) {
        switch (urgencyLevel) {
            case 5:
            case 4:
                return "#EF4444";
            case 3:
                return "#F97316";
            case 2:
                return "#EAB308";
            default:
                return "#10B981";
        }
    }

    public static String getUrgencyEmoji(int urgencyLevel) {
        switch (urgencyLevel) {
            case 5:
            case 4:
                return "🔴";
            case 3:
                return "🟠";
            case 2:
                return "🟡";
            default:
                return "🟢";
        }
    }

    public static String getUrgencyLabel(int urgencyLevel) {
        switch (urgencyLevel) {
            case 5:
                return "CRITICAL";
            case 4:
                return "URGENT";
            case 3:
                return "HIGH";
            case 2:
                return "MEDIUM";
            default:
                return "LOW";
        }
    }
}
