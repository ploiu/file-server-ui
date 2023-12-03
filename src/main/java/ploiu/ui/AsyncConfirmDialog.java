package ploiu.ui;

import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import ploiu.model.AsyncConfirmDialogOptions;

import java.io.IOException;

public class AsyncConfirmDialog extends AnchorPane {
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;
    private final String windowTitle;
    private final PublishSubject<Boolean> answer = PublishSubject.create();

    public AsyncConfirmDialog(AsyncConfirmDialogOptions options) {
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/ConfirmDialog/ConfirmDialog.fxml"));
        this.windowTitle = options.windowTitle();
        loader.setRoot(this);
        loader.setController(this);
        loader.getNamespace().put("confirmText", options.confirmText());
        loader.getNamespace().put("cancelText", options.cancelText());
        loader.getNamespace().put("bodyText", options.bodyText());
        try {
            loader.load();
            registerEvents();
            popup(options.parentWindow());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PublishSubject<Boolean> requestAnswer() {
        return this.answer;
    }

    private void popup(Window window) {
        Platform.runLater(() -> {
            var stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(window);
            stage.setScene(new Scene(this));
            stage.show();
            stage.setTitle(windowTitle);
        });
    }

    private void close() {
        ((Stage) this.getScene().getWindow()).close();
    }

    private void registerEvents() {
        confirmButton.setOnAction(ignored -> {
            answer.onNext(true);
            close();
        });
        cancelButton.setOnAction(ignored -> {
            answer.onNext(false);
            close();
        });
    }
}
