package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import models.Chapitre;
import models.Cours;
import services.ServiceChapitre;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class DetailsCoursController {

    public static Cours cours = new Cours();

    @FXML private Label lblTitre;
    @FXML private Label lblNiveau;
    @FXML private Label lblCategorie;
    @FXML private Label lblDuree;
    @FXML private Label lblCertifiant;
    @FXML private TextArea taDescription;
    @FXML private TextArea taObjectifs;
    @FXML private TableView<Chapitre> tableViewChapitres;
    @FXML private TableColumn<Chapitre, Integer> colOrdre;
    @FXML private TableColumn<Chapitre, String> colTitreChap;
    @FXML private TableColumn<Chapitre, String> colType;
    @FXML private TableColumn<Chapitre, Integer> colDureeMin;
    @FXML private Label lblTotalChapitres;
    @FXML private Button btnAjouterChapitre;
    @FXML private Button btnModifierChapitre;
    @FXML private Button btnSupprimerChapitre;

    private Chapitre chapitreEnEdition = null;
    private ServiceChapitre serviceChapitre = new ServiceChapitre();

    @FXML
    void initialize() {
        // Configurer les colonnes de la TableView
        colOrdre.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("ordre"));
        colTitreChap.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("typeContenu"));
        colDureeMin.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("dureeMinutes"));

        // Ajouter listener pour sélectionner une ligne
        tableViewChapitres.setOnMouseClicked(this::selectChapitreInTable);

        afficherDetails();
        chargerChapitres();
    }

    public void afficherDetails() {
        lblTitre.setText("📘 " + cours.getTitre());
        lblNiveau.setText("Niveau : " + cours.getNiveau());
        lblCategorie.setText("Catégorie : " + cours.getCategorie());
        lblDuree.setText("Durée : " + cours.getDureeHeures() + " heures");
        lblCertifiant.setText("Certifiant : " + (cours.isEstCertifiant() ? "Oui ✅" : "Non ❌"));
        taDescription.setText(cours.getDescription() != null ? cours.getDescription() : "N/A");
        taObjectifs.setText(cours.getObjectifs() != null ? cours.getObjectifs() : "N/A");
    }

    private void chargerChapitres() {
        if (cours.getId() > 0) {
            java.util.List<Chapitre> chapitres = serviceChapitre.getByCours(cours.getId());
            tableViewChapitres.getItems().setAll(chapitres);
            mettreAJourCompteur();
        }
    }

    private void mettreAJourCompteur() {
        int total = tableViewChapitres.getItems().size();
        lblTotalChapitres.setText("Total chapitres : " + total);
    }

    private void selectChapitreInTable(MouseEvent event) {
        Chapitre selected = tableViewChapitres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            chapitreEnEdition = selected;
        }
    }

    @FXML
    public void ajouterChapitre(ActionEvent event) {
        Dialog<Chapitre> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Chapitre");
        dialog.setHeaderText("Créer un nouveau chapitre");

        // Créer les contrôles
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField tfTitre = new TextField();
        tfTitre.setPromptText("Titre du chapitre");

        TextArea taDescChap = new TextArea();
        taDescChap.setPromptText("Description");
        taDescChap.setPrefRowCount(3);
        taDescChap.setWrapText(true);

        Spinner<Integer> spinOrdre = new Spinner<>(1, 100, 1);
        Spinner<Integer> spinDuree = new Spinner<>(1, 1000, 30);

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("video", "texte", "pdf", "quiz");

        TextField tfUrl = new TextField();
        tfUrl.setPromptText("URL du contenu");

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(tfTitre, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(taDescChap, 1, 1);
        grid.add(new Label("Ordre:"), 0, 2);
        grid.add(spinOrdre, 1, 2);
        grid.add(new Label("Durée (min):"), 0, 3);
        grid.add(spinDuree, 1, 3);
        grid.add(new Label("Type:"), 0, 4);
        grid.add(cbType, 1, 4);
        grid.add(new Label("URL:"), 0, 5);
        grid.add(tfUrl, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (tfTitre.getText().isEmpty() || cbType.getValue() == null) {
                    showAlert("Erreur", "Veuillez remplir les champs obligatoires!", Alert.AlertType.WARNING);
                    return null;
                }
                Chapitre ch = new Chapitre();
                ch.setTitre(tfTitre.getText());
                ch.setDescription(taDescChap.getText());
                ch.setOrdre(spinOrdre.getValue());
                ch.setDureeMinutes(spinDuree.getValue());
                ch.setTypeContenu(cbType.getValue());
                ch.setUrlContenu(tfUrl.getText());
                ch.setDateCreation(LocalDate.now());
                ch.setCoursId(cours.getId());
                return ch;
            }
            return null;
        });

        Optional<Chapitre> result = dialog.showAndWait();
        if (result.isPresent()) {
            serviceChapitre.add(result.get());
            chargerChapitres();
            showAlert("Succès", "✅ Chapitre ajouté avec succès!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void modifierChapitre(ActionEvent event) {
        if (chapitreEnEdition == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un chapitre à modifier!", Alert.AlertType.WARNING);
            return;
        }

        Dialog<Chapitre> dialog = new Dialog<>();
        dialog.setTitle("Modifier Chapitre");
        dialog.setHeaderText("Modifier : " + chapitreEnEdition.getTitre());

        // Créer les contrôles avec les valeurs existantes
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField tfTitre = new TextField(chapitreEnEdition.getTitre());
        TextArea taDescChap = new TextArea(chapitreEnEdition.getDescription());
        taDescChap.setPrefRowCount(3);
        taDescChap.setWrapText(true);

        Spinner<Integer> spinOrdre = new Spinner<>(1, 100, chapitreEnEdition.getOrdre());
        Spinner<Integer> spinDuree = new Spinner<>(1, 1000, chapitreEnEdition.getDureeMinutes());

        ComboBox<String> cbType = new ComboBox<>();
        cbType.getItems().addAll("video", "texte", "pdf", "quiz");
        cbType.setValue(chapitreEnEdition.getTypeContenu());

        TextField tfUrl = new TextField(chapitreEnEdition.getUrlContenu());

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(tfTitre, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(taDescChap, 1, 1);
        grid.add(new Label("Ordre:"), 0, 2);
        grid.add(spinOrdre, 1, 2);
        grid.add(new Label("Durée (min):"), 0, 3);
        grid.add(spinDuree, 1, 3);
        grid.add(new Label("Type:"), 0, 4);
        grid.add(cbType, 1, 4);
        grid.add(new Label("URL:"), 0, 5);
        grid.add(tfUrl, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (tfTitre.getText().isEmpty() || cbType.getValue() == null) {
                    showAlert("Erreur", "Veuillez remplir les champs obligatoires!", Alert.AlertType.WARNING);
                    return null;
                }
                chapitreEnEdition.setTitre(tfTitre.getText());
                chapitreEnEdition.setDescription(taDescChap.getText());
                chapitreEnEdition.setOrdre(spinOrdre.getValue());
                chapitreEnEdition.setDureeMinutes(spinDuree.getValue());
                chapitreEnEdition.setTypeContenu(cbType.getValue());
                chapitreEnEdition.setUrlContenu(tfUrl.getText());
                return chapitreEnEdition;
            }
            return null;
        });

        Optional<Chapitre> result = dialog.showAndWait();
        if (result.isPresent()) {
            serviceChapitre.update(result.get());
            chargerChapitres();
            chapitreEnEdition = null;
            tableViewChapitres.getSelectionModel().clearSelection();
            showAlert("Succès", "✅ Chapitre modifié avec succès!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void supprimerChapitre(ActionEvent event) {
        Chapitre selected = tableViewChapitres.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "❌ Veuillez sélectionner un chapitre à supprimer!", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le chapitre");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer le chapitre \"" + selected.getTitre() + "\" ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            serviceChapitre.delete(selected);
            chargerChapitres();
            chapitreEnEdition = null;
            tableViewChapitres.getSelectionModel().clearSelection();
            showAlert("Succès", "✅ Chapitre supprimé avec succès!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void retour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionCours.fxml"));
            Parent root = loader.load();
            lblTitre.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}