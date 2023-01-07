package ploiu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ploiu.client.ApiClient;
import ploiu.module.ConfigModule;
import ploiu.module.HttpModule;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("ui/MainFrame.fxml"));

        Scene scene = new Scene(root);
        //scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setTitle("JavaFX and Gradle");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws Exception {
        var injector = Guice.createInjector(new ConfigModule(), new HttpModule());
        var client = injector.getInstance(ApiClient.class);
        if (!client.isCompatibleWithServer()) {
            System.exit(1);
        }
        launch(args);
    }

}