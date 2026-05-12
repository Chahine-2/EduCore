package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Materiel;
import models.ReservationMateriel;
import services.ServiceMateriel;
import services.ServiceReservationMateriel;
import java.time.LocalDateTime;

public class GestionReservationMaterielController {

    @FXML private TextField tfMotif;
    @FXML private TextField tfDateDebut;
    @FXML private TextField tfDateFin;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TableView<ReservationMateriel> tvReservations;
    @FXML private TableColumn<ReservationMateriel, String> colMotif;
    @FXML private TableColumn<ReservationMateriel, String> colDateDebut;
    @FXML private TableColumn<ReservationMateriel, String> colDateFin;
    @FXML private TableColumn<ReservationMateriel, String> colStatut;

    private ServiceReservationMateriel sr = new ServiceReservationMateriel();
    private ServiceMateriel sm = new ServiceMateriel();
    private ReservationMateriel reservationSelectionnee = null;

    @FXML
    void initialize() {
        cbStatut.setItems(FXCollections.observableArrayList(
                "en_attente", "confirmee", "annulee"
        ));
        cbStatut.setValue("en_attente");

        colMotif.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("motif"));
        colDateDebut.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dateFin"));
        colStatut.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("statut"));

        tvReservations.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        reservationSelectionnee = newVal;
                        tfMotif.setText(newVal.getMotif());
                        tfDateDebut.setText(newVal.getDateDebut().toString());
                        tfDateFin.setText(newVal.getDateFin().toString());
                        cbStatut.setValue(newVal.getStatut());
                    }
                });
        afficherReservations(null);
    }

    @FXML
    public void ajouterReservation(ActionEvent e) {
        Integer materielId = getFirstDisponibleMaterielId();
        if (materielId == null) {
            showAlert("Aucun matériel disponible pour une nouvelle réservation !");
            return;
        }
        ReservationMateriel r = getFromForm(materielId);
        if (r == null) return;
        sr.add(r);
        afficherReservations(null);
        vider();
    }

    @FXML
    public void modifierReservation(ActionEvent e) {
        if (reservationSelectionnee == null) {
            showAlert("Selectionnez une reservation !");
            return;
        }
        ReservationMateriel r = getFromForm(reservationSelectionnee.getMaterielId());
        if (r == null) return;
        r.setId(reservationSelectionnee.getId());
        sr.update(r);
        afficherReservations(null);
        vider();
    }

    @FXML
    public void supprimerReservation(ActionEvent e) {
        if (reservationSelectionnee == null) {
            showAlert("Selectionnez une reservation !");
            return;
        }
        sr.delete(reservationSelectionnee.getId());
        afficherReservations(null);
        vider();
        reservationSelectionnee = null;
    }

    @FXML
    public void afficherReservations(ActionEvent e) {
        ObservableList<ReservationMateriel> list =
                FXCollections.observableArrayList(sr.getAll());
        tvReservations.setItems(list);
    }

    private ReservationMateriel getFromForm(int materielId) {
        try {
            ReservationMateriel r = new ReservationMateriel();
            r.setMaterielId(materielId);
            r.setMotif(tfMotif.getText().trim());
            r.setDateDebut(LocalDateTime.parse(tfDateDebut.getText().trim()));
            r.setDateFin(LocalDateTime.parse(tfDateFin.getText().trim()));
            r.setStatut(cbStatut.getValue());
            return r;
        } catch (Exception ex) {
            showAlert("Erreur : verifiez les champs !\nFormat date : yyyy-MM-ddTHH:mm");
            return null;
        }
    }

    private void vider() {
        tfDateDebut.clear(); tfDateFin.clear();
        tfMotif.clear();
        cbStatut.setValue("en_attente");
        reservationSelectionnee = null;
    }

    private Integer getFirstDisponibleMaterielId() {
        for (Materiel m : sm.getAll()) {
            if (m.getEtat() != null && m.getEtat().equalsIgnoreCase("disponible")) {
                return m.getId();
            }
        }
        return null;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention");
        a.setContentText(msg);
        a.showAndWait();
    }
}