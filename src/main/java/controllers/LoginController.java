package controllers;

import interfaces.IUtilisateurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;
import utils.UserSession;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    // On instancie le service backend
    private IUtilisateurService service = new UtilisateurService();

    @FXML
    void handleLogin(ActionEvent event) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            afficherErreur("Veuillez remplir tous les champs.");
            return;
        }

        // Appel au backend
        Utilisateur user = service.authentifier(email, password);

        if (user == null) {
            afficherErreur("Email/Mot de passe incorrect ou compte suspendu.");
        } else {
            // Connexion réussie ! On lance le routage
            System.out.println("Connexion réussie. Rôle : [" + user.getRole().getNomRole() + "]");
            redirigerVersTableauDeBord(event, user);
        }
    }

    private void afficherErreur(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private void redirigerVersTableauDeBord(ActionEvent event, Utilisateur user) {
        String fxmlFile = "";
        String roleNom = user.getRole().getNomRole().trim(); // .trim() enlève les espaces invisibles

        // equalsIgnoreCase permet d'ignorer les majuscules/minuscules (ex: "Enseignant" == "enseignant")
        if (roleNom.equalsIgnoreCase("Administrateur") || roleNom.equalsIgnoreCase("Admin")) {
            fxmlFile = "/views/AdminDashboard.fxml";
        } else if (roleNom.equalsIgnoreCase("Enseignant") || roleNom.equalsIgnoreCase("Professeur")) {
            fxmlFile = "/views/TeacherDashboard.fxml";
        } else if (roleNom.equalsIgnoreCase("Etudiant")) {
            fxmlFile = "/views/StudentDashboard.fxml";
        } else {
            afficherErreur("Rôle non reconnu dans le code : " + roleNom);
            return;
        }

        try {
            // Vérification de l'existence du fichier FXML
            URL resourceUrl = getClass().getResource(fxmlFile);
            if (resourceUrl == null) {
                afficherErreur("Fichier FXML introuvable : " + fxmlFile);
                return;
            }

            UserSession.setCurrentUser(user);

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 720));
            stage.setTitle("EduCore - Tableau de bord " + roleNom);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Erreur lors du chargement de l'interface.");
        }
    }
    @FXML
    void handleAllerInscription(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Inscription.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 450, 650));
            stage.setTitle("EduCore - Inscription Étudiant");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            afficherErreur("Impossible de charger la page d'inscription.");
        }
    }
}