# Test des Fonctionnalités d'Internationalisation

## 🚀 Comment lancer l'application

1. **Double-cliquez sur `RUN_WITH_I18N.bat`**
2. Attendez la compilation Maven
3. L'application se lance automatiquement

## ✅ Fonctionnalités à tester

### 1. Écran de connexion
- [ ] **Bouton de langue** en haut à droite (🇫🇷 FR / 🇺🇸 EN)
- [ ] **Changement instantané** de tous les textes
- [ ] **Persistance** de la langue choisie (relancer l'app)

### 2. Connexion utilisateur normal
**Identifiants de test :**
- Email : `user@test.com`
- Mot de passe : `password123`

**À vérifier :**
- [ ] **Message de bienvenue** traduit (10 secondes d'affichage)
- [ ] **Menu Account** avec option "Langue" / "Language"
- [ ] **Navigation** traduite (Accueil/Home, etc.)
- [ ] **Messages motivationnels** aléatoires en FR/EN

### 3. Connexion administrateur
**Identifiants admin :**
- Email : `admin@nutrilife.com`
- Mot de passe : `admin123`

**À vérifier :**
- [ ] **Interface admin** complètement traduite
- [ ] **Sidebar** (Tableau de bord / Dashboard, etc.)
- [ ] **Statistiques** avec labels traduits
- [ ] **Message de bienvenue admin** (6.5 secondes)

### 4. Changement de langue dynamique
- [ ] **Depuis login** : bouton 🇫🇷 FR / 🇺🇸 EN
- [ ] **Depuis menu compte** : option "Langue"
- [ ] **Mise à jour instantanée** de toute l'interface
- [ ] **Sauvegarde automatique** de la préférence

## 🎯 Points d'attention

### Messages de bienvenue
- **Utilisateur normal** : 10 secondes d'affichage
- **Administrateur** : 6.5 secondes d'affichage
- **8 messages motivationnels** différents en rotation
- **Salutation selon l'heure** (Bonjour/Good morning, etc.)

### Interface traduite
- **300+ clés de traduction** disponibles
- **Textes dynamiques** (pas de texte fixe dans les FXML)
- **Formats adaptés** selon la langue

### Persistance
- **Préférence sauvegardée** dans les préférences système
- **Langue restaurée** au redémarrage
- **Pas de perte de session**

## 🐛 Problèmes potentiels

### Si l'application ne se lance pas :
1. Vérifier Java 17 installé : `java -version`
2. Vérifier Maven installé : `mvn -version`
3. Lancer manuellement : `mvn clean compile javafx:run`

### Si les traductions ne s'affichent pas :
1. Vérifier les fichiers `.properties` dans `src/main/resources/`
2. Regarder la console pour les erreurs de ResourceBundle
3. Tester avec les clés de base : `app.name`, `language`

### Si le changement de langue ne fonctionne pas :
1. Vérifier que `LanguageManager` est bien initialisé
2. Contrôler les listeners sur `languageProperty()`
3. Tester la méthode `updateTexts()` dans chaque contrôleur

## 📊 Résultats attendus

### ✅ Succès si :
- Changement de langue instantané et fluide
- Tous les textes traduits correctement
- Messages de bienvenue animés et traduits
- Préférence sauvegardée entre les sessions
- Interface professionnelle et cohérente

### ❌ Échec si :
- Textes restent en anglais après changement
- Erreurs dans la console sur ResourceBundle
- Messages de bienvenue ne s'affichent pas
- Langue non sauvegardée au redémarrage
- Interface cassée ou incohérente

## 🔧 Dépannage rapide

```bash
# Nettoyer et recompiler
mvn clean compile

# Lancer avec debug
mvn javafx:run -X

# Vérifier les ressources
ls src/main/resources/messages*.properties
```

## 📝 Rapport de test

Après les tests, noter :
- [ ] Fonctionnalités qui marchent parfaitement
- [ ] Problèmes rencontrés et solutions
- [ ] Suggestions d'amélioration
- [ ] Performance générale du système i18n