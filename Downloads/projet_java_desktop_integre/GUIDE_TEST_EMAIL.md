# 📧 Guide de Test et Dépannage Email

## ✅ Améliorations Apportées

### 1. Validation Robuste
- ✅ Vérification du nom participant (non vide)
- ✅ Vérification de l'email (format valide avec @)
- ✅ Vérification de l'événement (non null)

### 2. Gestion des Erreurs Améliorée
- ✅ Try-catch autour de la météo (continue sans météo si erreur)
- ✅ Try-catch autour des sponsors (continue sans sponsors si erreur)
- ✅ Gestion des valeurs null (nom, lieu, coach)
- ✅ Messages d'erreur détaillés dans la console

### 3. Types d'Erreurs Capturées
```java
- AuthenticationFailedException → Problème de connexion SMTP
- MessagingException → Problème d'envoi d'email
- Exception générale → Autres erreurs
```

---

## 🔍 Comment Diagnostiquer le Problème

### Étape 1 : Vérifier les Logs Console
Après avoir cliqué sur "Participer", regardez la console IntelliJ pour voir les messages :

**Messages possibles :**
```
✅ Email moderne envoyé avec succès à : email@example.com
   → L'email a été envoyé correctement

❌ Nom du participant vide
   → Le nom n'a pas été saisi

❌ Email invalide : xxx
   → L'email ne contient pas de @

❌ Erreur d'authentification SMTP
   → Problème avec EMAIL_FROM ou EMAIL_PASSWORD

❌ Erreur de messagerie
   → Problème de connexion SMTP ou de configuration

⚠️ Erreur lors de la récupération de la météo
   → La météo n'a pas pu être récupérée (mais l'email continue)

⚠️ Erreur lors de la récupération des sponsors
   → Les sponsors n'ont pas pu être récupérés (mais l'email continue)
```

---

## 🛠️ Solutions aux Problèmes Courants

### Problème 1 : Authentification SMTP Échoue
**Symptôme :** `❌ Erreur d'authentification SMTP`

**Solutions :**
1. Vérifier que `EMAIL_FROM` et `EMAIL_PASSWORD` sont corrects dans `EmailServiceModerne.java`
2. Vérifier que le mot de passe d'application Gmail est valide
3. Vérifier que l'accès aux applications moins sécurisées est activé

**Fichier à modifier :** `src/main/java/tn/esprit/projet/services/EmailServiceModerne.java`
```java
private static final String EMAIL_FROM = "votre-email@gmail.com";
private static final String EMAIL_PASSWORD = "votre-mot-de-passe-app";
```

---

### Problème 2 : Connexion SMTP Timeout
**Symptôme :** `❌ Erreur de messagerie : Connection timed out`

**Solutions :**
1. Vérifier la connexion Internet
2. Vérifier que le port 587 n'est pas bloqué par un firewall
3. Essayer avec un autre réseau (pas de proxy d'entreprise)

---

### Problème 3 : Email Invalide
**Symptôme :** `❌ Email invalide : xxx`

**Solution :**
- S'assurer que l'email saisi contient un @
- Vérifier que le dialogue d'inscription fonctionne correctement

---

### Problème 4 : Météo ou Sponsors Échouent
**Symptôme :** `⚠️ Erreur lors de la récupération de la météo/sponsors`

**Impact :** L'email sera quand même envoyé, mais sans météo/sponsors

**Solution :**
- Vérifier que les services `WeatherService` et `SponsorService` fonctionnent
- Vérifier la connexion à l'API météo
- Vérifier la connexion à la base de données pour les sponsors

---

## 🧪 Test Manuel

### Test 1 : Email Simple
1. Lancer l'application
2. Cliquer sur "Participer" sur un événement
3. Remplir le formulaire :
   - Nom : `Test User`
   - Email : `votre-email@gmail.com`
   - Téléphone : `12345678`
4. Cliquer sur "Confirmer l'inscription"
5. Vérifier la console pour les messages
6. Vérifier votre boîte mail

### Test 2 : Email avec Météo
1. Créer un événement **extérieur** (avec mots-clés : outdoor, plein air, etc.)
2. S'inscrire à cet événement
3. L'email devrait contenir la section météo

### Test 3 : Email avec Sponsors
1. Créer un événement avec des sponsors associés
2. S'inscrire à cet événement
3. L'email devrait contenir les logos des sponsors

---

## 📝 Configuration Recommandée

### Gmail App Password
Pour générer un mot de passe d'application Gmail :

1. Aller sur https://myaccount.google.com/security
2. Activer la validation en 2 étapes
3. Aller dans "Mots de passe des applications"
4. Générer un nouveau mot de passe pour "Mail"
5. Copier le mot de passe (16 caractères)
6. Le mettre dans `EMAIL_PASSWORD`

### Configuration SMTP Actuelle
```java
SMTP_HOST = "smtp.gmail.com"
SMTP_PORT = "587"
TLS = activé
Authentification = requise
Timeout = 10 secondes
```

---

## 🎯 Checklist de Vérification

Avant de tester l'envoi d'email, vérifier :

- [ ] `EMAIL_FROM` est configuré avec un email Gmail valide
- [ ] `EMAIL_PASSWORD` contient le mot de passe d'application (16 caractères)
- [ ] La connexion Internet fonctionne
- [ ] Le port 587 n'est pas bloqué
- [ ] Le formulaire d'inscription se remplit correctement
- [ ] Les logs console sont visibles dans IntelliJ

---

## 🚀 Si Tout Fonctionne

Vous devriez voir dans la console :
```
📧 Tentative d'envoi d'email à : email@example.com
✅ Email moderne envoyé avec succès à : email@example.com
```

Et recevoir un email avec :
- ✅ Header vert avec icône 🏋️
- ✅ Badge "Votre place est réservée"
- ✅ Détails de l'événement
- ✅ Météo (si événement extérieur)
- ✅ Informations participant
- ✅ Logos sponsors (si disponibles)
- ✅ Bouton Google Maps
- ✅ Conseils pratiques
- ✅ Footer professionnel

---

## 📞 Support

Si le problème persiste après avoir suivi ce guide :

1. Copier les logs de la console
2. Vérifier le message d'erreur exact
3. Vérifier la configuration SMTP
4. Tester avec un autre email

**Fichiers concernés :**
- `src/main/java/tn/esprit/projet/services/EmailServiceModerne.java`
- `src/main/java/tn/esprit/projet/gui/FrontEvenementController.java`
- `src/main/java/tn/esprit/projet/gui/InscriptionDialog.java`
