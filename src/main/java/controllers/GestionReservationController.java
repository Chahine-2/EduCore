package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Reservation;
import services.ServiceReservation;

import java.io.IOException;
import java.time.LocalDate;

public class GestionReservationController {

    @FXML private TextField  tfTitre;
    @FXML private TextField  tfDescription;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private TextField  tfNbPlaces;
    @FXML private Button     btnAjouter;
    @FXML private Label      lbMessage;

    // ── Tableau ────────────────────────────────────────────────────────
    @FXML private TableView<Reservation>              tableReservation;
    @FXML private TableColumn<Reservation, String>    colTitre;
    @FXML private TableColumn<Reservation, String>    colDescription;
    @FXML private TableColumn<Reservation, LocalDate> colDateDebut;
    @FXML private TableColumn<Reservation, LocalDate> colDateFin;
    @FXML private TableColumn<Reservation, Integer>   colPlaces;

    // ── Recherche + filtre date ────────────────────────────────────────
    @FXML private TextField  tfRecherche;
    @FXML private DatePicker dpFiltreDebut;
    @FXML private DatePicker dpFiltreFin;

    private ObservableList<Reservation> toutesLesReservations = FXCollections.observableArrayList();
    private final ServiceReservation sr = new ServiceReservation();

    private static final String STYLE_OK    = "-fx-border-color: #0f9d58; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 2;";
    private static final String STYLE_ERROR = "-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 2;";
    private static final String STYLE_NONE  = "-fx-border-color: #dde1e7; -fx-border-radius: 6; -fx-background-radius: 6;";

    @FXML
    public void initialize() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("nbPlaces"));
        chargerTableau();
        attacherValidations();
    }

    private void attacherValidations() {
        tfTitre.textProperty().addListener((obs, o, newVal) -> {
            if (newVal.trim().length() >= 3)   tfTitre.setStyle(STYLE_OK);
            else if (newVal.trim().isEmpty())   tfTitre.setStyle(STYLE_NONE);
            else                                tfTitre.setStyle(STYLE_ERROR);
            validerFormulaire();
        });
        tfNbPlaces.textProperty().addListener((obs, o, newVal) -> {
            try { tfNbPlaces.setStyle(Integer.parseInt(newVal.trim()) > 0 ? STYLE_OK : STYLE_ERROR); }
            catch (NumberFormatException e) { tfNbPlaces.setStyle(newVal.trim().isEmpty() ? STYLE_NONE : STYLE_ERROR); }
            validerFormulaire();
        });
        dpDateDebut.valueProperty().addListener((obs, o, n) -> { validerDates(); validerFormulaire(); });
        dpDateFin.valueProperty().addListener((obs, o, n)   -> { validerDates(); validerFormulaire(); });
    }

    private void validerDates() {
        LocalDate debut = dpDateDebut.getValue();
        LocalDate fin   = dpDateFin.getValue();
        if (debut != null && fin != null) {
            String style = fin.isAfter(debut) ? STYLE_OK : STYLE_ERROR;
            dpDateDebut.setStyle(style);
            dpDateFin.setStyle(style);
        }
    }

    private void validerFormulaire() {
        boolean titreOk = tfTitre.getText().trim().length() >= 3;
        boolean placesOk;
        try { placesOk = Integer.parseInt(tfNbPlaces.getText().trim()) > 0; }
        catch (NumberFormatException e) { placesOk = false; }
        boolean datesOk = dpDateDebut.getValue() != null && dpDateFin.getValue() != null
                && dpDateFin.getValue().isAfter(dpDateDebut.getValue());
        btnAjouter.setDisable(!(titreOk && placesOk && datesOk));
    }

    private void chargerTableau() {
        toutesLesReservations = FXCollections.observableArrayList(sr.getAll());
        tableReservation.setItems(toutesLesReservations);
        if (tfRecherche   != null) tfRecherche.clear();
        if (dpFiltreDebut != null) dpFiltreDebut.setValue(null);
        if (dpFiltreFin   != null) dpFiltreFin.setValue(null);
    }

    // ── Appliquer tous les filtres actifs (texte + dates) ─────────────
    private void appliquerFiltres() {
        String motCle    = tfRecherche   != null ? tfRecherche.getText().toLowerCase().trim() : "";
        LocalDate debut  = dpFiltreDebut != null ? dpFiltreDebut.getValue() : null;
        LocalDate fin    = dpFiltreFin   != null ? dpFiltreFin.getValue()   : null;

        ObservableList<Reservation> filtrees = FXCollections.observableArrayList();
        for (Reservation r : toutesLesReservations) {

            // Filtre texte
            boolean matchTexte = motCle.isEmpty()
                    || r.getTitre().toLowerCase().contains(motCle)
                    || (r.getDescription() != null && r.getDescription().toLowerCase().contains(motCle));

            // Filtre date : la réservation doit chevaucher la période choisie
            boolean matchDate = true;
            if (debut != null && r.getDateFin()   != null && r.getDateFin().isBefore(debut))   matchDate = false;
            if (fin   != null && r.getDateDebut() != null && r.getDateDebut().isAfter(fin))     matchDate = false;

            if (matchTexte && matchDate) filtrees.add(r);
        }
        tableReservation.setItems(filtrees);
    }

    // ── Recherche texte (onKeyReleased) ───────────────────────────────
    @FXML
    public void rechercherReservation(javafx.scene.input.KeyEvent event) {
        appliquerFiltres();
    }

    // ── Filtre par date (onAction des DatePicker) ─────────────────────
    @FXML
    public void filtrerParDate(ActionEvent event) {
        appliquerFiltres();
    }

    // ── Effacer tous les filtres ──────────────────────────────────────
    @FXML
    public void effacerFiltres(ActionEvent event) {
        if (tfRecherche   != null) tfRecherche.clear();
        if (dpFiltreDebut != null) dpFiltreDebut.setValue(null);
        if (dpFiltreFin   != null) dpFiltreFin.setValue(null);
        tableReservation.setItems(toutesLesReservations);
        info("ℹ Filtres effacés — liste complète affichée.");
    }

    // ── CRUD ──────────────────────────────────────────────────────────
    @FXML
    public void ajouterReservation(ActionEvent event) {
        if (tableReservation.getSelectionModel().getSelectedItem() != null) {
            erreur("⚠ Désélectionnez la ligne — cliquez sur un endroit vide du tableau !");
            return;
        }
        try {
            Reservation r = new Reservation();
            r.setTitre(tfTitre.getText().trim());
            r.setDescription(tfDescription.getText().trim());
            r.setDateDebut(dpDateDebut.getValue());
            r.setDateFin(dpDateFin.getValue());
            r.setNbPlaces(Integer.parseInt(tfNbPlaces.getText().trim()));
            sr.add(r);
            succes("✔ Réservation ajoutée avec succès !");
            viderFormulaire();
            chargerTableau();
        } catch (NumberFormatException e) { erreur("⚠ Nb places doit être un entier !"); }
    }

    @FXML
    public void modifierReservation(ActionEvent event) {
        Reservation sel = tableReservation.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez une réservation à modifier !"); return; }
        if (tfTitre.getText().trim().length() < 3) { erreur("⚠ Titre min 3 caractères !"); return; }
        if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) { erreur("⚠ Dates obligatoires !"); return; }
        if (!dpDateFin.getValue().isAfter(dpDateDebut.getValue())) { erreur("⚠ Date fin doit être après date début !"); return; }
        try {
            int nbPlaces = Integer.parseInt(tfNbPlaces.getText().trim());
            if (nbPlaces <= 0) { erreur("⚠ Nb places doit être > 0 !"); return; }
            boolean rienChange =
                    tfTitre.getText().trim().equals(sel.getTitre()) &&
                            tfDescription.getText().trim().equals(sel.getDescription() == null ? "" : sel.getDescription()) &&
                            dpDateDebut.getValue().equals(sel.getDateDebut()) &&
                            dpDateFin.getValue().equals(sel.getDateFin()) &&
                            nbPlaces == sel.getNbPlaces();
            if (rienChange) { avertissement("⚠ Aucune modification détectée !"); return; }
            sel.setTitre(tfTitre.getText().trim());
            sel.setDescription(tfDescription.getText().trim());
            sel.setDateDebut(dpDateDebut.getValue());
            sel.setDateFin(dpDateFin.getValue());
            sel.setNbPlaces(nbPlaces);
            sr.update(sel);
            succes("✔ Réservation modifiée avec succès !");
            viderFormulaire();
            chargerTableau();
        } catch (NumberFormatException e) { erreur("⚠ Nb places doit être un entier !"); }
    }

    @FXML
    public void supprimerReservation(ActionEvent event) {
        Reservation sel = tableReservation.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez une réservation !"); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer cette réservation ?");
        alert.setContentText("\"" + sel.getTitre() + "\" sera supprimée définitivement.");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                sr.delete(sel);
                succes("✔ Réservation supprimée !");
                viderFormulaire();
                chargerTableau();
            }
        });
    }

    @FXML
    public void selectionnerLigne(javafx.scene.input.MouseEvent event) {
        Reservation s = tableReservation.getSelectionModel().getSelectedItem();
        if (s != null) {
            tfTitre.setText(s.getTitre());
            tfDescription.setText(s.getDescription() == null ? "" : s.getDescription());
            dpDateDebut.setValue(s.getDateDebut());
            dpDateFin.setValue(s.getDateFin());
            tfNbPlaces.setText(String.valueOf(s.getNbPlaces()));
            info("ℹ Ligne sélectionnée — modifiez et cliquez Modifier.");
        }
    }

    // ── Navigation ────────────────────────────────────────────────────
    @FXML public void allerAcaton(ActionEvent event) {
        try { tfTitre.getScene().setRoot(FXMLLoader.load(getClass().getResource("/GestionHackathon.fxml"))); }
        catch (IOException e) { System.out.println(e.getMessage()); }
    }
    @FXML public void allerDashboard(ActionEvent event) {
        try { tfTitre.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Dashboard.fxml"))); }
        catch (IOException e) { System.out.println(e.getMessage()); }
    }
    @FXML public void allerPaiement(ActionEvent event) {
        try { tfTitre.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Paiement.fxml"))); }
        catch (IOException e) { System.out.println(e.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private void erreur(String m)        { lbMessage.setStyle("-fx-text-fill: #d93025; -fx-font-style: italic;"); lbMessage.setText(m); }
    private void succes(String m)        { lbMessage.setStyle("-fx-text-fill: #0f9d58; -fx-font-style: italic;"); lbMessage.setText(m); }
    private void info(String m)          { lbMessage.setStyle("-fx-text-fill: #1a73e8; -fx-font-style: italic;"); lbMessage.setText(m); }
    private void avertissement(String m) { lbMessage.setStyle("-fx-text-fill: #f9ab00; -fx-font-style: italic;"); lbMessage.setText(m); }

    private void viderFormulaire() {
        tfTitre.clear();       tfTitre.setStyle(STYLE_NONE);
        tfDescription.clear();
        dpDateDebut.setValue(null); dpDateDebut.setStyle("");
        dpDateFin.setValue(null);   dpDateFin.setStyle("");
        tfNbPlaces.clear();    tfNbPlaces.setStyle(STYLE_NONE);
        tableReservation.getSelectionModel().clearSelection();
        btnAjouter.setDisable(false);
    }
}