package ploiu.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import ploiu.event.EventReceiver;
import ploiu.event.FolderEvent;
import ploiu.model.FolderApi;
import ploiu.model.TextInputDialogOptions;

import java.io.IOException;

import static ploiu.util.DialogUtils.showErrorDialog;

@SuppressWarnings("unused")
public class FolderEntry extends AnchorPane {
    /**
     * the folder backing this UI element
     */
    @Getter
    private final FolderApi folder;
    @FXML
    private ImageView icon;
    @FXML
    private Label folderName;
    @FXML
    private ContextMenu folderMenu;

    private final EventReceiver<FolderApi> folderReceiver;

    public FolderEntry(FolderApi folder, EventReceiver<FolderApi> folderReceiver) {
        this.folder = folder;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FolderEntry/FolderEntry.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            // currently api doesn't have a field for name, just full path
            var splitPath = folder.path().split("/");
            var name = splitPath[splitPath.length - 1];
            this.folderName.setText(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.folderReceiver = folderReceiver;
    }

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        this.setOnContextMenuRequested(event -> {
            folderMenu.show(this, event.getScreenX(), event.getScreenY());
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void renameItemClicked(ActionEvent event) {
        EventReceiver<String> dialogCallback = evt -> {
            var newName = evt.get();
            var newFolder = new FolderApi(folder.id(), folder.parentId(), newName, folder.folders(), folder.files());
            folderReceiver.process(new FolderEvent(newFolder, FolderEvent.Type.UPDATE));
            return true;
        };
        var dialog = new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), dialogCallback, "Rename Folder"));
    }

    @FXML
    @SuppressWarnings("unused")
    private void deleteItemClicked(ActionEvent event) {
        EventReceiver<String> dialogCallback = res -> {
            if (res.get().equals(folderName.getText())) {
                return folderReceiver.process(new FolderEvent(folder, FolderEvent.Type.DELETE));
            }
            showErrorDialog("[" + res.get() + "] does not match the folder name [" + folderName.getText() + "]", "Failed to delete folder", null);
            return false;
        };
        var dialog = new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), dialogCallback, "Delete")
                .bodyText("Are you sure you want to delete? Type the name of the folder and click Confirm to delete")
                .windowTitle("Confirm Delete?"));
    }

    @FXML
    private void onDragOver(DragEvent e) {
        var board = e.getDragboard();
        if (board.hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
        e.consume();
    }

    @FXML
    private void onDragDropped(DragEvent event) {
        var board = event.getDragboard();
        if (board.hasFiles()) {
            event.consume();
            var files = board.getFiles();
            for (var file : files) {
                //fileUploadReceiver.process()
            }
        }
    }
}
