package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Materiel;
import models.ReservationMateriel;
import services.ServiceMateriel;
import services.ServiceReservationMateriel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class GestionReservationMaterielController {

    @FXML private ComboBox<String> cbMateriel;
    @FXML private TextField tfMotif;
    @FXML private TextField tfDateDebut;
    @FXML private TextField tfDateFin;
    @FXML private ComboBox<String> cbStatut;

    @FXML private Label errMateriel;
    @FXML private Label errMotif;
    @FXML private Label errDateDebut;
    @FXML private Label errDateFin;
    @FXML private Label errStatut;

    @FXML private TableView<ReservationMateriel> tvReservations;
    @FXML private TableColumn<ReservationMateriel, String> colMateriel;
    @FXML private TableColumn<ReservationMateriel, String> colMotif;
    @FXML private TableColumn<ReservationMateriel, String> colDateDebut;
    @FXML private TableColumn<ReservationMateriel, String> colDateFin;
    @FXML private TableColumn<ReservationMateriel, String> colStatut;

    private ServiceReservationMateriel sr = new ServiceReservationMateriel();
    private ServiceMateriel sm = new ServiceMateriel();
    private ReservationMateriel reservationSelectionnee = null;
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @FXML
    void initialize() {
        chargerMateriels();

        cbStatut.setItems(FXCollections.observableArrayList(
                "en_attente", "confirmee", "annulee"
        ));
        cbStatut.setValue("en_attente");

        colMateriel.setCellValueFactory(new PropertyValueFactory<>("materielNom"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));

        cbMateriel.valueProperty().addListener((o, ov, nv) -> errMateriel.setText(""));
        tfMotif.textProperty().addListener((o, ov, nv) -> errMotif.setText(""));
        tfDateDebut.textProperty().addListener((o, ov, nv) -> errDateDebut.setText(""));
        tfDateFin.textProperty().addListener((o, ov, nv) -> errDateFin.setText(""));
        cbStatut.valueProperty().addListener((o, ov, nv) -> errStatut.setText(""));

        tvReservations.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        reservationSelectionnee = newVal;
                        for (String mat : cbMateriel.getItems()) {
                            if (mat.startsWith(newVal.getMaterielId() + " - ")) {
                                cbMateriel.setValue(mat);
                                break;
                            }
                        }
                        tfMotif.setText(newVal.getMotif());
                        tfDateDebut.setText(newVal.getDateDebut().format(FORMAT));
                        tfDateFin.setText(newVal.getDateFin().format(FORMAT));
                        cbStatut.setValue(newVal.getStatut());
                        viderErreurs();
                    }
                });

        afficherReservations(null);
    }

    private void chargerMateriels() {
        List<Materiel> materiels = sm.getAll();
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Materiel m : materiels) {
            items.add(m.getId() + " - " + m.getNom());
        }
        cbMateriel.setItems(items);
        if (!items.isEmpty()) cbMateriel.setValue(items.get(0));
    }

    private boolean valider() {
        boolean valide = true;
        viderErreurs();

        if (cbMateriel.getValue() == null) {
            errMateriel.setText("⚠ Veuillez selectionner un materiel");
            valide = false;
        }

        if (tfMotif.getText().trim().isEmpty()) {
            errMotif.setText("⚠ Le motif est obligatoire");
            tfMotif.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else if (tfMotif.getText().trim().length() < 3) {
            errMotif.setText("⚠ Le motif doit contenir au moins 3 caracteres");
            tfMotif.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else {
            tfMotif.setStyle("-fx-border-color: #34a853; -fx-border-radius: 6; -fx-background-radius: 6;");
        }

        LocalDateTime debut = null;
        if (tfDateDebut.getText().trim().isEmpty()) {
            errDateDebut.setText("⚠ La date de debut est obligatoire");
            tfDateDebut.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else {
            try {
                debut = LocalDateTime.parse(tfDateDebut.getText().trim(), FORMAT);
                tfDateDebut.setStyle("-fx-border-color: #34a853; -fx-border-radius: 6; -fx-background-radius: 6;");
            } catch (DateTimeParseException e) {
                errDateDebut.setText("⚠ Format invalide : yyyy-MM-ddTHH:mm");
                tfDateDebut.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
                valide = false;
            }
        }

        if (tfDateFin.getText().trim().isEmpty()) {
            errDateFin.setText("⚠ La date de fin est obligatoire");
            tfDateFin.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
            valide = false;
        } else {
            try {
                LocalDateTime fin = LocalDateTime.parse(tfDateFin.getText().trim(), FORMAT);
                if (debut != null && !fin.isAfter(debut)) {
                    errDateFin.setText("⚠ La date de fin doit etre apres la date de debut");
                    tfDateFin.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
                    valide = false;
                } else {
                    tfDateFin.setStyle("-fx-border-color: #34a853; -fx-border-radius: 6; -fx-background-radius: 6;");
                }
            } catch (DateTimeParseException e) {
                errDateFin.setText("⚠ Format invalide : yyyy-MM-ddTHH:mm");
                tfDateFin.setStyle("-fx-border-color: #d93025; -fx-border-radius: 6; -fx-background-radius: 6;");
                valide = false;
            }
        }

        if (cbStatut.getValue() == null) {
            errStatut.setText("⚠ Veuillez selectionner un statut");
            valide = false;
        }

        return valide;
    }

    private void viderErreurs() {
        errMateriel.setText(""); errMotif.setText("");
        errDateDebut.setText(""); errDateFin.setText("");
        errStatut.setText("");
        tfMotif.setStyle("-fx-border-radius: 6; -fx-background-radius: 6;");
        tfDateDebut.setStyle("-fx-border-radius: 6; -fx-background-radius: 6;");
        tfDateFin.setStyle("-fx-border-radius: 6; -fx-background-radius: 6;");
    }

    @FXML
    public void ajouterReservation(ActionEvent e) {
        if (!valider()) return;
        ReservationMateriel r = getFromForm();
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
        if (!valider()) return;
        ReservationMateriel r = getFromForm();
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Voulez-vous vraiment supprimer cette reservation ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sr.delete(reservationSelectionnee.getId());
                afficherReservations(null);
                vider();
                reservationSelectionnee = null;
            }
        });
    }

    @FXML
    public void afficherReservations(ActionEvent e) {
        ObservableList<ReservationMateriel> list =
                FXCollections.observableArrayList(sr.getAll());
        tvReservations.setItems(list);
    }

    private ReservationMateriel getFromForm() {
        try {
            ReservationMateriel r = new ReservationMateriel();
            String materielStr = cbMateriel.getValue();
            int materielId = Integer.parseInt(materielStr.split(" - ")[0]);
            r.setMaterielId(materielId);
            r.setMotif(tfMotif.getText().trim());
            r.setDateDebut(LocalDateTime.parse(tfDateDebut.getText().trim(), FORMAT));
            r.setDateFin(LocalDateTime.parse(tfDateFin.getText().trim(), FORMAT));
            r.setStatut(cbStatut.getValue());
            return r;
        } catch (Exception ex) {
            return null;
        }
    }

    private void vider() {
        chargerMateriels();
        tfMotif.clear();
        tfDateDebut.clear();
        tfDateFin.clear();
        cbStatut.setValue("en_attente");
        reservationSelectionnee = null;
        viderErreurs();
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Attention");
        a.setContentText(msg);
        a.showAndWait();
    }
}