package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import models.Cours;

import java.io.IOException;

public class DetailsCoursController {

    public static Cours cours = new Cours();

    @FXML private Label lbTitre;
    @FXML private Label lbNiveau;
    @FXML private Label lbCategorie;
    @FXML private Label lbDuree;
    @FXML private Label lbCertifiant;

    @FXML
    void initialize() {
        afficherDetails();
    }

    public void afficherDetails() {
        lbTitre.setText("Titre : " + cours.getTitre());
        lbNiveau.setText("Niveau : " + cours.getNiveau());
        lbCategorie.setText("Catégorie : " + cours.getCategorie());
        lbDuree.setText("Durée : " + cours.getDureeHeures() + " heures");
        lbCertifiant.setText("Certifiant : " + (cours.isEstCertifiant() ? "Oui ✅" : "Non ❌"));
    }

    @FXML
    public void retour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionCours.fxml"));
            Parent root = loader.load();
            lbTitre.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}