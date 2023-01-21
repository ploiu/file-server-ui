package ploiu.ui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ploiu.client.ApiClient;
import ploiu.module.ConfigModule;
import ploiu.module.HttpModule;

public class App extends Application {
    public static final Injector INJECTOR = Guice.createInjector(new ConfigModule(), new HttpModule());

    @Override
    public void start(Stage stage) {
        var client = INJECTOR.getInstance(ApiClient.class);
        if (!client.isCompatibleWithServer()) {
            System.exit(1);
        }
        var root = new MainFrame();
        Scene scene = new Scene(root);
        stage.setTitle("File Server UI");
        stage.setScene(scene);
        stage.show();
    }
}
