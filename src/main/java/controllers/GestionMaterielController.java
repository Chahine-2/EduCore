package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Materiel;
import services.ServiceMateriel;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class GestionMaterielController {

    @FXML private TextField tfNom;
    @FXML private TextField tfCode;
    @FXML private TextField tfDescription;
    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbEtat;
    @FXML private ComboBox<String> cbDepartement;
    @FXML private ComboBox<String> cbSalle;
    @FXML private TableView<Materiel> tvMateriels;
    @FXML private TableColumn<Materiel, String> colNom;
    @FXML private TableColumn<Materiel, String> colCode;
    @FXML private TableColumn<Materiel, Integer> colQuantite;
    @FXML private TableColumn<Materiel, String> colEtat;
    @FXML private TableColumn<Materiel, String> colSalle;
    @FXML private TableColumn<Materiel, String> colDepartement;

    private ServiceMateriel sm = new ServiceMateriel();
    private Materiel materielSelectionne = null;

    @FXML
    void initialize() {
        // Etat
        cbEtat.setItems(FXCollections.observableArrayList(
                "disponible", "indisponible", "maintenance"
        ));
        cbEtat.setValue("disponible");

        // Départements
        List<String> departements = sm.getDepartements();
        cbDepartement.setItems(FXCollections.observableArrayList(departements));
        if (!departements.isEmpty()) {
            cbDepartement.setValue(departements.get(0));
            chargerSalles(sm.getIdFromString(departements.get(0)));
        }

        // Quand département change → recharger salles
        cbDepartement.setOnAction(e -> {
            if (cbDepartement.getValue() != null) {
                chargerSalles(sm.getIdFromString(cbDepartement.getValue()));
            }
        });

        // Colonnes tab
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salleNom"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departementNom"));

        // Sélection tab
        tvMateriels.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        materielSelectionne = newVal;
                        tfNom.setText(newVal.getNom());
                        tfCode.setText(newVal.getCode());
                        tfDescription.setText(newVal.getDescription());
                        tfQuantite.setText(String.valueOf(newVal.getQuantite()));
                        cbEtat.setValue(newVal.getEtat());
                        // Sélectionner le bon département
                        for (String d : cbDepartement.getItems()) {
                            if (newVal.getDepartementNom() != null &&
                                    d.contains(newVal.getDepartementNom())) {
                                cbDepartement.setValue(d);
                                chargerSalles(sm.getIdFromString(d));
                                break;
                            }
                        }
                        // Sélectionner la bonne salle
                        for (String s : cbSalle.getItems()) {
                            if (s.startsWith(newVal.getSalleId() + " - ")) {
                                cbSalle.setValue(s);
                                break;
                            }
                        }
                    }
                });

        afficherMateriels(null);
    }

    private void chargerSalles(int departementId) {
        List<String> salles = sm.getSallesParDepartement(departementId);
        cbSalle.setItems(FXCollections.observableArrayList(salles));
        if (!salles.isEmpty()) cbSalle.setValue(salles.get(0));
    }

    @FXML
    public void ajouterMateriel(ActionEvent e) {
        Materiel m = getFromForm();
        sm.add(m);
        afficherMateriels(null);
        vider();
    }

    @FXML
    public void modifierMateriel(ActionEvent e) {
        if (materielSelectionne == null) {
            showAlert("Selectionnez un materiel !");
            return;
        }
        Materiel m = getFromForm();
        m.setId(materielSelectionne.getId());
        sm.update(m);
        afficherMateriels(null);
        vider();
    }

    @FXML
    public void supprimerMateriel(ActionEvent e) {
        if (materielSelectionne == null) {
            showAlert("Selectionnez un materiel !");
            return;
        }
        sm.delete(materielSelectionne);
        afficherMateriels(null);
        vider();
        materielSelectionne = null;
    }

    @FXML
    public void afficherMateriels(ActionEvent e) {
        ObservableList<Materiel> list =
                FXCollections.observableArrayList(sm.getAll());
        tvMateriels.setItems(list);
    }

    @FXML
    public void voirSurCarte(ActionEvent e) {
        if (materielSelectionne == null) {
            showAlert("Selectionnez un materiel !");
            return;
        }
        try {
            URL url = getClass().getClassLoader().getResource("Map.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            MapController mapController = loader.getController();
            mapController.afficherMateriel(materielSelectionne);
            Stage stage = new Stage();
            stage.setTitle("Localisation : " + materielSelectionne.getNom());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            System.out.println("Erreur : " + ex.getMessage());
        }
    }

    private Materiel getFromForm() {
        Materiel m = new Materiel();
        m.setNom(tfNom.getText());
        m.setCode(tfCode.getText());
        m.setDescription(tfDescription.getText());
        try { m.setQuantite(Integer.parseInt(tfQuantite.getText())); }
        catch (Exception ex) { m.setQuantite(1); }
        m.setEtat(cbEtat.getValue());
        if (cbSalle.getValue() != null) {
            m.setSalleId(sm.getIdFromString(cbSalle.getValue()));
        }
        return m;
    }

    private void vider() {
        tfNom.clear();
        tfCode.clear();
        tfDescription.clear();
        tfQuantite.clear();
        cbEtat.setValue("disponible");
        materielSelectionne = null;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention");
        a.setContentText(msg);
        a.showAndWait();
    }
}