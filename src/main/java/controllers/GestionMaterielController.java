package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Materiel;
import services.ServiceMateriel;

public class GestionMaterielController {

    @FXML private TextField tfNom;
    @FXML private TextField tfCode;
    @FXML private TextField tfDescription;
    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbEtat;
    @FXML private TableView<Materiel> tvMateriels;
    @FXML private TableColumn<Materiel, Integer> colId;
    @FXML private TableColumn<Materiel, String> colNom;
    @FXML private TableColumn<Materiel, String> colCode;
    @FXML private TableColumn<Materiel, Integer> colQuantite;
    @FXML private TableColumn<Materiel, String> colEtat;

    private ServiceMateriel sm = new ServiceMateriel();
    private Materiel materielSelectionne = null;

    @FXML
    void initialize() {
        cbEtat.setItems(FXCollections.observableArrayList(
                "disponible", "indisponible", "maintenance"
        ));
        cbEtat.setValue("disponible");
        tvMateriels.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        materielSelectionne = newVal;
                        tfNom.setText(newVal.getNom());
                        tfCode.setText(newVal.getCode());
                        tfDescription.setText(newVal.getDescription());
                        tfQuantite.setText(String.valueOf(newVal.getQuantite()));
                        cbEtat.setValue(newVal.getEtat());
                        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
                        colNom.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom"));
                        colCode.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("code"));
                        colQuantite.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantite"));
                        colEtat.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("etat"));
                    }
                });
        afficherMateriels(null);
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

    private Materiel getFromForm() {
        Materiel m = new Materiel();
        m.setNom(tfNom.getText());
        m.setCode(tfCode.getText());
        m.setDescription(tfDescription.getText());
        try { m.setQuantite(Integer.parseInt(tfQuantite.getText())); }
        catch (Exception ex) { m.setQuantite(1); }
        m.setEtat(cbEtat.getValue());
        return m;
    }

    private void vider() {
        tfNom.clear(); tfCode.clear();
        tfDescription.clear(); tfQuantite.clear();
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