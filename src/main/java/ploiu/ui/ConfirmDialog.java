package ploiu.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ploiu.event.Event;
import ploiu.event.EventReceiver;
import ploiu.model.ConfirmDialogOptions;

import java.io.IOException;

public class ConfirmDialog extends AnchorPane {
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;
    private final String windowTitle;

    public ConfirmDialog(ConfirmDialogOptions options) {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/ConfirmDialog/ConfirmDialog.fxml"));
        this.windowTitle = options.windowTitle();
        loader.setRoot(this);
        loader.setController(this);
        loader.getNamespace().put("confirmText", options.confirmText());
        loader.getNamespace().put("cancelText", options.cancelText());
        loader.getNamespace().put("bodyText", options.bodyText());
        try {
            loader.load();
            registerEvents(options.resultAction());
            popup(options.parentWindow());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void popup(Window window) {
        var stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(window);
        stage.setScene(new Scene(this));
        stage.show();
        stage.setTitle(windowTitle);
    }

    private void registerEvents(EventReceiver<Boolean> resultAction) {
        EventHandler<ActionEvent> confirmEvent = ignored -> {
            resultAction.process(new Event<>(true));
            ((Stage) this.getScene().getWindow()).close();
        };
        EventHandler<ActionEvent> cancelEvent = ignored -> {
            resultAction.process(new Event<>(false));
            ((Stage) this.getScene().getWindow()).close();
        };
        confirmButton.setOnAction(confirmEvent);
        cancelButton.setOnAction(cancelEvent);
    }
}
