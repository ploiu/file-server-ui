package ploiu.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ploiu.model.LoadingModalOptions;
import ploiu.model.LoadingModalOptions.LoadingType;

import java.io.IOException;

public class LoadingModal extends AnchorPane {

    @FXML
    private ProgressBar progressBar;
    private final LoadingModalOptions options;

    public LoadingModal(LoadingModalOptions options) {
        this.options = options;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/LoadingModal/LoadingModal.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void open() {
        Platform.runLater(() -> {
            var stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(options.getParentWindow());
            stage.setScene(new Scene(this));
            stage.show();
            stage.setTitle("Loading...");
        });
    }

    public void close() {
        Platform.runLater(() -> ((Stage) this.getScene().getWindow()).close());
    }

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        if (options.getType() == LoadingType.DETERMINATE) {
            progressBar.setProgress(0);
        }
    }

    public void updateProgress(double targetValue) {
        Platform.runLater(() -> progressBar.setProgress(targetValue));
    }
}
