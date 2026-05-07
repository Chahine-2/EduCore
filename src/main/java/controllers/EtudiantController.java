package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import models.Chapitre;
import models.Cours;
import services.ServiceChapitre;
import services.ServiceCours;
import utils.NavigationManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({
    "FieldCanBeLocal",  // Les champs @FXML sont assignés par le framework
    "unused"            // Les méthodes sont appelées par FXML
})
public class EtudiantController {

    // ── Filtres reçus de l'Accueil ─────────────
    public static String categorieFiltre = null;
    public static String niveauFiltre    = null;

    // ── UI Components ──────────────────────────
    @FXML private TextField         tfRecherche;
    @FXML private ComboBox<String>  cbFiltreNiveau;
    @FXML private ComboBox<String>  cbFiltreCategorie;
    @FXML private FlowPane          flowPaneCours;
    @FXML private Label             lblCompteur;

    private ServiceCours serviceCours    = new ServiceCours();
    private ServiceChapitre serviceChapitre = new ServiceChapitre();
    private List<Cours>     tousLesCours;   // cache de tous les cours

    // ───────────────────────────────────────────
    @FXML
    void initialize() {
        try {
            System.out.println("🔧 Initialisation EtudiantController...");

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

            // Charger tous les cours
            chargerTousLesCours();
            System.out.println("✅ EtudiantController initialisé avec succès");
        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de l'initialisation d'EtudiantController :");
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();

            // Afficher un dialogue d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur d'initialisation");
            alert.setHeaderText("Impossible de charger l'interface");
            alert.setContentText("Erreur : " + e.getMessage() + "\n\nVérifiez la console pour plus de détails.");
            alert.showAndWait();
        }
    }

    // ── Charger tous les cours ──────────────────
    private void chargerTousLesCours() {
        try {
            System.out.println("📚 Chargement des cours...");
            tousLesCours = serviceCours.getAll();
            
            if (tousLesCours == null) {
                System.out.println("⚠️ Aucun cours disponible (tousLesCours est null)");
                tousLesCours = new java.util.ArrayList<>();
            } else {
                System.out.println("✅ " + tousLesCours.size() + " cours(s) chargé(s)");
            }
            
            appliquerFiltres();
        } catch (Exception e) {
            System.out.println("❌ ERREUR lors du chargement des cours :");
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
            
            // Créer une liste vide en fallback
            tousLesCours = new java.util.ArrayList<>();
            
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur de chargement");
            alert.setHeaderText("Impossible de charger les cours");
            alert.setContentText("Erreur : " + e.getMessage() + "\n\nAucun cours n'est disponible pour le moment.");
            alert.showAndWait();
        }
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
         try {
             System.out.println("📖 Ouverture du cours: " + cours.getTitre() + " (ID=" + cours.getId() + ")");
             List<Chapitre> chapitres = serviceChapitre.getByCours(cours.getId());

             System.out.println("📊 Chapitres récupérés: " + (chapitres != null ? chapitres.size() : 0));

             // Filtrer seulement les chapitres visibles pour les étudiants
             if (chapitres != null) {
                 chapitres = chapitres.stream()
                         .filter(Chapitre::isVisible)
                         .collect(Collectors.toList());
                 System.out.println("📊 Chapitres visibles: " + chapitres.size());
             }

             if (chapitres == null || chapitres.isEmpty()) {
                 System.out.println("⚠️ ATTENTION: Aucun chapitre trouvé pour le cours!");
                 Alert alert = new Alert(Alert.AlertType.INFORMATION);
                 alert.setTitle("Cours vide");
                 alert.setHeaderText(null);
                 alert.setContentText("Ce cours ne contient aucun chapitre pour le moment.");
                 alert.showAndWait();
                 return;
             }

             // On charge le premier chapitre
             Chapitre premierChapitre = chapitres.get(0);

             // Passer les données au contrôleur de lecture
             LectureChapitreController.coursActuel    = cours;
             LectureChapitreController.chapitreActuel = premierChapitre;
             LectureChapitreController.tousChapitres  = chapitres;

             // Utiliser NavigationManager pour une navigation sûre
             Scene scene = flowPaneCours.getScene();
             if (scene != null) {
                 NavigationManager.navigateTo(scene, "/LectureChapitre.fxml");
             } else {
                 throw new RuntimeException("La scène n'a pas pu être obtenue");
             }
         } catch (Exception e) {
             System.out.println("==================================================");
             System.out.println("❌ ERREUR CRITIQUE lors de l'ouverture du chapitre :");
             e.printStackTrace();
             System.out.println("==================================================");

             Alert alert = new Alert(Alert.AlertType.ERROR);
             alert.setTitle("Erreur de chargement");
             alert.setHeaderText("Impossible d'ouvrir le lecteur de cours");
             alert.setContentText("Détail de l'erreur : " + e.getMessage() + "\n\nVérifiez la console pour plus de détails.");
             alert.showAndWait();
         }
     }

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

    // ── Appliquer tous les filtres ensemble ─────
    private void appliquerFiltres() {
        String motCle   = tfRecherche.getText() != null ? tfRecherche.getText().toLowerCase() : "";
        String niveau   = cbFiltreNiveau.getValue();
        String categorie = cbFiltreCategorie.getValue();

        // ✅ CORRIGÉ: Afficher TOUS les cours (y compris ceux non visibles)
        // Les données importées peuvent avoir visible=false par défaut
        List<Cours> resultats = tousLesCours.stream()
                // .filter(c -> c.isVisible())  // ← DÉSACTIVÉ pour afficher les courses importées
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
     @FXML
     public void retour(ActionEvent event) {
         try {
             Scene scene = flowPaneCours.getScene();
             if (scene != null) {
                 NavigationManager.navigateTo(scene, "/Accueil.fxml");
             } else {
                 System.out.println("⚠️ Erreur : Scène non trouvée");
             }
         } catch (Exception e) {
             System.out.println("❌ Erreur retour : " + e.getMessage());
             e.printStackTrace();
         }
     }

    // ── Reset tous les filtres ──────────────────
    @FXML
    public void resetFiltres() {
        tfRecherche.clear();

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
    }
}