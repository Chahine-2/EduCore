package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
<<<<<<< HEAD
=======
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
>>>>>>> 1b03cb2 (interface5)
import models.Chapitre;
import models.Cours;
import services.ServiceChapitre;

import java.io.File;
import java.io.IOException;
<<<<<<< HEAD
=======
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
>>>>>>> 1b03cb2 (interface5)
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
<<<<<<< HEAD
=======
    @FXML private TableColumn<Chapitre, Boolean> colVisible;
>>>>>>> 1b03cb2 (interface5)
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
<<<<<<< HEAD
=======
        colVisible.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("visible"));

        // Afficher les booléens comme "Visible" / "Masqué"
        colVisible.setCellFactory(col -> new javafx.scene.control.TableCell<Chapitre, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "👁 Visible" : "👁‍🗨 Masqué");
                    setStyle(item ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });
>>>>>>> 1b03cb2 (interface5)

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

<<<<<<< HEAD
        // Créer les contrôles
=======
>>>>>>> 1b03cb2 (interface5)
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

<<<<<<< HEAD
        TextField tfUrl = new TextField();
        tfUrl.setPromptText("URL du contenu");
=======
        // ── Champ fichier / URL ─────────────────────────────────
        TextField tfUrl = new TextField();
        tfUrl.setPromptText("URL ou chemin du fichier");
        tfUrl.setPrefWidth(220);

        Button btnParcourir = new Button("📂 Parcourir...");
        btnParcourir.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6;");
        btnParcourir.setOnAction(e -> {
            File f = ouvrirFileChooser();
            if (f != null) {
                String dest = copierFichier(f);
                tfUrl.setText(dest);
                // Détecter automatiquement le type
                String ext = obtenirExtension(f.getName()).toLowerCase();
                if (ext.equals("mp4") || ext.equals("avi") || ext.equals("mkv") || ext.equals("mov"))
                    cbType.setValue("video");
                else if (ext.equals("pdf"))
                    cbType.setValue("pdf");
                else
                    cbType.setValue("texte");
            }
        });

        HBox hboxFichier = new HBox(8, tfUrl, btnParcourir);
>>>>>>> 1b03cb2 (interface5)

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
<<<<<<< HEAD
        grid.add(new Label("URL:"), 0, 5);
        grid.add(tfUrl, 1, 5);
=======
        grid.add(new Label("Fichier / URL:"), 0, 5);
        grid.add(hboxFichier, 1, 5);
        grid.add(new Label("Visible pour les étudiants:"), 0, 6);
        CheckBox cbVisible = new CheckBox("Oui");
        cbVisible.setSelected(true);
        grid.add(cbVisible, 1, 6);
>>>>>>> 1b03cb2 (interface5)

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
<<<<<<< HEAD
=======
                ch.setVisible(cbVisible.isSelected());
>>>>>>> 1b03cb2 (interface5)
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
<<<<<<< HEAD
=======
            tableViewChapitres.refresh();  // Force le rafraîchissement de l'affichage
>>>>>>> 1b03cb2 (interface5)
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

<<<<<<< HEAD
        // Créer les contrôles avec les valeurs existantes
=======
>>>>>>> 1b03cb2 (interface5)
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

<<<<<<< HEAD
        TextField tfUrl = new TextField(chapitreEnEdition.getUrlContenu());
=======
        // ── Champ fichier / URL ─────────────────────────────────
        TextField tfUrl = new TextField(
            chapitreEnEdition.getUrlContenu() != null ? chapitreEnEdition.getUrlContenu() : "");
        tfUrl.setPrefWidth(220);

        Button btnParcourir = new Button("📂 Parcourir...");
        btnParcourir.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-padding: 6 10; -fx-background-radius: 6;");
        btnParcourir.setOnAction(e -> {
            File f = ouvrirFileChooser();
            if (f != null) {
                String dest = copierFichier(f);
                tfUrl.setText(dest);
                String ext = obtenirExtension(f.getName()).toLowerCase();
                if (ext.equals("mp4") || ext.equals("avi") || ext.equals("mkv") || ext.equals("mov"))
                    cbType.setValue("video");
                else if (ext.equals("pdf"))
                    cbType.setValue("pdf");
                else
                    cbType.setValue("texte");
            }
        });

        HBox hboxFichier = new HBox(8, tfUrl, btnParcourir);
>>>>>>> 1b03cb2 (interface5)

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
<<<<<<< HEAD
        grid.add(new Label("URL:"), 0, 5);
        grid.add(tfUrl, 1, 5);
=======
        grid.add(new Label("Fichier / URL:"), 0, 5);
        grid.add(hboxFichier, 1, 5);
        grid.add(new Label("Visible pour les étudiants:"), 0, 6);
        CheckBox cbVisible = new CheckBox("Oui");
        cbVisible.setSelected(chapitreEnEdition.isVisible());
        grid.add(cbVisible, 1, 6);
>>>>>>> 1b03cb2 (interface5)

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
<<<<<<< HEAD
=======
                chapitreEnEdition.setVisible(cbVisible.isSelected());
>>>>>>> 1b03cb2 (interface5)
                return chapitreEnEdition;
            }
            return null;
        });

        Optional<Chapitre> result = dialog.showAndWait();
        if (result.isPresent()) {
            serviceChapitre.update(result.get());
<<<<<<< HEAD
            chargerChapitres();
=======
            // Mettre à jour l'item dans la table directement sans recharger
            tableViewChapitres.refresh();  // Rafraîchit l'affichage de tous les items
>>>>>>> 1b03cb2 (interface5)
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
<<<<<<< HEAD
=======
            tableViewChapitres.refresh();
>>>>>>> 1b03cb2 (interface5)
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
<<<<<<< HEAD
=======

    // ── FileChooser : ouvrir la boîte de sélection de fichier ───
    private File ouvrirFileChooser() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sélectionner un fichier");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Tous les fichiers",  "*.*"),
            new FileChooser.ExtensionFilter("Vidéos",             "*.mp4", "*.avi", "*.mkv", "*.mov", "*.wmv"),
            new FileChooser.ExtensionFilter("PDF",                "*.pdf"),
            new FileChooser.ExtensionFilter("Images",             "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("Documents",          "*.docx", "*.pptx", "*.xlsx", "*.txt")
        );
        return fc.showOpenDialog(lblTitre.getScene().getWindow());
    }

    // ── Copier le fichier dans le dossier uploads du projet ─────
    private String copierFichier(File source) {
        try {
            // Dossier destination : uploads/ à côté du jar ou du projet
            Path dossierUploads = Paths.get(System.getProperty("user.home"), "EduCore_uploads");
            Files.createDirectories(dossierUploads);

            // Nom unique pour éviter les collisions
            String nomFichier = System.currentTimeMillis() + "_" + source.getName();
            Path destination = dossierUploads.resolve(nomFichier);

            Files.copy(source.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toAbsolutePath().toString();
        } catch (IOException ex) {
            showAlert("Erreur copie", "Impossible de copier le fichier : " + ex.getMessage(),
                      Alert.AlertType.ERROR);
            return source.getAbsolutePath(); // Retourner le chemin original en fallback
        }
    }

    // ── Obtenir l'extension d'un fichier ───────────────────────
    private String obtenirExtension(String nomFichier) {
        int idx = nomFichier.lastIndexOf('.');
        return idx >= 0 ? nomFichier.substring(idx + 1) : "";
    }
>>>>>>> 1b03cb2 (interface5)
}