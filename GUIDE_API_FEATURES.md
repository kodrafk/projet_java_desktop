# 📚 Guide des APIs et Fonctionnalités

## 1. 🚫 API BadWords (Profanity Detection)

### Comment ça marche ?

L'API **profanity.dev** analyse le texte pour détecter des mots inappropriés.

#### Étapes :

1. **Préparation du texte** (lignes 40-47)
   - Vérifier si le texte est vide
   - Ignorer les répétitions (aaaa, ffff, etc.)

2. **Appel HTTP POST** (lignes 49-68)
   ```java
   URL url = new URL("https://vector.profanity.dev");
   HttpURLConnection conn = (HttpURLConnection) url.openConnection();
   conn.setRequestMethod("POST");
   conn.setRequestProperty("Content-Type", "application/json");
   ```

3. **Envoi du JSON** (lignes 59-67)
   ```json
   {"message": "ton texte ici"}
   ```

4. **Réception de la réponse** (lignes 76-78)
   ```json
   {"isProfanity": true, "score": 0.92}
   ```

5. **Parsing et décision** (lignes 83-107)
   - Si `score >= 0.90` → Bloquer
   - Sinon → Autoriser

### Utilisation dans le code :

```java
// Dans BlogFrontController.java (ligne 148)
BadWordsFilter.Result bw = BadWordsFilter.checkAll(title, content);
if (bw.isProfanity) {
    showAlert(Alert.AlertType.WARNING, "⚠ Contenu inapproprié", "...");
    return;
}
```

---

## 2. 🔔 Système de Notifications

### Comment ça marche ?

Le système utilise **polling** (vérification périodique) pour détecter les nouvelles publications.

#### Architecture :

```
┌─────────────────┐
│  Timeline       │ ← Toutes les 5 secondes
│  (JavaFX)       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Thread         │ ← Appel base de données
│  (Background)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  getLatest      │ ← SELECT * FROM publication ORDER BY id DESC LIMIT 1
│  Publication    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Comparer ID    │ ← Si nouveau ID > ancien ID
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Notification   │ ← Afficher popup
│  (ControlsFX)   │
└─────────────────┘
```

#### Code détaillé :

**Ligne 85-103** : Polling toutes les 5 secondes
```java
pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
    Thread t = new Thread(() -> {
        Publication pub = pubService.getLatestPublication();
        Platform.runLater(() -> {
            if (pub != null && pub.getId() > lastKnownPubId) {
                lastKnownPubId = pub.getId();
                if (pub.getUserId() != SessionManager.getCurrentUser().getId()) {
                    showNotification("Nouveau post de " + pub.getAuthorName());
                    loadFeed();
                }
            }
        });
    });
    t.setDaemon(true);
    t.start();
}));
```

**Ligne 106-113** : Affichage de la notification
```java
Notifications.create()
    .title("Nouveau Post")
    .text(message)
    .hideAfter(Duration.seconds(5))
    .position(Pos.BOTTOM_RIGHT)
    .showInformation();
```

---

## 3. 🤖 API Google Gemini (IA)

### Comment ça marche ?

L'API **Google Gemini** génère du texte basé sur un prompt.

#### Étapes :

1. **Préparation du JSON** (lignes 46-51 dans AIService.java)
   ```java
   ObjectMapper mapper = new ObjectMapper();
   ObjectNode rootNode = mapper.createObjectNode();
   ArrayNode contentsArray = rootNode.putArray("contents");
   ObjectNode contentNode = contentsArray.addObject();
   ArrayNode partsArray = contentNode.putArray("parts");
   partsArray.addObject().put("text", prompt);
   ```

   Résultat JSON :
   ```json
   {
     "contents": [
       {
         "parts": [
           {"text": "Génère un titre pour : ..."}
         ]
       }
     ]
   }
   ```

2. **Appel HTTP POST** (lignes 53-60)
   ```java
   URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + API_KEY);
   HttpURLConnection conn = (HttpURLConnection) url.openConnection();
   conn.setRequestMethod("POST");
   conn.setRequestProperty("Content-Type", "application/json");
   ```

3. **Réception et parsing** (lignes 62-70)
   ```java
   JsonNode root = mapper.readTree(response);
   return root.path("candidates").get(0)
              .path("content").path("parts").get(0)
              .path("text").asText("");
   ```

### Utilisation :

```java
// Génération de titre
String title = AIService.generateTitle("Mon contenu ici");

// Génération de hashtags
String tags = AIService.generateHashtags("Mon contenu ici");
```

---

## 4. 🚩 Signalement de Publication (À implémenter)

### Architecture proposée :

#### Base de données :

```sql
CREATE TABLE publication_report (
    id INT AUTO_INCREMENT PRIMARY KEY,
    publication_id INT NOT NULL,
    user_id INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
```

#### Service Java :

```java
public class ReportService {
    public boolean reportPublication(int pubId, int userId, String reason, String description) {
        String sql = "INSERT INTO publication_report (publication_id, user_id, reason, description) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, pubId);
            ps.setInt(2, userId);
            ps.setString(3, reason);
            ps.setString(4, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Report> getPendingReports() {
        // SELECT * FROM publication_report WHERE status='PENDING'
    }
}
```

#### Interface utilisateur :

```java
// Dans createPublicationCard()
Button reportBtn = makeBtn("🚩 Signaler", "#FFF3CD", "#856404");
reportBtn.setOnAction(e -> {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Signaler la publication");
    dialog.setHeaderText("Raison du signalement");
    dialog.setContentText("Motif:");
    
    Optional<String> result = dialog.showAndWait();
    result.ifPresent(reason -> {
        if (reportService.reportPublication(p.getId(), SessionManager.getCurrentUser().getId(), reason, "")) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Publication signalée.");
        }
    });
});
```

---

## 5. 🙈 Masquage de Publication (À implémenter)

### Option 1 : Masquage côté utilisateur (soft delete)

#### Base de données :

```sql
CREATE TABLE publication_hidden (
    id INT AUTO_INCREMENT PRIMARY KEY,
    publication_id INT NOT NULL,
    user_id INT NOT NULL,
    hidden_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY unique_hide (publication_id, user_id)
);
```

#### Service :

```java
public boolean hidePublication(int pubId, int userId) {
    String sql = "INSERT INTO publication_hidden (publication_id, user_id) VALUES (?,?)";
    // ...
}

public List<Publication> getVisiblePublications(int userId) {
    String sql = "SELECT p.* FROM publication p " +
                 "WHERE p.id NOT IN (SELECT publication_id FROM publication_hidden WHERE user_id=?)";
    // ...
}
```

### Option 2 : Masquage admin (hard delete)

```java
// Ajouter colonne dans publication
ALTER TABLE publication ADD COLUMN is_hidden TINYINT(1) DEFAULT 0;

// Service
public boolean toggleHidden(int pubId, boolean hidden) {
    String sql = "UPDATE publication SET is_hidden=? WHERE id=?";
    // ...
}

// Dans loadFeed()
String sql = "SELECT * FROM publication WHERE is_hidden=0 ORDER BY created_at DESC";
```

---

## 📝 Résumé des APIs utilisées

| API | Type | Clé requise | Usage |
|-----|------|-------------|-------|
| **profanity.dev** | Externe | ❌ Non | Détection bad words |
| **Google Gemini** | Externe | ✅ Oui | Génération IA (titres/hashtags) |
| **Notifications** | Locale | ❌ Non | Polling base de données |

---

## 🔧 Comment créer ta propre API ?

### Exemple : API de signalement

1. **Créer le modèle** (`Report.java`)
2. **Créer le service** (`ReportService.java`)
3. **Créer la table SQL**
4. **Ajouter l'interface utilisateur** (bouton + dialog)
5. **Tester**

Veux-tu que j'implémente le signalement et le masquage maintenant ?
