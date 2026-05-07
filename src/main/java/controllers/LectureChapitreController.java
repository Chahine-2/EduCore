package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Chapitre;
import models.Cours;
import utils.NavigationManager;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
@SuppressWarnings({
    "FieldCanBeLocal",  // Les champs @FXML sont assignés par le framework
    "unused",           // Les méthodes sont appelées par FXML
    "UnusedAssignment"  // Certaines variables assignées par FXML
})
public class LectureChapitreController {

    // ── Données partagées depuis EtudiantController ──────────────
    public static Cours       coursActuel    = null;
    public static Chapitre    chapitreActuel = null;
    public static List<Chapitre> tousChapitres = null;

    // ── Navbar ───────────────────────────────────────────────────
    @FXML private Label        lblNomCours;
    @FXML private Label        lblTitreNavbar;
    @FXML private ProgressBar  progressBar;
    @FXML private Label        lblProgression;
    @FXML private Button       btnPrecedent;
    @FXML private Button       btnSuivant;

    // ── Sidebar ──────────────────────────────────────────────────
    @FXML private ListView<Chapitre> listViewSommaire;
    @FXML private Label              lblSommaireTotal;

    // ── Contenu principal ────────────────────────────────────────
    @FXML private Label   lblNumero;
    @FXML private Label   lblTitreChapitre;
    @FXML private Label   lblBadgeType;
    @FXML private Label   lblDureeEstimee;
    @FXML private Label   lblDescription;
    @FXML private VBox    boxSupport;
    @FXML private Label   lblUrl;


    // Boutons bas de page
    @FXML private Button btnPrevBottom;
    @FXML private Button btnNextBottom;

    private int indexActuel = 0;

    // ─────────────────────────────────────────────────────────────
    @FXML
    void initialize() {
        try {
            System.out.println("🔧 Initialisation LectureChapitreController...");

            if (tousChapitres == null || tousChapitres.isEmpty()) {
                System.out.println("⚠️ Aucun chapitre disponible");
                return;
            }

            // Trouver l'index du chapitre actuel
            if (chapitreActuel != null) {
                for (int i = 0; i < tousChapitres.size(); i++) {
                    if (tousChapitres.get(i).getId() == chapitreActuel.getId()) {
                        indexActuel = i;
                        break;
                    }
                }
            }

             // Configurer le sommaire (affichage personnalisé)
             listViewSommaire.setCellFactory(lv -> new ListCell<Chapitre>() {
                 @Override
                 protected void updateItem(Chapitre ch, boolean empty) {
                     super.updateItem(ch, empty);
                     if (empty || ch == null) {
                         setText(null);
                         setGraphic(null);
                         setStyle("-fx-background-color: transparent;");
                     } else {
                         String icone = getIconeType(ch.getTypeContenu());
                         // Ajouter un badge "Masqué" si le chapitre n'est pas visible
                         String masque = !ch.isVisible() ? " 👁‍🗨" : "";
                         setText(icone + "  " + ch.getOrdre() + ". " + ch.getTitre() + masque);
                         setStyle(
                             "-fx-text-fill: #475569;" +
                             "-fx-font-size: 13;" +
                             "-fx-padding: 10 15;" +
                             "-fx-background-color: transparent;" +
                             "-fx-border-color: transparent;" +
                             "-fx-cursor: hand;"
                         );

                         // Style conditionnel pour l'élément sélectionné
                         if (isSelected()) {
                             setStyle(getStyle() + "-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; -fx-font-weight: bold; -fx-background-radius: 6;");
                         }
                     }
                 }
             });

            // Remplir le sommaire
            listViewSommaire.getItems().setAll(tousChapitres);
            lblSommaireTotal.setText(tousChapitres.size() + " chapitre(s)");

            // Afficher le chapitre courant
            afficherChapitre(indexActuel);
            System.out.println("✅ LectureChapitreController initialisé avec succès");
        } catch (Exception e) {
            System.out.println("❌ ERREUR lors de l'initialisation de LectureChapitreController :");
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Afficher un chapitre par index ───────────────────────────
    private void afficherChapitre(int index) {
        if (index < 0 || index >= tousChapitres.size()) return;

        indexActuel = index;
        Chapitre ch = tousChapitres.get(index);
        chapitreActuel = ch;

        // ─ Navbar ─
        if (coursActuel != null) {
            lblNomCours.setText("📚 " + coursActuel.getTitre());
        }
        lblTitreNavbar.setText(ch.getTitre());

        // ─ Progression ─
        double pct = (double)(index + 1) / tousChapitres.size();
        progressBar.setProgress(pct);
        lblProgression.setText((index + 1) + " / " + tousChapitres.size());

        // ─ Boutons navigation ─
        btnPrecedent.setDisable(index == 0);
        btnPrevBottom.setDisable(index == 0);
        btnSuivant.setDisable(index == tousChapitres.size() - 1);
        btnNextBottom.setDisable(index == tousChapitres.size() - 1);

        // ─ Sélectionner dans le sommaire ─
        listViewSommaire.getSelectionModel().select(index);
        listViewSommaire.scrollTo(index);

        // ─ Contenu ─
        lblNumero.setText("CHAPITRE " + ch.getOrdre());
        lblTitreChapitre.setText(ch.getTitre());
        lblBadgeType.setText(getLabelType(ch.getTypeContenu()));
        lblBadgeType.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4338ca; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 20;");
        lblDureeEstimee.setText("⏱  " + ch.getDureeMinutes() + " min");
        lblDescription.setText(
            ch.getDescription() != null && !ch.getDescription().isEmpty()
                ? ch.getDescription()
                : "Aucune description disponible pour ce chapitre."
        );

        // ─ URL / Support ─
        boolean hasUrl = ch.getUrlContenu() != null && !ch.getUrlContenu().isEmpty();
        boxSupport.setVisible(hasUrl);
        boxSupport.setManaged(hasUrl);
        if (hasUrl) {
            String url = ch.getUrlContenu();
            // N'afficher que le nom du fichier s'il s'agit d'un chemin local
            String displayName = url;
            if (url.contains("/") || url.contains("\\")) {
                int lastIndex = Math.max(url.lastIndexOf("/"), url.lastIndexOf("\\"));
                displayName = url.substring(lastIndex + 1);
            }
            lblUrl.setText(displayName);
        }
    }

    // ── Clic sur le sommaire ─────────────────────────────────────
    @FXML
    private void selectionnerDepuisSommaire(MouseEvent event) {
        int idx = listViewSommaire.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {  // Toujours afficher le chapitre au clic, peu importe quel chapitre
            afficherChapitre(idx);
            listViewSommaire.refresh(); // Pour rafraîchir le style sélectionné
        }
    }

    // ── Navigation Précédent ─────────────────────────────────────
    @FXML
    public void chapitrePrec(ActionEvent event) {
        if (indexActuel > 0) {
            afficherChapitre(indexActuel - 1);
            listViewSommaire.refresh();
        }
    }

    // ── Navigation Suivant ───────────────────────────────────────
    @FXML
    public void chapitreNext(ActionEvent event) {
        if (indexActuel < tousChapitres.size() - 1) {
            afficherChapitre(indexActuel + 1);
            listViewSommaire.refresh();
        }
    }

    // ── Retour à la liste des cours ──────────────────────────
    @FXML
    public void retour(ActionEvent event) {
        try {
            Scene scene = lblTitreChapitre.getScene();
            if (scene != null) {
                NavigationManager.navigateTo(scene, "/Etudiant.fxml");
            } else {
                System.out.println("⚠️ Erreur : Scène non trouvée");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur retour : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Actions sur le support ───────────────────────────────────
    @FXML
    public void ouvrirSupport(ActionEvent event) {
        if (chapitreActuel != null && chapitreActuel.getUrlContenu() != null) {
            String url = chapitreActuel.getUrlContenu();
            try {
                if (url.startsWith("http")) {
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    File file = new File(url);
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        showAlert("Erreur", "Fichier introuvable", "Le fichier spécifié n'existe pas : " + url);
                    }
                }
            } catch (Exception e) {
                showAlert("Erreur", "Ouverture impossible", "Impossible d'ouvrir le support : " + e.getMessage());
            }
        }
    }
    @FXML
    public void ouvrirChatBot(ActionEvent event) {
        try {
            // Passer les données au ChatBot
            ChatBotController.coursActuel    = coursActuel;
            ChatBotController.chapitreActuel = chapitreActuel;

            NavigationManager.openNewWindow("/ChatBot.fxml", "🤖 Assistant IA — " + chapitreActuel.getTitre());
        } catch (Exception e) {
            System.out.println("❌ Erreur ChatBot : " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'assistant", e.getMessage());
        }
    }
    @FXML
    public void telechargerSupport(ActionEvent event) {
        // Le téléchargement peut être une simple ouverture ou copie.
        // On va réutiliser l'ouverture car Desktop.open() gère souvent le téléchargement/visualisation pour les fichiers.
        if (chapitreActuel != null && chapitreActuel.getUrlContenu() != null) {
            String url = chapitreActuel.getUrlContenu();
            try {
                if (url.startsWith("http")) {
                    // Si c'est un lien web, on l'ouvre dans le navigateur
                    Desktop.getDesktop().browse(new URI(url));
                } else {
                    // Si c'est un fichier local
                    File file = new File(url);
                    if (file.exists()) {
                        showAlert("Téléchargement", "Support disponible", "Le fichier est déjà présent localement. Nous allons l'ouvrir pour vous.");
                        Desktop.getDesktop().open(file);
                    } else {
                        showAlert("Erreur", "Fichier introuvable", "Le fichier spécifié n'existe pas : " + url);
                    }
                }
            } catch (Exception e) {
                showAlert("Erreur", "Téléchargement impossible", "Impossible de traiter la demande : " + e.getMessage());
            }
        }
    }
    
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ── Helpers ──────────────────────────────────────────────────
    private String getIconeType(String type) {
        if (type == null) return "📄";
        return switch (type.toLowerCase()) {
            case "video"  -> "🎬";
            case "pdf"    -> "📕";
            case "quiz"   -> "❓";
            case "texte"  -> "📝";
            default       -> "📄";
        };
    }

    private String getLabelType(String type) {
        if (type == null) return "Contenu";
        return switch (type.toLowerCase()) {
            case "video"  -> "🎬  Vidéo";
            case "pdf"    -> "📕  PDF";
            case "quiz"   -> "❓  Quiz";
            case "texte"  -> "📝  Texte";
            default       -> "📄  " + type;
        };
    }
}
