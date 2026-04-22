package tn.esprit.projet.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyBDConnexion {

    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String URL = "jdbc:mysql://localhost:3306/nutrilife_db?serverTimezone=UTC&sslMode=DISABLED&createDatabaseIfNotExist=true";
    private static MyBDConnexion instance;
    private Connection cnx;

    private MyBDConnexion() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion DB établie");
            initTable();
            initComplaintsTable();
            initOtherTables();
            initPublicationReportTable();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
        }
    }

    private void initPublicationReportTable() {
        String create = "CREATE TABLE IF NOT EXISTS `publication_report` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`publication_id` INT NOT NULL," +
                "`user_id` INT NOT NULL," +
                "`reason` VARCHAR(255)," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (publication_id) REFERENCES publication(id) ON DELETE CASCADE," +
                "FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (java.sql.Statement st = cnx.createStatement()) {
            st.executeUpdate(create);
            System.out.println("Table publication_report vérifiée/créée.");
        } catch (SQLException e) {
            System.err.println("[initPublicationReportTable] : " + e.getMessage());
        }
    }

    /** Ensure the user table exists with the correct schema. */
    private void initTable() {
        // Always ensure table exists first
        String create = "CREATE TABLE IF NOT EXISTS `user` (" +
                "`id`               INT AUTO_INCREMENT PRIMARY KEY," +
                "`email`            VARCHAR(180)  NOT NULL UNIQUE," +
                "`password`         VARCHAR(255)  NOT NULL," +
                "`roles`            VARCHAR(50)   NOT NULL DEFAULT 'ROLE_USER'," +
                "`is_active`        TINYINT(1)    NOT NULL DEFAULT 1," +
                "`created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`first_name`       VARCHAR(100)  NOT NULL DEFAULT ''," +
                "`last_name`        VARCHAR(100)  NOT NULL DEFAULT ''," +
                "`birthday`         DATE          NOT NULL DEFAULT '2000-01-01'," +
                "`weight`           FLOAT         DEFAULT NULL," +
                "`height`           FLOAT         DEFAULT NULL," +
                "`phone_number`     VARCHAR(20)   DEFAULT NULL," +
                "`phone_verified`   TINYINT(1)    NOT NULL DEFAULT 0," +
                "`photo_filename`   VARCHAR(255)  DEFAULT NULL," +
                "`welcome_message`  TEXT          DEFAULT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (java.sql.Statement st = cnx.createStatement()) {
            st.executeUpdate(create);
        } catch (SQLException e) {
            System.err.println("[initTable] create: " + e.getMessage());
        }

        // Add any missing columns (handles old schema upgrades)
        String[][] columns = {
                {"first_name",     "VARCHAR(100) NOT NULL DEFAULT ''"},
                {"last_name",      "VARCHAR(100) NOT NULL DEFAULT ''"},
                {"birthday",       "DATE NOT NULL DEFAULT '2000-01-01'"},
                {"weight",         "FLOAT DEFAULT NULL"},
                {"height",         "FLOAT DEFAULT NULL"},
                {"phone_number",   "VARCHAR(20) DEFAULT NULL"},
                {"phone_verified", "TINYINT(1) NOT NULL DEFAULT 0"},
                {"photo_filename", "VARCHAR(255) DEFAULT NULL"},
                {"welcome_message","TEXT DEFAULT NULL"},
        };
        for (String[] col : columns) {
            try (java.sql.PreparedStatement ps = cnx.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME=?")) {
                ps.setString(1, col[0]);
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    cnx.createStatement().executeUpdate(
                            "ALTER TABLE `user` ADD COLUMN `" + col[0] + "` " + col[1]);
                    System.out.println("[initTable] Added column: " + col[0]);
                }
            } catch (SQLException e) {
                System.err.println("[initTable] col " + col[0] + ": " + e.getMessage());
            }
        }

        // Fix roles column if it's still JSON type or has CHECK constraint
        try (java.sql.PreparedStatement ps = cnx.prepareStatement(
                "SELECT DATA_TYPE FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='user' AND COLUMN_NAME='roles'")) {
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Always force correct type — removes any CHECK constraint
                cnx.createStatement().executeUpdate(
                        "ALTER TABLE `user` MODIFY COLUMN `roles` VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER'");
                // Fix any remaining JSON-format values in existing rows
                cnx.createStatement().executeUpdate(
                        "UPDATE `user` SET `roles` = 'ROLE_ADMIN' WHERE `roles` LIKE '%ROLE_ADMIN%' AND `roles` != 'ROLE_ADMIN'");
                cnx.createStatement().executeUpdate(
                        "UPDATE `user` SET `roles` = 'ROLE_USER' WHERE `roles` NOT IN ('ROLE_USER','ROLE_ADMIN')");
            }
        } catch (SQLException e) {
            System.err.println("[initTable] roles fix: " + e.getMessage());
        }

        System.out.println("[initTable] Table ready.");
    }

    private void initComplaintsTable() {
        String create = "CREATE TABLE IF NOT EXISTS `complaint` (" +
                "`id`               INT AUTO_INCREMENT PRIMARY KEY," +
                "`user_id`          INT NOT NULL," +
                "`title`            VARCHAR(150) NOT NULL," +
                "`description`      TEXT NOT NULL," +
                "`phone_number`     VARCHAR(20)," +
                "`rate`             INT DEFAULT 0," +
                "`date_of_complaint` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`status`           VARCHAR(50) NOT NULL DEFAULT 'PENDING'," +
                "`admin_response`   TEXT," +
                "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (java.sql.Statement st = cnx.createStatement()) {
            st.executeUpdate(create);
            System.out.println("[initComplaintsTable] Complaint Table ready.");
        } catch (SQLException e) {
            System.err.println("[initComplaintsTable] error : " + e.getMessage());
        }
    }

    private void initOtherTables() {
        try (java.sql.Statement st = cnx.createStatement()) {
            // complaint_response
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `complaint_response` (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "`complaint_id` INT NOT NULL," +
                    "`response_content` TEXT NOT NULL," +
                    "`response_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (`complaint_id`) REFERENCES `complaint`(`id`) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // publication
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `publication` (" +
                    "`id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "`titre` VARCHAR(255) NOT NULL," +
                    "`contenu` LONGTEXT NOT NULL," +
                    "`description` LONGTEXT DEFAULT NULL," +
                    "`created_at` DATETIME DEFAULT NULL," +
                    "`author_name` VARCHAR(255) DEFAULT NULL," +
                    "`author_avatar` VARCHAR(255) DEFAULT NULL," +
                    "`is_admin` TINYINT(4) DEFAULT NULL," +
                    "`image` VARCHAR(255) DEFAULT NULL," +
                    "`view_count` INT(11) DEFAULT NULL," +
                    "`share_count` INT(11) DEFAULT NULL," +
                    "`visibility` VARCHAR(20) NOT NULL DEFAULT 'public'," +
                    "`scheduled_at` DATETIME DEFAULT NULL," +
                    "`shared_from_id` INT(11) DEFAULT NULL," +
                    "`user_id` INT(11) DEFAULT NULL," +
                    "CONSTRAINT `fk_pub_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL," +
                    "CONSTRAINT `fk_pub_shared` FOREIGN KEY (`shared_from_id`) REFERENCES `publication` (`id`) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // publication_comment
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `publication_comment` (" +
                    "`id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "`contenu` LONGTEXT NOT NULL," +
                    "`created_at` DATETIME DEFAULT NULL," +
                    "`author_name` VARCHAR(255) DEFAULT NULL," +
                    "`author_avatar` VARCHAR(255) DEFAULT NULL," +
                    "`is_admin` TINYINT(4) DEFAULT NULL," +
                    "`publication_id` INT(11) NOT NULL," +
                    "`user_id` INT(11) DEFAULT NULL," +
                    "CONSTRAINT `fk_com_pub` FOREIGN KEY (`publication_id`) REFERENCES `publication` (`id`) ON DELETE CASCADE," +
                    "CONSTRAINT `fk_com_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // publication_like
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `publication_like` (" +
                    "`id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                    "`created_at` DATETIME DEFAULT NULL," +
                    "`publication_id` INT(11) NOT NULL," +
                    "`user_id` INT(11) DEFAULT NULL," +
                    "`is_like` TINYINT(1) NOT NULL DEFAULT 1," +
                    "CONSTRAINT `fk_like_pub` FOREIGN KEY (`publication_id`) REFERENCES `publication` (`id`) ON DELETE CASCADE," +
                    "CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Ensure is_like column exists for older database versions
            try {
                st.executeUpdate("ALTER TABLE `publication_like` ADD COLUMN `is_like` TINYINT(1) NOT NULL DEFAULT 1");
                System.out.println("✅ Colonne 'is_like' ajoutée avec succès à la table publication_like");
            } catch (SQLException e) {
                // Ignore if column already exists
                if (!e.getMessage().contains("Duplicate column name")) {
                    System.err.println("Erreur mineure ALTER TABLE: " + e.getMessage());
                }
            }

            // recette
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `recette` (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "`nom` VARCHAR(150) NOT NULL," +
                    "`type` VARCHAR(100) DEFAULT NULL," +
                    "`difficulte` VARCHAR(50) DEFAULT NULL," +
                    "`temps_preparation` INT DEFAULT 0," +
                    "`description` TEXT," +
                    "`image` VARCHAR(255) DEFAULT NULL," +
                    "`user_id` INT NOT NULL," +
                    "`created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "`etapes` TEXT," +
                    "FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // ingredient
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `ingredient` (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "`nom` VARCHAR(100) NOT NULL," +
                    "`nom_en` VARCHAR(100) DEFAULT NULL," +
                    "`categorie` VARCHAR(100) DEFAULT NULL," +
                    "`quantite` DOUBLE DEFAULT 0," +
                    "`unite` VARCHAR(50) DEFAULT NULL," +
                    "`date_peremption` DATE DEFAULT NULL," +
                    "`notes` TEXT," +
                    "`image` VARCHAR(255) DEFAULT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // recette_ingredient
            st.executeUpdate("CREATE TABLE IF NOT EXISTS `recette_ingredient` (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "`recette_id` INT NOT NULL," +
                    "`ingredient_id` INT NOT NULL," +
                    "`quantite` VARCHAR(100) NOT NULL," +
                    "FOREIGN KEY (`recette_id`) REFERENCES `recette`(`id`) ON DELETE CASCADE," +
                    "FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient`(`id`) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            System.out.println("[initOtherTables] All other tables ready.");
        } catch (SQLException e) {
            System.err.println("[initOtherTables] error : " + e.getMessage());
        }
    }

    public static MyBDConnexion getInstance() {
        if (instance == null) {
            instance = new MyBDConnexion();
        }
        return instance;
    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("[MyBDConnexion] Reconnexion en cours...");
                cnx = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("[MyBDConnexion] Reconnexion réussie.");
            }
        } catch (SQLException e) {
            System.err.println("[MyBDConnexion] Échec de reconnexion: " + e.getMessage());
        }
        return cnx;
    }
}