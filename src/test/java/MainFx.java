package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // ── Fenêtre de choix du rôle ────────────
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("EduCore");
        alert.setHeaderText("Choisissez votre rôle");
        alert.setContentText("Êtes-vous un Enseignant ou un Étudiant ?");

        ButtonType btnEnseignant = new ButtonType("👨‍🏫 Enseignant");
        ButtonType btnEtudiant   = new ButtonType("🎓 Étudiant");
        alert.getButtonTypes().setAll(btnEnseignant, btnEtudiant);

        String fxml = "/GestionCours.fxml";  // par défaut enseignant
        var result = alert.showAndWait();

        if (result.isPresent() && result.get() == btnEtudiant) {
            fxml = "/Etudiant.fxml";
        }

        // ── Charger le bon FXML ─────────────────
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("EduCore");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}