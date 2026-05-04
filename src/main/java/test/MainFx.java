package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFx extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/evaluation.fxml"));
            var scene = new Scene(loader.load());

            stage.setTitle("EDUCORE · Evaluations");
            stage.setMinWidth(880);
            stage.setMinHeight(600);
            stage.setWidth(1480);
            stage.setHeight(940);
            stage.centerOnScreen();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}


