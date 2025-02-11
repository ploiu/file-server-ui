package ploiu.ui;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.pdfsam.rxjavafx.schedulers.JavaFxScheduler;
import ploiu.module.ConfigModule;
import ploiu.module.HttpModule;
import ploiu.service.ApiService;
import ploiu.util.UIUtils;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class App extends Application {
    public static final Injector INJECTOR = Guice.createInjector(new ConfigModule(), new HttpModule());

    @Override
    public void start(Stage stage) {
        UIUtils.init();
        var service = INJECTOR.getInstance(ApiService.class);
        stage.setMinHeight(600);
        stage.setMinWidth(750);
        service.isCompatibleWithServer()
                .subscribeOn(JavaFxScheduler.platform())
                .observeOn(JavaFxScheduler.platform())
                .subscribe(value -> {
                    if (value) {
                        var root = new MainFrame();
                        Scene scene = new Scene(root);
                        stage.setTitle("Ploiu File Server");
                        stage.setScene(scene);
                        stage.show();
                    } else {
                        // show a "good enough" message dialogue telling the user the server version is ahead of the client
                        JOptionPane.showMessageDialog(new JFrame(), "Client is incompatible with the server!", "Outdated Client", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    }
                }, error -> {
                    // show a "good enough" message dialogue telling the user the server version is ahead of the client
                    JOptionPane.showMessageDialog(new JFrame(), error.getMessage(), "Cannot Reach Server", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                });
    }
}
