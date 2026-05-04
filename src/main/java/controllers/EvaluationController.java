package controllers;

import interfaces.IService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import models.Evaluation;
import services.EvaluationDAOImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Evaluation Management — LMS-style dashboard (table, search, sort, pagination).
 */
public class EvaluationController {

    private static final DateTimeFormatter DATE_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm")
            .withLocale(Locale.ENGLISH);

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField dureeField;
    @FXML private TextField noteMaxField;
    @FXML private TextField notePassageField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private Spinner<Integer> debutHourSpinner;
    @FXML private Spinner<Integer> debutMinuteSpinner;
    @FXML private Spinner<Integer> debutSecondSpinner;
    @FXML private Spinner<Integer> finHourSpinner;
    @FXML private Spinner<Integer> finMinuteSpinner;
    @FXML private Spinner<Integer> finSecondSpinner;
    @FXML private Label dateCreationLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<Integer> pageSizeCombo;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Label pageInfoLabel;
    @FXML private TableView<Evaluation> evaluationTable;
    @FXML private Label statusLabel;
    @FXML private Label totalLabel;
    @FXML private Label emptyLabel;

    private IService<Evaluation> evaluationDAO;
    private Evaluation selectedEvaluation;
    /** Full list from persistence (unfiltered). */
    private final ObservableList<Evaluation> masterEvaluations = FXCollections.observableArrayList();
    private List<Evaluation> viewPipeline = new ArrayList<>();
    private int currentPageIndex;

    @FXML
    public void initialize() {
        evaluationDAO = new EvaluationDAOImpl();

        initAvailabilityControls();

        sortCombo.setItems(FXCollections.observableArrayList(
                "Start date · newest first",
                "Start date · oldest first",
                "End date · latest first",
                "End date · earliest first",
                "Created · newest first",
                "Created · oldest first",
                "Title · A to Z",
                "Title · Z to A",
                "Status · Upcoming → Active → Closed"
        ));
        sortCombo.getSelectionModel().selectFirst();

        pageSizeCombo.setItems(FXCollections.observableArrayList(8, 12, 25, 50));
        pageSizeCombo.getSelectionModel().select(Integer.valueOf(12));

        setupTableColumns();
        wireSelection();
        wireToolbarListeners();

        sortCombo.valueProperty().addListener((o, a, b) -> {
            currentPageIndex = 0;
            applyViewPipeline();
        });
        pageSizeCombo.valueProperty().addListener((o, a, b) -> {
            currentPageIndex = 0;
            applyViewPipeline();
        });

        loadAllEvaluations();
    }

    private void initAvailabilityControls() {
        dateDebutPicker.setPromptText("Select date");
        dateFinPicker.setPromptText("Select date");
        wireTimeSpinner(debutHourSpinner, 0, 23, 8);
        wireTimeSpinner(debutMinuteSpinner, 0, 59, 0);
        wireTimeSpinner(debutSecondSpinner, 0, 59, 0);
        wireTimeSpinner(finHourSpinner, 0, 23, 17);
        wireTimeSpinner(finMinuteSpinner, 0, 59, 0);
        wireTimeSpinner(finSecondSpinner, 0, 59, 0);
    }

    private static void wireTimeSpinner(Spinner<Integer> spinner, int min, int max, int initial) {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
    }

    private static void applyDateTimeToPickers(LocalDateTime ldt, DatePicker datePicker,
                                               Spinner<Integer> hour, Spinner<Integer> minute, Spinner<Integer> second) {
        if (ldt == null) {
            datePicker.setValue(null);
            hour.getValueFactory().setValue(0);
            minute.getValueFactory().setValue(0);
            second.getValueFactory().setValue(0);
            return;
        }
        datePicker.setValue(ldt.toLocalDate());
        hour.getValueFactory().setValue(ldt.getHour());
        minute.getValueFactory().setValue(ldt.getMinute());
        second.getValueFactory().setValue(ldt.getSecond());
    }

    private static int spinnerInt(Spinner<Integer> spinner) {
        try {
            Integer v = spinner.getValue();
            return v != null ? v : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /** Combines calendar date and time spinners; returns null if no date chosen. */
    private LocalDateTime combineDateAndTime(DatePicker datePicker, Spinner<Integer> hour,
                                            Spinner<Integer> minute, Spinner<Integer> second) {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            return null;
        }
        int h = Math.min(23, Math.max(0, spinnerInt(hour)));
        int m = Math.min(59, Math.max(0, spinnerInt(minute)));
        int s = Math.min(59, Math.max(0, spinnerInt(second)));
        return LocalDateTime.of(date, LocalTime.of(h, m, s));
    }

    private void wireToolbarListeners() {
        // reserved for future debounced search
    }

    private void wireSelection() {
        evaluationTable.getSelectionModel().selectedItemProperty().addListener((obs, prev, cur) -> {
            if (cur != null) {
                populateFormFromEvaluation(cur);
                selectedEvaluation = cur;
                setStatus("Loaded: " + cur.getTitre(), false);
            }
        });
    }

    private void setupTableColumns() {
        if (evaluationTable.getColumns().size() < 10) {
            return;
        }

        TableColumn<Evaluation, ?> c0 = evaluationTable.getColumns().get(0);
        TableColumn<Evaluation, ?> c1 = evaluationTable.getColumns().get(1);
        TableColumn<Evaluation, ?> c2 = evaluationTable.getColumns().get(2);
        TableColumn<Evaluation, ?> c3 = evaluationTable.getColumns().get(3);
        TableColumn<Evaluation, ?> c4 = evaluationTable.getColumns().get(4);
        TableColumn<Evaluation, ?> c5 = evaluationTable.getColumns().get(5);
        TableColumn<Evaluation, ?> c6 = evaluationTable.getColumns().get(6);
        TableColumn<Evaluation, ?> c7 = evaluationTable.getColumns().get(7);
        TableColumn<Evaluation, ?> c8 = evaluationTable.getColumns().get(8);
        TableColumn<Evaluation, ?> c9 = evaluationTable.getColumns().get(9);

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colTitle = (TableColumn<Evaluation, String>) c0;
        colTitle.setCellValueFactory(cv -> new SimpleStringProperty(cv.getValue().getTitre()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colDescription = (TableColumn<Evaluation, String>) c1;
        colDescription.setCellValueFactory(cv -> {
            String desc = cv.getValue().getDescription();
            String preview = (desc != null && desc.length() > 56) ? desc.substring(0, 56) + "…" : (desc == null ? "" : desc);
            return new SimpleStringProperty(preview);
        });

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, Integer> colDuration = (TableColumn<Evaluation, Integer>) c2;
        colDuration.setCellValueFactory(cv -> new javafx.beans.property.SimpleObjectProperty<>(cv.getValue().getDureeMinutes()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, Float> colPass = (TableColumn<Evaluation, Float>) c3;
        colPass.setCellValueFactory(cv -> new javafx.beans.property.SimpleObjectProperty<>(cv.getValue().getNotePassage()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, Float> colMax = (TableColumn<Evaluation, Float>) c4;
        colMax.setCellValueFactory(cv -> new javafx.beans.property.SimpleObjectProperty<>(cv.getValue().getNoteMax()));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colStart = (TableColumn<Evaluation, String>) c5;
        colStart.setCellValueFactory(cv -> new SimpleStringProperty(formatDateTime(cv.getValue().getDateDebut())));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colEnd = (TableColumn<Evaluation, String>) c6;
        colEnd.setCellValueFactory(cv -> new SimpleStringProperty(formatDateTime(cv.getValue().getDateFin())));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colCreated = (TableColumn<Evaluation, String>) c7;
        colCreated.setCellValueFactory(cv -> new SimpleStringProperty(formatDateTime(cv.getValue().getDateCreation())));

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colStatus = (TableColumn<Evaluation, String>) c8;
        colStatus.setCellValueFactory(cv -> new SimpleStringProperty(computeStatusLabel(cv.getValue())));
        colStatus.setCellFactory(column -> new TableCell<Evaluation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().setAll("status-pill");
                switch (item) {
                    case "Active" -> badge.getStyleClass().add("status-pill-active");
                    case "Upcoming" -> badge.getStyleClass().add("status-pill-upcoming");
                    case "Closed" -> badge.getStyleClass().add("status-pill-closed");
                    default -> badge.getStyleClass().add("status-pill-unknown");
                }
                setGraphic(badge);
            }
        });

        @SuppressWarnings("unchecked")
        TableColumn<Evaluation, String> colActions = (TableColumn<Evaluation, String>) c9;
        colActions.setCellValueFactory(cv -> new SimpleStringProperty(""));
        colActions.setCellFactory(column -> new TableCell<Evaluation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableView() == null) {
                    setGraphic(null);
                    return;
                }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                Evaluation evaluation = getTableView().getItems().get(idx);
                setGraphic(createActionButtons(evaluation));
            }
        });
    }

    private HBox createActionButtons(Evaluation evaluation) {
        HBox actionBox = new HBox(6);
        actionBox.setPadding(new Insets(2));

        Button btnView = new Button("View");
        btnView.getStyleClass().addAll("btn-row", "btn-row-view");
        btnView.setOnAction(e -> handleViewDetails(evaluation));

        Button btnEdit = new Button("Edit");
        btnEdit.getStyleClass().addAll("btn-row", "btn-row-edit");
        btnEdit.setOnAction(e -> handleEditEvaluation(evaluation));

        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().addAll("btn-row", "btn-row-delete");
        btnDelete.setOnAction(e -> handleDeleteEvaluation(evaluation));

        Button btnQuestions = new Button("Questions");
        btnQuestions.getStyleClass().addAll("btn-row", "btn-row-questions");
        btnQuestions.setOnAction(e -> handleManageQuestions(evaluation));

        actionBox.getChildren().addAll(btnView, btnEdit, btnDelete, btnQuestions);
        return actionBox;
    }

    /** Lifecycle status: Upcoming / Active / Closed from date window. */
    private String computeStatusLabel(Evaluation eval) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = eval.getDateDebut();
        LocalDateTime end = eval.getDateFin();

        if (start != null && end != null) {
            if (now.isBefore(start)) {
                return "Upcoming";
            }
            if (now.isAfter(end)) {
                return "Closed";
            }
            return "Active";
        }
        return "Unknown";
    }

    private int statusSortKey(Evaluation e) {
        return switch (computeStatusLabel(e)) {
            case "Upcoming" -> 0;
            case "Active" -> 1;
            case "Closed" -> 2;
            default -> 3;
        };
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "—" : dateTime.format(DATE_DISPLAY_FORMATTER);
    }

    private void populateFormFromEvaluation(Evaluation selected) {
        titreField.setText(selected.getTitre());
        descriptionField.setText(selected.getDescription() != null ? selected.getDescription() : "");
        dureeField.setText(String.valueOf(selected.getDureeMinutes()));
        noteMaxField.setText(String.valueOf(selected.getNoteMax()));
        notePassageField.setText(String.valueOf(selected.getNotePassage()));
        applyDateTimeToPickers(selected.getDateDebut(), dateDebutPicker, debutHourSpinner, debutMinuteSpinner, debutSecondSpinner);
        applyDateTimeToPickers(selected.getDateFin(), dateFinPicker, finHourSpinner, finMinuteSpinner, finSecondSpinner);
        dateCreationLabel.setText(formatDateTime(selected.getDateCreation()));
    }

    @FXML
    private void handleNewEvaluation() {
        handleClear();
        setStatus("New evaluation — fill the form and click Save (create).", false);
    }

    @FXML
    private void handleAdd() {
        try {
            if (!validateForm()) {
                return;
            }

            String titre = titreField.getText().trim();
            String description = descriptionField.getText();
            int duree = Integer.parseInt(dureeField.getText().trim());
            float noteMax = Float.parseFloat(noteMaxField.getText().trim());
            float notePassage = Float.parseFloat(notePassageField.getText().trim());
            LocalDateTime dateDebut = combineDateAndTime(dateDebutPicker, debutHourSpinner, debutMinuteSpinner, debutSecondSpinner);
            LocalDateTime dateFin = combineDateAndTime(dateFinPicker, finHourSpinner, finMinuteSpinner, finSecondSpinner);

            if (notePassage > noteMax) {
                showError("Passing grade cannot exceed max score.");
                return;
            }

            Evaluation evaluation = new Evaluation(titre, description, duree, noteMax, notePassage, dateDebut, dateFin);
            evaluationDAO.add(evaluation);

            setStatus("Evaluation created.", true);
            clearForm();
            loadAllEvaluations();
        } catch (Exception e) {
            showError("Could not create evaluation: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            if (selectedEvaluation == null) {
                showError("Select an evaluation in the table (or use Edit on a row).");
                return;
            }
            if (!validateForm()) {
                return;
            }

            String titre = titreField.getText().trim();
            String description = descriptionField.getText();
            int duree = Integer.parseInt(dureeField.getText().trim());
            float noteMax = Float.parseFloat(noteMaxField.getText().trim());
            float notePassage = Float.parseFloat(notePassageField.getText().trim());
            LocalDateTime dateDebut = combineDateAndTime(dateDebutPicker, debutHourSpinner, debutMinuteSpinner, debutSecondSpinner);
            LocalDateTime dateFin = combineDateAndTime(dateFinPicker, finHourSpinner, finMinuteSpinner, finSecondSpinner);

            if (notePassage > noteMax) {
                showError("Passing grade cannot exceed max score.");
                return;
            }

            Evaluation updated = new Evaluation(selectedEvaluation.getId(), titre, description, duree, noteMax,
                    notePassage, dateDebut, dateFin, selectedEvaluation.getDateCreation());
            evaluationDAO.update(updated);

            setStatus("Evaluation updated.", true);
            clearForm();
            loadAllEvaluations();
        } catch (Exception e) {
            showError("Could not update evaluation: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        try {
            if (selectedEvaluation == null) {
                showError("Select an evaluation to delete.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm delete");
            alert.setHeaderText("Delete this evaluation?");
            alert.setContentText(selectedEvaluation.getTitre());

            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                evaluationDAO.delete(selectedEvaluation.getId());
                setStatus("Evaluation deleted.", true);
                clearForm();
                loadAllEvaluations();
            }
        } catch (Exception e) {
            showError("Could not delete: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        currentPageIndex = 0;
        applyViewPipeline();
        setStatus("Filtered list updated.", false);
    }

    @FXML
    private void handleSearchKey() {
        currentPageIndex = 0;
        applyViewPipeline();
    }

    @FXML
    private void handleShowAll() {
        searchField.clear();
        currentPageIndex = 0;
        applyViewPipeline();
        setStatus("Showing all evaluations.", false);
    }

    @FXML
    private void handlePrevPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            applyViewPipeline();
        }
    }

    @FXML
    private void handleNextPage() {
        int pageSize = pageSizeCombo.getValue() != null ? pageSizeCombo.getValue() : 12;
        int maxPage = (int) Math.ceil(viewPipeline.size() / (double) pageSize) - 1;
        if (currentPageIndex < maxPage) {
            currentPageIndex++;
            applyViewPipeline();
        }
    }

    private void handleViewDetails(Evaluation evaluation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Evaluation details");
        alert.setHeaderText(evaluation.getTitre());
        alert.setContentText(
                "Description:\n" + (evaluation.getDescription() == null ? "—" : evaluation.getDescription()) + "\n\n"
                        + "Duration: " + evaluation.getDureeMinutes() + " min\n"
                        + "Passing grade: " + evaluation.getNotePassage() + "\n"
                        + "Max score: " + evaluation.getNoteMax() + "\n"
                        + "Start: " + formatDateTime(evaluation.getDateDebut()) + "\n"
                        + "End: " + formatDateTime(evaluation.getDateFin()) + "\n"
                        + "Created: " + formatDateTime(evaluation.getDateCreation()) + "\n"
                        + "Status: " + computeStatusLabel(evaluation)
        );
        alert.showAndWait();
    }

    private void handleEditEvaluation(Evaluation evaluation) {
        evaluationTable.getSelectionModel().select(evaluation);
        populateFormFromEvaluation(evaluation);
        selectedEvaluation = evaluation;
        setStatus("Editing: " + evaluation.getTitre(), false);
    }

    private void handleDeleteEvaluation(Evaluation evaluation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete evaluation");
        alert.setHeaderText("Permanently delete?");
        alert.setContentText(evaluation.getTitre());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            evaluationDAO.delete(evaluation.getId());
            if (selectedEvaluation != null && selectedEvaluation.getId() == evaluation.getId()) {
                clearForm();
            }
            setStatus("Evaluation deleted.", true);
            loadAllEvaluations();
        }
    }

    private void handleManageQuestions(Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/questions.fxml"));
            Parent root = loader.load();
            QuestionsController controller = loader.getController();
            controller.setEvaluation(evaluation);
            Stage stage = new Stage();
            stage.setTitle("Questions — " + evaluation.getTitre());
            stage.setScene(new Scene(root, 980, 760));
            stage.setMinWidth(720);
            stage.setMinHeight(520);
            if (evaluationTable.getScene() != null && evaluationTable.getScene().getWindow() != null) {
                stage.initOwner(evaluationTable.getScene().getWindow());
                stage.initModality(Modality.WINDOW_MODAL);
            }
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open questions: " + e.getMessage());
        }
    }

    private void loadAllEvaluations() {
        try {
            List<Evaluation> evaluations = evaluationDAO.getAll();
            masterEvaluations.setAll(evaluations);
            applyViewPipeline();
            setStatus("Ready", true);
        } catch (RuntimeException e) {
            setStatus("Unable to load evaluations.", false, true);
            masterEvaluations.clear();
            evaluationTable.setItems(FXCollections.observableArrayList());
            emptyLabel.setVisible(true);
            evaluationTable.setVisible(false);
        }
    }

    private void applyViewPipeline() {
        String keyword = searchField.getText() != null ? searchField.getText().trim().toLowerCase(Locale.ROOT) : "";

        List<Evaluation> filtered = new ArrayList<>(masterEvaluations);
        if (!keyword.isEmpty()) {
            filtered.removeIf(e -> e.getTitre() == null || !e.getTitre().toLowerCase(Locale.ROOT).contains(keyword));
        }

        Comparator<Evaluation> comparator = resolveComparator();
        filtered.sort(comparator);
        viewPipeline = filtered;

        totalLabel.setText(String.valueOf(viewPipeline.size()));

        int pageSize = pageSizeCombo.getValue() != null ? pageSizeCombo.getValue() : 12;
        int total = viewPipeline.size();
        int pageCount = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        if (currentPageIndex >= pageCount) {
            currentPageIndex = Math.max(0, pageCount - 1);
        }

        int from = currentPageIndex * pageSize;
        int to = Math.min(from + pageSize, total);
        List<Evaluation> pageItems = from >= total ? List.of() : viewPipeline.subList(from, to);

        evaluationTable.setItems(FXCollections.observableArrayList(pageItems));
        emptyLabel.setVisible(total == 0);
        evaluationTable.setVisible(total > 0);

        pageInfoLabel.setText(String.format("Page %d of %d · %d item(s)",
                currentPageIndex + 1, pageCount, total));

        prevPageBtn.setDisable(currentPageIndex <= 0);
        nextPageBtn.setDisable(currentPageIndex >= pageCount - 1);
    }

    private Comparator<Evaluation> resolveComparator() {
        String mode = sortCombo.getValue();
        if (mode == null) {
            return Comparator.comparing(Evaluation::getDateDebut, Comparator.nullsLast(Comparator.reverseOrder()));
        }
        return switch (mode) {
            case "Start date · newest first" ->
                    Comparator.comparing(Evaluation::getDateDebut, Comparator.nullsLast(Comparator.reverseOrder()));
            case "Start date · oldest first" ->
                    Comparator.comparing(Evaluation::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder()));
            case "End date · latest first" ->
                    Comparator.comparing(Evaluation::getDateFin, Comparator.nullsLast(Comparator.reverseOrder()));
            case "End date · earliest first" ->
                    Comparator.comparing(Evaluation::getDateFin, Comparator.nullsLast(Comparator.naturalOrder()));
            case "Created · newest first" ->
                    Comparator.comparing(Evaluation::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder()));
            case "Created · oldest first" ->
                    Comparator.comparing(Evaluation::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder()));
            case "Title · A to Z" ->
                    Comparator.comparing(e -> e.getTitre() != null ? e.getTitre().toLowerCase(Locale.ROOT) : "",
                            Comparator.naturalOrder());
            case "Title · Z to A" ->
                    Comparator.comparing(e -> e.getTitre() != null ? e.getTitre().toLowerCase(Locale.ROOT) : "",
                            Comparator.reverseOrder());
            case "Status · Upcoming → Active → Closed" ->
                    Comparator.comparing(this::statusSortKey).thenComparing(Evaluation::getDateDebut,
                            Comparator.nullsLast(Comparator.naturalOrder()));
            default ->
                    Comparator.comparing(Evaluation::getDateDebut, Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    @FXML
    private void handleClear() {
        clearForm();
        selectedEvaluation = null;
        evaluationTable.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        titreField.clear();
        descriptionField.clear();
        dureeField.clear();
        noteMaxField.clear();
        notePassageField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        wireTimeSpinner(debutHourSpinner, 0, 23, 0);
        wireTimeSpinner(debutMinuteSpinner, 0, 59, 0);
        wireTimeSpinner(debutSecondSpinner, 0, 59, 0);
        wireTimeSpinner(finHourSpinner, 0, 23, 0);
        wireTimeSpinner(finMinuteSpinner, 0, 59, 0);
        wireTimeSpinner(finSecondSpinner, 0, 59, 0);
        dateCreationLabel.setText("—");
        selectedEvaluation = null;
    }

    private boolean validateForm() {
        if (titreField.getText() == null || titreField.getText().trim().isEmpty()) {
            showError("Title is required.");
            return false;
        }
        try {
            Integer.parseInt(dureeField.getText().trim());
            Float.parseFloat(noteMaxField.getText().trim());
            Float.parseFloat(notePassageField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Check duration and numeric score fields.");
            return false;
        }

        LocalDateTime start = combineDateAndTime(dateDebutPicker, debutHourSpinner, debutMinuteSpinner, debutSecondSpinner);
        LocalDateTime end = combineDateAndTime(dateFinPicker, finHourSpinner, finMinuteSpinner, finSecondSpinner);
        if (start == null) {
            showError("Choose a start date from the calendar.");
            return false;
        }
        if (end == null) {
            showError("Choose an end date from the calendar.");
            return false;
        }
        if (!end.isAfter(start)) {
            showError("End date and time must be after the start.");
            return false;
        }
        return true;
    }

    private void setStatus(String message, boolean ok) {
        setStatus(message, ok, false);
    }

    private void setStatus(String message, boolean ok, boolean error) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("footer-status", "footer-status-ok", "footer-status-neutral", "footer-status-error");
        statusLabel.getStyleClass().add("footer-status");
        if (error) {
            statusLabel.getStyleClass().add("footer-status-error");
        } else if (ok) {
            statusLabel.getStyleClass().add("footer-status-ok");
        } else {
            statusLabel.getStyleClass().add("footer-status-neutral");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("footer-status", "footer-status-ok", "footer-status-neutral", "footer-status-error");
        statusLabel.getStyleClass().addAll("footer-status", "footer-status-error");
    }
}
