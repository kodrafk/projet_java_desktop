# 🥗 NutriLife - Système Complet de Gestion Utilisateurs

## 📋 Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Installation Rapide](#installation-rapide)
3. [Fonctionnalités](#fonctionnalités)
4. [Architecture](#architecture)
5. [Documentation](#documentation)
6. [Support](#support)

---

## 🎯 Vue d'ensemble

NutriLife est une application de gestion nutritionnelle avec un backoffice admin complet permettant :

- **Gestion des utilisateurs** - CRUD complet avec recherche et filtres
- **Messages personnalisés** - Communication directe avec SMS optionnel
- **Système d'alertes** - Notifications prioritaires avec 4 niveaux d'urgence
- **Détection d'anomalies IA** - Surveillance automatique des métriques de santé
- **Système de badges** - Gamification et motivation
- **Face ID** - Authentification biométrique

---

## ⚡ Installation Rapide

### Prérequis
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- XAMPP/WAMP (recommandé)

### Installation en 3 étapes

#### 1. Créer la base de données
```bash
mysql -u root -p
CREATE DATABASE nutrilife;
USE nutrilife;
```

#### 2. Créer les tables
```bash
cd projetJAV
CREATE_USER_ALERTS_TABLE.bat
```

#### 3. Lancer l'application
```bash
mvn javafx:run
```

**Identifiants par défaut** :
- Email : `admin@nutrilife.com`
- Password : `admin123`

---

## 🎨 Fonctionnalités

### 👥 Gestion des Utilisateurs

**Backoffice Admin** :
- ✅ Liste complète avec recherche en temps réel
- ✅ Tri par colonnes (ID, Email, Nom, Rôle, Statut, Date)
- ✅ Actions multiples :
  - View (Voir profil)
  - Edit (Modifier)
  - Delete (Supprimer)
  - Toggle (Activer/Désactiver)
  - Badges (Gérer badges)
  - Messages (Envoyer message)
  - Progress (Voir progression)
  - Gallery (Gérer photos)
  - FaceID (Gérer Face ID)

**Statistiques** :
- Total utilisateurs
- Utilisateurs actifs/inactifs
- Nombre d'admins

### 💬 Messages Personnalisés

**Fonctionnalités** :
- ✅ Envoi de messages personnalisés admin → utilisateur
- ✅ Option SMS via Twilio
- ✅ Compteur de caractères (max 500)
- ✅ Historique avec statuts (Read/Unread, SMS sent/failed)
- ✅ Affichage dans le front office (section Objectives)

**Interface** :
```
┌──────────┬──────────────────────┬─────────────┐
│ Users    │ Compose Message      │ History     │
│ (search) │ (textarea + SMS)     │ (timeline)  │
└──────────┴──────────────────────┴─────────────┘
```

### 🔔 Système d'Alertes (NOUVEAU)

**Types d'alertes** :
- **INFO** 🔵 - Information générale
- **WARNING** 🟠 - Avertissement
- **URGENT** 🔴 - Critique/Urgent
- **SUCCESS** 🟢 - Félicitations

**Catégories** :
- **HEALTH** ❤️ - Santé/Métriques
- **GOAL** 🎯 - Objectifs
- **REMINDER** ⏰ - Rappels
- **SYSTEM** ⚙️ - Système

**Options avancées** :
- ✅ Expiration automatique (date/heure)
- ✅ Boutons d'action personnalisés
- ✅ Indicateurs de lecture
- ✅ Système de fermeture (dismiss)
- ✅ Compteur d'alertes non lues

**Interface** :
```
┌──────────┬──────────────────────┬─────────────┐
│ Users    │ Create Alert         │ History     │
│ (+ count)│ (form + options)     │ (cards)     │
└──────────┴──────────────────────┴─────────────┘
```

**Widget Front Office** :
```
┌─────────────────────────────┐
│ 🔔 Alerts        [3] [🔄]   │
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │ 🚨 ❤️  26/04 15:30  ●  │ │
│ │ Urgent Health Alert     │ │
│ │ Your BMI indicates...   │ │
│ │ [Mark read] [Action] [✕]│ │
│ └─────────────────────────┘ │
└─────────────────────────────┘
```

---

## 🏗️ Architecture

### Structure du Projet

```
projetJAV/
├── src/main/java/tn/esprit/projet/
│   ├── models/
│   │   ├── User.java
│   │   ├── PersonalizedMessage.java
│   │   └── UserAlert.java (NOUVEAU)
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── PersonalizedMessageRepository.java
│   │   └── UserAlertRepository.java (NOUVEAU)
│   ├── services/
│   │   ├── PersonalizedMessageService.java
│   │   └── UserAlertService.java (NOUVEAU)
│   └── gui/
│       ├── AdminDashboardController.java
│       ├── AdminUserListController.java
│       ├── AdminPersonalizedMessagesController.java
│       ├── AdminAlertsController.java (NOUVEAU)
│       └── UserAlertsWidget.java (NOUVEAU)
└── src/main/resources/fxml/
    ├── admin_dashboard.fxml
    ├── admin_user_list.fxml
    ├── admin_personalized_messages.fxml
    └── admin_alerts.fxml (NOUVEAU)
```

### Base de Données

#### Tables Principales

**users**
```sql
id, email, password, roles
first_name, last_name, phone
is_active, created_at
weight, height, birthday
```

**personalized_messages**
```sql
id, user_id, admin_id
content, send_via_sms, sms_status
is_read, sent_at, read_at
```

**user_alerts** (NOUVEAU)
```sql
id, user_id, admin_id
title, message
type, category
is_read, is_dismissed
created_at, expires_at, read_at
action_url, action_label
```

---

## 📖 Documentation

### Guides Disponibles

| Document | Description | Pages |
|----------|-------------|-------|
| **USER_MANAGEMENT_AND_ALERTS_GUIDE.md** | Guide complet du système | 15 |
| **QUICK_START_USER_ALERTS.md** | Démarrage rapide | 2 |
| **IMPLEMENTATION_SUMMARY.md** | Résumé technique | 8 |
| **README_COMPLETE_SYSTEM.md** | Ce fichier | 5 |

### Guides Existants

- `ADMIN_BACKOFFICE_GUIDE.md` - Guide backoffice général
- `PERSONALIZED_MESSAGES_GUIDE.md` - Messages personnalisés
- `ANOMALY_DETECTION_GUIDE.md` - Détection d'anomalies IA
- `BADGES_SYSTEM.md` - Système de badges

---

## 🚀 Utilisation

### Scénario 1 : Admin envoie une alerte urgente

```bash
1. Connexion admin
2. Menu → 🔔 User Alerts
3. Sélectionner utilisateur
4. Remplir :
   - Title: "Consultation Recommandée"
   - Message: "Votre IMC indique un risque..."
   - Type: URGENT
   - Category: HEALTH
5. Send Alert
```

### Scénario 2 : Admin envoie un message avec SMS

```bash
1. Menu → 💬 Personalized Messages
2. Sélectionner utilisateur
3. Écrire message
4. ✓ Cocher "Send via SMS"
5. Send Message
```

### Scénario 3 : Utilisateur voit ses alertes

```bash
1. Connexion utilisateur
2. Widget d'alertes affiché automatiquement
3. Badge [3] = 3 alertes non lues
4. Cliquer sur alerte pour détails
5. Actions : Mark as read / Action / Dismiss
```

---

## 🔧 Configuration

### Twilio (SMS)

Pour activer l'envoi de SMS :

1. Créer un compte Twilio
2. Obtenir Account SID et Auth Token
3. Configurer dans `TwilioService.java` :

```java
private static final String ACCOUNT_SID = "your_account_sid";
private static final String AUTH_TOKEN = "your_auth_token";
private static final String FROM_NUMBER = "+1234567890";
```

### Base de Données

Fichier : `src/main/resources/application.properties`

```properties
db.url=jdbc:mysql://localhost:3306/nutrilife
db.username=root
db.password=your_password
```

---

## 🧪 Tests

### Test Automatique

```bash
cd projetJAV
TEST_ALERTS_SYSTEM.bat
```

### Tests Manuels

#### Test 1 : Créer une alerte
- [ ] Connexion admin OK
- [ ] Menu "User Alerts" visible
- [ ] Sélection utilisateur OK
- [ ] Formulaire rempli
- [ ] Alerte envoyée
- [ ] Historique mis à jour

#### Test 2 : Voir une alerte (utilisateur)
- [ ] Connexion utilisateur OK
- [ ] Widget d'alertes visible
- [ ] Badge de notification affiché
- [ ] Alerte affichée avec bon style
- [ ] Actions fonctionnent

#### Test 3 : Envoyer un message
- [ ] Menu "Personalized Messages" OK
- [ ] Sélection utilisateur OK
- [ ] Message rédigé
- [ ] SMS optionnel testé
- [ ] Message envoyé
- [ ] Historique OK

---

## 📊 Statistiques

### Métriques Disponibles

**Dashboard Admin** :
- Total utilisateurs
- Utilisateurs actifs
- Utilisateurs inactifs
- Nombre d'admins

**Messages** :
- Messages envoyés
- Messages lus/non lus
- SMS envoyés/échoués
- Taux de lecture

**Alertes** :
- Alertes actives
- Alertes non lues
- Alertes expirées
- Alertes par type/catégorie

---

## 🐛 Dépannage

### Problème : Table user_alerts n'existe pas

**Solution** :
```bash
cd projetJAV
CREATE_USER_ALERTS_TABLE.bat
```

### Problème : Menu "User Alerts" invisible

**Solution** :
1. Vérifier connexion admin
2. Vérifier `admin_dashboard.fxml` contient le bouton
3. Redémarrer l'application

### Problème : Alertes non affichées

**Solution** :
1. Vérifier table `user_alerts` existe
2. Vérifier données dans la table
3. Appeler `alertsWidget.refresh()`
4. Vérifier logs console

### Problème : SMS non envoyés

**Solution** :
1. Vérifier configuration Twilio
2. Vérifier numéro de téléphone utilisateur
3. Vérifier logs `TwilioService`

---

## 🔐 Sécurité

### Contrôles Implémentés

- ✅ Authentification requise (admin/user)
- ✅ Validation des données (longueur, format)
- ✅ PreparedStatement (protection SQL injection)
- ✅ Filtrage des utilisateurs (non-admin uniquement)
- ✅ Suppression en cascade (FK constraints)
- ✅ Hachage des mots de passe (BCrypt)

### Recommandations

- [ ] Chiffrer les numéros de téléphone
- [ ] Implémenter rate limiting
- [ ] Ajouter logs d'audit
- [ ] Configurer HTTPS en production
- [ ] Sauvegardes régulières de la base

---

## 🎯 Roadmap

### Version 1.1 (Court terme)
- [ ] Notifications push en temps réel
- [ ] Templates de messages prédéfinis
- [ ] Envoi groupé d'alertes
- [ ] Statistiques détaillées

### Version 1.2 (Moyen terme)
- [ ] Planification d'envoi différé
- [ ] Alertes récurrentes
- [ ] Filtres avancés (âge, BMI, etc.)
- [ ] Export de données

### Version 2.0 (Long terme)
- [ ] Intelligence artificielle pour suggestions
- [ ] Alertes automatiques basées sur métriques
- [ ] API REST pour applications mobiles
- [ ] Application mobile native

---

## 👥 Équipe

**Développement** : Équipe NutriLife  
**Version** : 1.0.0  
**Date** : 26 Avril 2026  
**Statut** : ✅ Production Ready

---

## 📞 Support

### Documentation
1. Consulter les guides dans `/projetJAV/`
2. Lire les commentaires dans le code
3. Vérifier les logs console

### Contact
- Email : support@nutrilife.com
- GitHub : github.com/nutrilife/app
- Documentation : docs.nutrilife.com

---

## 📄 Licence

Copyright © 2026 NutriLife. Tous droits réservés.

---

## ✅ Checklist de Déploiement

### Prérequis
- [ ] Java 17+ installé
- [ ] Maven 3.8+ installé
- [ ] MySQL 8.0+ installé et démarré
- [ ] Base `nutrilife` créée

### Installation
- [ ] Tables créées (users, personalized_messages, user_alerts)
- [ ] Compte admin créé
- [ ] Données de test insérées (optionnel)
- [ ] Configuration Twilio (optionnel)

### Tests
- [ ] Application compile sans erreurs
- [ ] Application lance correctement
- [ ] Connexion admin fonctionne
- [ ] Menu "User Alerts" visible
- [ ] Envoi d'alerte fonctionne
- [ ] Widget utilisateur affiche alertes
- [ ] Messages personnalisés fonctionnent

### Production
- [ ] Base de données sauvegardée
- [ ] Logs configurés
- [ ] Monitoring activé
- [ ] Documentation à jour
- [ ] Équipe formée

---

**🎉 Félicitations ! Votre système est prêt à l'emploi !**

Pour commencer, lancez :
```bash
mvn javafx:run
```

Et connectez-vous avec les identifiants admin par défaut.
