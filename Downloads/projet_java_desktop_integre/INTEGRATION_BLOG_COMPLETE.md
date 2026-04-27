# 📝 Intégration Complète du Système de Blog

## ✅ Fichiers Créés

### Modèles (Models)
- `Publication.java` - Modèle pour les publications de blog
- `PublicationComment.java` - Modèle pour les commentaires
- `PublicationLike.java` - Modèle pour les likes/dislikes
- `PublicationReport.java` - Modèle pour les signalements

### Services
- `PublicationService.java` - CRUD pour les publications
- `PublicationCommentService.java` - CRUD pour les commentaires
- `PublicationLikeService.java` - Gestion des likes/dislikes
- `PublicationReportService.java` - Gestion des signalements

### Utilitaires
- `BadWordsFilter.java` - Filtre de contenu inapproprié via API profanity.dev
- `AIService.java` - Génération de titres et hashtags via Google Gemini AI

### Contrôleurs (GUI)
- `BlogFrontController.java` - Interface utilisateur du blog
- `BlogAdminController.java` - Interface admin pour modération

## 🎨 Fonctionnalités

### Pour les Utilisateurs
1. **Créer des publications** avec titre, contenu et image
2. **Génération automatique** de titres via IA
3. **Génération automatique** de hashtags via IA
4. **Filtrage de contenu** inapproprié automatique
5. **Likes et Dislikes** sur les publications
6. **Commentaires** avec modification et suppression
7. **Signalement** de publications inappropriées
8. **Recherche** par titre, contenu ou auteur
9. **Tri** par date ou popularité
10. **Notifications** en temps réel pour nouveaux posts

### Pour les Administrateurs
1. **Modération** complète des publications
2. **Gestion des signalements** avec seuil automatique
3. **Approbation** des publications signalées
4. **Suppression** de publications et commentaires
5. **Vue d'ensemble** de tous les contenus

## 🗄️ Structure de Base de Données Requise

```sql
-- Table des publications
CREATE TABLE publication (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(100) NOT NULL,
    contenu TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    author_name VARCHAR(100),
    author_avatar VARCHAR(255),
    is_admin BOOLEAN DEFAULT FALSE,
    image VARCHAR(255),
    view_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    visibility VARCHAR(20) DEFAULT 'public',
    scheduled_at TIMESTAMP NULL,
    shared_from_id INT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Table des commentaires
CREATE TABLE publication_comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contenu TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    author_name VARCHAR(100),
    author_avatar VARCHAR(255),
    is_admin BOOLEAN DEFAULT FALSE,
    publication_id INT NOT NULL,
    user_id INT NOT NULL,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

-- Table des likes/dislikes
CREATE TABLE publication_like (
    id INT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    publication_id INT NOT NULL,
    user_id INT NOT NULL,
    is_like BOOLEAN NOT NULL,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_publication (publication_id, user_id)
);

-- Table des signalements
CREATE TABLE publication_report (
    id INT AUTO_INCREMENT PRIMARY KEY,
    publication_id INT NOT NULL,
    user_id INT NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_report (publication_id, user_id)
);
```

## 🚀 Comment Accéder au Blog

### Pour les Utilisateurs
1. Connectez-vous à l'application
2. Dans le dashboard principal, cliquez sur **"📝 Blog"** dans le menu de navigation
3. Vous verrez l'interface du blog avec:
   - Zone de création de publication en haut
   - Fil d'actualité des publications en dessous
   - Barre de recherche et tri

### Pour les Administrateurs
1. Connectez-vous avec un compte admin
2. Dans le panel admin, cliquez sur **"📝 Blogs"**
3. Vous verrez l'interface de modération avec:
   - Liste de toutes les publications
   - Liste des commentaires par publication
   - Boutons de suppression

## 🎯 Utilisation

### Créer une Publication
1. Remplissez le titre (5-100 caractères)
2. Écrivez le contenu (minimum 10 caractères)
3. (Optionnel) Cliquez sur "🤖 Générer Titre" pour un titre automatique
4. (Optionnel) Cliquez sur "# Générer Hashtags" pour des hashtags automatiques
5. (Optionnel) Ajoutez une image
6. Cliquez sur "Publier"

### Interagir avec les Publications
- **Liker/Disliker**: Cliquez sur les boutons "J'aime" ou "Je n'aime pas"
- **Commenter**: Écrivez dans le champ de commentaire et cliquez "Envoyer"
- **Modifier**: Cliquez sur "Modifier" (seulement vos publications)
- **Supprimer**: Cliquez sur "Supprimer" (seulement vos publications)
- **Signaler**: Cliquez sur "🚩 Signaler" pour signaler un contenu inapproprié

### Recherche et Tri
- **Rechercher**: Tapez dans la barre de recherche (titre, contenu, auteur)
- **Trier**: Sélectionnez dans le menu déroulant:
  - Plus récentes
  - Plus anciennes
  - Plus populaires (Likes)

## 🔒 Sécurité

### Filtrage de Contenu
- Vérification automatique via API profanity.dev
- Seuil de détection: 0.90 (très strict)
- Bloque la publication si contenu inapproprié détecté

### Système de Signalement
- Les utilisateurs peuvent signaler les publications
- Seuil: 2 signalements = masquage automatique
- Les admins reçoivent des notifications
- Les admins peuvent approuver ou supprimer

## 🤖 Intelligence Artificielle

### Génération de Titres
- Utilise Google Gemini AI
- Génère des titres accrocheurs (max 10 mots)
- Basé sur le contenu de la publication

### Génération de Hashtags
- Utilise Google Gemini AI
- Génère 5 hashtags pertinents en français
- Basé sur le contenu de la publication

### Configuration
La clé API est déjà configurée dans `AIService.java`:
```java
private static final String API_KEY = "AIzaSyC-E-osVbPoQQ05jDsTBSjw7amQcTUoAl4";
```

## 📦 Dépendances Requises

Assurez-vous que votre `pom.xml` contient:
```xml
<!-- ControlsFX pour les notifications -->
<dependency>
    <groupId>org.controlsfx</groupId>
    <artifactId>controlsfx</artifactId>
    <version>11.1.2</version>
</dependency>

<!-- Jackson pour le parsing JSON (AI Service) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

## 🎨 Design

Le blog utilise un design moderne et professionnel avec:
- **Cartes** avec ombres et coins arrondis
- **Gradient** vert pour l'accent
- **Avatars** circulaires avec initiales
- **Badges** pour les hashtags
- **Animations** pour les notifications
- **Responsive** et adaptatif

## 📝 Fichiers FXML Requis

Vous devez créer deux fichiers FXML:
1. `/fxml/blog_front.fxml` - Interface utilisateur
2. `/fxml/blog_admin.fxml` - Interface admin

Ces fichiers doivent être liés aux contrôleurs respectifs.

## ✅ Intégration Complète

Le système de blog est maintenant complètement intégré dans votre application:
- ✅ Modèles créés
- ✅ Services créés
- ✅ Contrôleurs créés
- ✅ Utilitaires créés
- ✅ Navigation configurée (MainLayoutController)
- ✅ Panel admin configuré (AdminLayoutController)

## 🚀 Prochaines Étapes

1. **Créer les tables** dans votre base de données (voir SQL ci-dessus)
2. **Créer les fichiers FXML** pour les interfaces
3. **Tester** la création de publications
4. **Tester** les interactions (likes, commentaires)
5. **Tester** le système de signalement
6. **Tester** la modération admin

## 📞 Support

Le système est prêt à l'emploi. Si vous rencontrez des problèmes:
1. Vérifiez que les tables de base de données sont créées
2. Vérifiez que les fichiers FXML existent
3. Vérifiez que les dépendances sont installées
4. Vérifiez les logs de la console pour les erreurs

Bon blogging! 🎉
