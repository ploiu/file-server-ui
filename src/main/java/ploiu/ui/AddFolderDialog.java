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

import java.io.IOException;

public class AddFolderDialog extends AnchorPane {
    @FXML
    private TextField folderTitle;
    @FXML
    private Button createFolder;

    public AddFolderDialog(Window parentWindow, CreateAction createAction) {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/AddFolderDialog/AddFolderDialog.fxml"));
        loader.setRoot(this);
        loader.setController(this);
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
        stage.setTitle("Create Folder");
    }

    private void registerEvents(CreateAction createAction) {
        EventHandler<ActionEvent> submitEvent = ignored -> {
            if (!folderTitle.getText().isBlank()) {
                createAction.handle(folderTitle.getText());
                ((Stage) this.getScene().getWindow()).close();
            }
        };
        createFolder.setOnAction(submitEvent);
        folderTitle.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                submitEvent.handle(null);
            }
        });
    }

    @FunctionalInterface
    public interface CreateAction {
        void handle(String folderName);
    }
}
