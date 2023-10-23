package ploiu.ui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ploiu.client.ApiClient;
import ploiu.exception.ServerUnavailableException;
import ploiu.module.ConfigModule;
import ploiu.module.HttpModule;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.HeadlessException;

public class App extends Application {
    public static final Injector INJECTOR = Guice.createInjector(new ConfigModule(), new HttpModule());

    @Override
    public void start(Stage stage) {
        var client = INJECTOR.getInstance(ApiClient.class);
        stage.setMinHeight(600);
        stage.setMinWidth(750);
        try {
            if (!client.isCompatibleWithServer()) {
                // show a "good enough" message dialogue telling the user the server version is ahead of the client
                JOptionPane.showMessageDialog(new JFrame(), "Client is incompatible with the server!", "Outdated Client", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        } catch (Exception e) {
            // show a "good enough" message dialogue telling the user the server version is ahead of the client
            JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Cannot Reach Server", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        var root = new MainFrame();
        Scene scene = new Scene(root);
        stage.setTitle("File Server UI");
        stage.setScene(scene);
        stage.show();
    }
}
