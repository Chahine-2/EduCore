import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import models.Chapitre;
import models.Cours;
import controllers.LectureChapitreController;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TestLoadFXML extends Application {
    private static final Logger LOGGER = Logger.getLogger(TestLoadFXML.class.getName());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Cours cours = new Cours();
            cours.setId(1);
            cours.setTitre("Test Cours");

            Chapitre chap = new Chapitre();
            chap.setId(1);
            chap.setOrdre(1);
            chap.setTitre("Test Chap");
            chap.setTypeContenu("video");
            chap.setDureeMinutes(30);
            chap.setDescription("Test desc");

            LectureChapitreController.coursActuel = cours;
            LectureChapitreController.chapitreActuel = chap;
            LectureChapitreController.tousChapitres = Arrays.asList(chap);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LectureChapitre.fxml"));
            loader.load();
            System.out.println("SUCCESS");
        } catch (Exception e) {
            System.err.println("FAILED TO LOAD FXML:");
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du fichier FXML", e);
        } finally {
            Platform.exit();
        }
    }
}
