package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Utilisateur;
import utils.NavigationManager;
import utils.UserSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Student dashboard shell: sidebar, welcome header (Dashboard tab only), overview cards.
 * Wire statistics to your DAOs in {@link #refreshStatistics()}.
 */
public class StudentDashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblWelcomeSub;
    @FXML private Label lblDateTime;
    @FXML private Label lblAvatarInitials;
    @FXML private Label lblStatCourses;
    @FXML private Label lblStatUpcoming;
    @FXML private Label lblStatPassed;
    @FXML private Label lblStatFraud;
    @FXML private Label lblProfileNom;
    @FXML private Label lblProfilePrenom;
    @FXML private Label lblProfileEmail;

    @FXML private StackPane contentStack;
    @FXML private VBox paneDashboard;
    @FXML private VBox paneCourses;
    @FXML private VBox paneEvaluations;
    @FXML private VBox paneResults;
    @FXML private VBox paneProfile;
    @FXML private BorderPane evalPortalShell;
    @FXML private BorderPane coursesAccueilShell;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavCourses;
    @FXML private Button btnNavEvaluations;
    @FXML private Button btnNavResults;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavLogout;

    private Timeline clockTimeline;

    private boolean evaluationsPortalLoaded;
    private StudentPortalController evalPortalController;

    private boolean coursesAccueilLoaded;
    private Parent accueilCourseRoot;
    private Parent etudiantCourseRoot;

    private final List<Button> navButtons = new java.util.ArrayList<>();

    @FXML
    void initialize() {
        navButtons.add(btnNavDashboard);
        navButtons.add(btnNavCourses);
        navButtons.add(btnNavEvaluations);
        navButtons.add(btnNavResults);
        navButtons.add(btnNavProfile);

        Utilisateur user = UserSession.getCurrentUser();
        if (user != null) {
            String prenom = safe(user.getPrenom());
            String nom = safe(user.getNom());
            String display = (prenom.isEmpty() && nom.isEmpty() ? "Student" : (prenom + " " + nom).trim());
            String welcomeLine = "Welcome back, " + display;
            lblWelcome.setText(welcomeLine);
            if (lblWelcomeSub != null) {
                lblWelcomeSub.setText("Voici un aperçu de votre progression et de vos prochaines échéances.");
            }
            lblAvatarInitials.setText(initials(prenom, nom));
            lblProfilePrenom.setText(prenom.isEmpty() ? "—" : prenom);
            lblProfileNom.setText(nom.isEmpty() ? "—" : nom);
            lblProfileEmail.setText(safe(user.getEmail()).isEmpty() ? "—" : user.getEmail());
        } else {
            lblWelcome.setText("Welcome back, Student");
            if (lblWelcomeSub != null) {
                lblWelcomeSub.setText("Voici un aperçu de votre progression et de vos prochaines échéances.");
            }
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
                st.setMinWidth(1024);
                st.setMinHeight(640);
            }
        });
    }

    /**
     * Replace with counts from your services (cours, évaluations, résultats, logs fraude).
     */
    private void refreshStatistics() {
        lblStatCourses.setText("6");
        lblStatUpcoming.setText("2");
        lblStatPassed.setText("11");
        lblStatFraud.setText("0");
    }

    private void updateClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy · HH:mm", Locale.ENGLISH);
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
            b.getStyleClass().removeAll("nav-button-active");
        }
        if (!active.getStyleClass().contains("nav-button")) {
            active.getStyleClass().add("nav-button");
        }
        if (!active.getStyleClass().contains("nav-button-active")) {
            active.getStyleClass().add("nav-button-active");
        }
    }

    private void showPane(VBox pane) {
        for (VBox p : List.of(paneDashboard, paneCourses, paneEvaluations, paneResults, paneProfile)) {
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
        ensureCoursesAccueilLoaded();
        showPane(paneCourses);
        selectNav(btnNavCourses);
    }

    private void embedAccueilInShell() {
        if (coursesAccueilShell != null && accueilCourseRoot != null) {
            coursesAccueilShell.setCenter(accueilCourseRoot);
        }
    }

    private void embedEtudiantCatalogInShell() {
        if (coursesAccueilShell == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/Etudiant.fxml")));
            Parent etudiant = loader.load();
            etudiantCourseRoot = etudiant;
            EtudiantController ec = loader.getController();
            ec.setStudentDashboardEmbedMode(
                    true,
                    this::embedAccueilInShell,
                    lectureRoot -> coursesAccueilShell.setCenter(lectureRoot),
                    () -> coursesAccueilShell.setCenter(etudiantCourseRoot));
            coursesAccueilShell.setCenter(etudiant);
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Impossible de charger le catalogue : " + ex.getMessage()).showAndWait();
        }
    }

    private void ensureCoursesAccueilLoaded() {
        if (coursesAccueilShell == null) {
            return;
        }
        if (coursesAccueilLoaded) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/Accueil.fxml")));
            Parent accueil = loader.load();
            accueilCourseRoot = accueil;
            AccueilController accueilController = loader.getController();
            accueilController.setDashboardEmbedMode(true, () -> {
                showPane(paneDashboard);
                selectNav(btnNavDashboard);
            }, this::embedEtudiantCatalogInShell);
            coursesAccueilShell.setCenter(accueil);
            coursesAccueilLoaded = true;
        } catch (IOException ex) {
            coursesAccueilLoaded = false;
            new Alert(Alert.AlertType.ERROR,
                    "Impossible de charger Accueil : " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void onNavEvaluations(ActionEvent event) {
        ensureEvaluationsPortalLoaded();
        if (evalPortalController != null) {
            evalPortalController.refreshEvaluationsList();
        }
        showPane(paneEvaluations);
        selectNav(btnNavEvaluations);
    }

    private void ensureEvaluationsPortalLoaded() {
        if (evalPortalShell == null) {
            return;
        }
        if (evaluationsPortalLoaded) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    getClass().getResource("/student-portal.fxml")));
            Parent portal = loader.load();
            evalPortalController = loader.getController();
            evalPortalController.setDashboardEmbedMode(true, () -> {
                showPane(paneDashboard);
                selectNav(btnNavDashboard);
            });
            evalPortalShell.setCenter(portal);
            evaluationsPortalLoaded = true;
        } catch (IOException ex) {
            evaluationsPortalLoaded = false;
            evalPortalController = null;
            new Alert(Alert.AlertType.ERROR,
                    "Impossible de charger le portail évaluations : " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    void onNavResults(ActionEvent event) {
        showPane(paneResults);
        selectNav(btnNavResults);
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
    void onCardResults(MouseEvent event) {
        onNavResults(null);
    }

    @FXML
    void onCardFraud(MouseEvent event) {
        onNavResults(null);
    }

    @FXML
    void onNotifications(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION,
                "No new notifications.\n(Evaluation reminders and anti-cheat alerts will appear here.)").showAndWait();
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
}
