package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Hackathon;
import services.ServiceHackathon;

import java.io.IOException;

public class GestionHackathonController {

    // ----- champs du formulaire -----
    @FXML private TextField tfNom;
    @FXML private TextField tfCategorie;
    @FXML private TextField tfDuree;
    @FXML private TextField tfPrix;
    @FXML private TextField tfReservationId;

    // ----- TableView -----
    @FXML private TableView<Hackathon> tableAcaton;
    @FXML private TableColumn<Hackathon, Integer> colId;
    @FXML private TableColumn<Hackathon, String>  colNom;
    @FXML private TableColumn<Hackathon, String>  colCategorie;
    @FXML private TableColumn<Hackathon, Integer> colDuree;
    @FXML private TableColumn<Hackathon, Double>  colPrix;
    @FXML private TableColumn<Hackathon, Integer> colReservationId;

    @FXML private Label lbMessage;

    private final ServiceHackathon sa = new ServiceHackathon();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colReservationId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        chargerTableau();
    }

    private void chargerTableau() {
        ObservableList<Hackathon> data =
                FXCollections.observableArrayList(sa.getAll());
        tableAcaton.setItems(data);
    }

    @FXML
    public void ajouterAcaton(ActionEvent event) {
        if (tfNom.getText().isEmpty()) {
            lbMessage.setText("Le champ Nom est obligatoire !");
            return;
        }
        Hackathon a = new Hackathon();
        a.setNom(tfNom.getText());
        a.setCategorie(tfCategorie.getText());
        a.setDuree(Integer.parseInt(tfDuree.getText()));
        a.setPrix(Double.parseDouble(tfPrix.getText()));
        a.setReservationId(Integer.parseInt(tfReservationId.getText()));
        sa.add(a);
        lbMessage.setText("Acaton ajouté !");
        viderFormulaire();
        chargerTableau();
    }

    @FXML
    public void supprimerAcaton(ActionEvent event) {
        Hackathon selected = tableAcaton.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lbMessage.setText("Sélectionnez un acaton !");
            return;
        }
        sa.delete(selected);
        lbMessage.setText("Acaton supprimé !");
        chargerTableau();
    }

    @FXML
    public void modifierAcaton(ActionEvent event) {
        Hackathon selected = tableAcaton.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lbMessage.setText("Sélectionnez un acaton à modifier !");
            return;
        }
        selected.setNom(tfNom.getText());
        selected.setCategorie(tfCategorie.getText());
        selected.setDuree(Integer.parseInt(tfDuree.getText()));
        selected.setPrix(Double.parseDouble(tfPrix.getText()));
        selected.setReservationId(Integer.parseInt(tfReservationId.getText()));
        sa.update(selected);
        lbMessage.setText("Acaton modifié !");
        viderFormulaire();
        chargerTableau();
    }

    @FXML
    public void selectionnerLigne(javafx.scene.input.MouseEvent event) {
        Hackathon selected = tableAcaton.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfNom.setText(selected.getNom());
            tfCategorie.setText(selected.getCategorie());
            tfDuree.setText(String.valueOf(selected.getDuree()));
            tfPrix.setText(String.valueOf(selected.getPrix()));
            tfReservationId.setText(String.valueOf(selected.getReservationId()));
        }
    }

    // Retour vers la scène Réservation
    @FXML
    public void allerReservation(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionReservation.fxml"));
        try {
            Parent root = loader.load();
            tfNom.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    @FXML
    public void allerDashboard(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
        try {
            Parent root = loader.load();
            tfNom.getScene().setRoot(root);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    private void viderFormulaire() {
        tfNom.clear();
        tfCategorie.clear();
        tfDuree.clear();
        tfPrix.clear();
        tfReservationId.clear();
    }
}
