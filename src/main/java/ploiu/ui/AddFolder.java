package ploiu.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import ploiu.client.FolderClient;
import ploiu.event.EventReceiver;
import ploiu.event.FolderEvent;
import ploiu.exception.BadFolderRequestException;
import ploiu.exception.BadFolderResponseException;
import ploiu.model.FolderApi;
import ploiu.model.FolderRequest;
import ploiu.model.TextInputDialogOptions;

import java.io.IOException;
import java.util.Optional;

import static ploiu.util.DialogUtils.showErrorDialog;

public class AddFolder extends AnchorPane {

    private final FolderClient folderClient = App.INJECTOR.getInstance(FolderClient.class);
    private final long currentFolderId;

    private final EventReceiver<FolderApi> receiver;

    public AddFolder(EventReceiver<FolderApi> receiver, long currentFolderId) {
        super();
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/AddFolder/AddFolder.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        this.currentFolderId = currentFolderId;
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.receiver = receiver;
    }

    @FXML
    @SuppressWarnings("unused")
    private void showNewFolderPane(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            EventReceiver<String> callback = evt -> {
                var folderName = evt.get();
                var folderId = currentFolderId == 0 ? null : currentFolderId;
                try {
                    this.receiver.process(new FolderEvent(folderClient.createFolder(new FolderRequest(Optional.empty(), Optional.ofNullable(folderId), folderName)), FolderEvent.Type.CREATE));
                } catch (BadFolderRequestException | BadFolderResponseException e) {
                    showErrorDialog("Failed to create folder. Message is " + e.getMessage(), "Failed to create folder", null);
                }
                return true;
            };
            new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), callback, "Create Folder"));
        }
    }
}
