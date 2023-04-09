package ploiu.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ploiu.event.Event;
import ploiu.event.EventReceiver;

import java.io.IOException;

public class TextInputDialog extends AnchorPane {
    @FXML
    private TextField textBox;
    @FXML
    private Button actionButton;
    private final String windowTitle;

    public TextInputDialog(Window parentWindow, EventReceiver<String> createAction, String confirmText) {
        this(parentWindow, createAction, confirmText, confirmText);
    }

    public TextInputDialog(Window parentWindow, EventReceiver<String> createAction, String confirmText, String windowTitle) {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/TextInputDialog/TextInputDialog.fxml"));
        this.windowTitle = windowTitle;
        loader.setRoot(this);
        loader.setController(this);
        loader.getNamespace().put("confirmText", confirmText);
        try {
            loader.load();
            registerEvents(createAction);
            popup(parentWindow);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void popup(Window window) {
        var stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(window);
        var scene = new Scene(this);
        stage.setScene(scene);
        stage.show();
        stage.setTitle(windowTitle);
    }

    private void registerEvents(EventReceiver<String> createAction) {
        EventHandler<ActionEvent> submitEvent = ignored -> {
            if (!textBox.getText().isBlank()) {
                createAction.process(new Event<>(textBox.getText()));
                ((Stage) this.getScene().getWindow()).close();
            }
        };
        actionButton.setOnAction(submitEvent);
        textBox.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                submitEvent.handle(null);
            }
        });
    }
}
