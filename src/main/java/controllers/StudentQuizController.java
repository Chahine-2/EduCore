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
import javafx.scene.image.ImageView;
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
import models.FraudeLog;
import services.FraudeLogDAO;
import services.FraudeLogDAOImpl;
import services.FraudeService;
import services.QuestionDAOImpl;
import services.ReponseDAOImpl;
import services.ReponseEtudiantDAOImpl;
import services.ResultatDAOImpl;
import utils.AppStageLayout;

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
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm", Locale.FRENCH);

    @FXML private BorderPane root;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label timerLabel;
    @FXML private Label timerCaptionLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private VBox questionHost;
    @FXML private ScrollPane quizScroll;
    @FXML private VBox explanationHost;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Button submitBtn;
    @FXML private Label navHintLabel;
    @FXML private ImageView webcamPreview;

    private Evaluation evaluation;
    private int studentId;
    private List<Question> questions;
    private final QuestionDAOImpl questionDAO = new QuestionDAOImpl();
    private final ReponseDAOImpl reponseDAO = new ReponseDAOImpl();
    private final ReponseEtudiantDAOImpl reponseEtudiantDAO = new ReponseEtudiantDAOImpl();
    private final ResultatDAOImpl resultatDAO = new ResultatDAOImpl();
    private final FraudeLogDAO fraudeLogDAO = new FraudeLogDAOImpl();
    private final FraudeService fraudeService = new FraudeService();

    private boolean viewMode;
    private Resultat viewResultat;

    private int currentIndex;
    private final Map<Integer, Integer> selectedReponseByQuestion = new HashMap<>();
    private final Map<Integer, String> texteLibreByQuestion = new HashMap<>();

    private Timeline countdown;
    private int remainingSeconds;
    private boolean submitted;
    private boolean fraudeDetected;
    private int activeResultatId = -1;

    private ToggleGroup activeToggleGroup;
    private TextArea activeTextArea;

    /** Normal timed attempt. */
    public void setEvaluationAndStudent(Evaluation evaluation, int studentId) {
        this.viewMode = false;
        this.viewResultat = null;
        this.evaluation = evaluation;
        this.studentId = studentId;
        this.submitted = false;
        this.fraudeDetected = false;
        this.activeResultatId = -1;
        applyLiveChrome();
        initQuestionsAndUi();
        subtitleLabel.setText(evaluation.getDescription() != null && !evaluation.getDescription().isBlank()
                ? evaluation.getDescription()
                : "Répondez à toutes les questions au mieux de vos capacités.");
        if (!questions.isEmpty()) {
            remainingSeconds = Math.max(60, evaluation.getDureeMinutes() * 60);
            if (timerCaptionLabel != null) {
                timerCaptionLabel.setText("TEMPS RESTANT");
            }
            updateTimerDisplay();
            startCountdown();
        }
        renderQuestion();
        updateNavState();
        if (webcamPreview != null) {
            boolean showCam = FraudeService.isVisionAnticheatEnabled();
            webcamPreview.setVisible(showCam);
            webcamPreview.setManaged(showCam);
        }
        Platform.runLater(this::initializeAntiCheatMonitoring);
    }

    /** Read-only review of a submitted attempt (no timer, no submit). */
    public void openInViewMode(Evaluation evaluation, int studentId, Resultat submittedResultat) {
        this.viewMode = true;
        this.viewResultat = submittedResultat;
        this.evaluation = evaluation;
        this.studentId = studentId;
        this.submitted = true;
        this.fraudeDetected = false;
        this.activeResultatId = submittedResultat != null ? submittedResultat.getId() : -1;
        applyLiveChrome();
        root.getStyleClass().remove("quiz-live");
        root.getStyleClass().add("quiz-view-mode");
        initQuestionsAndUi();
        if (submittedResultat != null) {
            loadSubmittedAnswers(submittedResultat.getId());
        }

        titleLabel.setText(evaluation.getTitre());
        String scorePart = submittedResultat.getScore() == null
                ? "Score en attente"
                : "Score: " + formatScore(submittedResultat.getScore());
        String when = submittedResultat.getDatePassage() != null
                ? "Submitted " + submittedResultat.getDatePassage().format(SUBMITTED_FMT)
                : "Submitted";
        subtitleLabel.setText("Lecture seule · " + when + " · " + scorePart);

        if (timerCaptionLabel != null) {
            timerCaptionLabel.setText("RÉVISION");
        }
        timerLabel.getStyleClass().removeAll("quiz-timer-warn", "quiz-timer-critical");
        timerLabel.setText("—");

        submitBtn.setText("Fermer");
        submitBtn.setOnAction(e -> closeQuizWindow());

        if (!questions.isEmpty()) {
            progressBar.setProgress((currentIndex + 1.0) / questions.size());
            progressLabel.setText("Question " + (currentIndex + 1) + " / " + questions.size());
        }
        renderQuestion();
        updateNavState();
        if (webcamPreview != null) {
            webcamPreview.setVisible(false);
            webcamPreview.setManaged(false);
        }
    }

    private void applyLiveChrome() {
        root.getStyleClass().removeAll("quiz-view-mode", "quiz-live");
        root.getStyleClass().add("quiz-live");
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
            Label empty = new Label("Cette évaluation n'a pas encore de questions.");
            empty.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-padding: 24;");
            questionHost.getChildren().add(empty);
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            submitBtn.setDisable(true);
            timerLabel.setText("—");
            progressBar.setProgress(0);
            progressLabel.setText("Pas de questions");
            return;
        }

        if (viewMode) {
            return;
        }

        submitBtn.setText("Soumettre le quiz");
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

        // Modal confirmation steals focus from the exam stage; keep monitoring on and you get a
        // false WINDOW_FOCUS_LOST → fraud. Pause anti-cheat for the dialog, then resume if cancelled.
        fraudeService.stop();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(root.getScene().getWindow());
        confirm.setTitle("Soumettre le quiz");
        confirm.setHeaderText("Soumettre vos réponses ?");
        confirm.setContentText("Vous ne pourrez plus modifier vos réponses après la soumission.");

        ButtonType choice = confirm.showAndWait().orElse(ButtonType.CANCEL);
        if (choice == ButtonType.OK) {
            persistSubmission(false);
        } else {
            Platform.runLater(this::initializeAntiCheatMonitoring);
        }
    }

    @FXML
    private void handleBackHome() {
        try {
            fraudeService.stop();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            Parent homeRoot = loader.load();
            Stage stage = root.getScene() != null && root.getScene().getWindow() instanceof Stage s ? s : null;
            if (stage != null) {
                stage.setScene(new Scene(homeRoot));
                stage.setTitle("EDUCORE");
                AppStageLayout.maximizeWorkArea(stage);
            }
        } catch (Exception e) {
            alertError("Impossible d'ouvrir la page d'accueil : " + e.getMessage());
        }
    }

    private void closeQuizWindow() {
        fraudeService.stop();
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
        Reponse correctResponse = findCorrectResponse(reps);
        Integer selectedResponseId = selectedReponseByQuestion.get(q.getId());
        Reponse selectedResponse = findResponseById(reps, selectedResponseId);
        boolean answeredCorrectly = viewMode && selectedResponse != null && correctResponse != null
                && selectedResponse.getId() == correctResponse.getId();
        boolean hasChosenAnswer = selectedResponseId != null;

        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.97); -fx-background-radius: 12; -fx-border-color: #dbeafe; -fx-border-radius: 12; -fx-padding: 16;");
        card.setMaxWidth(720);
        card.setMinWidth(640);
        if (viewMode) {
            card.getStyleClass().add(answeredCorrectly ? "quiz-review-correct-card"
                    : hasChosenAnswer ? "quiz-review-wrong-card" : "quiz-review-neutral-card");
        }

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

        if (viewMode) {
            Label reviewBadge = new Label(answeredCorrectly ? "Correct" : hasChosenAnswer ? "Incorrect" : "Pas de réponse");
            reviewBadge.getStyleClass().add(answeredCorrectly ? "quiz-review-badge-correct"
                    : hasChosenAnswer ? "quiz-review-badge-wrong" : "quiz-review-badge-neutral");
            HBox reviewHeader = new HBox(8, reviewBadge);
            reviewHeader.setAlignment(Pos.CENTER_LEFT);
            card.getChildren().add(reviewHeader);
        }

        VBox answers = new VBox(8);

        switch (q.getType()) {
            case TEXTE_LIBRE -> {
                if (viewMode) {
                    VBox reviewBox = buildTextReviewBox(q, correctResponse);
                    answers.getChildren().add(reviewBox);
                } else {
                    TextArea ta = new TextArea();
                    ta.getStyleClass().add("quiz-textarea");
                    ta.setWrapText(true);
                    ta.setPromptText("Tapez votre réponse ici…");
                    ta.setText(texteLibreByQuestion.getOrDefault(q.getId(), ""));
                    ta.setEditable(!viewMode);
                    ta.setFocusTraversable(!viewMode);
                    activeTextArea = ta;
                    answers.getChildren().add(ta);
                }
            }
            case QCM -> buildChoiceAnswers(q, reps, answers, true);
            case VRAI_FAUX -> {
                List<Reponse> ordered = orderVraiFauxForQuiz(reps);
                if (ordered.size() >= 2) {
                    buildChoiceAnswers(q, ordered, answers, false);
                } else {
                    Label warn = new Label(
                            "Cette question Vrai/Faux n'a pas de choix dans la base de données. Ouvrez la gestion des questions, modifiez la question et enregistrez pour que « Vrai » et « Faux » soient créés.");
                    warn.setWrapText(true);
                    warn.setStyle("-fx-text-fill: #b45309;");
                    answers.getChildren().add(warn);
                }
            }
        }

        // Move explanation out to the dedicated explanationHost (so it appears alone at the bottom)

        card.getChildren().addAll(meta, qText, answers);
        questionHost.getChildren().add(card);

        // Render the explanation in the bottom explanationHost when in view mode. Keep it hidden otherwise.
        if (explanationHost != null) {
            explanationHost.getChildren().clear();
            if (viewMode) {
                VBox panel = buildExplanationPanel(q, reps, correctResponse, selectedResponse, answeredCorrectly, hasChosenAnswer);
                explanationHost.getChildren().add(panel);
                explanationHost.setVisible(true);
                explanationHost.setManaged(true);
            } else {
                explanationHost.setVisible(false);
                explanationHost.setManaged(false);
            }
        }

        double progress = (currentIndex + 1.0) / questions.size();
        progressBar.setProgress(progress);
        progressLabel.setText("Question " + (currentIndex + 1) + " / " + questions.size());

        if (viewMode) {
            navHintLabel.setText("Vert = bonnes réponses · rouge = votre réponse incorrecte · explication affichée ci-dessous");
        } else {
            int unanswered = countUnanswered();
            navHintLabel.setText(unanswered > 0 ? unanswered + " question(s) sans réponse" : "Toutes les questions visitées");
        }
    }

    private void buildChoiceAnswers(Question q, List<Reponse> reps, VBox answers, boolean qcm) {
        if (viewMode) {
            buildReviewChoiceAnswers(q, reps, answers, qcm);
            return;
        }

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

    private void buildReviewChoiceAnswers(Question q, List<Reponse> reps, VBox answers, boolean qcm) {
        Integer selectedId = selectedReponseByQuestion.get(q.getId());
        Reponse correct = findCorrectResponse(reps);
        Reponse selected = findResponseById(reps, selectedId);

        for (int i = 0; i < reps.size(); i++) {
            Reponse r = reps.get(i);
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("quiz-review-row");

            boolean isCorrect = r.isEstCorrect();
            boolean isChosen = selected != null && selected.getId() == r.getId();
            if (isCorrect) {
                row.getStyleClass().add("quiz-review-row-correct");
            } else if (isChosen) {
                row.getStyleClass().add("quiz-review-row-wrong");
            } else {
                row.getStyleClass().add("quiz-review-row-neutral");
            }

            Label marker = new Label(String.valueOf((char) ('A' + i)));
            marker.getStyleClass().addAll("quiz-answer-tag", isCorrect ? "quiz-answer-tag-correct"
                    : isChosen ? "quiz-answer-tag-wrong" : "quiz-answer-tag-neutral");

            VBox textBox = new VBox(2);
            Label main = new Label(r.getTexte());
            main.getStyleClass().add("quiz-review-answer");
            main.setWrapText(true);
            Label sub = new Label(isCorrect ? "Bonne réponse" : isChosen ? "Votre réponse" : "Réponse possible");
            sub.getStyleClass().add(isCorrect ? "quiz-review-sub-correct"
                    : isChosen ? "quiz-review-sub-wrong" : "quiz-review-sub-neutral");
            textBox.getChildren().addAll(main, sub);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            Label status = new Label(isCorrect ? "Correct" : isChosen ? "Incorrect" : " ");
            status.getStyleClass().add(isCorrect ? "quiz-review-status-correct"
                    : isChosen ? "quiz-review-status-wrong" : "quiz-review-status-neutral");

            row.getChildren().addAll(marker, textBox, status);
            answers.getChildren().add(row);
        }

        if (reps.isEmpty()) {
            Label empty = new Label(qcm ? "No choices configured for this question." : "No choices configured.");
            empty.getStyleClass().add("quiz-hint");
            answers.getChildren().add(empty);
        }
    }

    private VBox buildTextReviewBox(Question q, Reponse correct) {
        VBox box = new VBox(10);
        box.getStyleClass().add("quiz-review-panel");

        Label heading = new Label("Votre réponse");
        heading.getStyleClass().add("quiz-review-panel-title");

        String studentAnswer = texteLibreByQuestion.getOrDefault(q.getId(), "");
        Label your = new Label(studentAnswer.isBlank() ? "Aucune réponse soumise." : studentAnswer);
        your.setWrapText(true);
        your.getStyleClass().add("quiz-review-answer-text");

        Label correctLabel = new Label("Bonne réponse : révision de réponse ouverte");
        correctLabel.getStyleClass().add("quiz-review-meta");

        box.getChildren().addAll(heading, your, correctLabel);
        return box;
    }

    private VBox buildExplanationPanel(Question q, List<Reponse> reps, Reponse correct, Reponse selected,
                                       boolean answeredCorrectly, boolean hasChosenAnswer) {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("quiz-review-panel");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Explication");
        title.getStyleClass().add("quiz-review-panel-title");
        Label badge = new Label(answeredCorrectly ? "Correct" : hasChosenAnswer ? "Incorrect" : "Pas de réponse");
        badge.getStyleClass().add(answeredCorrectly ? "quiz-review-badge-correct"
                : hasChosenAnswer ? "quiz-review-badge-wrong" : "quiz-review-badge-neutral");
        header.getChildren().addAll(title, badge);

        Label yourAnswer = new Label("Votre réponse : " + (selected != null ? selected.getTexte() : "Aucune réponse soumise."));
        yourAnswer.setWrapText(true);
        yourAnswer.getStyleClass().add("quiz-review-meta");

        Label correctAnswer = new Label("Bonne réponse : " + (correct != null ? correct.getTexte() : "Non disponible"));
        correctAnswer.setWrapText(true);
        correctAnswer.getStyleClass().add("quiz-review-meta");

        String explanation = resolveExplanation(q, reps, correct);
        Label explanationTitle = new Label("Explication Enseignant / IA");
        explanationTitle.getStyleClass().add("quiz-review-subtitle");
        Label explanationText = new Label(explanation);
        explanationText.setWrapText(true);
        explanationText.getStyleClass().add("quiz-review-explanation");

        panel.getChildren().addAll(header, yourAnswer, correctAnswer, explanationTitle, explanationText);
        return panel;
    }

    private Reponse findCorrectResponse(List<Reponse> reps) {
        if (reps == null) {
            return null;
        }
        for (Reponse r : reps) {
            if (r.isEstCorrect()) {
                return r;
            }
        }
        return null;
    }

    private Reponse findResponseById(List<Reponse> reps, Integer responseId) {
        if (reps == null || responseId == null) {
            return null;
        }
        for (Reponse r : reps) {
            if (r.getId() == responseId) {
                return r;
            }
        }
        return null;
    }

    private String resolveExplanation(Question q, List<Reponse> reps, Reponse correct) {
        if (q != null && q.getExplication() != null && !q.getExplication().isBlank()) {
            return q.getExplication().trim();
        }
        if (correct != null && correct.getExplication() != null && !correct.getExplication().isBlank()) {
            return correct.getExplication().trim();
        }
        if (reps != null) {
            for (Reponse r : reps) {
                if (r.getExplication() != null && !r.getExplication().isBlank()) {
                    return r.getExplication().trim();
                }
            }
        }
        return "Aucune explication disponible.";
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
        fraudeService.stop();
        if (countdown != null) {
            countdown.stop();
        }

        int resultatId = ensureResultatRow();
        if (resultatId <= 0) {
            alertError("Impossible d'enregistrer votre tentative. Veuillez réessayer.");
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
        done.setTitle(timeUp ? "Temps écoulé" : "Soumis");
        done.setHeaderText(timeUp ? "Le quiz s'est fermé automatiquement." : "Vos réponses ont été soumises.");
        done.setContentText("Votre tentative a été enregistrée.");
        done.showAndWait();

        closeQuizWindow();
    }

    private int ensureResultatRow() {
        if (activeResultatId > 0) {
            return activeResultatId;
        }
        LocalDateTime now = LocalDateTime.now();
        Resultat resultat = new Resultat(studentId, evaluation.getId(), null, now);
        activeResultatId = resultatDAO.insertAndGetId(resultat);
        return activeResultatId;
    }

    /**
     * Required anti-fraud entry point:
     * - save fraud in DB
     * - show warning
     * - auto submit immediately
     * <p>
     * Phone policy: {@code PHONE_IN_FRAME} is only raised after the vision pipeline accumulates
     * enough suspicion (consecutive detections + cooldown + score threshold in {@link FraudeService}),
     * not on a single SSD frame.
     */
    private void detectFraude(String type) {
        detectFraude(type, "Comportement suspect détecté par le module anti-triche.");
    }

    private void detectFraude(String type, String description) {
        if (viewMode || submitted || fraudeDetected) {
            return;
        }
        fraudeDetected = true;
        int resultatId = ensureResultatRow();
        if (resultatId > 0) {
            // Mark the resultat as fraudulent
            Resultat resultat = resultatDAO.getById(resultatId);
            if (resultat != null) {
                resultat.setFraudeDetecte(true);
                resultatDAO.update(resultat);
            }

            // Log the fraud details
            fraudeLogDAO.logFraude(new FraudeLog(
                    resultatId,
                    studentId,
                    type,
                    description
            ));
        }

        String reason = (description != null && !description.isBlank())
                ? description.trim()
                : "No additional detail was recorded for this event.";
        String category = fraudTypeLabel(type);
        String body = "Category: " + category + "\n\n"
                + "Reason:\n" + reason + "\n\n"
                + "Your quiz will be submitted immediately.";

        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.initOwner(root.getScene().getWindow());
        warning.setTitle("Fraud detected");
        warning.setHeaderText("Anti-cheat: " + category);
        warning.setContentText(body);
        warning.showAndWait();

        submitEvaluation();
    }

    private void submitEvaluation() {
        persistSubmission(false);
    }

    private void initializeAntiCheatMonitoring() {
        if (viewMode || submitted || root.getScene() == null || !(root.getScene().getWindow() instanceof Stage stage)) {
            return;
        }
        fraudeService.initialize(stage, (type, description) -> {
            switch (type) {
                case "WINDOW_FOCUS_LOST" -> detectFraude("WINDOW_FOCUS_LOST", description);
                case "FULLSCREEN_EXIT" -> detectFraude("FULLSCREEN_EXIT", description);
                case "CAMERA_OFF" -> detectFraude("CAMERA_OFF", description);
                case "CAMERA_BLOCKED" -> detectFraude("CAMERA_BLOCKED", description);
                case "NO_FACE_DETECTED" -> detectFraude("NO_FACE_DETECTED", description);
                case "MULTIPLE_FACES" -> detectFraude("MULTIPLE_FACES", description);
                case "MOTION_ANOMALY_NO_FACE" -> detectFraude("MOTION_ANOMALY_NO_FACE", description);
                case "OBJECT_NEAR_FACE" -> detectFraude("OBJECT_NEAR_FACE", description);
                case "EXCESSIVE_HEAD_TURNS" -> detectFraude("EXCESSIVE_HEAD_TURNS", description);
                case "PHONE_IN_FRAME" -> detectFraude("PHONE_IN_FRAME", description);
                default -> detectFraude(type, description);
            }
        }, webcamPreview);
    }

    private static String fraudTypeLabel(String type) {
        if (type == null || type.isBlank()) {
            return "Unknown";
        }
        return switch (type) {
            case "WINDOW_FOCUS_LOST" -> "Exam window lost focus";
            case "FULLSCREEN_EXIT" -> "Fullscreen was turned off";
            case "CAMERA_OFF" -> "Camera disconnected or off";
            case "CAMERA_BLOCKED" -> "Camera blocked or feed lost";
            case "NO_FACE_DETECTED" -> "No face detected";
            case "MULTIPLE_FACES" -> "Multiple faces detected";
            case "MOTION_ANOMALY_NO_FACE" -> "Movement with no face visible";
            case "OBJECT_NEAR_FACE" -> "Object movement near face";
            case "EXCESSIVE_HEAD_TURNS" -> "Too many head turns away from camera";
            case "PHONE_IN_FRAME" -> "Phone or device visible in frame";
            default -> type.replace('_', ' ');
        };
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
