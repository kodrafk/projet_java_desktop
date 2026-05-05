# 🌤️ Intégration Météo - Calendrier d'Événements

## 📋 Résumé

Cette intégration ajoute des informations météorologiques en temps réel à votre calendrier d'événements JavaFX. Les utilisateurs peuvent voir la météo pour chaque jour et pour chaque événement en fonction de son lieu.

## ✨ Fonctionnalités

- ☀️ **Météo du jour**: Affichage de la météo pour la date sélectionnée dans le calendrier
- 📍 **Météo par événement**: Chaque événement affiche la météo de son lieu
- 🌡️ **Température**: Affichage en Celsius ou Fahrenheit
- 🎨 **Emojis météo**: Représentation visuelle (☀️ ⛅ ☁️ 🌧️ ⛈️ ❄️ 🌫️)
- 🌍 **Multi-villes**: Support de toutes les villes du monde
- 🔧 **Configuration facile**: Fichier de configuration externe
- 🆓 **API gratuite**: Utilise OpenWeatherMap (1000 appels/jour gratuits)

## 🚀 Démarrage rapide

1. **Obtenez une clé API** sur https://openweathermap.org/api (gratuit)
2. **Configurez** le fichier `weather.properties` avec votre clé
3. **Lancez** l'application et ouvrez le calendrier

Voir `GUIDE_DEMARRAGE_METEO.md` pour les instructions détaillées.

## 📁 Structure des fichiers

```
projet/
├── src/main/java/tn/esprit/projet/
│   ├── services/
│   │   ├── WeatherService.java          # Service météo principal
│   │   └── WeatherServiceTest.java      # Tests
│   └── gui/
│       └── FrontCalendrierController.java # Contrôleur modifié
├── weather.properties                    # Configuration (à créer)
├── weather.properties.example            # Exemple de configuration
├── GUIDE_DEMARRAGE_METEO.md             # Guide rapide
├── CONFIGURATION_METEO.md               # Documentation complète
└── README_INTEGRATION_METEO.md          # Ce fichier
```

## 🔑 Configuration

### Fichier weather.properties

```properties
# Votre clé API OpenWeatherMap
api.key=VOTRE_CLE_ICI

# Ville par défaut
default.city=Tunis

# Langue (fr, en, ar, etc.)
language=fr

# Unités (metric=Celsius, imperial=Fahrenheit)
units=metric
```

## 💻 Utilisation dans le code

### Récupérer la météo

```java
WeatherService weatherService = new WeatherService();
LocalDate date = LocalDate.now();
WeatherInfo weather = weatherService.getWeatherForDate("Tunis", date);

System.out.println(weather.getFormattedTemp());  // "22.5°C"
System.out.println(weather.getDescription());    // "ciel dégagé"
System.out.println(weather.getWeatherEmoji());   // "☀️"
```

### Vérifier la configuration

```java
if (weatherService.isConfigured()) {
    // API configurée
} else {
    // Clé API manquante
}
```

## 🎨 Interface utilisateur

### Carte météo du jour
Affiche une carte colorée avec:
- Emoji météo (grande taille)
- Température
- Description
- Fond dégradé bleu

### Badge météo sur les événements
Chaque événement affiche:
- Nom de l'événement
- Heure
- Lieu avec badge météo (emoji + température)

## 🔧 Personnalisation

### Changer le style de la carte météo

Dans `FrontCalendrierController.java`, méthode `afficherMeteoJour()`:

```java
meteoBox.setStyle("-fx-background-color: linear-gradient(to right, #E3F2FD, #BBDEFB); ...");
```

### Ajouter plus d'informations météo

Le service peut être étendu pour afficher:
- Humidité
- Vitesse du vent
- Pression
- Lever/coucher du soleil

Modifiez la classe `WeatherInfo` dans `WeatherService.java`.

### Changer les emojis

Dans `WeatherService.java`, méthode `getWeatherEmoji()`:

```java
case "01": return "☀️";  // Changez l'emoji ici
```

## 📊 Limites de l'API gratuite

- **1000 appels/jour** (largement suffisant pour une application personnelle)
- **Prévisions sur 5 jours** (au-delà, données par défaut)
- **Mise à jour toutes les 3 heures**

## 🔒 Sécurité

⚠️ **Important**: Ne commitez jamais votre clé API!

Le fichier `weather.properties` est déjà dans `.gitignore`.

Pour partager le projet:
1. Utilisez `weather.properties.example`
2. Documentez comment obtenir une clé API
3. Chaque développeur crée son propre `weather.properties`

## 🧪 Tests

### Test automatique

```bash
mvn compile exec:java -Dexec.mainClass="tn.esprit.projet.services.WeatherServiceTest"
```

### Test manuel

1. Lancez l'application
2. Ouvrez le calendrier
3. Sélectionnez une date
4. Vérifiez que la météo s'affiche

## 🐛 Dépannage

| Problème | Solution |
|----------|----------|
| "Données météo non disponibles" | Vérifiez votre clé API et connexion internet |
| "401 Unauthorized" | Clé API incorrecte ou pas activée |
| "404 Not Found" | Nom de ville incorrect |
| Timeout | Connexion internet lente ou pare-feu |

## 📚 Documentation

- **Guide rapide**: `GUIDE_DEMARRAGE_METEO.md`
- **Configuration détaillée**: `CONFIGURATION_METEO.md`
- **API OpenWeatherMap**: https://openweathermap.org/api
- **Documentation API**: https://openweathermap.org/forecast5

## 🔄 Améliorations futures

- [ ] Cache des données météo
- [ ] Sélection de ville dans l'interface
- [ ] Alertes météo pour événements extérieurs
- [ ] Graphiques de température
- [ ] Support d'autres API météo
- [ ] Mode hors ligne avec dernières données
- [ ] Notifications push pour changements météo

## 📝 Changelog

### Version 1.0 (Avril 2026)
- ✅ Intégration API OpenWeatherMap
- ✅ Affichage météo dans le calendrier
- ✅ Météo par événement
- ✅ Configuration externe
- ✅ Support multi-langues
- ✅ Emojis météo
- ✅ Gestion des erreurs

## 👥 Contribution

Pour contribuer:
1. Créez une branche pour votre fonctionnalité
2. Testez avec `WeatherServiceTest`
3. Documentez vos changements
4. Créez une pull request

## 📄 Licence

Ce code utilise l'API OpenWeatherMap qui a ses propres conditions d'utilisation.
Consultez: https://openweathermap.org/terms

## 🎉 Remerciements

- OpenWeatherMap pour l'API gratuite
- Gson pour le parsing JSON
- JavaFX pour l'interface utilisateur

---

**Développé avec ☕ et 🌤️**
