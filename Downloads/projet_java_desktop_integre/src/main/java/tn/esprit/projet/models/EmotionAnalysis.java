package tn.esprit.projet.models;

public class EmotionAnalysis {
    private String primaryEmotion;
    private double emotionScore;
    private int urgencyLevel;
    private String recommendation;
    
    public EmotionAnalysis() {
    }
    
    public EmotionAnalysis(String primaryEmotion, double emotionScore, int urgencyLevel, String recommendation) {
        this.primaryEmotion = primaryEmotion;
        this.emotionScore = emotionScore;
        this.urgencyLevel = urgencyLevel;
        this.recommendation = recommendation;
    }

    public String getPrimaryEmotion() { return primaryEmotion; }
    public double getEmotionScore() { return emotionScore; }
    public int getUrgencyLevel() { return urgencyLevel; }
    public String getRecommendation() { return recommendation; }

    public void setPrimaryEmotion(String primaryEmotion) { this.primaryEmotion = primaryEmotion; }
    public void setEmotionScore(double emotionScore) { this.emotionScore = emotionScore; }
    public void setUrgencyLevel(int urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    @Override
    public String toString() {
        return "EmotionAnalysis{" +
                "primaryEmotion='" + primaryEmotion + '\'' +
                ", emotionScore=" + emotionScore +
                ", urgencyLevel=" + urgencyLevel +
                ", recommendation='" + recommendation + '\'' +
                '}';
    }
}
