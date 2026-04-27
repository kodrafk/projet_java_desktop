# 📋 Guide Complet : Gestion des Utilisateurs et Système d'Alertes

## 🎯 Vue d'ensemble

Ce système offre une gestion complète des utilisateurs dans le backoffice admin avec trois fonctionnalités principales :

1. **Liste des utilisateurs** - Gestion complète des comptes utilisateurs
2. **Messages personnalisés** - Communication directe admin → utilisateur (avec SMS optionnel)
3. **Système d'alertes** - Notifications prioritaires avec différents niveaux d'urgence

---

## 🏗️ Architecture

### Modèles de données

#### User (existant)
```java
- id, email, password, role
- firstName, lastName, phone
- isActive, createdAt
- weight, height, birthday
```

#### PersonalizedMessage (existant)
```java
- id, userId, adminId
- content, sendViaSms, smsStatus
- isRead, sentAt, readAt
```

#### UserAlert (nouveau)
```java
- id, userId, adminId
- title, message
- type (INFO, WARNING, URGENT, SUCCESS)
- category (HEALTH, GOAL, REMINDER, SYSTEM)
- isRead, isDismissed
- createdAt, expiresAt, readAt
- actionUrl, actionLabel
```

### Services

#### UserAlertService
- `createAlert()` - Créer une alerte simple
- `createAlertWithExpiry()` - Alerte avec date d'expiration
- `createAlertWithAction()` - Alerte avec bouton d'action
- `getActiveAlertsForUser()` - Récupérer les alertes actives
- `markAsRead()` - Marquer comme lu
- `dismissAlert()` - Fermer une alerte

#### PersonalizedMessageService (existant)
- `sendMessage()` - Envoyer un message (avec SMS optionnel)
- `getMessagesForUser()` - Récupérer les messages
- `markAsRead()` - Marquer comme lu

---

## 🎨 Interface Backoffice Admin

### 1. Dashboard Admin

**Accès** : Menu latéral gauche

**Sections disponibles** :
- 📊 Dashboard - Vue d'ensemble des statistiques
- 👥 User Management - Gestion des utilisateurs
- 💬 Personalized Messages - Messages personnalisés
- 🔔 User Alerts - Système d'alertes
- 📈 Statistics - Statistiques détaillées
- 🤖 AI Anomaly Detection - Détection d'anomalies

### 2. User Management (Liste des utilisateurs)

**Fonctionnalités** :
- ✅ Recherche en temps réel (nom, email)
- ✅ Tri par colonnes (ID, Email, Nom, Rôle, Statut, Date)
- ✅ Affichage du statut (Active/Inactive)
- ✅ Actions par utilisateur :
  - **View** - Voir le profil complet
  - **Badges** - Gérer les badges
  - **Message** - Envoyer un message personnalisé
  - **Progress** - Voir la progression
  - **Gallery** - Gérer la galerie photos
  - **FaceID** - Gérer Face ID
  - **Edit** - Modifier le profil
  - **Toggle** - Activer/Désactiver
  - **Delete** - Supprimer (avec confirmation)

**Bouton spécial** :
- 👤 **My Profile** - L'admin peut modifier son propre profil

### 3. Personalized Messages

**Interface** :
- **Panneau gauche** : Liste des utilisateurs avec recherche
- **Panneau central** : Formulaire d'envoi de message
- **Panneau droit** : Historique des messages envoyés

**Fonctionnalités** :
- ✅ Sélection d'un utilisateur
- ✅ Rédaction de message (max 500 caractères)
- ✅ Option d'envoi par SMS (via Twilio)
- ✅ Compteur de caractères en temps réel
- ✅ Historique avec statuts :
  - ✓ Read / ● Unread
  - 📱 SMS sent / SMS failed / No phone
- ✅ Filtrage automatique (utilisateurs non-admin uniquement)

### 4. User Alerts (Nouveau)

**Interface** :
- **Panneau gauche** : Liste des utilisateurs avec compteur d'alertes non lues
- **Panneau central** : Formulaire de création d'alerte
- **Panneau droit** : Historique des alertes envoyées

**Formulaire de création** :
```
📝 Alert Title
📄 Alert Message (max 500 caractères)
🎨 Alert Type : INFO | WARNING | URGENT | SUCCESS
📂 Category : HEALTH | GOAL | REMINDER | SYSTEM
⏰ Set expiration date (optionnel)
🔗 Add action button (optionnel)
```

**Types d'alertes** :
- **INFO** (bleu) - Information générale
- **WARNING** (orange) - Avertissement
- **URGENT** (rouge) - Urgent/Critique
- **SUCCESS** (vert) - Félicitations/Succès

**Catégories** :
- **HEALTH** ❤️ - Santé/Métriques
- **GOAL** 🎯 - Objectifs
- **REMINDER** ⏰ - Rappels
- **SYSTEM** ⚙️ - Système

**Options avancées** :
- **Expiration** : Définir une date/heure d'expiration
- **Action** : Ajouter un bouton avec URL et label personnalisé

---

## 💻 Interface Front Office (Utilisateur)

### Widget d'alertes (UserAlertsWidget)

**Intégration** :
```java
UserAlertsWidget alertsWidget = new UserAlertsWidget();
alertsWidget.setUser(currentUser, stage);
// Ajouter à votre layout
```

**Affichage** :
- 🔔 Badge avec nombre d'alertes non lues
- Cartes colorées selon le type d'alerte
- Icônes visuelles (type + catégorie)
- Date et heure de création
- Indicateur "non lu" (●)

**Actions utilisateur** :
- **Mark as read** - Marquer comme lu
- **Action button** - Bouton d'action personnalisé (si défini)
- **✕ Dismiss** - Fermer l'alerte
- **🔄 Refresh** - Actualiser la liste

**Styles visuels** :
- INFO : Fond bleu clair, bordure bleue
- WARNING : Fond jaune clair, bordure orange
- URGENT : Fond rouge clair, bordure rouge
- SUCCESS : Fond vert clair, bordure verte

---

## 🔧 Installation et Configuration

### 1. Créer la table user_alerts

**Windows** :
```bash
cd projetJAV
CREATE_USER_ALERTS_TABLE.bat
```

**Ou manuellement** :
```bash
mysql -u root -p nutrilife < CREATE_USER_ALERTS_TABLE.sql
```

### 2. Vérifier la table personalized_messages

La table devrait déjà exister. Si ce n'est pas le cas :
```bash
CREATE_PERSONALIZED_MESSAGES_TABLE.bat
```

### 3. Compiler le projet

```bash
mvn clean compile
```

### 4. Lancer l'application

```bash
mvn javafx:run
```

---

## 📊 Utilisation

### Scénario 1 : Envoyer un message personnalisé

1. Connexion en tant qu'admin
2. Menu : **💬 Personalized Messages**
3. Sélectionner un utilisateur dans la liste
4. Rédiger le message (max 500 caractères)
5. Cocher "Send via SMS" si souhaité
6. Cliquer sur **Send Message**
7. L'utilisateur verra le message dans sa section "Objectives"

### Scénario 2 : Créer une alerte urgente

1. Connexion en tant qu'admin
2. Menu : **🔔 User Alerts**
3. Sélectionner un utilisateur
4. Remplir le formulaire :
   - Title : "Urgent: Health Check Required"
   - Message : "Your BMI indicates a health risk. Please consult your doctor."
   - Type : **URGENT**
   - Category : **HEALTH**
5. Cliquer sur **Send Alert**
6. L'utilisateur verra l'alerte en rouge dans son interface

### Scénario 3 : Créer un rappel avec expiration

1. Menu : **🔔 User Alerts**
2. Sélectionner un utilisateur
3. Remplir le formulaire :
   - Title : "Weekly Weigh-In Reminder"
   - Message : "Don't forget to log your weight this week!"
   - Type : **INFO**
   - Category : **REMINDER**
4. Cocher **Set expiration date**
5. Sélectionner la date (ex: dans 7 jours)
6. Entrer l'heure (ex: 23:59)
7. Cliquer sur **Send Alert**
8. L'alerte disparaîtra automatiquement après expiration

### Scénario 4 : Alerte avec action

1. Menu : **🔔 User Alerts**
2. Sélectionner un utilisateur
3. Remplir le formulaire :
   - Title : "Complete Your Profile"
   - Message : "Add your health metrics to get personalized recommendations."
   - Type : **INFO**
   - Category : **SYSTEM**
4. Cocher **Add action button**
5. Action URL : "/profile"
6. Button label : "Go to Profile"
7. Cliquer sur **Send Alert**
8. L'utilisateur verra un bouton "Go to Profile" dans l'alerte

---

## 🎯 Cas d'usage recommandés

### Messages personnalisés
- ✅ Communication longue et détaillée
- ✅ Félicitations personnalisées
- ✅ Conseils nutritionnels spécifiques
- ✅ Suivi individuel
- ✅ Messages nécessitant SMS

### Alertes système
- ✅ Notifications urgentes
- ✅ Rappels avec expiration
- ✅ Alertes de santé critiques
- ✅ Notifications de succès/badges
- ✅ Appels à l'action rapides

---

## 🔐 Sécurité

### Contrôles d'accès
- ✅ Seuls les admins peuvent envoyer messages/alertes
- ✅ Les utilisateurs ne voient que leurs propres messages/alertes
- ✅ Validation des données côté serveur
- ✅ Protection contre les injections SQL (PreparedStatement)

### Données sensibles
- ✅ Numéros de téléphone chiffrés (recommandé)
- ✅ Messages stockés de manière sécurisée
- ✅ Suppression en cascade (si utilisateur supprimé)

---

## 📈 Statistiques et Monitoring

### Métriques disponibles
- Nombre total d'alertes envoyées
- Taux de lecture des messages
- Alertes actives par utilisateur
- Messages SMS envoyés/échoués
- Alertes expirées (nettoyage automatique)

### Nettoyage automatique
```java
UserAlertService alertService = new UserAlertService();
int deleted = alertService.cleanupExpiredAlerts();
System.out.println("Deleted " + deleted + " expired alerts");
```

---

## 🐛 Dépannage

### Problème : Table user_alerts n'existe pas
**Solution** : Exécuter `CREATE_USER_ALERTS_TABLE.bat`

### Problème : SMS non envoyés
**Solution** : Vérifier la configuration Twilio dans `TwilioService`

### Problème : Alertes non affichées
**Solution** : 
1. Vérifier que l'utilisateur est connecté
2. Appeler `alertsWidget.refresh()`
3. Vérifier les logs console

### Problème : Caractères spéciaux mal affichés
**Solution** : Vérifier l'encodage UTF-8 de la base de données

---

## 🚀 Améliorations futures

### Court terme
- [ ] Notifications push en temps réel
- [ ] Templates de messages prédéfinis
- [ ] Envoi groupé d'alertes
- [ ] Statistiques détaillées par admin

### Moyen terme
- [ ] Planification d'envoi différé
- [ ] Alertes récurrentes
- [ ] Système de priorités
- [ ] Filtres avancés (âge, BMI, etc.)

### Long terme
- [ ] Intelligence artificielle pour suggestions
- [ ] Alertes automatiques basées sur métriques
- [ ] Intégration avec calendrier
- [ ] API REST pour applications mobiles

---

## 📞 Support

Pour toute question ou problème :
1. Consulter ce guide
2. Vérifier les logs console
3. Tester avec les données de démonstration
4. Contacter l'équipe de développement

---

## ✅ Checklist de déploiement

- [ ] Base de données configurée
- [ ] Table `user_alerts` créée
- [ ] Table `personalized_messages` créée
- [ ] Twilio configuré (optionnel)
- [ ] Compte admin créé
- [ ] Données de test insérées
- [ ] Application compilée
- [ ] Tests effectués
- [ ] Documentation lue

---

**Version** : 1.0.0  
**Date** : 26 Avril 2026  
**Auteur** : Équipe NutriLife
