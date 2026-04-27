# Guide d'Internationalisation (i18n) - NutriLife

## Vue d'ensemble

Le système d'internationalisation de NutriLife permet de supporter plusieurs langues (actuellement anglais et français) avec changement dynamique de langue sans redémarrage de l'application.

## Architecture

### 1. LanguageManager
Gestionnaire centralisé des langues avec :
- Chargement automatique des ResourceBundle
- Persistance des préférences utilisateur
- Notification des changements de langue via JavaFX Properties
- API simple pour récupérer les traductions

### 2. Fichiers de traduction
- `messages.properties` - Fichier par défaut (anglais)
- `messages_en.properties` - Traductions anglaises
- `messages_fr.properties` - Traductions françaises

### 3. Contrôleurs mis à jour
Tous les contrôleurs principaux supportent maintenant l'i18n :
- `LoginController` - Écran de connexion avec sélecteur de langue
- `HomeController` - Tableau de bord utilisateur
- `AdminDashboardController` - Tableau de bord administrateur
- `LanguageSelectorController` - Sélecteur de langue modal

## Utilisation

### Dans un contrôleur

```java
public class MonController {
    private LanguageManager languageManager;
    
    @FXML
    public void initialize() {
        languageManager = LanguageManager.getInstance();
        updateTexts();
        
        // Écouter les changements de langue
        languageManager.languageProperty().addListener((obs, old, newLang) -> {
            updateTexts();
        });
    }
    
    private void updateTexts() {
        if (monLabel != null) {
            monLabel.setText(languageManager.getText("ma.cle.traduction"));
        }
        if (monBouton != null) {
            monBouton.setText(languageManager.getText("mon.bouton.texte"));
        }
    }
}
```

### Méthodes utilitaires

```java
// Texte simple
String texte = LanguageManager.t("ma.cle");

// Texte avec paramètres
String texte = LanguageManager.t("welcome.message", userName);

// Vérifier la langue actuelle
if (languageManager.isEnglish()) {
    // Logique spécifique à l'anglais
}
```

### Changer de langue

```java
// Par code de langue
LanguageManager.getInstance().setLanguage("fr");
LanguageManager.getInstance().setLanguage("en");

// Par Locale
LanguageManager.getInstance().setLanguage(Locale.FRENCH);
```

## Clés de traduction importantes

### Interface générale
- `app.name` - Nom de l'application
- `language` - "Langue"
- `english` - "English"
- `french` - "Français"
- `ok`, `cancel`, `save`, `delete`, etc. - Actions communes

### Connexion
- `login.title` - "Welcome Back" / "Bon Retour"
- `login.email` - "Email"
- `login.password` - "Password" / "Mot de passe"
- `login.signin` - "Sign In" / "Se connecter"

### Accueil
- `home.welcome` - "Welcome back, {0}!" / "Bon retour, {0} !"
- `home.nav.home` - "Home" / "Accueil"
- `account.menu` - "Account" / "Compte"

### Administration
- `admin.title` - "Admin Dashboard" / "Tableau de Bord Administrateur"
- `admin.users` - "User Management" / "Gestion des Utilisateurs"
- `admin.statistics` - "Statistics" / "Statistiques"

### Messages de bienvenue
- `welcome.morning` - "Good morning" / "Bonjour"
- `welcome.afternoon` - "Good afternoon" / "Bon après-midi"
- `welcome.evening` - "Good evening" / "Bonsoir"
- `welcome.motivation.1` à `welcome.motivation.8` - Messages motivationnels

## Ajout de nouvelles traductions

### 1. Ajouter la clé dans tous les fichiers
```properties
# messages_en.properties
nouvelle.cle=My new text

# messages_fr.properties
nouvelle.cle=Mon nouveau texte
```

### 2. Utiliser dans le contrôleur
```java
private void updateTexts() {
    monLabel.setText(languageManager.getText("nouvelle.cle"));
}
```

## Fonctionnalités implémentées

### ✅ Complété
- [x] Système LanguageManager avec persistance
- [x] Fichiers de traduction EN/FR complets
- [x] LoginController avec sélecteur de langue
- [x] HomeController avec menu compte multilingue
- [x] AdminDashboardController avec interface admin
- [x] WelcomeNotification avec messages motivationnels traduits
- [x] LanguageSelectorController modal
- [x] Bouton de langue dans la navbar de connexion

### 🔄 À compléter
- [ ] RegisterController
- [ ] ForgotPasswordController
- [ ] ProfileController
- [ ] BadgesController
- [ ] WeeklyChallengesController
- [ ] Tous les contrôleurs admin (UserList, Statistics, etc.)
- [ ] Messages d'erreur et de validation
- [ ] Toasts et notifications

## Bonnes pratiques

### 1. Nommage des clés
- Utiliser des points pour la hiérarchie : `login.error.email`
- Préfixer par la section : `admin.users.title`
- Être descriptif : `validation.password.length`

### 2. Paramètres
- Utiliser {0}, {1}, etc. pour les paramètres
- Exemple : `"Welcome back, {0}!"` avec `getText("welcome", userName)`

### 3. Mise à jour des contrôleurs
- Toujours écouter `languageProperty()` pour les changements
- Séparer la logique de mise à jour dans `updateTexts()`
- Mettre à jour tous les éléments UI (labels, boutons, menus, etc.)

### 4. Tests
- Tester le changement de langue en temps réel
- Vérifier que tous les textes sont traduits
- S'assurer que la préférence est sauvegardée

## Prochaines étapes

1. **Compléter tous les contrôleurs** - Ajouter l'i18n aux 30+ contrôleurs restants
2. **Mise à jour des FXML** - Certains textes statiques dans les FXML doivent être dynamiques
3. **Messages d'erreur** - Internationaliser tous les messages d'erreur et de validation
4. **Format de date/heure** - Adapter les formats selon la locale
5. **Tests automatisés** - Créer des tests pour vérifier les traductions

Le système est maintenant opérationnel et peut être étendu facilement pour supporter d'autres langues (espagnol, italien, etc.) en ajoutant simplement de nouveaux fichiers `messages_xx.properties`.