# Configuration de l'intégration météo dans le calendrier

## 📋 Vue d'ensemble

L'intégration météo a été ajoutée au calendrier d'événements. Elle affiche:
- La météo du jour sélectionné dans le calendrier
- La météo pour chaque événement en fonction de son lieu

## 🔑 Configuration de l'API OpenWeatherMap

### Étape 1: Obtenir une clé API gratuite

1. Allez sur [OpenWeatherMap](https://openweathermap.org/api)
2. Créez un compte gratuit
3. Allez dans "API keys" dans votre profil
4. Copiez votre clé API

### Étape 2: Configurer via le fichier weather.properties

Ouvrez le fichier `weather.properties` à la racine du projet et modifiez:

```properties
# Votre clé API OpenWeatherMap
api.key=VOTRE_CLE_API_ICI

# Ville par défaut pour le calendrier
default.city=Tunis

# Langue des descriptions météo (fr, en, ar, etc.)
language=fr

# Unités de mesure (metric pour Celsius, imperial pour Fahrenheit)
units=metric
```

**Avantages de cette méthode:**
- Pas besoin de modifier le code source
- Configuration centralisée
- Facile à changer sans recompiler
- Peut être exclu du contrôle de version (.gitignore)

## 🎨 Fonctionnalités ajoutées

### 1. Service météo (`WeatherService.java`)
- Récupère les prévisions météo via l'API OpenWeatherMap
- Parse les données JSON
- Convertit les codes météo en emojis
- Gère les erreurs avec des valeurs par défaut

### 2. Affichage dans le calendrier
- **Carte météo du jour**: Affiche la météo pour la date sélectionnée
- **Météo par événement**: Chaque événement affiche la météo de son lieu
- **Emojis météo**: ☀️ ⛅ ☁️ 🌧️ ⛈️ ❄️ 🌫️

### 3. Informations affichées
- Température en °C
- Description (ensoleillé, nuageux, pluvieux, etc.)
- Icône emoji correspondante

## 🌍 Personnalisation

### Changer la ville par défaut

Modifiez le fichier `weather.properties`:

```properties
default.city=VotreVille
```

Exemples: Tunis, Paris, Sousse, Sfax, Monastir, etc.

### Changer la langue

```properties
language=fr  # Français
language=en  # Anglais
language=ar  # Arabe
```

### Changer les unités

```properties
units=metric    # Celsius (°C)
units=imperial  # Fahrenheit (°F)
```

### Ajouter d'autres informations météo

Le service peut être étendu pour afficher:
- Humidité
- Vitesse du vent
- Pression atmosphérique
- Lever/coucher du soleil

## 🔧 Dépannage

### Erreur "Données météo non disponibles"
- Vérifiez que votre clé API est correcte
- Vérifiez votre connexion internet
- Attendez quelques minutes (nouvelle clé API peut prendre du temps à s'activer)

### Météo incorrecte
- L'API fournit des prévisions sur 5 jours
- Pour les dates plus lointaines, des données par défaut sont affichées

### Problème de connexion
- Le timeout est configuré à 5 secondes
- En cas d'échec, une météo par défaut s'affiche

## 📝 Fichiers créés/modifiés

### Nouveaux fichiers:
1. **WeatherService.java** - Service de récupération météo avec configuration
2. **WeatherServiceTest.java** - Classe de test pour vérifier l'intégration
3. **weather.properties** - Fichier de configuration (clé API, ville, langue, unités)
4. **CONFIGURATION_METEO.md** - Ce fichier de documentation

### Fichiers modifiés:
1. **FrontCalendrierController.java** - Affichage météo dans le calendrier

### Fichiers inchangés:
1. **pom.xml** - Gson déjà présent pour le parsing JSON

## 🚀 Utilisation

1. Obtenez votre clé API sur OpenWeatherMap
2. Configurez le fichier `weather.properties` avec votre clé
3. (Optionnel) Personnalisez la ville, langue et unités
4. Lancez l'application
5. Ouvrez le calendrier
6. Sélectionnez une date pour voir la météo
7. Les événements affichent automatiquement la météo de leur lieu

## 🔒 Sécurité

**Important:** N'incluez pas votre clé API dans le contrôle de version!

Ajoutez à votre `.gitignore`:
```
weather.properties
```

Créez un fichier exemple pour les autres développeurs:
```
weather.properties.example
```

## 💡 Améliorations futures possibles

- Cache des données météo pour réduire les appels API
- Choix de la ville dans l'interface
- Alertes météo pour les événements extérieurs
- Graphiques de température sur la semaine
- Intégration avec d'autres API météo
