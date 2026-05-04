package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import utils.NavigationManager;

import java.io.IOException;

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

    private String categorieChoisie = null;
    private String niveauChoisi      = null;

    // ── Sélection catégorie ─────────────────────
    @FXML
    public void selectionnerCategorie(ActionEvent event) {
        // Désactiver les autres
        tbInfo.setSelected(false);
        tbMeca.setSelected(false);
        tbElec.setSelected(false);

        ToggleButton clique = (ToggleButton) event.getSource();
        clique.setSelected(true);

        // Appliquer style sélectionné
        styleNormal(tbInfo); styleNormal(tbMeca); styleNormal(tbElec);
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

        // Appliquer style sélectionné
        styleNormal(tbDebutant); styleNormal(tbIntermediaire); styleNormal(tbAvance);
        styleSelectionne(clique);

        if (clique == tbDebutant)       niveauChoisi = "debutant";
        if (clique == tbIntermediaire)  niveauChoisi = "intermediaire";
        if (clique == tbAvance)         niveauChoisi = "avance";

        mettreAJourResume();
    }

    // ── Mettre à jour le résumé ─────────────────
    private void mettreAJourResume() {
        String cat  = categorieChoisie != null ? "✅ " + categorieChoisie : "❌ Catégorie non choisie";
        String niv  = niveauChoisi     != null ? "✅ " + niveauChoisi     : "❌ Niveau non choisi";
        lblSelection.setText(cat + "     |     " + niv);

        // Activer bouton seulement si les deux sont choisis
        btnContinuer.setDisable(categorieChoisie == null || niveauChoisi == null);

        if (categorieChoisie != null && niveauChoisi != null) {
            btnContinuer.setStyle(
                    "-fx-background-color: #27ae60; -fx-text-fill: white;" +
                            "-fx-font-size: 16; -fx-padding: 15 50;" +
                            "-fx-background-radius: 10; -fx-font-weight: bold;"
            );
        }
    }

    // ── Continuer vers l'espace étudiant ────────
    @FXML
    public void continuer(ActionEvent event) {
        try {
            // Passer les choix au EtudiantController
            EtudiantController.categorieFiltre = categorieChoisie;
            EtudiantController.niveauFiltre    = niveauChoisi;

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

    // ── Styles ──────────────────────────────────
    private void styleSelectionne(ToggleButton tb) {
        tb.setStyle(
                "-fx-background-color: #1a5276; -fx-text-fill: white;" +
                        "-fx-font-size: 14; -fx-padding: 15 30; -fx-background-radius: 10;" +
                        "-fx-font-weight: bold;"
        );
    }

    private void styleNormal(ToggleButton tb) {
        tb.setStyle(
                "-fx-font-size: 14; -fx-padding: 15 30; -fx-background-radius: 10;"
        );
    }
}