package controllers;

import interfaces.IUtilisateurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Enseignant;
import models.Role;
import services.UtilisateurService;

public class AjouterEnseignantController {

    @FXML private TextField txtNom, txtPrenom, txtAge, txtEmail, txtTel;
    @FXML private TextField txtSpecialite, txtMatricule;
    @FXML private PasswordField txtMdp;
    @FXML private Label lblMessage;

    private IUtilisateurService service = new UtilisateurService();

    @FXML
    void handleValider(ActionEvent event) {
        try {
            // 1. Récupération des champs
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            int age = Integer.parseInt(txtAge.getText().trim());
            String email = txtEmail.getText().trim();
            int tel = Integer.parseInt(txtTel.getText().trim());
            String mdp = txtMdp.getText();
            String specialite = txtSpecialite.getText().trim();
            String matricule = txtMatricule.getText().trim();

            // 2. Vérification rapide
            if (nom.isEmpty() || email.isEmpty() || mdp.isEmpty() || matricule.isEmpty()) {
                lblMessage.setText("Veuillez remplir tous les champs obligatoires.");
                lblMessage.setVisible(true);
                return;
            }

            // 3. Création de l'objet Enseignant
            Role roleProf = new Role(2, "Enseignant");

            // Note : Vérifiez bien l'ordre dans votre fichier Enseignant.java !
            // L'ordre habituel : id, nom, prenom, age, email, tel, mdp, role, specialite, matricule, statutActif
            Enseignant nouvelEnseignant = new Enseignant(0, nom, prenom, age, email, tel, mdp, roleProf, specialite, matricule, true);

            // 4. Appel de votre backend
            if (service.ajouterEnseignant(nouvelEnseignant)) {
                // Succès : on ferme la fenêtre
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                lblMessage.setText("Erreur d'insertion (Email ou Matricule déjà existant).");
                lblMessage.setVisible(true);
            }

        } catch (NumberFormatException e) {
            lblMessage.setText("L'âge et le téléphone doivent être des nombres.");
            lblMessage.setVisible(true);
        }
    }
}