# 🏗️ Architecture du Backoffice Admin - NutriLife

## 📐 Vue d'Ensemble

```
┌─────────────────────────────────────────────────────────────────┐
│                    BACKOFFICE ADMIN NUTRILIFE                   │
└─────────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                │                               │
        ┌───────▼────────┐            ┌────────▼────────┐
        │  Admin User    │            │  Admin User     │
        │  List          │            │  Show           │
        └───────┬────────┘            └────────┬────────┘
                │                               │
    ┌───────────┼───────────────────────────────┼───────────┐
    │           │                               │           │
    │           │                               │           │
┌───▼───┐  ┌───▼───┐  ┌────▼────┐  ┌────▼────┐  ┌───▼────┐
│Badges │  │Message│  │Progress │  │Gallery  │  │ Edit   │
│Module │  │Module │  │Module   │  │Module   │  │ User   │
└───┬───┘  └───┬───┘  └────┬────┘  └────┬────┘  └────────┘
    │          │           │            │
    │          │           │            │
┌───▼──────────▼───────────▼────────────▼───────────────────┐
│                    DATABASE LAYER                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │  badge   │  │  admin_  │  │  weight_ │  │ gallery  │ │
│  │user_badge│  │ messages │  │objective │  │          │ │
│  └──────────┘  └──────────┘  │weight_log│  └──────────┘ │
│                               └──────────┘                 │
└────────────────────────────────────────────────────────────┘
```

---

## 🎯 Flux de Navigation

### Depuis la Liste des Utilisateurs

```
┌─────────────────────────────────────────────────────────────┐
│                   Admin User List                           │
│  ┌────────────────────────────────────────────────────────┐ │
│  │ ID │ Email │ Name │ Role │ Status │ Actions           │ │
│  ├────────────────────────────────────────────────────────┤ │
│  │ 1  │ john@ │ John │ User │ Active │ [8 Buttons]       │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        │                 │                 │
    ┌───▼───┐      ┌──────▼──────┐    ┌───▼────┐
    │ View  │      │   Badges    │    │Message │
    │ User  │      │   Module    │    │ Module │
    └───┬───┘      └─────────────┘    └────────┘
        │
    ┌───▼───┐      ┌──────────────┐   ┌────────┐
    │Edit   │      │   Progress   │   │Gallery │
    │Toggle │      │   Module     │   │ Module │
    │Delete │      └──────────────┘   └────────┘
    └───────┘
```

### Depuis la Page Utilisateur

```
┌─────────────────────────────────────────────────────────────┐
│                   Admin User Show                           │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  👤 John Doe                                           │ │
│  │  john@mail.com  |  User  |  Active                    │ │
│  │                                                        │ │
│  │  [Edit Profile]                                        │ │
│  │  [🏆 View Badges]     ──────────────┐                 │ │
│  │  [💬 Send Message]    ──────────┐   │                 │ │
│  │  [📊 View Progress]   ──────┐   │   │                 │ │
│  │  [🖼️ View Gallery]    ──┐   │   │   │                 │ │
│  │  [Back]                 │   │   │   │                 │ │
│  └─────────────────────────┼───┼───┼───┼─────────────────┘ │
└────────────────────────────┼───┼───┼───┼───────────────────┘
                             │   │   │   │
                    ┌────────┘   │   │   └────────┐
                    │            │   │            │
              ┌─────▼─────┐ ┌────▼───▼───┐ ┌─────▼─────┐
              │  Gallery  │ │  Progress  │ │  Badges   │
              │  Module   │ │   Module   │ │  Module   │
              └───────────┘ └────────────┘ └───────────┘
                                 │
                          ┌──────▼──────┐
                          │   Message   │
                          │   Module    │
                          └─────────────┘
```

---

## 📦 Structure des Modules

### Module Badges

```
┌─────────────────────────────────────────────────────────────┐
│              AdminUserBadgesController                      │
├─────────────────────────────────────────────────────────────┤
│  Components:                                                │
│  • lblUserName, lblTotalBadges, lblUnlockedCount           │
│  • progressBar, searchField                                 │
│  • filterCategory, filterRarity                             │
│  • unlockedList, inProgressList, lockedList                │
├─────────────────────────────────────────────────────────────┤
│  Methods:                                                   │
│  • setUser(User)                                            │
│  • loadBadges()                                             │
│  • applyFilters()                                           │
│  • displayBadges(BadgesDisplay)                             │
│  • buildBadgeCard(UserBadge, String)                        │
│  • handleRefresh()                                          │
│  • handleExportReport()                                     │
├─────────────────────────────────────────────────────────────┤
│  Dependencies:                                              │
│  • BadgeRepository                                          │
│  • BadgeService                                             │
│  • User, UserBadge models                                   │
└─────────────────────────────────────────────────────────────┘
```

### Module Messages

```
┌─────────────────────────────────────────────────────────────┐
│            AdminUserMessagesController                      │
├─────────────────────────────────────────────────────────────┤
│  Components:                                                │
│  • lblUserName, messageText                                 │
│  • messageTemplate, markAsImportant                         │
│  • messageHistoryList, lblMessageCount                      │
├─────────────────────────────────────────────────────────────┤
│  Methods:                                                   │
│  • setUser(User)                                            │
│  • loadMessageHistory()                                     │
│  • buildMessageCard(AdminMessage)                           │
│  • handleSendMessage()                                      │
│  • handleClear()                                            │
│  • saveMessage(int, String, boolean)                        │
│  • getMessagesForUser(int)                                  │
│  • toggleReadStatus(int, boolean)                           │
│  • deleteMessage(int)                                       │
├─────────────────────────────────────────────────────────────┤
│  Database:                                                  │
│  • admin_messages table                                     │
│  • Auto-create if not exists                                │
├─────────────────────────────────────────────────────────────┤
│  Templates:                                                 │
│  • 9 predefined message templates                           │
└─────────────────────────────────────────────────────────────┘
```

### Module Progress

```
┌─────────────────────────────────────────────────────────────┐
│            AdminUserProgressController                      │
├─────────────────────────────────────────────────────────────┤
│  Components:                                                │
│  • lblUserName, lblCurrentWeight, lblCurrentBMI            │
│  • lblTotalLogs, lblActiveObjectives                        │
│  • objectivesList, weightLogsList                           │
│  • weightChart (LineChart)                                  │
├─────────────────────────────────────────────────────────────┤
│  Methods:                                                   │
│  • setUser(User)                                            │
│  • loadObjectives()                                         │
│  • loadWeightLogs()                                         │
│  • loadWeightChart()                                        │
│  • buildObjectiveCard(WeightObjective)                      │
│  • buildWeightLogCard(WeightLog)                            │
│  • handleRefresh()                                          │
│  • handleExportReport()                                     │
├─────────────────────────────────────────────────────────────┤
│  Database:                                                  │
│  • weight_objective table                                   │
│  • weight_log table                                         │
├─────────────────────────────────────────────────────────────┤
│  Features:                                                  │
│  • Progress calculation                                     │
│  • Weight change indicators                                 │
│  • Interactive chart                                        │
└─────────────────────────────────────────────────────────────┘
```

### Module Gallery

```
┌─────────────────────────────────────────────────────────────┐
│            AdminUserGalleryController                       │
├─────────────────────────────────────────────────────────────┤
│  Components:                                                │
│  • lblUserName, lblTotalImages                              │
│  • lblActiveImages, lblInactiveImages                       │
│  • galleryGrid (FlowPane)                                   │
│  • filterStatus, searchField                                │
├─────────────────────────────────────────────────────────────┤
│  Methods:                                                   │
│  • setUser(User)                                            │
│  • loadGallery()                                            │
│  • applyFilters()                                           │
│  • buildImageCard(GalleryImage)                             │
│  • handleActivateAll()                                      │
│  • handleDeactivateAll()                                    │
│  • handleRefresh()                                          │
│  • toggleImageStatus(int, boolean)                          │
│  • deleteImage(int)                                         │
├─────────────────────────────────────────────────────────────┤
│  Database:                                                  │
│  • gallery table                                            │
│  • Auto-create if not exists                                │
├─────────────────────────────────────────────────────────────┤
│  Features:                                                  │
│  • Image preview                                            │
│  • Bulk operations                                          │
│  • Status management                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Schéma de Base de Données

```
┌─────────────────────────────────────────────────────────────┐
│                        DATABASE                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐         ┌──────────────┐                │
│  │     user     │◄────────┤    badge     │                │
│  ├──────────────┤         ├──────────────┤                │
│  │ id (PK)      │         │ id (PK)      │                │
│  │ email        │         │ nom          │                │
│  │ first_name   │         │ description  │                │
│  │ last_name    │         │ condition_*  │                │
│  │ weight       │         │ svg          │                │
│  │ height       │         │ couleur      │                │
│  │ ...          │         │ rarete       │                │
│  └──────┬───────┘         └──────────────┘                │
│         │                                                  │
│         │                 ┌──────────────┐                │
│         ├─────────────────┤ user_badge   │                │
│         │                 ├──────────────┤                │
│         │                 │ id (PK)      │                │
│         │                 │ user_id (FK) │                │
│         │                 │ badge_id (FK)│                │
│         │                 │ unlocked     │                │
│         │                 │ current_value│                │
│         │                 │ is_vitrine   │                │
│         │                 └──────────────┘                │
│         │                                                  │
│         │                 ┌──────────────┐                │
│         ├─────────────────┤admin_messages│ ✨ NEW         │
│         │                 ├──────────────┤                │
│         │                 │ id (PK)      │                │
│         │                 │ user_id (FK) │                │
│         │                 │ message      │                │
│         │                 │ is_important │                │
│         │                 │ is_read      │                │
│         │                 │ sent_at      │                │
│         │                 └──────────────┘                │
│         │                                                  │
│         │                 ┌──────────────┐                │
│         ├─────────────────┤weight_objective              │
│         │                 ├──────────────┤                │
│         │                 │ id (PK)      │                │
│         │                 │ user_id (FK) │                │
│         │                 │ start_weight │                │
│         │                 │ target_weight│                │
│         │                 │ is_active    │                │
│         │                 └──────────────┘                │
│         │                                                  │
│         │                 ┌──────────────┐                │
│         ├─────────────────┤ weight_log   │                │
│         │                 ├──────────────┤                │
│         │                 │ id (PK)      │                │
│         │                 │ user_id (FK) │                │
│         │                 │ weight       │                │
│         │                 │ logged_at    │                │
│         │                 └──────────────┘                │
│         │                                                  │
│         │                 ┌──────────────┐                │
│         └─────────────────┤   gallery    │ ✨ NEW         │
│                           ├──────────────┤                │
│                           │ id (PK)      │                │
│                           │ user_id (FK) │                │
│                           │ filename     │                │
│                           │ caption      │                │
│                           │ is_active    │                │
│                           │ uploaded_at  │                │
│                           └──────────────┘                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flux de Données

### Consultation des Badges

```
User Action
    │
    ▼
AdminUserListController.handleViewBadges(User)
    │
    ▼
AdminUserBadgesController.setUser(User)
    │
    ▼
BadgeService.refreshBadges(User)
    │
    ├──► BadgeRepository.findByUser(userId)
    │        │
    │        ▼
    │    Database: SELECT * FROM user_badge WHERE user_id = ?
    │
    ├──► BadgeService.calculateCurrentValue(User, conditionType)
    │        │
    │        ▼
    │    Various calculations (weight_logs, streak, etc.)
    │
    └──► BadgeRepository.unlock(userBadgeId) if condition met
         │
         ▼
    Database: UPDATE user_badge SET unlocked = 1
    │
    ▼
AdminUserBadgesController.displayBadges(BadgesDisplay)
    │
    ▼
UI Update: Show badges in 3 tabs (unlocked, in progress, locked)
```

### Envoi de Message

```
User Action: Click "Send Message"
    │
    ▼
AdminUserMessagesController.handleSendMessage()
    │
    ├──► Validate message text
    │
    ├──► Get template if selected
    │
    ├──► Get importance flag
    │
    └──► saveMessage(userId, message, important)
         │
         ▼
    Database: INSERT INTO admin_messages (user_id, message, is_important, sent_at)
    │
    ▼
loadMessageHistory()
    │
    ▼
Database: SELECT * FROM admin_messages WHERE user_id = ? ORDER BY sent_at DESC
    │
    ▼
UI Update: Display message history with status badges
```

### Consultation de la Progression

```
User Action: Click "Progress"
    │
    ▼
AdminUserProgressController.setUser(User)
    │
    ├──► loadObjectives()
    │        │
    │        ▼
    │    Database: SELECT * FROM weight_objective WHERE user_id = ?
    │        │
    │        ▼
    │    Calculate progress percentage
    │        │
    │        ▼
    │    Display objective cards with progress bars
    │
    ├──► loadWeightLogs()
    │        │
    │        ▼
    │    Database: SELECT * FROM weight_log WHERE user_id = ? ORDER BY logged_at DESC
    │        │
    │        ▼
    │    Calculate weight changes (↑ ↓)
    │        │
    │        ▼
    │    Display last 10 logs
    │
    └──► loadWeightChart()
         │
         ▼
    Create LineChart with all weight data points
         │
         ▼
    UI Update: Display interactive chart
```

### Gestion de la Galerie

```
User Action: Click "Gallery"
    │
    ▼
AdminUserGalleryController.setUser(User)
    │
    ▼
loadGallery()
    │
    ▼
Database: SELECT * FROM gallery WHERE user_id = ? ORDER BY uploaded_at DESC
    │
    ▼
For each image:
    │
    ├──► Load image file from uploads/gallery/
    │
    ├──► Create ImageView with preview
    │
    ├──► Add status badge (Active/Inactive)
    │
    ├──► Add action buttons (Activate/Deactivate, Delete)
    │
    └──► Display in FlowPane grid
         │
         ▼
    UI Update: Gallery grid with all images

User Action: Toggle image status
    │
    ▼
toggleImageStatus(imageId, newStatus)
    │
    ▼
Database: UPDATE gallery SET is_active = ? WHERE id = ?
    │
    ▼
Reload gallery
```

---

## 🎨 Hiérarchie des Composants UI

```
Stage (Modal Window)
    │
    └── Scene
         │
         └── Root (VBox/BorderPane)
              │
              ├── Header
              │    ├── Title Label
              │    └── Statistics Labels
              │
              ├── Filters Section
              │    ├── ComboBox (Category/Status)
              │    ├── ComboBox (Rarity)
              │    └── TextField (Search)
              │
              ├── Content Area
              │    ├── TabPane (for Badges)
              │    │    ├── Tab: Unlocked
              │    │    ├── Tab: In Progress
              │    │    └── Tab: Locked
              │    │
              │    ├── VBox (for Messages/Progress)
              │    │    └── ScrollPane
              │    │         └── VBox (message/objective cards)
              │    │
              │    └── FlowPane (for Gallery)
              │         └── VBox (image cards)
              │
              └── Footer
                   ├── Button: Refresh
                   ├── Button: Export/Action
                   └── Button: Close
```

---

## 🔐 Sécurité et Permissions

```
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY LAYER                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Admin Check:                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ if (!Session.getCurrentUser().isAdmin()) {           │  │
│  │     // Redirect to login or show error               │  │
│  │     return;                                           │  │
│  │ }                                                     │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  Database Security:                                         │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ • PreparedStatement (SQL injection protection)       │  │
│  │ • Foreign Key constraints                            │  │
│  │ • ON DELETE CASCADE for data integrity               │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  Input Validation:                                          │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ • Message text not empty                             │  │
│  │ • User exists before operations                      │  │
│  │ • File exists before loading images                  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Performance et Optimisation

```
┌─────────────────────────────────────────────────────────────┐
│                    PERFORMANCE                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Database Queries:                                          │
│  • Indexed columns (user_id, badge_id)                     │
│  • LIMIT clauses for large datasets                         │
│  • ORDER BY for sorted results                              │
│                                                             │
│  UI Rendering:                                              │
│  • Lazy loading for images                                  │
│  • Pagination for large lists (last 10 logs)               │
│  • Efficient card building                                  │
│                                                             │
│  Memory Management:                                         │
│  • Close database connections                               │
│  • Clear lists before reload                                │
│  • Dispose of unused resources                              │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Points d'Extension

### Futures Fonctionnalités

```
1. Export Reports
   ├── PDF generation
   ├── CSV export
   └── Email reports

2. Notifications
   ├── Push notifications
   ├── Email notifications
   └── In-app notifications

3. Analytics
   ├── User engagement metrics
   ├── Badge completion rates
   └── Gallery activity

4. Bulk Operations
   ├── Bulk message sending
   ├── Bulk badge management
   └── Bulk user operations

5. Advanced Filters
   ├── Date range filters
   ├── Custom queries
   └── Saved filters
```

---

## 🎊 Conclusion

L'architecture du backoffice admin est :

✅ **Modulaire** - 4 modules indépendants  
✅ **Scalable** - Facile à étendre  
✅ **Maintenable** - Code bien structuré  
✅ **Performante** - Optimisée pour la vitesse  
✅ **Sécurisée** - Protection contre les injections  

---

**🚀 Architecture solide et professionnelle ! Prête pour la production ! 🎉**
