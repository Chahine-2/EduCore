package controllers;

import interfaces.IService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import models.Evaluation;
import models.EvaluationStatut;
import models.EvaluationType;
import services.EvaluationDAOImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class EvaluationController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField dureeField;
    @FXML private TextField noteMaxField;
    @FXML private TextField notePassageField;
    @FXML private TextField nbTentativesField;
    @FXML private TextField dateDebutField;
    @FXML private CheckBox ordreAleatoireCheck;
    @FXML private CheckBox afficherCorrecCheck;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextField searchField;
    @FXML private TableView<Evaluation> evaluationTable;
    @FXML private Label statusLabel;

    private IService<Evaluation> evaluationDAO;
    private Evaluation selectedEvaluation;

    @FXML
    public void initialize() {
        evaluationDAO = new EvaluationDAOImpl();

        // Populate ComboBoxes
        typeCombo.setItems(FXCollections.observableArrayList("qcm", "examen", "devoir", "projet", "tp"));
        statutCombo.setItems(FXCollections.observableArrayList("brouillon", "publie", "ferme"));

        setupTableColumns();

        // Try to load evaluations, handle connection errors gracefully
        try {
            loadAllEvaluations();
        } catch (NullPointerException e) {
            statusLabel.setText("✗ Erreur de connexion à la base de données!");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            System.err.println("Database connection failed. Check console for details.");
        }
    }

    private void setupTableColumns() {
        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, Integer> colId = (TableColumn<Evaluation, Integer>) evaluationTable.getColumns().get(0);
        colId.setCellValueFactory(cv -> new javafx.beans.property.SimpleObjectProperty<>(cv.getValue().getId()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colTitre = (TableColumn<Evaluation, String>) evaluationTable.getColumns().get(1);
        colTitre.setCellValueFactory(cv -> new javafx.beans.property.SimpleStringProperty(cv.getValue().getTitre()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colDesc = (TableColumn<Evaluation, String>) evaluationTable.getColumns().get(2);
        colDesc.setCellValueFactory(cv -> new javafx.beans.property.SimpleStringProperty(cv.getValue().getDescription()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colType = (TableColumn<Evaluation, String>) evaluationTable.getColumns().get(3);
        colType.setCellValueFactory(cv -> new javafx.beans.property.SimpleStringProperty(cv.getValue().getType().getDbValue()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, Integer> colDuree = (TableColumn<Evaluation, Integer>) evaluationTable.getColumns().get(4);
        colDuree.setCellValueFactory(cv -> new javafx.beans.property.SimpleObjectProperty<>(cv.getValue().getDureeMinutes()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, Float> colNoteMax = (TableColumn<Evaluation, Float>) evaluationTable.getColumns().get(5);
        colNoteMax.setCellValueFactory(cv -> new javafx.beans.property.SimpleObjectProperty<>(cv.getValue().getNoteMax()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, Float> colNotePass = (TableColumn<Evaluation, Float>) evaluationTable.getColumns().get(6);
        colNotePass.setCellValueFactory(cv -> new javafx.beans.property.SimpleObjectProperty<>(cv.getValue().getNotePassage()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, Integer> colNbTent = (TableColumn<Evaluation, Integer>) evaluationTable.getColumns().get(7);
        colNbTent.setCellValueFactory(cv -> new javafx.beans.property.SimpleObjectProperty<>(cv.getValue().getNbTentatives()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colStatut = (TableColumn<Evaluation, String>) evaluationTable.getColumns().get(8);
        colStatut.setCellValueFactory(cv -> new javafx.beans.property.SimpleStringProperty(cv.getValue().getStatut().getDbValue()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colDateDebut = (TableColumn<Evaluation, String>) evaluationTable.getColumns().get(9);
        colDateDebut.setCellValueFactory(cv -> new SimpleStringProperty(formatDateTime(cv.getValue().getDateDebut())));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colDateCreation = (TableColumn<Evaluation, String>) evaluationTable.getColumns().get(10);
        colDateCreation.setCellValueFactory(cv -> new SimpleStringProperty(formatDateTime(cv.getValue().getDateCreation())));
    }

    @FXML
    private void handleAdd() {
        try {
            if (!validateForm()) return;

            String titre = titreField.getText();
            String description = descriptionField.getText();
            EvaluationType type = EvaluationType.fromDbValue(typeCombo.getValue());
            int duree = Integer.parseInt(dureeField.getText());
            float noteMax = Float.parseFloat(noteMaxField.getText());
            float notePassage = Float.parseFloat(notePassageField.getText());
            int nbTentatives = Integer.parseInt(nbTentativesField.getText());
            boolean ordreAleatoire = ordreAleatoireCheck.isSelected();
            boolean afficherCorrec = afficherCorrecCheck.isSelected();
            LocalDateTime dateDebut = LocalDateTime.parse(dateDebutField.getText(), DATE_TIME_FORMATTER);
            LocalDateTime dateFin = dateDebut.plusMinutes(duree);
            EvaluationStatut statut = EvaluationStatut.fromDbValue(statutCombo.getValue());

            if (notePassage > noteMax) {
                showError("Note passage ne peut pas être > Note max");
                return;
            }

            Evaluation evaluation = new Evaluation(titre, description, type, duree, noteMax, notePassage,
                    nbTentatives, ordreAleatoire, afficherCorrec, dateDebut, dateFin, statut);
            evaluationDAO.add(evaluation);

            statusLabel.setText("✓ Évaluation ajoutée avec succès");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            clearForm();
            loadAllEvaluations();
        } catch (NullPointerException e) {
            showError("Erreur: Base de données non disponible");
        } catch (Exception e) {
            showError("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            if (selectedEvaluation == null) {
                showError("Sélectionnez une évaluation à modifier");
                return;
            }
            if (!validateForm()) return;

            String titre = titreField.getText();
            String description = descriptionField.getText();
            EvaluationType type = EvaluationType.fromDbValue(typeCombo.getValue());
            int duree = Integer.parseInt(dureeField.getText());
            float noteMax = Float.parseFloat(noteMaxField.getText());
            float notePassage = Float.parseFloat(notePassageField.getText());
            int nbTentatives = Integer.parseInt(nbTentativesField.getText());
            boolean ordreAleatoire = ordreAleatoireCheck.isSelected();
            boolean afficherCorrec = afficherCorrecCheck.isSelected();
            LocalDateTime dateDebut = LocalDateTime.parse(dateDebutField.getText(), DATE_TIME_FORMATTER);
            LocalDateTime dateFin = dateDebut.plusMinutes(duree);
            EvaluationStatut statut = EvaluationStatut.fromDbValue(statutCombo.getValue());

            if (notePassage > noteMax) {
                showError("Note passage ne peut pas être > Note max");
                return;
            }

            Evaluation updated = new Evaluation(selectedEvaluation.getId(), titre, description, type, duree, noteMax,
                    notePassage, nbTentatives, ordreAleatoire, afficherCorrec, dateDebut, dateFin, statut, null);
            evaluationDAO.update(updated);

            statusLabel.setText("✓ Évaluation mise à jour avec succès");
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            clearForm();
            loadAllEvaluations();
        } catch (Exception e) {
            showError("Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (selectedEvaluation == null) {
                showError("Sélectionnez une évaluation à supprimer");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer l'évaluation?");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer: " + selectedEvaluation.getTitre());

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                evaluationDAO.delete(selectedEvaluation.getId());
                statusLabel.setText("✓ Évaluation supprimée");
                statusLabel.setStyle("-fx-text-fill: #27ae60;");
                clearForm();
                loadAllEvaluations();
            }
        } catch (Exception e) {
            showError("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoadDetails() {
        Evaluation selected = evaluationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionnez une évaluation");
            return;
        }

        selectedEvaluation = selected;
        titreField.setText(selected.getTitre());
        descriptionField.setText(selected.getDescription());
        typeCombo.setValue(selected.getType().getDbValue());
        dureeField.setText(String.valueOf(selected.getDureeMinutes()));
        noteMaxField.setText(String.valueOf(selected.getNoteMax()));
        notePassageField.setText(String.valueOf(selected.getNotePassage()));
        nbTentativesField.setText(String.valueOf(selected.getNbTentatives()));
        ordreAleatoireCheck.setSelected(selected.isOrdreAleatoire());
        afficherCorrecCheck.setSelected(selected.isAfficherCorrec());
        dateDebutField.setText(selected.getDateDebut().format(DATE_TIME_FORMATTER));
        afficherCorrecCheck.setSelected(selected.isAfficherCorrec());
        statutCombo.setValue(selected.getStatut().getDbValue());
    }

    @FXML
    private void handleShowAll() {
        searchField.clear();
        loadAllEvaluations();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllEvaluations();
            return;
        }

        List<Evaluation> allEvaluations = evaluationDAO.getAll();
        List<Evaluation> filtered = allEvaluations.stream()
                .filter(e -> e.getTitre().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        ObservableList<Evaluation> data = FXCollections.observableArrayList(filtered);
        evaluationTable.setItems(data);
        statusLabel.setText("Trouvé: " + filtered.size() + " résultat(s)");
    }

    @FXML
    private void handleClear() {
        clearForm();
        selectedEvaluation = null;
    }

    private void loadAllEvaluations() {
        try {
            List<Evaluation> evaluations = evaluationDAO.getAll();
            ObservableList<Evaluation> data = FXCollections.observableArrayList(evaluations);
            evaluationTable.setItems(data);
            statusLabel.setText("Total: " + evaluations.size() + " évaluation(s)");
        } catch (NullPointerException e) {
            statusLabel.setText("✗ Base de données non disponible");
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            evaluationTable.setItems(FXCollections.observableArrayList());
        }
    }

    private boolean validateForm() {
        if (titreField.getText().trim().isEmpty()) {
            showError("Le titre est requis");
            return false;
        }
        if (typeCombo.getValue() == null) {
            showError("Sélectionnez un type");
            return false;
        }
        if (statutCombo.getValue() == null) {
            showError("Sélectionnez un statut");
            return false;
        }
        try {
            Integer.parseInt(dureeField.getText());
            Float.parseFloat(noteMaxField.getText());
            Float.parseFloat(notePassageField.getText());
            Integer.parseInt(nbTentativesField.getText());
            LocalDateTime.parse(dateDebutField.getText(), DATE_TIME_FORMATTER);
        } catch (NumberFormatException | DateTimeParseException e) {
            showError("Vérifiez les valeurs numériques et la date");
            return false;
        }
        return true;
    }

    private void clearForm() {
        titreField.clear();
        descriptionField.clear();
        typeCombo.setValue(null);
        dureeField.clear();
        noteMaxField.clear();
        notePassageField.clear();
        nbTentativesField.clear();
        dateDebutField.clear();
        ordreAleatoireCheck.setSelected(false);
        afficherCorrecCheck.setSelected(false);
        statutCombo.setValue(null);
        selectedEvaluation = null;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        statusLabel.setText("✗ " + message);
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }
}




