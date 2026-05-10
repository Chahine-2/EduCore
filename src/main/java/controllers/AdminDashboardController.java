package controllers;

import interfaces.IUtilisateurService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Utilisateur;
import services.UtilisateurService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import java.util.Optional;
import javafx.stage.Modality;
import javafx.scene.control.ButtonType;




public class AdminDashboardController implements Initializable {

    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colStatut;

    private IUtilisateurService service = new UtilisateurService();
    private ObservableList<Utilisateur> utilisateursObservableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Lier les colonnes aux attributs de l'objet Utilisateur
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // 2. Traitement spécial pour les objets imbriqués (Role) et les booléens (Statut)
        colRole.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole().getNomRole()));

        colStatut.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isStatutActif() ? "Actif ✅" : "Suspendu ❌"));

        // 3. Charger les données au démarrage de l'écran
        chargerUtilisateurs();
    }

    @FXML
    void chargerUtilisateurs() {
        // On vide la liste, on appelle le backend, et on remplit la liste
        utilisateursObservableList.clear();
        List<Utilisateur> usersFromDB = service.listerUtilisateurs();
        utilisateursObservableList.addAll(usersFromDB);

        // On injecte la liste dans le tableau JavaFX
        tableUtilisateurs.setItems(utilisateursObservableList);
    }

    @FXML
    void handleAjouterEtudiant(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AjouterEtudiant.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Étudiant");

            // Ceci empêche de cliquer sur le tableau de bord tant que la pop-up est ouverte
            stage.initModality(Modality.APPLICATION_MODAL);

            // On attend que la fenêtre se ferme...
            stage.showAndWait();

            // ... et on rafraîchit le tableau automatiquement !
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

            // Fenêtre modale qui bloque l'arrière-plan
            stage.initModality(Modality.APPLICATION_MODAL);

            // On attend la fermeture de la popup
            stage.showAndWait();

            // Dès que c'est fermé, on rafraîchit le tableau principal !
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
            String nouvelleClasse = result.get().trim().toUpperCase();

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
    void handleDeconnexion(ActionEvent event) {
        try {
            // Retourner à l'écran de Login
            Parent root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 400, 500));
            stage.setTitle("EduCore - Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // --- NOUVEAUX BOUTONS DU MENU CLI ---

    // --- MÉTHODE UTILITAIRE POUR AFFICHER LES ERREURS ---
    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- 3. MODIFIER EMAIL ---
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

            // Pass the selected user to the controller
            ModifierUtilisateurController controller = loader.getController();
            controller.initData(userSelectionne);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Utilisateur");
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.showAndWait();

            // Refresh table when window closes
            chargerUtilisateurs();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- 4. SUPPRIMER UTILISATEUR ---
    @FXML
    void handleSupprimerUtilisateur(ActionEvent event) {
        Utilisateur userSelectionne = tableUtilisateurs.getSelectionModel().getSelectedItem();

        if (userSelectionne == null) {
            afficherAlerte("Action requise", "Veuillez sélectionner un utilisateur à supprimer.", Alert.AlertType.WARNING);
            return;
        }

        // Fenêtre de confirmation pour éviter les suppressions accidentelles
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Attention, action irréversible !");
        alert.setContentText("Voulez-vous vraiment supprimer " + userSelectionne.getPrenom() + " " + userSelectionne.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (service.supprimerUtilisateur(userSelectionne.getId())) {
                afficherAlerte("Succès", "L'utilisateur a été supprimé.", Alert.AlertType.INFORMATION);
                chargerUtilisateurs(); // Rafraîchissement
            } else {
                afficherAlerte("Erreur", "Impossible de supprimer cet utilisateur.", Alert.AlertType.ERROR);
            }
        }
    }

    // --- 5. SUSPENDRE / ACTIVER ---
    @FXML
    void handleSuspendreCompte(ActionEvent event) {
        Utilisateur userSelectionne = tableUtilisateurs.getSelectionModel().getSelectedItem();

        if (userSelectionne == null) {
            afficherAlerte("Action requise", "Veuillez sélectionner un compte à modifier.", Alert.AlertType.WARNING);
            return;
        }

        // On inverse le statut actuel (Si actif -> on veut suspendre. Si suspendu -> on veut activer)
        boolean nouveauStatut = !userSelectionne.isStatutActif();
        String actionTexte = nouveauStatut ? "ACTIVER" : "SUSPENDRE";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Modification du statut");
        alert.setHeaderText("Changement de statut de compte");
        alert.setContentText("Voulez-vous vraiment " + actionTexte + " le compte de " + userSelectionne.getPrenom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (service.changerStatutCompte(userSelectionne.getId(), nouveauStatut)) {
                chargerUtilisateurs(); // Le tableau mettra à jour la colonne Statut instantanément !
            } else {
                afficherAlerte("Erreur", "Impossible de modifier le statut.", Alert.AlertType.ERROR);
            }
        }
    }

    // --- 6. HISTORIQUE DE CONNEXION ---
    @FXML
    void handleHistorique(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Historique.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Sécurité - Journal des connexions");

            // Rend la fenêtre modale (bloque le fond tant qu'elle est ouverte)
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible de charger l'interface d'historique.", Alert.AlertType.ERROR);
        }
    }
}