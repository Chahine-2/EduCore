package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Evaluation;
import models.Question;
import models.QuestionType;
import models.Reponse;
import models.ReponseEtudiant;
import models.Resultat;
import services.QuestionDAOImpl;
import services.ReponseDAOImpl;
import services.ReponseEtudiantDAOImpl;
import services.ResultatDAOImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Student quiz: timed attempt or read-only review of a submitted attempt.
 */
public class StudentQuizController {

    private static final DateTimeFormatter SUBMITTED_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.ENGLISH);

    @FXML private BorderPane root;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label timerLabel;
    @FXML private Label timerCaptionLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private VBox questionHost;
    @FXML private ScrollPane quizScroll;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Button submitBtn;
    @FXML private Label navHintLabel;

    private Evaluation evaluation;
    private int studentId;
    private List<Question> questions;
    private final QuestionDAOImpl questionDAO = new QuestionDAOImpl();
    private final ReponseDAOImpl reponseDAO = new ReponseDAOImpl();
    private final ReponseEtudiantDAOImpl reponseEtudiantDAO = new ReponseEtudiantDAOImpl();
    private final ResultatDAOImpl resultatDAO = new ResultatDAOImpl();

    private boolean viewMode;
    private Resultat viewResultat;

    private int currentIndex;
    private final Map<Integer, Integer> selectedReponseByQuestion = new HashMap<>();
    private final Map<Integer, String> texteLibreByQuestion = new HashMap<>();

    private Timeline countdown;
    private int remainingSeconds;
    private boolean submitted;

    private ToggleGroup activeToggleGroup;
    private TextArea activeTextArea;

    /** Normal timed attempt. */
    public void setEvaluationAndStudent(Evaluation evaluation, int studentId) {
        this.viewMode = false;
        this.viewResultat = null;
        this.evaluation = evaluation;
        this.studentId = studentId;
        this.submitted = false;
        applyLiveChrome();
        initQuestionsAndUi();
        subtitleLabel.setText(evaluation.getDescription() != null && !evaluation.getDescription().isBlank()
                ? evaluation.getDescription()
                : "Answer all parts to the best of your ability.");
        if (!questions.isEmpty()) {
            remainingSeconds = Math.max(60, evaluation.getDureeMinutes() * 60);
            if (timerCaptionLabel != null) {
                timerCaptionLabel.setText("TIME LEFT");
            }
            updateTimerDisplay();
            startCountdown();
        }
        renderQuestion();
        updateNavState();
    }

    /** Read-only review of a submitted attempt (no timer, no submit). */
    public void openInViewMode(Evaluation evaluation, int studentId, Resultat submittedResultat) {
        this.viewMode = true;
        this.viewResultat = submittedResultat;
        this.evaluation = evaluation;
        this.studentId = studentId;
        this.submitted = true;
        applyLiveChrome();
        root.getStyleClass().add("quiz-view-mode");
        initQuestionsAndUi();
        loadSubmittedAnswers(submittedResultat.getId());

        titleLabel.setText(evaluation.getTitre());
        String scorePart = submittedResultat.getScore() == null
                ? "Score pending"
                : "Score: " + formatScore(submittedResultat.getScore());
        String when = submittedResultat.getDatePassage() != null
                ? "Submitted " + submittedResultat.getDatePassage().format(SUBMITTED_FMT)
                : "Submitted";
        subtitleLabel.setText("View only · " + when + " · " + scorePart);

        if (timerCaptionLabel != null) {
            timerCaptionLabel.setText("REVIEW");
        }
        timerLabel.getStyleClass().removeAll("quiz-timer-warn", "quiz-timer-critical");
        timerLabel.setText("—");

        submitBtn.setText("Close");
        submitBtn.setOnAction(e -> closeQuizWindow());

        if (!questions.isEmpty()) {
            progressBar.setProgress((currentIndex + 1.0) / questions.size());
            progressLabel.setText("Question " + (currentIndex + 1) + " / " + questions.size());
        }
        renderQuestion();
        updateNavState();
    }

    private void applyLiveChrome() {
        root.setStyle("-fx-font-family: 'Segoe UI'; -fx-background-color: #f8fafc;");
    }

    private void initQuestionsAndUi() {
        titleLabel.setText(evaluation.getTitre());
        questions = questionDAO.findByEvaluationId(evaluation.getId()).stream()
                .filter(q -> q.getType() == QuestionType.QCM || q.getType() == QuestionType.VRAI_FAUX)
                .toList();
        currentIndex = 0;
        if (!viewMode) {
            selectedReponseByQuestion.clear();
            texteLibreByQuestion.clear();
        }

        if (questions.isEmpty()) {
            questionHost.getChildren().clear();
            Label empty = new Label("This evaluation has no questions yet.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-padding: 24;");
            questionHost.getChildren().add(empty);
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            submitBtn.setDisable(true);
            timerLabel.setText("—");
            progressBar.setProgress(0);
            progressLabel.setText("No questions");
            return;
        }

        if (viewMode) {
            return;
        }

        submitBtn.setText("Submit quiz");
        submitBtn.setOnAction(e -> handleSubmitClick());
    }

    private void loadSubmittedAnswers(int resultatId) {
        selectedReponseByQuestion.clear();
        texteLibreByQuestion.clear();
        for (ReponseEtudiant re : reponseEtudiantDAO.findByResultatId(resultatId)) {
            if (re.getReponseId() != null) {
                selectedReponseByQuestion.put(re.getQuestionId(), re.getReponseId());
            } else {
                texteLibreByQuestion.put(re.getQuestionId(),
                        re.getTexteLibre() != null ? re.getTexteLibre() : "");
            }
        }
    }

    private static String formatScore(float s) {
        if (Math.abs(s - Math.rint(s)) < 1e-4f) {
            return String.valueOf((int) s);
        }
        return String.valueOf(s);
    }

    private void startCountdown() {
        if (viewMode) {
            return;
        }
        if (countdown != null) {
            countdown.stop();
        }
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remainingSeconds--;
            updateTimerDisplay();
            if (remainingSeconds <= 0) {
                countdown.stop();
                Platform.runLater(this::submitWhenTimeUp);
            }
        }));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void updateTimerDisplay() {
        if (viewMode) {
            return;
        }
        int m = Math.max(0, remainingSeconds) / 60;
        int s = Math.max(0, remainingSeconds) % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
        timerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #1e3a8a;");
        if (remainingSeconds <= 60) {
            timerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #dc2626;");
        } else if (remainingSeconds <= 300) {
            timerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #d97706;");
        }
    }

    private void submitWhenTimeUp() {
        if (submitted || viewMode) {
            return;
        }
        persistSubmission(true);
    }

    @FXML
    private void handlePrevious() {
        if (currentIndex <= 0) {
            return;
        }
        captureFromActiveUi();
        currentIndex--;
        renderQuestion();
        updateNavState();
        quizScroll.setVvalue(0);
    }

    @FXML
    private void handleNext() {
        if (currentIndex >= questions.size() - 1) {
            return;
        }
        captureFromActiveUi();
        currentIndex++;
        renderQuestion();
        updateNavState();
        quizScroll.setVvalue(0);
    }

    @FXML
    private void handleSubmitClick() {
        if (viewMode) {
            closeQuizWindow();
            return;
        }
        if (submitted || questions.isEmpty()) {
            return;
        }
        captureFromActiveUi();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(root.getScene().getWindow());
        confirm.setTitle("Submit quiz");
        confirm.setHeaderText("Submit your answers?");
        confirm.setContentText("You will not be able to change responses after submitting.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            persistSubmission(false);
        }
    }

    @FXML
    private void handleBackHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent homeRoot = loader.load();
            Stage stage = root.getScene() != null && root.getScene().getWindow() instanceof Stage s ? s : null;
            if (stage != null) {
                stage.setScene(new Scene(homeRoot));
                stage.setTitle("EDUCORE");
                stage.centerOnScreen();
            }
        } catch (Exception e) {
            alertError("Could not open home page: " + e.getMessage());
        }
    }

    private void closeQuizWindow() {
        var w = root.getScene() != null ? root.getScene().getWindow() : null;
        if (w instanceof javafx.stage.Stage st) {
            st.close();
        }
    }

    private void captureFromActiveUi() {
        if (viewMode || questions.isEmpty()) {
            return;
        }
        Question q = questions.get(currentIndex);
        switch (q.getType()) {
            case TEXTE_LIBRE -> {
                if (activeTextArea != null) {
                    texteLibreByQuestion.put(q.getId(), activeTextArea.getText());
                }
            }
            case QCM, VRAI_FAUX -> {
                if (activeToggleGroup != null) {
                    Toggle sel = activeToggleGroup.getSelectedToggle();
                    if (sel instanceof RadioButton rb && rb.getUserData() instanceof Integer rid) {
                        selectedReponseByQuestion.put(q.getId(), rid);
                    }
                }
            }
        }
    }

    private void renderQuestion() {
        questionHost.getChildren().clear();
        activeToggleGroup = null;
        activeTextArea = null;

        if (questions.isEmpty()) {
            return;
        }

        Question q = questions.get(currentIndex);
        List<Reponse> reps = reponseDAO.findByQuestionId(q.getId());

        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 12; -fx-border-color: #dbeafe; -fx-border-radius: 12; -fx-padding: 16;");
        card.setMaxWidth(720);
        card.setMinWidth(640);

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(typeLabel(q.getType()));
        badge.setStyle(badgeStyle(q.getType()));
        Label pts = new Label(pointsLabel(q.getPoints()));
        pts.setStyle("-fx-text-fill: #334155; -fx-font-weight: 700;");
        meta.getChildren().addAll(badge, pts);

        Label qText = new Label(q.getTexte());
        qText.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #0f172a;");
        qText.setWrapText(true);

        VBox answers = new VBox(8);

        switch (q.getType()) {
            case TEXTE_LIBRE -> {
                TextArea ta = new TextArea();
                ta.getStyleClass().add("quiz-textarea");
                ta.setWrapText(true);
                ta.setPromptText("Type your answer here…");
                ta.setText(texteLibreByQuestion.getOrDefault(q.getId(), ""));
                ta.setEditable(!viewMode);
                ta.setFocusTraversable(!viewMode);
                activeTextArea = ta;
                answers.getChildren().add(ta);
            }
            case QCM -> buildChoiceAnswers(q, reps, answers, true);
            case VRAI_FAUX -> {
                List<Reponse> ordered = orderVraiFauxForQuiz(reps);
                if (ordered.size() >= 2) {
                    buildChoiceAnswers(q, ordered, answers, false);
                } else {
                    Label warn = new Label(
                            "This Vrai/Faux question has no choices in the database. Open question management, edit the question, and save so “Vrai” and “Faux” are created.");
                    warn.setWrapText(true);
                    warn.setStyle("-fx-text-fill: #b45309;");
                    answers.getChildren().add(warn);
                }
            }
        }

        card.getChildren().addAll(meta, qText, answers);
        questionHost.getChildren().add(card);

        double progress = (currentIndex + 1.0) / questions.size();
        progressBar.setProgress(progress);
        progressLabel.setText("Question " + (currentIndex + 1) + " / " + questions.size());

        if (viewMode) {
            navHintLabel.setText("Read-only · your submitted answers");
        } else {
            int unanswered = countUnanswered();
            navHintLabel.setText(unanswered > 0 ? unanswered + " question(s) without an answer" : "All questions visited");
        }
    }

    private void buildChoiceAnswers(Question q, List<Reponse> reps, VBox answers, boolean qcm) {
        ToggleGroup group = new ToggleGroup();
        activeToggleGroup = group;

        Integer saved = selectedReponseByQuestion.get(q.getId());

        for (Reponse r : reps) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #dbeafe; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10;");
            row.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(row, Priority.ALWAYS);

            RadioButton rb = new RadioButton(r.getTexte());
            rb.setToggleGroup(group);
            rb.setWrapText(true);
            rb.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(rb, Priority.ALWAYS);
            rb.setUserData(r.getId());
            rb.setDisable(viewMode);

            if (saved != null && saved.equals(r.getId())) {
                rb.setSelected(true);
                row.setStyle("-fx-background-color: #eef2ff; -fx-border-color: #a5b4fc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10;");
            }

            rb.selectedProperty().addListener((obs, o, n) -> {
                if (n) {
                    row.setStyle("-fx-background-color: #eef2ff; -fx-border-color: #a5b4fc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10;");
                } else {
                    row.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #dbeafe; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 10;");
                }
            });

            row.setOnMouseClicked(e -> {
                if (!viewMode && !rb.isDisabled()) {
                    rb.setSelected(true);
                    e.consume();
                }
            });

            row.getChildren().add(rb);
            answers.getChildren().add(row);
        }

        if (!viewMode) {
            group.selectedToggleProperty().addListener((obs, o, n) -> {
                if (n instanceof RadioButton rb && rb.getUserData() instanceof Integer rid) {
                    selectedReponseByQuestion.put(q.getId(), rid);
                }
            });
        }

        if (reps.isEmpty()) {
            Label empty = new Label(qcm ? "No choices configured for this question." : "No choices configured.");
            empty.setStyle("-fx-text-fill: #94a3b8;");
            answers.getChildren().add(empty);
        }
    }

    private int countUnanswered() {
        if (viewMode) {
            return 0;
        }
        captureFromActiveUi();
        int n = 0;
        for (Question q : questions) {
            switch (q.getType()) {
                case TEXTE_LIBRE -> {
                    String t = texteLibreByQuestion.get(q.getId());
                    if (t == null || t.trim().isEmpty()) {
                        n++;
                    }
                }
                case QCM, VRAI_FAUX -> {
                    if (!selectedReponseByQuestion.containsKey(q.getId())) {
                        n++;
                    }
                }
            }
        }
        return n;
    }

    private void updateNavState() {
        prevBtn.setDisable(currentIndex == 0);
        nextBtn.setDisable(currentIndex >= questions.size() - 1);
        if (viewMode) {
            submitBtn.setDisable(false);
            return;
        }
        submitBtn.setDisable(submitted);
    }

    private void persistSubmission(boolean timeUp) {
        if (viewMode || submitted || questions.isEmpty()) {
            return;
        }
        captureFromActiveUi();
        if (countdown != null) {
            countdown.stop();
        }

        LocalDateTime now = LocalDateTime.now();
        Resultat resultat = new Resultat(studentId, evaluation.getId(), null, now);
        int resultatId = resultatDAO.insertAndGetId(resultat);
        if (resultatId <= 0) {
            alertError("Could not save your attempt. Please try again.");
            return;
        }

        for (Question q : questions) {
            Integer rid = selectedReponseByQuestion.get(q.getId());
            String txt = texteLibreByQuestion.get(q.getId());
            switch (q.getType()) {
                case TEXTE_LIBRE ->
                        reponseEtudiantDAO.add(new ReponseEtudiant(resultatId, q.getId(), null,
                                txt != null ? txt : ""));
                case QCM, VRAI_FAUX ->
                        reponseEtudiantDAO.add(new ReponseEtudiant(resultatId, q.getId(), rid, null));
            }
        }

        resultatDAO.corrigerEvaluation(resultatId);

        submitted = true;
        submitBtn.setDisable(true);
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);

        Alert done = new Alert(Alert.AlertType.INFORMATION);
        done.initOwner(root.getScene().getWindow());
        done.setTitle(timeUp ? "Time is up" : "Submitted");
        done.setHeaderText(timeUp ? "The quiz closed automatically." : "Your responses were submitted.");
        done.setContentText("Your attempt has been recorded.");
        done.showAndWait();

        closeQuizWindow();
    }

    private void alertError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(root.getScene().getWindow());
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private static String typeLabel(QuestionType t) {
        return switch (t) {
            case QCM -> "Multiple choice";
            case VRAI_FAUX -> "Vrai / Faux";
            case TEXTE_LIBRE -> "Open answer";
        };
    }

    /** Vrai first, Faux second (same idea as the instructor QCM-style rows). */
    private static List<Reponse> orderVraiFauxForQuiz(List<Reponse> reps) {
        if (reps == null || reps.isEmpty()) {
            return List.of();
        }
        List<Reponse> copy = new ArrayList<>(reps);
        copy.sort((a, b) -> Integer.compare(vfOrderKey(a.getTexte()), vfOrderKey(b.getTexte())));
        return copy;
    }

    private static int vfOrderKey(String texte) {
        if (texte == null) {
            return 2;
        }
        String t = texte.trim();
        if (t.equalsIgnoreCase("Vrai")) {
            return 0;
        }
        if (t.equalsIgnoreCase("Faux")) {
            return 1;
        }
        return 2;
    }

    private static String badgeStyle(QuestionType t) {
        return switch (t) {
            case QCM -> "-fx-padding: 4 10; -fx-background-color: #dbeafe; -fx-text-fill: #1e3a8a; -fx-background-radius: 999; -fx-font-weight: 700;";
            case VRAI_FAUX -> "-fx-padding: 4 10; -fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-background-radius: 999; -fx-font-weight: 700;";
            case TEXTE_LIBRE -> "-fx-padding: 4 10; -fx-background-color: #f3f4f6; -fx-text-fill: #334155; -fx-background-radius: 999; -fx-font-weight: 700;";
        };
    }

    private static String pointsLabel(float p) {
        if (Math.abs(p - Math.rint(p)) < 1e-5f) {
            return (int) p + " pt" + ((int) p == 1 ? "" : "s");
        }
        return p + " pts";
    }
}
