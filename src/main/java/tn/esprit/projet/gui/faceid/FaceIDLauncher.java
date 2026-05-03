package tn.esprit.projet.gui.faceid;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Face ID Launcher Utility
 * Opens Face ID authentication and enrollment windows
 */
public class FaceIDLauncher {

    /**
     * Launch Face ID authentication window
     */
    public static void launchAuthentication(Runnable onSuccess, Runnable onFailure) {
        try {
            FXMLLoader loader = new FXMLLoader(
                FaceIDLauncher.class.getResource("/fxml/faceid/professional_face_auth.fxml")
            );
            
            Parent root = loader.load();
            
            ProfessionalFaceIDController controller = loader.getController();
            controller.setOnSuccess(onSuccess);
            controller.setOnFailure(onFailure);

            Stage stage = new Stage();
            stage.setTitle("Face ID Authentication");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setScene(new Scene(root, 600, 700));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            System.err.println("[FaceID] Failed to launch authentication: " + e.getMessage());
            e.printStackTrace();
            
            if (onFailure != null) {
                onFailure.run();
            }
        }
    }

    /**
     * Launch Face ID enrollment window
     */
    public static void launchEnrollment(int userId, Runnable onSuccess, Runnable onCancel) {
        try {
            FXMLLoader loader = new FXMLLoader(
                FaceIDLauncher.class.getResource("/fxml/faceid/face_enroll.fxml")
            );
            
            Parent root = loader.load();
            
            FaceIDEnrollController controller = loader.getController();
            controller.setUserId(userId);
            controller.setOnSuccess(onSuccess);
            controller.setOnCancel(onCancel);

            Stage stage = new Stage();
            stage.setTitle("Enroll Face ID");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.DECORATED);
            stage.setScene(new Scene(root, 600, 700));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            System.err.println("[FaceID] Failed to launch enrollment: " + e.getMessage());
            e.printStackTrace();
            
            if (onCancel != null) {
                onCancel.run();
            }
        }
    }
}
