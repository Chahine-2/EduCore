package utils;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Agrandit la fenêtre sur toute la zone utile de l'écran principal (sous la barre des tâches).
 * <p>Sous Windows, {@link Stage#setMaximized(boolean)} seul est souvent ignoré avant le premier
 * {@link Stage#show()} ou <strong>réinitialisé</strong> juste après {@link Stage#setScene(javafx.scene.Scene)}
 * (ex. passage login → tableau de bord). On impose donc la géométrie via
 * {@link Screen#getVisualBounds()} et on la ré-applique sur les prochains cycles JavaFX
 * (y compris après un court délai pour la fin du layout post-{@code setScene}).
 */
public final class AppStageLayout {

    private AppStageLayout() {
    }

    public static void maximizeWorkArea(Stage stage) {
        if (stage == null) {
            return;
        }
        applyVisualWorkArea(stage);
        Platform.runLater(() -> applyVisualWorkArea(stage));
        Platform.runLater(() -> Platform.runLater(() -> applyVisualWorkArea(stage)));
        PauseTransition later = new PauseTransition(Duration.millis(120));
        later.setOnFinished(e -> applyVisualWorkArea(stage));
        later.play();
    }

    private static void applyVisualWorkArea(Stage stage) {
        Rectangle2D v = Screen.getPrimary().getVisualBounds();
        stage.setMaximized(false);
        stage.setX(v.getMinX());
        stage.setY(v.getMinY());
        stage.setWidth(v.getWidth());
        stage.setHeight(v.getHeight());
    }
}
