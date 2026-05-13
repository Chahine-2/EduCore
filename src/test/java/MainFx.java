package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.AppStageLayout;

import java.io.IOException;

public class MainFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // ── Fenêtre de choix du rôle ────────────────
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("EduCore");
            alert.setHeaderText("Choisissez votre rôle");
            alert.setContentText("Êtes-vous un Enseignant ou un Étudiant ?");

            ButtonType btnEnseignant = new ButtonType("👨‍🏫 Enseignant");
            ButtonType btnEtudiant   = new ButtonType("🎓 Étudiant");
            alert.getButtonTypes().setAll(btnEnseignant, btnEtudiant);

            String fxml = "/GestionCours.fxml";  // Par défaut = Enseignant
            var result = alert.showAndWait();

            if (result.isPresent() && result.get() == btnEtudiant) {
                fxml = "/Accueil.fxml";  // Étudiant - Ouvre d'abord l'Accueil
            }

            System.out.println("📂 Tentative de chargement : " + fxml);

            // ── Charger le bon FXML ─────────────────────
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

            if (loader.getLocation() == null) {
                System.out.println("❌ ERREUR : Le fichier " + fxml + " n'a pas pu être trouvé!");
                showErrorDialog("Erreur de démarrage", "Fichier non trouvé : " + fxml);
                primaryStage.close();
                return;
            }

            Parent root = loader.load();

            if (root == null) {
                System.out.println("❌ ERREUR : Le contenu FXML est null!");
                showErrorDialog("Erreur de démarrage", "Impossible de charger le fichier FXML");
                primaryStage.close();
                return;
            }

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("EduCore - Plateforme d'apprentissage");
            AppStageLayout.maximizeWorkArea(primaryStage);
            primaryStage.show();

            System.out.println("✅ Application démarrée avec succès");

        } catch (IOException e) {
            System.out.println("❌ IOException lors du démarrage :");
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur IO", "IOException : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ ERREUR CRITIQUE :");
            System.out.println("    Type : " + e.getClass().getName());
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur critique", e.getClass().getSimpleName() + " : " + e.getMessage());
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Erreur au démarrage");
        alert.setContentText(message + "\n\nVérifiez la console pour plus de détails.");
        alert.showAndWait();
    }
}