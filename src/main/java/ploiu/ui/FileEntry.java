package ploiu.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import lombok.Getter;
import ploiu.event.*;
import ploiu.model.*;
import ploiu.util.MimeUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FileEntry extends AnchorPane {
    // cache because creating an image takes a lot of time
    private static final Map<String, Image> MIME_IMAGE_MAPPING = new HashMap<>();

    // load all the icons into memory on application start, instead of on the fly
    static {
        for (var mimeType : MimeUtils.MIME_TYPES) {
            var icon = MimeUtils.getFileIconForMimeType(mimeType);
            MIME_IMAGE_MAPPING.put(mimeType, new Image(icon, 100.25, 76.25, true, true));
        }
    }

    @Getter
    private final FileApi file;
    private final AsyncEventReceiver<FileObject> fileReceiver;
    @FXML
    private ImageView icon;
    @FXML
    private Label fileName;
    @FXML
    private ContextMenu fileMenu;

    public FileEntry(FileApi file, AsyncEventReceiver<FileObject> eventHandler) {
        super();
        this.file = file;
        var loader = new FXMLLoader(getClass().getClassLoader().getResource("ui/components/FileEntry/FileEntry.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
            this.fileName.setText(file.name());
            var image = MIME_IMAGE_MAPPING.get(MimeUtils.determineMimeType(file.name()));
            icon.setImage(image);
            this.fileReceiver = eventHandler;
            Tooltip.install(this, new Tooltip(file.name()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        this.setOnContextMenuRequested(event -> {
            fileMenu.show(this, event.getScreenX(), event.getScreenY());
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void renameItemClicked(ActionEvent event) {
        EventReceiver<String> renameCallback = evt -> {
            var newName = evt.get();
            if (newName != null && !newName.isBlank()) {
                fileReceiver.process(new FileUpdateEvent(new FileApiWithFolder(file.id(), newName, Optional.empty())))
                        .subscribe();
                return true;
            }
            return false;
        };
        new TextInputDialog(new TextInputDialogOptions(getScene().getWindow(), renameCallback, "Rename File").initialText(file.name()));
    }

    @FXML
    @SuppressWarnings("unused")
    private void deleteItemClicked(ActionEvent ignored) {
        EventReceiver<Boolean> dialogCallback = res -> {
            if (res.get()) {
                fileReceiver.process(new FileDeleteEvent(file))
                        .subscribe();
                return true;
            }
            return false;
        };
        new ConfirmDialog(new ConfirmDialogOptions(getScene().getWindow(), dialogCallback, "Are you sure you want to delete this file?"));
    }

    @FXML
    @SuppressWarnings("unused")
    private void saveAsClicked(ActionEvent ignored) {
        var chooser = new DirectoryChooser();
        chooser.setTitle("Save " + file.name() + "...");
        var selectedDir = chooser.showDialog(getScene().getWindow());
        if (selectedDir != null) {
            fileReceiver.process(new FileSaveEvent(file, selectedDir))
                    .subscribe();
        }
    }

    @FXML
    private void onDragDetected(MouseEvent e) {
        var board = startDragAndDrop(TransferMode.MOVE);
        board.setContent(Map.of(DataTypes.FILE, this.file));
        e.consume();
    }
}
