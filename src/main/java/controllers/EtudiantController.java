package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import models.Chapitre;
import models.Cours;
import services.ServiceChapitre;
import services.ServiceCours;

import java.io.IOException;        // ← manquant
import java.util.List;
import java.util.stream.Collectors;

public class EtudiantController {

    // ── Filtres reçus de l'Accueil ─────────────
    public static String categorieFiltre = null;
    public static String niveauFiltre    = null;

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

        // Charger tous les cours
        chargerTousLesCours();
    }

    // ── Charger tous les cours ──────────────────
    private void chargerTousLesCours() {
        tousLesCours = serviceCours.getAll();
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
    @FXML
    public void retour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Accueil.fxml"));
            Parent root = loader.load();
            tableViewCours.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    // ── Reset tous les filtres ──────────────────
    @FXML
    public void resetFiltres() {
        tfRecherche.clear();
        cbFiltreNiveau.setValue("Tous");
        cbFiltreCategorie.setValue("Tous");
        tableViewCours.getItems().setAll(tousLesCours);
        lblCompteur.setText(tousLesCours.size() + " cours disponibles");
        viderDetails();
    }
}