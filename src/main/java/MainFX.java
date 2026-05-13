import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.AppStageLayout;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // On charge l'interface de connexion depuis les ressources
        Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));

        primaryStage.setTitle("EduCore - Connexion");
        primaryStage.setScene(new Scene(root, 400, 500)); // Largeur: 400, Hauteur: 500
        primaryStage.setResizable(true);
        primaryStage.show();
        AppStageLayout.maximizeWorkArea(primaryStage);
    }

    public static void main(String[] args) {
        // Lance l'application graphique
        launch(args);
    }
}