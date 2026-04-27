# 🔧 SOLUTION COMPLÈTE - ERREUR MYSQL XAMPP

## 🚨 Problème Détecté

D'après vos captures d'écran :
- **XAMPP** : MySQL s'arrête immédiatement après le démarrage
- **phpMyAdmin** : Erreur de connexion (HY000/2002)
- **Message** : "MySQL shutdown unexpectedly"

---

## ✅ SOLUTION RAPIDE (RECOMMANDÉE)

### Option 1 : Script Automatique Complet

**Exécutez ce script qui fait TOUT automatiquement :**

```batch
FIX_MYSQL_COMPLETE.bat
```

Ce script va :
1. ✅ Arrêter MySQL
2. ✅ Sauvegarder vos données existantes
3. ✅ Supprimer les fichiers corrompus
4. ✅ Redémarrer MySQL
5. ✅ Créer la base de données `nutrilife_db`
6. ✅ Créer les tables
7. ✅ Créer le compte admin

**Après l'exécution :**
- Ouvrez phpMyAdmin : http://localhost/phpmyadmin
- Vérifiez que la base `nutrilife_db` existe
- Lancez votre application Java

---

## 🔄 SOLUTIONS ALTERNATIVES

### Option 2 : Changer le Port MySQL

Si MySQL ne démarre toujours pas, le port 3306 est peut-être bloqué.

**Exécutez :**
```batch
CHANGE_MYSQL_PORT.bat
```

Cela change le port de **3306** vers **3307**.

**⚠️ IMPORTANT** : Vous devez aussi modifier votre code Java :

```java
// Fichier: src/main/java/tn/esprit/projet/utils/DatabaseConnection.java
private static final int PORT = 3307;  // Changez de 3306 à 3307
```

---

### Option 3 : Réparation Manuelle

Si les scripts ne fonctionnent pas, suivez ces étapes :

#### Étape 1 : Arrêter MySQL
```batch
taskkill /F /IM mysqld.exe
```

#### Étape 2 : Supprimer les fichiers corrompus
Allez dans : `C:\xampp\mysql\data\`

Supprimez ces fichiers :
- `ibdata1`
- `ib_logfile0`
- `ib_logfile1`
- `aria_log_control`
- `aria_log.00000001`

#### Étape 3 : Redémarrer XAMPP
1. Ouvrez XAMPP Control Panel
2. Cliquez sur "Start" à côté de MySQL
3. MySQL devrait démarrer

#### Étape 4 : Créer la base de données
Ouvrez phpMyAdmin et exécutez :

```sql
CREATE DATABASE nutrilife_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### Étape 5 : Importer le SQL
Exécutez le fichier : `CREATE_ADMIN_ACCOUNT.sql`

---

### Option 4 : Utiliser SQLite (Déjà Configuré)

**Bonne nouvelle** : Votre application utilise déjà SQLite comme fallback !

Si MySQL ne fonctionne pas :
- L'application basculera automatiquement sur SQLite
- Fichier de base de données : `nutrilife.db`
- Toutes les fonctionnalités marchent avec SQLite

**Vous pouvez utiliser l'application immédiatement sans réparer MySQL !**

---

## 📊 RÉCUPÉRATION DES DONNÉES SQL

### Structure de la Base de Données

Voici la structure complète de `nutrilife_db` :

```sql
-- Base de données
CREATE DATABASE nutrilife_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE nutrilife_db;

-- Table user
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(50) DEFAULT 'ROLE_USER',
    is_active TINYINT(1) DEFAULT 1,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    birthday DATE,
    weight DECIMAL(5,2),
    height DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table badge
CREATE TABLE badge (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_path VARCHAR(255),
    requirement_type VARCHAR(50),
    requirement_value INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table user_badge
CREATE TABLE user_badge (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    badge_id INT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badge(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_badge (user_id, badge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table weight_log
CREATE TABLE weight_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    log_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table weight_objective
CREATE TABLE weight_objective (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    target_weight DECIMAL(5,2) NOT NULL,
    start_date DATE NOT NULL,
    target_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Compte ADMIN
INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height)
VALUES ('admin@nutrilife.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN', 1, 'Admin', 'NutriLife', '1990-01-01', 75.0, 175.0);

-- Compte USER test
INSERT INTO user (email, password, roles, is_active, first_name, last_name, birthday, weight, height)
VALUES ('user@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', 1, 'John', 'Doe', '1995-05-15', 80.0, 180.0);
```

### Comptes Créés

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| **ADMIN** | admin@nutrilife.com | admin123 |
| **USER** | user@test.com | admin123 |

---

## 🔍 DIAGNOSTIC

### Vérifier si MySQL tourne
```batch
tasklist | findstr mysqld
```

### Vérifier le port 3306
```batch
netstat -ano | findstr :3306
```

### Voir les logs d'erreur MySQL
```batch
type C:\xampp\mysql\data\*.err
```

---

## 🆘 SI RIEN NE FONCTIONNE

### Solution 1 : Réinstaller XAMPP

1. Désinstallez XAMPP
2. Supprimez le dossier `C:\xampp`
3. Téléchargez XAMPP : https://www.apachefriends.org/download.html
4. Réinstallez XAMPP
5. Exécutez `FIX_MYSQL_COMPLETE.bat`

### Solution 2 : Utiliser WAMP

1. Téléchargez WAMP : https://www.wampserver.com/en/
2. Installez WAMP
3. MySQL devrait fonctionner automatiquement
4. Exécutez le script SQL : `CREATE_ADMIN_ACCOUNT.sql`

### Solution 3 : MySQL Standalone

1. Téléchargez MySQL : https://dev.mysql.com/downloads/installer/
2. Installez MySQL seul (sans XAMPP)
3. Configurez le port 3306
4. Exécutez le script SQL

---

## 📝 RÉSUMÉ DES FICHIERS UTILES

| Fichier | Description |
|---------|-------------|
| `FIX_MYSQL_COMPLETE.bat` | ✅ **RECOMMANDÉ** - Répare MySQL + crée la base |
| `CHANGE_MYSQL_PORT.bat` | Change le port 3306 → 3307 |
| `CREATE_ADMIN_ACCOUNT.sql` | Script SQL pour créer les comptes |
| `MYSQL_REPAIR_GUIDE.txt` | Guide de réparation détaillé |
| `nutrilife.db` | Base SQLite (fallback automatique) |

---

## 🎯 PROCHAINES ÉTAPES

1. **Exécutez** : `FIX_MYSQL_COMPLETE.bat`
2. **Vérifiez** : http://localhost/phpmyadmin
3. **Lancez** : Votre application Java
4. **Connectez-vous** : admin@nutrilife.com / admin123

---

## ✅ VÉRIFICATION FINALE

Après avoir exécuté le script, vérifiez :

- [ ] MySQL démarre dans XAMPP Control Panel
- [ ] phpMyAdmin s'ouvre sans erreur
- [ ] La base `nutrilife_db` existe
- [ ] Les tables sont créées
- [ ] Le compte admin existe

---

## 💡 ASTUCE

**Votre application fonctionne déjà avec SQLite !**

Si vous êtes pressé :
- Ignorez MySQL pour l'instant
- Lancez directement votre application
- Elle utilisera SQLite automatiquement
- Réparez MySQL plus tard

---

## 📞 BESOIN D'AIDE ?

Si aucune solution ne fonctionne :
1. Prenez une capture d'écran des logs MySQL
2. Notez le message d'erreur exact
3. Cherchez l'erreur sur Google ou Stack Overflow

---

**Bonne chance ! 🚀**
