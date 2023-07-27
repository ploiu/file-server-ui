package ploiu.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ploiu.model.LoadingModalOptions;

import java.io.IOException;

public class LoadingModal extends AnchorPane {

    public LoadingModal(LoadingModalOptions options) {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/LoadingModal/LoadingModal.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.getNamespace().put("bodyText", options.bodyText());
        try {
            loader.load();
            popup(options.parentWindow());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void popup(Window window) {
        var stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(window);
        stage.setScene(new Scene(this));
        stage.show();
        stage.setTitle("Loading...");
    }

    public void close() {
        ((Stage) this.getScene().getWindow()).close();
    }
}
