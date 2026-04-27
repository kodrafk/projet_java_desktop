# 🧪 Guide de Test - Système de Badges

## 🎯 Objectif
Tester le nouveau système de badges intelligent et créatif de NutriLife.

---

## ⚡ Tests Rapides (5 minutes)

### 1. Lancer l'Application
```bash
cd projetJAV
mvn clean javafx:run
```

### 2. Se Connecter
- Utiliser un compte existant ou créer un nouveau compte

### 3. Ouvrir la Page Badges
- Cliquer sur "Badges" dans le menu principal
- **Attendu** : Voir les badges organisés en 3 sections :
  - 🏆 Badges débloqués
  - 📊 Badges en cours
  - 🔒 Badges verrouillés

### 4. Vérifier les Badges Automatiques
**Badges qui devraient être débloqués automatiquement :**
- ✅ 🌟 **Welcome!** (compte créé)
- ✅ ✅ **Profile Ready** (si profil complet)
- ✅ 📸 **Say Cheese!** (si photo uploadée)
- ✅ 🔐 **Face Unlocked** (si Face ID activé)

### 5. Tester le Déblocage de Badge
**Action** : Logger un poids
1. Aller dans "Weight Tracking"
2. Ajouter un nouveau log de poids
3. Retourner dans "Badges"
4. **Attendu** : 
   - 🎉 Notification toast "Badge unlocked: ⚖️ First Weigh-In"
   - Badge visible dans la section "Débloqués"
   - Date de déblocage affichée
   - Message de récompense visible

---

## 🔍 Tests Détaillés (15 minutes)

### Test 1 : Progression des Badges de Poids

| Action | Badge Attendu | Vérification |
|--------|---------------|--------------|
| Log 1 poids | ⚖️ First Weigh-In | ✅ Débloqué |
| Log 5 poids | 📊 Consistent Tracker | ✅ Débloqué |
| Log 10 poids | 💪 Dedicated Logger | ✅ Débloqué |
| Log 30 poids | 🏋️ Weight Warrior | ✅ Débloqué |
| Log 50 poids | 📈 Data Champion | ✅ Débloqué |

**Comment tester rapidement** :
```sql
-- Insérer plusieurs logs de test (dans la base de données)
INSERT INTO weight_log (user_id, weight, logged_at) VALUES 
(1, 75.5, NOW()),
(1, 75.3, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 75.1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 74.9, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 74.7, DATE_SUB(NOW(), INTERVAL 4 DAY));
```

---

### Test 2 : Badges d'Objectifs

| Action | Badge Attendu |
|--------|---------------|
| Créer un objectif de poids | 🎯 Goal Setter |
| Atteindre 3kg de progrès | 📉 3kg Milestone |
| Atteindre 5kg de progrès | 🎖️ 5kg Achiever |
| Atteindre 50% de l'objectif | 🔥 Halfway Hero |
| Atteindre 100% de l'objectif | 🏆 Goal Crusher |

**Comment tester** :
1. Aller dans "Weight Objectives"
2. Créer un objectif (ex: passer de 80kg à 70kg)
3. Logger des poids progressifs
4. Vérifier les badges dans la page Badges

---

### Test 3 : Badges de Streak

| Jours Consécutifs | Badge Attendu |
|-------------------|---------------|
| 3 jours | 🔥 On a Roll |
| 7 jours | ⚡ Week Warrior |
| 14 jours | 💥 Two Week Titan |
| 30 jours | 💎 Monthly Master |

**Comment tester** :
```sql
-- Créer un streak de 7 jours
INSERT INTO weight_log (user_id, weight, logged_at) VALUES 
(1, 75.0, DATE_SUB(NOW(), INTERVAL 0 DAY)),
(1, 75.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 75.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 75.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 75.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 75.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 75.0, DATE_SUB(NOW(), INTERVAL 6 DAY));
```

---

### Test 4 : Badges Spéciaux

#### 🌅 Early Bird
**Action** : Logger un poids avant 8h du matin
```sql
INSERT INTO weight_log (user_id, weight, logged_at) 
VALUES (1, 75.0, CONCAT(CURDATE(), ' 07:30:00'));
```

#### 🦉 Night Owl
**Action** : Logger un poids après 22h
```sql
INSERT INTO weight_log (user_id, weight, logged_at) 
VALUES (1, 75.0, CONCAT(CURDATE(), ' 22:30:00'));
```

#### ✨ Perfect Week
**Action** : Logger un poids chaque jour pendant 7 jours consécutifs
- Vérifier que tous les jours de la semaine ont un log

#### 🎊 Comeback Kid
**Action** : Revenir après 30 jours d'inactivité
1. Ne pas logger pendant 30 jours
2. Logger à nouveau
3. Badge débloqué

---

### Test 5 : Badges d'Arène

| Points | Badge Attendu |
|--------|---------------|
| 100 points | 🎮 Arena Rookie |
| 500 points | ⚔️ Arena Warrior |
| 1000 points | 🏆 Arena Champion |

**Comment tester** :
1. Aller dans "Arena" ou "Roue"
2. Tourner la roue plusieurs fois
3. Accumuler des points
4. Vérifier les badges

---

### Test 6 : Vitrine de Badges

**Fonctionnalité** : Épingler jusqu'à 3 badges dans la vitrine

**Actions** :
1. Débloquer plusieurs badges
2. Cliquer sur "☆ Pin" sur un badge débloqué
3. **Attendu** : Badge apparaît dans la vitrine en haut
4. Épingler 2 autres badges
5. **Attendu** : 3 badges dans la vitrine
6. Essayer d'épingler un 4ème badge
7. **Attendu** : Message d'erreur "Max 3 badges"
8. Cliquer sur "⭐ Pinned" pour désépingler
9. **Attendu** : Badge retiré de la vitrine

---

### Test 7 : Recherche de Badges

**Actions** :
1. Dans la page Badges, utiliser le champ de recherche
2. Taper "weight" → Voir tous les badges liés au poids
3. Taper "streak" → Voir tous les badges de streak
4. Taper "legendary" → Voir tous les badges légendaires
5. Taper "goal" → Voir tous les badges d'objectifs

---

### Test 8 : Progression en Temps Réel

**Actions** :
1. Noter le nombre de badges débloqués
2. Logger un poids
3. Rafraîchir la page Badges
4. **Attendu** :
   - Barre de progression mise à jour
   - Pourcentage de complétion augmenté
   - Badge "en cours" mis à jour avec nouvelle progression
   - Notification si badge débloqué

---

### Test 9 : Level Up

**Actions** :
1. Débloquer plusieurs badges
2. **Attendu** : Popup animée "🎊 LEVEL UP!"
3. Vérifier :
   - Emoji du nouveau rank
   - Titre du rank
   - Message de motivation
   - Bouton "Let's go!"

---

### Test 10 : Statistiques

**Vérifications** :
- ✅ Total de badges affiché
- ✅ Nombre de badges débloqués
- ✅ Nombre de badges en cours
- ✅ Nombre de badges verrouillés
- ✅ Pourcentage de complétion
- ✅ Barre de progression globale
- ✅ "Prochain badge" avec conseil

---

## 🎨 Tests Visuels

### Vérifier les Couleurs par Rareté

| Rareté | Couleur Attendue | Badge Exemple |
|--------|------------------|---------------|
| Common | Gris/Vert clair | 🌟 Welcome! |
| Rare | Bleu | 🔐 Face Unlocked |
| Epic | Violet | 💚 Healthy BMI |
| Legendary | Or/Rouge | 🏆 Goal Crusher |

### Vérifier les Animations

- ✅ Fade-in des badges débloqués
- ✅ Hover effect sur les cartes
- ✅ Animation de la popup Level Up
- ✅ Transition smooth entre les sections

---

## 🐛 Tests de Bugs Potentiels

### Bug 1 : Badges Dupliqués
**Test** : Rafraîchir plusieurs fois la page
**Attendu** : Pas de badges dupliqués

### Bug 2 : Progression Négative
**Test** : Supprimer un log de poids
**Attendu** : Progression mise à jour correctement

### Bug 3 : Vitrine Overflow
**Test** : Essayer d'épingler 4+ badges
**Attendu** : Message d'erreur, max 3 badges

### Bug 4 : Recherche Vide
**Test** : Rechercher "xyz123" (inexistant)
**Attendu** : Message "No badges found"

### Bug 5 : Badge Verrouillé Visible
**Test** : Vérifier que les badges verrouillés affichent "???" et "🔒"
**Attendu** : Nom et détails cachés

---

## 📊 Checklist Complète

### Fonctionnalités de Base
- [ ] Page Badges s'ouvre correctement
- [ ] 3 sections visibles (débloqués, en cours, verrouillés)
- [ ] Statistiques affichées en haut
- [ ] Vitrine visible (vide ou avec badges)
- [ ] Barre de recherche fonctionnelle

### Déblocage de Badges
- [ ] Badges automatiques débloqués au premier lancement
- [ ] Notification toast lors du déblocage
- [ ] Date de déblocage affichée
- [ ] Message de récompense visible
- [ ] Badge apparaît dans la section "Débloqués"

### Progression
- [ ] Barre de progression sur badges en cours
- [ ] Pourcentage affiché
- [ ] Conseil "X more to go" visible
- [ ] Progression mise à jour en temps réel

### Vitrine
- [ ] Épingler un badge fonctionne
- [ ] Max 3 badges respecté
- [ ] Désépingler fonctionne
- [ ] Vitrine affichée sur le profil

### Recherche
- [ ] Recherche par nom fonctionne
- [ ] Recherche par catégorie fonctionne
- [ ] Recherche par rareté fonctionne
- [ ] Résultats filtrés correctement

### Level Up
- [ ] Popup s'affiche lors du level up
- [ ] Animation smooth
- [ ] Informations correctes (rank, emoji, motivation)
- [ ] Bouton "Let's go!" ferme la popup

### Performance
- [ ] Page se charge rapidement (<2s)
- [ ] Pas de lag lors du scroll
- [ ] Animations fluides
- [ ] Pas d'erreurs dans la console

---

## 🎯 Résultats Attendus

### Après les Tests
- ✅ **33 badges** disponibles au total
- ✅ **4-6 badges** débloqués automatiquement (nouveau compte)
- ✅ **Progression fluide** avec jalons fréquents
- ✅ **Notifications** pour chaque badge débloqué
- ✅ **Level up** automatique quand suffisamment de badges
- ✅ **Vitrine** fonctionnelle avec max 3 badges
- ✅ **Recherche** rapide et précise
- ✅ **Aucun bug** critique

---

## 🚀 Prochaines Étapes

Si tous les tests passent :
1. ✅ Marquer la version comme **STABLE**
2. ✅ Déployer en **PRODUCTION**
3. ✅ Monitorer l'engagement utilisateur
4. ✅ Collecter les feedbacks
5. ✅ Itérer sur les améliorations

---

## 📝 Rapport de Test

### Template de Rapport

```markdown
# Rapport de Test - Badges System

**Date** : [Date]
**Testeur** : [Nom]
**Version** : 1.0-SNAPSHOT

## Tests Effectués
- [ ] Tests Rapides (5 min)
- [ ] Tests Détaillés (15 min)
- [ ] Tests Visuels
- [ ] Tests de Bugs

## Résultats
- **Badges débloqués** : X/33
- **Bugs trouvés** : X
- **Performance** : ⭐⭐⭐⭐⭐

## Bugs Identifiés
1. [Description du bug]
2. [Description du bug]

## Recommandations
- [Recommandation 1]
- [Recommandation 2]

## Conclusion
✅ Prêt pour production / ❌ Nécessite corrections
```

---

**🎊 Bon test ! Le système est prêt à impressionner les utilisateurs !**
