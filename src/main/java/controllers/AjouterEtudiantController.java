package controllers;

import interfaces.IUtilisateurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Etudiant;
import models.Role;
import services.UtilisateurService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AjouterEtudiantController implements Initializable {

    @FXML private TextField txtNom, txtPrenom, txtAge, txtEmail, txtTel, txtNumEtud;
    @FXML private PasswordField txtMdp;
    @FXML private ComboBox<String> comboClasse;
    @FXML private Label lblMessage;

    private IUtilisateurService service = new UtilisateurService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Remplir la liste des classes au démarrage de la pop-up
        List<String> classes = service.listerToutesLesClasses();
        comboClasse.getItems().addAll(classes);
    }

    @FXML
    void handleValider(ActionEvent event) {
        try {
            // Récupération des données
            String nom = txtNom.getText();
            String prenom = txtPrenom.getText();
            int age = Integer.parseInt(txtAge.getText());
            String email = txtEmail.getText();
            int tel = Integer.parseInt(txtTel.getText());
            String mdp = txtMdp.getText();
            String numEtud = txtNumEtud.getText();
            String classe = comboClasse.getValue();

            if (classe == null || nom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
                lblMessage.setText("Veuillez remplir tous les champs requis.");
                lblMessage.setVisible(true);
                return;
            }

            Role roleEtudiant = new Role(3, "Etudiant");
            Etudiant nouvelEtudiant = new Etudiant(0, nom, prenom, age, email, tel, mdp, roleEtudiant, "N/A", numEtud, true);

            if (service.ajouterEtudiant(nouvelEtudiant)) {
                // Fermer la fenêtre si succès
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                lblMessage.setText("Erreur lors de l'enregistrement (Email existant ?)");
                lblMessage.setVisible(true);
            }

        } catch (NumberFormatException e) {
            lblMessage.setText("L'âge et le téléphone doivent être des nombres.");
            lblMessage.setVisible(true);
        }
    }
}