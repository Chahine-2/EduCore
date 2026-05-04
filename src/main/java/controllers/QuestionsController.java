package controllers;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.util.StringConverter;
import models.Evaluation;
import models.Question;
import models.QuestionType;
import models.Reponse;
import services.QuestionDAOImpl;
import services.ReponseDAOImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Questions Management UI for one evaluation.
 */
public class QuestionsController {

    private static final String VF_LABEL_VRAI = "Vrai";
    private static final String VF_LABEL_FAUX = "Faux";

    @FXML private BorderPane root;
    @FXML private Label evaluationTitleLabel;
    @FXML private Label questionCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label emptyLabel;
    @FXML private VBox questionsContainer;

    private Evaluation evaluation;

    private final QuestionDAOImpl questionDAO = new QuestionDAOImpl();
    private final ReponseDAOImpl reponseDAO = new ReponseDAOImpl();

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
        evaluationTitleLabel.setText(evaluation.getTitre());
        refreshQuestions();
    }

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleClose() {
        var w = root.getScene() != null ? root.getScene().getWindow() : null;
        if (w instanceof javafx.stage.Stage stage) {
            stage.close();
        }
    }

    @FXML
    private void handleAddQuestion() {
        showQuestionEditor(null);
    }

    private void refreshQuestions() {
        if (evaluation == null) {
            return;
        }
        questionsContainer.setOpacity(0);
        questionsContainer.getChildren().clear();

        List<Question> questions = questionDAO.findByEvaluationId(evaluation.getId());
        questionCountLabel.setText(questions.size() + (questions.size() == 1 ? " question" : " questions"));
        emptyLabel.setVisible(questions.isEmpty());
        emptyLabel.setManaged(questions.isEmpty());

        for (Question q : questions) {
            List<Reponse> reps = (q.getType() == QuestionType.QCM || q.getType() == QuestionType.VRAI_FAUX)
                    ? reponseDAO.findByQuestionId(q.getId())
                    : List.of();
            questionsContainer.getChildren().add(buildQuestionCard(q, reps));
        }

        FadeTransition ft = new FadeTransition(Duration.millis(260), questionsContainer);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        setFooterStatus("Ready", false);
    }

    private TitledPane buildQuestionCard(Question q, List<Reponse> reponses) {
        String titlePreview = q.getTexte() == null ? "(empty)" : q.getTexte();
        if (titlePreview.length() > 72) {
            titlePreview = titlePreview.substring(0, 69) + "…";
        }
        TitledPane pane = new TitledPane();
        pane.setText(titlePreview + "  ·  " + formatPoints(q.getPoints()) + " pts");
        pane.getStyleClass().add("question-titled-pane");
        pane.setExpanded(true);

        VBox body = new VBox(12);
        body.setPadding(new Insets(0));

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getStyleClass().add("q-meta-row");
        meta.getChildren().addAll(
                typeBadge(q.getType()),
                pointsBadge(q.getPoints())
        );
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        meta.getChildren().add(spacer);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getStyleClass().add("q-actions");

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().addAll("btn-row", "btn-row-edit");
        editBtn.setOnAction(e -> showQuestionEditor(q));

        Button delBtn = new Button("Delete");
        delBtn.getStyleClass().addAll("btn-row", "btn-row-delete");
        delBtn.setOnAction(e -> deleteQuestion(q));

        actions.getChildren().addAll(editBtn, delBtn);

        if (q.getType() == QuestionType.QCM) {
            Button addAnsBtn = new Button("+ Answer");
            addAnsBtn.getStyleClass().addAll("btn-row", "btn-row-view");
            addAnsBtn.setOnAction(e -> showQuickAddAnswer(q));
            actions.getChildren().add(addAnsBtn);
        }

        meta.getChildren().add(actions);

        Label fullText = new Label(q.getTexte());
        fullText.getStyleClass().add("q-body-text");
        fullText.setWrapText(true);
        fullText.setMaxWidth(Double.MAX_VALUE);

        body.getChildren().addAll(meta, fullText);

        if (q.getType() == QuestionType.QCM) {
            Label sec = new Label("Answer choices");
            sec.getStyleClass().add("q-section-label");
            VBox list = new VBox(8);
            if (reponses.isEmpty()) {
                Label emptyAns = new Label("No answers yet. Use Edit or + Answer to add options.");
                emptyAns.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
                list.getChildren().add(emptyAns);
            }
            for (Reponse r : reponses) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("answer-row");
                if (r.isEstCorrect()) {
                    row.getStyleClass().add("answer-row-correct");
                }
                Label txt = new Label(r.getTexte());
                txt.getStyleClass().add("answer-text");
                txt.setWrapText(true);
                HBox.setHgrow(txt, Priority.ALWAYS);
                row.getChildren().add(txt);
                if (r.isEstCorrect()) {
                    Label ok = new Label("Correct");
                    ok.getStyleClass().add("badge-correct");
                    row.getChildren().add(ok);
                }
                list.getChildren().add(row);
            }
            body.getChildren().addAll(sec, list);
        } else if (q.getType() == QuestionType.VRAI_FAUX) {
            Label sec = new Label("Vrai / Faux");
            sec.getStyleClass().add("q-section-label");
            VBox list = new VBox(8);
            if (reponses.isEmpty() || reponses.size() < 2) {
                Label emptyAns = new Label("Open Edit and save to create the “Vrai” and “Faux” options and mark the correct one.");
                emptyAns.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
                emptyAns.setWrapText(true);
                list.getChildren().add(emptyAns);
            }
            for (Reponse r : orderVraiFaux(reponses)) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("answer-row");
                if (r.isEstCorrect()) {
                    row.getStyleClass().add("answer-row-correct");
                }
                Label txt = new Label(r.getTexte());
                txt.getStyleClass().add("answer-text");
                txt.setWrapText(true);
                HBox.setHgrow(txt, Priority.ALWAYS);
                row.getChildren().add(txt);
                if (r.isEstCorrect()) {
                    Label ok = new Label("Correct");
                    ok.getStyleClass().add("badge-correct");
                    row.getChildren().add(ok);
                }
                list.getChildren().add(row);
            }
            body.getChildren().addAll(sec, list);
        }

        pane.setContent(body);
        return pane;
    }

    private static Label typeBadge(QuestionType t) {
        Label l = new Label(typeDisplay(t));
        l.getStyleClass().add("badge-type");
        switch (t) {
            case QCM -> l.getStyleClass().add("badge-qcm");
            case VRAI_FAUX -> l.getStyleClass().add("badge-vf");
            case TEXTE_LIBRE -> l.getStyleClass().add("badge-texte");
        }
        return l;
    }

    private static Label pointsBadge(float p) {
        Label l = new Label(formatPoints(p) + " pts");
        l.getStyleClass().add("badge-points");
        return l;
    }

    private static String formatPoints(float p) {
        if (Math.abs(p - Math.rint(p)) < 1e-6) {
            return String.valueOf((int) p);
        }
        return String.valueOf(p);
    }

    private static String typeDisplay(QuestionType t) {
        return switch (t) {
            case QCM -> "QCM";
            case VRAI_FAUX -> "Vrai / Faux";
            case TEXTE_LIBRE -> "Texte libre";
        };
    }

    /** Puts Vrai before Faux for a stable list order in the UI. */
    private static List<Reponse> orderVraiFaux(List<Reponse> reps) {
        if (reps == null || reps.isEmpty()) {
            return List.of();
        }
        List<Reponse> copy = new ArrayList<>(reps);
        copy.sort((a, b) -> {
            int oa = orderKey(a.getTexte());
            int ob = orderKey(b.getTexte());
            return Integer.compare(oa, ob);
        });
        return copy;
    }

    private static int orderKey(String texte) {
        if (texte == null) {
            return 2;
        }
        String t = texte.trim();
        if (t.equalsIgnoreCase(VF_LABEL_VRAI)) {
            return 0;
        }
        if (t.equalsIgnoreCase(VF_LABEL_FAUX)) {
            return 1;
        }
        return 2;
    }

    private void deleteQuestion(Question q) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(root.getScene().getWindow());
        alert.setTitle("Delete question");
        alert.setHeaderText("Remove this question?");
        alert.setContentText(q.getTexte() != null && q.getTexte().length() > 120
                ? q.getTexte().substring(0, 117) + "…" : q.getTexte());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            reponseDAO.deleteByQuestionId(q.getId());
            questionDAO.delete(q.getId());
            refreshQuestions();
            setFooterStatus("Question deleted.", true);
        }
    }

    private void showQuickAddAnswer(Question q) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add answer option");
        dialog.initOwner(root.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);

        TextField textField = new TextField();
        textField.setPromptText("Answer text");
        textField.getStyleClass().add("field-inline");

        CheckBox correct = new CheckBox("Mark as correct");

        VBox vbox = new VBox(12,
                labeledRow("Text", textField),
                correct);
        dialog.getDialogPane().setContent(wrapDialogContent(vbox));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (textField.getText() == null || textField.getText().trim().isEmpty()) {
                ev.consume();
                showAlert(Alert.AlertType.ERROR, "Answer text is required.");
            }
        });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            reponseDAO.add(new Reponse(textField.getText().trim(), correct.isSelected(), q.getId()));
            refreshQuestions();
            setFooterStatus("Answer added.", true);
        }
    }

    private void showQuestionEditor(Question existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "New question" : "Edit question");
        dialog.initOwner(root.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);

        TextArea textArea = new TextArea(existing != null ? existing.getTexte() : "");
        textArea.setWrapText(true);
        textArea.setPromptText("Enter the question wording…");
        textArea.getStyleClass().add("field-inline");
        textArea.setPrefRowCount(5);

        ComboBox<QuestionType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(QuestionType.values()));
        typeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(QuestionType object) {
                return object == null ? "" : typeDisplay(object);
            }

            @Override
            public QuestionType fromString(String string) {
                for (QuestionType t : QuestionType.values()) {
                    if (typeDisplay(t).equals(string)) {
                        return t;
                    }
                }
                return QuestionType.QCM;
            }
        });
        typeCombo.setValue(existing != null ? existing.getType() : QuestionType.QCM);

        TextField pointsField = new TextField(existing != null ? formatPoints(existing.getPoints()) : "1");
        pointsField.getStyleClass().add("field-inline");

        VBox answerSection = new VBox(10);
        Label ansTitle = new Label("Answer options (QCM only)");
        ansTitle.getStyleClass().add("q-section-label");
        VBox answerList = new VBox(6);
        List<AnswerRow> answerRows = new ArrayList<>();

        ToggleGroup vfGroup = new ToggleGroup();
        RadioButton rbVrai = new RadioButton(VF_LABEL_VRAI);
        RadioButton rbFaux = new RadioButton(VF_LABEL_FAUX);
        rbVrai.setToggleGroup(vfGroup);
        rbFaux.setToggleGroup(vfGroup);
        HBox vfRow = new HBox(20, rbVrai, rbFaux);
        vfRow.setAlignment(Pos.CENTER_LEFT);
        VBox vfSection = new VBox(8);
        Label vfTitle = new Label("Correct answer (Vrai / Faux)");
        vfTitle.getStyleClass().add("q-section-label");
        Label vfHint = new Label("Same pattern as QCM: two fixed choices (Vrai and Faux) are stored; choose which one is correct.");
        vfHint.setWrapText(true);
        vfHint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        vfSection.getChildren().addAll(vfTitle, vfRow, vfHint);

        Runnable refillForQcm = () -> {
            answerList.getChildren().clear();
            answerRows.clear();
            if (existing != null && existing.getType() == QuestionType.QCM) {
                List<Reponse> reps = reponseDAO.findByQuestionId(existing.getId());
                if (reps.isEmpty()) {
                    addAnswerRow(answerList, answerRows, "", false);
                    addAnswerRow(answerList, answerRows, "", false);
                } else {
                    for (Reponse r : reps) {
                        addAnswerRow(answerList, answerRows, r.getTexte(), r.isEstCorrect());
                    }
                }
            } else {
                addAnswerRow(answerList, answerRows, "", false);
                addAnswerRow(answerList, answerRows, "", false);
            }
        };

        Runnable refillForVf = () -> {
            vfGroup.selectToggle(null);
            if (existing != null && existing.getType() == QuestionType.VRAI_FAUX) {
                List<Reponse> reps = reponseDAO.findByQuestionId(existing.getId());
                Reponse rVrai = findReponseByLabel(reps, VF_LABEL_VRAI);
                Reponse rFaux = findReponseByLabel(reps, VF_LABEL_FAUX);
                if (rVrai != null && rVrai.isEstCorrect()) {
                    vfGroup.selectToggle(rbVrai);
                } else if (rFaux != null && rFaux.isEstCorrect()) {
                    vfGroup.selectToggle(rbFaux);
                } else {
                    vfGroup.selectToggle(rbVrai);
                }
            } else {
                vfGroup.selectToggle(rbVrai);
            }
        };

        Button addOptionBtn = new Button("+ Add option");
        addOptionBtn.getStyleClass().add("btn-q-ghost");
        addOptionBtn.setOnAction(e -> addAnswerRow(answerList, answerRows, "", false));

        answerSection.getChildren().addAll(ansTitle, answerList, addOptionBtn);

        Runnable updateAnswerVisibility = () -> {
            boolean qcm = typeCombo.getValue() == QuestionType.QCM;
            boolean vf = typeCombo.getValue() == QuestionType.VRAI_FAUX;
            answerSection.setManaged(qcm);
            answerSection.setVisible(qcm);
            vfSection.setManaged(vf);
            vfSection.setVisible(vf);
            if (qcm) {
                refillForQcm.run();
            }
            if (vf) {
                refillForVf.run();
            }
        };

        typeCombo.valueProperty().addListener((o, a, b) -> updateAnswerVisibility.run());
        updateAnswerVisibility.run();

        StackPane answerStack = new StackPane();
        answerStack.getChildren().addAll(answerSection, vfSection);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.add(new Label("Question"), 0, 0);
        grid.add(textArea, 1, 0);
        grid.add(new Label("Type"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Points"), 0, 2);
        grid.add(pointsField, 1, 2);
        grid.add(answerStack, 0, 3, 2, 1);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(18);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(620);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Save");
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (!validateQuestionForm(textArea, pointsField, typeCombo.getValue(), answerRows, vfGroup)) {
                ev.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            persistQuestion(existing, textArea.getText(), typeCombo.getValue(), parsePoints(pointsField.getText()),
                    answerRows, vfGroup, rbVrai);
            refreshQuestions();
            setFooterStatus(existing == null ? "Question created." : "Question updated.", true);
        }
    }

    private static Reponse findReponseByLabel(List<Reponse> reps, String label) {
        if (reps == null) {
            return null;
        }
        for (Reponse r : reps) {
            if (r.getTexte() != null && r.getTexte().trim().equalsIgnoreCase(label)) {
                return r;
            }
        }
        return null;
    }

    private void addAnswerRow(VBox answerList, List<AnswerRow> models, String initialText, boolean correct) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        TextField tf = new TextField(initialText);
        tf.getStyleClass().add("field-inline");
        HBox.setHgrow(tf, Priority.ALWAYS);
        CheckBox cb = new CheckBox("Correct");
        cb.setSelected(correct);
        Button remove = new Button("Remove");
        remove.getStyleClass().add("btn-q-ghost");
        AnswerRow model = new AnswerRow(tf, cb);
        models.add(model);
        remove.setOnAction(e -> {
            models.remove(model);
            answerList.getChildren().remove(row);
        });
        row.getChildren().addAll(tf, cb, remove);
        answerList.getChildren().add(row);
    }

    private boolean validateQuestionForm(TextArea textArea, TextField pointsField, QuestionType type,
                                         List<AnswerRow> answerRows, ToggleGroup vfGroup) {
        if (textArea.getText() == null || textArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Question text is required.");
            return false;
        }
        try {
            float p = Float.parseFloat(pointsField.getText().trim());
            if (p <= 0) {
                showAlert(Alert.AlertType.ERROR, "Points must be greater than zero.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Points must be a valid number.");
            return false;
        }
        if (type == QuestionType.QCM) {
            int filled = 0;
            int correctCount = 0;
            for (AnswerRow row : answerRows) {
                String t = row.textField.getText() != null ? row.textField.getText().trim() : "";
                if (!t.isEmpty()) {
                    filled++;
                    if (row.correct.isSelected()) {
                        correctCount++;
                    }
                }
            }
            if (filled < 2) {
                showAlert(Alert.AlertType.ERROR, "QCM requires at least two non-empty answer options.");
                return false;
            }
            if (correctCount < 1) {
                showAlert(Alert.AlertType.ERROR, "Mark at least one answer as correct.");
                return false;
            }
        }
        if (type == QuestionType.VRAI_FAUX && (vfGroup == null || vfGroup.getSelectedToggle() == null)) {
            showAlert(Alert.AlertType.ERROR, "Select whether Vrai or Faux is the correct answer.");
            return false;
        }
        return true;
    }

    private float parsePoints(String s) {
        return Float.parseFloat(s.trim());
    }

    private void persistQuestion(Question existing, String texte, QuestionType type, float points, List<AnswerRow> rows,
                                 ToggleGroup vfGroup, RadioButton rbVrai) {
        if (existing == null) {
            Question q = new Question(texte.trim(), type, points, evaluation.getId());
            if (type == QuestionType.QCM) {
                int id = questionDAO.insertAndGetId(q);
                if (id <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Could not save the question.");
                    return;
                }
                for (AnswerRow row : rows) {
                    String t = row.textField.getText() != null ? row.textField.getText().trim() : "";
                    if (t.isEmpty()) {
                        continue;
                    }
                    reponseDAO.add(new Reponse(t, row.correct.isSelected(), id));
                }
            } else if (type == QuestionType.VRAI_FAUX) {
                int id = questionDAO.insertAndGetId(q);
                if (id <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Could not save the question.");
                    return;
                }
                addVraiFauxReponses(id, vfGroup, rbVrai);
            } else {
                questionDAO.add(q);
            }
        } else {
            Question updated = new Question(existing.getId(), texte.trim(), type, points, evaluation.getId());
            questionDAO.update(updated);
            reponseDAO.deleteByQuestionId(existing.getId());
            if (type == QuestionType.QCM) {
                for (AnswerRow row : rows) {
                    String t = row.textField.getText() != null ? row.textField.getText().trim() : "";
                    if (t.isEmpty()) {
                        continue;
                    }
                    reponseDAO.add(new Reponse(t, row.correct.isSelected(), existing.getId()));
                }
            } else if (type == QuestionType.VRAI_FAUX) {
                addVraiFauxReponses(existing.getId(), vfGroup, rbVrai);
            }
        }
    }

    private void addVraiFauxReponses(int questionId, ToggleGroup vfGroup, RadioButton rbVrai) {
        boolean vraiCorrect = vfGroup.getSelectedToggle() == rbVrai;
        reponseDAO.add(new Reponse(VF_LABEL_VRAI, vraiCorrect, questionId));
        reponseDAO.add(new Reponse(VF_LABEL_FAUX, !vraiCorrect, questionId));
    }

    private static VBox wrapDialogContent(VBox inner) {
        VBox v = new VBox(inner);
        v.setPadding(new Insets(6, 0, 0, 0));
        return v;
    }

    private static HBox labeledRow(String label, javafx.scene.Node field) {
        Label l = new Label(label);
        l.setMinWidth(72);
        HBox h = new HBox(12, l, field);
        h.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return h;
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.initOwner(root.getScene().getWindow());
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void setFooterStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("footer-status", "footer-status-ok", "footer-status-neutral", "footer-status-error");
        statusLabel.getStyleClass().add("footer-status");
        statusLabel.getStyleClass().add(ok ? "footer-status-ok" : "footer-status-neutral");
    }

    private static final class AnswerRow {
        final TextField textField;
        final CheckBox correct;

        AnswerRow(TextField textField, CheckBox correct) {
            this.textField = textField;
            this.correct = correct;
        }
    }
}
