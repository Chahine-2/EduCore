package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Reservation;
import services.ServiceReservation;
import java.time.LocalDateTime;

public class GestionReservationController {

    @FXML private TextField tfMotif;
    @FXML private TextField tfDateDebut;
    @FXML private TextField tfDateFin;
    @FXML private ComboBox<String> cbStatut;
    @FXML private TableView<Reservation> tvReservations;
    @FXML private TableColumn<Reservation, Integer> colMaterielId;
    @FXML private TableColumn<Reservation, String> colMotif;
    @FXML private TableColumn<Reservation, String> colDateDebut;
    @FXML private TableColumn<Reservation, String> colDateFin;
    @FXML private TableColumn<Reservation, String> colStatut;

    private ServiceReservation sr = new ServiceReservation();
    private Reservation reservationSelectionnee = null;

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
        Reservation r = getFromForm();
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
        Reservation r = getFromForm();
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
        sr.delete(reservationSelectionnee);
        afficherReservations(null);
        vider();
        reservationSelectionnee = null;
    }

    @FXML
    public void afficherReservations(ActionEvent e) {
        ObservableList<Reservation> list =
                FXCollections.observableArrayList(sr.getAll());
        tvReservations.setItems(list);
    }

    private Reservation getFromForm() {
        try {
            Reservation r = new Reservation();

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
        cbStatut.setValue("en_attente");
        reservationSelectionnee = null;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention");
        a.setContentText(msg);
        a.showAndWait();
    }
}