# 🎨 Aperçu visuel de l'intégration météo

## 📅 Vue du calendrier avec météo

### Avant l'intégration
```
┌─────────────────────────────────────────┐
│  Lundi 28 Avril 2026                    │
├─────────────────────────────────────────┤
│                                         │
│  🏃 Course matinale                     │
│  🕐 08:00                               │
│                                         │
│  🍽️ Déjeuner d'équipe                  │
│  🕐 12:30                               │
│                                         │
└─────────────────────────────────────────┘
```

### Après l'intégration ✨
```
┌─────────────────────────────────────────┐
│  Lundi 28 Avril 2026                    │
├─────────────────────────────────────────┤
│  ┌───────────────────────────────────┐  │
│  │ 🌤️  22.5°C    Météo du jour      │  │
│  │     Partiellement nuageux         │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ 🏃 Course matinale                │  │
│  │ 🕐 08:00                          │  │
│  │ 📍 Lac de Tunis  ☀️ 18.2°C       │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ 🍽️ Déjeuner d'équipe              │  │
│  │ 🕐 12:30                          │  │
│  │ 📍 Restaurant Le Golfe  🌤️ 24.1°C│  │
│  └───────────────────────────────────┘  │
│                                         │
└─────────────────────────────────────────┘
```

## 🌈 Codes couleur

### Carte météo du jour
- **Fond**: Dégradé bleu (#E3F2FD → #BBDEFB)
- **Bordure**: Bleu (#2196F3)
- **Température**: Bleu foncé (#1565C0), gras, 18px
- **Description**: Gris (#424242), 12px
- **Emoji**: 32px

### Badge météo sur événements
- **Fond**: Bleu clair (#E3F2FD)
- **Texte**: Bleu (#1565C0), gras, 11px
- **Bordure**: Arrondie (12px)
- **Padding**: 3px 8px

## 🌤️ Emojis météo utilisés

| Condition | Code | Emoji | Description |
|-----------|------|-------|-------------|
| Ciel dégagé | 01d/01n | ☀️ | Soleil |
| Peu nuageux | 02d/02n | ⛅ | Soleil et nuages |
| Nuageux | 03d/03n | ☁️ | Nuages |
| Très nuageux | 04d/04n | ☁️ | Beaucoup de nuages |
| Pluie | 09d/09n | 🌧️ | Pluie |
| Pluie légère | 10d/10n | 🌦️ | Soleil et pluie |
| Orage | 11d/11n | ⛈️ | Éclair |
| Neige | 13d/13n | ❄️ | Flocon |
| Brouillard | 50d/50n | 🌫️ | Brume |

## 📱 Responsive Design

### Petite fenêtre
```
┌─────────────────┐
│ 🌤️ 22.5°C      │
│ Nuageux         │
└─────────────────┘
```

### Grande fenêtre
```
┌─────────────────────────────────────┐
│ 🌤️  22.5°C              Météo du jour│
│     Partiellement nuageux            │
└─────────────────────────────────────┘
```

## 🎭 Exemples de conditions météo

### Journée ensoleillée
```
☀️ 28.5°C - ciel dégagé
```

### Journée pluvieuse
```
🌧️ 16.2°C - pluie modérée
```

### Journée orageuse
```
⛈️ 19.8°C - orage avec pluie
```

### Journée neigeuse
```
❄️ -2.1°C - chute de neige
```

### Journée brumeuse
```
🌫️ 12.4°C - brouillard
```

## 🎨 Palette de couleurs complète

### Bleus (Météo)
- `#E3F2FD` - Fond clair
- `#BBDEFB` - Fond dégradé
- `#2196F3` - Bordure
- `#1565C0` - Texte foncé

### Verts (Événements)
- `#E8F5E9` - Fond jour actuel
- `#C8E6C9` - Fond jour sélectionné
- `#1F4D3A` - Bordure/texte

### Oranges (Cartes événements)
- `#D6A46D` - Bordure événement

### Gris (Texte secondaire)
- `#424242` - Description
- `#666666` - Texte désactivé
- `#E0E0E0` - Bordures légères

## 📐 Dimensions et espacements

### Carte météo du jour
- **Padding**: 15px
- **Espacement interne**: 10px
- **Rayon bordure**: 10px
- **Hauteur**: Auto (min 60px)

### Badge météo événement
- **Padding**: 3px 8px
- **Rayon bordure**: 12px
- **Hauteur**: Auto
- **Largeur**: Auto

### Carte événement
- **Padding**: 12px
- **Espacement interne**: 8px
- **Rayon bordure**: 8px
- **Ombre**: 5px, rgba(0,0,0,0.1)

## 🔤 Typographie

### Titres
- **Police**: System
- **Poids**: Bold
- **Taille**: 14px
- **Couleur**: #1F4D3A

### Température
- **Police**: System
- **Poids**: Bold
- **Taille**: 18px (carte) / 11px (badge)
- **Couleur**: #1565C0

### Description
- **Police**: System
- **Poids**: Normal
- **Taille**: 12px
- **Couleur**: #424242

### Emojis
- **Taille**: 32px (carte) / 11px (badge)

## 🎬 Animations (futures)

Possibilités d'animations:
- Fade in lors du chargement de la météo
- Transition douce entre les températures
- Rotation de l'emoji lors du changement
- Pulse sur la carte météo du jour

## 📊 Hiérarchie visuelle

1. **Carte météo du jour** (plus visible)
   - Grande taille
   - Couleurs vives
   - En haut de la liste

2. **Événements avec météo** (secondaire)
   - Taille moyenne
   - Badge discret
   - Dans la liste

3. **Informations textuelles** (tertiaire)
   - Petite taille
   - Couleurs neutres

## 🌍 Exemples multi-langues

### Français
```
☀️ 22.5°C - ciel dégagé
```

### Anglais
```
☀️ 72.5°F - clear sky
```

### Arabe
```
☀️ 22.5°C - سماء صافية
```

## 💡 Conseils de design

1. **Contraste**: Assurez-vous que le texte est lisible sur tous les fonds
2. **Cohérence**: Utilisez les mêmes emojis partout
3. **Simplicité**: Ne surchargez pas avec trop d'informations
4. **Accessibilité**: Gardez une taille de police lisible (min 11px)
5. **Responsive**: Testez sur différentes tailles d'écran

---

**Design inspiré par Material Design et les applications météo modernes** 🎨
