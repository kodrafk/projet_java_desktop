import re, os

files = [
    "src/main/java/tn/esprit/projet/services/SubstitutionService.java",
    "src/main/java/tn/esprit/projet/utils/BadWordsFilter.java",
    "src/main/java/tn/esprit/projet/utils/GeminiService.java",
    "src/main/java/tn/esprit/projet/services/AIFoodAnalyzerService.java",
    "src/main/java/tn/esprit/projet/services/AIRecipeService.java",
]

for f in files:
    if os.path.exists(f):
        with open(f, "r", encoding="utf-8", errors="ignore") as fh:
            content = fh.read()
        content = re.sub(r'gsk_[A-Za-z0-9]+', 'YOUR_GROQ_API_KEY_HERE', content)
        content = re.sub(r'AIzaSy[A-Za-z0-9_-]+', 'YOUR_GEMINI_API_KEY_HERE', content)
        with open(f, "w", encoding="utf-8") as fh:
            fh.write(content)
        print(f"Fixed: {f}")
