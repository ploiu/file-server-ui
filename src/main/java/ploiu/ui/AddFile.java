package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import ploiu.event.Event;
import ploiu.event.EventReceiver;

import java.io.File;
import java.io.IOException;

public class AddFile extends AnchorPane {
    private final EventReceiver<File> receiver;
    private final Long currentFolderId;

    public AddFile(EventReceiver<File> receiver, Long currentFolderId) {
        super();
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/AddFile/AddFile.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        this.receiver = receiver;
        this.currentFolderId = currentFolderId;
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void openFileBrowser(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            var chooser = new FileChooser();
            chooser.setTitle("Upload Files");
            var selectedFiles = chooser.showOpenMultipleDialog(getScene().getWindow());
            for (File selectedFile : selectedFiles) {
                receiver.process(new Event<>(selectedFile));
            }
        }
    }
}