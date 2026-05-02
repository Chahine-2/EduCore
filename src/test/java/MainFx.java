package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // 👇 Écran d'accueil par défaut
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Accueil.fxml"));
            // Pour tester directement:
            // - Enseignant (Gestion) → "/GestionCours.fxml"
            // - Étudiant             → "/Etudiant.fxml"
            // - Accueil              → "/Accueil.fxml" ← DÉFAUT

            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("EduCore - Plateforme d'Apprentissage");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}