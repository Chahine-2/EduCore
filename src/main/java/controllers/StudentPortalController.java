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
import services.QuestionDAOImpl;
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
    private final QuestionDAOImpl questionDAO = new QuestionDAOImpl();
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
    private void handleBackHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent homeRoot = loader.load();
            if (root.getScene() != null && root.getScene().getWindow() instanceof Stage stage) {
                stage.setScene(new Scene(homeRoot));
                stage.setTitle("EDUCORE");
                stage.centerOnScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        card.setStyle(submitted == null
                ? "-fx-background-color: rgba(255,255,255,0.96); -fx-background-radius: 12; -fx-border-color: #dbeafe; -fx-border-radius: 12; -fx-padding: 14 16; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 12, 0.2, 0, 2);"
                : "-fx-background-color: linear-gradient(to bottom, #fffef7, #fffdf2); -fx-background-radius: 12; -fx-border-color: #fcd34d; -fx-border-radius: 12; -fx-padding: 14 16; -fx-effect: dropshadow(gaussian, rgba(120,53,15,0.10), 12, 0.2, 0, 2);");
        card.setPrefWidth(300);
        card.setMinWidth(280);
        card.setMaxWidth(340);
        card.setAlignment(Pos.TOP_LEFT);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(submitted == null ? "Live now" : "Submitted");
        badge.setStyle(submitted == null
                ? "-fx-padding: 4 10; -fx-background-color: #d1fae5; -fx-background-radius: 999; -fx-text-fill: #065f46; -fx-font-size: 11px; -fx-font-weight: 800;"
                : "-fx-padding: 4 10; -fx-background-color: #fef3c7; -fx-background-radius: 999; -fx-text-fill: #92400e; -fx-font-size: 11px; -fx-font-weight: 800;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        top.getChildren().addAll(badge, sp);

        Label title = new Label(ev.getTitre());
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        title.setWrapText(true);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, Priority.ALWAYS);
        titleRow.getChildren().add(title);

        if (submitted != null && submitted.getScore() != null) {
            float totalPoints = questionDAO.findByEvaluationId(ev.getId()).stream()
                    .map(q -> q.getPoints())
                    .reduce(0f, Float::sum);
            Label titleScore = new Label(formatScore(submitted.getScore()) + "/" + formatScore(totalPoints));
            titleScore.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #1e3a8a; -fx-background-color: #dbeafe; -fx-background-radius: 999; -fx-padding: 3 9;");
            titleRow.getChildren().add(titleScore);
        }

        String desc = ev.getDescription();
        if (desc != null && desc.length() > 140) {
            desc = desc.substring(0, 137) + "…";
        }
        Label descLbl = new Label(desc == null || desc.isBlank() ? "No description provided." : desc);
        descLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        descLbl.setWrapText(true);

        String meta = ev.getDureeMinutes() + " min · Ends " + formatDt(ev.getDateFin());
        if (submitted != null) {
            if (submitted.getDatePassage() != null) {
                meta = meta + " · Submitted " + submitted.getDatePassage().format(META_FMT);
            }
        }
        Label metaLbl = new Label(meta);
        metaLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        metaLbl.setWrapText(true);

        Button action = new Button(submitted == null ? "Start quiz" : "View attempt");
        action.setStyle(submitted == null
                ? "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 9 12;"
                : "-fx-background-color: white; -fx-text-fill: #1e40af; -fx-font-weight: 700; -fx-border-color: #93c5fd; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 9 12;");
        action.setMaxWidth(Double.MAX_VALUE);
        if (submitted == null) {
            action.setOnAction(e -> openQuiz(ev, null));
        } else {
            final Resultat r = submitted;
            action.setOnAction(e -> openQuiz(ev, r));
        }

        card.getChildren().addAll(top, titleRow, descLbl, metaLbl, action);
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
