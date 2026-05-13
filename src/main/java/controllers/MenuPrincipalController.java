package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class MenuPrincipalController {

    private void ouvrirFenetre(String fxmlFile, String titre) {
        try {
            URL url = getClass().getClassLoader().getResource(fxmlFile);
            if (url == null) {
                System.out.println("Fichier introuvable : " + fxmlFile);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titre);
            stage.setScene(new Scene(root, 1100, 750));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    @FXML
    public void ouvrirMateriel(ActionEvent e) {
        ouvrirFenetre("GestionMateriel.fxml", "Gestion des Materiels");
    }

    @FXML
    public void ouvrirReservation(ActionEvent e) {
        ouvrirFenetre("GestionReservationMateriel.fxml", "Gestion des Reservations");
    }

    @FXML
    public void ouvrirStatistiques(ActionEvent e) {
        ouvrirFenetre("Statistiques.fxml", "Statistiques");
    }

    @FXML
    public void ouvrirMap(ActionEvent e) {
        ouvrirFenetre("Map.fxml", "Localisation du Centre");
    }

    @FXML
    public void quitter(ActionEvent e) {
        Platform.exit();
    }
}