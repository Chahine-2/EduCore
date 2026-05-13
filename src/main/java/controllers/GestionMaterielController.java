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

    @FXML private Label errNom;
    @FXML private Label errCode;
    @FXML private Label errDescription;
    @FXML private Label errQuantite;
    @FXML private Label errEtat;
    @FXML private Label errDepartement;
    @FXML private Label errSalle;

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
        cbEtat.setItems(FXCollections.observableArrayList(
                "disponible", "indisponible", "maintenance"
        ));
        cbEtat.setValue("disponible");

        List<String> departements = sm.getDepartements();
        cbDepartement.setItems(FXCollections.observableArrayList(departements));
        if (!departements.isEmpty()) {
            cbDepartement.setValue(departements.get(0));
            chargerSalles(sm.getIdFromString(departements.get(0)));
        }

        cbDepartement.setOnAction(e -> {
            if (cbDepartement.getValue() != null) {
                chargerSalles(sm.getIdFromString(cbDepartement.getValue()));
            }
        });

        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        colSalle.setCellValueFactory(new PropertyValueFactory<>("salleNom"));
        colDepartement.setCellValueFactory(new PropertyValueFactory<>("departementNom"));

        // Effacer erreur quand l'utilisateur tape
        tfNom.textProperty().addListener((o, ov, nv) -> errNom.setText(""));
        tfCode.textProperty().addListener((o, ov, nv) -> errCode.setText(""));
        tfDescription.textProperty().addListener((o, ov, nv) -> errDescription.setText(""));
        tfQuantite.textProperty().addListener((o, ov, nv) -> errQuantite.setText(""));
        cbEtat.valueProperty().addListener((o, ov, nv) -> errEtat.setText(""));
        cbDepartement.valueProperty().addListener((o, ov, nv) -> errDepartement.setText(""));
        cbSalle.valueProperty().addListener((o, ov, nv) -> errSalle.setText(""));

        tvMateriels.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        materielSelectionne = newVal;
                        tfNom.setText(newVal.getNom());
                        tfCode.setText(newVal.getCode());
                        tfDescription.setText(newVal.getDescription());
                        tfQuantite.setText(String.valueOf(newVal.getQuantite()));
                        cbEtat.setValue(newVal.getEtat());
                        for (String d : cbDepartement.getItems()) {
                            if (newVal.getDepartementNom() != null &&
                                    d.contains(newVal.getDepartementNom())) {
                                cbDepartement.setValue(d);
                                chargerSalles(sm.getIdFromString(d));
                                break;
                            }
                        }
                        for (String s : cbSalle.getItems()) {
                            if (s.startsWith(newVal.getSalleId() + " - ")) {
                                cbSalle.setValue(s);
                                break;
                            }
                        }
                        viderErreurs();
                    }
                });

        afficherMateriels(null);
    }

    private void chargerSalles(int departementId) {
        List<String> salles = sm.getSallesParDepartement(departementId);
        cbSalle.setItems(FXCollections.observableArrayList(salles));
        if (!salles.isEmpty()) cbSalle.setValue(salles.get(0));
    }

    private boolean valider() {
        boolean valide = true;
        viderErreurs();

        // Nom
        if (tfNom.getText().trim().isEmpty()) {
            errNom.setText("⚠ Le nom est obligatoire");
            tfNom.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else if (tfNom.getText().trim().length() < 2) {
            errNom.setText("⚠ Le nom doit contenir au moins 2 caracteres");
            tfNom.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else {
            tfNom.setStyle("-fx-border-color: #34a853; -fx-border-radius: 6; -fx-background-radius: 6;");
        }

        // Code
        if (tfCode.getText().trim().isEmpty()) {
            errCode.setText("⚠ Le code est obligatoire");
            tfCode.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else if (tfCode.getText().trim().length() < 3) {
            errCode.setText("⚠ Le code doit contenir au moins 3 caracteres");
            tfCode.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else {
            tfCode.setStyle("-fx-border-color: #34a853; -fx-border-radius: 6; -fx-background-radius: 6;");
        }

        // Description
        if (tfDescription.getText().trim().isEmpty()) {
            errDescription.setText("⚠ La description est obligatoire");
            tfDescription.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else {
            tfDescription.setStyle("-fx-border-color: #34a853; -fx-border-radius: 6; -fx-background-radius: 6;");
        }

        // Quantite
        if (tfQuantite.getText().trim().isEmpty()) {
            errQuantite.setText("⚠ La quantite est obligatoire");
            tfQuantite.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else {
            try {
                int q = Integer.parseInt(tfQuantite.getText().trim());
                if (q <= 0) {
                    errQuantite.setText("⚠ La quantite doit etre superieure a 0");
                    tfQuantite.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
                    valide = false;
                } else {
                    tfQuantite.setStyle("-fx-border-color: #34a853; -fx-border-radius: 6; -fx-background-radius: 6;");
                }
            } catch (NumberFormatException e) {
                errQuantite.setText("⚠ La quantite doit etre un nombre entier");
                tfQuantite.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
                valide = false;
            }
        }

        // Etat
        if (cbEtat.getValue() == null) {
            errEtat.setText("⚠ Veuillez selectionner un etat");
            valide = false;
        }

        // Departement
        if (cbDepartement.getValue() == null) {
            errDepartement.setText("⚠ Veuillez selectionner un departement");
            valide = false;
        }

        // Salle
        if (cbSalle.getValue() == null) {
            errSalle.setText("⚠ Veuillez selectionner une salle");
            valide = false;
        }

        return valide;
    }

    private void viderErreurs() {
        errNom.setText(""); errCode.setText("");
        errDescription.setText(""); errQuantite.setText("");
        errEtat.setText(""); errDepartement.setText("");
        errSalle.setText("");
        tfNom.setStyle("-fx-border-radius: 6; -fx-background-radius: 6;");
        tfCode.setStyle("-fx-border-radius: 6; -fx-background-radius: 6;");
        tfDescription.setStyle("-fx-border-radius: 6; -fx-background-radius: 6;");
        tfQuantite.setStyle("-fx-border-radius: 6; -fx-background-radius: 6;");
    }

    @FXML
    public void ajouterMateriel(ActionEvent e) {
        if (!valider()) return;
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
        if (!valider()) return;
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Voulez-vous vraiment supprimer ce materiel ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sm.delete(materielSelectionne);
                afficherMateriels(null);
                vider();
                materielSelectionne = null;
            }
        });
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
        m.setNom(tfNom.getText().trim());
        m.setCode(tfCode.getText().trim());
        m.setDescription(tfDescription.getText().trim());
        m.setQuantite(Integer.parseInt(tfQuantite.getText().trim()));
        m.setEtat(cbEtat.getValue());
        if (cbSalle.getValue() != null) {
            m.setSalleId(sm.getIdFromString(cbSalle.getValue()));
        }
        return m;
    }

    private void vider() {
        tfNom.clear(); tfCode.clear();
        tfDescription.clear(); tfQuantite.clear();
        cbEtat.setValue("disponible");
        materielSelectionne = null;
        viderErreurs();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention");
        a.setContentText(msg);
        a.showAndWait();
    }
}