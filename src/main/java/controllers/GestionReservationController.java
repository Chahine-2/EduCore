package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Reservation;
import services.ServiceReservation;

import java.io.IOException;
import java.time.LocalDate;

public class GestionReservationController {

    // ----- champs du formulaire -----
    @FXML private TextField tfTitre;
    @FXML private TextField tfDescription;
    @FXML private DatePicker dpDateDebut;
    @FXML private DatePicker dpDateFin;
    @FXML private TextField tfNbPlaces;

    // ----- TableView -----
    @FXML private TableView<Reservation> tableReservation;
    @FXML private TableColumn<Reservation, Integer> colId;
    @FXML private TableColumn<Reservation, String>  colTitre;
    @FXML private TableColumn<Reservation, String>  colDescription;
    @FXML private TableColumn<Reservation, LocalDate> colDateDebut;
    @FXML private TableColumn<Reservation, LocalDate> colDateFin;
    @FXML private TableColumn<Reservation, Integer> colPlaces;

    @FXML private Label lbMessage;

    private final ServiceReservation sr = new ServiceReservation();

    // appelé automatiquement au chargement du FXML
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colPlaces.setCellValueFactory(new PropertyValueFactory<>("nbPlaces"));
        chargerTableau();
    }

    private void chargerTableau() {
        ObservableList<Reservation> data =
                FXCollections.observableArrayList(sr.getAll());
        tableReservation.setItems(data);
    }

    @FXML
    public void ajouterReservation(ActionEvent event) {
        if (tfTitre.getText().isEmpty() || dpDateDebut.getValue() == null) {
            lbMessage.setText("Veuillez remplir tous les champs !");
            return;
        }
        Reservation r = new Reservation();
        r.setTitre(tfTitre.getText());
        r.setDescription(tfDescription.getText());
        r.setDateDebut(dpDateDebut.getValue());
        r.setDateFin(dpDateFin.getValue());
        r.setNbPlaces(Integer.parseInt(tfNbPlaces.getText()));
        sr.add(r);
        lbMessage.setText("Réservation ajoutée !");
        viderFormulaire();
        chargerTableau();
    }

    @FXML
    public void supprimerReservation(ActionEvent event) {
        Reservation selected = tableReservation.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lbMessage.setText("Sélectionnez une réservation !");
            return;
        }
        sr.delete(selected);
        lbMessage.setText("Réservation supprimée !");
        chargerTableau();
    }

    @FXML
    public void modifierReservation(ActionEvent event) {
        Reservation selected = tableReservation.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lbMessage.setText("Sélectionnez une réservation à modifier !");
            return;
        }
        selected.setTitre(tfTitre.getText());
        selected.setDescription(tfDescription.getText());
        selected.setDateDebut(dpDateDebut.getValue());
        selected.setDateFin(dpDateFin.getValue());
        selected.setNbPlaces(Integer.parseInt(tfNbPlaces.getText()));
        sr.update(selected);
        lbMessage.setText("Réservation modifiée !");
        viderFormulaire();
        chargerTableau();
    }

    // Remplir le formulaire en cliquant sur une ligne du tableau
    @FXML
    public void selectionnerLigne(javafx.scene.input.MouseEvent event) {
        Reservation selected = tableReservation.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfTitre.setText(selected.getTitre());
            tfDescription.setText(selected.getDescription());
            dpDateDebut.setValue(selected.getDateDebut());
            dpDateFin.setValue(selected.getDateFin());
            tfNbPlaces.setText(String.valueOf(selected.getNbPlaces()));
        }
    }

    // Navigation vers la scène Acaton (comme le prof)
    @FXML
    public void allerAcaton(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionHackathon.fxml"));
        try {
            Parent root = loader.load();
            tfTitre.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    @FXML
    public void allerDashboard(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
        try {
            Parent root = loader.load();
            tfTitre.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    private void viderFormulaire() {
        tfTitre.clear();
        tfDescription.clear();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        tfNbPlaces.clear();
    }
}
