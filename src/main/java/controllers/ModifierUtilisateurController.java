package controllers;

import interfaces.IUtilisateurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Enseignant;
import models.Etudiant;
import models.Utilisateur;
import services.UtilisateurService;

public class ModifierUtilisateurController {

    @FXML private Label lblTitre, lblMessage;
    @FXML private TextField txtNom, txtPrenom, txtAge, txtEmail, txtTel;

    @FXML private VBox boxEtudiant;
    @FXML private TextField txtNumEtud, txtClasse;

    @FXML private VBox boxProf;
    @FXML private TextField txtSpecialite, txtMatriculeProf;

    private IUtilisateurService service = new UtilisateurService();
    private Utilisateur utilisateurComplet; // Holds the full DB record

    // This method is called from the Admin Dashboard to pass the selected user
    public void initData(Utilisateur userBasic) {
        String role = userBasic.getRole().getNomRole();
        lblTitre.setText("Modifier : " + userBasic.getPrenom() + " (" + role + ")");

        // Fetch all details from DB
        utilisateurComplet = service.getUtilisateurComplet(userBasic.getId(), role);

        if (utilisateurComplet == null) {
            lblMessage.setText("Erreur de chargement.");
            lblMessage.setVisible(true);
            return;
        }

        // Fill common data
        txtNom.setText(utilisateurComplet.getNom());
        txtPrenom.setText(utilisateurComplet.getPrenom());
        txtAge.setText(String.valueOf(utilisateurComplet.getAge()));
        txtEmail.setText(utilisateurComplet.getEmail());
        txtTel.setText(String.valueOf(utilisateurComplet.getTel()));

        // Show/Fill specific data based on Role
        if (role.equals("Etudiant")) {
            boxEtudiant.setVisible(true); boxEtudiant.setManaged(true);
            Etudiant e = (Etudiant) utilisateurComplet;
            txtNumEtud.setText(e.getNumeroEtudiant());
            txtClasse.setText(e.getClasse());
        } else if (role.equals("Enseignant")) {
            boxProf.setVisible(true); boxProf.setManaged(true);
            Enseignant p = (Enseignant) utilisateurComplet;
            txtSpecialite.setText(p.getSpecialite());
            txtMatriculeProf.setText(p.getMatricule());
        }
    }

    @FXML
    void handleValiderModif(ActionEvent event) {
        try {
            // Update common fields
            utilisateurComplet.setNom(txtNom.getText());
            utilisateurComplet.setPrenom(txtPrenom.getText());
            utilisateurComplet.setAge(Integer.parseInt(txtAge.getText()));
            utilisateurComplet.setEmail(txtEmail.getText());
            utilisateurComplet.setTel(Integer.parseInt(txtTel.getText()));

            // Update specific fields
            if (utilisateurComplet instanceof Etudiant) {
                ((Etudiant) utilisateurComplet).setNumeroEtudiant(txtNumEtud.getText());
                ((Etudiant) utilisateurComplet).setClasse(txtClasse.getText());
            } else if (utilisateurComplet instanceof Enseignant) {
                ((Enseignant) utilisateurComplet).setSpecialite(txtSpecialite.getText());
                ((Enseignant) utilisateurComplet).setMatricule(txtMatriculeProf.getText());
            }

            // Save to database
            if (service.modifierUtilisateur(utilisateurComplet)) {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close();
            } else {
                lblMessage.setText("Erreur lors de la modification.");
                lblMessage.setVisible(true);
            }

        } catch (NumberFormatException e) {
            lblMessage.setText("Âge et Téléphone doivent être des nombres.");
            lblMessage.setVisible(true);
        }
    }
}