# 🥗 NutriLife - Nutrition Management System

A comprehensive nutrition tracking and management application built with JavaFX and integrated with a Symfony web platform.

## 📋 Overview

NutriLife is a desktop application that helps users track their nutrition goals, log daily meals, and monitor their progress. It features both user and admin interfaces with cross-platform synchronization with a Symfony web application.

## ✨ Features

### 👤 User Features
- **Nutrition Objectives**: Create and manage personalized nutrition plans
  - Multiple goal types: Lose Weight, Gain Weight, Maintain, Build Muscle, Clean Eating
  - Customizable intensity levels: Light, Moderate, Intense
  - 7-day tracking system
  
- **Daily Meal Logging**: Track meals across 4 meal types
  - Breakfast, Lunch, Dinner, Snacks
  - Pre-defined food database with macros
  - Custom food entry with full macro tracking
  - AI-powered food recognition (image upload)
  
- **Progress Tracking**:
  - Real-time calorie and macro tracking
  - Visual progress indicators
  - Mood and notes logging
  - Weight tracking with photo logs
  
- **Gamification**:
  - Badge system for achievements
  - Progress milestones
  - User galleries

### 👨‍💼 Admin Features
- **User Management**: View and manage all users
- **Objective Management**: Monitor and edit user nutrition plans
- **Daily Log Management**: 
  - View all user meal logs
  - Edit/add/remove foods
  - Adjust macros
  - Cross-platform food synchronization
  
- **Analytics Dashboard**: Track user progress and anomalies
- **Messaging System**: Send personalized messages to users
- **Complaint Management**: Handle user feedback and issues

## 🛠️ Technology Stack

- **Frontend**: JavaFX 17
- **Backend**: Java 17
- **Database**: MySQL (nutrilife_db)
- **Build Tool**: Maven
- **Integration**: Symfony Web Platform (cross-platform data sync)

## 📦 Prerequisites

- Java JDK 17 or higher
- Maven 3.6+
- MySQL 8.0+
- JavaFX SDK 17+

## 🚀 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/nutrilife.git
   cd nutrilife
   ```

2. **Configure Database**
   - Create MySQL database: `nutrilife_db`
   - Update database credentials in `src/main/java/tn/esprit/projet/utils/MyBDConnexion.java`
   ```java
   private static final String USER = "root";
   private static final String PASSWORD = "your_password";
   ```

3. **Install Dependencies**
   ```bash
   mvn clean install
   ```

4. **Run the Application**
   ```bash
   mvn javafx:run
   ```

## 🗄️ Database Schema

The application automatically creates the following tables on first run:
- `user` - User accounts and profiles
- `nutrition_objective` - Nutrition plans
- `daily_log` - Daily meal tracking
- `complaint` - User feedback
- `badge` - Achievement system
- `weight_log` - Weight tracking
- `progress_photo` - Progress photos
- And more...

## 🔧 Configuration

### Database Connection
Edit `MyBDConnexion.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/nutrilife_db?serverTimezone=UTC&sslMode=DISABLED&createDatabaseIfNotExist=true";
```

### Default Admin Account
The system creates a default admin account on first run:
- Email: `admin@nutrilife.com`
- Password: `admin123`

## 📱 Cross-Platform Integration

NutriLife seamlessly integrates with the Symfony web platform:
- **Food Synchronization**: Foods logged on web appear in desktop app
- **Objective Sync**: Nutrition plans sync across platforms
- **Real-time Updates**: Changes reflect immediately

## 🎯 Usage

### For Users
1. **Create Account**: Register with email and password
2. **Set Objective**: Choose your nutrition goal and intensity
3. **Log Meals**: Track your daily food intake
4. **Monitor Progress**: View your progress dashboard

### For Admins
1. **Login**: Use admin credentials
2. **Manage Users**: View and edit user profiles
3. **Monitor Objectives**: Track user nutrition plans
4. **Manage Logs**: Edit meal logs and adjust macros

## 🏗️ Project Structure

```
src/main/java/tn/esprit/projet/
├── controllers/        # REST controllers
├── dao/               # Data Access Objects
├── gui/               # JavaFX controllers
├── models/            # Entity models
├── repository/        # Data repositories
├── security/          # Authentication & security
├── services/          # Business logic
├── utils/             # Utility classes
└── MainApp.java       # Application entry point

src/main/resources/
├── fxml/              # JavaFX layouts
├── css/               # Stylesheets
└── images/            # Application images
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 Recent Updates

### Latest Changes
- ✅ Added Symfony food format parsing for cross-platform compatibility
- ✅ Fixed objective plan name display (e.g., "Gain Weight — Moderate")
- ✅ Added visual feedback for goal selection (green borders)
- ✅ Fixed progress bar to show 0% on new objectives
- ✅ Added database refresh for fresh data loading
- ✅ Fixed admin food editing and custom food synchronization
- ✅ Improved food macro tracking with per-food data

## 🐛 Known Issues

- None currently reported

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **Your Name** - *Initial work*

## 🙏 Acknowledgments

- ESPRIT University
- JavaFX Community
- Symfony Framework

## 📞 Support

For support, email support@nutrilife.com or open an issue in the repository.

---

**Note**: This is an academic project developed as part of the curriculum at ESPRIT.
