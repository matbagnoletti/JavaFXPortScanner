package tpsit.javaportscanner.javafxportscanner;

import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

/**
 * Classe JavaFX
 */
public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        
        /* CSS AtlantaFX*/
        Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
        
        /* Caricamento file FXML */
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("JavaPortScanner.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        
        /* Caricamento icona */
        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon.png"))));
        } catch (Exception e){
            System.err.println("Impossibile caricare correttamente l'icona del programma: " + e.getMessage());
        }
        
        /* Impostazioni stage */
        stage.setTitle("JavaFX Port Scanner");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}