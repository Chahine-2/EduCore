package controllers;

import interfaces.IUtilisateurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.Enseignant;
import models.Etudiant;
import models.Role;
import services.UtilisateurService;

import java.io.IOException;

public class InscriptionController {

    @FXML private ComboBox<String> comboRole;
    @FXML private TextField txtNom, txtPrenom, txtAge, txtEmail, txtTel;
    @FXML private PasswordField txtMdp;
    @FXML private Label lblMessage;

    // Champs Étudiant
    @FXML private VBox boxEtudiant;
    @FXML private TextField txtNumEtud, txtClasse;

    // Champs Enseignant
    @FXML private VBox boxProf;
    @FXML private TextField txtSpecialite, txtMatriculeProf;

    private IUtilisateurService service = new UtilisateurService();

    @FXML
    public void initialize() {
        // Remplir la liste déroulante au démarrage
        comboRole.getItems().addAll("Étudiant", "Enseignant");

        // Sélectionner "Étudiant" par défaut et afficher ses champs
        comboRole.setValue("Étudiant");
        handleChangementRole(null);
    }

    @FXML
    void handleChangementRole(ActionEvent event) {
        String roleChoisi = comboRole.getValue();

        if ("Étudiant".equals(roleChoisi)) {
            boxEtudiant.setVisible(true); boxEtudiant.setManaged(true);
            boxProf.setVisible(false); boxProf.setManaged(false);
        } else if ("Enseignant".equals(roleChoisi)) {
            boxProf.setVisible(true); boxProf.setManaged(true);
            boxEtudiant.setVisible(false); boxEtudiant.setManaged(false);
        }
    }

    @FXML
    void handleValiderInscription(ActionEvent event) {
        try {
            String roleChoisi = comboRole.getValue();
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String mdp = txtMdp.getText();

            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
                afficherMessage("Veuillez remplir tous les champs communs.", false);
                return;
            }

            int age = Integer.parseInt(txtAge.getText().trim());
            int tel = Integer.parseInt(txtTel.getText().trim());

            boolean succes = false;

            // --- SI C'EST UN ÉTUDIANT ---
            if ("Étudiant".equals(roleChoisi)) {
                String numEtud = txtNumEtud.getText().trim();
                String classe = txtClasse.getText().trim();

                if (numEtud.isEmpty() || classe.isEmpty()) {
                    afficherMessage("Veuillez remplir les champs de l'étudiant.", false);
                    return;
                }

                Role roleEtud = new Role(3, "Etudiant");
                Etudiant nouvelEtudiant = new Etudiant(0, nom, prenom, age, email, tel, mdp, roleEtud, numEtud, classe, true);
                succes = service.ajouterEtudiant(nouvelEtudiant);
            }
            // --- SI C'EST UN ENSEIGNANT ---
            else if ("Enseignant".equals(roleChoisi)) {
                String specialite = txtSpecialite.getText().trim();
                String matricule = txtMatriculeProf.getText().trim();

                if (specialite.isEmpty() || matricule.isEmpty()) {
                    afficherMessage("Veuillez remplir les champs de l'enseignant.", false);
                    return;
                }

                Role roleProf = new Role(2, "Enseignant");
                Enseignant nouvelEnseignant = new Enseignant(0, nom, prenom, age, email, tel, mdp, roleProf, specialite, matricule, true);
                succes = service.ajouterEnseignant(nouvelEnseignant);
            }

            // --- RÉSULTAT ---
            if (succes) {
                afficherMessage("Inscription réussie ! Redirection...", true);
                retourAuLogin(event);
            } else {
                afficherMessage("Erreur : Email ou Identifiant déjà existant.", false);
            }

        } catch (NumberFormatException e) {
            afficherMessage("L'âge et le téléphone doivent être des nombres.", false);
        }
    }

    @FXML
    void handleRetourConnexion(ActionEvent event) {
        retourAuLogin(event);
    }

    private void afficherMessage(String message, boolean succes) {
        lblMessage.setText(message);
        lblMessage.setTextFill(succes ? Color.GREEN : Color.RED);
        lblMessage.setVisible(true);
    }

    private void retourAuLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
            stage.setTitle("EduCore - Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}