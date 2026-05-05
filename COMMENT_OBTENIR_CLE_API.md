# 🔑 Comment obtenir votre clé API OpenWeatherMap

## 📋 Étapes détaillées

### 1️⃣ Créer un compte (2 minutes)

1. Allez sur **https://openweathermap.org/api**
2. Cliquez sur **"Sign Up"** (en haut à droite)
3. Remplissez le formulaire:
   - Username (nom d'utilisateur)
   - Email
   - Password (mot de passe)
4. Acceptez les conditions
5. Cliquez sur **"Create Account"**

### 2️⃣ Confirmer votre email (1 minute)

1. Ouvrez votre boîte email
2. Cherchez l'email de OpenWeatherMap
3. Cliquez sur le lien de confirmation

### 3️⃣ Récupérer votre clé API (30 secondes)

1. Connectez-vous sur https://openweathermap.org
2. Cliquez sur votre nom (en haut à droite)
3. Sélectionnez **"My API keys"**
4. Vous verrez une clé par défaut déjà créée
5. **Copiez cette clé** (elle ressemble à: `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`)

### 4️⃣ Configurer dans votre application (30 secondes)

Ouvrez le fichier `weather.properties` et remplacez:

```properties
api.key=votre_cle_api_openweathermap
```

Par:

```properties
api.key=VOTRE_VRAIE_CLE_ICI
```

**Exemple** (avec une fausse clé):
```properties
api.key=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6
```

## ⚠️ Important

### Activation de la clé
- Votre clé peut prendre **10 minutes** à s'activer après création
- Si vous voyez "401 Unauthorized", attendez un peu

### Sécurité
- ❌ **NE PARTAGEZ JAMAIS** votre clé API publiquement
- ❌ **NE COMMITEZ PAS** le fichier `weather.properties` dans Git
- ✅ Le fichier est déjà dans `.gitignore` pour vous protéger

## 🆓 Plan gratuit

Le plan gratuit inclut:
- ✅ 1000 appels API par jour
- ✅ Prévisions sur 5 jours
- ✅ Données météo actuelles
- ✅ Pas de carte de crédit requise

C'est **largement suffisant** pour votre application!

## 🧪 Tester votre clé

### Test dans le navigateur

Remplacez `VOTRE_CLE` par votre vraie clé et ouvrez dans un navigateur:

```
https://api.openweathermap.org/data/2.5/weather?q=Tunis&appid=VOTRE_CLE&units=metric&lang=fr
```

**Résultat attendu**: Vous devriez voir du JSON avec les données météo

**Si erreur 401**: Votre clé n'est pas valide ou pas encore activée

### Test dans l'application

Exécutez le test automatique:

```bash
mvn compile exec:java -Dexec.mainClass="tn.esprit.projet.services.WeatherServiceTest"
```

## 📊 Quelle API utiliser?

### ✅ API Forecast (Recommandée - Gratuite)
- **URL**: `https://api.openweathermap.org/data/2.5/forecast`
- **Gratuit**: Oui, totalement
- **Prévisions**: 5 jours
- **Utilisée par**: Notre intégration actuelle

### ⚠️ API One Call 3.0 (Limitée)
- **URL**: `https://api.openweathermap.org/data/3.0/onecall`
- **Gratuit**: 1000 appels/jour puis payant
- **Prévisions**: 7 jours
- **Nécessite**: Carte de crédit

**Notre intégration utilise l'API Forecast (gratuite)** qui est parfaite pour votre calendrier!

## 🔄 Si vous voulez changer d'API

Si vous préférez utiliser One Call 3.0, vous devrez modifier `WeatherService.java`:

1. Changer l'URL de l'API
2. Adapter le parsing JSON (structure différente)
3. Gérer les coordonnées (lat/lon) au lieu des noms de ville

**Mais ce n'est pas nécessaire!** L'API Forecast actuelle fonctionne très bien.

## 📞 Support OpenWeatherMap

- **FAQ**: https://openweathermap.org/faq
- **Documentation**: https://openweathermap.org/api
- **Support**: https://openweathermap.org/support

## ✅ Checklist

- [ ] Compte créé sur OpenWeatherMap
- [ ] Email confirmé
- [ ] Clé API copiée
- [ ] Clé ajoutée dans `weather.properties`
- [ ] Attendu 10 minutes (si nouvelle clé)
- [ ] Testé dans le navigateur
- [ ] Testé avec WeatherServiceTest.java
- [ ] Application lancée avec succès

---

**Une fois configuré, votre calendrier affichera la météo automatiquement!** 🌤️
