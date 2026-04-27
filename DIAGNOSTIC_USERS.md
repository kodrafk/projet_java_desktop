# 🔍 DIAGNOSTIC - Pourquoi "No users found"

## Étape 1 : Vérifier les logs de l'application

Quand vous ouvrez l'interface "Personalized Messages", regardez la console.

Vous devriez voir des lignes comme :
```
[DEBUG] Total users from DB: X
[DEBUG] User: email@example.com | Role: ROLE_XXX | FullName: Name
[DEBUG] Filtered users (non-admin): Y
```

### Cas 1 : Vous ne voyez AUCUN log [DEBUG]
**Problème** : Le code n'a pas été recompilé
**Solution** :
```bash
mvn clean compile
mvn javafx:run
```

### Cas 2 : Vous voyez `[DEBUG] Total users from DB: 0`
**Problème** : La base de données est vide ou la connexion échoue
**Solution** : Ajouter des utilisateurs (voir Étape 2)

### Cas 3 : Vous voyez `[DEBUG] Total users from DB: X` mais `[DEBUG] Filtered users (non-admin): 0`
**Problème** : Tous les utilisateurs ont `roles = 'ROLE_ADMIN'`
**Solution** : Ajouter des utilisateurs avec `roles = 'ROLE_USER'` (voir Étape 2)

### Cas 4 : Vous voyez `[DEBUG] Filtered users (non-admin): 5` mais l'interface est vide
**Problème** : Problème d'affichage JavaFX
**Solution** : Vérifier le fichier FXML (voir Étape 3)

---

## Étape 2 : Ajouter des utilisateurs manuellement

### Option A : Via phpMyAdmin (RECOMMANDÉ)

1. Ouvrir http://localhost/phpmyadmin
2. Cliquer sur "nutrilife" à gauche
3. Cliquer sur l'onglet "SQL"
4. Ouvrir le fichier `INSERT_USERS_DIRECT.sql`
5. Copier TOUT le contenu
6. Coller dans phpMyAdmin
7. Cliquer "Exécuter"
8. Vous devriez voir "SUCCESS: 5 users added!"

### Option B : Via ligne de commande

Si vous avez MySQL dans le PATH :
```bash
cd projetJAV
mysql -u root nutrilife < INSERT_USERS_DIRECT.sql
```

---

## Étape 3 : Vérifier que les utilisateurs sont dans la base

Dans phpMyAdmin :
1. Cliquer sur "nutrilife"
2. Cliquer sur la table "user"
3. Cliquer sur "Afficher" ou "Browse"
4. Vérifier que vous voyez les 5 utilisateurs avec `roles = 'ROLE_USER'`

Ou via SQL :
```sql
SELECT id, email, roles, first_name, last_name FROM user WHERE roles = 'ROLE_USER';
```

Vous devriez voir :
```
id | email                        | roles     | first_name | last_name
---+------------------------------+-----------+------------+-----------
X  | john.doe@nutrilife.com       | ROLE_USER | John       | Doe
X  | jane.smith@nutrilife.com     | ROLE_USER | Jane       | Smith
X  | bob.johnson@nutrilife.com    | ROLE_USER | Bob        | Johnson
X  | alice.williams@nutrilife.com | ROLE_USER | Alice      | Williams
X  | charlie.brown@nutrilife.com  | ROLE_USER | Charlie    | Brown
```

---

## Étape 4 : Relancer l'application

```bash
mvn clean compile
mvn javafx:run
```

Puis :
1. Se connecter : admin@nutrilife.com / admin123
2. Menu → Personalized Messages
3. Regarder la console pour les logs [DEBUG]
4. Vérifier l'interface

---

## Étape 5 : Vérifier la connexion à la base de données

Au démarrage de l'application, vous devriez voir :
```
[MyBDConnexion] Attempting MySQL...
✅ [MyBDConnexion] MySQL connected
```

Si vous voyez :
```
⚠️ [MyBDConnexion] MySQL unavailable
✅ [MyBDConnexion] SQLite connected: nutrilife.db
```

Alors l'application utilise SQLite au lieu de MySQL. Dans ce cas :
1. Les utilisateurs doivent être ajoutés dans le fichier `nutrilife.db`
2. Utilisez DB Browser for SQLite pour ajouter les utilisateurs

---

## 🆘 Si rien ne fonctionne

Envoyez-moi :

1. **Les logs de la console** au démarrage de l'application
2. **Les logs [DEBUG]** quand vous ouvrez Personalized Messages
3. **Le résultat de cette requête SQL** :
   ```sql
   SELECT id, email, roles, first_name, last_name FROM user;
   ```

---

## ✅ Checklist de vérification

- [ ] XAMPP est lancé (Apache + MySQL)
- [ ] Base de données "nutrilife" existe
- [ ] Table "user" existe
- [ ] Au moins 5 utilisateurs avec `roles = 'ROLE_USER'` dans la table
- [ ] Application recompilée avec `mvn clean compile`
- [ ] Logs [DEBUG] visibles dans la console
- [ ] `[DEBUG] Filtered users (non-admin): 5` affiché

Si tous les points sont cochés et l'interface est toujours vide, il y a un problème avec le fichier FXML ou le chargement de l'interface.
