# 🏆 NutriLife Badge System - Documentation

## Overview

The NutriLife badge system has been redesigned to be **smarter, more motivating, and more relevant** for a nutrition and fitness application. Badges reward healthy behaviors and real user engagement.

---

## 🎯 Badge Categories

### 1. **Getting Started**
Badges for new users setting up their account.

| Badge | Description | Condition | Rarity |
|-------|-------------|-----------|--------|
| 🌟 Welcome! | Join NutriLife | Create an account | Common |
| ✅ Profile Ready | Complete profile | Fill all fields | Common |
| 🔐 Face Unlocked | Biometric security | Activate Face ID | Rare |
| 📸 Say Cheese! | Profile photo | Upload a photo | Common |

### 2. **Weight Tracking**
Rewards consistency in weight tracking.

| Badge | Description | Condition | Rarity |
|-------|-------------|-----------|--------|
| ⚖️ First Weigh-In | First weigh-in | 1 weight entry | Common |
| 📊 Consistent Tracker | Regular tracking | 5 entries | Common |
| 💪 Dedicated Logger | Strong commitment | 10 entries | Rare |
| 🏋️ Weight Warrior | Tracking master | 30 entries | Epic |
| 📈 Data Champion | Tracking expert | 50 entries | Legendary |

### 3. **Goals & Progress**
Badges related to weight goals and progress achieved.

| Badge | Description | Condition | Rarity |
|-------|-------------|-----------|--------|
| 🎯 Goal Setter | Set a goal | Create a weight goal | Common |
| 🔥 Halfway Hero | Halfway there | 50% of goal reached | Rare |
| 🏆 Goal Crusher | Goal achieved | 100% of goal | Legendary |
| 📉 3kg Milestone | 3kg progress | 3kg progression | Common |
| 🎖️ 5kg Achiever | 5kg progress | 5kg progression | Common |
| 🥇 10kg Champion | 10kg progress | 10kg progression | Epic |
| 👑 20kg Legend | 20kg progress | 20kg progression | Legendary |

### 4. **Consistency & Habits**
Rewards daily activity streaks.

| Badge | Description | Condition | Rarity |
|-------|-------------|-----------|--------|
| 🔥 On a Roll | 3 consecutive days | 3-day streak | Common |
| ⚡ Week Warrior | 7 consecutive days | 7-day streak | Rare |
| 💥 Two Week Titan | 14 consecutive days | 14-day streak | Rare |
| 💎 Monthly Master | 30 consecutive days | 30-day streak | Legendary |

### 5. **Health & Wellness**
Badges related to overall health and long-term engagement.

| Badge | Description | Condition | Rarity |
|-------|-------------|-----------|--------|
| 💚 Healthy BMI | Healthy BMI | BMI between 18.5 and 24.9 | Epic |
| 🎖️ One Month In | 30 days of use | 30 days since registration | Rare |
| 🏅 Veteran Member | 90 days of use | 90 days since registration | Epic |
| 🌟 Lifestyle Legend | 1 year of use | 365 days since registration | Legendary |

### 6. **Engagement & Community**
Badges for interaction with app features.

| Badge | Description | Condition | Rarity |
|-------|-------------|-----------|--------|
| 🎮 Arena Rookie | First points | 100 arena points | Common |
| ⚔️ Arena Warrior | Active competitor | 500 arena points | Rare |
| 🏆 Arena Champion | Dominator | 1000 arena points | Epic |
| 🎯 Challenge Starter | First challenge | 1 weekly challenge completed | Common |
| 🏅 Challenge Master | Challenge master | 10 challenges completed | Epic |

### 7. **Special Achievements**
Unique badges for specific actions.

| Badge | Description | Condition | Rarity |
|-------|-------------|-----------|--------|
| 🌅 Early Bird | Morning weigh-in | Log before 8 AM | Rare |
| 🦉 Night Owl | Night weigh-in | Log after 10 PM | Rare |
| ✨ Perfect Week | Perfect week | 7 consecutive days of logs | Epic |
| 🎊 Comeback Kid | Return after break | Return after 30 days of inactivity | Rare |

---

## 🎨 Rarity System

Badges have 4 rarity levels:

- **Common**: Easy to obtain, encourages first steps
- **Rare**: Requires regular engagement
- **Epic**: Reserved for dedicated users
- **Legendary**: Most difficult, for champions

---

## 🎁 Rewards and Motivation

Each unlocked badge displays:
- ✅ **Unlock date**
- 🎁 **Personalized reward message**
- ⭐ **Ability to pin in showcase** (max 3 badges)

### Badge Showcase
Users can pin up to **3 badges** in their showcase to proudly display them on their profile.

---

## 🎯 Real-Time Progression

The system displays:
- **Unlocked badges**: With date and reward
- **In-progress badges**: With progress bar and tips
- **Locked badges**: With hints to unlock them

---

## 🎮 Integration with Arena

Badges are linked to the **Rank** and **XP** system:
- Each unlocked badge = **Bonus XP**
- More badges = **Higher rank**
- Automatic **Level Up** notifications

---

## 🚀 Improvements over the old system

### ❌ Removed (not relevant)
- ❌ "Create a recipe" badges (users don't create recipes)
- ❌ "Tracked ingredients" badges (not logical for the app)
- ❌ Useless and irrelevant actions

### ✅ Added (smart & creative)
- ✅ **Weight progression** badges (3kg, 5kg, 10kg, 20kg)
- ✅ **Advanced streak** badges (14 days, 30 days)
- ✅ **Engagement** badges (arena points, challenges)
- ✅ **Special** badges (early bird, night owl, perfect week, comeback)
- ✅ **Longevity** badges (30 days, 90 days, 1 year)
- ✅ More **rarity** levels for more challenge

---

## 📊 Statistics

The system displays:
- **Total badges** available
- **Completion percentage**
- **Next badge** to unlock with tips
- **Current rank** and XP

---

## 🔄 Automatic Refresh

Badges are automatically checked and unlocked:
- Every time the Badges page is opened
- After each relevant action (weight log, etc.)
- Toast notifications for new badges

---

## 🎊 Level Up Notifications

When a user unlocks enough badges to level up:
- 🎊 **Animated popup** with the new rank
- 🎨 **Rank emoji and color**
- 💬 **Personalized motivational message**

---

## 🛠️ Technical Implementation

### Main classes
- `Badge.java`: Badge model
- `UserBadge.java`: User-badge relationship with progression
- `BadgeService.java`: Business logic and calculations
- `BadgeRepository.java`: Database access
- `BadgesController.java`: JavaFX user interface

### Supported condition types
```java
- weight_logs         // Number of weight logs
- streak_days         // Consecutive activity days
- kg_progress         // Progress in kg
- objective_set       // Goal set
- objective_50pct     // 50% of goal
- objective_100pct    // 100% of goal
- bmi_normal          // Healthy BMI
- account_age_days    // Account age in days
- arena_points        // Arena points
- challenges_done     // Completed challenges
- early_morning_log   // Morning log
- late_night_log      // Night log
- perfect_week        // Perfect week
- comeback            // Return after break
```

---

## 🎯 Next Steps

To test the system:
1. ✅ Compile the project
2. ✅ Launch the application
3. ✅ Login with an account
4. ✅ Open the "Badges" page
5. ✅ Check automatically unlocked badges
6. ✅ Log weight to unlock new badges
7. ✅ Test the showcase (pin/unpin)
8. ✅ Check level up notifications

---

**Final version ready for testing! 🚀**
