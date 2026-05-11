package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import models.Chapitre;
import models.Cours;
import models.FeedbackEtudiant;
import services.ServiceChapitre;
import services.ServiceFeedback;
import utils.NavigationManager;

import java.io.File;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Optional;

@SuppressWarnings({
    "FieldCanBeLocal",  // Les champs @FXML sont assignés par le framework
    "unused"            // Les méthodes sont appelées par FXML
})
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
    @FXML private TableColumn<Chapitre, Boolean> colVisible;
    @FXML private Label lblTotalChapitres;
    @FXML private Button btnAjouterChapitre;
    @FXML private Button btnModifierChapitre;
    @FXML private Button btnSupprimerChapitre;
    @FXML private ListView<FeedbackEtudiant> listFeedbacks;
    @FXML private Label lblTotalFeedbacks;

    private Chapitre chapitreEnEdition = null;
    private ServiceChapitre serviceChapitre = new ServiceChapitre();
    private final ServiceFeedback serviceFeedback = new ServiceFeedback();

    private Runnable embeddedBackToGestion;

    public void setTeacherDashboardEmbedMode(boolean enabled, Runnable backToGestion) {
        this.embeddedBackToGestion = (enabled && backToGestion != null) ? backToGestion : null;
    }

    @FXML
    void initialize() {
        try {
            System.out.println("🔧 Initialisation DetailsCoursController...");
            System.out.println("📊 État du cours statique au démarrage:");
            System.out.println("   - cours = " + cours);
            System.out.println("   - cours.getId() = " + (cours != null ? cours.getId() : "NULL"));
            System.out.println("   - cours.getTitre() = " + (cours != null ? cours.getTitre() : "NULL"));

            // Configurer les colonnes de la TableView
            colOrdre.setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("ordre"));
            colTitreChap.setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("titre"));
            colType.setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("typeContenu"));
            colDureeMin.setCellValueFactory(
                    new javafx.scene.control.cell.PropertyValueFactory<>("dureeMinutes"));
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

            // Ajouter listener pour sélectionner une ligne
            tableViewChapitres.setOnMouseClicked(this::selectChapitreInTable);

            afficherDetails();
            chargerChapitres();
            chargerFeedbacks();

            System.out.println("✅ DetailsCoursController initialisé avec succès");
        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de l'initialisation de DetailsCoursController :");
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'initialisation");
            alert.setHeaderText("Impossible de charger l'interface");
            alert.setContentText("Erreur : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void afficherDetails() {
        System.out.println("🎯 afficherDetails() appelé");
        System.out.println("   - cours = " + cours);
        System.out.println("   - cours.getId() = " + (cours != null ? cours.getId() : "NULL"));
        System.out.println("   - cours.getTitre() = " + (cours != null ? cours.getTitre() : "NULL"));
        
        if (cours == null) {
            System.out.println("⚠️ AVERTISSEMENT: cours est NULL!");
            lblTitre.setText("❌ Cours non chargé");
            return;
        }
        
        if (cours.getId() <= 0) {
            System.out.println("⚠️ AVERTISSEMENT: cours.id=" + cours.getId() + " (invalide!)");
        }
        
        lblTitre.setText("📘 " + (cours.getTitre() != null ? cours.getTitre() : "N/A"));
        lblNiveau.setText("Niveau : " + (cours.getNiveau() != null ? cours.getNiveau() : "N/A"));
        lblCategorie.setText("Catégorie : " + (cours.getCategorie() != null ? cours.getCategorie() : "N/A"));
        lblDuree.setText("Durée : " + cours.getDureeHeures() + " heures");
        lblCertifiant.setText("Certifiant : " + (cours.isEstCertifiant() ? "Oui ✅" : "Non ❌"));
        taDescription.setText(cours.getDescription() != null ? cours.getDescription() : "N/A");
        taObjectifs.setText(cours.getObjectifs() != null ? cours.getObjectifs() : "N/A");
    }

    private void chargerChapitres() {
        System.out.println("📖 chargerChapitres() appelé");
        System.out.println("   - cours.getId() = " + (cours != null ? cours.getId() : "NULL"));
        
        if (cours == null || cours.getId() <= 0) {
            System.out.println("⚠️ IMPOSSIBLE de charger les chapitres: cours.id invalide!");
            tableViewChapitres.getItems().clear();
            lblTotalChapitres.setText("Total chapitres : 0 (❌ Cours non sélectionné)");
            return;
        }
        
        java.util.List<Chapitre> chapitres = serviceChapitre.getByCours(cours.getId());
        System.out.println("   - Chapitres trouvés: " + (chapitres != null ? chapitres.size() : 0));
        tableViewChapitres.getItems().setAll(chapitres);
        mettreAJourCompteur();
        chargerFeedbacks();
    }

    private void mettreAJourCompteur() {
        int total = tableViewChapitres.getItems() != null ? tableViewChapitres.getItems().size() : 0;
        String status = (cours != null && cours.getId() > 0) ? "" : " (❌ Cours non valide)";
        lblTotalChapitres.setText("Total chapitres : " + total + status);
    }

    private void selectChapitreInTable(MouseEvent event) {
        Chapitre selected = tableViewChapitres.getSelectionModel().getSelectedItem();
        if (selected != null) {
            chapitreEnEdition = selected;
        }
    }

    private void chargerFeedbacks() {
        if (listFeedbacks == null || lblTotalFeedbacks == null) return;
        if (cours == null || cours.getId() <= 0) {
            listFeedbacks.getItems().clear();
            lblTotalFeedbacks.setText("0");
            return;
        }

        java.util.List<FeedbackEtudiant> feedbacks = serviceFeedback.getFeedbacksByCours(cours.getId());
        listFeedbacks.getItems().setAll(feedbacks);
        lblTotalFeedbacks.setText(String.valueOf(feedbacks.size()));
        listFeedbacks.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(FeedbackEtudiant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getCoursTitre() + " : " + item.getChapitreTitre() + " : \"" + item.getMessage() + "\"");
                    setStyle("-fx-wrap-text: true; -fx-padding: 10 8;");
                }
            }
        });
    }

    @FXML
    public void actualiserFeedbacks(ActionEvent event) {
        chargerFeedbacks();
    }

    @FXML
    public void ajouterChapitre(ActionEvent event) {
        // ✅ VÉRIFICATION CRITIQUE: Le cours doit avoir un ID valide
        System.out.println("🔍 DEBUG: cours = " + cours);
        System.out.println("🔍 DEBUG: cours.getId() = " + (cours != null ? cours.getId() : "NULL"));
        System.out.println("🔍 DEBUG: cours.getTitre() = " + (cours != null ? cours.getTitre() : "NULL"));

        if (cours == null || cours.getId() <= 0) {
            System.out.println("❌ ERREUR CRITIQUE: Le cours n'a pas un ID valide!");
            showAlert("Erreur", "❌ ERREUR: Le cours n'a pas pu être correctement chargé.\n" +
                    "Assurez-vous de sélectionner un cours valide dans la liste.", Alert.AlertType.ERROR);
            return;
        }

        Dialog<Chapitre> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un Chapitre");
        dialog.setHeaderText("Créer un nouveau chapitre");

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
        grid.add(new Label("Fichier / URL:"), 0, 5);
        grid.add(hboxFichier, 1, 5);
        grid.add(new Label("Visible pour les étudiants:"), 0, 6);
        CheckBox cbVisible = new CheckBox("Oui");
        cbVisible.setSelected(true);
        grid.add(cbVisible, 1, 6);

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
                ch.setVisible(cbVisible.isSelected());
                ch.setDateCreation(LocalDate.now());
                ch.setCoursId(cours.getId());
                return ch;
            }
            return null;
        });

        Optional<Chapitre> result = dialog.showAndWait();
        if (result.isPresent()) {
            Chapitre chapitre = result.get();
            System.out.println("📝 Tentative d'ajout du chapitre: " + chapitre.getTitre() +
                             " pour cours_id=" + chapitre.getCoursId());

            boolean success = serviceChapitre.addChapitre(chapitre);

            if (success) {
                chargerChapitres();
                tableViewChapitres.refresh();
                showAlert("Succès", "✅ Chapitre ajouté avec succès!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "❌ Impossible d'ajouter le chapitre. Consultez la console pour les détails.", Alert.AlertType.ERROR);
            }
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
        grid.add(new Label("Fichier / URL:"), 0, 5);
        grid.add(hboxFichier, 1, 5);
        grid.add(new Label("Visible pour les étudiants:"), 0, 6);
        CheckBox cbVisible = new CheckBox("Oui");
        cbVisible.setSelected(chapitreEnEdition.isVisible());
        grid.add(cbVisible, 1, 6);

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
                chapitreEnEdition.setVisible(cbVisible.isSelected());
                return chapitreEnEdition;
            }
            return null;
        });

        Optional<Chapitre> result = dialog.showAndWait();
        if (result.isPresent()) {
            boolean success = serviceChapitre.updateChapitre(result.get());
            if (success) {
                // Mettre à jour l'item dans la table directement sans recharger
                tableViewChapitres.refresh();  // Rafraîchit l'affichage de tous les items
                chapitreEnEdition = null;
                tableViewChapitres.getSelectionModel().clearSelection();
                showAlert("Succès", "✅ Chapitre modifié avec succès!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "❌ Impossible de modifier le chapitre. Consultez la console pour les détails.", Alert.AlertType.ERROR);
            }
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
            tableViewChapitres.refresh();
            chapitreEnEdition = null;
            tableViewChapitres.getSelectionModel().clearSelection();
            showAlert("Succès", "✅ Chapitre supprimé avec succès!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    public void retour(ActionEvent event) {
        try {
            if (embeddedBackToGestion != null) {
                embeddedBackToGestion.run();
                return;
            }
            Scene scene = lblTitre.getScene();
            if (scene != null) {
                NavigationManager.navigateTo(scene, "/GestionCours.fxml");
            } else {
                System.out.println("⚠️ Erreur : Scène non trouvée");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



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
}