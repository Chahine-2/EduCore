package controllers;

import models.Cours;
import models.Presence;
import models.Utilisateur;
import services.ServiceCours;
import services.ServicePresence;
import services.UtilisateurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class GestionPresenceController implements Initializable {

    @FXML private ComboBox<Cours> comboBoxCours;
    @FXML private DatePicker datePickerPresence;
    @FXML private TableView<PresenceRow> tablePresence;
    @FXML private TableColumn<PresenceRow, Integer> colEtudiantId;
    @FXML private TableColumn<PresenceRow, String> colNomEtudiant;
    @FXML private TableColumn<PresenceRow, String> colPrenomEtudiant;
    @FXML private TableColumn<PresenceRow, Boolean> colPresent;
    @FXML private TableColumn<PresenceRow, String> colNotes;
    @FXML private Label labelStatistiques;
    @FXML private Button btnSauvegarder;
    @FXML private Button btnCharger;

    private final ServiceCours serviceCours = new ServiceCours();
    private final ServicePresence servicePresence = new ServicePresence();
    private final UtilisateurService serviceUtilisateur = new UtilisateurService();
    private final ObservableList<PresenceRow> presenceData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chargerCours();
        initializeTableColumns();
        datePickerPresence.setValue(LocalDate.now());

        comboBoxCours.setOnAction(e -> chargerEtudiants());
        btnCharger.setOnAction(e -> chargerPresencesDuJour());
        btnSauvegarder.setOnAction(e -> sauvegarderPresences());
    }

    private void chargerCours() {
        try {
            List<Cours> coursList = serviceCours.getAll();
            ObservableList<Cours> coursObservable = FXCollections.observableArrayList(coursList);
            comboBoxCours.setItems(coursObservable);
        } catch (Exception e) {
            afficherErreur("Erreur lors du chargement des cours", e.getMessage());
        }
    }

    private void chargerEtudiants() {
        presenceData.clear();
        try {
            Cours coursSelectionne = comboBoxCours.getValue();
            if (coursSelectionne == null) return;

            // Get all students using the available service method
            List<Utilisateur> etudiants = serviceUtilisateur.listerUtilisateurs();

            for (Utilisateur etudiant : etudiants) {
                // Filter by role "Etudiant" if applicable
                if (etudiant.getRole() != null && "Etudiant".equalsIgnoreCase(etudiant.getRole().getNomRole())) {
                    PresenceRow row = new PresenceRow(
                        etudiant.getId(),
                        etudiant.getNom(),
                        etudiant.getPrenom(),
                        false,
                        ""
                    );
                    presenceData.add(row);
                }
            }
            tablePresence.setItems(presenceData);
        } catch (Exception e) {
            afficherErreur("Erreur lors du chargement des étudiants", e.getMessage());
        }
    }

    private void chargerPresencesDuJour() {
        try {
            Cours coursSelectionne = comboBoxCours.getValue();
            LocalDate dateSelectionnee = datePickerPresence.getValue();

            if (coursSelectionne == null || dateSelectionnee == null) {
                afficherInfo("Veuillez sélectionner un cours et une date");
                return;
            }

            List<Presence> presences = servicePresence.getPresenceByCoursIdAndDate(coursSelectionne.getId(), dateSelectionnee);

            // Create a map for quick lookup
            Map<Integer, Presence> presenceMap = new HashMap<>();
            for (Presence p : presences) {
                presenceMap.put(p.getEtudiantId(), p);
            }

            // Update table data
            for (PresenceRow row : presenceData) {
                Presence p = presenceMap.get(row.getEtudiantId());
                if (p != null) {
                    row.setPresent(p.isEstPresent());
                    row.setNotes(p.getNotes() != null ? p.getNotes() : "");
                }
            }

            mettreAJourStatistiques();
        } catch (Exception e) {
            afficherErreur("Erreur lors du chargement des présences", e.getMessage());
        }
    }

    private void sauvegarderPresences() {
        try {
            Cours coursSelectionne = comboBoxCours.getValue();
            LocalDate dateSelectionnee = datePickerPresence.getValue();

            if (coursSelectionne == null || dateSelectionnee == null) {
                afficherInfo("Veuillez sélectionner un cours et une date");
                return;
            }

            for (PresenceRow row : presenceData) {
                Presence presence = new Presence(
                    row.getEtudiantId(),
                    coursSelectionne.getId(),
                    dateSelectionnee,
                    row.isPresent(),
                    row.getNotes()
                );

                try {
                    servicePresence.add(presence);
                } catch (SQLException e) {
                    // If record exists, update it instead
                    if (e.getMessage().contains("UNIQUE")) {
                        servicePresence.update(presence);
                    }
                }
            }
            afficherSucces("✅ Présences sauvegardées avec succès!");
            mettreAJourStatistiques();
        } catch (Exception e) {
            afficherErreur("Erreur lors de la sauvegarde", e.getMessage());
        }
    }

    private void mettreAJourStatistiques() {
        try {
            Cours coursSelectionne = comboBoxCours.getValue();
            if (coursSelectionne == null) return;

            Map<String, Object> stats = servicePresence.getPresenceStatsForCours(coursSelectionne.getId());

            int total = (Integer) stats.getOrDefault("total", 0);
            int presentes = (Integer) stats.getOrDefault("presentes", 0);
            int absentes = (Integer) stats.getOrDefault("absentes", 0);
            double taux = (Double) stats.getOrDefault("tauxPresence", 0.0);

            String statsText = String.format("Total: %d | Présentes: %d | Absentes: %d | Taux: %.1f%%",
                    total, presentes, absentes, taux);
            labelStatistiques.setText(statsText);
        } catch (Exception e) {
            System.err.println("Erreur statistiques: " + e.getMessage());
        }
    }

    private void initializeTableColumns() {
        colEtudiantId.setCellValueFactory(new PropertyValueFactory<>("etudiantId"));
        colNomEtudiant.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenomEtudiant.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        colPresent.setCellValueFactory(new PropertyValueFactory<>("present"));
        colPresent.setCellFactory(CheckBoxTableCell.forTableColumn(colPresent));

        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        colNotes.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        colNotes.setOnEditCommit(e -> e.getRowValue().setNotes(e.getNewValue()));
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class to represent a row in the presence table
    public static class PresenceRow {
        private int etudiantId;
        private String nom;
        private String prenom;
        private boolean present;
        private String notes;

        public PresenceRow(int etudiantId, String nom, String prenom, boolean present, String notes) {
            this.etudiantId = etudiantId;
            this.nom = nom;
            this.prenom = prenom;
            this.present = present;
            this.notes = notes;
        }

        public int getEtudiantId() { return etudiantId; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public boolean isPresent() { return present; }
        public void setPresent(boolean present) { this.present = present; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}



