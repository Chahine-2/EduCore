package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import models.Hackathon;
import services.ServiceHackathon;

import java.io.IOException;

public class GestionHackathonController {

    @FXML private TextField tfNom;
    @FXML private TextField tfCategorie;
    @FXML private TextField tfDuree;
    @FXML private TextField tfPrix;
    @FXML private TextField tfReservationId;
    @FXML private TextField tfRecherche;
    @FXML private Button    btnAjouter;

    @FXML private TableView<Hackathon>            tableAcaton;
    @FXML private TableColumn<Hackathon, String>  colNom;
    @FXML private TableColumn<Hackathon, String>  colCategorie;
    @FXML private TableColumn<Hackathon, Integer> colDuree;
    @FXML private TableColumn<Hackathon, Double>  colPrix;
    @FXML private TableColumn<Hackathon, Integer> colReservationId;

    @FXML private Label lbMessage;

    private final ServiceHackathon sa = new ServiceHackathon();
    private ObservableList<Hackathon> tousLesHackathons = FXCollections.observableArrayList();

    private static final String STYLE_OK    = "-fx-border-color: #0f9d58; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 2;";
    private static final String STYLE_ERROR = "-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 2;";
    private static final String STYLE_NONE  = "-fx-border-color: #dde1e7; -fx-border-radius: 6; -fx-background-radius: 6;";

    @FXML
    public void initialize() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colReservationId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        chargerTableau();
        attacherValidations();
    }

    private void attacherValidations() {
        tfNom.textProperty().addListener((obs, o, newVal) -> {
            if (newVal.trim().length() >= 3)   tfNom.setStyle(STYLE_OK);
            else if (newVal.trim().isEmpty())   tfNom.setStyle(STYLE_NONE);
            else                                tfNom.setStyle(STYLE_ERROR);
            validerFormulaire();
        });
        tfDuree.textProperty().addListener((obs, o, newVal) -> {
            try { tfDuree.setStyle(Integer.parseInt(newVal.trim()) > 0 ? STYLE_OK : STYLE_ERROR); }
            catch (NumberFormatException e) { tfDuree.setStyle(newVal.trim().isEmpty() ? STYLE_NONE : STYLE_ERROR); }
            validerFormulaire();
        });
        tfPrix.textProperty().addListener((obs, o, newVal) -> {
            try { tfPrix.setStyle(Double.parseDouble(newVal.trim()) >= 0 ? STYLE_OK : STYLE_ERROR); }
            catch (NumberFormatException e) { tfPrix.setStyle(newVal.trim().isEmpty() ? STYLE_NONE : STYLE_ERROR); }
            validerFormulaire();
        });
        tfReservationId.textProperty().addListener((obs, o, newVal) -> {
            try { tfReservationId.setStyle(Integer.parseInt(newVal.trim()) > 0 ? STYLE_OK : STYLE_ERROR); }
            catch (NumberFormatException e) { tfReservationId.setStyle(newVal.trim().isEmpty() ? STYLE_NONE : STYLE_ERROR); }
            validerFormulaire();
        });
    }

    private void validerFormulaire() {
        boolean nomOk = tfNom.getText().trim().length() >= 3;
        boolean dureeOk, prixOk, idOk;
        try { dureeOk = Integer.parseInt(tfDuree.getText().trim()) > 0; } catch (NumberFormatException e) { dureeOk = false; }
        try { prixOk  = Double.parseDouble(tfPrix.getText().trim()) >= 0; } catch (NumberFormatException e) { prixOk = false; }
        try { idOk    = Integer.parseInt(tfReservationId.getText().trim()) > 0; } catch (NumberFormatException e) { idOk = false; }
        btnAjouter.setDisable(!(nomOk && dureeOk && prixOk && idOk));
    }

    private void chargerTableau() {
        tousLesHackathons = FXCollections.observableArrayList(sa.getAll());
        tableAcaton.setItems(tousLesHackathons);
        if (tfRecherche != null) tfRecherche.clear();
    }

    // ── Recherche ──────────────────────────────────────────────────────
    @FXML
    public void rechercherHackathon(KeyEvent event) {
        String motCle = tfRecherche.getText().toLowerCase().trim();
        if (motCle.isEmpty()) {
            tableAcaton.setItems(tousLesHackathons);
        } else {
            ObservableList<Hackathon> filtres = FXCollections.observableArrayList();
            for (Hackathon h : tousLesHackathons) {
                if (h.getNom().toLowerCase().contains(motCle) ||
                        (h.getCategorie() != null && h.getCategorie().toLowerCase().contains(motCle))) {
                    filtres.add(h);
                }
            }
            tableAcaton.setItems(filtres);
        }
    }

    @FXML
    public void ajouterAcaton(ActionEvent event) {
        if (tableAcaton.getSelectionModel().getSelectedItem() != null) {
            erreur("⚠ Désélectionnez la ligne — cliquez sur un endroit vide du tableau !");
            return;
        }
        try {
            Hackathon a = new Hackathon();
            a.setNom(tfNom.getText().trim());
            a.setCategorie(tfCategorie.getText().trim());
            a.setDuree(Integer.parseInt(tfDuree.getText().trim()));
            a.setPrix(Double.parseDouble(tfPrix.getText().trim()));
            a.setReservationId(Integer.parseInt(tfReservationId.getText().trim()));
            sa.add(a);
            succes("✔ Hackathon ajouté avec succès !");
            viderFormulaire();
            chargerTableau();
        } catch (NumberFormatException e) { erreur("⚠ Durée et ID = entiers, Prix = décimal !"); }
    }

    @FXML
    public void modifierAcaton(ActionEvent event) {
        Hackathon sel = tableAcaton.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez un hackathon à modifier !"); return; }
        if (tfNom.getText().trim().length() < 3) { erreur("⚠ Nom min 3 caractères !"); return; }
        try {
            int    duree = Integer.parseInt(tfDuree.getText().trim());
            double prix  = Double.parseDouble(tfPrix.getText().trim());
            int    resId = Integer.parseInt(tfReservationId.getText().trim());
            if (duree <= 0) { erreur("⚠ Durée doit être > 0 !"); return; }
            if (prix  <  0) { erreur("⚠ Prix ne peut pas être négatif !"); return; }
            if (resId <= 0) { erreur("⚠ ID Réservation doit être > 0 !"); return; }

            boolean rienChange =
                    tfNom.getText().trim().equals(sel.getNom()) &&
                            tfCategorie.getText().trim().equals(sel.getCategorie() == null ? "" : sel.getCategorie()) &&
                            duree == sel.getDuree() && prix == sel.getPrix() && resId == sel.getReservationId();
            if (rienChange) { avertissement("⚠ Aucune modification détectée !"); return; }

            sel.setNom(tfNom.getText().trim());
            sel.setCategorie(tfCategorie.getText().trim());
            sel.setDuree(duree);
            sel.setPrix(prix);
            sel.setReservationId(resId);
            sa.update(sel);
            succes("✔ Hackathon modifié avec succès !");
            viderFormulaire();
            chargerTableau();
        } catch (NumberFormatException e) { erreur("⚠ Durée et ID = entiers, Prix = décimal !"); }
    }

    @FXML
    public void supprimerAcaton(ActionEvent event) {
        Hackathon sel = tableAcaton.getSelectionModel().getSelectedItem();
        if (sel == null) { erreur("⚠ Sélectionnez un hackathon !"); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer ce hackathon ?");
        alert.setContentText("\"" + sel.getNom() + "\" sera supprimé définitivement.");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                sa.delete(sel);
                succes("✔ Hackathon supprimé !");
                viderFormulaire();
                chargerTableau();
            }
        });
    }

    @FXML
    public void selectionnerLigne(javafx.scene.input.MouseEvent event) {
        Hackathon s = tableAcaton.getSelectionModel().getSelectedItem();
        if (s != null) {
            tfNom.setText(s.getNom());
            tfCategorie.setText(s.getCategorie() == null ? "" : s.getCategorie());
            tfDuree.setText(String.valueOf(s.getDuree()));
            tfPrix.setText(String.valueOf(s.getPrix()));
            tfReservationId.setText(s.getReservationId() == 0 ? "" : String.valueOf(s.getReservationId()));
            info("ℹ Ligne sélectionnée — modifiez et cliquez Modifier.");
        }
    }

    @FXML
    public void allerReservation(ActionEvent event) {
        try { tfNom.getScene().setRoot(FXMLLoader.load(getClass().getResource("/GestionReservation.fxml"))); }
        catch (IOException e) { System.out.println(e.getMessage()); }
    }

    @FXML
    public void allerDashboard(ActionEvent event) {
        try { tfNom.getScene().setRoot(FXMLLoader.load(getClass().getResource("/Dashboard.fxml"))); }
        catch (IOException e) { System.out.println(e.getMessage()); }
    }

    private void erreur(String m)        { lbMessage.setStyle("-fx-text-fill: #d93025; -fx-font-style: italic;"); lbMessage.setText(m); }
    private void succes(String m)        { lbMessage.setStyle("-fx-text-fill: #0f9d58; -fx-font-style: italic;"); lbMessage.setText(m); }
    private void info(String m)          { lbMessage.setStyle("-fx-text-fill: #1a73e8; -fx-font-style: italic;"); lbMessage.setText(m); }
    private void avertissement(String m) { lbMessage.setStyle("-fx-text-fill: #f9ab00; -fx-font-style: italic;"); lbMessage.setText(m); }

    private void viderFormulaire() {
        tfNom.clear();           tfNom.setStyle(STYLE_NONE);
        tfCategorie.clear();
        tfDuree.clear();         tfDuree.setStyle(STYLE_NONE);
        tfPrix.clear();          tfPrix.setStyle(STYLE_NONE);
        tfReservationId.clear(); tfReservationId.setStyle(STYLE_NONE);
        tableAcaton.getSelectionModel().clearSelection();
        btnAjouter.setDisable(false);
    }
}