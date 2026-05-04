package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Gestionnaire de navigation centralisé pour éviter les crashes liés à getScene().setRoot()
 */
public class NavigationManager {

    /**
     * Navigue vers une nouvelle page FXML en remplaçant la scène actuelle
     * @param currentScene La scène actuelle
     * @param fxmlPath Le chemin du fichier FXML (ex: "/Accueil.fxml")
     */
    public static void navigateTo(Scene currentScene, String fxmlPath) {
        try {
            System.out.println("🔄 Navigation vers : " + fxmlPath);
            
            if (currentScene == null) {
                System.out.println("❌ ERREUR : La scène est null!");
                throw new IllegalArgumentException("La scène ne peut pas être null");
            }

            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            
            if (loader.getLocation() == null) {
                System.out.println("❌ ERREUR : Le fichier " + fxmlPath + " n'a pas été trouvé!");
                throw new IOException("Fichier FXML introuvable : " + fxmlPath);
            }
            
            Parent root = loader.load();

            if (root == null) {
                System.out.println("❌ ERREUR : Le contenu FXML est null!");
                throw new IOException("Impossible de charger le fichier FXML : " + fxmlPath);
            }

            currentScene.setRoot(root);
            System.out.println("✅ Navigation réussie vers " + fxmlPath);
            
        } catch (IOException e) {
            System.out.println("❌ ERREUR NAVIGATION - IOException");
            System.out.println("    Fichier : " + fxmlPath);
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur de navigation", "IOException : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ ERREUR NAVIGATION - Exception");
            System.out.println("    Type : " + e.getClass().getName());
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur de navigation", e.getClass().getSimpleName() + " : " + e.getMessage());
        }
    }

    /**
     * Ouvre une nouvelle fenêtre (Stage) pour une page FXML
     * @param fxmlPath Le chemin du fichier FXML
     * @param title Le titre de la nouvelle fenêtre
     */
    public static void openNewWindow(String fxmlPath, String title) {
        try {
            System.out.println("🪟 Ouverture d'une nouvelle fenêtre : " + fxmlPath);
            
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            
            if (loader.getLocation() == null) {
                throw new IOException("Fichier FXML introuvable : " + fxmlPath);
            }
            
            Parent root = loader.load();

            if (root == null) {
                throw new IOException("Impossible de charger le fichier FXML : " + fxmlPath);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
            
            System.out.println("✅ Nouvelle fenêtre ouverte : " + title);
        } catch (IOException e) {
            System.out.println("❌ ERREUR OUVERTURE FENETRE - IOException");
            System.out.println("    Fichier : " + fxmlPath);
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur d'ouverture", "IOException : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ ERREUR OUVERTURE FENETRE - Exception");
            System.out.println("    Type : " + e.getClass().getName());
            System.out.println("    Message : " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur d'ouverture", e.getClass().getSimpleName() + " : " + e.getMessage());
        }
    }

    /**
     * Obtient le contrôleur d'un FXML après son chargement
     * @param fxmlPath Le chemin du fichier FXML
     * @return Le contrôleur associé au FXML
     */
    public static Object loadFXMLAndGetController(String fxmlPath) throws IOException {
        System.out.println("📂 Chargement du contrôleur : " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }
    
    private static void showErrorDialog(String title, String message) {
        System.out.println("🔴 Affichage d'une boîte de dialogue d'erreur");
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Une erreur s'est produite");
        alert.setContentText(message + "\n\nL'application peut ne pas fonctionner correctement.\nVérifiez la console pour plus de détails.");
        alert.showAndWait();
    }
}

