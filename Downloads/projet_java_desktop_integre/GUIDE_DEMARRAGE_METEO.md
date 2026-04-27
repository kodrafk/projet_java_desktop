# 🌤️ Guide de démarrage rapide - Intégration Météo

## ⚡ Installation en 3 étapes

### 1️⃣ Obtenez votre clé API (2 minutes)

1. Allez sur https://openweathermap.org/api
2. Cliquez sur "Sign Up" (gratuit)
3. Confirmez votre email
4. Allez dans votre profil → "API keys"
5. Copiez la clé qui commence par quelque chose comme `a1b2c3d4e5f6...`

### 2️⃣ Configurez votre application (30 secondes)

1. Ouvrez le fichier `weather.properties` à la racine du projet
2. Remplacez `votre_cle_api_openweathermap` par votre vraie clé
3. (Optionnel) Changez la ville si vous n'êtes pas à Tunis

```properties
api.key=a1b2c3d4e5f6g7h8i9j0  # ← Collez votre clé ici
default.city=Tunis             # ← Changez si nécessaire
```

### 3️⃣ Testez (1 minute)

Exécutez la classe de test:
```bash
mvn compile exec:java -Dexec.mainClass="tn.esprit.projet.services.WeatherServiceTest"
```

Ou lancez directement `WeatherServiceTest.java` depuis votre IDE.

## ✅ Vérification

Si tout fonctionne, vous devriez voir:
```
=== Test du Service Météo ===

📍 Test 1: Météo à Tunis aujourd'hui
   Résultat: ☀️ 22.5°C - ciel dégagé
   ...
```

Si vous voyez "Données météo non disponibles", vérifiez:
- ✓ Votre clé API est correcte
- ✓ Vous avez une connexion internet
- ✓ Votre clé API est activée (peut prendre 10 minutes)

## 🎨 Résultat dans l'application

Une fois configuré, dans le calendrier vous verrez:

### Carte météo du jour
```
🌤️  22.5°C                    Météo du jour
    Partiellement nuageux
```

### Météo sur chaque événement
```
┌─────────────────────────────────┐
│ 🏃 Course matinale              │
│ 🕐 08:00                        │
│ 📍 Lac de Tunis  ☀️ 18.2°C     │
└─────────────────────────────────┘
```

## 🌍 Villes supportées

Vous pouvez utiliser n'importe quelle ville:
- **Tunisie**: Tunis, Sousse, Sfax, Monastir, Bizerte, Nabeul, Kairouan, Gabès, etc.
- **France**: Paris, Lyon, Marseille, Toulouse, Nice, etc.
- **International**: London, New York, Tokyo, Dubai, etc.

## 🔧 Personnalisation rapide

### Changer la langue en arabe
```properties
language=ar
```

### Utiliser Fahrenheit au lieu de Celsius
```properties
units=imperial
```

### Changer la ville par défaut
```properties
default.city=Sousse
```

## ❓ Problèmes courants

### "401 Unauthorized"
→ Votre clé API est incorrecte ou pas encore activée

### "404 Not Found"
→ Le nom de la ville est incorrect (essayez en anglais: "Tunis" pas "تونس")

### Timeout / Pas de connexion
→ Vérifiez votre connexion internet et votre pare-feu

## 📞 Support

- Documentation complète: `CONFIGURATION_METEO.md`
- API OpenWeatherMap: https://openweathermap.org/api
- Limite gratuite: 1000 appels/jour (largement suffisant)

## 🎉 C'est tout!

Votre calendrier affiche maintenant la météo automatiquement! 🌤️
