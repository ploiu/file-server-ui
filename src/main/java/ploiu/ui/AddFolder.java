package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.EventReceiver;
import ploiu.event.FolderEvent;
import ploiu.model.FolderApi;
import ploiu.model.TextInputDialogOptions;

import java.io.IOException;
import java.util.List;

public class AddFolder extends AnchorPane {

    private final long currentFolderId;

    private final AsyncEventReceiver<FolderApi> receiver;

    public AddFolder(AsyncEventReceiver<FolderApi> receiver, long currentFolderId) {
        super();
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/AddFolder/AddFolder.fxml"));
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
    private void showNewFolderPane(MouseEvent event) {

        if (event.getButton() == MouseButton.PRIMARY) {
            EventReceiver<String> callback = evt -> {
                var folderName = evt.get();
                var dummyFolder = new FolderApi(-1, currentFolderId, folderName, null, List.of(), List.of(), List.of());
                this.receiver.process(new FolderEvent(dummyFolder, FolderEvent.Type.CREATE))
                        .subscribe();
                return true;
            };
            new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), callback, "Create Folder"));
        }
    }
}
