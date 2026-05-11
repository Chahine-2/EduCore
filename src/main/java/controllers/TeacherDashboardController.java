package controllers;

import interfaces.IUtilisateurService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Etudiant;
import models.Utilisateur;
import services.UtilisateurService;
import utils.UserSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    @FXML private Label lblWelcomeDash;
    @FXML private Label lblWelcomeSubDash;
    @FXML private Label lblDateTimeDash;
    @FXML private Label lblAvatarDash;
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
    @FXML private VBox paneStudents;
    @FXML private VBox paneFraud;
    @FXML private VBox paneStatistics;
    @FXML private VBox paneProfile;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavCourses;
    @FXML private Button btnNavEvaluations;
    @FXML private Button btnNavAbsence;
    @FXML private Button btnNavStudents;
    @FXML private Button btnNavFraud;
    @FXML private Button btnNavStatistics;
    @FXML private Button btnNavProfile;
    @FXML private Button btnNavLogout;

    @FXML private ComboBox<String> comboClasse;
    @FXML private TableView<Etudiant> tableEtudiants;
    @FXML private TableColumn<Etudiant, String> colMatricule;
    @FXML private TableColumn<Etudiant, String> colNom;
    @FXML private TableColumn<Etudiant, String> colPrenom;
    @FXML private TableColumn<Etudiant, String> colStatut;

    private final IUtilisateurService service = new UtilisateurService();
    private final ObservableList<Etudiant> listeEtudiants = FXCollections.observableArrayList();
    private final List<Button> navButtons = new java.util.ArrayList<>();
    private Timeline clockTimeline;

    private boolean evaluationsEmbeddedLoaded;
    private boolean coursesEmbeddedLoaded;

    @FXML
    void initialize() {
        navButtons.add(btnNavDashboard);
        navButtons.add(btnNavCourses);
        navButtons.add(btnNavEvaluations);
        navButtons.add(btnNavAbsence);
        navButtons.add(btnNavStudents);
        navButtons.add(btnNavFraud);
        navButtons.add(btnNavStatistics);
        navButtons.add(btnNavProfile);

        colMatricule.setCellValueFactory(new PropertyValueFactory<>("numeroEtudiant"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutAppel"));

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

        bindDashboardHeaderStrip();

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

    private void bindDashboardHeaderStrip() {
        if (lblWelcomeDash != null && lblWelcome != null) {
            lblWelcomeDash.textProperty().bind(lblWelcome.textProperty());
        }
        if (lblWelcomeSubDash != null && lblWelcomeSub != null) {
            lblWelcomeSubDash.textProperty().bind(lblWelcomeSub.textProperty());
        }
        if (lblDateTimeDash != null && lblDateTime != null) {
            lblDateTimeDash.textProperty().bind(lblDateTime.textProperty());
        }
        if (lblAvatarDash != null && lblAvatarInitials != null) {
            lblAvatarDash.textProperty().bind(lblAvatarInitials.textProperty());
        }
    }

    private void refreshStatistics() {
        lblStatCourses.setText("8");
        lblStatEvaluations.setText("14");
        lblStatStudents.setText("126");
        lblStatFraud.setText("3");
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
        for (VBox p : List.of(paneDashboard, paneCourseMgmt, paneEvalMgmt, paneAbsence,
                paneStudents, paneFraud, paneStatistics, paneProfile)) {
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
            EvaluationController evalCtrl = loader.getController();
            evalCtrl.setTeacherDashboardEmbedMode(true, () -> {
                showPane(paneDashboard);
                selectNav(btnNavDashboard);
            });
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
    void onNavStudents(ActionEvent event) {
        showPane(paneStudents);
        selectNav(btnNavStudents);
    }

    @FXML
    void onNavFraud(ActionEvent event) {
        showPane(paneFraud);
        selectNav(btnNavFraud);
    }

    @FXML
    void onNavStatistics(ActionEvent event) {
        showPane(paneStatistics);
        selectNav(btnNavStatistics);
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
    void onCardStudents(MouseEvent event) {
        onNavStudents(null);
    }

    @FXML
    void onCardFraud(MouseEvent event) {
        onNavFraud(null);
    }

    @FXML
    void onNotifications(ActionEvent event) {
        new Alert(Alert.AlertType.INFORMATION,
                "No critical notifications.\n(Evaluation deadlines and fraud alerts will surface here.)").showAndWait();
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
}
