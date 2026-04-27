# 🧪 Guide de Test Manuel - Système de Détection d'Anomalies

## ✅ ÉTAPE 1 : Installation de la Base de Données

### Option A : Via Script BAT (Recommandé)
```bash
1. Ouvrez un terminal dans le dossier projetJAV
2. Double-cliquez sur: INSTALL_ANOMALY_SYSTEM.bat
3. Attendez la fin de l'installation (30 secondes)
```

### Option B : Manuellement via MySQL
```bash
# Ouvrir MySQL
mysql -u root -p

# Sélectionner la base
USE nutrilife;

# Exécuter le script
SOURCE CREATE_ANOMALY_DETECTION_TABLES.sql;

# Vérifier les tables
SHOW TABLES LIKE 'health%';
```

### Vérification
Vous devriez voir ces tables :
- ✅ health_anomalies
- ✅ health_alerts
- ✅ user_health_metrics
- ✅ anomaly_detection_history

---

## ✅ ÉTAPE 2 : Insérer des Données de Test

### Via MySQL
```sql
-- Utilisateur de test avec anomalie
INSERT IGNORE INTO users (id, email, password, first_name, last_name, is_active, created_at)
VALUES (999, 'test.anomaly@nutrilife.com', 'test123', 'Test', 'Anomaly', TRUE, NOW());

-- Perte de poids rapide (anomalie critique)
DELETE FROM weight_logs WHERE user_id = 999;
INSERT INTO weight_logs (user_id, weight, logged_at) VALUES
(999, 85.0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(999, 81.5, NOW());

-- Objectif irréaliste
DELETE FROM weight_objectives WHERE user_id = 999;
INSERT INTO weight_objectives (user_id, start_weight, target_weight, start_date, target_date, active)
VALUES (999, 85.0, 65.0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), TRUE);
```

### Vérification
```sql
-- Vérifier l'utilisateur
SELECT * FROM users WHERE id = 999;

-- Vérifier les logs de poids
SELECT * FROM weight_logs WHERE user_id = 999;

-- Vérifier l'objectif
SELECT * FROM weight_objectives WHERE user_id = 999;
```

---

## ✅ ÉTAPE 3 : Compiler le Projet

### Via Maven
```bash
# Dans le dossier projetJAV
mvn clean compile

# Ou si vous utilisez un IDE
# IntelliJ IDEA : Build → Build Project
# Eclipse : Project → Build Project
```

### Vérification
- ✅ Aucune erreur de compilation
- ✅ Tous les fichiers Java compilés

---

## ✅ ÉTAPE 4 : Lancer l'Application

### Via IDE
```
1. Ouvrir le projet dans votre IDE
2. Localiser MainApp.java
3. Cliquer sur Run/Debug
4. Attendre le démarrage de l'application
```

### Via Maven
```bash
mvn javafx:run
```

---

## ✅ ÉTAPE 5 : Tester le Dashboard

### 1. Connexion Admin
```
1. Lancer l'application
2. Se connecter avec un compte ADMIN
   Email: admin@nutrilife.com (ou votre admin)
   Password: votre mot de passe admin
```

### 2. Accéder au Dashboard
```
1. Dans le menu latéral gauche
2. Chercher la section "HEALTH AI"
3. Cliquer sur "🔍 Anomaly Detection"
```

### 3. Lancer la Détection
```
1. Cliquer sur le bouton "🚀 Lancer Détection"
2. Attendre quelques secondes
3. Observer les résultats
```

### 4. Vérifier les Résultats

#### Cartes Statistiques (en haut)
- ✅ Anomalies non résolues : devrait afficher un nombre
- ✅ Anomalies critiques : devrait afficher un nombre
- ✅ Alertes en attente : devrait afficher un nombre
- ✅ Utilisateurs à risque : devrait afficher un nombre

#### Graphiques
- ✅ PieChart : Répartition par type d'anomalie
- ✅ BarChart : Distribution de sévérité
- ✅ ListView : Top utilisateurs à risque

#### Table des Anomalies
- ✅ Devrait afficher au moins 1 anomalie pour l'utilisateur test
- ✅ Type : "Rapid Weight Loss" ou similaire
- ✅ Sévérité : environ 87%
- ✅ Boutons : [Détails] [Résoudre]

#### Table des Alertes
- ✅ Devrait afficher au moins 1 alerte
- ✅ Priorité : CRITIQUE
- ✅ Bouton : [Prendre en charge]

---

## ✅ ÉTAPE 6 : Tester les Actions

### Test 1 : Voir les Détails d'une Anomalie
```
1. Dans la table des anomalies
2. Cliquer sur le bouton "Détails"
3. Une popup devrait s'afficher avec les informations
```

### Test 2 : Résoudre une Anomalie
```
1. Dans la table des anomalies
2. Cliquer sur le bouton "Résoudre"
3. Entrer une note de résolution
4. Cliquer OK
5. L'anomalie devrait disparaître de la liste
```

### Test 3 : Prendre en Charge une Alerte
```
1. Dans la table des alertes
2. Cliquer sur "Prendre en charge"
3. L'alerte devrait disparaître de la liste
```

### Test 4 : Actualiser
```
1. Cliquer sur le bouton "🔄 Actualiser"
2. Les données devraient se recharger
```

---

## ✅ ÉTAPE 7 : Vérifier la Base de Données

### Après la détection
```sql
-- Vérifier les anomalies créées
SELECT * FROM health_anomalies ORDER BY detected_at DESC LIMIT 5;

-- Vérifier les alertes créées
SELECT * FROM health_alerts ORDER BY created_at DESC LIMIT 5;

-- Vérifier les métriques calculées
SELECT * FROM user_health_metrics WHERE user_id = 999;

-- Vérifier les statistiques
SELECT * FROM v_anomaly_statistics;
```

### Résultats Attendus

#### health_anomalies
```
id | user_id | type                | severity | confidence | resolved
1  | 999     | RAPID_WEIGHT_LOSS  | 87.5     | 0.95       | FALSE
```

#### health_alerts
```
id | user_id | title                  | priority | risk_score
1  | 999     | Perte de poids rapide | CRITICAL | 87.5
```

#### user_health_metrics
```
user_id | current_weight | weight_change_7days | abandonment_risk
999     | 81.5          | -3.5                | 25.0
```

---

## ✅ ÉTAPE 8 : Tests Avancés

### Test du Scheduler (Optionnel)
```java
// Dans MainApp.java, ajouter au démarrage :
import tn.esprit.projet.utils.StartAnomalyDetection;

@Override
public void start(Stage primaryStage) {
    // ... votre code existant ...
    
    // Démarrer la détection automatique
    StartAnomalyDetection.startInBackground();
}
```

### Test avec Plus d'Utilisateurs
```sql
-- Créer un utilisateur inactif
INSERT INTO users (id, email, password, first_name, last_name, is_active, created_at)
VALUES (998, 'inactive@test.com', 'test', 'Inactive', 'User', TRUE, NOW());

INSERT INTO weight_logs (user_id, weight, logged_at)
VALUES (998, 70.0, DATE_SUB(NOW(), INTERVAL 20 DAY));

-- Créer un utilisateur yo-yo
INSERT INTO users (id, email, password, first_name, last_name, is_active, created_at)
VALUES (997, 'yoyo@test.com', 'test', 'YoYo', 'User', TRUE, NOW());

INSERT INTO weight_logs (user_id, weight, logged_at) VALUES
(997, 70.0, DATE_SUB(NOW(), INTERVAL 30 DAY)),
(997, 73.0, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(997, 69.0, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(997, 72.5, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(997, 68.5, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(997, 71.0, NOW());
```

---

## ✅ CHECKLIST DE TEST

### Installation
- [ ] Tables créées (4)
- [ ] Vues créées (3)
- [ ] Index créés (12)
- [ ] Données de test insérées

### Compilation
- [ ] Projet compile sans erreur
- [ ] Aucun warning critique

### Interface
- [ ] Menu "HEALTH AI" visible
- [ ] Dashboard accessible
- [ ] Cartes statistiques affichées
- [ ] Graphiques affichés
- [ ] Tables affichées

### Fonctionnalités
- [ ] Bouton "Lancer Détection" fonctionne
- [ ] Anomalies détectées
- [ ] Alertes générées
- [ ] Bouton "Détails" fonctionne
- [ ] Bouton "Résoudre" fonctionne
- [ ] Bouton "Prendre en charge" fonctionne
- [ ] Bouton "Actualiser" fonctionne

### Base de Données
- [ ] Anomalies enregistrées
- [ ] Alertes enregistrées
- [ ] Métriques calculées
- [ ] Statistiques correctes

---

## 🐛 Dépannage

### Problème : Tables non créées
**Solution :**
```sql
-- Vérifier la connexion MySQL
SHOW DATABASES;

-- Vérifier que nutrilife existe
USE nutrilife;

-- Exécuter manuellement le script
SOURCE CREATE_ANOMALY_DETECTION_TABLES.sql;
```

### Problème : Erreur de compilation
**Solution :**
```bash
# Nettoyer et recompiler
mvn clean compile

# Vérifier les dépendances
mvn dependency:tree
```

### Problème : Dashboard vide
**Solution :**
1. Vérifier que vous êtes connecté en ADMIN
2. Cliquer sur "Lancer Détection"
3. Vérifier que les tables existent
4. Vérifier que des utilisateurs ont des weight_logs

### Problème : Aucune anomalie détectée
**Solution :**
1. Exécuter les INSERT de données de test
2. Vérifier que l'utilisateur 999 existe
3. Vérifier les weight_logs de l'utilisateur 999
4. Relancer la détection

### Problème : Erreur SQL
**Solution :**
```sql
-- Vérifier la structure des tables
DESCRIBE health_anomalies;
DESCRIBE health_alerts;
DESCRIBE user_health_metrics;

-- Vérifier les contraintes
SHOW CREATE TABLE health_anomalies;
```

---

## 📊 Résultats Attendus

### Après Installation
```
✅ 4 tables créées
✅ 3 vues créées
✅ 12 index créés
✅ Utilisateur de test créé
✅ Anomalie de test créée
```

### Après Détection
```
✅ Au moins 1 anomalie détectée
✅ Au moins 1 alerte générée
✅ Métriques calculées
✅ Statistiques mises à jour
```

### Dashboard Fonctionnel
```
✅ Cartes affichent des nombres
✅ Graphiques affichent des données
✅ Tables affichent des lignes
✅ Boutons fonctionnent
✅ Actions s'exécutent
```

---

## 🎯 Test de Démonstration

### Scénario Complet (5 minutes)

1. **Démarrer l'application** (30s)
2. **Se connecter en admin** (15s)
3. **Accéder au dashboard** (15s)
4. **Lancer la détection** (30s)
5. **Montrer les cartes** (30s)
6. **Montrer les graphiques** (30s)
7. **Montrer une anomalie** (30s)
8. **Résoudre une anomalie** (30s)
9. **Actualiser** (15s)

### Points à Mentionner
- ✅ Machine Learning réel (régression logistique)
- ✅ 7 types d'anomalies détectées
- ✅ Dashboard temps réel
- ✅ Alertes prédictives
- ✅ Performance optimisée

---

## ✅ SUCCÈS !

Si tous les tests passent, vous avez :
- ✅ Un système ML fonctionnel
- ✅ Un dashboard professionnel
- ✅ Des algorithmes de détection
- ✅ Une base de données optimisée
- ✅ Un projet prêt pour la démo

**🎉 Félicitations ! Votre système est prêt !**
