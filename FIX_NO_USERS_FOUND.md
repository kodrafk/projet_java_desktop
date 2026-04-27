# 🔧 Fix: "No users found" dans Personalized Messages

## 🎯 Problème

L'interface "Personalized Messages" affiche "No users found" au lieu de la liste des utilisateurs.

## 🔍 Causes Possibles

1. **Aucun utilisateur dans la base de données**
2. **Tous les utilisateurs ont le rôle ROLE_ADMIN**
3. **Problème de connexion à la base de données**
4. **Nom de table incorrect (user vs users)**

## ✅ Solutions

### Solution 1 : Vérifier et créer des utilisateurs de test

```bash
cd projetJAV
CHECK_AND_CREATE_TEST_USERS.bat
```

Ce script va :
- ✅ Afficher tous les utilisateurs existants
- ✅ Créer 5 utilisateurs de test si nécessaire
- ✅ Afficher un résumé

**Identifiants des utilisateurs de test** :
- Email : `john.doe@nutrilife.com`
- Email : `jane.smith@nutrilife.com`
- Email : `bob.johnson@nutrilife.com`
- Email : `alice.williams@nutrilife.com`
- Email : `charlie.brown@nutrilife.com`
- Password : `password123`

### Solution 2 : Vérifier manuellement dans MySQL

```sql
-- Connexion à MySQL
mysql -u root -p

-- Utiliser la base nutrilife
USE nutrilife;

-- Vérifier les utilisateurs
SELECT id, email, roles, first_name, last_name FROM user;

-- Vérifier le nombre d'utilisateurs par rôle
SELECT roles, COUNT(*) FROM user GROUP BY roles;
```

### Solution 3 : Créer un utilisateur manuellement

```sql
INSERT INTO user (email, password, roles, first_name, last_name, is_active, created_at)
VALUES (
    'test@nutrilife.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ROLE_USER',
    'Test',
    'User',
    1,
    NOW()
);
```

### Solution 4 : Vérifier les logs de l'application

Après avoir modifié le code, les logs afficheront :

```
[DEBUG] Total users from DB: 6
[DEBUG] User: admin@nutrilife.com | Role: ROLE_ADMIN | FullName: Admin User
[DEBUG] User: john.doe@nutrilife.com | Role: ROLE_USER | FullName: John Doe
[DEBUG] User: jane.smith@nutrilife.com | Role: ROLE_USER | FullName: Jane Smith
...
[DEBUG] Filtered users (non-admin): 5
```

Si vous voyez `Total users from DB: 0`, le problème vient de la connexion à la base de données.

## 🚀 Test Rapide

1. **Exécuter le script de création** :
   ```bash
   CHECK_AND_CREATE_TEST_USERS.bat
   ```

2. **Relancer l'application** :
   ```bash
   mvn javafx:run
   ```

3. **Connexion admin** :
   - Email : `admin@nutrilife.com`
   - Password : `admin123`

4. **Aller dans Personalized Messages** :
   - Menu → 💬 Personalized Messages
   - Vous devriez voir 5 utilisateurs

## 🐛 Dépannage Avancé

### Problème : Table 'user' n'existe pas

**Erreur** : `Table 'nutrilife.user' doesn't exist`

**Solution** :
```sql
-- Vérifier le nom de la table
SHOW TABLES LIKE '%user%';

-- Si la table s'appelle 'users' au lieu de 'user'
-- Modifier UserRepository.java ligne 23 :
-- "SELECT * FROM users WHERE id=?"
```

### Problème : Colonne 'roles' n'existe pas

**Erreur** : `Unknown column 'roles' in 'field list'`

**Solution** :
```sql
-- Vérifier la structure de la table
DESCRIBE user;

-- Si la colonne s'appelle 'role' au lieu de 'roles'
ALTER TABLE user CHANGE role roles VARCHAR(50);
```

### Problème : Connexion MySQL refusée

**Erreur** : `Communications link failure`

**Solution** :
1. Vérifier que MySQL est démarré (XAMPP/WAMP)
2. Vérifier les identifiants dans `DatabaseConnection.java`
3. Tester la connexion :
   ```bash
   mysql -u root -p
   ```

## 📊 Vérification Finale

Après avoir appliqué les solutions, vérifiez :

- [ ] Script `CHECK_AND_CREATE_TEST_USERS.bat` exécuté avec succès
- [ ] Au moins 5 utilisateurs non-admin dans la base
- [ ] Application relancée
- [ ] Connexion admin OK
- [ ] Menu "Personalized Messages" affiche les utilisateurs
- [ ] Logs console affichent le nombre correct d'utilisateurs

## 💡 Astuce

Si le problème persiste, vérifiez dans les logs console :

```
[DEBUG] Total users from DB: X
[DEBUG] Filtered users (non-admin): Y
```

- Si X = 0 → Problème de connexion DB ou table vide
- Si X > 0 mais Y = 0 → Tous les utilisateurs sont admin
- Si Y > 0 mais interface vide → Problème d'affichage JavaFX

## 📞 Support

Si aucune solution ne fonctionne :

1. Copier les logs console
2. Exécuter `CHECK_AND_CREATE_TEST_USERS.bat`
3. Copier le résultat SQL
4. Vérifier la structure de la table `user`

---

**Version** : 1.0.0  
**Date** : 26 Avril 2026
