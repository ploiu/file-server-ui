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
import lombok.Getter;
import ploiu.event.Event;
import ploiu.event.EventReceiver;
import ploiu.model.TextInputDialogOptions;

import java.io.IOException;

public class TextInputDialog extends AnchorPane {
    @FXML
    private TextField textBox;
    @FXML
    private Button actionButton;
    @Getter
    private final String windowTitle;

    public TextInputDialog(TextInputDialogOptions options) {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/TextInputDialog/TextInputDialog.fxml"));
        this.windowTitle = options.windowTitle();
        loader.setRoot(this);
        loader.setController(this);
        loader.getNamespace().put("confirmText", options.confirmText());
        loader.getNamespace().put("bodyText", options.bodyText());
        try {
            loader.load();
            if (options.initialText() != null) {
                textBox.setText(options.initialText());
            }
            registerEvents(options.confirmCallback());
            popup(options.parentWindow());
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
            // TODO ESCAPE = close window
        });
    }
}
