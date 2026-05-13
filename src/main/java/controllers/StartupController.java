package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.AppStageLayout;

public class StartupController {

    @FXML private Label statusLabel;

    @FXML
    private void handleOpenHome() {
        openInSameWindow("/home.fxml", "EDUCORE");
    }

    @FXML
    private void handleOpenAccueil() {
        openInSameWindow("/Accueil.fxml", "EDUCORE · Accueil");
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
            statusLabel.setText("Impossible d'ouvrir la page : " + e.getMessage());
        }
    }

    private Stage currentStage() {
        if (statusLabel == null || statusLabel.getScene() == null || statusLabel.getScene().getWindow() == null) {
            return null;
        }
        return (Stage) statusLabel.getScene().getWindow();
    }
}
