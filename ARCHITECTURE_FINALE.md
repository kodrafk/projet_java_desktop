# ✅ ARCHITECTURE FINALE - Backoffice Admin

## 🎯 Structure Complète et Professionnelle

Voici l'architecture finale de votre backoffice admin avec toutes les fonctionnalités intégrées.

---

## 📋 Menu Principal (Sidebar)

```
🥗 NutriLife
├── 📊 Dashboard
├── 👥 User Management          ← Gestion complète des utilisateurs
├── 💬 Personalized Messages    ← Interface de messages globale
├── 🔔 User Alerts              ← Système d'alertes
├── 📈 Statistics               ← Statistiques
├── 🤖 AI Anomaly Detection     ← Détection d'anomalies IA
└── 🚪 Logout
```

---

## 1️⃣ USER MANAGEMENT (Gestion des Utilisateurs)

### Interface Principale
- **Tableau complet** avec tous les utilisateurs
- **Colonnes** : ID, Email, Full Name, Role, Status, Created At, Actions
- **Recherche** en temps réel
- **Tri** par colonne
- **Compteur** d'utilisateurs

### Boutons d'Action (9 boutons par utilisateur)

| Bouton | Couleur | Fonction |
|--------|---------|----------|
| **View** | Bleu clair | Voir les détails de l'utilisateur |
| **Badges** | Violet | Gérer les badges de l'utilisateur |
| **Message** | Orange | Envoyer un message personnalisé à cet utilisateur |
| **Progress** | Vert | Voir la progression poids/objectifs |
| **Gallery** | Rose | Gérer la galerie de photos |
| **FaceID** | Noir | Gérer la reconnaissance faciale |
| **Edit** | Bleu | Modifier l'utilisateur |
| **Toggle** | Violet clair | Activer/Désactiver l'utilisateur |
| **Delete** | Rouge | Supprimer l'utilisateur |

### Bouton "+ Add New User"
- Créer un nouvel utilisateur
- Formulaire complet

### Bouton "A" (Admin Profile)
- Accès rapide au profil de l'admin connecté

---

## 2️⃣ PERSONALIZED MESSAGES (Menu Séparé)

### Interface Globale en 3 Sections

#### Section Gauche : Liste des Utilisateurs
- Tous les utilisateurs (admins + users)
- Badge "👑 Admin" pour les administrateurs
- Recherche en temps réel
- Compteur d'utilisateurs
- Sélection par clic

#### Section Centre : Composition du Message
- Zone de texte (max 500 caractères)
- Compteur de caractères en temps réel
- Option "Send via SMS"
- Bouton "Send Message" (vert)
- Bouton "Clear"

#### Section Droite : Historique des Messages
- Tous les messages envoyés à l'utilisateur sélectionné
- Badges : Read/Unread
- Badges SMS : Sent/Failed/No phone
- Date et heure d'envoi
- Bouton refresh

---

## 3️⃣ USER ALERTS (Système d'Alertes)

### Interface en 3 Sections

#### Section Gauche : Liste des Utilisateurs
- Tous les utilisateurs
- Badge "👑 Admin"
- Compteur d'alertes non lues par utilisateur

#### Section Centre : Création d'Alerte
- Titre de l'alerte
- Message (max 500 caractères)
- Type : INFO, WARNING, URGENT, SUCCESS
- Catégorie : HEALTH, GOAL, REMINDER, SYSTEM
- Option d'expiration (date + heure)
- Option de bouton d'action (URL + label)

#### Section Droite : Historique des Alertes
- Toutes les alertes envoyées
- Icônes par type et catégorie
- Badges : Read/Unread, Dismissed, Expired

---

## 🔄 Flux de Travail

### Scénario 1 : Envoyer un Message à un Utilisateur Spécifique

**Depuis User Management** :
1. Ouvrir "User Management"
2. Trouver l'utilisateur dans le tableau
3. Cliquer sur le bouton "Message" (orange)
4. ✅ Interface de messages s'ouvre pour cet utilisateur
5. Écrire et envoyer le message

### Scénario 2 : Envoyer des Messages à Plusieurs Utilisateurs

**Depuis Personalized Messages** :
1. Ouvrir "Personalized Messages" dans le menu
2. Sélectionner un utilisateur dans la liste
3. Écrire et envoyer le message
4. Sélectionner un autre utilisateur
5. Répéter

### Scénario 3 : Gérer un Utilisateur Complètement

**Depuis User Management** :
1. Trouver l'utilisateur
2. **View** → Voir tous les détails
3. **Edit** → Modifier les informations
4. **Badges** → Gérer les badges
5. **Progress** → Voir la progression
6. **Gallery** → Gérer les photos
7. **Message** → Envoyer un message
8. **FaceID** → Gérer la reconnaissance faciale

---

## 📊 Base de Données

### Tables Principales

```sql
-- Utilisateurs
user (24 utilisateurs actuellement)

-- Messages et Alertes
personalized_message
user_alert

-- Progression
weight_objective
weight_log
progress_photo

-- Badges
badge
user_badge

-- Reconnaissance Faciale
face_embeddings
face_verification_attempts

-- Anomalies IA
health_anomalies
health_alerts
user_health_metrics
anomaly_detection_history
```

---

## 🎨 Design et UX

### Couleurs
- **Vert principal** : #2E7D5A (boutons actifs)
- **Vert foncé** : #0F2820 (sidebar)
- **Orange** : #F59E0B (bouton Message)
- **Violet** : #7C3AED (bouton Badges, Alerts)
- **Rouge** : #DC2626 (bouton Delete, Logout)
- **Bleu** : #3B82F6 (bouton Edit)

### Badges
- **👑 Admin** : Rouge (#DC2626)
- **✓ Read** : Vert (#2E7D32)
- **● Unread** : Orange (#D97706)
- **📱 SMS sent** : Vert (#2E7D32)
- **📱 SMS failed** : Rouge (#DC2626)

---

## 🔍 Logs de Debug

### User Management
```
[UserRepository] Executing sorted query: SELECT * FROM user ORDER BY `id` DESC
[UserRepository] Sorted query returned: 24 users
[DEBUG UserManagement] Loading users with sort: id DESC
[DEBUG UserManagement] Loaded 24 users from database
[DEBUG UserManagement] User: test@test.com | Role: ROLE_ADMIN | Active: true
[DEBUG UserManagement] Table updated with 24 users
```

### Personalized Messages
```
[UserRepository] Executing query: SELECT * FROM user ORDER BY id DESC
[UserRepository] Total users loaded: 24
[DEBUG] Total users from DB: 24
[DEBUG] User: test@test.com | Role: ROLE_ADMIN | FullName: Admin User
[DEBUG] Filtered users (excluding current admin): 23
```

---

## 📁 Structure des Fichiers

```
src/main/java/tn/esprit/projet/
├── gui/
│   ├── AdminDashboardController.java
│   ├── AdminUserListController.java          ← User Management
│   ├── AdminUserShowController.java
│   ├── AdminUserEditController.java
│   ├── AdminUserNewController.java
│   ├── AdminUserBadgesController.java
│   ├── AdminUserMessagesController.java      ← Messages par utilisateur
│   ├── AdminUserProgressController.java
│   ├── AdminUserGalleryController.java
│   ├── AdminFaceIdController.java
│   ├── AdminPersonalizedMessagesController.java  ← Messages globaux
│   ├── AdminAlertsController.java
│   └── AdminAnomalyDetectionController.java
├── models/
│   ├── User.java
│   ├── PersonalizedMessage.java
│   ├── UserAlert.java
│   └── ...
├── repository/
│   ├── UserRepository.java
│   ├── PersonalizedMessageRepository.java
│   ├── UserAlertRepository.java
│   └── ...
└── services/
    ├── PersonalizedMessageService.java
    ├── UserAlertService.java
    ├── AnomalyDetectionService.java
    └── ...

src/main/resources/fxml/
├── admin_dashboard.fxml
├── admin_user_list.fxml
├── admin_user_show.fxml
├── admin_user_edit.fxml
├── admin_user_new.fxml
├── admin_user_badges.fxml
├── admin_user_messages.fxml
├── admin_user_progress.fxml
├── admin_user_gallery.fxml
├── admin_face_id.fxml
├── admin_personalized_messages.fxml
└── admin_alerts.fxml
```

---

## ✅ Fonctionnalités Complètes

### User Management
- ✅ Liste complète de tous les utilisateurs (24 actuellement)
- ✅ Recherche et tri
- ✅ 9 boutons d'action par utilisateur
- ✅ Création de nouveaux utilisateurs
- ✅ Édition et suppression
- ✅ Activation/Désactivation
- ✅ Gestion des badges
- ✅ Envoi de messages individuels
- ✅ Suivi de progression
- ✅ Gestion de galerie
- ✅ Reconnaissance faciale

### Personalized Messages (Menu)
- ✅ Interface globale de messages
- ✅ Liste de tous les utilisateurs
- ✅ Badge "👑 Admin"
- ✅ Envoi de messages à n'importe quel utilisateur
- ✅ Historique complet
- ✅ Option SMS
- ✅ Compteur de caractères

### User Alerts
- ✅ 4 types d'alertes
- ✅ 4 catégories
- ✅ Expiration optionnelle
- ✅ Boutons d'action optionnels
- ✅ Historique complet

### AI Anomaly Detection
- ✅ Détection automatique de 6 types d'anomalies
- ✅ Génération d'alertes automatiques
- ✅ Tableau des anomalies
- ✅ Résolution et suivi

---

## 🎯 Résultat Final

**Backoffice admin complet et professionnel** :
- ✅ User Management avec 9 actions par utilisateur
- ✅ Personalized Messages dans le menu
- ✅ Bouton "Message" dans User Management
- ✅ User Alerts avec options avancées
- ✅ AI Anomaly Detection
- ✅ Chargement direct depuis la base (24 utilisateurs)
- ✅ Logs détaillés pour debug
- ✅ Interface moderne et intuitive
- ✅ Compilation réussie : BUILD SUCCESS

---

**Date** : 2024-04-27  
**Statut** : ✅ Architecture complète et fonctionnelle  
**Prêt pour production** : ✅ OUI

**Commande** : `mvn javafx:run`
