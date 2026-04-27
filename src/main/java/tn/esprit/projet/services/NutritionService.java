package tn.esprit.projet.services;

import tn.esprit.projet.models.User;

/**
 * Calculates nutrition metrics and generates personalized health analysis.
 *
 * Used after the user sets their photo, weight, height and birthday.
 */
public class NutritionService {

    /**
     * Full health analysis for a user.
     */
    public static class HealthAnalysis {
        public final double bmi;
        public final String bmiCategory;
        public final String bmiColor;       // CSS color
        public final double idealWeightMin; // kg
        public final double idealWeightMax; // kg
        public final double kgToLose;       // > 0 = need to lose, < 0 = need to gain
        public final String motivationTitle;
        public final String motivationMessage;
        public final String actionAdvice;
        public final int    dailyCalories;  // estimated TDEE
        public final String progressEmoji;

        public HealthAnalysis(double bmi, String bmiCategory, String bmiColor,
                              double idealWeightMin, double idealWeightMax,
                              double kgToLose, String motivationTitle,
                              String motivationMessage, String actionAdvice,
                              int dailyCalories, String progressEmoji) {
            this.bmi = bmi;
            this.bmiCategory = bmiCategory;
            this.bmiColor = bmiColor;
            this.idealWeightMin = idealWeightMin;
            this.idealWeightMax = idealWeightMax;
            this.kgToLose = kgToLose;
            this.motivationTitle = motivationTitle;
            this.motivationMessage = motivationMessage;
            this.actionAdvice = actionAdvice;
            this.dailyCalories = dailyCalories;
            this.progressEmoji = progressEmoji;
        }
    }

    /**
     * Full health analysis based on weight, height, age and gender.
     */
    public HealthAnalysis analyse(User user) {
        double weight = user.getWeight();
        double height = user.getHeight();
        int    age    = user.getAge();

        if (weight <= 0 || height <= 0) return null;

        // BMI = weight / (height/100)²
        double heightM = height / 100.0;
        double bmi = Math.round((weight / (heightM * heightM)) * 10.0) / 10.0;

        // Ideal weight range (Devine formula adapted)
        // Ideal BMI range: 18.5 – 24.9
        double idealMin = Math.round(18.5 * heightM * heightM * 10.0) / 10.0;
        double idealMax = Math.round(24.9 * heightM * heightM * 10.0) / 10.0;

        // kg to lose (positive) or gain (negative)
        double kgToLose;
        if (bmi > 24.9) {
            kgToLose = Math.round((weight - idealMax) * 10.0) / 10.0;
        } else if (bmi < 18.5) {
            kgToLose = Math.round((weight - idealMin) * 10.0) / 10.0; // negative = need to gain
        } else {
            kgToLose = 0;
        }

        // BMI category + color + motivation
        String category, color, emoji, title, message, advice;
        if (bmi < 18.5) {
            category = "Underweight";
            color    = "#3B82F6"; // blue
            emoji    = "💙";
            title    = "You need to gain some weight";
            message  = String.format("Your BMI is %.1f. You should gain about %.1f kg to reach a healthy weight.", bmi, Math.abs(kgToLose));
            advice   = "Increase your caloric intake with nutritious foods: nuts, avocado, legumes, whole grains.";
        } else if (bmi < 25.0) {
            category = "Normal weight";
            color    = "#16A34A"; // green
            emoji    = "💚";
            title    = "Excellent! You are in great health";
            message  = String.format("Your BMI is %.1f — in the ideal range (18.5–24.9). Keep it up!", bmi);
            advice   = "Maintain your weight with a balanced diet and regular physical activity.";
        } else if (bmi < 30.0) {
            category = "Overweight";
            color    = "#F59E0B"; // amber
            emoji    = "🟡";
            title    = "Goal: lose " + String.format("%.1f", kgToLose) + " kg";
            message  = String.format("Your BMI is %.1f. Losing %.1f kg would bring you into the healthy range.", bmi, kgToLose);
            advice   = "Reduce refined sugars and saturated fats. Aim for 30 min of physical activity per day.";
        } else {
            category = "Obesity";
            color    = "#EF4444"; // red
            emoji    = "❤️";
            title    = "Goal: lose " + String.format("%.1f", kgToLose) + " kg";
            message  = String.format("Your BMI is %.1f. Medical follow-up is recommended. Goal: lose %.1f kg.", bmi, kgToLose);
            advice   = "Consult a nutritionist. Start with small changes: less sugar, more vegetables, daily walks.";
        }

        // Estimated daily calories (Mifflin-St Jeor, sedentary × 1.2)
        // For simplicity, use average male formula (can be improved with gender field)
        double bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        int dailyCalories = (int) Math.round(bmr * 1.375); // lightly active

        return new HealthAnalysis(bmi, category, color, idealMin, idealMax,
                kgToLose, title, message, advice, dailyCalories, emoji);
    }

    /**
     * Returns a short motivational tip based on BMI.
     */
    public String getDailyTip(User user) {
        if (user.getWeight() <= 0 || user.getHeight() <= 0) return null;
        double bmi = user.getBmi();
        if (bmi < 18.5) return "💡 Daily tip: Add a handful of nuts to each meal to increase your calories healthily.";
        if (bmi < 25.0) return "💡 Daily tip: Drink a large glass of water before each meal to maintain your ideal weight.";
        if (bmi < 30.0) return "💡 Daily tip: Replace sugary drinks with water or green tea — save 200 kcal/day.";
        return "💡 Daily tip: Start with a 15-minute walk after dinner. Every step counts!";
    }
}
