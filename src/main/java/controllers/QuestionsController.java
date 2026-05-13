package controllers;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
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
    @FXML private Button addQuestionBtn;

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
    private void handleAddQuestion() {
        showQuestionEditor(null);
    }

    private void refreshQuestions() {
        if (evaluation == null) {
            return;
        }
        questionsContainer.setOpacity(0);
        questionsContainer.getChildren().clear();

        List<Question> allQuestions = questionDAO.findByEvaluationId(evaluation.getId());
        List<Question> questions = allQuestions.stream()
                .filter(q -> q.getType() == QuestionType.QCM || q.getType() == QuestionType.VRAI_FAUX)
                .toList();
        questionCountLabel.setText(questions.size() + (questions.size() == 1 ? " question" : " questions"));
        emptyLabel.setVisible(questions.isEmpty());
        emptyLabel.setManaged(questions.isEmpty());

        for (Question q : questions) {
            List<Reponse> reps = (q.getType() == QuestionType.QCM || q.getType() == QuestionType.VRAI_FAUX)
                    ? reponseDAO.findByQuestionId(q.getId())
                    : List.of();
            questionsContainer.getChildren().add(buildQuestionCard(q, reps));
        }

        // Check if we've reached the evaluation's points budget
        float totalPoints = questions.stream().map(Question::getPoints).reduce(0f, Float::sum);
        float noteMax = evaluation.getNoteMax();
        boolean budgetFull = totalPoints >= noteMax;

        // Disable/enable the Add Question button and apply visual styling
        if (addQuestionBtn != null) {
            addQuestionBtn.setDisable(budgetFull);
            if (budgetFull) {
                addQuestionBtn.getStyleClass().add("btn-disabled-budget");
                addQuestionBtn.setStyle("-fx-opacity: 0.5;");
                addQuestionBtn.setTooltip(new Tooltip("Score maximum atteint. Plus aucune question ne peut être ajoutée."));
            } else {
                addQuestionBtn.getStyleClass().remove("btn-disabled-budget");
                addQuestionBtn.setStyle("");
                addQuestionBtn.setTooltip(null);
            }
        }

        FadeTransition ft = new FadeTransition(Duration.millis(260), questionsContainer);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        setFooterStatus(budgetFull ? "Score maximum atteint." : "Prêt", false);
    }

    private VBox buildQuestionCard(Question q, List<Reponse> reponses) {
        String titlePreview = q.getTexte() == null ? "(vide)" : q.getTexte();
        if (titlePreview.length() > 72) {
            titlePreview = titlePreview.substring(0, 69) + "…";
        }
        VBox card = new VBox(12);
        card.getStyleClass().add("question-card");

        HBox heading = new HBox();
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.getStyleClass().add("question-card-heading");
        Label headingText = new Label(titlePreview + "  ·  " + formatPoints(q.getPoints()) + " pts");
        headingText.getStyleClass().add("question-card-title");
        heading.getChildren().add(headingText);

        VBox body = new VBox(12);
        body.setPadding(new Insets(0));

        HBox meta = new HBox(10);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getChildren().addAll(
                typeBadge(q.getType()),
                pointsBadge(q.getPoints())
        );
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        meta.getChildren().add(spacer);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("btn-q-edit");
        editBtn.setOnAction(e -> showQuestionEditor(q));

        Button delBtn = new Button("Supprimer");
        delBtn.getStyleClass().add("btn-q-delete");
        delBtn.setOnAction(e -> deleteQuestion(q));

        actions.getChildren().addAll(editBtn, delBtn);

        if (q.getType() == QuestionType.QCM) {
            Button addAnsBtn = new Button("+ Réponse");
            addAnsBtn.getStyleClass().add("btn-q-accent");
            addAnsBtn.setOnAction(e -> showQuickAddAnswer(q));
            actions.getChildren().add(addAnsBtn);
        }

        meta.getChildren().add(actions);

        Label fullText = new Label(q.getTexte());
        fullText.getStyleClass().add("question-body-text");
        fullText.setWrapText(true);
        fullText.setMaxWidth(Double.MAX_VALUE);

        body.getChildren().addAll(meta, fullText);

        if (q.getType() == QuestionType.QCM) {
            Label sec = new Label("Choix de réponse");
            sec.getStyleClass().add("question-section-title");
            VBox list = new VBox(8);
            if (reponses.isEmpty()) {
                Label emptyAns = new Label("Pas encore de réponses. Utilisez Modifier ou + Réponse pour ajouter des options.");
                emptyAns.getStyleClass().add("question-empty-hint");
                list.getChildren().add(emptyAns);
            }
            for (Reponse r : reponses) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("answer-chip-row");
                if (r.isEstCorrect()) {
                    row.getStyleClass().add("answer-chip-row-correct");
                }
                Label txt = new Label(r.getTexte());
                txt.getStyleClass().add("answer-chip-text");
                txt.setWrapText(true);
                HBox.setHgrow(txt, Priority.ALWAYS);
                row.getChildren().add(txt);
                if (r.isEstCorrect()) {
                    Label ok = new Label("Correct");
                    ok.getStyleClass().add("badge-correct-pill");
                    row.getChildren().add(ok);
                }
                list.getChildren().add(row);
            }
            body.getChildren().addAll(sec, list);
        } else if (q.getType() == QuestionType.VRAI_FAUX) {
            Label sec = new Label("Vrai / Faux");
            sec.getStyleClass().add("question-section-title");
            VBox list = new VBox(8);
            if (reponses.isEmpty() || reponses.size() < 2) {
                Label emptyAns = new Label("Ouvrez Modifier et enregistrez pour créer les options « Vrai » et « Faux » et marquer la correcte.");
                emptyAns.getStyleClass().add("question-empty-hint");
                emptyAns.setWrapText(true);
                list.getChildren().add(emptyAns);
            }
            for (Reponse r : orderVraiFaux(reponses)) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("answer-chip-row");
                if (r.isEstCorrect()) {
                    row.getStyleClass().add("answer-chip-row-correct");
                }
                Label txt = new Label(r.getTexte());
                txt.getStyleClass().add("answer-chip-text");
                txt.setWrapText(true);
                HBox.setHgrow(txt, Priority.ALWAYS);
                row.getChildren().add(txt);
                if (r.isEstCorrect()) {
                    Label ok = new Label("Correct");
                    ok.getStyleClass().add("badge-correct-pill");
                    row.getChildren().add(ok);
                }
                list.getChildren().add(row);
            }
            body.getChildren().addAll(sec, list);
        }

        card.getChildren().addAll(heading, body);
        return card;
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
            case TEXTE_LIBRE -> "Type non pris en charge";
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
        alert.setTitle("Supprimer la question");
        alert.setHeaderText("Retirer cette question ?");
        alert.setContentText(q.getTexte() != null && q.getTexte().length() > 120
                ? q.getTexte().substring(0, 117) + "…" : q.getTexte());

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            reponseDAO.deleteByQuestionId(q.getId());
            questionDAO.delete(q.getId());
            refreshQuestions();
            setFooterStatus("Question supprimée.", true);
        }
    }

    private void attachDialogStyles(Dialog<?> dialog) {
        var url = getClass().getResource("/styles/questions-management.css");
        if (url != null) {
            String s = url.toExternalForm();
            if (!dialog.getDialogPane().getStylesheets().contains(s)) {
                dialog.getDialogPane().getStylesheets().add(s);
            }
        }
    }

    private void showQuickAddAnswer(Question q) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une option de réponse");
        dialog.initOwner(root.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
        attachDialogStyles(dialog);

        TextField textField = new TextField();
        textField.setPromptText("Texte de la réponse");
        textField.getStyleClass().add("qm-dialog-field");

        CheckBox correct = new CheckBox("Marquer comme correct");

        VBox vbox = new VBox(12,
                labeledRow("Texte", textField),
                correct);
        dialog.getDialogPane().setContent(wrapDialogContent(vbox));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (textField.getText() == null || textField.getText().trim().isEmpty()) {
                ev.consume();
                showAlert(Alert.AlertType.ERROR, "Le texte de la réponse est obligatoire.");
            }
        });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            reponseDAO.add(new Reponse(textField.getText().trim(), correct.isSelected(), q.getId()));
            refreshQuestions();
            setFooterStatus("Réponse ajoutée.", true);
        }
    }

    private void showQuestionEditor(Question existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvelle question" : "Modifier la question");
        dialog.initOwner(root.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
        attachDialogStyles(dialog);

        TextArea textArea = new TextArea(existing != null ? existing.getTexte() : "");
        textArea.setWrapText(true);
        textArea.setPromptText("Entrez l'énoncé de la question…");
        textArea.getStyleClass().add("qm-dialog-textarea");
        textArea.setPrefRowCount(5);

        ComboBox<QuestionType> typeCombo = new ComboBox<>(
                FXCollections.observableArrayList(QuestionType.QCM, QuestionType.VRAI_FAUX));
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
        QuestionType initialType = existing != null ? existing.getType() : QuestionType.QCM;
        if (initialType == QuestionType.TEXTE_LIBRE) {
            initialType = QuestionType.QCM;
        }
        typeCombo.setValue(initialType);
        typeCombo.getStyleClass().add("qm-dialog-combo");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        TextField pointsField = new TextField(existing != null ? formatPoints(existing.getPoints()) : "1");
        pointsField.getStyleClass().add("qm-dialog-field");

        // Optional professional explanation for the question (shown in review)
        TextArea explanationArea = new TextArea(existing != null ? existing.getExplication() : "");
        explanationArea.setPromptText("Optionnel : explication pour les réviseurs");
        explanationArea.setWrapText(true);
        explanationArea.setPrefRowCount(3);
        explanationArea.getStyleClass().add("qm-dialog-textarea");

        VBox answerSection = new VBox(10);
        Label ansTitle = new Label("Options de réponse (QCM uniquement)");
        ansTitle.getStyleClass().add("qm-dialog-section");
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
        Label vfTitle = new Label("Réponse correcte (Vrai / Faux)");
        vfTitle.getStyleClass().add("qm-dialog-section");
        Label vfHint = new Label("Options de réponse (vrai / faux uniquement)");
        vfHint.setWrapText(true);
        vfHint.getStyleClass().add("qm-dialog-hint");
        vfSection.getChildren().addAll(vfTitle, vfRow, vfHint);

        // Built after answerScroll exists — assigned below
        Runnable[] refillForQcmRef = new Runnable[1];

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

        Button addOptionBtn = new Button("+ Ajouter une option");
        addOptionBtn.getStyleClass().add("qm-btn-add-option");
        addOptionBtn.setMaxWidth(Double.MAX_VALUE);

        VBox answerScrollContent = new VBox(10);
        answerScrollContent.setFillWidth(true);
        answerScrollContent.getChildren().addAll(answerList, addOptionBtn);

        ScrollPane answerScroll = new ScrollPane(answerScrollContent);
        answerScroll.setFitToWidth(true);
        answerScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        answerScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        answerScroll.getStyleClass().add("qm-dialog-scroll");

        addOptionBtn.setOnAction(e -> addAnswerRow(answerList, answerRows, "", false, "", answerScroll));

        refillForQcmRef[0] = () -> {
            answerList.getChildren().clear();
            answerRows.clear();
                if (existing != null && existing.getType() == QuestionType.QCM) {
                List<Reponse> reps = reponseDAO.findByQuestionId(existing.getId());
                if (reps.isEmpty()) {
                    addAnswerRow(answerList, answerRows, "", false, "", answerScroll);
                    addAnswerRow(answerList, answerRows, "", false, "", answerScroll);
                } else {
                    for (Reponse r : reps) {
                        addAnswerRow(answerList, answerRows, r.getTexte(), r.isEstCorrect(), r.getExplication(), answerScroll);
                    }
                }
            } else {
                addAnswerRow(answerList, answerRows, "", false, "", answerScroll);
                addAnswerRow(answerList, answerRows, "", false, "", answerScroll);
            }
        };

        answerSection.getChildren().addAll(ansTitle, answerScroll);

        Runnable updateAnswerVisibility = () -> {
            boolean qcm = typeCombo.getValue() == QuestionType.QCM;
            boolean vf = typeCombo.getValue() == QuestionType.VRAI_FAUX;
            answerSection.setManaged(qcm);
            answerSection.setVisible(qcm);
            vfSection.setManaged(vf);
            vfSection.setVisible(vf);
            if (qcm) {
                refillForQcmRef[0].run();
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
        Label lQuestion = new Label("Question");
        lQuestion.getStyleClass().add("qm-form-label");
        Label lType = new Label("Type");
        lType.getStyleClass().add("qm-form-label");
        Label lPoints = new Label("Points");
        lPoints.getStyleClass().add("qm-form-label");
        grid.add(lQuestion, 0, 0);
        grid.add(textArea, 1, 0);
        grid.add(lType, 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(lPoints, 0, 2);
        grid.add(pointsField, 1, 2);
        Label lExp = new Label("Explication");
        lExp.getStyleClass().add("qm-form-label");
        grid.add(lExp, 0, 3);
        grid.add(explanationArea, 1, 3);
        grid.add(answerStack, 0, 4, 2, 1);

        ColumnConstraints c0 = new ColumnConstraints();
        c0.setPercentWidth(18);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(620);
        dialog.setResizable(true);
        dialog.setOnShown(e -> {
            if (dialog.getDialogPane().getScene() != null
                    && dialog.getDialogPane().getScene().getWindow() instanceof Stage stage) {
                stage.setMinWidth(560);
                stage.setMinHeight(480);
            }
        });
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.setText("Enregistrer");
        ok.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            if (!validateQuestionForm(textArea, pointsField, typeCombo.getValue(), answerRows, vfGroup)) {
                ev.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            persistQuestion(existing, textArea.getText(), typeCombo.getValue(), parsePoints(pointsField.getText()),
                    explanationArea.getText(), answerRows, vfGroup, rbVrai);
            refreshQuestions();
            setFooterStatus(existing == null ? "Question créée." : "Question mise à jour.", true);
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

    /** ~5 answer rows + spacing + “+ Add option” — viewport caps here, then scrolls. */
    private static final int QCM_ANSWER_VISIBLE_ROWS_CAP = 5;

    private static void syncQcmAnswerScrollViewport(ScrollPane scroll, boolean scrollToBottom) {
        Region content = scroll.getContent() instanceof Region r ? r : null;
        if (content == null) {
            return;
        }
        Platform.runLater(() -> {
            content.applyCss();
            content.layout();
            double cw = scroll.getWidth() > 0 ? scroll.getWidth() : 580;
            double natural = content.prefHeight(cw);
            double cap = qcmAnswerScrollViewportCap();
            double vh = Math.min(Math.max(natural, 72), cap);
            scroll.setPrefViewportHeight(vh);
            scroll.setMinViewportHeight(0);
            scroll.setMaxHeight(cap + 32);
            scroll.layout();
            if (scrollToBottom && natural > vh - 2) {
                scroll.setVvalue(1.0);
            }
        });
    }

    private static double qcmAnswerScrollViewportCap() {
        double rowH = 48;
        double listSpacing = 6;
        double shellSpacing = 10;
        double addBtnH = 44;
        int n = QCM_ANSWER_VISIBLE_ROWS_CAP;
        return n * rowH + Math.max(0, n - 1) * listSpacing + shellSpacing + addBtnH + 24;
    }

    private void addAnswerRow(VBox answerList, List<AnswerRow> models, String initialText, boolean correct,
                              String initialExplanation, ScrollPane answerScroll) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        TextField tf = new TextField(initialText);
        tf.getStyleClass().add("qm-dialog-field");
        HBox.setHgrow(tf, Priority.ALWAYS);
        CheckBox cb = new CheckBox("Correct");
        cb.setSelected(correct);
        cb.setText("Correct");
        Button remove = new Button("Supprimer");
        remove.getStyleClass().add("qm-btn-remove");

        // small button to edit per-answer explanation (optional)
        Button explainBtn = new Button("Expliquer");
        explainBtn.getStyleClass().add("qm-btn-explain");

        AnswerRow model = new AnswerRow(tf, cb);
        model.explanation = initialExplanation != null ? initialExplanation : "";
        if (model.explanation != null && !model.explanation.isBlank()) {
            explainBtn.setTooltip(new javafx.scene.control.Tooltip(model.explanation));
        }

        models.add(model);
        remove.setOnAction(e -> {
            models.remove(model);
            answerList.getChildren().remove(row);
            syncQcmAnswerScrollViewport(answerScroll, false);
        });

        explainBtn.setOnAction(e -> {
            Dialog<ButtonType> d = new Dialog<>();
            d.setTitle("Modifier l'explication de la réponse");
            d.initOwner(root.getScene().getWindow());
            TextArea ta = new TextArea(model.explanation != null ? model.explanation : "");
            ta.setWrapText(true);
            ta.setPrefRowCount(4);
            d.getDialogPane().setContent(ta);
            d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> res = d.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                model.explanation = ta.getText();
                if (model.explanation != null && !model.explanation.isBlank()) {
                    explainBtn.setTooltip(new javafx.scene.control.Tooltip(model.explanation));
                } else {
                    explainBtn.setTooltip(null);
                }
            }
        });

        HBox.setHgrow(tf, Priority.ALWAYS);
        row.getChildren().addAll(tf, cb, explainBtn, remove);
        answerList.getChildren().add(row);
        syncQcmAnswerScrollViewport(answerScroll, true);
    }

    private boolean validateQuestionForm(TextArea textArea, TextField pointsField, QuestionType type,
                                         List<AnswerRow> answerRows, ToggleGroup vfGroup) {
        if (textArea.getText() == null || textArea.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Le texte de la question est obligatoire.");
            return false;
        }
        try {
            float p = Float.parseFloat(pointsField.getText().trim());
            if (p <= 0) {
                showAlert(Alert.AlertType.ERROR, "Les points doivent être supérieurs à zéro.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Les points doivent être un nombre valide.");
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
                showAlert(Alert.AlertType.ERROR, "Le QCM nécessite au moins deux options de réponse non vides.");
                return false;
            }
            if (correctCount < 1) {
                showAlert(Alert.AlertType.ERROR, "Marquez au moins une réponse comme correcte.");
                return false;
            }
        }
        if (type == QuestionType.VRAI_FAUX && (vfGroup == null || vfGroup.getSelectedToggle() == null)) {
            showAlert(Alert.AlertType.ERROR, "Sélectionnez si Vrai ou Faux est la réponse correcte.");
            return false;
        }
        return true;
    }

    private float parsePoints(String s) {
        return Float.parseFloat(s.trim());
    }

    private void persistQuestion(Question existing, String texte, QuestionType type, float points, String explication, List<AnswerRow> rows,
                                 ToggleGroup vfGroup, RadioButton rbVrai) {
        if (existing == null) {
            // If this question belongs to an evaluation, ensure we don't silently exceed the evaluation's max score.
            float requestedPoints = points;
            if (evaluation != null) {
                try {
                    List<Question> existingQs = questionDAO.findByEvaluationId(evaluation.getId());
                    float sum = existingQs.stream().map(Question::getPoints).reduce(0f, Float::sum);
                    float max = evaluation.getNoteMax();
                    if (sum + requestedPoints > max) {
                        float remaining = max - sum;
                        // Use standard dialog with simple YES/NO/CANCEL buttons
                        ButtonType adjust = new ButtonType("Ajuster au restant", ButtonBar.ButtonData.YES);
                        ButtonType saveAnyway = new ButtonType("Enregistrer quand même (dépasser)", ButtonBar.ButtonData.NO);
                        ButtonType cancel = ButtonType.CANCEL;

                        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                        a.initOwner(root.getScene().getWindow());
                        a.setTitle("Budget de points dépassé");
                        a.setHeaderText("Le score maximum de cette évaluation serait dépassé.");
                        a.setContentText("Score max évaluation : " + formatPoints(max) + " pts\n"
                                + "Total actuel des questions enregistrées : " + formatPoints(sum) + " pts\n"
                                + "Demandé pour cette question : " + formatPoints(requestedPoints) + " pts\n\n"
                                + (remaining > 0 ? "Points restants si ajustés : " + formatPoints(remaining) + " pts." : "Aucun point restant. Choisissez 'Enregistrer quand même' pour dépasser ou annuler."));
                        a.getButtonTypes().setAll(adjust, saveAnyway, cancel);

                        Optional<ButtonType> choice = a.showAndWait();

                        if (choice.isEmpty()) {
                            return; // user closed dialog
                        }

                        ButtonType selected = choice.get();
                        if (selected == cancel) {
                            return; // user cancelled
                        } else if (selected == adjust) {
                            if (remaining <= 0f) {
                                showAlert(Alert.AlertType.ERROR, "Aucun point restant à attribuer. Augmentez le score max de l'évaluation ou choisissez 'Enregistrer quand même'.");
                                return;
                            }
                            requestedPoints = remaining;
                        }
                        // if selected == saveAnyway -> keep requestedPoints as-is
                    }
                } catch (Exception ex) {
                    // If anything goes wrong calculating totals, fall back to requested value and proceed.
                }
            }

            Question q = new Question(texte.trim(), type, requestedPoints, evaluation.getId());
            if (explication != null && !explication.trim().isEmpty()) {
                q.setExplication(explication.trim());
            }
            if (type == QuestionType.QCM) {
                int id = questionDAO.insertAndGetId(q);
                if (id <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Impossible d'enregistrer la question.");
                    return;
                }
                for (AnswerRow row : rows) {
                    String t = row.textField.getText() != null ? row.textField.getText().trim() : "";
                    if (t.isEmpty()) {
                        continue;
                    }
                    Reponse r = new Reponse(t, row.correct.isSelected(), id);
                    if (row.explanation != null && !row.explanation.trim().isEmpty()) {
                        r.setExplication(row.explanation.trim());
                    }
                    reponseDAO.add(r);
                }
            } else if (type == QuestionType.VRAI_FAUX) {
                int id = questionDAO.insertAndGetId(q);
                if (id <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Impossible d'enregistrer la question.");
                    return;
                }
                addVraiFauxReponses(id, vfGroup, rbVrai);
            }
        } else {
            Question updated = new Question(existing.getId(), texte.trim(), type, points, evaluation.getId());
            if (explication != null && !explication.trim().isEmpty()) {
                updated.setExplication(explication.trim());
            } else {
                updated.setExplication(null);
            }
            questionDAO.update(updated);
            reponseDAO.deleteByQuestionId(existing.getId());
            if (type == QuestionType.QCM) {
                for (AnswerRow row : rows) {
                    String t = row.textField.getText() != null ? row.textField.getText().trim() : "";
                    if (t.isEmpty()) {
                        continue;
                    }
                    Reponse r = new Reponse(t, row.correct.isSelected(), existing.getId());
                    if (row.explanation != null && !row.explanation.trim().isEmpty()) {
                        r.setExplication(row.explanation.trim());
                    }
                    reponseDAO.add(r);
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
        l.getStyleClass().add("qm-form-label");
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
        statusLabel.getStyleClass().removeAll("q-status-ok", "q-status-neutral");
        statusLabel.getStyleClass().add(ok ? "q-status-ok" : "q-status-neutral");
    }

    private static final class AnswerRow {
        final TextField textField;
        final CheckBox correct;
        String explanation;

        AnswerRow(TextField textField, CheckBox correct) {
            this.textField = textField;
            this.correct = correct;
            this.explanation = "";
        }
    }
}
