# 🚀 Guide de Lancement de l'Interface - Dashboard Anomalies

## 🎯 MÉTHODE 1 : Via IDE (Recommandé)

### IntelliJ IDEA

1. **Ouvrir le projet**
   ```
   File → Open → Sélectionner le dossier projetJAV
   ```

2. **Attendre l'indexation**
   - Laisser IntelliJ charger le projet
   - Attendre que Maven télécharge les dépendances

3. **Localiser MainApp.java**
   ```
   src/main/java/tn/esprit/projet/MainApp.java
   ```

4. **Lancer l'application**
   - Clic droit sur MainApp.java
   - Sélectionner "Run 'MainApp.main()'"
   - Ou appuyer sur Shift+F10

5. **Se connecter en ADMIN**
   ```
   Email: admin@nutrilife.com (ou votre admin)
   Password: votre mot de passe admin
   ```

6. **Accéder au Dashboard**
   ```
   Menu latéral → HEALTH AI → 🔍 Anomaly Detection
   ```

### Eclipse

1. **Importer le projet**
   ```
   File → Import → Maven → Existing Maven Projects
   Sélectionner le dossier projetJAV
   ```

2. **Attendre la synchronisation Maven**

3. **Localiser MainApp.java**
   ```
   src/main/java → tn.esprit.projet → MainApp.java
   ```

4. **Lancer**
   - Clic droit sur MainApp.java
   - Run As → Java Application

5. **Se connecter et accéder au dashboard** (même que IntelliJ)

---

## 🎯 MÉTHODE 2 : Via Maven

### Depuis le terminal

```bash
# Se placer dans le dossier projetJAV
cd projetJAV

# Compiler le projet
mvn clean compile

# Lancer l'application
mvn javafx:run
```

### Depuis PowerShell (Windows)

```powershell
cd projetJAV
mvn clean compile
mvn javafx:run
```

---

## 🎯 MÉTHODE 3 : Via JAR (Production)

### Créer le JAR

```bash
cd projetJAV
mvn clean package
```

### Lancer le JAR

```bash
java -jar target/nutrilife-1.0.jar
```

---

## 📊 CE QUE VOUS VERREZ

### 1. Écran de Connexion
```
┌─────────────────────────────────┐
│     NutriLife - Login           │
│                                 │
│  Email: [________________]      │
│  Password: [____________]       │
│                                 │
│         [  Login  ]             │
└─────────────────────────────────┘
```

### 2. Menu Admin (après connexion)
```
┌─────────────────────────────────┐
│ 📗 Nutri Admin                  │
├─────────────────────────────────┤
│ MAIN                            │
│   Dashboard                     │
│                                 │
│ MANAGEMENT                      │
│   Users                         │
│   User Profiles                 │
│   Statistics                    │
│                                 │
│ HEALTH AI          ← NOUVEAU    │
│ 🔍 Anomaly Detection ← CLIQUEZ  │
│                                 │
│ CONTENT                         │
│   Ingredients                   │
│   Recipes                       │
│   Events                        │
└─────────────────────────────────┘
```

### 3. Dashboard Anomalies (après clic)
```
┌─────────────────────────────────────────────────────────────────┐
│ 🔍 Détection Intelligente d'Anomalies                          │
│ Système ML de surveillance de la santé des utilisateurs        │
│                                    [🚀 Lancer] [🔄 Actualiser] │
├─────────────────────────────────────────────────────────────────┤
│ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│ │📊 42     │ │🚨 8      │ │⚠️ 15     │ │👥 12     │          │
│ │Non Rés.  │ │Critiques │ │Alertes   │ │À Risque  │          │
│ └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
├─────────────────────────────────────────────────────────────────┤
│ [📊 PieChart]  [📊 BarChart]  [👥 Top Risques]                │
├─────────────────────────────────────────────────────────────────┤
│ 📋 Anomalies Détectées                                          │
│ ┌───────────────────────────────────────────────────────────┐  │
│ │User│Type│Description│Sévérité│Date│[Détails][Résoudre]│  │
│ └───────────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│ 🔔 Alertes Prédictives en Attente                              │
│ ┌───────────────────────────────────────────────────────────┐  │
│ │User│Titre│Priorité│Risque│Âge│[Prendre en charge]│      │
│ └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧪 TESTER L'INTERFACE

### Test 1 : Lancer la Détection

1. Cliquez sur **"🚀 Lancer Détection"**
2. Attendez quelques secondes (traitement)
3. Observez :
   - Les cartes se mettent à jour
   - Les graphiques se remplissent
   - Les tables affichent des données

### Test 2 : Voir les Détails

1. Dans la table des anomalies
2. Cliquez sur **[Détails]**
3. Une popup s'affiche avec :
   - Type d'anomalie
   - Description
   - Sévérité
   - Date de détection

### Test 3 : Résoudre une Anomalie

1. Dans la table des anomalies
2. Cliquez sur **[Résoudre]**
3. Entrez une note de résolution
4. Cliquez **OK**
5. L'anomalie disparaît de la liste

### Test 4 : Prendre en Charge une Alerte

1. Dans la table des alertes
2. Cliquez sur **[Prendre en charge]**
3. L'alerte disparaît de la liste

### Test 5 : Actualiser

1. Cliquez sur **"🔄 Actualiser"**
2. Les données se rechargent
3. Les graphiques se mettent à jour

---

## 🎨 ÉLÉMENTS VISUELS

### Cartes Statistiques (Couleurs)

```
📊 Anomalies Non Résolues : Bleu (#007bff)
🚨 Anomalies Critiques : Rouge (#dc3545)
⚠️ Alertes en Attente : Jaune (#ffc107)
👥 Utilisateurs à Risque : Orange (#fd7e14)
```

### Graphiques

**PieChart** : Répartition par type
- Perte rapide : Rouge
- Gain rapide : Orange
- Inactivité : Gris
- Yo-yo : Violet
- Objectif irréaliste : Jaune
- Risque abandon : Rouge foncé
- Comportement anormal : Bleu

**BarChart** : Distribution de sévérité
- Faible (< 40%) : Bleu
- Moyen (40-59%) : Jaune
- Élevé (60-79%) : Orange
- Critique (≥ 80%) : Rouge

**ListView** : Top utilisateurs à risque
- Nom de l'utilisateur
- Score de risque (barre de progression)
- Nombre d'anomalies

### Tables

**Colonnes Anomalies** :
- Utilisateur
- Type
- Description
- Sévérité (avec couleur)
- Date
- Statut
- Actions

**Colonnes Alertes** :
- Utilisateur
- Titre
- Priorité (avec couleur)
- Score de risque
- Âge
- Actions

---

## 🐛 DÉPANNAGE

### Problème : Application ne démarre pas

**Solution 1 : Vérifier Java**
```bash
java -version
# Doit afficher Java 11 ou supérieur
```

**Solution 2 : Nettoyer et recompiler**
```bash
mvn clean compile
```

**Solution 3 : Vérifier les dépendances**
```bash
mvn dependency:tree
```

### Problème : Menu "HEALTH AI" invisible

**Vérification 1 : Fichier modifié**
```
Ouvrir : src/main/resources/fxml/admin_layout.fxml
Chercher : "HEALTH AI"
Doit être présent
```

**Vérification 2 : Contrôleur modifié**
```
Ouvrir : src/main/java/tn/esprit/projet/gui/AdminLayoutController.java
Chercher : "handleAnomalyDetection"
Doit être présent
```

**Solution : Recompiler**
```bash
mvn clean compile
```

### Problème : Dashboard vide

**Solution 1 : Lancer la détection**
```
Cliquer sur "🚀 Lancer Détection"
```

**Solution 2 : Vérifier la base de données**
```sql
-- Vérifier les tables
SHOW TABLES LIKE 'health%';

-- Vérifier les données
SELECT COUNT(*) FROM health_anomalies;
```

**Solution 3 : Insérer des données de test**
```bash
# Exécuter le script de test
TEST_ANOMALY_DETECTION.bat
```

### Problème : Erreur de connexion base de données

**Solution : Vérifier MySQL**
```bash
# Vérifier que MySQL est démarré
mysql -u root -p -e "SHOW DATABASES;"

# Vérifier que nutrilife existe
mysql -u root -p -e "USE nutrilife; SHOW TABLES;"
```

### Problème : Graphiques ne s'affichent pas

**Solution : Vérifier JavaFX**
```bash
# Vérifier que JavaFX est installé
mvn dependency:tree | grep javafx
```

---

## 📸 CAPTURES D'ÉCRAN (Description)

### Vue 1 : Dashboard Initial
- 4 cartes en haut avec statistiques
- 3 graphiques au milieu
- 2 tables en bas

### Vue 2 : Après Détection
- Cartes mises à jour avec nombres
- Graphiques remplis avec données
- Tables avec lignes d'anomalies

### Vue 3 : Popup Détails
- Fenêtre modale
- Informations complètes de l'anomalie
- Bouton OK pour fermer

### Vue 4 : Dialog Résolution
- Champ de texte pour note
- Boutons OK/Annuler
- Validation de la résolution

---

## 🎯 CHECKLIST DE TEST INTERFACE

### Lancement
- [ ] Application démarre sans erreur
- [ ] Écran de connexion s'affiche
- [ ] Connexion admin fonctionne
- [ ] Menu principal s'affiche

### Navigation
- [ ] Section "HEALTH AI" visible
- [ ] Bouton "Anomaly Detection" visible
- [ ] Clic sur le bouton fonctionne
- [ ] Dashboard s'affiche

### Dashboard
- [ ] 4 cartes statistiques visibles
- [ ] 3 graphiques visibles
- [ ] 2 tables visibles
- [ ] Boutons d'action visibles

### Fonctionnalités
- [ ] Bouton "Lancer Détection" fonctionne
- [ ] Données se chargent
- [ ] Graphiques se remplissent
- [ ] Tables se remplissent
- [ ] Bouton "Actualiser" fonctionne

### Actions
- [ ] Bouton "Détails" fonctionne
- [ ] Popup s'affiche correctement
- [ ] Bouton "Résoudre" fonctionne
- [ ] Dialog s'affiche correctement
- [ ] Résolution fonctionne
- [ ] Bouton "Prendre en charge" fonctionne

### Visuel
- [ ] Couleurs correctes
- [ ] Polices lisibles
- [ ] Icônes affichées
- [ ] Layout responsive
- [ ] Pas de débordement

---

## 🎬 VIDÉO DE DÉMONSTRATION (Script)

### Séquence 1 : Lancement (30s)
1. Lancer l'application
2. Se connecter en admin
3. Naviguer vers le dashboard

### Séquence 2 : Vue d'ensemble (30s)
1. Montrer les 4 cartes
2. Montrer les 3 graphiques
3. Montrer les 2 tables

### Séquence 3 : Détection (1min)
1. Cliquer "Lancer Détection"
2. Montrer le traitement
3. Montrer les résultats

### Séquence 4 : Actions (1min)
1. Voir les détails d'une anomalie
2. Résoudre une anomalie
3. Prendre en charge une alerte

### Séquence 5 : Actualisation (30s)
1. Cliquer "Actualiser"
2. Montrer la mise à jour

---

## ✅ RÉSULTAT ATTENDU

Après avoir suivi ce guide, vous devriez avoir :

✅ Application lancée
✅ Dashboard accessible
✅ Cartes affichées avec données
✅ Graphiques remplis
✅ Tables avec anomalies
✅ Actions fonctionnelles
✅ Interface responsive

---

## 🎉 SUCCÈS !

Si tout fonctionne, vous avez :
- ✅ Un dashboard professionnel
- ✅ Des graphiques temps réel
- ✅ Des actions interactives
- ✅ Une interface moderne

**Prêt pour la démonstration ! 🚀**
