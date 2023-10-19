package ploiu.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import ploiu.event.AsyncEventReceiver;
import ploiu.event.EventReceiver;
import ploiu.event.FileUpdateEvent;
import ploiu.event.FolderEvent;
import ploiu.model.*;
import ploiu.service.DragNDropService;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static ploiu.util.DialogUtils.showErrorDialog;

@Getter
@SuppressWarnings("unused")
public class FolderEntry extends AnchorPane {
    /**
     * the folder backing this UI element
     */
    private final FolderApi folder;
    @FXML
    private ImageView icon;
    @FXML
    private Label folderName;
    @FXML
    private ContextMenu folderMenu;
    private final AsyncEventReceiver<FolderApi> folderReceiver;
    private final AsyncEventReceiver<FileObject> fileReceiver;

    private final DragNDropService dragNDropService = App.INJECTOR.getInstance(DragNDropService.class);

    public FolderEntry(FolderApi folder, AsyncEventReceiver<FolderApi> folderReceiver, AsyncEventReceiver<FileObject> fileReceiver) {
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
            Tooltip.install(this, new Tooltip(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.folderReceiver = folderReceiver;
        this.fileReceiver = fileReceiver;
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
            folderReceiver.process(new FolderEvent(newFolder, FolderEvent.Type.UPDATE))
                    .subscribe();
            return true;
        };
        var dialog = new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), dialogCallback, "Rename Folder"));
    }

    @FXML
    @SuppressWarnings("unused")
    private void deleteItemClicked(ActionEvent event) {
        EventReceiver<String> dialogCallback = res -> {
            // make the user type the folder name they're deleting to confirm
            if (res.get().equals(folderName.getText())) {
                folderReceiver.process(new FolderEvent(folder, FolderEvent.Type.DELETE))
                        .subscribe();
                return true;
            }
            showErrorDialog("[" + res.get() + "] does not match the folder name [" + folderName.getText() + "]", "Failed to delete folder", null);
            return false;
        };
        var dialog = new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), dialogCallback, "Delete")
                .bodyText("Are you sure you want to delete? Type the name of the folder and click Confirm to delete")
                .windowTitle("Confirm Delete?"));
    }

    @FXML
    private void onDragDetected(MouseEvent e) {
        var board = startDragAndDrop(TransferMode.MOVE);
        board.setContent(Map.of(DataTypes.FOLDER, this.folder));
        e.consume();
    }

    @FXML
    private void onDragOver(DragEvent e) {
        e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        e.consume();
    }

    @FXML
    private void onDragDropped(DragEvent event) {
        var board = event.getDragboard();
        if (!event.isConsumed()) {
            if (board.hasFiles()) {
                event.consume();
                dragNDropService.dropFiles(board.getFiles(), folder, getScene().getWindow())
                        .subscribe();
            } else if (board.getContent(DataTypes.FOLDER) instanceof FolderApi droppedFolder) {
                event.consume();
                var newApi = new FolderApi(droppedFolder.id(), folder.id(), droppedFolder.path(), droppedFolder.folders(), droppedFolder.files());
                folderReceiver.process(new FolderEvent(newApi, FolderEvent.Type.UPDATE))
                        .subscribe();
            } else if (board.getContent(DataTypes.FILE) instanceof FileApi droppedFile) {
                event.consume();
                var newApi = new FileApiWithFolder(droppedFile.id(), droppedFile.name(), Optional.of(folder.id()));
                fileReceiver.process(new FileUpdateEvent(newApi))
                        .subscribe();
            }
        }
    }
}
