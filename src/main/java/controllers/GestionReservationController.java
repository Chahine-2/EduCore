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

    @FXML private TableView<Reservation>              tableReservation;
    @FXML private TableColumn<Reservation, String>    colTitre;
    @FXML private TableColumn<Reservation, String>    colDescription;
    @FXML private TableColumn<Reservation, LocalDate> colDateDebut;
    @FXML private TableColumn<Reservation, LocalDate> colDateFin;
    @FXML private TableColumn<Reservation, Integer>   colPlaces;

    @FXML private TextField  tfRecherche;
    @FXML private DatePicker dpFiltreDebut;
    @FXML private DatePicker dpFiltreFin;

    private ObservableList<Reservation> toutesLesReservations = FXCollections.observableArrayList();
    private final ServiceReservation sr = new ServiceReservation();

    private static final String OK   = "-fx-border-color:#0f9d58;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:2;";
    private static final String ERR  = "-fx-border-color:#d93025;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:2;";
    private static final String WARN = "-fx-border-color:#f9ab00;-fx-border-radius:6;-fx-background-radius:6;-fx-border-width:2;";
    private static final String NONE = "-fx-border-color:#dde1e7;-fx-border-radius:6;-fx-background-radius:6;";

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

        // ── Titre : min 3, max 100, pas que chiffres, pas que espaces ──
        tfTitre.textProperty().addListener((obs, o, n) -> {
            String v = n.trim();
            if (v.isEmpty()) {
                tfTitre.setStyle(NONE);
            } else if (v.length() < 3) {
                tfTitre.setStyle(ERR); infoChamp("Titre : minimum 3 caractères.");
            } else if (v.length() > 100) {
                tfTitre.setText(n.substring(0, 100));
                tfTitre.setStyle(WARN); infoChamp("Titre : maximum 100 caractères atteint.");
            } else if (v.matches("\\d+")) {
                tfTitre.setStyle(ERR); infoChamp("Titre : ne peut pas contenir uniquement des chiffres.");
            } else if (!v.matches(".*[a-zA-ZÀ-ÿ].*")) {
                tfTitre.setStyle(ERR); infoChamp("Titre : doit contenir au moins une lettre.");
            } else {
                tfTitre.setStyle(OK); lbMessage.setText("");
            }
            validerFormulaire();
        });

        // ── Description : optionnelle, si remplie min 5, max 300 ───────
        tfDescription.textProperty().addListener((obs, o, n) -> {
            String v = n.trim();
            if (v.isEmpty()) {
                tfDescription.setStyle(NONE);
            } else if (v.length() < 5) {
                tfDescription.setStyle(ERR); infoChamp("Description : minimum 5 caractères.");
            } else if (v.length() > 300) {
                tfDescription.setText(n.substring(0, 300));
                tfDescription.setStyle(WARN); infoChamp("Description : maximum 300 caractères atteint.");
            } else {
                tfDescription.setStyle(OK); lbMessage.setText("");
            }
            validerFormulaire();
        });

        // ── Nb places : chiffres uniquement, entre 1 et 10 000 ─────────
        tfNbPlaces.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) {
                tfNbPlaces.setText(n.replaceAll("[^\\d]", "")); return;
            }
            if (n.isEmpty()) {
                tfNbPlaces.setStyle(NONE);
            } else {
                try {
                    int v = Integer.parseInt(n);
                    if (v <= 0) {
                        tfNbPlaces.setStyle(ERR); infoChamp("Nb places : doit être supérieur à 0.");
                    } else if (v > 10000) {
                        tfNbPlaces.setStyle(ERR); infoChamp("Nb places : maximum 10 000 autorisées.");
                    } else {
                        tfNbPlaces.setStyle(OK); lbMessage.setText("");
                    }
                } catch (NumberFormatException e) { tfNbPlaces.setStyle(ERR); }
            }
            validerFormulaire();
        });

        // ── Dates ───────────────────────────────────────────────────────
        dpDateDebut.valueProperty().addListener((obs, o, n) -> { validerDates(); validerFormulaire(); });
        dpDateFin.valueProperty().addListener((obs, o, n)   -> { validerDates(); validerFormulaire(); });
    }

    private void validerDates() {
        LocalDate debut = dpDateDebut.getValue();
        LocalDate fin   = dpDateFin.getValue();
        LocalDate today = LocalDate.now();
        if (debut != null) {
            if (debut.isBefore(today)) {
                dpDateDebut.setStyle(ERR); infoChamp("Date début : ne peut pas être dans le passé."); return;
            } else {
                dpDateDebut.setStyle(OK);
            }
        }
        if (fin != null) {
            if (fin.isBefore(today)) {
                dpDateFin.setStyle(ERR); infoChamp("Date fin : ne peut pas être dans le passé."); return;
            }
            if (debut != null && !fin.isAfter(debut)) {
                dpDateFin.setStyle(ERR); dpDateDebut.setStyle(ERR);
                infoChamp("Date fin : doit être strictement après la date de début."); return;
            }
            if (debut != null) { dpDateFin.setStyle(OK); dpDateDebut.setStyle(OK); lbMessage.setText(""); }
        }
    }

    private void validerFormulaire() {
        String titre = tfTitre.getText().trim();
        boolean titreOk = titre.length() >= 3 && titre.length() <= 100
                && !titre.matches("\\d+") && titre.matches(".*[a-zA-ZÀ-ÿ].*");
        String desc = tfDescription.getText().trim();
        boolean descOk = desc.isEmpty() || (desc.length() >= 5 && desc.length() <= 300);
        boolean placesOk = false;
        try { int v = Integer.parseInt(tfNbPlaces.getText().trim()); placesOk = v > 0 && v <= 10000; }
        catch (NumberFormatException ignored) {}
        LocalDate debut = dpDateDebut.getValue(), fin = dpDateFin.getValue(), today = LocalDate.now();
        boolean datesOk = debut != null && fin != null && !debut.isBefore(today) && fin.isAfter(debut);
        btnAjouter.setDisable(!(titreOk && descOk && placesOk && datesOk));
    }

    private void appliquerFiltres() {
        String motCle   = tfRecherche   != null ? tfRecherche.getText().toLowerCase().trim() : "";
        LocalDate debut = dpFiltreDebut != null ? dpFiltreDebut.getValue() : null;
        LocalDate fin   = dpFiltreFin   != null ? dpFiltreFin.getValue()   : null;
        ObservableList<Reservation> filtrees = FXCollections.observableArrayList();
        for (Reservation r : toutesLesReservations) {
            boolean matchTexte = motCle.isEmpty()
                    || r.getTitre().toLowerCase().contains(motCle)
                    || (r.getDescription() != null && r.getDescription().toLowerCase().contains(motCle));
            boolean matchDate = true;
            if (debut != null && r.getDateFin()   != null && r.getDateFin().isBefore(debut))  matchDate = false;
            if (fin   != null && r.getDateDebut() != null && r.getDateDebut().isAfter(fin))   matchDate = false;
            if (matchTexte && matchDate) filtrees.add(r);
        }
        tableReservation.setItems(filtrees);
    }

    @FXML public void rechercherReservation(javafx.scene.input.KeyEvent e) { appliquerFiltres(); }
    @FXML public void filtrerParDate(ActionEvent e)  { appliquerFiltres(); }
    @FXML public void effacerFiltres(ActionEvent e)  {
        if (tfRecherche   != null) tfRecherche.clear();
        if (dpFiltreDebut != null) dpFiltreDebut.setValue(null);
        if (dpFiltreFin   != null) dpFiltreFin.setValue(null);
        tableReservation.setItems(toutesLesReservations);
        info("ℹ Filtres effacés — liste complète affichée.");
    }

    private void chargerTableau() {
        toutesLesReservations = FXCollections.observableArrayList(sr.getAll());
        tableReservation.setItems(toutesLesReservations);
        if (tfRecherche   != null) tfRecherche.clear();
        if (dpFiltreDebut != null) dpFiltreDebut.setValue(null);
        if (dpFiltreFin   != null) dpFiltreFin.setValue(null);
    }

    @FXML
    public void ajouterReservation(ActionEvent event) {
        if (tableReservation.getSelectionModel().getSelectedItem() != null) {
            erreur("⚠ Désélectionnez la ligne avant d'ajouter !"); return;
        }
        String titre = tfTitre.getText().trim();
        boolean titreExiste = toutesLesReservations.stream()
                .anyMatch(r -> r.getTitre().equalsIgnoreCase(titre));
        if (titreExiste) { erreur("⚠ Une réservation avec ce titre existe déjà !"); tfTitre.setStyle(ERR); return; }
        try {
            Reservation r = new Reservation();
            r.setTitre(titre);
            r.setDescription(tfDescription.getText().trim());
            r.setDateDebut(dpDateDebut.getValue());
            r.setDateFin(dpDateFin.getValue());
            r.setNbPlaces(Integer.parseInt(tfNbPlaces.getText().trim()));
            sr.add(r);
            succes("✔ Réservation \"" + titre + "\" ajoutée avec succès !");
            viderFormulaire(); chargerTableau();
        } catch (NumberFormatException e) { erreur("⚠ Nb places doit être un entier valide !"); }
    }

    @FXML
    public void modifierReservation(ActionEvent event) {
        Reservation sel = tableReservation.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez une réservation à modifier !"); return; }
        String titre = tfTitre.getText().trim();
        if (titre.length() < 3)          { erreur("⚠ Titre : minimum 3 caractères !"); return; }
        if (titre.length() > 100)         { erreur("⚠ Titre : maximum 100 caractères !"); return; }
        if (titre.matches("\\d+"))        { erreur("⚠ Titre : ne peut pas être uniquement des chiffres !"); return; }
        if (!titre.matches(".*[a-zA-ZÀ-ÿ].*")) { erreur("⚠ Titre : doit contenir au moins une lettre !"); return; }
        String desc = tfDescription.getText().trim();
        if (!desc.isEmpty() && desc.length() < 5) { erreur("⚠ Description : minimum 5 caractères !"); return; }
        if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) { erreur("⚠ Les deux dates sont obligatoires !"); return; }
        LocalDate today = LocalDate.now();
        if (dpDateDebut.getValue().isBefore(today)) { erreur("⚠ Date début : ne peut pas être dans le passé !"); return; }
        if (!dpDateFin.getValue().isAfter(dpDateDebut.getValue())) { erreur("⚠ Date fin : doit être après la date de début !"); return; }
        try {
            int nbPlaces = Integer.parseInt(tfNbPlaces.getText().trim());
            if (nbPlaces <= 0)    { erreur("⚠ Nb places : doit être supérieur à 0 !"); return; }
            if (nbPlaces > 10000) { erreur("⚠ Nb places : maximum 10 000 !"); return; }
            boolean titreExiste = toutesLesReservations.stream()
                    .anyMatch(r -> r.getTitre().equalsIgnoreCase(titre) && r.getId() != sel.getId());
            if (titreExiste) { erreur("⚠ Ce titre est déjà utilisé par une autre réservation !"); return; }
            boolean rienChange = titre.equals(sel.getTitre())
                    && desc.equals(sel.getDescription() == null ? "" : sel.getDescription())
                    && dpDateDebut.getValue().equals(sel.getDateDebut())
                    && dpDateFin.getValue().equals(sel.getDateFin())
                    && nbPlaces == sel.getNbPlaces();
            if (rienChange) { avertissement("⚠ Aucune modification détectée !"); return; }
            sel.setTitre(titre); sel.setDescription(desc);
            sel.setDateDebut(dpDateDebut.getValue()); sel.setDateFin(dpDateFin.getValue());
            sel.setNbPlaces(nbPlaces);
            sr.update(sel);
            succes("✔ Réservation modifiée avec succès !"); viderFormulaire(); chargerTableau();
        } catch (NumberFormatException e) { erreur("⚠ Nb places doit être un entier valide !"); }
    }

    @FXML
    public void supprimerReservation(ActionEvent event) {
        Reservation sel = tableReservation.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez une réservation à supprimer !"); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer \"" + sel.getTitre() + "\" ?");
        alert.setContentText("Cette action est irréversible !");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                sr.delete(sel); succes("✔ Réservation supprimée !"); viderFormulaire(); chargerTableau();
            }
        });
    }

    @FXML
    public void selectionnerLigne(javafx.scene.input.MouseEvent event) {
        Reservation s = tableReservation.getSelectionModel().getSelectedItem();
        if (s != null) {
            tfTitre.setText(s.getTitre());
            tfDescription.setText(s.getDescription() == null ? "" : s.getDescription());
            dpDateDebut.setValue(s.getDateDebut()); dpDateFin.setValue(s.getDateFin());
            tfNbPlaces.setText(String.valueOf(s.getNbPlaces()));
            info("ℹ \"" + s.getTitre() + "\" sélectionné — modifiez et cliquez Modifier.");
        }
    }

    @FXML public void allerAcaton(ActionEvent e) {
        try { tfTitre.getScene().setRoot(FXMLLoader.load(getClass().getResource("/GestionHackathon.fxml"))); }
        catch (IOException ex) { System.out.println(ex.getMessage()); }
    }
    @FXML public void allerDashboard(ActionEvent e) {
        try { tfTitre.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Dashboard.fxml"))); }
        catch (IOException ex) { System.out.println(ex.getMessage()); }
    }
    @FXML public void allerPaiement(ActionEvent e) {
        try { tfTitre.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Paiement.fxml"))); }
        catch (IOException ex) { System.out.println(ex.getMessage()); }
    }

    private void erreur(String m)        { lbMessage.setStyle("-fx-text-fill:#d93025;-fx-font-weight:bold;"); lbMessage.setText(m); }
    private void succes(String m)        { lbMessage.setStyle("-fx-text-fill:#0f9d58;-fx-font-weight:bold;"); lbMessage.setText(m); }
    private void info(String m)          { lbMessage.setStyle("-fx-text-fill:#1a73e8;-fx-font-style:italic;"); lbMessage.setText(m); }
    private void avertissement(String m) { lbMessage.setStyle("-fx-text-fill:#f9ab00;-fx-font-weight:bold;"); lbMessage.setText(m); }
    private void infoChamp(String m)     { lbMessage.setStyle("-fx-text-fill:#5f6368;-fx-font-style:italic;"); lbMessage.setText(m); }

    private void viderFormulaire() {
        tfTitre.clear(); tfTitre.setStyle(NONE);
        tfDescription.clear(); tfDescription.setStyle(NONE);
        dpDateDebut.setValue(null); dpDateDebut.setStyle("");
        dpDateFin.setValue(null);   dpDateFin.setStyle("");
        tfNbPlaces.clear(); tfNbPlaces.setStyle(NONE);
        tableReservation.getSelectionModel().clearSelection();
        btnAjouter.setDisable(false); lbMessage.setText("");
    }
}