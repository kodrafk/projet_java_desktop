# 🚀 COMMENT LANCER L'APPLICATION NUTRILIFE

## 📋 PRÉREQUIS

Avant de lancer l'application, assurez-vous d'avoir:

✅ **Java JDK 11+** installé
```bash
java -version
```

✅ **Maven** installé
```bash
mvn --version
```

✅ **MySQL** en cours d'exécution (WAMP/XAMPP)
- Vérifiez que l'icône WAMP/XAMPP est verte
- MySQL doit être sur le port 3306

✅ **Base de données 'nutrilife'** créée
```sql
CREATE DATABASE nutrilife;
```

---

## 🎯 MÉTHODE 1: SCRIPT AUTOMATIQUE (RECOMMANDÉ)

### Option A: Script complet avec installation
```bash
Double-cliquez sur: LAUNCH_ANOMALY_SYSTEM.bat
```

Ce script va:
1. ✅ Vérifier MySQL
2. ✅ Vérifier la base de données
3. ✅ Installer les tables d'anomalies
4. ✅ Créer le compte admin
5. ✅ Compiler le projet
6. ✅ Lancer l'application

### Option B: Script simple (si déjà installé)
```bash
Double-cliquez sur: LAUNCH_APP.bat
```

---

## 🎯 MÉTHODE 2: VIA INTELLIJ IDEA

### Étape 1: Ouvrir le projet
1. Ouvrez IntelliJ IDEA
2. File → Open
3. Sélectionnez le dossier `projetJAV`
4. Attendez que Maven télécharge les dépendances

### Étape 2: Configurer le projet
1. File → Project Structure
2. Project SDK: Sélectionnez Java 11 ou supérieur
3. Project language level: 11

### Étape 3: Lancer l'application
1. Naviguez vers: `src/main/java/tn/esprit/projet/MainApp.java`
2. Clic droit sur `MainApp.java`
3. Sélectionnez **Run 'MainApp.main()'**

Ou utilisez le raccourci: **Shift + F10**

---

## 🎯 MÉTHODE 3: VIA ECLIPSE

### Étape 1: Importer le projet
1. Ouvrez Eclipse
2. File → Import → Maven → Existing Maven Projects
3. Sélectionnez le dossier `projetJAV`
4. Cliquez sur Finish

### Étape 2: Lancer l'application
1. Naviguez vers: `src/main/java/tn/esprit/projet/MainApp.java`
2. Clic droit sur `MainApp.java`
3. Sélectionnez **Run As → Java Application**

---

## 🎯 MÉTHODE 4: VIA LIGNE DE COMMANDE

### Étape 1: Ouvrir le terminal
```bash
cd projetJAV
```

### Étape 2: Compiler le projet
```bash
mvn clean compile
```

### Étape 3: Lancer l'application
```bash
mvn javafx:run
```

Ou avec package:
```bash
mvn clean package
java -jar target/projet-1.0-SNAPSHOT.jar
```

---

## 🔐 CONNEXION À L'APPLICATION

### Compte Admin de Test
```
📧 Email: admin.test@nutrilife.com
🔐 Password: admin123
```

### Créer le compte admin (si nécessaire)
```bash
Double-cliquez sur: CREATE_ADMIN_FOR_TESTING.bat
```

Ou manuellement:
```bash
mysql -u root -p nutrilife < CREATE_ADMIN_FOR_TESTING.sql
```

---

## 🔍 ACCÉDER AU DASHBOARD D'ANOMALIES

### Une fois connecté:

1. **Regardez le menu latéral gauche**
   ```
   ┌─────────────────────┐
   │ MAIN                │
   │   Dashboard         │
   │                     │
   │ MANAGEMENT          │
   │   Users             │
   │   User Profiles     │
   │   Statistics        │
   │                     │
   │ HEALTH AI           │  ← Cherchez cette section
   │ 🔍 Anomaly Detection│  ← Cliquez ici!
   │                     │
   │ CONTENT             │
   │   Ingredients       │
   │   Recipes           │
   └─────────────────────┘
   ```

2. **Cliquez sur "🔍 Anomaly Detection"**

3. **Dans le dashboard:**
   - Cliquez sur **[🚀 Lancer Détection]**
   - Attendez 5-10 secondes
   - Observez les résultats!

---

## 🐛 DÉPANNAGE

### Problème: "MySQL connection refused"
**Solution:**
```bash
# Vérifiez que MySQL est démarré
# Dans WAMP: Clic gauche sur l'icône → MySQL → Service → Start/Resume Service
# Dans XAMPP: Cliquez sur "Start" à côté de MySQL
```

### Problème: "Database 'nutrilife' doesn't exist"
**Solution:**
```bash
mysql -u root -p -e "CREATE DATABASE nutrilife;"
```

### Problème: "Table 'health_anomalies' doesn't exist"
**Solution:**
```bash
Double-cliquez sur: INSTALL_ANOMALY_SYSTEM.bat
```

Ou manuellement:
```bash
mysql -u root -p nutrilife < CREATE_ANOMALY_DETECTION_TABLES.sql
```

### Problème: "Email ou mot de passe incorrect"
**Solution:**
```bash
# Créez le compte admin
Double-cliquez sur: CREATE_ADMIN_FOR_TESTING.bat

# Ou vérifiez les comptes existants
Double-cliquez sur: GET_ADMIN_ACCOUNT.bat
```

### Problème: "Maven not found"
**Solution:**
```bash
# Téléchargez Maven: https://maven.apache.org/download.cgi
# Ajoutez Maven au PATH système
# Vérifiez: mvn --version
```

### Problème: "JAVA_HOME not set"
**Solution:**
```bash
# Windows:
# 1. Panneau de configuration → Système → Paramètres système avancés
# 2. Variables d'environnement
# 3. Nouvelle variable système:
#    Nom: JAVA_HOME
#    Valeur: C:\Program Files\Java\jdk-11.0.x
```

### Problème: "Port 3306 already in use"
**Solution:**
```bash
# Vérifiez qu'une seule instance de MySQL est en cours
# Arrêtez les autres instances de MySQL
# Ou changez le port dans MyBDConnexion.java
```

### Problème: "JavaFX runtime components are missing"
**Solution:**
```bash
# Vérifiez le pom.xml contient:
# <dependency>
#     <groupId>org.openjfx</groupId>
#     <artifactId>javafx-controls</artifactId>
#     <version>17.0.2</version>
# </dependency>

# Puis:
mvn clean install
```

---

## 📊 VÉRIFICATION QUE TOUT FONCTIONNE

### Checklist avant de lancer:
- [ ] MySQL est démarré (icône WAMP/XAMPP verte)
- [ ] Base 'nutrilife' existe
- [ ] Tables 'health_*' créées
- [ ] Compte admin créé
- [ ] Java 11+ installé
- [ ] Maven installé
- [ ] Projet compilé sans erreurs

### Checklist après le lancement:
- [ ] Fenêtre de connexion s'affiche
- [ ] Connexion avec admin.test@nutrilife.com réussie
- [ ] Menu "HEALTH AI" visible dans le menu latéral
- [ ] Dashboard d'anomalies s'affiche
- [ ] Bouton "Lancer Détection" fonctionne
- [ ] Résultats s'affichent (cartes, graphiques, tables)

---

## 🎥 DÉMONSTRATION RAPIDE

### Test complet en 2 minutes:

```bash
# 1. Installer (30 secondes)
Double-cliquez sur: INSTALL_ANOMALY_SYSTEM.bat

# 2. Créer admin (10 secondes)
Double-cliquez sur: CREATE_ADMIN_FOR_TESTING.bat

# 3. Lancer (1 minute)
Double-cliquez sur: LAUNCH_ANOMALY_SYSTEM.bat

# 4. Se connecter
Email: admin.test@nutrilife.com
Password: admin123

# 5. Tester
Menu → HEALTH AI → Anomaly Detection → Lancer Détection
```

---

## 📞 BESOIN D'AIDE?

### Fichiers utiles:
- `ADMIN_CREDENTIALS.txt` - Identifiants admin
- `ANOMALY_DETECTION_GUIDE.md` - Guide complet du système
- `ANOMALY_SYSTEM_STATUS.md` - Statut de l'implémentation
- `EXPLICATION_COMPLETE.md` - Explication détaillée

### Scripts utiles:
- `INSTALL_ANOMALY_SYSTEM.bat` - Installation complète
- `CREATE_ADMIN_FOR_TESTING.bat` - Créer compte admin
- `GET_ADMIN_ACCOUNT.bat` - Voir comptes admin
- `LAUNCH_ANOMALY_SYSTEM.bat` - Lancer avec vérifications
- `TEST_ANOMALY_DETECTION.bat` - Tester le système

---

## ✅ RÉSUMÉ

**Pour lancer rapidement:**
1. Assurez-vous que MySQL est démarré
2. Double-cliquez sur `LAUNCH_ANOMALY_SYSTEM.bat`
3. Connectez-vous avec admin.test@nutrilife.com / admin123
4. Menu → HEALTH AI → Anomaly Detection

**C'est tout! 🎉**

---

**Date**: 25 Avril 2026  
**Version**: 1.0.0  
**Statut**: ✅ PRÊT À LANCER
