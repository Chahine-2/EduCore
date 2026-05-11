package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Evaluation;
import models.Resultat;
import models.Utilisateur;
import services.EvaluationDAOImpl;
import services.QuestionDAOImpl;
import services.ResultatDAOImpl;
import utils.UserSession;

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
 * Student identity comes from {@link UserSession} (no manual ID field).
 */
public class StudentPortalController {

    private static final DateTimeFormatter META_FMT =
            DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale.FRENCH);

    @FXML private BorderPane root;
    @FXML private FlowPane cardsFlow;
    @FXML private Label countLabel;
    @FXML private Label emptyLabel;
    @FXML private Label lblPortalEyebrow;
    @FXML private Label lblPortalTitle;
    @FXML private Label lblPortalSubtitle;
    @FXML private Label lblSessionContext;
    @FXML private VBox sessionColumn;
    @FXML private Button btnPortalBack;
    @FXML private HBox bottomBar;
    @FXML private VBox portalTopChrome;
    @FXML private HBox portalEmbedToolbar;
    @FXML private Label countLabelCompact;

    private boolean embeddedInDashboard;
    private Runnable onBackToDashboard;

    private final EvaluationDAOImpl evaluationDAO = new EvaluationDAOImpl();
    private final QuestionDAOImpl questionDAO = new QuestionDAOImpl();
    private final ResultatDAOImpl resultatDAO = new ResultatDAOImpl();

    @FXML
    public void initialize() {
        if (lblPortalTitle != null) {
            lblPortalTitle.setText("Évaluations ouvertes");
        }
        if (lblPortalSubtitle != null) {
            lblPortalSubtitle.setText("Les quiz actifs apparaissent ci-dessous. Vos tentatives sont enregistrées sur votre compte.");
        }
        updateSessionContext();
        refreshCards();
    }

    /**
     * When the portal is shown inside the student dashboard, the large header (title, session chip,
     * full toolbar) is hidden; a slim count + refresh row appears above the cards. Footer is hidden.
     */
    public void setDashboardEmbedMode(boolean enabled, Runnable backToDashboard) {
        this.embeddedInDashboard = enabled;
        this.onBackToDashboard = backToDashboard;

        if (root != null) {
            root.getStyleClass().removeAll("embedded-dashboard");
            if (enabled) {
                root.getStyleClass().add("embedded-dashboard");
            }
        }
        if (portalTopChrome != null) {
            portalTopChrome.setVisible(!enabled);
            portalTopChrome.setManaged(!enabled);
        }
        if (portalEmbedToolbar != null) {
            portalEmbedToolbar.setVisible(enabled);
            portalEmbedToolbar.setManaged(enabled);
        }

        if (!enabled) {
            if (bottomBar != null) {
                bottomBar.setVisible(true);
                bottomBar.setManaged(true);
            }
            return;
        }

        if (bottomBar != null) {
            bottomBar.setVisible(false);
            bottomBar.setManaged(false);
        }
        updateSessionContext();
        refreshCards();
    }

    private void setCountTexts(String text) {
        if (countLabel != null) {
            countLabel.setText(text);
        }
        if (countLabelCompact != null) {
            countLabelCompact.setText(text);
        }
    }

    private void updateSessionContext() {
        if (lblSessionContext == null) {
            return;
        }
        lblSessionContext.getStyleClass().removeAll("portal-session-muted");
        Utilisateur u = UserSession.getCurrentUser();
        if (u != null && u.getId() > 0) {
            String name = (trimStr(u.getPrenom()) + " " + trimStr(u.getNom())).trim();
            if (name.isEmpty()) {
                name = "Étudiant";
            }
            lblSessionContext.setText(name + "  ·  session active");
        } else {
            lblSessionContext.setText("Non connecté");
            lblSessionContext.getStyleClass().add("portal-session-muted");
        }
        if (sessionColumn != null) {
            sessionColumn.setVisible(true);
            sessionColumn.setManaged(true);
        }
    }

    private static String trimStr(String s) {
        return s == null ? "" : s.trim();
    }

    /** Positive utilisateur / étudiant id from session, or -1 if none. */
    private int getLoggedInStudentId() {
        Utilisateur u = UserSession.getCurrentUser();
        if (u == null || u.getId() <= 0) {
            return -1;
        }
        return u.getId();
    }

    /** Reloads active evaluations and counts (e.g. when opening the Evaluations tab). */
    public void refreshEvaluationsList() {
        updateSessionContext();
        refreshCards();
    }

    @FXML
    private void handleBackHome() {
        if (embeddedInDashboard && onBackToDashboard != null) {
            onBackToDashboard.run();
            return;
        }
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

        int n = active.size();
        String countText;
        if (n == 0) {
            countText = "Aucune évaluation ouverte";
        } else if (n == 1) {
            countText = "1 évaluation ouverte maintenant";
        } else {
            countText = n + " évaluations ouvertes maintenant";
        }
        setCountTexts(countText);

        emptyLabel.setVisible(n == 0);
        emptyLabel.setManaged(n == 0);

        int studentId = getLoggedInStudentId();
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

    /** Quiz window is open between {@code date_debut} and {@code date_fin} (inclusive). */
    public static boolean isActive(Evaluation e, LocalDateTime now) {
        if (e.getDateDebut() == null || e.getDateFin() == null) {
            return false;
        }
        return !now.isBefore(e.getDateDebut()) && !now.isAfter(e.getDateFin());
    }

    private VBox buildCard(Evaluation ev, Resultat submitted) {
        VBox card = new VBox(10);
        card.getStyleClass().add("portal-card");
        if (submitted != null) {
            card.getStyleClass().add("portal-card-done");
        }
        card.setAlignment(Pos.TOP_LEFT);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(submitted == null ? "En cours" : "Soumis");
        badge.getStyleClass().add(submitted == null ? "portal-badge-live" : "portal-badge-done");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        top.getChildren().addAll(badge, sp);

        Label title = new Label(ev.getTitre());
        title.getStyleClass().add("portal-card-title");
        title.setWrapText(true);

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, Priority.ALWAYS);
        titleRow.getChildren().add(title);

        if (submitted != null && submitted.getScore() != null) {
            float totalPoints = questionDAO.findByEvaluationId(ev.getId()).stream()
                    .map(q -> q.getPoints())
                    .reduce(0f, Float::sum);
            Label titleScore = new Label(formatScore(submitted.getScore()) + " / " + formatScore(totalPoints));
            titleScore.getStyleClass().add("portal-title-score");
            titleRow.getChildren().add(titleScore);
        }

        String desc = ev.getDescription();
        if (desc != null && desc.length() > 140) {
            desc = desc.substring(0, 137) + "…";
        }
        Label descLbl = new Label(desc == null || desc.isBlank() ? "Aucune description." : desc);
        descLbl.getStyleClass().add("portal-card-desc");
        descLbl.setWrapText(true);

        String meta = ev.getDureeMinutes() + " min · fin " + formatDt(ev.getDateFin());
        if (submitted != null && submitted.getDatePassage() != null) {
            meta = meta + " · envoyé " + submitted.getDatePassage().format(META_FMT);
        }
        Label metaLbl = new Label(meta);
        metaLbl.getStyleClass().add("portal-meta");
        metaLbl.setWrapText(true);

        Button action = new Button(submitted == null ? "Démarrer le quiz" : "Voir la tentative");
        action.getStyleClass().add(submitted == null ? "portal-start" : "portal-view");
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
        int studentId = getLoggedInStudentId();
        if (studentId <= 0) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            if (root.getScene() != null && root.getScene().getWindow() != null) {
                a.initOwner(root.getScene().getWindow());
            }
            a.setHeaderText(null);
            a.setContentText("Veuillez vous connecter en tant qu'étudiant pour passer ou consulter une évaluation.");
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
            stage.setTitle(existingResultat == null ? "Quiz — " + evaluation.getTitre() : "Relecture — " + evaluation.getTitre());
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
            Alert a = new Alert(Alert.AlertType.ERROR);
            if (root.getScene() != null && root.getScene().getWindow() != null) {
                a.initOwner(root.getScene().getWindow());
            }
            a.setHeaderText(null);
            a.setContentText("Impossible d'ouvrir le quiz : " + ex.getMessage());
            a.showAndWait();
        }
    }
}
