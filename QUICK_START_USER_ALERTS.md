# 🚀 Quick Start : Système d'Alertes Utilisateur

## ⚡ Installation Rapide

### 1. Créer la table (1 minute)
```bash
cd projetJAV
CREATE_USER_ALERTS_TABLE.bat
```
Entrez votre mot de passe MySQL root quand demandé.

### 2. Lancer l'application
```bash
mvn javafx:run
```

---

## 🎯 Utilisation Immédiate

### En tant qu'Admin

#### Envoyer une alerte
1. Connexion → Menu **🔔 User Alerts**
2. Sélectionner un utilisateur
3. Remplir :
   - **Title** : "Bienvenue !"
   - **Message** : "Nous sommes ravis de vous accueillir"
   - **Type** : INFO
   - **Category** : SYSTEM
4. Cliquer **Send Alert**

#### Envoyer un message personnalisé
1. Menu **💬 Personalized Messages**
2. Sélectionner un utilisateur
3. Écrire le message
4. Cocher "Send via SMS" si souhaité
5. Cliquer **Send Message**

### En tant qu'Utilisateur

Les alertes s'affichent automatiquement dans votre interface avec :
- 🔔 Badge de notification
- Cartes colorées selon l'urgence
- Boutons d'action

---

## 🎨 Types d'Alertes

| Type | Couleur | Usage |
|------|---------|-------|
| **INFO** | 🔵 Bleu | Information générale |
| **WARNING** | 🟠 Orange | Avertissement |
| **URGENT** | 🔴 Rouge | Critique/Urgent |
| **SUCCESS** | 🟢 Vert | Félicitations |

---

## 📂 Catégories

| Catégorie | Icône | Usage |
|-----------|-------|-------|
| **HEALTH** | ❤️ | Santé/Métriques |
| **GOAL** | 🎯 | Objectifs |
| **REMINDER** | ⏰ | Rappels |
| **SYSTEM** | ⚙️ | Système |

---

## 💡 Exemples Rapides

### Alerte de santé urgente
```
Title: "Consultation Recommandée"
Message: "Votre IMC indique un risque. Consultez un médecin."
Type: URGENT
Category: HEALTH
```

### Rappel avec expiration
```
Title: "Pesée Hebdomadaire"
Message: "N'oubliez pas de vous peser cette semaine !"
Type: INFO
Category: REMINDER
Expiration: Dans 7 jours
```

### Alerte avec action
```
Title: "Complétez Votre Profil"
Message: "Ajoutez vos métriques pour des recommandations personnalisées."
Type: INFO
Category: SYSTEM
Action URL: /profile
Action Label: "Aller au Profil"
```

---

## 🔧 Intégration dans le Code

### Ajouter le widget d'alertes
```java
// Dans votre contrôleur
UserAlertsWidget alertsWidget = new UserAlertsWidget();
alertsWidget.setUser(Session.getCurrentUser(), stage);

// Ajouter au layout
yourVBox.getChildren().add(alertsWidget);
```

### Créer une alerte programmatiquement
```java
UserAlertService alertService = new UserAlertService();

// Alerte simple
alertService.createAlert(
    adminId, 
    userId, 
    "Titre", 
    "Message", 
    AlertType.INFO, 
    AlertCategory.SYSTEM
);

// Alerte avec expiration
alertService.createAlertWithExpiry(
    adminId, 
    userId, 
    "Titre", 
    "Message", 
    AlertType.WARNING, 
    AlertCategory.REMINDER,
    LocalDateTime.now().plusDays(7)
);

// Alerte avec action
alertService.createAlertWithAction(
    adminId, 
    userId, 
    "Titre", 
    "Message", 
    AlertType.INFO, 
    AlertCategory.GOAL,
    "/profile",
    "Voir Profil"
);
```

---

## 📊 Vérification

### Tester que tout fonctionne
1. ✅ Table créée : `SELECT * FROM user_alerts;`
2. ✅ Admin connecté : Menu visible
3. ✅ Alerte envoyée : Historique affiché
4. ✅ Utilisateur voit : Widget affiché

---

## 🐛 Problèmes Courants

| Problème | Solution |
|----------|----------|
| Table n'existe pas | Exécuter `CREATE_USER_ALERTS_TABLE.bat` |
| Menu invisible | Vérifier connexion admin |
| Alertes non affichées | Appeler `alertsWidget.refresh()` |
| Erreur SQL | Vérifier connexion MySQL |

---

## 📚 Documentation Complète

Pour plus de détails, consultez :
- `USER_MANAGEMENT_AND_ALERTS_GUIDE.md` - Guide complet
- `ADMIN_BACKOFFICE_GUIDE.md` - Guide backoffice

---

## ✅ Checklist

- [ ] Table `user_alerts` créée
- [ ] Application lancée
- [ ] Connexion admin OK
- [ ] Première alerte envoyée
- [ ] Alerte visible côté utilisateur

---

**Temps d'installation** : ~2 minutes  
**Difficulté** : ⭐ Facile  
**Support** : Voir documentation complète
