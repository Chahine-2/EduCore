package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Evaluation;
import models.Resultat;
import services.EvaluationDAOImpl;
import services.ResultatDAOImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Lists evaluations that are currently active (within start/end window). Opens {@link StudentQuizController} on demand.
 */
public class StudentPortalController {

    private static final DateTimeFormatter META_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm", Locale.ENGLISH);

    @FXML private BorderPane root;
    @FXML private TextField studentIdField;
    @FXML private FlowPane cardsFlow;
    @FXML private Label countLabel;
    @FXML private Label emptyLabel;

    private final EvaluationDAOImpl evaluationDAO = new EvaluationDAOImpl();
    private final ResultatDAOImpl resultatDAO = new ResultatDAOImpl();

    @FXML
    public void initialize() {
        studentIdField.setText("1");
        refreshCards();
    }

    @FXML
    private void handleRefresh() {
        refreshCards();
    }

    @FXML
    private void handleOpenAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluation.fxml"));
            Parent adminRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("EDUCORE · Administration");
            stage.setMinWidth(880);
            stage.setMinHeight(600);
            stage.setWidth(1440);
            stage.setHeight(920);
            stage.setScene(new Scene(adminRoot));
            if (root.getScene() != null && root.getScene().getWindow() != null) {
                stage.initOwner(root.getScene().getWindow());
            }
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshCards() {
        cardsFlow.getChildren().clear();
        LocalDateTime now = LocalDateTime.now();
        List<Evaluation> active = new ArrayList<>();
        for (Evaluation ev : evaluationDAO.getAll()) {
            if (isActive(ev, now)) {
                active.add(ev);
            }
        }

        countLabel.setText(active.size() == 1
                ? "1 assessment available now"
                : active.size() + " assessments available now");
        emptyLabel.setVisible(active.isEmpty());
        emptyLabel.setManaged(active.isEmpty());

        int studentId = parseStudentIdQuiet();
        List<Evaluation> pending = new ArrayList<>();
        List<Evaluation> completed = new ArrayList<>();
        Map<Integer, Resultat> latestByEvalId = new HashMap<>();
        for (Evaluation ev : active) {
            Resultat latest = null;
            if (studentId > 0) {
                latest = resultatDAO.findLatestByStudentAndEvaluation(studentId, ev.getId());
            }
            if (latest != null) {
                completed.add(ev);
                latestByEvalId.put(ev.getId(), latest);
            } else {
                pending.add(ev);
            }
        }
        completed.sort(Comparator.comparing(
                ev -> latestByEvalId.get(ev.getId()).getDatePassage(),
                Comparator.nullsFirst(Comparator.naturalOrder())));

        List<Evaluation> ordered = new ArrayList<>(pending);
        ordered.addAll(completed);

        for (Evaluation ev : ordered) {
            cardsFlow.getChildren().add(buildCard(ev, latestByEvalId.get(ev.getId())));
        }
    }

    /** Returns a positive student id, or -1 if the field is empty or invalid (no dialog). */
    private int parseStudentIdQuiet() {
        try {
            int id = Integer.parseInt(studentIdField.getText().trim());
            return id > 0 ? id : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Quiz window is open between {@code date_debut} and {@code date_fin} (inclusive). */
    public static boolean isActive(Evaluation e, LocalDateTime now) {
        if (e.getDateDebut() == null || e.getDateFin() == null) {
            return false;
        }
        return !now.isBefore(e.getDateDebut()) && !now.isAfter(e.getDateFin());
    }

    private VBox buildCard(Evaluation ev, Resultat submitted) {
        VBox card = new VBox(12);
        card.getStyleClass().add("portal-card");
        if (submitted != null) {
            card.getStyleClass().add("portal-card-done");
        }
        card.setAlignment(Pos.TOP_LEFT);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(submitted == null ? "Live now" : "Submitted");
        badge.getStyleClass().add(submitted == null ? "portal-badge-live" : "portal-badge-done");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        top.getChildren().addAll(badge, sp);

        Label title = new Label(ev.getTitre());
        title.getStyleClass().add("portal-card-title");
        title.setWrapText(true);

        String desc = ev.getDescription();
        if (desc != null && desc.length() > 140) {
            desc = desc.substring(0, 137) + "…";
        }
        Label descLbl = new Label(desc == null || desc.isBlank() ? "No description provided." : desc);
        descLbl.getStyleClass().add("portal-card-desc");
        descLbl.setWrapText(true);

        String meta = ev.getDureeMinutes() + " min · Ends " + formatDt(ev.getDateFin());
        if (submitted != null) {
            String score = submitted.getScore() == null ? "Score pending" : "Score: " + formatScore(submitted.getScore());
            meta = meta + " · " + score;
            if (submitted.getDatePassage() != null) {
                meta = meta + " · " + submitted.getDatePassage().format(META_FMT);
            }
        }
        Label metaLbl = new Label(meta);
        metaLbl.getStyleClass().add("portal-meta");

        Button action = new Button(submitted == null ? "Start quiz" : "View attempt");
        action.getStyleClass().addAll(submitted == null ? "portal-start" : "portal-view");
        action.setMaxWidth(Double.MAX_VALUE);
        if (submitted == null) {
            action.setOnAction(e -> openQuiz(ev, null));
        } else {
            final Resultat r = submitted;
            action.setOnAction(e -> openQuiz(ev, r));
        }

        card.getChildren().addAll(top, title, descLbl, metaLbl, action);
        return card;
    }

    private static String formatScore(float s) {
        if (Math.abs(s - Math.rint(s)) < 1e-4f) {
            return String.valueOf((int) s);
        }
        return String.valueOf(s);
    }

    private String formatDt(LocalDateTime dt) {
        return dt == null ? "—" : dt.format(META_FMT);
    }

    /**
     * @param existingResultat if non-null, opens read-only review; otherwise a new timed attempt
     */
    private void openQuiz(Evaluation evaluation, Resultat existingResultat) {
        int studentId;
        try {
            studentId = Integer.parseInt(studentIdField.getText().trim());
            if (studentId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            a.initOwner(root.getScene().getWindow());
            a.setHeaderText(null);
            a.setContentText("Enter a valid positive student ID at the top of the page.");
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/student-quiz.fxml"));
            Parent quizRoot = loader.load();
            StudentQuizController controller = loader.getController();
            if (existingResultat == null) {
                controller.setEvaluationAndStudent(evaluation, studentId);
            } else {
                controller.openInViewMode(evaluation, studentId, existingResultat);
            }
            Stage stage = new Stage();
            stage.setTitle(existingResultat == null ? "Quiz — " + evaluation.getTitre() : "Review — " + evaluation.getTitre());
            stage.setScene(new Scene(quizRoot, 920, 800));
            stage.setMinWidth(640);
            stage.setMinHeight(560);
            if (root.getScene() != null && root.getScene().getWindow() != null) {
                stage.initOwner(root.getScene().getWindow());
                stage.initModality(Modality.WINDOW_MODAL);
            }
            stage.centerOnScreen();
            stage.showAndWait();
            refreshCards();
        } catch (Exception ex) {
            ex.printStackTrace();
            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            a.initOwner(root.getScene().getWindow());
            a.setHeaderText(null);
            a.setContentText("Could not open quiz: " + ex.getMessage());
            a.showAndWait();
        }
    }
}
