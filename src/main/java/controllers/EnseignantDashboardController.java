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
import utils.AppStageLayout;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Legacy enseignant screen: attendance only. Prefer {@link TeacherDashboardController} for the full teacher workspace.
 */
public class EnseignantDashboardController implements Initializable {

    @FXML private ComboBox<String> comboClasse;
    @FXML private TableView<Etudiant> tableEtudiants;
    @FXML private TableColumn<Etudiant, String> colMatricule;
    @FXML private TableColumn<Etudiant, String> colNom;
    @FXML private TableColumn<Etudiant, String> colPrenom;
    @FXML private TableColumn<Etudiant, String> colStatut;

    private final IUtilisateurService service = new UtilisateurService();
    private final ObservableList<Etudiant> listeEtudiants = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("numeroEtudiant"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutAppel"));
        comboClasse.getItems().addAll(service.listerToutesLesClasses());
    }

    @FXML
    void chargerEtudiants(ActionEvent event) {
        String classeChoisie = comboClasse.getValue();
        if (classeChoisie == null || classeChoisie.isEmpty()) {
            afficherAlerte("Erreur", "Veuillez sélectionner une classe.");
            return;
        }
        listeEtudiants.clear();
        listeEtudiants.addAll(service.listerEtudiantsParClasse(classeChoisie));
        tableEtudiants.setItems(listeEtudiants);
        service.preparerNouvelleSessionAppel(classeChoisie);
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
        if (service.enregistrerPresence(etudiantSelect.getId(), statut)) {
            etudiantSelect.setStatutAppel(statut);
            tableEtudiants.refresh();
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
            stage.setScene(new Scene(root));
            stage.setTitle("EduCore - Connexion");
            AppStageLayout.maximizeWorkArea(stage);
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
