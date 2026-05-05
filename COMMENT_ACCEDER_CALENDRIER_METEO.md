# 📅 Comment accéder au calendrier avec la météo

## 🎯 Navigation dans l'application

### Méthode 1: Depuis le menu principal

1. **Lancez votre application**
   ```bash
   mvn javafx:run
   ```

2. **Connectez-vous** avec votre compte

3. **Cliquez sur "Events"** dans le menu de navigation

4. **Cliquez sur le bouton "📅 Calendrier avec Météo"** (en haut à droite)

5. **Voilà!** Vous êtes sur le calendrier avec la météo! 🌤️

### Méthode 2: Navigation visuelle

```
┌─────────────────────────────────────────────────────────────┐
│  Nutri Coach Pro                                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  [Home] [About] [My Kitchen] [Events] [Blog] [Wellness]    │
│                                  ↑                          │
│                            Cliquez ici                      │
│                                                             │
└─────────────────────────────────────────────────────────────┘

                        ↓

┌─────────────────────────────────────────────────────────────┐
│  Événements                                                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  [🔍 Rechercher]  [0 événements]  [📅 Calendrier avec Météo]│
│                                              ↑              │
│                                        Cliquez ici          │
│                                                             │
│  [Carte événement 1]  [Carte événement 2]  [Carte...]      │
│                                                             │
└─────────────────────────────────────────────────────────────┘

                        ↓

┌─────────────────────────────────────────────────────────────┐
│  📅 Calendrier des Événements                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────────────┐  ┌──────────────────┐    │
│  │  [◀] Avril 2026 [▶] [Aujourd'hui]│  │ Événements du jour│    │
│  │                              │  │                  │    │
│  │  Lun Mar Mer Jeu Ven Sam Dim │  │ 🌤️ 22.5°C       │    │
│  │   1   2   3   4   5   6   7  │  │ Partiellement    │    │
│  │   8   9  10  11  12  13  14  │  │ nuageux          │    │
│  │  15  16  17  18  19  20  21  │  │                  │    │
│  │  22  23  24  25  26  27  28  │  │ 🏃 Course        │    │
│  │  29  30                      │  │ 🕐 08:00         │    │
│  │                              │  │ 📍 Lac ☀️ 18°C  │    │
│  └──────────────────────────────┘  └──────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 🌤️ Fonctionnalités du calendrier

### 1. Vue mensuelle
- Naviguez entre les mois avec les flèches ◀ ▶
- Retournez à aujourd'hui avec le bouton "📍 Aujourd'hui"
- Les jours avec événements affichent un badge (ex: "2 📅")

### 2. Météo du jour
- Sélectionnez une date dans le calendrier
- La météo s'affiche automatiquement en haut de la liste des événements
- Format: 🌤️ 22.5°C - Description

### 3. Événements avec météo
- Chaque événement affiche la météo de son lieu
- Format: 📍 Lieu ☀️ 18.2°C
- Mise à jour automatique selon la date

## 🔑 Configuration requise

⚠️ **Important**: Pour que la météo s'affiche, vous devez avoir configuré votre clé API!

1. Ouvrez le fichier `weather.properties`
2. Ajoutez votre clé API OpenWeatherMap
3. Sauvegardez et relancez l'application

Voir **CONFIGURATION_RAPIDE.txt** pour les instructions détaillées.

## 🎨 Aperçu visuel

### Calendrier sans météo (avant configuration)
```
┌────────────────────────────────────┐
│ Événements du jour                 │
├────────────────────────────────────┤
│ Lundi 28 Avril 2026                │
│                                    │
│ 🏃 Course matinale                 │
│ 🕐 08:00                           │
│ 📍 Lac de Tunis                    │
│                                    │
│ 🍽️ Déjeuner d'équipe               │
│ 🕐 12:30                           │
│ 📍 Restaurant Le Golfe             │
└────────────────────────────────────┘
```

### Calendrier avec météo (après configuration) ✨
```
┌────────────────────────────────────┐
│ Événements du jour                 │
├────────────────────────────────────┤
│ Lundi 28 Avril 2026                │
│                                    │
│ ┌──────────────────────────────┐   │
│ │ 🌤️  22.5°C   Météo du jour  │   │
│ │     Partiellement nuageux    │   │
│ └──────────────────────────────┘   │
│                                    │
│ 🏃 Course matinale                 │
│ 🕐 08:00                           │
│ 📍 Lac de Tunis  ☀️ 18.2°C        │
│                                    │
│ 🍽️ Déjeuner d'équipe               │
│ 🕐 12:30                           │
│ 📍 Restaurant Le Golfe  🌤️ 24.1°C │
└────────────────────────────────────┘
```

## 🐛 Problèmes courants

### Le bouton "Calendrier avec Météo" n'apparaît pas
**Solution**: 
- Recompilez l'application: `mvn clean compile`
- Relancez: `mvn javafx:run`

### La météo ne s'affiche pas
**Solution**:
1. Vérifiez que vous avez configuré votre clé API dans `weather.properties`
2. Vérifiez votre connexion internet
3. Attendez 10 minutes si votre clé API est nouvelle

### "Données météo non disponibles"
**Solution**:
- Testez votre clé API dans le navigateur
- Voir **CONFIGURATION_RAPIDE.txt** section "Problèmes courants"

## 📱 Raccourcis clavier (futurs)

Dans une version future, vous pourrez:
- `Ctrl + K` - Ouvrir le calendrier
- `Flèches ← →` - Naviguer entre les mois
- `Aujourd'hui` - Retour à la date actuelle

## 💡 Astuces

### Astuce 1: Planification d'événements
Utilisez le calendrier avec météo pour:
- Planifier vos événements sportifs selon la météo
- Éviter les jours de pluie pour les activités extérieures
- Choisir les meilleurs jours pour vos courses

### Astuce 2: Vérification rapide
Avant de participer à un événement:
1. Ouvrez le calendrier
2. Sélectionnez la date de l'événement
3. Vérifiez la météo prévue
4. Préparez-vous en conséquence!

### Astuce 3: Personnalisation
Vous pouvez personnaliser:
- La ville par défaut dans `weather.properties`
- La langue des descriptions météo
- Les unités (Celsius/Fahrenheit)

## 🎯 Checklist d'utilisation

Avant d'utiliser le calendrier avec météo:

- [ ] Application lancée
- [ ] Connecté avec votre compte
- [ ] Clé API configurée dans `weather.properties`
- [ ] Connexion internet active
- [ ] Navigué vers "Events" → "Calendrier avec Météo"

## 📞 Besoin d'aide?

- **Configuration API**: Voir `CONFIGURATION_RAPIDE.txt`
- **Documentation complète**: Voir `LISEZ_MOI_METEO.md`
- **Guide de démarrage**: Voir `GUIDE_DEMARRAGE_METEO.md`

---

**Profitez de votre calendrier avec météo en temps réel!** 🌤️📅
