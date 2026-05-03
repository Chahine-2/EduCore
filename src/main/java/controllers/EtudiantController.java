package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
<<<<<<< HEAD
=======
import javafx.geometry.Insets;
import javafx.geometry.Pos;
>>>>>>> 1b03cb2 (interface5)
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
<<<<<<< HEAD
=======
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
>>>>>>> 1b03cb2 (interface5)
import models.Chapitre;
import models.Cours;
import services.ServiceChapitre;
import services.ServiceCours;

<<<<<<< HEAD
import java.io.IOException;        // ← manquant
=======
import java.io.IOException;
>>>>>>> 1b03cb2 (interface5)
import java.util.List;
import java.util.stream.Collectors;

public class EtudiantController {

    // ── Filtres reçus de l'Accueil ─────────────
    public static String categorieFiltre = null;
    public static String niveauFiltre    = null;

<<<<<<< HEAD
    // ── Cours ──────────────────────────────────
    @FXML private TextField         tfRecherche;
    @FXML private ComboBox<String>  cbFiltreNiveau;
    @FXML private ComboBox<String>  cbFiltreCategorie;
    @FXML private TableView<Cours>  tableViewCours;
    @FXML private TableColumn<Cours, String>  colTitre;
    @FXML private TableColumn<Cours, String>  colNiveau;
    @FXML private TableColumn<Cours, String>  colCategorie;
    @FXML private TableColumn<Cours, Integer> colDuree;
    @FXML private TableColumn<Cours, Boolean> colCertif;
    @FXML private Label lblCompteur;

    // ── Détails cours ───────────────────────────
    @FXML private Label   lblTitre;
    @FXML private Label   lblNiveau;
    @FXML private Label   lblCategorie;
    @FXML private Label   lblDuree;
    @FXML private Label   lblCertifiant;
    @FXML private TextArea taDescription;
    @FXML private TextArea taObjectifs;

    // ── Chapitres ───────────────────────────────
    @FXML private TableView<Chapitre>  tableViewChapitres;
    @FXML private TableColumn<Chapitre, Integer> colOrdre;
    @FXML private TableColumn<Chapitre, String>  colChapTitre;
    @FXML private TableColumn<Chapitre, String>  colType;
    @FXML private TableColumn<Chapitre, Integer> colDureeMin;
    @FXML private Label lblTotalChapitres;
=======
    // ── UI Components ──────────────────────────
    @FXML private TextField         tfRecherche;
    @FXML private ComboBox<String>  cbFiltreNiveau;
    @FXML private ComboBox<String>  cbFiltreCategorie;
    @FXML private FlowPane          flowPaneCours;
    @FXML private Label             lblCompteur;
>>>>>>> 1b03cb2 (interface5)

    private ServiceCours    serviceCours    = new ServiceCours();
    private ServiceChapitre serviceChapitre = new ServiceChapitre();
    private List<Cours>     tousLesCours;   // cache de tous les cours

    // ───────────────────────────────────────────
    @FXML
    void initialize() {

        // Remplir les ComboBox de filtre
        cbFiltreNiveau.getItems().addAll("Tous", "debutant", "intermediaire", "avance");
        cbFiltreCategorie.getItems().addAll("Tous", "informatique", "mecanique", "electrique");

        // Appliquer les filtres reçus de l'Accueil si disponibles
        if (categorieFiltre != null) {
            cbFiltreCategorie.setValue(categorieFiltre);
        } else {
            cbFiltreCategorie.setValue("Tous");
        }

        if (niveauFiltre != null) {
            cbFiltreNiveau.setValue(niveauFiltre);
        } else {
            cbFiltreNiveau.setValue("Tous");
        }

<<<<<<< HEAD
        // ...existing code...
        colTitre.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("titre"));
        colNiveau.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("niveau"));
        colCategorie.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("categorie"));
        colDuree.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("dureeHeures"));
        colCertif.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("estCertifiant"));

        // Configurer colonnes Chapitres
        colOrdre.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("ordre"));
        colChapTitre.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("titre"));
        colType.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("typeContenu"));
        colDureeMin.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("dureeMinutes"));

        // Listener clic sur un cours
        tableViewCours.setOnMouseClicked(this::selectCours);

=======
>>>>>>> 1b03cb2 (interface5)
        // Charger tous les cours
        chargerTousLesCours();
    }

    // ── Charger tous les cours ──────────────────
    private void chargerTousLesCours() {
        tousLesCours = serviceCours.getAll();
<<<<<<< HEAD
        tableViewCours.getItems().setAll(tousLesCours);
        lblCompteur.setText(tousLesCours.size() + " cours disponibles");

        // Appliquer les filtres immédiatement si définis
        if (categorieFiltre != null || niveauFiltre != null) {
            appliquerFiltres();
        }

        viderDetails();
    }

    // ── Sélectionner un cours ───────────────────
    private void selectCours(MouseEvent event) {
        Cours selected = tableViewCours.getSelectionModel().getSelectedItem();
        if (selected != null) {
            afficherDetails(selected);
            chargerChapitres(selected.getId());
        }
    }

    // ── Afficher les détails du cours ───────────
    private void afficherDetails(Cours c) {
        lblTitre.setText("📘 " + c.getTitre());
        lblNiveau.setText("📊 Niveau : " + c.getNiveau());
        lblCategorie.setText("🏷️ Catégorie : " + c.getCategorie());
        lblDuree.setText("⏱️ Durée : " + c.getDureeHeures() + "h");
        lblCertifiant.setText(c.isEstCertifiant() ? "🏆 Certifiant" : "");
        taDescription.setText(c.getDescription() != null ? c.getDescription() : "Aucune description");
        taObjectifs.setText(c.getObjectifs() != null ? c.getObjectifs() : "Aucun objectif");
    }

    // ── Charger les chapitres du cours ──────────
    private void chargerChapitres(int coursId) {
        List<Chapitre> chapitres = serviceChapitre.getByCours(coursId);
        tableViewChapitres.getItems().setAll(chapitres);
        lblTotalChapitres.setText(chapitres.size() + " chapitre(s)");
    }

    // ── Vider les détails ───────────────────────
    private void viderDetails() {
        lblTitre.setText("Sélectionnez un cours");
        lblNiveau.setText("");
        lblCategorie.setText("");
        lblDuree.setText("");
        lblCertifiant.setText("");
        taDescription.clear();
        taObjectifs.clear();
        tableViewChapitres.getItems().clear();
        lblTotalChapitres.setText("0 chapitre(s)");
    }
=======
        appliquerFiltres();
    }

    // ── Créer une carte de cours (Design type Dashboard) ─
    private VBox creerCarteCours(Cours cours) {
        VBox card = new VBox();
        card.setPrefWidth(280);
        card.setPrefHeight(320);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e9ecef; -fx-border-radius: 6; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 4); -fx-cursor: hand;");

        // Image Placeholder (Couleur de fond selon catégorie)
        StackPane imagePane = new StackPane();
        imagePane.setPrefHeight(140);
        imagePane.setStyle("-fx-background-radius: 6 6 0 0; " + getCouleurCategorie(cours.getCategorie()));
        
        Label lblType = new Label(cours.getCategorie().toUpperCase());
        lblType.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18; -fx-opacity: 0.8;");
        imagePane.getChildren().add(lblType);

        // Contenu de la carte
        VBox content = new VBox(8);
        content.setPadding(new Insets(15));
        
        Label title = new Label(cours.getTitre());
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #343a40;");
        title.setMinHeight(45);
        title.setAlignment(Pos.TOP_LEFT);

        Label status = new Label("Ouvert");
        status.setStyle("-fx-text-fill: #198754; -fx-font-size: 12;");

        HBox bottomInfo = new HBox();
        bottomInfo.setAlignment(Pos.CENTER_LEFT);
        Label prof = new Label("Niveau : " + cours.getNiveau());
        prof.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 12;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label icon = new Label("⭐");
        if (cours.isEstCertifiant()) {
            icon.setText("🏆");
        }
        
        bottomInfo.getChildren().addAll(prof, spacer, icon);

        // Séparateur ligne fine
        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 10 0 0 0;");

        content.getChildren().addAll(title, status, sep, bottomInfo);
        
        card.getChildren().addAll(imagePane, content);

        // Clic sur la carte -> Ouvrir le cours
        card.setOnMouseClicked(e -> ouvrirCours(cours));

        // Animation au survol
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 8); -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 8); -fx-scale-x: 1.02; -fx-scale-y: 1.02;", "")));

        return card;
    }

    private String getCouleurCategorie(String cat) {
        if (cat == null) return "-fx-background-color: #6c757d;";
        return switch (cat.toLowerCase()) {
            case "informatique" -> "-fx-background-color: #0d6efd;"; // Bleu
            case "mecanique"    -> "-fx-background-color: #fd7e14;"; // Orange
            case "electrique"   -> "-fx-background-color: #ffc107;"; // Jaune
            default             -> "-fx-background-color: #6c757d;"; // Gris
        };
    }

     // ── Ouvrir le premier chapitre du cours sélectionné ─────────
     private void ouvrirCours(Cours cours) {
         List<Chapitre> chapitres = serviceChapitre.getByCours(cours.getId());

         // Filtrer seulement les chapitres visibles pour les étudiants
         if (chapitres != null) {
             chapitres = chapitres.stream()
                     .filter(Chapitre::isVisible)
                     .collect(Collectors.toList());
         }

         if (chapitres == null || chapitres.isEmpty()) {
             Alert alert = new Alert(Alert.AlertType.INFORMATION);
             alert.setTitle("Cours vide");
             alert.setHeaderText(null);
             alert.setContentText("Ce cours ne contient aucun chapitre pour le moment.");
             alert.showAndWait();
             return;
         }

         try {
             // On charge le premier chapitre
             Chapitre premierChapitre = chapitres.get(0);

             // Passer les données au contrôleur de lecture
             LectureChapitreController.coursActuel    = cours;
             LectureChapitreController.chapitreActuel = premierChapitre;
             LectureChapitreController.tousChapitres  = chapitres;

             FXMLLoader loader = new FXMLLoader(getClass().getResource("/LectureChapitre.fxml"));
             Parent root = loader.load();
             flowPaneCours.getScene().setRoot(root);
         } catch (Exception e) {
             System.out.println("==================================================");
             System.out.println("ERREUR CRITIQUE lors de l'ouverture du chapitre :");
             e.printStackTrace();
             System.out.println("==================================================");
             
             Alert alert = new Alert(Alert.AlertType.ERROR);
             alert.setTitle("Erreur de chargement");
             alert.setHeaderText("Impossible d'ouvrir le lecteur de cours");
             alert.setContentText("Détail de l'erreur : " + e.getMessage() + "\n\nVérifiez la console pour plus de détails.");
             alert.showAndWait();
         }
     }
>>>>>>> 1b03cb2 (interface5)

    // ── Recherche par mot clé ───────────────────
    @FXML
    public void rechercherCours(KeyEvent event) {
        appliquerFiltres();
    }

    // ── Filtrer par niveau ──────────────────────
    @FXML
    public void filtrerParNiveau() {
        appliquerFiltres();
    }

    // ── Filtrer par catégorie ───────────────────
    @FXML
    public void filtrerParCategorie() {
        appliquerFiltres();
    }

<<<<<<< HEAD
    // ── Appliquer tous les filtres ensemble ─────
    private void appliquerFiltres() {
        String motCle   = tfRecherche.getText().toLowerCase();
        String niveau   = cbFiltreNiveau.getValue();
        String categorie = cbFiltreCategorie.getValue();

        List<Cours> resultats = tousLesCours.stream()
                .filter(c -> motCle.isEmpty() ||
                        c.getTitre().toLowerCase().contains(motCle) ||
                        (c.getDescription() != null &&
                                c.getDescription().toLowerCase().contains(motCle)))
                .filter(c -> niveau == null || niveau.equals("Tous") ||
                        c.getNiveau().equals(niveau))
                .filter(c -> categorie == null || categorie.equals("Tous") ||
                        c.getCategorie().equals(categorie))
                .collect(Collectors.toList());

        tableViewCours.getItems().setAll(resultats);
        lblCompteur.setText(resultats.size() + " cours trouvés");
        viderDetails();
    }
=======
     // ── Appliquer tous les filtres ensemble ─────
     private void appliquerFiltres() {
         String motCle   = tfRecherche.getText() != null ? tfRecherche.getText().toLowerCase() : "";
         String niveau   = cbFiltreNiveau.getValue();
         String categorie = cbFiltreCategorie.getValue();

         List<Cours> resultats = tousLesCours.stream()
                 .filter(c -> c.isVisible())  // Filtrer les cours visibles
                 .filter(c -> motCle.isEmpty() ||
                         c.getTitre().toLowerCase().contains(motCle) ||
                         (c.getDescription() != null &&
                                 c.getDescription().toLowerCase().contains(motCle)))
                 .filter(c -> niveau == null || niveau.equals("Tous") ||
                         c.getNiveau().equals(niveau))
                 .filter(c -> categorie == null || categorie.equals("Tous") ||
                         c.getCategorie().equals(categorie))
                 .collect(Collectors.toList());

         // Mettre à jour l'affichage
         flowPaneCours.getChildren().clear();
         for (Cours c : resultats) {
             flowPaneCours.getChildren().add(creerCarteCours(c));
         }

         lblCompteur.setText(resultats.size() + " résultats");
     }

>>>>>>> 1b03cb2 (interface5)
    @FXML
    public void retour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Accueil.fxml"));
            Parent root = loader.load();
<<<<<<< HEAD
            tableViewCours.getScene().setRoot(root);
=======
            flowPaneCours.getScene().setRoot(root);
>>>>>>> 1b03cb2 (interface5)
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
<<<<<<< HEAD
=======

>>>>>>> 1b03cb2 (interface5)
    // ── Reset tous les filtres ──────────────────
    @FXML
    public void resetFiltres() {
        tfRecherche.clear();
<<<<<<< HEAD
        cbFiltreNiveau.setValue("Tous");
        cbFiltreCategorie.setValue("Tous");
        tableViewCours.getItems().setAll(tousLesCours);
        lblCompteur.setText(tousLesCours.size() + " cours disponibles");
        viderDetails();
=======
        
        // Restaurer les valeurs sélectionnées dans Accueil.fxml s'il y en avait, sinon "Tous"
        if (categorieFiltre != null) {
            cbFiltreCategorie.setValue(categorieFiltre);
        } else {
            cbFiltreCategorie.setValue("Tous");
        }
        
        if (niveauFiltre != null) {
            cbFiltreNiveau.setValue(niveauFiltre);
        } else {
            cbFiltreNiveau.setValue("Tous");
        }
        
        appliquerFiltres();
>>>>>>> 1b03cb2 (interface5)
    }
}