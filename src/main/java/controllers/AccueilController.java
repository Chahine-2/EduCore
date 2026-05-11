package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.NavigationManager;

import java.io.IOException;
import java.util.Objects;

@SuppressWarnings({
    "FieldCanBeLocal",  // Les champs @FXML sont assignés par le framework
    "unused"            // Les méthodes sont appelées par FXML
})
public class AccueilController {

    // ── Catégorie ──────────────────────────────
    @FXML private ToggleButton tbInfo;
    @FXML private ToggleButton tbMeca;
    @FXML private ToggleButton tbElec;

    // ── Niveau ─────────────────────────────────
    @FXML private ToggleButton tbDebutant;
    @FXML private ToggleButton tbIntermediaire;
    @FXML private ToggleButton tbAvance;

    // ── Résumé + Bouton ────────────────────────
    @FXML private Label  lblSelection;
    @FXML private Button btnContinuer;
    @FXML private Button btnRetourAccueil;
    @FXML private Button btnEspaceEnseignant;
    @FXML private HBox accueilFooterBar;
    @FXML private VBox accueilTopChrome;

    private boolean embeddedInDashboard;
    private Runnable onBackToDashboard;
    /** When non-null, catalogue opens here instead of a new window or full-scene navigation. */
    private Runnable onOpenCatalogEmbedded;

    private String categorieChoisie = null;
    private String niveauChoisi      = null;

    public void setDashboardEmbedMode(boolean enabled, Runnable backToDashboard) {
        setDashboardEmbedMode(enabled, backToDashboard, null);
    }

    /**
     * When Accueil is embedded in the student dashboard, back stays in the shell.
     * If {@code onOpenCatalogEmbedded} is set, "Continuer" runs it instead of opening a separate window.
     */
    public void setDashboardEmbedMode(boolean enabled, Runnable backToDashboard, Runnable onOpenCatalogEmbedded) {
        this.embeddedInDashboard = enabled;
        this.onBackToDashboard = backToDashboard;
        this.onOpenCatalogEmbedded = onOpenCatalogEmbedded;
        if (!enabled) {
            return;
        }
        if (accueilTopChrome != null) {
            accueilTopChrome.setVisible(false);
            accueilTopChrome.setManaged(false);
        }
        if (btnEspaceEnseignant != null) {
            btnEspaceEnseignant.setVisible(false);
            btnEspaceEnseignant.setManaged(false);
        }
        if (accueilFooterBar != null) {
            accueilFooterBar.setVisible(false);
            accueilFooterBar.setManaged(false);
        }
    }

    // ── Sélection catégorie ─────────────────────
    @FXML
    public void selectionnerCategorie(ActionEvent event) {
        // Désactiver les autres
        tbInfo.setSelected(false);
        tbMeca.setSelected(false);
        tbElec.setSelected(false);

        ToggleButton clique = (ToggleButton) event.getSource();
        clique.setSelected(true);

        styleNormal(tbInfo);
        styleNormal(tbMeca);
        styleNormal(tbElec);
        styleSelectionne(clique);

        if (clique == tbInfo)  categorieChoisie = "informatique";
        if (clique == tbMeca)  categorieChoisie = "mecanique";
        if (clique == tbElec)  categorieChoisie = "electrique";

        mettreAJourResume();
    }

    // ── Sélection niveau ────────────────────────
    @FXML
    public void selectionnerNiveau(ActionEvent event) {
        // Désactiver les autres
        tbDebutant.setSelected(false);
        tbIntermediaire.setSelected(false);
        tbAvance.setSelected(false);

        ToggleButton clique = (ToggleButton) event.getSource();
        clique.setSelected(true);

        styleNormal(tbDebutant);
        styleNormal(tbIntermediaire);
        styleNormal(tbAvance);
        styleSelectionne(clique);

        if (clique == tbDebutant)       niveauChoisi = "debutant";
        if (clique == tbIntermediaire)  niveauChoisi = "intermediaire";
        if (clique == tbAvance)         niveauChoisi = "avance";

        mettreAJourResume();
    }

    // ── Mettre à jour le résumé ─────────────────
    private void mettreAJourResume() {
        String cat = categorieChoisie != null ? "✅ " + categorieChoisie : "❌ Catégorie non choisie";
        String niv = niveauChoisi != null ? "✅ " + niveauChoisi : "❌ Niveau non choisi";
        lblSelection.setText(cat + "     |     " + niv);

        btnContinuer.setDisable(categorieChoisie == null || niveauChoisi == null);

        if (categorieChoisie != null && niveauChoisi != null) {
            btnContinuer.setStyle(
                    "-fx-background-color: #27ae60; -fx-text-fill: white;"
                            + "-fx-font-size: 16; -fx-padding: 15 50;"
                            + "-fx-background-radius: 10; -fx-font-weight: bold;"
            );
        }
    }

    private void styleSelectionne(ToggleButton tb) {
        tb.setStyle(
                "-fx-background-color: #1a5276; -fx-text-fill: white;"
                        + "-fx-font-size: 14; -fx-padding: 15 30; -fx-background-radius: 10;"
                        + "-fx-font-weight: bold;"
        );
    }

    private void styleNormal(ToggleButton tb) {
        tb.setStyle(
                "-fx-font-size: 14; -fx-padding: 15 30; -fx-background-radius: 10;"
        );
    }

    // ── Continuer vers l'espace étudiant ────────
    @FXML
    public void continuer(ActionEvent event) {
        try {
            EtudiantController.categorieFiltre = categorieChoisie;
            EtudiantController.niveauFiltre = niveauChoisi;

            if (embeddedInDashboard) {
                if (onOpenCatalogEmbedded != null) {
                    onOpenCatalogEmbedded.run();
                } else {
                    openEtudiantCatalogInNewWindow(event);
                }
                return;
            }

            Scene scene = btnContinuer.getScene();
            if (scene != null) {
                NavigationManager.navigateTo(scene, "/Etudiant.fxml");
            } else {
                System.out.println("⚠️ Erreur : Scène non trouvée");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openEtudiantCatalogInNewWindow(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Etudiant.fxml")));
        Stage stage = new Stage();
        stage.setTitle("EduCore — Catalogue de cours");
        stage.setScene(new Scene(root, 1000, 720));
        Node src = event != null && event.getSource() instanceof Node n ? n : btnContinuer;
        if (src.getScene() != null && src.getScene().getWindow() != null) {
            stage.initOwner(src.getScene().getWindow());
        }
        stage.show();
    }

    // ── Aller vers l'espace enseignant ──────────
    @FXML
    public void ouvrirEspaceEnseignant(ActionEvent event) {
        try {
            Scene scene = null;
            if (event.getSource() instanceof Button btn) {
                scene = btn.getScene();
            }
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

    @FXML
    public void retourStartup(ActionEvent event) {
        if (embeddedInDashboard && onBackToDashboard != null) {
            onBackToDashboard.run();
            return;
        }
        try {
            Scene scene = null;
            if (event.getSource() instanceof Button btn) {
                scene = btn.getScene();
            }
            if (scene != null) {
                NavigationManager.navigateTo(scene, "/startup.fxml");
            } else {
                System.out.println("⚠️ Erreur : Scène non trouvée");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}