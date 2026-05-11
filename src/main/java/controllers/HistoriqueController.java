package controllers;

import interfaces.IUtilisateurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.HistoriqueConnexion;
import services.UtilisateurService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HistoriqueController implements Initializable {

    @FXML private TableView<HistoriqueConnexion> tableHistorique;
    @FXML private TableColumn<HistoriqueConnexion, String> colDate;
    @FXML private TableColumn<HistoriqueConnexion, String> colEmail;
    @FXML private TableColumn<HistoriqueConnexion, String> colStatut;

    private IUtilisateurService service = new UtilisateurService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Associer les colonnes aux attributs de l'objet HistoriqueConnexion
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        chargerHistorique();
    }

    private void chargerHistorique() {
        List<HistoriqueConnexion> historiqueData = service.recupererHistoriqueConnexions();
        ObservableList<HistoriqueConnexion> data = FXCollections.observableArrayList(historiqueData);
        tableHistorique.setItems(data);
    }
}