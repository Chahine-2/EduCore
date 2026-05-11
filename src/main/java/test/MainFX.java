package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            var scene = new Scene(loader.load());

            stage.setTitle("EDUCORE");
            stage.setMinWidth(760);
            stage.setMinHeight(520);
            stage.setWidth(980);
            stage.setHeight(680);
            stage.centerOnScreen();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

}
