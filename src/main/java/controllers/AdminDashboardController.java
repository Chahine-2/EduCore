package controllers;

import interfaces.IUtilisateurService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Hackathon;
import models.Utilisateur;
import services.ServiceHackathon;
import services.UtilisateurService;
import utils.AppStageLayout;
import utils.UserSession;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Admin workspace — layout and styling aligned with teacher/student dashboards ({@code teacher-dashboard.css}).
 */
public class AdminDashboardController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblWelcomeSub;
    @FXML private Label lblDateTime;
    @FXML private Label lblAvatarInitials;
    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colStatut;
    @FXML private Label lbTotalHackathonsAdmin;
    @FXML private Label lbPrixMoyenHackathonsAdmin;

    private final IUtilisateurService service = new UtilisateurService();
    private final ServiceHackathon serviceHackathon = new ServiceHackathon();
    private final ObservableList<Utilisateur> utilisateursObservableList = FXCollections.observableArrayList();
    private Timeline clockTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole().getNomRole()));
        colStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isStatutActif() ? "Actif ✅" : "Suspendu ❌"));

        Utilisateur user = UserSession.getCurrentUser();
        if (user != null) {
            String prenom = safe(user.getPrenom());
            String nom = safe(user.getNom());
            String display = (prenom + " " + nom).trim();
            lblWelcome.setText(display.isEmpty() ? "Administration" : "Bonjour, " + display);
            lblAvatarInitials.setText(initials(prenom, nom));
        } else {
            lblWelcome.setText("Administration");
            lblAvatarInitials.setText("A");
        }
        if (lblWelcomeSub != null) {
            lblWelcomeSub.setText("Gérez les comptes, les classes, le matériel et les hackathons depuis un seul espace.");
        }

        chargerUtilisateurs();
        chargerStatistiquesHackathon();

        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

        Platform.runLater(() -> {
            if (lblWelcome != null && lblWelcome.getScene() != null) {
                Stage st = (Stage) lblWelcome.getScene().getWindow();
                st.setMinWidth(1050);
                st.setMinHeight(640);
                AppStageLayout.maximizeWorkArea(st);
            }
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String initials(String prenom, String nom) {
        String a = prenom.isEmpty() ? "?" : prenom.substring(0, 1);
        String b = nom.isEmpty() ? "" : nom.substring(0, 1);
        return (a + b).toUpperCase(Locale.FRENCH);
    }

    private void updateClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE d MMM yyyy '·' HH:mm", Locale.FRENCH);
        lblDateTime.setText(fmt.format(LocalDateTime.now()));
    }

    @FXML
    void chargerUtilisateurs() {
        utilisateursObservableList.clear();
        utilisateursObservableList.addAll(service.listerUtilisateurs());
        tableUtilisateurs.setItems(utilisateursObservableList);
    }

    private void chargerStatistiquesHackathon() {
        List<Hackathon> hackathons = serviceHackathon.getAll();
        lbTotalHackathonsAdmin.setText(String.valueOf(hackathons.size()));

        double prixMoyen = hackathons.stream()
                .mapToDouble(Hackathon::getPrix)
                .average()
                .orElse(0.0);
        lbPrixMoyenHackathonsAdmin.setText(String.format(Locale.FRENCH, "%.1f DT", prixMoyen));
    }

    @FXML
    void handleAjouterEtudiant(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterEtudiant.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Étudiant");
            stage.initModality(Modality.APPLICATION_MODAL);
            AppStageLayout.maximizeWorkArea(stage);
            stage.showAndWait();
            chargerUtilisateurs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleAjouterProf(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterEnseignant.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Enseignant");
            stage.initModality(Modality.APPLICATION_MODAL);
            AppStageLayout.maximizeWorkArea(stage);
            stage.showAndWait();
            chargerUtilisateurs();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger l'interface d'ajout d'enseignant.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleGestionClasses(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Gestion des Classes");
        dialog.setHeaderText("Ajouter une nouvelle classe");
        dialog.setContentText("Nom de la classe (ex: GL-3) :");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String nouvelleClasse = result.get().trim().toUpperCase(Locale.ROOT);

            if (service.ajouterClasse(nouvelleClasse)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("La classe " + nouvelleClasse + " a été ajoutée !");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Cette classe existe déjà ou une erreur est survenue.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    void handleGestionMateriel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionMateriel.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion du matériel");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            AppStageLayout.maximizeWorkArea(stage);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger l'interface de gestion du matériel.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleStatMateriel(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Statistiques.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Statistiques du matériel");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            AppStageLayout.maximizeWorkArea(stage);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger les statistiques du matériel.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleStatHackathon(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques Hackathon");
            stage.initModality(Modality.APPLICATION_MODAL);
            AppStageLayout.maximizeWorkArea(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger le tableau de bord des statistiques.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleGestionHackathonSection(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionHackathon.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion Hackathon");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            AppStageLayout.maximizeWorkArea(stage);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger la gestion des hackathons.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void handleDeconnexion(ActionEvent event) {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        UserSession.clear();
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/Login.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EduCore - Connexion");
            AppStageLayout.maximizeWorkArea(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void handleModifierEmail(ActionEvent event) {
        Utilisateur userSelectionne = tableUtilisateurs.getSelectionModel().getSelectedItem();

        if (userSelectionne == null) {
            afficherAlerte("Action requise", "Veuillez d'abord sélectionner un utilisateur.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ModifierUtilisateur.fxml"));
            Parent root = loader.load();

            ModifierUtilisateurController controller = loader.getController();
            controller.initData(userSelectionne);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Utilisateur");
            stage.initModality(Modality.APPLICATION_MODAL);
            AppStageLayout.maximizeWorkArea(stage);
            stage.showAndWait();
            chargerUtilisateurs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSupprimerUtilisateur(ActionEvent event) {
        Utilisateur userSelectionne = tableUtilisateurs.getSelectionModel().getSelectedItem();

        if (userSelectionne == null) {
            afficherAlerte("Action requise", "Veuillez sélectionner un utilisateur à supprimer.", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Attention, action irréversible !");
        alert.setContentText("Voulez-vous vraiment supprimer " + userSelectionne.getPrenom() + " " + userSelectionne.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (service.supprimerUtilisateur(userSelectionne.getId())) {
                afficherAlerte("Succès", "L'utilisateur a été supprimé.", Alert.AlertType.INFORMATION);
                chargerUtilisateurs();
            } else {
                afficherAlerte("Erreur", "Impossible de supprimer cet utilisateur.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    void handleSuspendreCompte(ActionEvent event) {
        Utilisateur userSelectionne = tableUtilisateurs.getSelectionModel().getSelectedItem();

        if (userSelectionne == null) {
            afficherAlerte("Action requise", "Veuillez sélectionner un compte à modifier.", Alert.AlertType.WARNING);
            return;
        }

        boolean nouveauStatut = !userSelectionne.isStatutActif();
        String actionTexte = nouveauStatut ? "ACTIVER" : "SUSPENDRE";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Modification du statut");
        alert.setHeaderText("Changement de statut de compte");
        alert.setContentText("Voulez-vous vraiment " + actionTexte + " le compte de " + userSelectionne.getPrenom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (service.changerStatutCompte(userSelectionne.getId(), nouveauStatut)) {
                chargerUtilisateurs();
            } else {
                afficherAlerte("Erreur", "Impossible de modifier le statut.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    void handleHistorique(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Historique.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Sécurité - Journal des connexions");
            stage.initModality(Modality.APPLICATION_MODAL);
            AppStageLayout.maximizeWorkArea(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger l'interface d'historique.", Alert.AlertType.ERROR);
        }
    }
}
