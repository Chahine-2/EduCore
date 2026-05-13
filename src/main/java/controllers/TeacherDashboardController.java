package controllers;

import interfaces.IUtilisateurService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import models.Etudiant;
import models.Evaluation;
import models.Resultat;
import models.TeacherEvalAttemptRow;
import models.TeacherFraudAuditRow;
import models.Utilisateur;
import services.EvaluationDAOImpl;
import services.FraudeLogDAOImpl;
import services.ResultatDAOImpl;
import services.UtilisateurService;
import utils.UserSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Modern teacher workspace (FXML: {@code TeacherDashboard.fxml}, CSS: {@code teacher-dashboard.css}).
 */
public class TeacherDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblWelcomeSub;
    @FXML private Label lblDateTime;
    @FXML private Label lblAvatarInitials;
    @FXML private Label lblStatCourses;
    @FXML private Label lblStatEvaluations;
    @FXML private Label lblStatStudents;
    @FXML private Label lblStatFraud;
    @FXML private Label lblProfileNom;
    @FXML private Label lblProfilePrenom;
    @FXML private Label lblProfileEmail;

    @FXML private VBox paneDashboard;
    @FXML private VBox paneCourseMgmt;
    @FXML private BorderPane courseMgmtShell;
    @FXML private VBox paneEvalMgmt;
    @FXML private BorderPane evalMgmtShell;
    @FXML private VBox paneAbsence;
    @FXML private VBox paneFraud;
    @FXML private VBox paneStatistics;
    @FXML private VBox paneHackathon;
    @FXML private BorderPane hackathonShell;
    @FXML private VBox paneProfile;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavCourses;
    @FXML private Button btnNavEvaluations;
    @FXML private Button btnNavAbsence;
    @FXML private Button btnNavFraud;
    @FXML private Button btnNavStatistics;
    @FXML private Button btnNavStatHackathon;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavLogout;

    @FXML private ComboBox<String> comboClasse;
    @FXML private TableView<Etudiant> tableEtudiants;
    @FXML private TableColumn<Etudiant, String> colMatricule;
    @FXML private TableColumn<Etudiant, String> colNom;
    @FXML private TableColumn<Etudiant, String> colPrenom;
    @FXML private TableColumn<Etudiant, String> colStatut;

    @FXML private TableView<TeacherFraudAuditRow> tableFraudReports;
    @FXML private Label lblFraudHeadline;
    @FXML private Label lblFraudMetricEvents;
    @FXML private Label lblFraudMetricStudents;
    @FXML private Label lblFraudMetricResults;

    @FXML private ComboBox<Evaluation> comboEvaluationStats;
    @FXML private Label lblEvalStatsMeta;
    @FXML private Label lblEvalMetricAttempts;
    @FXML private Label lblEvalMetricAverage;
    @FXML private Label lblEvalMetricPass;
    @FXML private Label lblEvalMetricFraud;
    @FXML private TableView<TeacherEvalAttemptRow> tableEvalAttempts;
    @FXML private VBox chartScoreHolder;
    @FXML private VBox chartOutcomePieHolder;
    @FXML private VBox chartIntegrityPieHolder;

    private BarChart<String, Number> chartScoreDistribution;
    private PieChart chartOutcomePie;
    private PieChart chartIntegrityPie;

    private final IUtilisateurService service = new UtilisateurService();
    private final FraudeLogDAOImpl fraudeLogDAO = new FraudeLogDAOImpl();
    private final ResultatDAOImpl resultatDAO = new ResultatDAOImpl();
    private final EvaluationDAOImpl evaluationDAO = new EvaluationDAOImpl();
    private final ObservableList<Etudiant> listeEtudiants = FXCollections.observableArrayList();
    private final List<Button> navButtons = new java.util.ArrayList<>();
    private Timeline clockTimeline;

    private boolean evaluationsEmbeddedLoaded;
    private boolean coursesEmbeddedLoaded;
    private boolean hackathonEmbeddedLoaded;

    @FXML
    void initialize() {
        navButtons.add(btnNavDashboard);
        navButtons.add(btnNavCourses);
        navButtons.add(btnNavEvaluations);
        navButtons.add(btnNavAbsence);
        navButtons.add(btnNavFraud);
        navButtons.add(btnNavStatistics);
        navButtons.add(btnNavStatHackathon);
        navButtons.add(btnNavProfile);

        colMatricule.setCellValueFactory(new PropertyValueFactory<>("numeroEtudiant"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutAppel"));

        wireTeacherAnalyticsUi();
        initEvalStatisticsCharts();

        comboClasse.getItems().setAll(service.listerToutesLesClasses());

        Utilisateur user = UserSession.getCurrentUser();
        if (user != null) {
            String prenom = safe(user.getPrenom());
            String nom = safe(user.getNom());
            String display = (prenom + " " + nom).trim();
            lblWelcome.setText(display.isEmpty() ? "Welcome, Professor" : "Welcome, Professor " + display);
            lblAvatarInitials.setText(initials(prenom, nom));
            lblProfilePrenom.setText(prenom.isEmpty() ? "—" : prenom);
            lblProfileNom.setText(nom.isEmpty() ? "—" : nom);
            lblProfileEmail.setText(safe(user.getEmail()).isEmpty() ? "—" : user.getEmail());
        } else {
            lblWelcome.setText("Welcome, Professor");
            lblAvatarInitials.setText("?");
            lblProfilePrenom.setText("—");
            lblProfileNom.setText("—");
            lblProfileEmail.setText("—");
        }

        refreshStatistics();

        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

        Platform.runLater(() -> {
            Scene scene = lblWelcome.getScene();
            if (scene != null) {
                Stage st = (Stage) scene.getWindow();
                st.setMinWidth(1100);
                st.setMinHeight(680);
            }
        });
    }

    private void refreshStatistics() {
        lblStatCourses.setText("8");
        lblStatEvaluations.setText("14");
        lblStatStudents.setText("126");
        try {
            long fraudRows = resultatDAO.getAll().stream().filter(Resultat::isFraudeDetecte).count();
            lblStatFraud.setText(String.valueOf(fraudRows));
        } catch (Exception e) {
            lblStatFraud.setText("—");
        }
    }

    private void updateClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy ' - ' HH:mm", Locale.ENGLISH);
        lblDateTime.setText(fmt.format(LocalDateTime.now()));
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String initials(String prenom, String nom) {
        String a = prenom.isEmpty() ? "?" : prenom.substring(0, 1);
        String b = nom.isEmpty() ? "" : nom.substring(0, 1);
        return (a + b).toUpperCase(Locale.ROOT);
    }

    private void selectNav(Button active) {
        for (Button b : navButtons) {
            b.getStyleClass().removeAll("teacher-nav-button-active");
        }
        if (!active.getStyleClass().contains("teacher-nav-button")) {
            active.getStyleClass().add("teacher-nav-button");
        }
        if (!active.getStyleClass().contains("teacher-nav-button-active")) {
            active.getStyleClass().add("teacher-nav-button-active");
        }
    }

    private void showPane(VBox pane) {
        for (VBox p : List.of(paneDashboard, paneFraud, paneCourseMgmt, paneEvalMgmt, paneAbsence,
                paneStatistics, paneHackathon, paneProfile)) {
            boolean on = p == pane;
            p.setVisible(on);
            p.setManaged(on);
        }
    }

    @FXML
    void onNavDashboard(ActionEvent event) {
        showPane(paneDashboard);
        selectNav(btnNavDashboard);
    }

    @FXML
    void onNavCourses(ActionEvent event) {
        ensureCoursesEmbedded();
        showPane(paneCourseMgmt);
        selectNav(btnNavCourses);
    }

    private void ensureCoursesEmbedded() {
        if (coursesEmbeddedLoaded || courseMgmtShell == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/GestionCours.fxml")));
            Parent gestionRoot = loader.load();
            GestionCoursController coursCtrl = loader.getController();
            coursCtrl.setTeacherDashboardEmbedMode(true, () -> {
                showPane(paneDashboard);
                selectNav(btnNavDashboard);
            }, (cours) -> {
                try {
                    FXMLLoader detailsLoader = new FXMLLoader(getClass().getResource("/DetailsCours.fxml"));
                    Parent detailsRoot = detailsLoader.load();
                    DetailsCoursController detailsCtrl = detailsLoader.getController();
                    detailsCtrl.setTeacherDashboardEmbedMode(true, () -> {
                        courseMgmtShell.setCenter(gestionRoot);
                    });
                    courseMgmtShell.setCenter(detailsRoot);
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Impossible de charger les détails : " + ex.getMessage()).showAndWait();
                    ex.printStackTrace();
                }
            });
            courseMgmtShell.setCenter(gestionRoot);
            coursesEmbeddedLoaded = true;
        } catch (IOException ex) {
            coursesEmbeddedLoaded = false;
            new Alert(Alert.AlertType.ERROR,
                    "Could not load course management: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void onNavEvaluations(ActionEvent event) {
        ensureEvaluationsEmbedded();
        showPane(paneEvalMgmt);
        selectNav(btnNavEvaluations);
    }

    private void ensureEvaluationsEmbedded() {
        if (evaluationsEmbeddedLoaded || evalMgmtShell == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/evaluation.fxml")));
            Parent evaluationRoot = loader.load();
            evalMgmtShell.setCenter(evaluationRoot);
            evaluationsEmbeddedLoaded = true;
        } catch (IOException ex) {
            evaluationsEmbeddedLoaded = false;
            new Alert(Alert.AlertType.ERROR,
                    "Could not load evaluation module: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void onNavAbsence(ActionEvent event) {
        showPane(paneAbsence);
        selectNav(btnNavAbsence);
    }

    @FXML
    void onNavFraud(ActionEvent event) {
        refreshFraudReports();
        showPane(paneFraud);
        selectNav(btnNavFraud);
    }

    @FXML
    void onRefreshFraudReports(ActionEvent event) {
        refreshFraudReports();
    }

    @FXML
    void onNavStatistics(ActionEvent event) {
        loadEvaluationsIntoCombo();
        showPane(paneStatistics);
        selectNav(btnNavStatistics);
    }

    @FXML
    void onLoadEvaluationStats(ActionEvent event) {
        loadEvaluationStatsForSelection();
    }

    @FXML
    void onNavStatHackathon(ActionEvent event) {
        ensureHackathonStatsEmbedded();
        showPane(paneHackathon);
        selectNav(btnNavStatHackathon);
    }

    private void ensureHackathonStatsEmbedded() {
        if (hackathonEmbeddedLoaded || hackathonShell == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/Dashboard.fxml")));
            Parent dashboardRoot = loader.load();
            hackathonShell.setCenter(dashboardRoot);
            hackathonEmbeddedLoaded = true;
        } catch (IOException ex) {
            hackathonEmbeddedLoaded = false;
            new Alert(Alert.AlertType.ERROR,
                    "Could not load hackathon statistics: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void onNavProfile(ActionEvent event) {
        showPane(paneProfile);
        selectNav(btnNavProfile);
    }

    @FXML
    void onCardCourses(MouseEvent event) {
        onNavCourses(null);
    }

    @FXML
    void onCardEvaluations(MouseEvent event) {
        onNavEvaluations(null);
    }

    @FXML
    void onCardFraud(MouseEvent event) {
        onNavFraud(null);
    }

    @FXML
    void onNotifications(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION,
                "No critical notifications.\n(Evaluation deadlines and integrity alerts will surface here.)").showAndWait();
    }

    @FXML
    void chargerEtudiants(ActionEvent event) {
        String classeChoisie = comboClasse.getValue();
        if (classeChoisie == null || classeChoisie.isEmpty()) {
            afficherAlerte("Selection required", "Please choose a class.");
            return;
        }
        listeEtudiants.clear();
        listeEtudiants.addAll(service.listerEtudiantsParClasse(classeChoisie));
        tableEtudiants.setItems(listeEtudiants);
        service.preparerNouvelleSessionAppel(classeChoisie);
    }

    @FXML
    void marquerPresent(ActionEvent event) {
        enregistrerAppel("Présent");
    }

    @FXML
    void marquerAbsent(ActionEvent event) {
        enregistrerAppel("Absent");
    }

    private void enregistrerAppel(String statut) {
        Etudiant etudiantSelect = tableEtudiants.getSelectionModel().getSelectedItem();
        if (etudiantSelect == null) {
            afficherAlerte("Selection required", "Select a student row in the table first.");
            return;
        }
        if (service.enregistrerPresence(etudiantSelect.getId(), statut)) {
            etudiantSelect.setStatutAppel(statut);
            tableEtudiants.refresh();
            tableEtudiants.getSelectionModel().selectNext();
        } else {
            afficherAlerte("Database error", "Could not save attendance.");
        }
    }

    @FXML
    void onLogout(ActionEvent event) {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        UserSession.clear();
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/views/Login.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 520));
            stage.setTitle("EduCore - Connexion");
            stage.centerOnScreen();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Could not load login: " + ex.getMessage()).showAndWait();
        }
    }

    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void wireTeacherAnalyticsUi() {
        TableColumn<TeacherFraudAuditRow, String> cWhen = new TableColumn<>("Detected");
        cWhen.setPrefWidth(150);
        cWhen.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().detectedAt()));

        TableColumn<TeacherFraudAuditRow, String> cStudent = new TableColumn<>("Student");
        cStudent.setPrefWidth(200);
        cStudent.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().student()));

        TableColumn<TeacherFraudAuditRow, String> cEval = new TableColumn<>("Evaluation");
        cEval.setPrefWidth(180);
        cEval.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().evaluation()));

        TableColumn<TeacherFraudAuditRow, String> cType = new TableColumn<>("Event type");
        cType.setPrefWidth(160);
        cType.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().fraudType()));

        TableColumn<TeacherFraudAuditRow, String> cDesc = new TableColumn<>("Details");
        cDesc.setPrefWidth(360);
        cDesc.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().description()));

        tableFraudReports.getColumns().setAll(cWhen, cStudent, cEval, cType, cDesc);
        tableFraudReports.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<TeacherEvalAttemptRow, String> eStudent = new TableColumn<>("Student");
        eStudent.setPrefWidth(200);
        eStudent.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().student()));

        TableColumn<TeacherEvalAttemptRow, String> eScore = new TableColumn<>("Score");
        eScore.setPrefWidth(110);
        eScore.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().score()));

        TableColumn<TeacherEvalAttemptRow, String> eOutcome = new TableColumn<>("Outcome");
        eOutcome.setPrefWidth(120);
        eOutcome.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().outcome()));

        TableColumn<TeacherEvalAttemptRow, String> eIntegrity = new TableColumn<>("Integrity");
        eIntegrity.setPrefWidth(130);
        eIntegrity.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().integrity()));

        TableColumn<TeacherEvalAttemptRow, String> eWhen = new TableColumn<>("Completed");
        eWhen.setPrefWidth(160);
        eWhen.setCellValueFactory(f -> new ReadOnlyStringWrapper(f.getValue().completedAt()));

        tableEvalAttempts.getColumns().setAll(eStudent, eScore, eOutcome, eIntegrity, eWhen);
        tableEvalAttempts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        comboEvaluationStats.setConverter(new StringConverter<>() {
            @Override
            public String toString(Evaluation e) {
                return e == null ? "" : e.getTitre();
            }

            @Override
            public Evaluation fromString(String s) {
                return comboEvaluationStats.getItems().stream()
                        .filter(ev -> ev.getTitre().equals(s))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void refreshFraudReports() {
        List<TeacherFraudAuditRow> rows = fraudeLogDAO.findAllAuditRows();
        tableFraudReports.setItems(FXCollections.observableArrayList(rows));
        long distinctStudents = rows.stream().mapToInt(TeacherFraudAuditRow::studentUserId).distinct().count();
        long flaggedResults = resultatDAO.getAll().stream().filter(Resultat::isFraudeDetecte).count();
        lblFraudMetricEvents.setText(String.valueOf(rows.size()));
        lblFraudMetricStudents.setText(String.valueOf(distinctStudents));
        lblFraudMetricResults.setText(String.valueOf(flaggedResults));
        lblFraudHeadline.setText(rows.isEmpty()
                ? "No anti-cheat events recorded yet. Events appear when the proctoring pipeline logs an incident."
                : rows.size() + " logged event(s) — newest first.");
    }

    private void loadEvaluationsIntoCombo() {
        List<Evaluation> all = evaluationDAO.getAll();
        Evaluation previous = comboEvaluationStats.getSelectionModel().getSelectedItem();
        comboEvaluationStats.getItems().setAll(all);
        if (previous != null) {
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getId() == previous.getId()) {
                    comboEvaluationStats.getSelectionModel().select(i);
                    return;
                }
            }
        }
        if (!all.isEmpty()) {
            comboEvaluationStats.getSelectionModel().selectFirst();
        }
    }

    private void loadEvaluationStatsForSelection() {
        Evaluation ev = comboEvaluationStats.getSelectionModel().getSelectedItem();
        if (ev == null) {
            afficherAlerte("Evaluation", "Please select an evaluation from the list.");
            return;
        }
        List<TeacherEvalAttemptRow> rows = resultatDAO.findAttemptsForTeacherEvaluation(ev.getId());
        tableEvalAttempts.setItems(FXCollections.observableArrayList(rows));

        lblEvalStatsMeta.setText(String.format(Locale.US,
                "Max score %.0f — Passing threshold %.0f", ev.getNoteMax(), ev.getNotePassage()));

        int n = rows.size();
        lblEvalMetricAttempts.setText(String.valueOf(n));

        double sum = 0;
        int scored = 0;
        for (TeacherEvalAttemptRow r : rows) {
            String s = r.score();
            if (s != null && !s.equals("—") && s.contains("/")) {
                try {
                    String num = s.split("/")[0].trim();
                    sum += Double.parseDouble(num);
                    scored++;
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
        }
        if (scored > 0) {
            lblEvalMetricAverage.setText(String.format(Locale.US, "%.1f / %.0f", sum / scored, ev.getNoteMax()));
        } else {
            lblEvalMetricAverage.setText("—");
        }

        long pass = rows.stream().filter(r -> "Pass".equals(r.outcome())).count();
        if (n > 0) {
            lblEvalMetricPass.setText(String.format(Locale.US, "%.0f%% (%d/%d)",
                    100.0 * pass / n, pass, n));
        } else {
            lblEvalMetricPass.setText("—");
        }

        long fraud = rows.stream().filter(r -> r.integrity().toLowerCase(Locale.ROOT).contains("fraud")).count();
        lblEvalMetricFraud.setText(n > 0 ? fraud + " / " + n : "0");

        updateEvaluationCharts(ev, rows);
    }

    private void initEvalStatisticsCharts() {
        if (chartScoreHolder == null || chartOutcomePieHolder == null || chartIntegrityPieHolder == null) {
            return;
        }

        CategoryAxis scoreX = new CategoryAxis();
        scoreX.setLabel("Score band (% of max)");
        NumberAxis scoreY = new NumberAxis();
        scoreY.setLabel("Students");
        scoreY.setForceZeroInRange(true);
        scoreY.setMinorTickVisible(false);
        scoreY.setAutoRanging(true);

        chartScoreDistribution = new BarChart<>(scoreX, scoreY);
        chartScoreDistribution.setTitle(null);
        chartScoreDistribution.setLegendVisible(false);
        chartScoreDistribution.setAnimated(false);
        chartScoreDistribution.setVerticalGridLinesVisible(false);
        chartScoreDistribution.setHorizontalGridLinesVisible(true);
        chartScoreDistribution.getStyleClass().add("teacher-stats-bar-chart");
        chartScoreDistribution.setMinHeight(220);
        VBox.setVgrow(chartScoreDistribution, Priority.ALWAYS);
        chartScoreHolder.getChildren().setAll(chartScoreDistribution);

        chartOutcomePie = new PieChart();
        chartOutcomePie.setTitle(null);
        chartOutcomePie.setLabelsVisible(true);
        chartOutcomePie.setLegendSide(Side.BOTTOM);
        chartOutcomePie.setClockwise(true);
        chartOutcomePie.setStartAngle(90);
        chartOutcomePie.getStyleClass().addAll("teacher-stats-pie-chart", "teacher-stats-pie-outcome");
        chartOutcomePie.setMinHeight(220);
        VBox.setVgrow(chartOutcomePie, Priority.ALWAYS);
        chartOutcomePieHolder.getChildren().setAll(chartOutcomePie);

        chartIntegrityPie = new PieChart();
        chartIntegrityPie.setTitle(null);
        chartIntegrityPie.setLabelsVisible(true);
        chartIntegrityPie.setLegendSide(Side.BOTTOM);
        chartIntegrityPie.setClockwise(true);
        chartIntegrityPie.setStartAngle(90);
        chartIntegrityPie.getStyleClass().addAll("teacher-stats-pie-chart", "teacher-stats-pie-integrity");
        chartIntegrityPie.setMinHeight(220);
        VBox.setVgrow(chartIntegrityPie, Priority.ALWAYS);
        chartIntegrityPieHolder.getChildren().setAll(chartIntegrityPie);
    }

    private static Double parseScoreNumerator(String scoreText) {
        if (scoreText == null || scoreText.isBlank() || "—".equals(scoreText) || !scoreText.contains("/")) {
            return null;
        }
        try {
            return Double.parseDouble(scoreText.split("/")[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void updateEvaluationCharts(Evaluation ev, List<TeacherEvalAttemptRow> rows) {
        if (chartScoreDistribution == null || chartOutcomePie == null || chartIntegrityPie == null) {
            return;
        }

        double noteMax = Math.max(1.0, ev.getNoteMax());
        int[] bins = new int[5];
        int pass = 0;
        int below = 0;
        int noscore = 0;
        int clean = 0;
        int fraud = 0;

        for (TeacherEvalAttemptRow r : rows) {
            Double pts = parseScoreNumerator(r.score());
            if (pts != null) {
                double pct = Math.min(100.0, Math.max(0.0, (pts / noteMax) * 100.0));
                int idx = (int) Math.floor(pct / 20.0);
                if (idx > 4) {
                    idx = 4;
                }
                bins[idx]++;
            }

            String o = r.outcome();
            if ("Pass".equals(o)) {
                pass++;
            } else if ("Below threshold".equals(o)) {
                below++;
            } else {
                noscore++;
            }

            if (r.integrity().toLowerCase(Locale.ROOT).contains("fraud")) {
                fraud++;
            } else {
                clean++;
            }
        }

        XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
        scoreSeries.setName("Students");
        String[] bandLabels = {"0–20%", "20–40%", "40–60%", "60–80%", "80–100%"};
        for (int i = 0; i < 5; i++) {
            scoreSeries.getData().add(new XYChart.Data<>(bandLabels[i], (double) bins[i]));
        }
        chartScoreDistribution.getData().setAll(scoreSeries);
        CategoryAxis scoreXAxis = (CategoryAxis) chartScoreDistribution.getXAxis();
        scoreXAxis.setCategories(FXCollections.observableArrayList(Arrays.asList(bandLabels)));
        Platform.runLater(() -> {
            chartScoreDistribution.applyCss();
            chartScoreDistribution.layout();
        });

        ObservableList<PieChart.Data> outcomeSlices = FXCollections.observableArrayList();
        if (pass > 0) {
            outcomeSlices.add(new PieChart.Data("Pass", pass));
        }
        if (below > 0) {
            outcomeSlices.add(new PieChart.Data("Below threshold", below));
        }
        if (noscore > 0) {
            outcomeSlices.add(new PieChart.Data("No score / pending", noscore));
        }
        chartOutcomePie.setData(outcomeSlices);

        ObservableList<PieChart.Data> integritySlices = FXCollections.observableArrayList();
        if (clean > 0) {
            integritySlices.add(new PieChart.Data("OK", clean));
        }
        if (fraud > 0) {
            integritySlices.add(new PieChart.Data("Fraud flagged", fraud));
        }
        chartIntegrityPie.setData(integritySlices);
    }
}
