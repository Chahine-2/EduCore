package controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.AIQuestion;
import models.Question;
import models.Reponse;
import models.QuestionType;
import services.QuizGenerationService;
import services.QuestionDAOImpl;
import services.ReponseDAOImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller for the AI Quiz Generator popup.
 * - Shows prompt inputs
 * - Calls QuizGenerationService
 * - Displays editable cards for each generated question
 * - Saves to DB using existing DAO implementations
 */
public class QuizGeneratorController {

    private static final String CARD_KEY_QUESTION = "qm.questionField";
    private static final String CARD_KEY_CHOICES = "qm.choiceFields";
    private static final String CARD_KEY_GROUP = "qm.toggleGroup";
    private static final String CARD_KEY_EXPLANATION = "qm.explanationField";

    @FXML private TextField tfTopic;
    @FXML private ComboBox<String> cbDifficulty;
    @FXML private Spinner<Integer> spCount;
    @FXML private Button btnGenerate;
    @FXML private ProgressIndicator progress;
    @FXML private VBox vboxResults;
    @FXML private Button btnSaveAll;
    @FXML private Label emptyResultsLabel;

    private final QuizGenerationService generationService = new QuizGenerationService();
    // If non-null, saved questions will be attached to this evaluation
    private models.Evaluation targetEvaluation;

    @FXML
    public void initialize() {
        cbDifficulty.getItems().addAll("Easy", "Medium", "Hard");
        cbDifficulty.getSelectionModel().select("Medium");
        spCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 5));
        progress.setVisible(false);
        tfTopic.getStyleClass().addAll("field-inline", "qm-dialog-field");
        cbDifficulty.getStyleClass().addAll("combo-toolbar", "qm-dialog-combo");
        spCount.getStyleClass().addAll("time-spinner", "qm-dialog-field");
        btnGenerate.getStyleClass().add("btn-primary");
        btnSaveAll.getStyleClass().add("btn-muted");
        vboxResults.getStyleClass().add("qm-results-list");
        progress.getStyleClass().add("qm-progress");
        setEmptyState("No AI questions generated yet. Choose a topic and click Generate.", true);
    }

    @FXML
    void onGenerate() {
        String topic = tfTopic.getText().trim();
        String difficulty = cbDifficulty.getValue();
        int count = spCount.getValue();

        if (topic.isEmpty()) {
            showAlert("Veuillez entrer un sujet.", Alert.AlertType.WARNING);
            return;
        }

        progress.setVisible(true);
        btnGenerate.setDisable(true);
        vboxResults.getChildren().clear();
        setEmptyState(null, false);

        Task<List<AIQuestion>> task = new Task<>() {
            @Override
            protected List<AIQuestion> call() throws Exception {
                return generationService.generate(topic, difficulty, count);
            }
        };

        task.setOnSucceeded(ev -> {
            progress.setVisible(false);
            btnGenerate.setDisable(false);
            List<AIQuestion> list = task.getValue();
            if (list == null || list.isEmpty()) {
                setEmptyState("Gemini did not return any usable questions. Try a different topic or regenerate.", true);
                showAlert("La génération n'a retourné aucune question.", Alert.AlertType.INFORMATION);
                return;
            }
            for (AIQuestion aiq : list) {
                vboxResults.getChildren().add(createCardFor(aiq));
            }
            setEmptyState(null, false);
        });

        task.setOnFailed(ev -> {
            progress.setVisible(false);
            btnGenerate.setDisable(false);
            Throwable ex = task.getException();
            setEmptyState("Generation failed. Check the API key, endpoint, and the error message below.", true);
            showAlert("Erreur durant la génération: " + (ex != null ? ex.getMessage() : "unknown"), Alert.AlertType.ERROR);
        });

        new Thread(task, "AI-Gen-Thread").start();
    }

    private VBox createCardFor(AIQuestion aiq) {
        VBox root = new VBox(14);
        root.getStyleClass().add("question-card");

        HBox header = new HBox(10);
        header.getStyleClass().add("question-card-heading");
        Label title = new Label("Generated question");
        title.getStyleClass().add("question-card-title");
        Label typeBadge = new Label("MCQ");
        typeBadge.getStyleClass().addAll("badge-type", "badge-qcm");
        Label infoBadge = new Label("4 choices");
        infoBadge.getStyleClass().addAll("badge-type", "badge-points");
        Label difficultyBadge = new Label(Objects.toString(cbDifficulty.getValue(), "Medium"));
        difficultyBadge.getStyleClass().addAll("badge-type", "badge-texte");
        HBox headerLeft = new HBox(8, title, typeBadge, infoBadge, difficultyBadge);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label status = new Label("Editable before save");
        status.getStyleClass().add("question-empty-hint");
        header.getChildren().addAll(headerLeft, spacer, status);

        Label qLabel = new Label("Question statement");
        qLabel.getStyleClass().add("question-section-title");
        TextArea taQuestion = new TextArea(aiq.getQuestion() == null ? "" : aiq.getQuestion());
        taQuestion.getStyleClass().addAll("field-inline", "field-textarea", "qm-dialog-textarea");
        taQuestion.setWrapText(true);
        taQuestion.setPrefRowCount(3);

        Label choicesLabel = new Label("Answer choices");
        choicesLabel.getStyleClass().add("question-section-title");

        List<TextField> choiceFields = new ArrayList<>();
        ToggleGroup tg = new ToggleGroup();
        GridPane choicesGrid = new GridPane();
        choicesGrid.getStyleClass().add("answer-grid");
        choicesGrid.setHgap(12);
        choicesGrid.setVgap(12);

        for (int i = 0; i < 4; i++) {
            VBox col = new VBox(6);
            col.getStyleClass().add("answer-option-card");
            col.setMaxWidth(Double.MAX_VALUE);
            Label letter = new Label(String.valueOf((char) ('A' + i)));
            letter.getStyleClass().addAll("badge-type", "badge-texte");
            RadioButton rb = new RadioButton();
            rb.setToggleGroup(tg);
            rb.getStyleClass().add("qm-radio");

            if (aiq.getCorrectAnswer() != null && aiq.getChoices() != null) {
                String expected = aiq.getCorrectAnswer().trim();
                if (expected.equalsIgnoreCase("ABCD".substring(i, i + 1)) || (aiq.getChoices().size() > i && aiq.getChoices().get(i).equalsIgnoreCase(expected))) {
                    rb.setSelected(true);
                }
            }

            TextField tf = new TextField();
            tf.getStyleClass().addAll("field-inline", "qm-dialog-field");
            if (aiq.getChoices() != null && aiq.getChoices().size() > i) {
                tf.setText(aiq.getChoices().get(i));
            }
            choiceFields.add(tf);
            col.getChildren().addAll(letter, rb, tf);

            int row = i / 2;
            int colIndex = i % 2;
            choicesGrid.add(col, colIndex, row);
            GridPane.setHgrow(col, javafx.scene.layout.Priority.ALWAYS);
        }

        Label explanationLabel = new Label("Explanation");
        explanationLabel.getStyleClass().add("question-section-title");
        TextArea taExplanation = new TextArea(aiq.getExplanation() == null ? "" : aiq.getExplanation());
        taExplanation.getStyleClass().addAll("field-inline", "field-textarea", "qm-dialog-textarea");
        taExplanation.setWrapText(true);
        taExplanation.setPrefRowCount(3);

        HBox actions = new HBox(10);
        actions.getStyleClass().add("q-actions");
        Button btnSave = new Button("Save");
        btnSave.getStyleClass().add("btn-primary");
        Button btnRegenerate = new Button("Regenerate");
        btnRegenerate.getStyleClass().add("btn-muted");
        actions.getChildren().addAll(btnSave, btnRegenerate);

        btnSave.setOnAction(e -> {
            try {
                saveCard(taQuestion, choiceFields, tg, taExplanation);
                showAlert("Question saved.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Save failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });

        btnRegenerate.setOnAction(e -> {
            // simple re-run generation for single card topic using same topic/difficulty
            onGenerate();
        });

        root.getChildren().addAll(header, qLabel, taQuestion, choicesLabel, choicesGrid, explanationLabel, taExplanation, actions);

        root.getProperties().put(CARD_KEY_QUESTION, taQuestion);
        root.getProperties().put(CARD_KEY_CHOICES, choiceFields);
        root.getProperties().put(CARD_KEY_GROUP, tg);
        root.getProperties().put(CARD_KEY_EXPLANATION, taExplanation);

        return root;
    }

    private void saveCard(TextArea taQuestion, List<TextField> choices, ToggleGroup tg, TextArea explanation) {
        String texte = taQuestion.getText().trim();
        if (texte.isEmpty()) throw new IllegalArgumentException("Question vide");

        int selectedIndex = -1;
        for (int i = 0; i < tg.getToggles().size(); i++) {
            if (tg.getToggles().get(i) instanceof RadioButton) {
                RadioButton rb = (RadioButton) tg.getToggles().get(i);
                if (rb.isSelected()) { selectedIndex = i; break; }
            }
        }

        if (selectedIndex < 0) throw new IllegalArgumentException("Veuillez sélectionner la bonne réponse.");

        // Insert question into DB. If generator was opened for an Evaluation, distribute the
        // evaluation's max score equally across all generated questions; otherwise default to 1.0.
        Question q = new Question();
        q.setTexte(texte);
        q.setType(QuestionType.QCM);
        float pointsPerQuestion = 1.0f;
        try {
            int totalCards = vboxResults != null ? vboxResults.getChildren().size() : 0;
            if (targetEvaluation != null && totalCards > 0) {
                pointsPerQuestion = targetEvaluation.getNoteMax() / (float) totalCards;
            }
        } catch (Exception ex) {
            pointsPerQuestion = 1.0f;
        }
        q.setPoints(pointsPerQuestion);
        q.setExplication(explanation.getText() != null ? explanation.getText().trim() : null);
        q.setEvaluationId(targetEvaluation != null ? targetEvaluation.getId() : 0); // attach if provided

        QuestionDAOImpl qdao = new QuestionDAOImpl();
        int qid = qdao.insertAndGetId(q);
        if (qid <= 0) throw new RuntimeException("Failed to insert question into DB");

        ReponseDAOImpl rdao = new ReponseDAOImpl();
        for (int i = 0; i < choices.size(); i++) {
            String txt = choices.get(i).getText().trim();
            if (txt.isEmpty()) continue;
            Reponse r = new Reponse();
            r.setTexte(txt);
            r.setEstCorrect(i == selectedIndex);
            r.setQuestionId(qid);
            rdao.add(r);
        }
    }

    @FXML
    void onSaveAll() {
        // iterate through cards and save each; for brevity just invoke save action on each child
        try {
            for (javafx.scene.Node node : vboxResults.getChildren()) {
                if (node instanceof VBox root) {
                    TextArea taQuestion = (TextArea) root.getProperties().get(CARD_KEY_QUESTION);
                    @SuppressWarnings("unchecked")
                    List<TextField> fields = (List<TextField>) root.getProperties().get(CARD_KEY_CHOICES);
                    ToggleGroup tg = (ToggleGroup) root.getProperties().get(CARD_KEY_GROUP);
                    TextArea taExplanation = (TextArea) root.getProperties().get(CARD_KEY_EXPLANATION);
                    if (taQuestion != null && fields != null && tg != null && taExplanation != null) {
                        saveCard(taQuestion, fields, tg, taExplanation);
                    }
                }
            }
            showAlert("Toutes les questions ont été sauvegardées.", Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            showAlert("Erreur lors de la sauvegarde: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setEmptyState(String message, boolean visible) {
        if (emptyResultsLabel == null) {
            return;
        }
        if (message != null) {
            emptyResultsLabel.setText(message);
        }
        emptyResultsLabel.setVisible(visible);
        emptyResultsLabel.setManaged(visible);
    }

    private void showAlert(String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert a = new Alert(type, message, ButtonType.OK);
            a.initModality(Modality.APPLICATION_MODAL);
            a.showAndWait();
        });
    }

    /** Convenience method to show the generator as a modal dialog from other controllers */
    public static void openModal() {
        try {
            FXMLLoader loader = new FXMLLoader(QuizGeneratorController.class.getResource("/views/QuizGenerator.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Générateur de quiz AI");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le générateur: " + e.getMessage());
            a.showAndWait();
        }
    }

    /** Open the generator and attach generated questions to a specific evaluation. */
    public static void openModalForEvaluation(models.Evaluation evaluation) {
        try {
            FXMLLoader loader = new FXMLLoader(QuizGeneratorController.class.getResource("/views/QuizGenerator.fxml"));
            Parent root = loader.load();
            QuizGeneratorController controller = loader.getController();
            controller.targetEvaluation = evaluation;

            Stage stage = new Stage();
            stage.setTitle("Générateur de quiz AI — " + (evaluation != null ? evaluation.getTitre() : ""));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le générateur: " + e.getMessage());
            a.showAndWait();
        }
    }
}


