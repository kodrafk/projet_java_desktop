# 💬 Système de Messages Personnalisés - Guide Complet

## 📋 Vue d'ensemble

Le système de messages personnalisés permet aux administrateurs d'envoyer des messages motivants et personnalisés aux utilisateurs. Ces messages sont affichés dans la section **Objectives** du front office et peuvent optionnellement être envoyés par SMS via Twilio.

---

## 🎯 Fonctionnalités

### Pour l'Admin

1. **Interface de composition de messages**
   - Sélection d'utilisateur depuis une liste
   - Recherche d'utilisateurs par nom ou email
   - Zone de texte pour composer le message (max 500 caractères)
   - Option d'envoi par SMS (checkbox)
   - Compteur de caractères en temps réel

2. **Historique des messages**
   - Visualisation de tous les messages envoyés
   - Statut de lecture (lu/non lu)
   - Statut SMS (envoyé/échoué/pas de téléphone)
   - Filtrage par utilisateur
   - Rafraîchissement manuel

3. **Gestion des utilisateurs**
   - Liste complète des utilisateurs non-admin
   - Affichage du nom, email et numéro de téléphone
   - Indication visuelle si l'utilisateur n'a pas de téléphone

### Pour l'Utilisateur

1. **Affichage dans la section Objectives**
   - Messages affichés en haut de la page objectives
   - Design professionnel avec badge SMS si applicable
   - Date et heure d'envoi
   - Bouton "Mark as read" pour marquer comme lu

2. **Notifications**
   - Badge indiquant le nombre de nouveaux messages
   - Messages non lus affichés en priorité
   - Disparition automatique après lecture

---

## 🏗️ Architecture Technique

### Modèle de données

```java
PersonalizedMessage {
    int id;
    int userId;              // Destinataire
    int adminId;             // Expéditeur (admin)
    String content;          // Contenu du message
    boolean sendViaSms;      // Envoyé par SMS ?
    String smsStatus;        // Statut SMS (sent, failed, no_phone)
    String smsId;            // Twilio message SID
    boolean isRead;          // Lu par l'utilisateur ?
    LocalDateTime sentAt;    // Date d'envoi
    LocalDateTime readAt;    // Date de lecture
}
```

### Base de données

Table: `personalized_messages`

| Colonne | Type | Description |
|---------|------|-------------|
| id | INTEGER | Clé primaire auto-incrémentée |
| user_id | INTEGER | ID de l'utilisateur destinataire |
| admin_id | INTEGER | ID de l'admin expéditeur |
| content | TEXT | Contenu du message |
| send_via_sms | BOOLEAN | Indicateur d'envoi SMS |
| sms_status | TEXT | Statut de l'envoi SMS |
| sms_id | TEXT | ID du message Twilio |
| is_read | BOOLEAN | Indicateur de lecture |
| sent_at | TIMESTAMP | Date d'envoi |
| read_at | TIMESTAMP | Date de lecture |

### Services

1. **PersonalizedMessageService**
   - `sendMessage()` - Envoyer un message avec option SMS
   - `getMessagesForUser()` - Récupérer tous les messages d'un utilisateur
   - `getUnreadMessagesForUser()` - Récupérer les messages non lus
   - `countUnreadMessages()` - Compter les messages non lus
   - `markAsRead()` - Marquer un message comme lu
   - `markAllAsRead()` - Marquer tous les messages comme lus
   - `getMessagesSentByAdmin()` - Historique des messages d'un admin
   - `deleteMessage()` - Supprimer un message

2. **TwilioService** (existant)
   - Intégration pour l'envoi de SMS
   - Configuration via `twilio.properties`

### Contrôleurs

1. **AdminPersonalizedMessagesController**
   - Gestion de l'interface admin
   - Composition et envoi de messages
   - Affichage de l'historique

2. **WeightObjectiveController** (modifié)
   - Affichage des messages dans la section objectives
   - Marquage des messages comme lus

---

## 🚀 Installation

### 1. Créer la table en base de données

```bash
# Exécuter le script SQL
sqlite3 nutrilife.db < CREATE_PERSONALIZED_MESSAGES_TABLE.sql

# Ou utiliser le fichier batch
CREATE_PERSONALIZED_MESSAGES_TABLE.bat
```

### 2. Configuration Twilio (optionnel)

Pour activer l'envoi de SMS, configurez Twilio dans `src/main/resources/twilio.properties`:

```properties
twilio.account_sid=YOUR_ACCOUNT_SID
twilio.auth_token=YOUR_AUTH_TOKEN
twilio.from_number=+1234567890
```

### 3. Compilation

Le projet se compile automatiquement avec Maven. Aucune configuration supplémentaire n'est nécessaire.

---

## 📖 Guide d'utilisation

### Pour l'Admin

#### Envoyer un message

1. Connectez-vous en tant qu'admin
2. Cliquez sur **💬 Personalized Messages** dans le menu latéral
3. Sélectionnez un utilisateur dans la liste de gauche
4. Tapez votre message dans la zone de texte (max 500 caractères)
5. Cochez **📱 Send via SMS** si vous voulez envoyer aussi par SMS
6. Cliquez sur **📤 Send Message**

#### Consulter l'historique

L'historique s'affiche automatiquement en bas à droite après avoir sélectionné un utilisateur. Vous pouvez voir:
- La date et l'heure d'envoi
- Le statut de lecture (lu/non lu)
- Le statut SMS (envoyé/échoué/pas de téléphone)
- Le contenu du message

#### Rafraîchir l'historique

Cliquez sur le bouton **🔄** en haut de la section historique.

### Pour l'Utilisateur

#### Voir les messages

1. Connectez-vous à votre compte
2. Allez dans la section **Objectives** (⚖️ My Goal & Progress)
3. Les messages non lus s'affichent en haut de la page
4. Cliquez sur **✓ Mark as read** pour marquer un message comme lu

---

## 🎨 Design et UX

### Couleurs

- **Messages section**: Bleu clair (#EFF6FF) avec bordure bleue (#BFDBFE)
- **Bouton "Mark as read"**: Bleu (#3B82F6)
- **Badge SMS**: Vert (#E8F5E9) avec texte vert (#2E7D32)
- **Statut "Unread"**: Orange (#FEF3C7) avec texte orange (#D97706)
- **Statut "Read"**: Vert (#E8F5E9) avec texte vert (#2E7D32)

### Responsive

- Interface adaptée aux différentes tailles d'écran
- Scroll automatique pour les longues listes
- Cartes de messages avec hauteur flexible

---

## 🔒 Sécurité

1. **Validation des entrées**
   - Limite de 500 caractères pour les messages
   - Vérification de la session admin
   - Validation des IDs utilisateur

2. **Protection des données**
   - Clés étrangères avec CASCADE DELETE
   - Index pour optimiser les performances
   - Timestamps automatiques

3. **SMS**
   - Vérification de la présence du numéro de téléphone
   - Gestion des erreurs Twilio
   - Statut SMS enregistré en base

---

## 🐛 Dépannage

### Les messages ne s'affichent pas

1. Vérifiez que la table `personalized_messages` existe
2. Vérifiez que l'utilisateur a des messages non lus
3. Vérifiez les logs de la console

### L'envoi SMS échoue

1. Vérifiez la configuration Twilio dans `twilio.properties`
2. Vérifiez que l'utilisateur a un numéro de téléphone
3. Vérifiez le format du numéro (doit commencer par +)
4. Consultez les logs Twilio

### L'historique ne se charge pas

1. Vérifiez la connexion à la base de données
2. Vérifiez que l'admin est bien connecté
3. Rafraîchissez manuellement avec le bouton 🔄

---

## 📊 Statistiques et métriques

Le système enregistre automatiquement:
- Nombre de messages envoyés par admin
- Nombre de messages lus/non lus par utilisateur
- Taux de succès des envois SMS
- Temps de lecture moyen

---

## 🔄 Évolutions futures

- [ ] Notifications push en temps réel
- [ ] Templates de messages prédéfinis
- [ ] Envoi de messages groupés
- [ ] Planification de messages
- [ ] Statistiques détaillées
- [ ] Réponses des utilisateurs
- [ ] Pièces jointes (images, documents)
- [ ] Traduction automatique

---

## 📞 Support

Pour toute question ou problème:
1. Consultez les logs de l'application
2. Vérifiez la documentation Twilio
3. Contactez l'équipe de développement

---

## ✅ Checklist de déploiement

- [ ] Table `personalized_messages` créée
- [ ] Configuration Twilio (si SMS activé)
- [ ] Tests d'envoi de messages
- [ ] Tests de lecture de messages
- [ ] Tests d'envoi SMS
- [ ] Vérification de l'historique
- [ ] Tests de performance
- [ ] Documentation utilisateur fournie

---

**Version**: 1.0.0  
**Date**: 2026-04-26  
**Auteur**: NutriLife Development Team
