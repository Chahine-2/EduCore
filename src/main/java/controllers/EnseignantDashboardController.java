package controllers;

import interfaces.IUtilisateurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Etudiant;
import services.UtilisateurService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EnseignantDashboardController implements Initializable {

    @FXML private ComboBox<String> comboClasse;
    @FXML private TableView<Etudiant> tableEtudiants;
    @FXML private TableColumn<Etudiant, String> colMatricule;
    @FXML private TableColumn<Etudiant, String> colNom;
    @FXML private TableColumn<Etudiant, String> colPrenom;
    @FXML private TableColumn<Etudiant, String> colStatut;


    private IUtilisateurService service = new UtilisateurService();
    private ObservableList<Etudiant> listeEtudiants = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Configurer les colonnes du tableau
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("numeroEtudiant"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        // 2. Remplir la liste des classes disponibles
        comboClasse.getItems().addAll(service.listerToutesLesClasses());
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutAppel"));
    }

    @FXML
    void chargerEtudiants(ActionEvent event) {
        String classeChoisie = comboClasse.getValue();
        if (classeChoisie == null || classeChoisie.isEmpty()) {
            afficherAlerte("Erreur", "Veuillez sélectionner une classe.");
            return;
        }

        // On vide l'ancien tableau et on charge la nouvelle classe
        listeEtudiants.clear();
        listeEtudiants.addAll(service.listerEtudiantsParClasse(classeChoisie));
        tableEtudiants.setItems(listeEtudiants);
    }

    @FXML
    void marquerPresent(ActionEvent event) {
        enregistrerAppel("Présent");
    }

    @FXML
    void marquerAbsent(ActionEvent event) {
        enregistrerAppel("Absent");
    }

    private void enregistrerAppel(String statut) {
        Etudiant etudiantSelect = tableEtudiants.getSelectionModel().getSelectedItem();

        if (etudiantSelect == null) {
            afficherAlerte("Sélection requise", "Veuillez cliquer sur un étudiant dans le tableau pour le marquer.");
            return;
        }

        // On enregistre dans la base de données (MySQL)
        if (service.enregistrerPresence(etudiantSelect.getId(), statut)) {

            // ---> LE CHANGEMENT EST ICI <---
            // Au lieu de supprimer l'étudiant, on change son statut en mémoire
            etudiantSelect.setStatutAppel(statut);

            // On force le tableau à se rafraîchir visuellement pour afficher le nouveau texte !
            tableEtudiants.refresh();

            // Petite astuce bonus : on sélectionne automatiquement l'étudiant suivant
            // pour que le prof puisse cliquer super vite !
            tableEtudiants.getSelectionModel().selectNext();

        } else {
            afficherAlerte("Erreur DB", "Impossible d'enregistrer la présence.");
        }
    }

    @FXML
    void handleDeconnexion(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
            stage.setTitle("EduCore - Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}