package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import models.Cours;
import services.ServiceCours;

import java.io.IOException;
import java.time.LocalDate;

public class GestionCoursController {

    // fx:id du FXML → doivent correspondre exactement
    @FXML private TextField tfTitre;
    @FXML private TextArea taDescription;
    @FXML private TextArea taObjectifs;
    @FXML private Spinner<Integer> spinDuree;
    @FXML private ComboBox<String> cbNiveau;
    @FXML private ComboBox<String> cbCategorie;
    @FXML private CheckBox cbCertifiant;
    @FXML private CheckBox cbVisible;
    @FXML private DatePicker dpDebut;
    @FXML private DatePicker dpFin;
    @FXML private TableView<Cours> tableViewCours;
    @FXML private TableColumn<Cours, Integer> colId;
    @FXML private TableColumn<Cours, String> colTitre;
    @FXML private TableColumn<Cours, String> colNiveau;
    @FXML private TableColumn<Cours, String> colCategorie;
    @FXML private TableColumn<Cours, Integer> colDuree;
    @FXML private TableColumn<Cours, Boolean> colCertifiant;

    private Cours coursEnEdition = null;  // Stocke le cours en cours de modification
    private int lastSelectedIndex = -1;  // Pour permettre la désélection

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

        // Ajouter listener pour sélectionner une ligne dans la table
        tableViewCours.setOnMouseClicked(this::selectCoursInTable);

        // Charger les cours dans la table
        refreshTable();
    }

    private void refreshTable() {
        ServiceCours sc = new ServiceCours();
        tableViewCours.getItems().setAll(sc.getAll());
    }

    // Remplir le formulaire avec les données du cours sélectionné
    private void selectCoursInTable(MouseEvent event) {
        int selectedIndex = tableViewCours.getSelectionModel().getSelectedIndex();
        Cours selected = tableViewCours.getSelectionModel().getSelectedItem();
        
        // Permettre la désélection en cliquant à nouveau sur le même cours
        if (selectedIndex == lastSelectedIndex && selected != null) {
            tableViewCours.getSelectionModel().clearSelection();
            clearForm();
            lastSelectedIndex = -1;
        } else if (selected != null) {
            coursEnEdition = selected;
            remplirFormulaire(selected);
            lastSelectedIndex = selectedIndex;
        }
    }

    // Remplir le formulaire avec les données d'un cours
    private void remplirFormulaire(Cours c) {
        tfTitre.setText(c.getTitre());
        taDescription.setText(c.getDescription());
        taObjectifs.setText(c.getObjectifs());
        spinDuree.getValueFactory().setValue(c.getDureeHeures());
        cbNiveau.setValue(c.getNiveau());
        cbCategorie.setValue(c.getCategorie());
        cbCertifiant.setSelected(c.isEstCertifiant());
        cbVisible.setSelected(c.isVisible());
        dpDebut.setValue(c.getDateDebut());
        dpFin.setValue(c.getDateFin());
    }

    // Nettoyer/réinitialiser le formulaire
    private void clearForm() {
        tfTitre.clear();
        taDescription.clear();
        taObjectifs.clear();
        spinDuree.getValueFactory().setValue(1);
        cbNiveau.setValue(null);
        cbCategorie.setValue(null);
        cbCertifiant.setSelected(false);
        cbVisible.setSelected(true);
        dpDebut.setValue(LocalDate.now());
        dpFin.setValue(LocalDate.now().plusMonths(6));
        tableViewCours.getSelectionModel().clearSelection();
        coursEnEdition = null;
    }

    @FXML
    public void ajouterCours(ActionEvent event) {
        // Valider les champs obligatoires
        if (tfTitre.getText().isEmpty() || cbNiveau.getValue() == null || cbCategorie.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires!", Alert.AlertType.WARNING);
            return;
        }

        ServiceCours sc = new ServiceCours();
        Cours c = new Cours();
        c.setTitre(tfTitre.getText());
        c.setDescription(taDescription.getText());
        c.setObjectifs(taObjectifs.getText());
        c.setDureeHeures(spinDuree.getValue());
        c.setNiveau(cbNiveau.getValue());
        c.setCategorie(cbCategorie.getValue());
        c.setEstCertifiant(cbCertifiant.isSelected());
        c.setVisible(cbVisible.isSelected());
        c.setDateDebut(dpDebut.getValue() != null ? dpDebut.getValue() : LocalDate.now());
        c.setDateFin(dpFin.getValue() != null ? dpFin.getValue() : LocalDate.now().plusMonths(6));

        sc.add(c);
        refreshTable();
        clearForm();
        showAlert("Succès", "✅ Cours ajouté avec succès!", Alert.AlertType.INFORMATION);

        // Passer à la scène des détails
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
        if (coursEnEdition == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un cours à modifier!", Alert.AlertType.WARNING);
            return;
        }

        // Valider les champs
        if (tfTitre.getText().isEmpty() || cbNiveau.getValue() == null || cbCategorie.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires!", Alert.AlertType.WARNING);
            return;
        }

        // Mettre à jour les données du cours
        coursEnEdition.setTitre(tfTitre.getText());
        coursEnEdition.setDescription(taDescription.getText());
        coursEnEdition.setObjectifs(taObjectifs.getText());
        coursEnEdition.setDureeHeures(spinDuree.getValue());
        coursEnEdition.setNiveau(cbNiveau.getValue());
        coursEnEdition.setCategorie(cbCategorie.getValue());
        coursEnEdition.setEstCertifiant(cbCertifiant.isSelected());
        coursEnEdition.setVisible(cbVisible.isSelected());
        coursEnEdition.setDateDebut(dpDebut.getValue());
        coursEnEdition.setDateFin(dpFin.getValue());

        // Persister en base de données
        ServiceCours sc = new ServiceCours();
        sc.update(coursEnEdition);

        refreshTable();
        clearForm();
        showAlert("Succès", "✅ Cours modifié avec succès!", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void supprimerCours(ActionEvent event) {
        Cours selected = tableViewCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un cours à supprimer!", Alert.AlertType.WARNING);
            return;
        }

        // Demander confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le cours");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le cours \"" + selected.getTitre() + "\" ?");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            ServiceCours sc = new ServiceCours();
            sc.delete(selected);
            refreshTable();
            clearForm();
            showAlert("Succès", "✅ Cours supprimé avec succès!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void afficherDetails(ActionEvent event) {
        Cours selected = tableViewCours.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un cours!", Alert.AlertType.WARNING);
            return;
        }

        // Passer le cours sélectionné au contrôleur des détails
        DetailsCoursController.cours = selected;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsCours.fxml"));
            Parent root = loader.load();
            tableViewCours.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println("Erreur lors du chargement: " + e.getMessage());
        }
    }

    // Afficher une alerte
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}