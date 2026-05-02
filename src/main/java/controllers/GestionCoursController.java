package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import models.Cours;
import services.ServiceCours;

import java.io.IOException;
import java.time.LocalDate;

public class GestionCoursController {

    // fx:id du FXML → doivent correspondre exactement
    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;   // était TextField, doit être TextArea
    @FXML private TextArea taObjectifs;     // manquait
    @FXML private Spinner<Integer> spinDuree; // était TextField tfDuree
    @FXML private ComboBox<String> cbNiveau;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private CheckBox cbCertifiant;    // était chkCertifiant
    @FXML private DatePicker dpDebut;       // manquait
    @FXML private DatePicker dpFin;         // manquait
    @FXML private TableView<Cours> tableViewCours;  // manquait
    @FXML private TableColumn<Cours, Integer> colId;
    @FXML private TableColumn<Cours, String> colTitre;
    @FXML private TableColumn<Cours, String> colNiveau;
    @FXML private TableColumn<Cours, String> colCategorie;
    @FXML private TableColumn<Cours, Integer> colDuree;
    @FXML private TableColumn<Cours, Boolean> colCertifiant;

    @FXML
    void initialize() {
        cbNiveau.getItems().addAll("debutant", "intermediaire", "avance");
        cbCategorie.getItems().addAll("informatique", "mecanique", "electrique");

        // Configurer le Spinner pour les heures (1 à 200)
        spinDuree.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 200, 1)
        );

        // Configurer les colonnes de la TableView
        colId.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("titre"));
        colNiveau.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("niveau"));
        colCategorie.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("categorie"));
        colDuree.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("dureeHeures"));
        colCertifiant.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("estCertifiant"));

        // Charger les cours dans la table
        refreshTable();
    }

    private void refreshTable() {
        ServiceCours sc = new ServiceCours();
        tableViewCours.getItems().setAll(sc.getAll());
    }

    @FXML
    public void ajouterCours(ActionEvent event) {
        ServiceCours sc = new ServiceCours();

        Cours c = new Cours();
        c.setTitre(tfTitre.getText());
        c.setDescription(taDescription.getText());
        c.setObjectifs(taObjectifs.getText());
        c.setDureeHeures(spinDuree.getValue());
        c.setNiveau(cbNiveau.getValue());
        c.setCategorie(cbCategorie.getValue());
        c.setEstCertifiant(cbCertifiant.isSelected());
        c.setDateDebut(dpDebut.getValue() != null ? dpDebut.getValue() : LocalDate.now());
        c.setDateFin(dpFin.getValue() != null ? dpFin.getValue() : LocalDate.now().plusMonths(6));

        sc.add(c);
        refreshTable();

        // Passer à la scène 2
        try {
            DetailsCoursController.cours = c;
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsCours.fxml"));
            Parent root = loader.load();
            tfTitre.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @FXML
    public void modifierCours(ActionEvent event) {
        // TODO
    }

    @FXML
    public void supprimerCours(ActionEvent event) {
        // TODO
    }

    @FXML
    public void afficherDetails(ActionEvent event) {
        // TODO
    }
}