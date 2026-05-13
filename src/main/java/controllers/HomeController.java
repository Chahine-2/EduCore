package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.AppStageLayout;

public class HomeController {

    @FXML private Label statusLabel;

    @FXML
    private void handleOpenStudent() {
        openInSameWindow("/student-portal.fxml", "EDUCORE · Étudiant");
    }

    @FXML
    private void handleOpenAdmin() {
        openInSameWindow("/evaluation.fxml", "EDUCORE · Administration");
    }

    @FXML
    private void handleExit() {
        Stage stage = currentStage();
        if (stage != null) {
            stage.close();
        }
    }

    private void openInSameWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = currentStage();
            if (stage == null) {
                return;
            }
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            AppStageLayout.maximizeWorkArea(stage);
        } catch (Exception e) {
            statusLabel.setText("Impossible d'ouvrir l'interface : " + e.getMessage());
        }
    }

    private Stage currentStage() {
        if (statusLabel == null || statusLabel.getScene() == null || statusLabel.getScene().getWindow() == null) {
            return null;
        }
        return (Stage) statusLabel.getScene().getWindow();
    }
}
